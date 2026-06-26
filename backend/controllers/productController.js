const Product = require('../models/Product');
const { analyzeIngredients } = require('../config/claude');
const { analyzeIngredientsFallback } = require('../config/fallbackAnalyzer');
const { fetchFromOpenFoodFacts } = require('../services/openFoodFactsService');
const { searchFoodUSDA } = require('../services/usdaService');

// Cache TTL: 7 days — AI analysis doesn't change per product
const CACHE_TTL_MS = 7 * 24 * 60 * 60 * 1000;

// @route GET /api/product/:barcode
const getProduct = async (req, res, next) => {
  try {
    const { barcode } = req.params;
    if (!barcode || barcode.trim() === '') {
      return res.status(400).json({ success: false, message: 'Barcode is required' });
    }

    console.log(`🔍 Product lookup: barcode=${barcode}`);

    // ── 1. Return from MongoDB cache if still fresh ──────────────────────────
    const cached = await Product.findOne({ barcode });
    if (cached && cached.aiAnalysis && (Date.now() - cached.cachedAt.getTime()) < CACHE_TTL_MS) {
      console.log(`✅ Cache hit (AI included): ${cached.name}`);
      return res.json({ success: true, data: formatProduct(cached, req.user) });
    }

    // ── 2. Fetch product data from Open Food Facts ───────────────────────────
    console.log('🌍 Trying Open Food Facts...');
    let productData = await fetchFromOpenFoodFacts(barcode);

    // ── 3. Fallback to USDA ──────────────────────────────────────────────────
    if (!productData) {
      console.log('⚠️  Not in Open Food Facts — trying USDA...');
      productData = await searchFoodUSDA(barcode);
    }

    if (!productData) {
      console.log(`❌ Product not found in any database: ${barcode}`);
      return res.status(404).json({
        success: false,
        message: 'Product not found. Try scanning another barcode.'
      });
    }

    console.log(`📦 Found: ${productData.name}`);

    // ── 4. Build user preferences string ────────────────────────────────────
    const userPrefs = [];
    if (req.user && req.user.preferences) {
      if (req.user.preferences.isVegan)           userPrefs.push('vegan');
      if (req.user.preferences.isGlutenFree)      userPrefs.push('gluten-free');
      if (req.user.preferences.isDiabeticFriendly) userPrefs.push('diabetic');
      if (req.user.preferences.isKeto)            userPrefs.push('keto');
    }
    const userPreferencesText = userPrefs.length > 0 ? userPrefs.join(', ') : 'None specified';

    // ── 5. Run Claude AI analysis (analyzeIngredients) ──────────────────────
    let aiAnalysis = null;
    try {
      console.log('🤖 Sending to Claude AI for analysis...');
      aiAnalysis = await analyzeIngredients(
        productData.name,
        productData.brand,
        productData.ingredients,
        productData.nutrition,
        userPreferencesText
      );
      console.log(`🤖 Claude analysis done — health score: ${aiAnalysis.health_score}/10`);
    } catch (aiError) {
      console.error('⚠️  Claude AI error (falling back to basic analyzer):', aiError.message);
      aiAnalysis = analyzeIngredientsFallback(
        productData.name,
        productData.brand,
        productData.ingredients,
        productData.nutrition,
        userPreferencesText
      );
    }

    // ── 6. Upsert into MongoDB (barcode is the unique key) ───────────────────
    const product = await Product.findOneAndUpdate(
      { barcode },
      {
        barcode,
        name:        productData.name,
        brand:       productData.brand,
        category:    productData.category || '',
        quantity:    productData.quantity,
        image:       productData.imageUrl,
        ingredients: productData.ingredients,
        nutriScore:  productData.nutriScore,
        nutrition:   productData.nutrition,
        aiAnalysis,
        cachedAt:    new Date()
      },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );

    return res.json({ success: true, data: formatProduct(product, req.user) });

  } catch (error) {
    console.error('❌ Product controller error:', error.message);
    next(error);
  }
};

/**
 * Formats a MongoDB Product document into the API response shape.
 * Maps new flat analyzeIngredients fields to the legacy keys the Android app uses.
 */
const formatProduct = (product, user) => {
  let aiAnalysis = null;

  if (product.aiAnalysis) {
    const a = product.aiAnalysis;

    // Filter personalized warnings to only those relevant to this user
    let personalizedWarnings = a.personalized_warnings || [];
    if (user && user.preferences) {
      const activePrefs = [];
      if (user.preferences.isVegan)            activePrefs.push('vegan');
      if (user.preferences.isGlutenFree)       activePrefs.push('gluten-free');
      if (user.preferences.isDiabeticFriendly) activePrefs.push('diabetic');
      if (user.preferences.isKeto)             activePrefs.push('keto');

      if (activePrefs.length > 0) {
        personalizedWarnings = personalizedWarnings.filter(w =>
          activePrefs.some(pref =>
            (w.applies_to || '').toLowerCase().includes(pref)
          )
        );
      }
    }

    // Build the unified response that satisfies both the new structure
    // and the Android AiAnalysisDto legacy fields
    aiAnalysis = {
      // ── New flat fields (full detail for future UI updates) ──
      health_score:         a.health_score || 0,
      score_label:          a.score_label || '',
      score_reason:         a.score_reason || '',
      harmful_ingredients:  (a.harmful_ingredients || []).map(h => ({
        name:       h.name || '',
        reason:     h.body_impact || h.what_it_is || '',   // mapped to legacy "reason"
        risk_level: h.risk_level || '',
        what_it_is: h.what_it_is || '',
        body_impact: h.body_impact || '',
        banned_in:  h.banned_in || [],
        avoid_if:   h.avoid_if || ''
      })),
      safe_ingredients:     a.safe_ingredients || [],
      additives:            (a.additives || []).map(ad => ({
        code:        ad.code || '',
        name:        ad.name || '',
        purpose:     ad.purpose || '',
        risk:        ad.risk || '',
        note:        ad.explanation || ad.note || ''        // mapped to legacy "note"
      })),
      nutrition_flags:      a.nutrition_flags || [],
      allergens_detected:   a.allergens || [],             // legacy key
      allergens:            a.allergens || [],
      personalized_warnings: personalizedWarnings,
      positive_ingredients: a.positive_ingredients || [],
      final_verdict:        a.final_verdict || '',
      better_alternative:   a.better_alternative || '',

      // ── Legacy Android AiAnalysisDto compatibility keys ─────
      summary:          a.score_reason || '',
      recommendation:   a.final_verdict || '',
      neutral_ingredients: []
    };
  }

  return {
    _id:         product._id,
    barcode:     product.barcode,
    name:        product.name,
    brand:       product.brand,
    category:    product.category || '',
    quantity:    product.quantity,
    image:       product.image,
    ingredients: product.ingredients,
    nutriScore:  product.nutriScore,
    nutrition:   product.nutrition,
    aiAnalysis,
    cachedAt:    product.cachedAt?.getTime() || 0
  };
};

module.exports = { getProduct };

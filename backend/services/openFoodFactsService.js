const axios = require('axios');

const BASE_URL = process.env.OFF_BASE_URL || 'https://world.openfoodfacts.org/api/v2';

/**
 * Fetch product data from Open Food Facts API
 * @param {string} barcode
 * @returns {object|null} Normalized product data
 */
const fetchFromOpenFoodFacts = async (barcode) => {
  try {
    const response = await axios.get(`${BASE_URL}/product/${barcode}`, {
      timeout: 10000,
      headers: {
        'User-Agent': 'ScanSafe/1.0 (health food scanner app)'
      }
    });

    if (response.data.status !== 1 || !response.data.product) {
      return null;
    }

    const p = response.data.product;

    // Parse nutrition (per 100g)
    const nutriments = p.nutriments || {};
    const nutrition = {
      calories: nutriments['energy-kcal_100g'] || nutriments['energy_100g'] / 4.184 || 0,
      protein: nutriments['proteins_100g'] || 0,
      carbs: nutriments['carbohydrates_100g'] || 0,
      fat: nutriments['fat_100g'] || 0,
      sugar: nutriments['sugars_100g'] || 0,
      fiber: nutriments['fiber_100g'] || 0,
      sodium: nutriments['sodium_100g'] ? nutriments['sodium_100g'] * 1000 : 0 // convert to mg
    };

    return {
      name: p.product_name || p.product_name_en || 'Unknown Product',
      brand: p.brands || '',
      quantity: p.quantity || '',
      imageUrl: p.image_url || p.image_front_url || '',
      ingredients: p.ingredients_text || p.ingredients_text_en || '',
      nutriScore: (p.nutriscore_grade || '').toUpperCase(),
      category: p.categories || '',
      nutrition
    };
  } catch (error) {
    console.error('OpenFoodFacts error:', error.message);
    return null;
  }
};

module.exports = { fetchFromOpenFoodFacts };

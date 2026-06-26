/**
 * Fallback analyzer when Claude AI is unavailable (e.g. invalid API key).
 * Uses rule-based logic to analyze ingredients and nutrition.
 */

const analyzeIngredientsFallback = (productName, brand, ingredientsText, nutritionJson, userPreferences) => {
  let score = 8; 
  const harmfulIngredients = [];
  const safeIngredients = [];
  const additives = [];
  const nutritionFlags = [];
  const personalizedWarnings = [];
  const positiveIngredients = [];
  
  const ingredientsLower = (ingredientsText || '').toLowerCase();
  let consumptionAdvice = "";
  let harmAnalysis = [];
  let benefitsAnalysis = [];

  // 1. Analyze Nutrition
  if (nutritionJson) {
    // Sugar analysis
    const sugar = nutritionJson.sugar || nutritionJson.sugars || 0;
    if (sugar > 22.5) {
      score -= 3;
      harmfulIngredients.push({
        name: "High Sugar Content",
        what_it_is: "Simple carbohydrates added for sweetness.",
        body_impact: "Causes rapid blood sugar spikes, leading to energy crashes. Long-term consumption is linked to insulin resistance, type 2 diabetes, and fat accumulation around the liver.",
        risk_level: "High",
        banned_in: [],
        avoid_if: "Diabetics, people aiming for weight loss"
      });
      harmAnalysis.push(`With ${sugar}g of sugar per 100g, this is dangerously high. The WHO recommends no more than 25g of added sugar per day. Consuming just 50g of this product exceeds your entire daily limit.`);
      consumptionAdvice = "Strictly limit consumption to very small occasional portions (e.g., less than 20g).";
    } else if (sugar > 10) {
      score -= 1;
      harmAnalysis.push("Contains moderate sugar levels. Monitor your overall daily intake.");
    }

    // Fat analysis
    const fat = nutritionJson.fat || 0;
    if (fat > 17.5) {
      score -= 2;
      harmfulIngredients.push({
        name: "High Fat Content",
        what_it_is: "Concentrated source of calories, often saturated fats in processed foods.",
        body_impact: "High intake of saturated fats raises LDL (bad) cholesterol, increasing the risk of heart disease and stroke.",
        risk_level: "Medium",
        banned_in: [],
        avoid_if: "People with cardiovascular issues"
      });
      harmAnalysis.push(`High fat content (${fat}g per 100g) makes this very calorie-dense and heavy on the cardiovascular system if eaten regularly.`);
    }

    // Sodium analysis
    const sodium = nutritionJson.sodium || 0;
    if (sodium > 600 || sodium > 1.5) { // Handling both mg or g depending on API
      score -= 2;
      harmfulIngredients.push({
        name: "High Sodium / Salt",
        what_it_is: "Mineral compound used for flavor and preservation.",
        body_impact: "Forces the heart to work harder to pump blood, leading to high blood pressure, and strains the kidneys.",
        risk_level: "High",
        banned_in: [],
        avoid_if: "People with hypertension"
      });
      harmAnalysis.push("High sodium levels can elevate blood pressure. Drink plenty of water if consumed.");
    }

    // Protein benefits
    const protein = nutritionJson.protein || 0;
    if (protein > 15) {
      score += 1;
      positiveIngredients.push({ name: "High Protein", benefit: "Excellent for muscle repair, satiety, and maintaining lean body mass." });
      benefitsAnalysis.push("Good source of protein, making it somewhat filling.");
    }

    // Fiber benefits
    const fiber = nutritionJson.fiber || 0;
    if (fiber > 5) {
      score += 1;
      positiveIngredients.push({ name: "Dietary Fiber", benefit: "Promotes healthy digestion and helps stabilize blood sugar levels." });
      benefitsAnalysis.push("Contains beneficial fiber for gut health.");
    }
  }

  // 2. Analyze Ingredients Text
  if (ingredientsLower) {
    if (ingredientsLower.includes('palm oil') || ingredientsLower.includes('palm fat')) {
      score -= 1;
      harmfulIngredients.push({
        name: "Palm Oil",
        what_it_is: "A highly processed vegetable oil.",
        body_impact: "Rich in saturated fats that can negatively impact heart health and cholesterol levels.",
        risk_level: "Medium",
        banned_in: [],
        avoid_if: "People monitoring cholesterol"
      });
    }

    if (ingredientsLower.includes('artificial color') || ingredientsLower.match(/e1[0-9]{2}/)) {
      score -= 1;
      additives.push({
        code: "Artificial Colors",
        name: "Food Coloring",
        purpose: "Visual enhancement",
        risk: "Caution",
        explanation: "Some food dyes are linked to allergic reactions and hyperactivity in sensitive children."
      });
    }

    if (ingredientsLower.includes('emulsifier') || ingredientsLower.match(/e4[0-9]{2}/)) {
      additives.push({
        code: "Emulsifiers",
        name: "Texture Modifiers",
        purpose: "Prevents separation of ingredients",
        risk: "Caution",
        explanation: "Heavy consumption of artificial emulsifiers can disrupt the healthy bacteria in your gut microbiome."
      });
    }
    
    if (ingredientsLower.includes('cocoa') || ingredientsLower.includes('cacao')) {
      positiveIngredients.push({ name: "Cocoa", benefit: "Contains antioxidants and flavonoids that support heart health and mood." });
      benefitsAnalysis.push("Contains real cocoa which offers some antioxidant benefits.");
    }
  }

  // 3. Formulate Final Verdict and Reason
  score = Math.max(1, Math.min(10, score));

  let scoreLabel = "Moderate";
  if (score >= 8) {
    scoreLabel = "Excellent";
    if (!consumptionAdvice) consumptionAdvice = "Safe for regular consumption as part of a balanced diet.";
  } else if (score >= 6) {
    scoreLabel = "Good";
    if (!consumptionAdvice) consumptionAdvice = "Consume in moderation (1 standard serving).";
  } else if (score >= 4) {
    scoreLabel = "Poor";
    if (!consumptionAdvice) consumptionAdvice = "Limit to small, occasional treats.";
  } else {
    scoreLabel = "Dangerous";
    if (!consumptionAdvice) consumptionAdvice = "Avoid entirely, or consume very strictly measured micro-portions.";
  }

  const harmText = harmAnalysis.length > 0 ? harmAnalysis.join(" ") : "No major health hazards detected.";
  const benefitText = benefitsAnalysis.length > 0 ? benefitsAnalysis.join(" ") : "Minimal nutritional benefits.";

  const scoreReason = `Score: ${score}/10. ${harmAnalysis.length > 0 ? harmAnalysis[0] : "This product's ingredients define its health impact."} Recommended Amount: ${consumptionAdvice}`;
  
  const finalVerdict = `Why to consume: ${benefitText}\n\nWhy NOT to consume: ${harmText}\n\nAmount to consume: ${consumptionAdvice}`;

  return {
    health_score: score,
    score_label: scoreLabel,
    score_reason: `Recommended amount: ${consumptionAdvice} ${harmAnalysis.length > 0 ? harmAnalysis[0] : ''}`.trim(),
    harmful_ingredients: harmfulIngredients,
    safe_ingredients: safeIngredients,
    additives: additives,
    nutrition_flags: nutritionFlags,
    allergens: [],
    personalized_warnings: personalizedWarnings,
    positive_ingredients: positiveIngredients,
    final_verdict: finalVerdict,
    better_alternative: score <= 5 ? "Look for whole food alternatives without added sugars and heavy processing." : "This is a decent choice, but always check for fresh alternatives."
  };
};

module.exports = { analyzeIngredientsFallback };

module.exports = { analyzeIngredientsFallback };

const axios = require('axios');

const BASE_URL = process.env.USDA_BASE_URL || 'https://api.nal.usda.gov/fdc/v1';
const API_KEY = process.env.USDA_API_KEY;

/**
 * Search USDA FoodData Central for a food by name
 * @param {string} query
 * @returns {object|null} Normalized product data
 */
const searchFoodUSDA = async (query) => {
  if (!API_KEY) {
    console.warn('USDA_API_KEY not set — USDA lookup disabled');
    return null;
  }
  try {
    const response = await axios.get(`${BASE_URL}/foods/search`, {
      params: { query, api_key: API_KEY, pageSize: 1 },
      timeout: 10000
    });

    const food = response.data.foods?.[0];
    if (!food) return null;

    const getNutrient = (id) => food.foodNutrients?.find(n => n.nutrientId === id)?.value || 0;

    return {
      name: food.description,
      brand: food.brandOwner || '',
      quantity: '',
      imageUrl: '',
      ingredients: food.ingredients || '',
      nutriScore: '',
      category: food.foodCategory || '',
      nutrition: {
        calories: getNutrient(1008),   // Energy kcal
        protein: getNutrient(1003),    // Protein
        carbs: getNutrient(1005),      // Carbohydrates
        fat: getNutrient(1004),        // Total fat
        sugar: getNutrient(2000),      // Sugars
        fiber: getNutrient(1079),      // Fiber
        sodium: getNutrient(1093)      // Sodium
      }
    };
  } catch (error) {
    console.error('USDA error:', error.message);
    return null;
  }
};

module.exports = { searchFoodUSDA };

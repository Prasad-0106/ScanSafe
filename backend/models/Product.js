const mongoose = require('mongoose');

const nutritionSchema = new mongoose.Schema({
  calories: { type: Number, default: 0 },
  protein: { type: Number, default: 0 },
  carbs: { type: Number, default: 0 },
  fat: { type: Number, default: 0 },
  sugar: { type: Number, default: 0 },
  fiber: { type: Number, default: 0 },
  sodium: { type: Number, default: 0 }
}, { _id: false });

const harmfulIngredientSchema = new mongoose.Schema({
  name: String,
  reason: String,
  risk_level: String
}, { _id: false });

const additiveSchema = new mongoose.Schema({
  code: String,
  name: String,
  risk: String,
  note: String
}, { _id: false });

const aiAnalysisSchema = new mongoose.Schema({
  health_score: { type: Number, min: 1, max: 10 },
  score_label: String,
  summary: String,
  harmful_ingredients: [harmfulIngredientSchema],
  safe_ingredients: [String],
  neutral_ingredients: [String],
  allergens_detected: [String],
  additives: [additiveSchema],
  recommendation: String
}, { _id: false });

const productSchema = new mongoose.Schema({
  barcode: { type: String, required: true, unique: true, index: true },
  name: { type: String, required: true, trim: true },
  brand: { type: String, default: '', trim: true },
  category: { type: String, default: '', trim: true },
  quantity: { type: String, default: '' },
  image: { type: String, default: '' },
  ingredients: { type: String, default: '' },
  nutriScore: { type: String, default: '' },
  nutrition: { type: nutritionSchema, default: () => ({}) },
  aiAnalysis: { type: mongoose.Schema.Types.Mixed, default: null },
  cachedAt: { type: Date, default: Date.now }
}, { timestamps: true });

// barcode already indexed via unique:true on the field above
productSchema.index({ cachedAt: 1 });

module.exports = mongoose.model('Product', productSchema);

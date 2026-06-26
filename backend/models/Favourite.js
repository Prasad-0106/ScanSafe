const mongoose = require('mongoose');

const favouriteSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  productId: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
  savedAt: { type: Date, default: Date.now }
}, { timestamps: false });

// Compound index to prevent duplicates
favouriteSchema.index({ userId: 1, productId: 1 }, { unique: true });

module.exports = mongoose.model('Favourite', favouriteSchema);

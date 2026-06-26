const ScanHistory = require('../models/ScanHistory');
const Product = require('../models/Product');
const User = require('../models/User');

// @route GET /api/history
const getHistory = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const skip = (page - 1) * limit;

    const history = await ScanHistory.find({ userId: req.user._id })
      .sort({ scannedAt: -1 })
      .skip(skip)
      .limit(limit)
      .populate('productId');

    const formatted = history.map(h => ({
      _id: h._id,
      product: h.productId,
      scannedAt: h.scannedAt?.getTime() || 0
    }));

    res.json({ success: true, data: formatted });
  } catch (error) {
    next(error);
  }
};

// @route POST /api/history
const saveHistory = async (req, res, next) => {
  try {
    const { barcode } = req.body;
    if (!barcode) return res.status(400).json({ success: false, message: 'Barcode is required' });

    const product = await Product.findOne({ barcode });
    if (!product) return res.status(404).json({ success: false, message: 'Product not found. Please fetch it first.' });

    let historyEntry = await ScanHistory.findOne({ userId: req.user._id, productId: product._id });
    if (historyEntry) {
      historyEntry.scannedAt = new Date();
      await historyEntry.save();
    } else {
      historyEntry = await ScanHistory.create({
        userId: req.user._id,
        productId: product._id,
        scannedAt: new Date()
      });
    }

    // Update user stats
    const allHistory = await ScanHistory.find({ userId: req.user._id }).populate('productId');
    const totalScans = allHistory.length;
    const scoresWithAnalysis = allHistory.filter(h => h.productId?.aiAnalysis?.health_score);
    const averageScore = scoresWithAnalysis.length > 0
      ? scoresWithAnalysis.reduce((sum, h) => sum + h.productId.aiAnalysis.health_score, 0) / scoresWithAnalysis.length
      : 0;
    await User.findByIdAndUpdate(req.user._id, { totalScans, averageScore });

    await historyEntry.populate('productId');
    res.status(201).json({
      success: true,
      data: {
        _id: historyEntry._id,
        product: historyEntry.productId,
        scannedAt: historyEntry.scannedAt.getTime()
      }
    });
  } catch (error) {
    next(error);
  }
};

// @route DELETE /api/history/:id
const deleteHistory = async (req, res, next) => {
  try {
    const entry = await ScanHistory.findOneAndDelete({ _id: req.params.id, userId: req.user._id });
    if (!entry) return res.status(404).json({ success: false, message: 'History entry not found' });
    res.json({ success: true, message: 'History entry deleted' });
  } catch (error) {
    next(error);
  }
};

module.exports = { getHistory, saveHistory, deleteHistory };

const Favourite = require('../models/Favourite');
const Product = require('../models/Product');

// @route GET /api/favourites
const getFavourites = async (req, res, next) => {
  try {
    const favourites = await Favourite.find({ userId: req.user._id })
      .sort({ savedAt: -1 })
      .populate('productId');
    const formatted = favourites.map(f => ({
      _id: f._id,
      product: f.productId,
      savedAt: f.savedAt?.getTime() || 0
    }));
    res.json({ success: true, data: formatted });
  } catch (error) { next(error); }
};

// @route POST /api/favourites
const addFavourite = async (req, res, next) => {
  try {
    const { barcode } = req.body;
    if (!barcode) return res.status(400).json({ success: false, message: 'Barcode is required' });
    const product = await Product.findOne({ barcode });
    if (!product) return res.status(404).json({ success: false, message: 'Product not found' });

    const favourite = await Favourite.findOneAndUpdate(
      { userId: req.user._id, productId: product._id },
      { userId: req.user._id, productId: product._id, savedAt: new Date() },
      { upsert: true, new: true }
    );
    await favourite.populate('productId');
    res.status(201).json({
      success: true,
      data: { _id: favourite._id, product: favourite.productId, savedAt: favourite.savedAt.getTime() }
    });
  } catch (error) { next(error); }
};

// @route DELETE /api/favourites/:id
const removeFavourite = async (req, res, next) => {
  try {
    const fav = await Favourite.findOneAndDelete({ _id: req.params.id, userId: req.user._id });
    if (!fav) return res.status(404).json({ success: false, message: 'Favourite not found' });
    res.json({ success: true, message: 'Removed from favourites' });
  } catch (error) { next(error); }
};

module.exports = { getFavourites, addFavourite, removeFavourite };

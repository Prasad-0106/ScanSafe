const User = require('../models/User');

// @route GET /api/user/profile
const getProfile = async (req, res, next) => {
  try {
    const user = await User.findById(req.user._id);
    res.json({ success: true, data: user });
  } catch (error) { next(error); }
};

// @route PUT /api/user/profile
const updateProfile = async (req, res, next) => {
  try {
    const { name } = req.body;
    if (!name?.trim()) return res.status(400).json({ success: false, message: 'Name is required' });
    const user = await User.findByIdAndUpdate(req.user._id, { name: name.trim() }, { new: true, runValidators: true });
    res.json({ success: true, data: user });
  } catch (error) { next(error); }
};

// @route PUT /api/user/preferences
const updatePreferences = async (req, res, next) => {
  try {
    const { preferences } = req.body;
    if (!preferences) return res.status(400).json({ success: false, message: 'Preferences object required' });
    const user = await User.findByIdAndUpdate(
      req.user._id,
      { preferences },
      { new: true, runValidators: true }
    );
    res.json({ success: true, data: user });
  } catch (error) { next(error); }
};

module.exports = { getProfile, updateProfile, updatePreferences };

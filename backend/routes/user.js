const express = require('express');
const router = express.Router();
const { getProfile, updateProfile, updatePreferences } = require('../controllers/userController');
const { protect } = require('../middleware/auth');

router.get('/profile', protect, getProfile);
router.put('/profile', protect, updateProfile);
router.put('/preferences', protect, updatePreferences);

module.exports = router;

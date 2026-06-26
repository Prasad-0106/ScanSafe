const express = require('express');
const router = express.Router();
const { getFavourites, addFavourite, removeFavourite } = require('../controllers/favouritesController');
const { protect } = require('../middleware/auth');

router.get('/', protect, getFavourites);
router.post('/', protect, addFavourite);
router.delete('/:id', protect, removeFavourite);

module.exports = router;

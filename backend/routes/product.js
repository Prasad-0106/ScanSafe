const express = require('express');
const router = express.Router();
const { getProduct } = require('../controllers/productController');
const { protect } = require('../middleware/auth');

router.get('/:barcode', protect, getProduct);

module.exports = router;

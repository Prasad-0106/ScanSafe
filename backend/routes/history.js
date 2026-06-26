const express = require('express');
const router = express.Router();
const { getHistory, saveHistory, deleteHistory } = require('../controllers/historyController');
const { protect } = require('../middleware/auth');

router.get('/', protect, getHistory);
router.post('/', protect, saveHistory);
router.delete('/:id', protect, deleteHistory);

module.exports = router;

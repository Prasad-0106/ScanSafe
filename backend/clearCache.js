const mongoose = require('mongoose');
const dotenv = require('dotenv');
const Product = require('./models/Product');

dotenv.config();

mongoose.connect(process.env.MONGODB_URI)
  .then(async () => {
    console.log('MongoDB Connected');
    await Product.deleteMany({});
    console.log('Cache cleared');
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });

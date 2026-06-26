const request = require('supertest');
const app = require('../server');
const mongoose = require('mongoose');
const User = require('../models/User');

describe('Auth API', () => {
  beforeAll(async () => {
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/scansafe_test');
  });

  afterAll(async () => {
    await User.deleteMany({ email: /test.*@example\.com/ });
    await mongoose.disconnect();
  });

  describe('POST /api/auth/register', () => {
    it('should register a new user and return token', async () => {
      const res = await request(app)
        .post('/api/auth/register')
        .send({ name: 'Test User', email: 'test_reg@example.com', password: 'password123' });
      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data.token).toBeDefined();
      expect(res.body.data.user.email).toBe('test_reg@example.com');
    });

    it('should reject duplicate email', async () => {
      await request(app)
        .post('/api/auth/register')
        .send({ name: 'User2', email: 'test_dup@example.com', password: 'password123' });
      const res = await request(app)
        .post('/api/auth/register')
        .send({ name: 'User3', email: 'test_dup@example.com', password: 'password123' });
      expect(res.status).toBe(409);
    });

    it('should reject missing fields', async () => {
      const res = await request(app)
        .post('/api/auth/register')
        .send({ email: 'nope@example.com' });
      expect(res.status).toBe(400);
    });
  });

  describe('POST /api/auth/login', () => {
    it('should login with correct credentials', async () => {
      await request(app)
        .post('/api/auth/register')
        .send({ name: 'Login Test', email: 'test_login@example.com', password: 'securepass1' });
      const res = await request(app)
        .post('/api/auth/login')
        .send({ email: 'test_login@example.com', password: 'securepass1' });
      expect(res.status).toBe(200);
      expect(res.body.data.token).toBeDefined();
    });

    it('should reject wrong password', async () => {
      const res = await request(app)
        .post('/api/auth/login')
        .send({ email: 'test_login@example.com', password: 'wrongpassword' });
      expect(res.status).toBe(401);
    });
  });
});

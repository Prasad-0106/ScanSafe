const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { verifyFirebaseToken, verifyGoogleIdToken } = require('../config/firebase');
const { otpStore, generateOTP, sendOTP } = require('../services/emailService');

const generateTokens = (userId) => {
  const token = jwt.sign({ id: userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '7d'
  });
  const refreshToken = jwt.sign({ id: userId }, process.env.JWT_REFRESH_SECRET, {
    expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '30d'
  });
  return { token, refreshToken };
};

// @route POST /api/auth/register
const register = async (req, res, next) => {
  try {
    const { name, email, password } = req.body;
    console.log('📝 Register attempt:', { name, email, passwordLength: password?.length });

    if (!name || !email || !password) {
      return res.status(400).json({ success: false, message: 'Name, email, and password are required' });
    }
    const existing = await User.findOne({ email: email.toLowerCase() });
    if (existing) {
      return res.status(409).json({ success: false, message: 'Email already in use' });
    }
    const user = await User.create({ name, email: email.toLowerCase(), passwordHash: password });
    const { token, refreshToken } = generateTokens(user._id);
    console.log('✅ User registered:', user.email);
    res.status(201).json({
      success: true,
      data: { token, refreshToken, user }
    });
  } catch (error) {
    console.error('❌ Register error:', error.message, error.errors || '');
    next(error);
  }
};

// @route POST /api/auth/login
const login = async (req, res, next) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Email and password are required' });
    }
    const user = await User.findOne({ email: email.toLowerCase() }).select('+passwordHash');
    if (!user || !(await user.comparePassword(password))) {
      return res.status(401).json({ success: false, message: 'Invalid email or password' });
    }
    const { token, refreshToken } = generateTokens(user._id);
    res.json({
      success: true,
      data: { token, refreshToken, user }
    });
  } catch (error) {
    next(error);
  }
};

// @route POST /api/auth/logout
const logout = async (req, res) => {
  res.json({ success: true, message: 'Logged out successfully' });
};

// @route GET /api/auth/me
const getMe = async (req, res, next) => {
  try {
    const user = await User.findById(req.user._id);
    res.json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
};

// @route POST /api/auth/google
const googleSignIn = async (req, res, next) => {
  try {
    const { idToken } = req.body;
    if (!idToken) {
      return res.status(400).json({ success: false, message: 'Google ID token required' });
    }
    // Verify Google ID token
    let decodedToken;
    try {
      decodedToken = await verifyGoogleIdToken(idToken);
    } catch (e) {
      return res.status(401).json({ success: false, message: e.message || 'Invalid Google token' });
    }
    const { uid, email, name, picture } = decodedToken;
    let user = await User.findOne({ $or: [{ googleId: uid }, { email }] });
    if (!user) {
      user = await User.create({ name: name || 'User', email, googleId: uid });
    } else if (!user.googleId) {
      user.googleId = uid;
      await user.save();
    }
    const { token, refreshToken } = generateTokens(user._id);
    res.json({ success: true, data: { token, refreshToken, user } });
  } catch (error) {
    next(error);
  }
};

// @route POST /api/auth/forgot-password
const forgotPassword = async (req, res, next) => {
  try {
    const { email } = req.body;
    if (!email) return res.status(400).json({ success: false, message: 'Email is required' });

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user) return res.status(404).json({ success: false, message: 'No account found with this email address' });

    const otp = generateOTP();
    const expiresAt = Date.now() + 10 * 60 * 1000; // 10 minutes
    otpStore.set(email.toLowerCase(), { otp, expiresAt });

    try {
      await sendOTP(email, otp);
      console.log(`✅ OTP sent to ${email}`);
    } catch (emailErr) {
      console.error('❌ Email send error:', emailErr.message);
      return res.status(500).json({ success: false, message: 'Failed to send OTP email. Please check your email address.' });
    }

    res.json({ success: true, message: 'OTP sent to your email address' });
  } catch (error) {
    next(error);
  }
};

// @route POST /api/auth/verify-otp
const verifyOTP = async (req, res, next) => {
  try {
    const { email, otp } = req.body;
    if (!email || !otp) return res.status(400).json({ success: false, message: 'Email and OTP are required' });

    const record = otpStore.get(email.toLowerCase());
    if (!record) return res.status(400).json({ success: false, message: 'OTP not found. Please request a new one.' });
    if (Date.now() > record.expiresAt) {
      otpStore.delete(email.toLowerCase());
      return res.status(400).json({ success: false, message: 'OTP has expired. Please request a new one.' });
    }
    if (record.otp !== otp.trim()) {
      return res.status(400).json({ success: false, message: 'Invalid OTP. Please try again.' });
    }

    // Mark OTP as verified (don't delete yet, keep for reset)
    otpStore.set(email.toLowerCase(), { ...record, verified: true });
    res.json({ success: true, message: 'OTP verified successfully' });
  } catch (error) {
    next(error);
  }
};

// @route POST /api/auth/reset-password
const resetPassword = async (req, res, next) => {
  try {
    const { email, otp, newPassword } = req.body;
    if (!email || !otp || !newPassword) {
      return res.status(400).json({ success: false, message: 'Email, OTP, and new password are required' });
    }
    if (newPassword.length < 6) {
      return res.status(400).json({ success: false, message: 'Password must be at least 6 characters' });
    }

    const record = otpStore.get(email.toLowerCase());
    if (!record || !record.verified || record.otp !== otp.trim()) {
      return res.status(400).json({ success: false, message: 'Invalid or unverified OTP. Please start over.' });
    }
    if (Date.now() > record.expiresAt) {
      otpStore.delete(email.toLowerCase());
      return res.status(400).json({ success: false, message: 'OTP has expired. Please request a new one.' });
    }

    const user = await User.findOne({ email: email.toLowerCase() }).select('+passwordHash');
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });

    user.passwordHash = newPassword; // pre-save hook will hash this
    await user.save();
    otpStore.delete(email.toLowerCase());

    console.log(`✅ Password reset for ${email}`);
    res.json({ success: true, message: 'Password reset successfully. You can now log in.' });
  } catch (error) {
    next(error);
  }
};

module.exports = { register, login, logout, getMe, googleSignIn, forgotPassword, verifyOTP, resetPassword };


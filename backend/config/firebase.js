const { initializeApp, getApps, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const axios = require('axios');

/**
 * Initialize Firebase Admin SDK using environment variables.
 * This is production-ready and works on Render, Railway, Heroku,
 * and other cloud platforms without requiring a JSON file.
 *
 * Safe to call multiple times — no-op if already initialized.
 */
const initFirebase = () => {
  if (getApps().length > 0) return; // Already initialized

  initializeApp({
    credential: cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
    }),
  });

  console.log('✅ Firebase Admin SDK initialized');
};

/**
 * Verify a Firebase ID token and return the decoded payload.
 * @param {string} idToken - The Firebase ID token from the Android client
 */
const verifyFirebaseToken = async (idToken) => {
  return getAuth().verifyIdToken(idToken);
};

/**
 * Verify a Google ID token by calling Google's public tokeninfo endpoint.
 * This is used because the Android app authenticates with Google directly
 * and sends the Google ID token to the backend, rather than a Firebase ID token.
 *
 * @param {string} idToken - The Google ID token from the Android client
 */
const verifyGoogleIdToken = async (idToken) => {
  try {
    const response = await axios.get(
      'https://oauth2.googleapis.com/tokeninfo',
      {
        params: { id_token: idToken },
        timeout: 10000,
      }
    );

    const payload = response.data;

    // Validate issuer
    const validIssuers = [
      'accounts.google.com',
      'https://accounts.google.com',
    ];

    if (!validIssuers.includes(payload.iss)) {
      throw new Error('Invalid token issuer');
    }

    // Validate audience (client ID)
    const expectedClientId =
      process.env.GOOGLE_CLIENT_ID ||
      '731520560876-fm3c15rj7742mtgbon4jqg3tf6us20ad.apps.googleusercontent.com';

    if (payload.aud !== expectedClientId) {
      throw new Error('Token audience mismatch');
    }

    // Validate expiration
    const nowSecs = Math.floor(Date.now() / 1000);

    if (parseInt(payload.exp) < nowSecs) {
      throw new Error('Token has expired');
    }

    // Normalize payload to match what authController expects
    return {
      uid: payload.sub,
      email: payload.email,
      name: payload.name,
      picture: payload.picture,
    };
  } catch (error) {
    console.error(
      'Google token verification failed:',
      error.response?.data || error.message
    );

    throw new Error(
      error.response?.data?.error_description || error.message
    );
  }
};

module.exports = {
  initFirebase,
  verifyFirebaseToken,
  verifyGoogleIdToken,
};
module.exports = {
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.js'],
  collectCoverageFrom: ['controllers/**/*.js', 'services/**/*.js', 'middleware/**/*.js'],
  coverageThreshold: { global: { lines: 70 } },
  testTimeout: 30000
};

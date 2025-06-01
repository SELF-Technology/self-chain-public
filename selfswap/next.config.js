/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  images: {
    domains: ['api.self.app', 'self.app'],
  },
  env: {
    SELF_API_URL: process.env.SELF_API_URL || 'https://api.self.app',
    SELF_CHAIN_ID: process.env.SELF_CHAIN_ID || '1',
  },
}

module.exports = nextConfig

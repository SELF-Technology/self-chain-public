/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  images: {
    domains: ['api.self.app', 'self.app', 'localhost'],
  },
  env: {
    SELF_API_URL: process.env.SELF_API_URL || 'https://api.self.app',
    SELF_CHAIN_ID: process.env.SELF_CHAIN_ID || '1',
    API_URL: process.env.NEXT_PUBLIC_API_URL,
    SELF_NETWORK: process.env.NEXT_PUBLIC_SELF_NETWORK,
    WEB3_PROVIDER_URL: process.env.NEXT_PUBLIC_WEB3_PROVIDER_URL,
  },
}

module.exports = nextConfig

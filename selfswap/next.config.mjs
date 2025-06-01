import { defineConfig } from 'next'

export default defineConfig({
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
  webpack: (config) => {
    // Explicitly disable app directory
    config.resolve.alias = {
      ...config.resolve.alias,
      '@': __dirname,
      'app': false, // Disable app directory alias
    }
    return config
  },
  // Explicitly disable app directory features
  experimental: {
    appDir: false,
    serverComponents: false,
    serverActions: false,
  }
})

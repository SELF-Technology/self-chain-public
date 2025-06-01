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
  webpack: (config) => {
    // Remove app directory from resolve
    config.resolve.alias = {
      ...config.resolve.alias,
      'app': false,
      '@': __dirname,
    }
    
    // Add explicit ignore for app directory
    config.resolve.modules = [
      path.resolve(__dirname, 'pages'),
      'node_modules'
    ]
    
    // Disable CSS modules
    config.module.rules = config.module.rules.map(rule => {
      if (rule.test && rule.test.toString().includes('\\.css$')) {
        rule.oneOf = rule.oneOf.map(oneOf => {
          if (oneOf.test && oneOf.test.toString().includes('\\.module\\.css$')) {
            return false
          }
          return oneOf
        }).filter(Boolean)
      }
      return rule
    })
    
    return config
  },
  // Explicitly disable app directory and enable pages directory
  experimental: {
    appDir: false,
    pagesDir: true,
    serverComponents: false,
    serverActions: false,
  },
  // Force pages directory structure
  pageExtensions: ['js', 'jsx', 'ts', 'tsx'],
  // Disable app directory
  appDir: false,
  // Force pages directory
  pagesDir: true,
  // Disable any automatic imports
  webpackDevMiddleware: (config) => {
    config.watchOptions = {
      ignored: ['**/app/**'],
    }
    return config
  },
  // Disable CSS modules
  cssModules: false,
  css: {
    modules: {
      enabled: false,
    }
  }
}

module.exports = nextConfig

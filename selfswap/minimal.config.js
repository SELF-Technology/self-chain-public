const nextConfig = {
  webpack: (config) => {
    // Remove any app directory references
    config.resolve.alias = {
      ...config.resolve.alias,
      'app': false,
      '@': __dirname,
    }
    return config
  },
  // Explicitly disable app directory features
  experimental: {
    appDir: false,
    serverComponents: false,
    serverActions: false,
  },
  // Force pages directory
  pageExtensions: ['js', 'jsx', 'ts', 'tsx'],
  // Disable any automatic imports
  webpackDevMiddleware: (config) => {
    config.watchOptions = {
      ignored: ['**/app/**'],
    }
    return config
  }
}

module.exports = nextConfig

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
  headers: () => [
    {
      source: '/(.*)',
      headers: [
        { key: 'X-Frame-Options', value: 'DENY' },
        { key: 'X-Content-Type-Options', value: 'nosniff' },
        { key: 'X-DNS-Prefetch-Control', value: 'off' },
        { key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains; preload' },
        { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
        { key: 'Permissions-Policy', value: 'camera=(), microphone=(), geolocation=()' },
        { 
          key: 'Content-Security-Policy', 
          value: [
            "default-src 'self' https:;",
            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https:;",
            "style-src 'self' 'unsafe-inline' https:;",
            "img-src 'self' data: https:;",
            "connect-src 'self' https:;",
            "font-src 'self' https: data:;",
            "object-src 'none';",
            "base-uri 'self';",
            "form-action 'self';",
          ].join(' ') 
        }
      ]
    }
  ],
}

module.exports = nextConfig

# Test Deployment

This is a test deployment to verify Cloudflare Pages configuration and headers.

## Purpose

1. Verify Cloudflare Pages deployment works
2. Test headers configuration
3. Provide a simple test page to verify deployment

## Structure

- `public/`: Contains the static files
  - `index.html`: Simple test page
  - `_headers`: Security headers configuration
- `cloudflare-pages.json`: Cloudflare Pages configuration

## Next Steps

Once this test deployment succeeds:
1. Remove this test directory
2. Configure SELFswap Next.js deployment with proper headers

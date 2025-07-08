# Algolia Search Setup for Production

The Algolia search is configured but needs environment variables set in production.

## Environment Variables Required

```
ALGOLIA_APP_ID=0X1QYF886S
ALGOLIA_SEARCH_API_KEY=529f2d29487ab836fe56f52b39493466
```

## Setting up in Cloudflare Pages

1. Go to your Cloudflare Pages project dashboard
2. Navigate to Settings → Environment variables
3. Add the following variables for Production:
   - `ALGOLIA_APP_ID` = `0X1QYF886S`
   - `ALGOLIA_SEARCH_API_KEY` = `529f2d29487ab836fe56f52b39493466`
4. Redeploy the site for changes to take effect

## Alternative: Hardcode in Config (Less Secure)

If you want search to work immediately without setting up environment variables, you can temporarily hardcode the values in `docusaurus.config.js`, but this is NOT recommended for security:

```javascript
algolia: {
  appId: '0X1QYF886S',
  apiKey: '529f2d29487ab836fe56f52b39493466',
  indexName: 'self',
},
```

## Verifying Algolia Index

Make sure your Algolia index named 'self' exists and is populated with your documentation content.
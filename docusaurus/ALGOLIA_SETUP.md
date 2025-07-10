# Algolia Search Setup for Production

The Algolia search is configured but needs environment variables set in production.

## Environment Variables Required

```
ALGOLIA_APP_ID=<your-app-id>
ALGOLIA_SEARCH_API_KEY=<your-search-api-key>
```

## Security Considerations

While Algolia search-only API keys are designed to be exposed publicly, they can still be misused for:
- Scraping all index data
- Flooding the account with search requests

### Recommended: Generate a Secured API Key

Instead of using the basic search API key, generate a secured API key with restrictions:

1. Go to your Algolia dashboard
2. Navigate to API Keys
3. Create a new secured search-only API key with:
   - Rate limiting
   - Index restrictions (only 'self' index)
   - Referer restrictions (only from your domain)
   - Valid until date (optional)

## Setting up in GitHub (for GitHub Pages deployment)

1. Go to your GitHub repository (SELF-Technology/self-chain-public)
2. Navigate to Settings → Secrets and variables → Actions
3. Click "New repository secret" and add:
   - Name: `ALGOLIA_APP_ID`, Value: Your Algolia App ID
   - Name: `ALGOLIA_SEARCH_API_KEY`, Value: Your secured search API key
4. The next deployment will automatically use these secrets

## Verifying Algolia Index

Make sure your Algolia index named 'self' exists and is populated with your documentation content.

## Status

✅ GitHub secrets configured - Search functionality enabled

## Note on Hardcoding

Avoid hardcoding API keys directly in source code as security scanners will flag this as a vulnerability (SCT-1035). Always use environment variables for production deployments.
# Algolia Search Setup for Production

The Algolia search is configured but needs environment variables set in production.

## Environment Variables Required

```
ALGOLIA_APP_ID=<your-app-id>
ALGOLIA_SEARCH_API_KEY=<your-search-api-key>
ALGOLIA_ASSISTANT_ID=<your-assistant-id>  # For Ask AI feature
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

## Setting up in Cloudflare Pages

1. Go to your Cloudflare Pages dashboard
2. Select your project
3. Navigate to Settings → Environment variables
4. Add the following variables:
   - `ALGOLIA_APP_ID`: Your Algolia App ID
   - `ALGOLIA_SEARCH_API_KEY`: Your secured search API key
   - `ALGOLIA_ASSISTANT_ID`: Your Algolia Assistant ID (for Ask AI feature)
5. The next deployment will automatically use these environment variables

## Setting up in GitHub (for GitHub Pages deployment)

1. Go to your GitHub repository (SELF-Technology/self-chain-public)
2. Navigate to Settings → Secrets and variables → Actions
3. Click "New repository secret" and add:
   - Name: `ALGOLIA_APP_ID`, Value: Your Algolia App ID
   - Name: `ALGOLIA_SEARCH_API_KEY`, Value: Your secured search API key
   - Name: `ALGOLIA_ASSISTANT_ID`, Value: Your Algolia Assistant ID
4. The next deployment will automatically use these secrets

## Verifying Algolia Index

Make sure your Algolia index named 'self' exists and is populated with your documentation content.

## Ask AI Feature (DocSearch v4)

The Ask AI feature provides intelligent, contextually relevant responses directly from your documentation. This implementation uses DocSearch v4 (beta) with a custom Docusaurus plugin.

### Features:
- Users can ask natural language questions
- The AI assistant provides answers based on your documentation content
- Results are filtered to only use content records (not navigation or metadata)
- Implemented via standalone DocSearch v4 for full Ask AI support

### Implementation Details:
- Custom plugin at `/src/plugins/algolia-v4.js`
- Loads DocSearch v4 beta from CDN
- Injects search functionality into the navbar
- Automatically enables Ask AI when `ALGOLIA_ASSISTANT_ID` is provided

### To enable Ask AI:
1. Ensure you have an Algolia Assistant configured in your Algolia dashboard
2. Add the `ALGOLIA_ASSISTANT_ID` environment variable in Cloudflare Pages
3. The Ask AI feature will automatically appear after deployment

## Status

✅ GitHub secrets configured - Search functionality enabled
✅ Ask AI integration configured - Pending assistant ID

## Note on Hardcoding

Avoid hardcoding API keys directly in source code as security scanners will flag this as a vulnerability (SCT-1035). Always use environment variables for production deployments.
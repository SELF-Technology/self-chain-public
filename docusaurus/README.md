# SELF Documentation Site (Docusaurus)

This directory contains the Docusaurus configuration for docs.self.app.

## 🚀 Quick Start

```bash
# Install dependencies
npm install

# Start development server
npm run start

# Build for production
npm run build
```

## 📁 Structure

- **Configuration**: This directory (`/docusaurus/`)
- **Documentation**: Parent directory (`/docs/`)
- **Output**: Will be deployed to docs.self.app

## 🔧 Configuration

The site is configured to:
- Use `/docs/` from the parent directory as content source
- Serve documentation at the root path (`/`)
- Support dark/light mode
- Include SELF branding
- Algolia DocSearch integration (requires API keys)

### Search Configuration

To enable Algolia DocSearch:

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Add your Algolia credentials to `.env`:
   ```
   ALGOLIA_APP_ID=your-algolia-app-id
   ALGOLIA_SEARCH_API_KEY=your-algolia-search-api-key
   ```

3. The search will automatically appear in the navbar when valid credentials are provided

## 📝 Editing Documentation

**Important**: Edit documentation files in the `/docs/` directory, NOT in this docusaurus folder.

```
self-chain-public/
├── docs/           ← Edit documentation here
└── docusaurus/     ← Docusaurus config only (don't edit docs here)
```

## 🚢 Deployment

Coming soon: Automatic deployment to docs.self.app via GitHub Actions.
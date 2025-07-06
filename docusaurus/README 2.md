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

## 📝 Editing Documentation

**Important**: Edit documentation files in the `/docs/` directory, NOT in this docusaurus folder.

```
self-chain-public/
├── docs/           ← Edit documentation here
└── docusaurus/     ← Docusaurus config only (don't edit docs here)
```

## 🚢 Deployment

Coming soon: Automatic deployment to docs.self.app via GitHub Actions.
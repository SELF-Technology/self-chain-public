#!/bin/bash

# Safe folder reorganization script for Docusaurus
# This script helps prevent chunk loading errors when moving folders

echo "🛑 Stopping any running development servers..."
pkill -f "npm start" || true
pkill -f "docusaurus start" || true
sleep 2

echo "🧹 Clearing Docusaurus cache and build artifacts..."
cd /Users/jmac/Documents/GitHub/self-chain-public/docusaurus
rm -rf .docusaurus .docusaurus-cache node_modules/.cache build

echo "✅ Cache cleared! You can now safely reorganize folders."
echo ""
echo "📝 Tips for reorganizing:"
echo "1. Avoid spaces in folder/file names - use hyphens instead"
echo "2. Update any hardcoded links in index.md after moving files"
echo "3. Run 'npm start' in the docusaurus folder when done"
echo ""
echo "🔄 To restart the server after reorganizing, run:"
echo "   cd docusaurus && npm start"
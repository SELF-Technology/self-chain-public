#!/bin/bash

# Safe start script to avoid webpack chunk conflicts

echo "🧹 Clearing all caches and build artifacts..."
npm run clear

echo "🔍 Removing any additional cache directories..."
rm -rf .docusaurus
rm -rf build
rm -rf node_modules/.cache

echo "🚀 Starting Docusaurus dev server..."
npm start
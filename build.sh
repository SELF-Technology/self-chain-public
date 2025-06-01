#!/bin/bash

# Print current directory
echo "Starting in directory: $(pwd)"

# Clean up previous build
echo "Cleaning previous build..."
rm -rf dist
rm -rf node_modules
rm -rf package-lock.json

# Create output directory
echo "Creating output directory..."
mkdir -p dist

# Install dependencies
echo "Installing dependencies..."
npm install --no-cache --legacy-peer-deps

# Build the application
echo "Building application..."
NODE_ENV=production npm run build

# Copy build output
echo "Copying build output..."
cp -r dist/* output/

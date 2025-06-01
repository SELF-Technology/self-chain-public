#!/bin/bash

# Print current directory
echo "Current directory: $(pwd)"

# Navigate to selfswap directory
if [ -d "../selfswap" ]; then
  cd ../selfswap
  echo "Navigated to: $(pwd)"
fi

# Clean up previous build
echo "Cleaning up previous build..."
rm -rf .next
rm -rf output

# Create output directory
echo "Creating output directory..."
mkdir -p output

# Install dependencies
echo "Installing dependencies..."
npm install

# Build the application
echo "Building application..."
npm run build

# Copy build output
echo "Copying build output..."
if [ -d ".next" ]; then
  cp -r .next output/.next
else
  echo ".next directory not found"
  exit 1
fi

# Copy public files
echo "Copying public files..."
mkdir -p output/public
if [ -d "public" ]; then
  cp -r public/* output/public/
fi

# Copy headers file
if [ -f "public/_headers" ]; then
  cp public/_headers output/public/_headers
fi

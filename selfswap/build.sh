#!/bin/bash

# Navigate to the root of the project
if [ -d "../.." ]; then
  cd ../..
fi

# Clean up previous build
rm -rf .next
rm -rf output

# Create output directory
mkdir -p output

# Install dependencies
npm install

# Build the application
npm run build

# Copy build output
if [ -d ".next" ]; then
  cp -r .next output/.next
else
  echo ".next directory not found"
  exit 1
fi

# Copy public files
mkdir -p output/public
if [ -d "selfswap/public" ]; then
  cp -r selfswap/public/* output/public/
fi

# Copy headers file
if [ -f "selfswap/public/_headers" ]; then
  cp selfswap/public/_headers output/public/_headers
fi

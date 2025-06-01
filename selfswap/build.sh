#!/bin/bash

# Navigate to the root of the project
if [ -d "../.." ]; then
  cd ../..
fi

# Install dependencies
npm install

# Build the application
npm run build

# Create output directory
mkdir -p output

# Copy build output
if [ -d "selfswap/.next" ]; then
  cp -r selfswap/.next output/.next
else
  echo ".next directory not found"
  exit 1
fi

# Copy static files
mkdir -p output/public
if [ -d "selfswap/public" ]; then
  cp -r selfswap/public/* output/public/
fi

# Copy headers file
if [ -f "selfswap/public/_headers" ]; then
  cp selfswap/public/_headers output/public/_headers
fi

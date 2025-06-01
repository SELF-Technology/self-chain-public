#!/bin/bash

# Install dependencies
npm install

# Build the application
npm run build

# Create output directory
mkdir -p output

# Copy build output
if [ -d ".next" ]; then
  cp -r .next output/.next
else
  echo ".next directory not found"
  exit 1
fi

# Copy static files
mkdir -p output/public
if [ -d "public" ]; then
  cp -r public/* output/public/
fi

# Copy headers file
if [ -f "public/_headers" ]; then
  cp public/_headers output/public/_headers
fi

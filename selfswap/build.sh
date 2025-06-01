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

# Copy public files
mkdir -p output/public
if [ -d "public" ]; then
  cp -r public/* output/public/
fi

# Copy app directory
mkdir -p output/app
if [ -d "app" ]; then
  cp -r app/* output/app/
fi

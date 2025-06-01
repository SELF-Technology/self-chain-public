#!/bin/bash

# Install dependencies
npm install

# Build the application
npm run build

# Create output directory and copy build output
mkdir -p output
mkdir -p output/.next

# Copy .next directory
if [ -d ".next" ]; then
  cp -r .next/* output/.next/
else
  echo ".next directory not found"
  exit 1
fi

# Copy static files
mkdir -p output/public
if [ -d "public" ]; then
  cp -r public/* output/public/
fi

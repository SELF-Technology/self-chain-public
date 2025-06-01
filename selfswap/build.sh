#!/bin/bash

# Install dependencies
npm install

# Build the application
npm run build

# Copy the build output to the output directory
mkdir -p output
mkdir -p output/.next

# Copy all contents from .next to output/.next
if [ -d ".next" ]; then
  cp -r .next/* output/.next/
else
  echo ".next directory not found"
  exit 1
fi

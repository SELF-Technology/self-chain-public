#!/bin/bash

# Install dependencies
npm install

# Build the application
npm run build

# Copy the build output to the output directory
mkdir -p output
mkdir -p output/.next
mv .next/* output/.next/

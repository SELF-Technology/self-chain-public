#!/bin/bash

# Print current directory
echo "Starting in directory: $(pwd)"

# Navigate to selfswap directory if running from root
current_dir=$(basename $(pwd))
if [ "$current_dir" != "selfswap" ]; then
  echo "Navigating to selfswap directory"
  cd selfswap
fi

echo "Running in directory: $(pwd)"

# Clean up previous build
echo "Cleaning previous build..."
rm -rf .next
rm -rf output
rm -rf node_modules
rm -rf package-lock.json
rm -rf "app" # Ensure app directory is removed

# Create output directory
echo "Creating output directory..."
mkdir -p output

# Install dependencies
echo "Installing dependencies..."
npm install --no-cache --legacy-peer-deps

# Build the application
echo "Building application..."
# Set the root directory explicitly
export NEXT_PUBLIC_ROOT_DIR=$(pwd)
export NEXT_PUBLIC_PAGES_DIR="pages"

# Force pages directory structure
export NEXT_PUBLIC_USE_PAGES=true

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

#!/bin/bash
echo "🚀 Starting SELF Documentation locally..."
echo "The site will be available at: http://localhost:4000"
echo "Press Ctrl+C to stop the server"
echo ""

# Check if vendor/bundle exists, if not install dependencies
if [ ! -d "vendor/bundle" ]; then
    echo "Installing dependencies..."
    bundle install --path vendor/bundle
fi

bundle exec jekyll serve --livereload --open-url
#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Create dist directory if it doesn't exist
mkdir -p dist

# Install dependencies
npm install -g tailwindcss

# Generate Tailwind CSS
npx tailwindcss -i ./index.html -o ./dist/styles.css --minify

# Copy static files
cp index.html dist/

# Copy images if they exist
if [ -d "images" ]; then
    mkdir -p dist/images
    cp -r images/* dist/images/
fi

# Copy robots.txt if it exists
if [ -f "robots.txt" ]; then
    cp robots.txt dist/
fi

# Copy sitemap.xml if it exists
if [ -f "sitemap.xml" ]; then
    cp sitemap.xml dist/
fi

# Copy manifest.json if it exists
if [ -f "manifest.json" ]; then
    cp manifest.json dist/
fi

# Copy offline.html if it exists
if [ -f "offline.html" ]; then
    cp offline.html dist/
fi

# Copy service worker if it exists
if [ -f "sw.js" ]; then
    cp sw.js dist/
fi

# Minify HTML
if [ -f "dist/index.html" ]; then
    echo "Minifying HTML..."
    npx html-minifier --collapse-whitespace --remove-comments --remove-optional-tags --remove-redundant-attributes --remove-script-type-attributes --remove-tag-whitespace --use-short-doctype --minify-css --minify-js dist/index.html -o dist/index.html
fi

# Generate SEO files
if [ -f "dist/index.html" ]; then
    echo "Generating SEO files..."
    # Extract title and description
    title=$(grep -oP '(?<=<title>)[^<]+' dist/index.html)
    description=$(grep -oP '(?<=<meta name="description" content=")[^"]+' dist/index.html)
    
    # Generate sitemap.xml if not exists
    if [ ! -f "dist/sitemap.xml" ]; then
        echo "<?xml version="1.0" encoding="UTF-8"?>" > dist/sitemap.xml
        echo "<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">" >> dist/sitemap.xml
        echo "  <url>" >> dist/sitemap.xml
        echo "    <loc>https://$CLOUDFLARE_PAGES_URL/</loc>" >> dist/sitemap.xml
        echo "    <lastmod>$(date -u +"%Y-%m-%dT%H:%M:%SZ")</lastmod>" >> dist/sitemap.xml
        echo "    <changefreq>weekly</changefreq>" >> dist/sitemap.xml
        echo "    <priority>1.0</priority>" >> dist/sitemap.xml
        echo "  </url>" >> dist/sitemap.xml
        echo "</urlset>" >> dist/sitemap.xml
    fi
    
    # Generate robots.txt if not exists
    if [ ! -f "dist/robots.txt" ]; then
        echo "User-agent: *" > dist/robots.txt
        echo "Allow: /" >> dist/robots.txt
        echo "Sitemap: https://$CLOUDFLARE_PAGES_URL/sitemap.xml" >> dist/robots.txt
    fi
fi

# Generate offline page if not exists
if [ ! -f "dist/offline.html" ]; then
    echo "<!DOCTYPE html>" > dist/offline.html
    echo "<html>" >> dist/offline.html
    echo "<head>" >> dist/offline.html
    echo "    <meta charset="UTF-8">" >> dist/offline.html
    echo "    <title>Offline</title>" >> dist/offline.html
    echo "    <style>" >> dist/offline.html
    echo "        body { " >> dist/offline.html
    echo "            font-family: Arial, sans-serif; " >> dist/offline.html
    echo "            text-align: center; " >> dist/offline.html
    echo "            padding: 50px; " >> dist/offline.html
    echo "            background: #f5f5f5; " >> dist/offline.html
    echo "        }" >> dist/offline.html
    echo "        .container { " >> dist/offline.html
    echo "            max-width: 600px; " >> dist/offline.html
    echo "            margin: 0 auto; " >> dist/offline.html
    echo "            background: white; " >> dist/offline.html
    echo "            padding: 30px; " >> dist/offline.html
    echo "            border-radius: 8px; " >> dist/offline.html
    echo "            box-shadow: 0 2px 4px rgba(0,0,0,0.1); " >> dist/offline.html
    echo "        }" >> dist/offline.html
    echo "    </style>" >> dist/offline.html
    echo "</head>" >> dist/offline.html
    echo "<body>" >> dist/offline.html
    echo "    <div class="container">" >> dist/offline.html
    echo "        <h1>Offline</h1>" >> dist/offline.html
    echo "        <p>You are currently offline. Please check your internet connection and try again.</p>" >> dist/offline.html
    echo "    </div>" >> dist/offline.html
    echo "</body>" >> dist/offline.html
    echo "</html>" >> dist/offline.html
fi

# Generate manifest.json for PWA support
if [ ! -f "dist/manifest.json" ]; then
    echo '{
        "name": "SELF Chain Token Creator",
        "short_name": "Token Creator",
        "description": "Create ERC20 tokens and NFT collections on SELF Chain",
        "start_url": "/",
        "display": "standalone",
        "theme_color": "#2196f3",
        "background_color": "#ffffff",
        "icons": [
            {
                "src": "/icon-192x192.png",
                "sizes": "192x192",
                "type": "image/png"
            },
            {
                "src": "/icon-512x512.png",
                "sizes": "512x512",
                "type": "image/png"
            }
        ]
    }' > dist/manifest.json
fi

# Generate service worker
if [ ! -f "dist/service-worker.js" ]; then
    echo 'const CACHE_NAME = "token-creator-cache-v1";
const urlsToCache = [
    "/",
    "/manifest.json",
    "/offline.html"
];

self.addEventListener("install", event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(urlsToCache))
    );
});

self.addEventListener("fetch", event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => response || fetch(event.request))
    );
});' > dist/service-worker.js
fi

# Create README for build process
echo '# Build Process

This script automates the build process for the SELF Chain Token Creator Cloudflare Pages version.

## Features

- HTML minification
- SEO optimization
- PWA support
- Offline capability
- Service worker
- Sitemap generation
- Robots.txt configuration
- Tailwind CSS compilation

## Build Output

The script creates the following files in the `dist` directory:

- `index.html`: Minified version of the main page
- `.htaccess`: URL routing configuration
- `robots.txt`: Search engine configuration
- `sitemap.xml`: Site map for search engines
- `manifest.json`: PWA configuration
- `offline.html`: Offline page
- `service-worker.js`: Service worker for PWA features' > dist/README.md

# Make script executable
chmod +x $0

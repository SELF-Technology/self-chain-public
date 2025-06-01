#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Create dist directory if it doesn't exist
mkdir -p dist

# Copy static files
cp index.html dist/

# Minify HTML
html-minifier --collapse-whitespace --remove-comments --remove-optional-tags --remove-redundant-attributes --remove-script-type-attributes --remove-tag-whitespace --use-short-doctype --minify-css true --minify-js true index.html > dist/index.html

# Create .htaccess for better routing
echo "RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^ dist/index.html [L]" > dist/.htaccess

# Create robots.txt
echo "User-agent: *
Allow: /" > dist/robots.txt

# Create sitemap.xml
echo "<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <url>
        <loc>https://$CLOUDFLARE_PAGES_URL/</loc>
        <priority>1.0</priority>
    </url>
</urlset>" > dist/sitemap.xml

# Create manifest.json for PWA support
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

# Create offline page
echo '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Offline</title>
    <style>
        body { 
            font-family: Arial, sans-serif; 
            text-align: center; 
            padding: 50px; 
            background: #f5f5f5; 
        }
        .container { 
            max-width: 600px; 
            margin: 0 auto; 
            background: white; 
            padding: 30px; 
            border-radius: 8px; 
            box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Offline</h1>
        <p>You are currently offline. Please check your internet connection and try again.</p>
    </div>
</body>
</html>' > dist/offline.html

# Create service worker
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

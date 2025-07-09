#!/usr/bin/env node
/**
 * Script to generate PWA icons from the main logo
 * This creates a simple placeholder - for production, use proper image editing tools
 */

const fs = require('fs');
const path = require('path');

const sizes = [72, 96, 128, 144, 152, 192, 384, 512];

console.log('PWA Icon Generation Instructions:');
console.log('==================================');
console.log('Please create the following icon sizes from your SELF logo:');
console.log('');

sizes.forEach(size => {
  console.log(`- icon-${size}x${size}.png (${size}x${size} pixels)`);
});

console.log('\nRecommended approach:');
console.log('1. Use a tool like https://realfavicongenerator.net/');
console.log('2. Or use ImageMagick: convert SELF-BLACK.png -resize 192x192 icon-192x192.png');
console.log('3. Place all generated icons in /docusaurus/static/img/');
console.log('\nFor now, the manifest.json is configured and ready.');

// Create a simple service worker registration file
const swContent = `
// PWA Service Worker Registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').then(
      registration => console.log('ServiceWorker registered'),
      error => console.log('ServiceWorker registration failed:', error)
    );
  });
}
`;

fs.writeFileSync(
  path.join(__dirname, '../src/theme/Root.js'),
  `
import React from 'react';
import ExecutionEnvironment from '@docusaurus/ExecutionEnvironment';

// PWA Service Worker Registration
if (ExecutionEnvironment.canUseDOM && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(error => 
      console.log('ServiceWorker registration failed:', error)
    );
  });
}

export default function Root({children}) {
  return children;
}
`
);

console.log('\n✅ PWA setup complete!');
console.log('Next steps: Generate the icon files listed above.');
const fs = require('fs');
const path = require('path');

// Read the routes chunk names to get all pages
const routesChunkNames = require('./.docusaurus/routesChunkNames.json');

console.log('Testing all page routes...\n');

let totalPages = 0;
let validPages = 0;

// Check each route
for (const [route, chunks] of Object.entries(routesChunkNames)) {
  if (route.includes('/self-chain-public/') && !route.includes('__docusaurus')) {
    totalPages++;
    
    // Extract the content chunk if it exists
    const contentChunk = chunks.content;
    if (contentChunk) {
      console.log(`✓ Route: ${route}`);
      console.log(`  Content chunk: ${contentChunk}`);
      validPages++;
    } else if (route === '/self-chain-public/-a16' || route === '/self-chain-public/-5fd' || route === '/self-chain-public/-221') {
      // These are layout routes, not content pages
      console.log(`✓ Layout route: ${route}`);
      validPages++;
    } else {
      console.log(`✗ Route missing content: ${route}`);
    }
  }
}

console.log('\n=== Summary ===');
console.log(`Total pages tested: ${totalPages}`);
console.log(`Valid pages: ${validPages}`);
console.log(`Success rate: ${(validPages/totalPages * 100).toFixed(1)}%`);

if (validPages === totalPages) {
  console.log('\n✅ All pages have proper chunk configuration!');
  process.exit(0);
} else {
  console.log('\n❌ Some pages are missing content chunks');
  process.exit(1);
}
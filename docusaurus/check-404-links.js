#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

// Function to recursively find all .md files
function findMarkdownFiles(dir, files = []) {
  const items = fs.readdirSync(dir);
  
  for (const item of items) {
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);
    
    if (stat.isDirectory() && !item.startsWith('.') && item !== 'node_modules') {
      findMarkdownFiles(fullPath, files);
    } else if (item.endsWith('.md') || item.endsWith('.mdx')) {
      files.push(fullPath);
    }
  }
  
  return files;
}

// Function to check if a documentation page exists
function checkDocLink(linkPath, docsRoot) {
  // Remove any anchors
  const cleanPath = linkPath.split('#')[0];
  
  // Skip external links
  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://') || cleanPath.startsWith('mailto:')) {
    return true;
  }
  
  // For absolute paths starting with /
  if (cleanPath.startsWith('/')) {
    // Check various possibilities
    const possibilities = [
      path.join(docsRoot, cleanPath + '.md'),
      path.join(docsRoot, cleanPath + '.mdx'),
      path.join(docsRoot, cleanPath, 'index.md'),
      path.join(docsRoot, cleanPath, 'index.mdx'),
      path.join(docsRoot, cleanPath.replace(/^\//, '') + '.md'),
      path.join(docsRoot, cleanPath.replace(/^\//, '') + '.mdx'),
    ];
    
    return possibilities.some(p => fs.existsSync(p));
  }
  
  return true; // Skip relative paths for now
}

// Main function
function check404Links() {
  const docsRoot = path.join(__dirname, '..', 'docs');
  const mdFiles = findMarkdownFiles(docsRoot);
  
  let brokenLinks = [];
  
  for (const file of mdFiles) {
    const content = fs.readFileSync(file, 'utf8');
    const lines = content.split('\n');
    
    // Find all markdown links
    const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g;
    
    lines.forEach((line, lineIndex) => {
      let match;
      const regex = /\[([^\]]+)\]\(([^)]+)\)/g;
      
      while ((match = regex.exec(line)) !== null) {
        const linkText = match[1];
        const linkPath = match[2];
        
        // Check if this is an internal doc link that doesn't exist
        if (!linkPath.startsWith('http') && !linkPath.startsWith('#') && !linkPath.startsWith('mailto:')) {
          if (!checkDocLink(linkPath, docsRoot)) {
            brokenLinks.push({
              file: path.relative(path.dirname(docsRoot), file),
              linkText,
              linkPath,
              line: lineIndex + 1
            });
          }
        }
      }
    });
  }
  
  if (brokenLinks.length > 0) {
    console.log(`Found ${brokenLinks.length} broken links that would cause 404s:\n`);
    brokenLinks.forEach(link => {
      console.log(`File: ${link.file}:${link.line}`);
      console.log(`  Text: "${link.linkText}"`);
      console.log(`  Path: ${link.linkPath}\n`);
    });
  } else {
    console.log('No broken links found that would cause 404s!');
  }
}

check404Links();
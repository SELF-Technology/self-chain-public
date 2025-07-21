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
    } else if (item.endsWith('.md')) {
      files.push(fullPath);
    }
  }
  
  return files;
}

// Function to check if a file path exists
function fileExists(basePath, linkPath) {
  // Handle absolute paths
  if (linkPath.startsWith('/')) {
    const absolutePath = path.join(__dirname, 'docs', linkPath);
    const withMd = absolutePath.endsWith('.md') ? absolutePath : absolutePath + '.md';
    const withoutMd = absolutePath.replace(/\.md$/, '');
    
    return fs.existsSync(withMd) || fs.existsSync(withoutMd) || fs.existsSync(absolutePath);
  }
  
  // Handle relative paths
  const dir = path.dirname(basePath);
  const absolutePath = path.resolve(dir, linkPath);
  const withMd = absolutePath.endsWith('.md') ? absolutePath : absolutePath + '.md';
  const withoutMd = absolutePath.replace(/\.md$/, '');
  
  return fs.existsSync(withMd) || fs.existsSync(withoutMd) || fs.existsSync(absolutePath);
}

// Main function
function checkBrokenLinks() {
  const docsDir = path.join(__dirname, 'docs');
  const mdFiles = findMarkdownFiles(docsDir);
  
  let brokenLinks = [];
  
  for (const file of mdFiles) {
    const content = fs.readFileSync(file, 'utf8');
    
    // Find all markdown links
    const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g;
    let match;
    
    while ((match = linkRegex.exec(content)) !== null) {
      const linkText = match[1];
      const linkPath = match[2];
      
      // Skip external links and anchors
      if (linkPath.startsWith('http://') || 
          linkPath.startsWith('https://') || 
          linkPath.startsWith('#') ||
          linkPath.startsWith('mailto:')) {
        continue;
      }
      
      // Remove URL encoding and anchors
      const cleanPath = decodeURIComponent(linkPath.split('#')[0]);
      
      if (!fileExists(file, cleanPath)) {
        brokenLinks.push({
          file: file.replace(__dirname + '/', ''),
          linkText,
          linkPath,
          line: content.substring(0, match.index).split('\n').length
        });
      }
    }
  }
  
  if (brokenLinks.length > 0) {
    console.log(`Found ${brokenLinks.length} broken links:\n`);
    brokenLinks.forEach(link => {
      console.log(`File: ${link.file}:${link.line}`);
      console.log(`  Text: "${link.linkText}"`);
      console.log(`  Path: ${link.linkPath}\n`);
    });
  } else {
    console.log('No broken links found!');
  }
}

checkBrokenLinks();
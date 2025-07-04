#!/bin/bash

echo "🧪 Testing Docusaurus with minimal content..."

DOCS_DIR="/Users/jmac/Documents/GitHub/docs/docs"
BACKUP_DIR="/Users/jmac/Documents/GitHub/docs/docs_full_backup"

# Backup current docs
echo "📦 Backing up current docs..."
mv "$DOCS_DIR" "$BACKUP_DIR"

# Create minimal docs
echo "📄 Creating minimal test docs..."
mkdir -p "$DOCS_DIR"

# Create a simple test file
cat > "$DOCS_DIR/test.md" << 'EOF'
---
title: Test Page
---

# Test Page

This is a simple test page to verify Docusaurus is working.

## Section 1

Some content here.

## Section 2

More content here.
EOF

# Create another simple file
cat > "$DOCS_DIR/intro.md" << 'EOF'
---
title: Introduction
---

# Introduction

Welcome to the documentation.
EOF

echo ""
echo "✅ Minimal test docs created!"
echo ""
echo "Now try running Docusaurus:"
echo "  cd /Users/jmac/Documents/GitHub/docs"
echo "  npm run start"
echo ""
echo "If this works, we know Docusaurus is set up correctly."
echo ""
echo "To restore your full docs:"
echo "  rm -rf $DOCS_DIR"
echo "  mv $BACKUP_DIR $DOCS_DIR"
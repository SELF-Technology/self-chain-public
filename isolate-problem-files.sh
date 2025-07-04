#!/bin/bash

echo "🔍 Isolating problematic files to get Docusaurus running..."

DOCS_DIR="/Users/jmac/Documents/GitHub/docs/docs"
PROBLEM_DIR="/Users/jmac/Documents/GitHub/docs/docs_problematic"

# Create a directory for problematic files
mkdir -p "$PROBLEM_DIR"

# Move files that commonly cause MDX issues
echo "📁 Moving potentially problematic files..."

# Files we know have issues
problem_files=(
    "Constellation/Industry_Validation_Rules.md"
    "Architecture/Constellation_Architecture.md"
    "Grid Compute/Competitive_Advantages.md"
)

for file in "${problem_files[@]}"; do
    if [ -f "$DOCS_DIR/$file" ]; then
        echo "Moving: $file"
        mkdir -p "$PROBLEM_DIR/$(dirname "$file")"
        mv "$DOCS_DIR/$file" "$PROBLEM_DIR/$file"
    fi
done

# Also move any files with certain patterns that cause issues
echo "🔍 Finding and moving files with problematic patterns..."

# Files with complex tables
grep -l '|.*|.*|.*|.*|' "$DOCS_DIR"/**/*.md 2>/dev/null | while read file; do
    echo "Moving file with complex table: $file"
    rel_path="${file#$DOCS_DIR/}"
    mkdir -p "$PROBLEM_DIR/$(dirname "$rel_path")"
    mv "$file" "$PROBLEM_DIR/$rel_path"
done

echo ""
echo "✅ Problematic files moved to: $PROBLEM_DIR"
echo ""
echo "Now try running Docusaurus:"
echo "  cd /Users/jmac/Documents/GitHub/docs"
echo "  npm run start"
echo ""
echo "Once it's running, we can fix and add back the problematic files one by one."
echo ""
echo "To restore all files later:"
echo "  cp -r $PROBLEM_DIR/* $DOCS_DIR/"
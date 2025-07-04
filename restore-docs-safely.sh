#!/bin/bash

echo "🔄 Restoring docs safely..."

DOCS_DIR="/Users/jmac/Documents/GitHub/docs/docs"
BACKUP_DIR="/Users/jmac/Documents/GitHub/docs/docs_full_backup"

# First, let's copy back safe directories one by one
echo "📁 Restoring documentation sections..."

# Start with directories that are less likely to have issues
safe_dirs=(
    "SDK"
    "Security"
    "API"
    "Development"
    "Storage"
    "Monitoring"
    "Performance"
)

for dir in "${safe_dirs[@]}"; do
    if [ -d "$BACKUP_DIR/$dir" ]; then
        echo "Restoring: $dir"
        cp -r "$BACKUP_DIR/$dir" "$DOCS_DIR/"
    fi
done

# Now let's add About SELF (but skip problematic files)
echo "📁 Restoring About SELF (without problematic files)..."
mkdir -p "$DOCS_DIR/About SELF"
cp -r "$BACKUP_DIR/About SELF"/* "$DOCS_DIR/About SELF/" 2>/dev/null || true

# Skip the Creation of SELF Brand.md file we know has issues
rm -f "$DOCS_DIR/About SELF/2. Approach/Creation of SELF Brand.md" 2>/dev/null

# Add Architecture but skip Constellation_Architecture.md
echo "📁 Restoring Architecture..."
mkdir -p "$DOCS_DIR/Architecture"
cp "$BACKUP_DIR/Architecture"/*.md "$DOCS_DIR/Architecture/" 2>/dev/null || true
rm -f "$DOCS_DIR/Architecture/Constellation_Architecture.md" 2>/dev/null

# Add History since it's being requested
echo "📁 Restoring History..."
if [ -d "$BACKUP_DIR/History" ]; then
    cp -r "$BACKUP_DIR/History" "$DOCS_DIR/"
fi

echo ""
echo "✅ Safe restoration complete!"
echo ""
echo "The site should now have most of your content without the problematic files."
echo ""
echo "Skipped files:"
echo "- Creation of SELF Brand.md (has JSX)"
echo "- Constellation_Architecture.md (complex formatting)"
echo ""
echo "To add more sections, copy them from:"
echo "  $BACKUP_DIR"
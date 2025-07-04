#!/bin/bash

echo "🧹 Completely removing ALL Jekyll dependencies and syntax..."

DOCS_DIR="/Users/jmac/Documents/GitHub/docs/docs"

# Function to clean a markdown file
clean_markdown_file() {
    local file="$1"
    echo "Cleaning: $file"
    
    # Create a temporary file
    temp_file="${file}.tmp"
    
    # Process the file
    awk '
    BEGIN { in_frontmatter = 0; frontmatter_count = 0 }
    
    # Handle front matter
    /^---$/ && frontmatter_count < 2 { 
        frontmatter_count++
        if (frontmatter_count == 1) {
            in_frontmatter = 1
            print "---"
        } else {
            in_frontmatter = 0
            print "---"
            print ""
        }
        next
    }
    
    # Skip Jekyll-specific front matter fields
    in_frontmatter && /^(layout|parent|nav_order|has_children|permalink|grand_parent):/ { next }
    
    # Clean up Jekyll-specific content markers
    !in_frontmatter {
        # Remove TOC markers
        if ($0 ~ /{:toc}/) next
        if ($0 ~ /{: \.no_toc/) next
        if ($0 ~ /{: \.text-delta/) next
        if ($0 ~ /{: \.fs-/) next
        if ($0 ~ /{: \.btn/) next
        if ($0 ~ /{: \.float-/) next
        
        # Remove liquid tags
        gsub(/\{\{[^}]*\}\}/, "")
        gsub(/\{%[^}]*%\}/, "")
        
        # Print the cleaned line
        print
    }
    
    # Print front matter content (except excluded fields)
    in_frontmatter { print }
    ' "$file" > "$temp_file"
    
    # Replace original file
    mv "$temp_file" "$file"
}

# Export the function so it can be used with find -exec
export -f clean_markdown_file

# 1. Remove problematic Jekyll index files
echo "📄 Removing Jekyll index files..."
find "$DOCS_DIR" -name "index.md" -type f -delete

# 2. Clean all markdown files
echo "🔧 Cleaning Jekyll syntax from all markdown files..."
find "$DOCS_DIR" -name "*.md" -type f -exec bash -c 'clean_markdown_file "$0"' {} \;

# 3. Fix files with JSX content (rename to .mdx or remove JSX)
echo "🔍 Handling files with JSX content..."

# Find files with JSX-style content
files_with_jsx=$(grep -l "<[A-Z]\\|<div\\|<iframe\\|style={{" "$DOCS_DIR"/**/*.md 2>/dev/null || true)

if [ ! -z "$files_with_jsx" ]; then
    echo "Found files with JSX content:"
    echo "$files_with_jsx"
    
    # For now, remove the JSX content to get it working
    for file in $files_with_jsx; do
        echo "Removing JSX from: $file"
        # Remove JSX blocks (simple approach - removes content between JSX tags)
        sed -i '' '/<div style={{/,/<\/div>/d' "$file"
        sed -i '' '/<iframe/,/<\/iframe>/d' "$file"
    done
fi

# 4. Fix empty front matter
echo "🔧 Fixing empty front matter..."
find "$DOCS_DIR" -name "*.md" -type f -exec bash -c '
    file="$1"
    # Check if file has empty front matter (just --- --- with nothing between)
    if grep -q "^---$" "$file" && [ $(grep -c "^---$" "$file") -eq 2 ]; then
        content=$(awk "BEGIN{p=0} /^---$/{p++; if(p==2) p++; next} p>2{print}" "$file")
        if [ ! -z "$content" ]; then
            # Get filename without extension for title
            filename=$(basename "$file" .md)
            echo "---" > "$file"
            echo "title: \"$filename\"" >> "$file"
            echo "---" >> "$file"
            echo "" >> "$file"
            echo "$content" >> "$file"
        fi
    fi
' bash {} \;

# 5. Remove any remaining liquid includes
echo "🔧 Removing liquid includes..."
find "$DOCS_DIR" -name "*.md" -type f -exec sed -i '' '/{% include/d' {} \;
find "$DOCS_DIR" -name "*.md" -type f -exec sed -i '' '/{% raw %}/d' {} \;
find "$DOCS_DIR" -name "*.md" -type f -exec sed -i '' '/{% endraw %}/d' {} \;

# 6. Clean up category files
echo "📁 Cleaning category files..."
find "$DOCS_DIR" -name "_category_.json" -type f -delete
find "$DOCS_DIR" -name "_category_.yml" -type f -delete

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "The markdown files have been cleaned of Jekyll-specific syntax."
echo "Try running Docusaurus again:"
echo "  cd /Users/jmac/Documents/GitHub/docs"
echo "  npm run start"
echo ""
echo "If you still see errors, they will be about specific content issues we can fix."
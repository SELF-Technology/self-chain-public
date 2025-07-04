#!/bin/bash
# Quick setup script for public repository

echo "Setting up SELF documentation..."

# Check if we're in the right place
if [ ! -f "_config.yml" ]; then
    echo "Error: Please run this from the repository root"
    exit 1
fi

# Install dependencies
echo "Installing Ruby dependencies..."
bundle install

# Create CNAME file for custom domain
echo "docs.self.app" > CNAME

# Create .gitignore if it doesn't exist
if [ ! -f ".gitignore" ]; then
    cat > .gitignore << 'GITIGNORE'
_site/
.sass-cache/
.jekyll-cache/
.jekyll-metadata
vendor/
.bundle/
Gemfile.lock
GITIGNORE
fi

echo "✓ Setup complete!"
echo ""
echo "To test locally: bundle exec jekyll serve"
echo "To deploy: git add . && git commit -m 'Add documentation' && git push"

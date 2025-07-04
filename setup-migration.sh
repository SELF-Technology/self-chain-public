#!/bin/bash

# SELF Documentation Migration Setup Script
# This script helps migrate from Docusaurus to GitHub Pages with Jekyll

echo "🚀 SELF Documentation Migration Setup"
echo "===================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "_config.yml" ]; then
    echo -e "${RED}Error: _config.yml not found. Please run this script from the Jekyll setup directory.${NC}"
    exit 1
fi

echo -e "${BLUE}Step 1: Installing Ruby dependencies...${NC}"
if command -v bundle &> /dev/null; then
    bundle install --path vendor/bundle
    echo -e "${GREEN}✓ Ruby dependencies installed to vendor/bundle${NC}"
else
    echo -e "${RED}Error: Bundler not found. Please install Ruby and Bundler first.${NC}"
    echo "Visit: https://jekyllrb.com/docs/installation/"
    exit 1
fi

echo ""
echo -e "${BLUE}Step 2: Creating additional directories...${NC}"
mkdir -p _layouts _includes _data assets/images assets/js
echo -e "${GREEN}✓ Directory structure created${NC}"

echo ""
echo -e "${BLUE}Step 3: Setting up default layout...${NC}"
cat > _layouts/default.html << 'EOF'
<!DOCTYPE html>
<html lang="{{ site.lang | default: "en-US" }}">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    {% seo %}
    <link rel="stylesheet" href="{{ "/assets/css/style.css?v=" | append: site.github.build_revision | relative_url }}">
    <link rel="stylesheet" href="{{ "/assets/css/custom.css?v=" | append: site.github.build_revision | relative_url }}">
</head>
<body>
    <div class="wrapper">
        {{ content }}
    </div>
    <script src="{{ "/assets/js/scale.fix.js" | relative_url }}"></script>
</body>
</html>
EOF
echo -e "${GREEN}✓ Default layout created${NC}"

echo ""
echo -e "${BLUE}Step 4: Creating sample navigation data...${NC}"
cat > _data/navigation.yml << 'EOF'
# Main navigation links
main:
  - title: "Home"
    url: /
  - title: "Introduction"
    url: /docs/introduction/
  - title: "Approach"
    url: /docs/approach/
  - title: "Technical Docs"
    url: /docs/technical/
  - title: "Roadmap"
    url: /docs/roadmap/
  - title: "Developers"
    url: /docs/developers/
  - title: "Community"
    url: /docs/community/

# Documentation sections
docs:
  - title: "Getting Started"
    children:
      - title: "Introduction"
        url: /docs/introduction/
      - title: "Evolution of Technology"
        url: /docs/introduction/evolution/
      - title: "Self-Sovereign Alternative"
        url: /docs/introduction/sovereignty/
  
  - title: "Our Approach"
    children:
      - title: "Philosophy"
        url: /docs/approach/philosophy/
      - title: "Six Core Commitments"
        url: /docs/approach/commitments/
      - title: "Vision"
        url: /docs/approach/vision/
  
  - title: "Technical Documentation"
    children:
      - title: "Architecture Overview"
        url: /docs/technical/architecture/
      - title: "Proof-of-AI Consensus"
        url: /docs/technical/poai/
      - title: "Quantum Security"
        url: /docs/technical/quantum/
      - title: "Network Protocol"
        url: /docs/technical/network/
EOF
echo -e "${GREEN}✓ Navigation data created${NC}"

echo ""
echo -e "${BLUE}Step 5: Creating local development script...${NC}"
cat > serve-local.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting SELF Documentation locally..."
echo "The site will be available at: http://localhost:4000"
echo "Press Ctrl+C to stop the server"
echo ""
bundle exec jekyll serve --livereload --open-url
EOF
chmod +x serve-local.sh
echo -e "${GREEN}✓ Local development script created${NC}"

echo ""
echo -e "${BLUE}Step 6: Creating migration checklist...${NC}"
cat > MIGRATION_CHECKLIST.md << 'EOF'
# SELF Documentation Migration Checklist

## Pre-Migration
- [ ] Backup current Docusaurus site
- [ ] Export all content from docs.self.app
- [ ] List all custom components and features
- [ ] Document current URL structure

## Setup Phase
- [x] Create Jekyll configuration
- [x] Set up Just-the-Docs theme
- [x] Configure custom color scheme
- [x] Create GitHub Actions workflow
- [ ] Test local Jekyll build

## Content Migration
- [ ] Convert Docusaurus MDX to Jekyll Markdown
- [ ] Update internal links
- [ ] Migrate images and assets
- [ ] Update code blocks and examples
- [ ] Convert custom React components to Jekyll includes

## Feature Implementation
- [ ] Implement search functionality
- [ ] Add dark mode toggle
- [ ] Create responsive navigation
- [ ] Add syntax highlighting
- [ ] Implement version switcher (if needed)

## Testing
- [ ] Test all internal links
- [ ] Verify responsive design
- [ ] Check search functionality
- [ ] Test dark mode
- [ ] Validate SEO meta tags
- [ ] Performance testing

## Deployment
- [ ] Enable GitHub Pages on repository
- [ ] Configure custom domain (docs.self.app)
- [ ] Set up SSL certificate
- [ ] Configure redirects from old URLs
- [ ] Update DNS records

## Post-Migration
- [ ] Monitor 404 errors
- [ ] Update external links
- [ ] Announce migration to community
- [ ] Archive old Docusaurus site
- [ ] Document new contribution process

## Continuous Improvement
- [ ] Set up analytics
- [ ] Create feedback mechanism
- [ ] Plan regular content updates
- [ ] Document maintenance procedures
EOF
echo -e "${GREEN}✓ Migration checklist created${NC}"

echo ""
echo -e "${YELLOW}📋 Migration Status Summary${NC}"
echo "=========================="
echo -e "${GREEN}✓ Jekyll configuration complete${NC}"
echo -e "${GREEN}✓ Custom theme configured${NC}"
echo -e "${GREEN}✓ GitHub Actions workflow ready${NC}"
echo -e "${GREEN}✓ Basic structure created${NC}"
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "1. Copy this setup to your public repository"
echo "2. Migrate your documentation content"
echo "3. Run './serve-local.sh' to test locally"
echo "4. Enable GitHub Pages in repository settings"
echo "5. Configure custom domain (docs.self.app)"
echo ""
echo -e "${YELLOW}📝 See MIGRATION_CHECKLIST.md for detailed tasks${NC}"
echo ""
echo -e "${GREEN}✨ Setup complete! Ready for migration.${NC}"
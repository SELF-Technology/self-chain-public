# SELF Documentation - Jekyll Migration

This directory contains the Jekyll setup for migrating SELF documentation from Docusaurus to GitHub Pages.

## 🚀 Quick Start

1. **Copy to Public Repository**
   ```bash
   cp -r docs-migration/jekyll-setup/* /path/to/self-chain-public/
   ```

2. **Install Dependencies**
   ```bash
   cd /path/to/self-chain-public/
   bundle install --path vendor/bundle
   ```

3. **Run Locally**
   ```bash
   ./serve-local.sh
   # Or manually:
   bundle exec jekyll serve --livereload
   ```

4. **Deploy to GitHub Pages**
   - Push to main branch
   - Enable GitHub Pages in repository settings
   - Select "GitHub Actions" as the source

## 📁 Structure

```
.
├── _config.yml              # Jekyll configuration
├── _sass/                   # Custom styles
│   └── color_schemes/       
│       └── self.scss        # SELF brand colors
├── assets/                  
│   └── css/
│       └── custom.scss      # Additional styling
├── docs/                    # Documentation content
│   ├── introduction/
│   ├── approach/
│   ├── technical/
│   └── ...
├── .github/workflows/       
│   └── deploy-docs.yml      # Auto-deployment
├── Gemfile                  # Ruby dependencies
├── index.md                 # Homepage
└── setup-migration.sh       # Setup helper script
```

## 🎨 Customization

### Color Scheme
Edit `_sass/color_schemes/self.scss` to modify brand colors.

### Styling
Add custom CSS to `assets/css/custom.scss`.

### Navigation
Update `_data/navigation.yml` for menu structure.

## 📝 Writing Documentation

### Front Matter
```yaml
---
layout: default
title: Page Title
nav_order: 1
has_children: true
permalink: /section/
---
```

### Callout Boxes
```markdown
{: .callout .callout-info}
> **Note:** This is an informational callout.

{: .callout .callout-warning}
> **Warning:** This is a warning callout.
```

### Code Blocks
````markdown
```rust
fn main() {
    println!("Hello, SELF!");
}
```
````

## 🚢 Deployment

The GitHub Actions workflow automatically deploys to GitHub Pages when you push to main.

### Manual Deployment
```bash
# Build the site
bundle exec jekyll build

# The output is in _site/
```

## 🔧 Configuration

### Custom Domain
1. Add `docs.self.app` to repository settings
2. Create `CNAME` file with domain
3. Update DNS records

### SEO
Configure in `_config.yml`:
```yaml
title: Your Title
description: Your Description
twitter:
  username: YourTwitter
```

## 📚 Resources

- [Jekyll Documentation](https://jekyllrb.com/docs/)
- [Just the Docs Theme](https://just-the-docs.github.io/just-the-docs/)
- [GitHub Pages Guide](https://docs.github.com/en/pages)

## ✅ Migration Checklist

See `MIGRATION_CHECKLIST.md` for detailed migration tasks.

---

For questions or issues, please open an issue in the SELF repository.
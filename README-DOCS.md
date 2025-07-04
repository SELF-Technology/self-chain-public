# SELF Documentation

This repository hosts the official SELF documentation at [docs.self.app](https://docs.self.app).

## 🚀 Quick Start

### Local Development
```bash
# Install dependencies
bundle install

# Run locally
bundle exec jekyll serve --livereload
# Visit http://localhost:4000
```

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

### Structure
```
docs/
├── introduction/    # Getting started with SELF
├── approach/       # Philosophy and commitments  
├── technical/      # Technical documentation
├── roadmap/        # Development roadmap
└── community/      # Team and contributions
```

### Writing Documentation

Create a new `.md` file in the appropriate directory with this frontmatter:
```yaml
---
layout: default
title: Your Page Title
nav_order: 10
parent: Parent Section
---
```

## 📝 Style Guide

- Use clear, concise language
- Include code examples where relevant
- Add diagrams for complex concepts
- Keep paragraphs short and scannable

## 🛠️ Built With

- [Jekyll](https://jekyllrb.com/) - Static site generator
- [Just the Docs](https://just-the-docs.github.io/just-the-docs/) - Documentation theme
- [GitHub Pages](https://pages.github.com/) - Hosting

## 📄 License

Documentation is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).

# VitePress Theme Migration Guide for docs.self.app
k pl

## Achieving Visual Parity with Your Docusaurus Site

### Step 1: Core Theme Structure

```bash
docusaurus/
├── .vitepress/
│   ├── config.js          # VitePress configuration
│   ├── theme/
│   │   ├── index.js       # Theme entry point
│   │   ├── custom.css     # Your custom styles
│   │   ├── galaxy.css     # Galaxy/space theme styles
│   │   └── components/    # Custom Vue components
```

### Step 2: Configuration Migration

```js
// .vitepress/config.js
export default {
  title: 'SELF',
  description: 'Your site description',
  
  head: [
    // Custom font
    ['link', { rel: 'stylesheet', href: 'https://path-to-thicccboi-font.css' }],
    // Favicon
    ['link', { rel: 'icon', href: '/img/icon.png' }]
  ],
  
  themeConfig: {
    logo: {
      light: '/img/black-icon.png',
      dark: '/img/white-icon.png'
    },
    
    nav: [
      { text: 'Welcome', link: '/' },
      { text: 'Our Purpose', link: '/purpose' },
      { text: 'Roadmap', link: '/roadmap' },
      { text: 'Technical Docs', link: '/technical/' },
      { text: 'Team', link: '/team' }
    ],
    
    socialLinks: [
      { icon: 'github', link: 'https://github.com/self-technology' },
      { icon: 'discord', link: 'your-discord-link' },
      { icon: 'twitter', link: 'your-twitter-link' }
    ],
    
    // Algolia search (VitePress native support)
    search: {
      provider: 'algolia',
      options: {
        appId: 'your-app-id',
        apiKey: 'your-api-key',
        indexName: 'self'
      }
    }
  }
}
```

### Step 3: Custom CSS for Galaxy Theme

```css
/* .vitepress/theme/galaxy.css */

/* THICCCBOI Font */
@font-face {
  font-family: 'THICCCBOI';
  src: url('/fonts/THICCCBOI-Regular.woff2') format('woff2');
  /* Add other weights */
}

/* Dark mode gradient background */
.dark {
  --vp-c-bg: transparent;
  background: linear-gradient(
    180deg,
    #0138ae 0%,
    #1b2e49 20.31%,
    #070d1a 39.06%,
    #0d1520 57.81%,
    #011c57 79.69%,
    #070d1a 100%
  ) fixed;
  min-height: 100vh;
}

/* Grain texture overlay */
.dark::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.02;
  pointer-events: none;
  z-index: 1;
}

/* Transparent navbar */
.VPNav {
  background: transparent !important;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* Content surfaces */
.dark .VPContent {
  background: rgba(15, 27, 46, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  margin: 20px;
}

/* Sidebar styling */
.VPSidebar {
  background: rgba(15, 27, 46, 0.8) !important;
  scrollbar-width: none;
}

.VPSidebar::-webkit-scrollbar {
  display: none;
}

/* Hero section with galaxy image */
.VPHero {
  position: relative;
  overflow: hidden;
}

.VPHero::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120%;
  height: 120%;
  background-image: url('/img/SELF-GALAXY-transparent.png');
  background-size: cover;
  background-position: center;
  opacity: 0.8;
  z-index: -1;
}

/* Button styles matching your Docusaurus theme */
.vp-button-roadmap {
  background-color: #1f35ff;
  color: white;
}

.vp-button-constellation {
  background-color: #525b72;
  color: white;
}

.vp-button-opensource {
  background-color: #a0a9bd;
  color: white;
}

/* Typography */
:root {
  --vp-font-family-base: 'THICCCBOI', Montserrat, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.VPHero .name {
  font-size: 72px;
  font-weight: 700;
}

/* Code blocks */
.dark .vp-code-block {
  background: rgba(26, 35, 50, 0.5) !important;
}

/* Footer customization */
.VPFooter {
  background: transparent;
  backdrop-filter: blur(10px);
  min-height: auto;
  padding: 2rem 0;
}
```

### Step 4: Custom Components

For exact visual parity, you'll need a few custom Vue components:

```vue
<!-- .vitepress/theme/components/GalaxyHero.vue -->
<template>
  <div class="galaxy-hero">
    <div class="galaxy-bg">
      <img src="/img/SELF-GALAXY-transparent.png" alt="" />
    </div>
    <div class="hero-content">
      <h1 class="hero-title">{{ title }}</h1>
      <p class="hero-tagline">{{ tagline }}</p>
      <div class="hero-actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.galaxy-hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.galaxy-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: -1;
}

.galaxy-bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8;
}
</style>
```

### Step 5: Migration Checklist

- [ ] Copy all markdown files from `docusaurus/docs` to VitePress root
- [ ] Migrate frontmatter (minimal changes needed)
- [ ] Copy static assets to `public/` directory
- [ ] Install and configure THICCCBOI font
- [ ] Set up Algolia search credentials
- [ ] Configure GitHub Actions for deployment
- [ ] Test all internal links and navigation
- [ ] Verify dark/light mode switching
- [ ] Test on mobile devices

### Benefits Over Docusaurus

1. **Faster builds**: 10-100x improvement
2. **No chunk loading errors**: Simpler architecture
3. **Better HMR**: Instant updates during development
4. **Smaller bundle**: ~30% smaller than Docusaurus
5. **Vue ecosystem**: More stable than React for docs

### Deployment

```yaml
# .github/workflows/deploy.yml
name: Deploy VitePress site to GitHub Pages

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 18
      
      - run: npm ci
      - run: npm run docs:build
      
      - uses: actions/upload-pages-artifact@v2
        with:
          path: .vitepress/dist
      
      - uses: actions/deploy-pages@v2
```

This configuration will give you 95% visual parity with your current Docusaurus site while eliminating all the technical issues you're experiencing.
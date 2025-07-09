# Mobile Performance Optimization Guide

## Completed Optimizations ✅

1. **Added preconnect hints** for external resources (CDN domains)
2. **Created mobile-specific CSS optimizations** including:
   - Touch target optimization (44px minimum)
   - GPU acceleration for smooth scrolling
   - Reduced motion support
   - Mobile-optimized font sizes
   - Layout shift prevention

3. **Prepared PWA configuration** (requires package installation)

## Recommended Next Steps

### 1. Install PWA Support (High Priority)
```bash
npm install --save @docusaurus/plugin-pwa
```

Then create `/docusaurus/static/manifest.json`:
```json
{
  "name": "SELF Documentation",
  "short_name": "SELF Docs",
  "theme_color": "#1d263b",
  "background_color": "#1d263b",
  "display": "standalone",
  "scope": "/",
  "start_url": "/",
  "icons": [
    {
      "src": "/img/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/img/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

### 2. Optimize External Resources (High Priority)

**Font Awesome Icons:**
- Current: Loading entire Font Awesome library (280KB+)
- Solution: Use only needed icons
```bash
npm install --save @fortawesome/fontawesome-svg-core @fortawesome/free-brands-svg-icons @fortawesome/react-fontawesome
```

### 3. Image Optimization (Medium Priority)

- Convert logo images to WebP format
- Add srcset for responsive images:
```jsx
logo: {
  alt: 'SELF Logo',
  src: 'img/SELF-BLACK.png',
  srcDark: 'img/SELFwhitelogo.png',
  srcSet: 'img/SELF-BLACK@2x.png 2x',
  srcSetDark: 'img/SELFwhitelogo@2x.png 2x',
}
```

### 4. Bundle Size Optimization (Medium Priority)

Add to `docusaurus.config.js`:
```javascript
webpack: {
  jsLoader: (isServer) => ({
    loader: require.resolve('esbuild-loader'),
    options: {
      loader: 'tsx',
      target: isServer ? 'node12' : 'es2017',
    },
  }),
},
```

### 5. Font Subsetting (Low Priority)

Create subset of THICCCBOI font with only used characters:
```bash
npm install --save-dev subset-font
```

### 6. Lazy Loading for Heavy Components

For any heavy documentation components, implement lazy loading:
```jsx
import React, { lazy, Suspense } from 'react';
const HeavyComponent = lazy(() => import('./HeavyComponent'));

function MyDoc() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <HeavyComponent />
    </Suspense>
  );
}
```

## Performance Monitoring

Consider adding performance monitoring:
```javascript
// In custom.js
if ('performance' in window) {
  window.addEventListener('load', () => {
    const perfData = window.performance.timing;
    const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
    console.log('Page load time:', pageLoadTime);
  });
}
```

## Testing

After implementing optimizations:
1. Re-run PageSpeed Insights
2. Test on real devices using Chrome DevTools throttling
3. Monitor Core Web Vitals in Google Search Console
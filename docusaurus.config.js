// Load environment variables - Cloudflare Pages will provide these
// require('dotenv').config({ path: './algolia.env' });

// console.log('Algolia Config:', {
//   appId: process.env.ALGOLIA_APP_ID,
//   apiKey: process.env.ALGOLIA_SEARCH_API_KEY ? 'Set' : 'Not set'
// });

const config = {
  title: 'SELF Documentation',
  tagline: 'Creating the Future of Self-Sovereign Technology',
  url: 'https://docs.self.app',
  baseUrl: '/',
  favicon: 'img/favicon.ico',
  organizationName: 'SELF Technology',
  projectName: 'self-docs',
  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',
  headTags: [
    // Preconnect to font origins for faster loading
    {
      tagName: 'link',
      attributes: {
        rel: 'preconnect',
        href: 'https://fonts.gstatic.com',
        crossorigin: 'anonymous',
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'dns-prefetch',
        href: 'https://www.googletagmanager.com',
      },
    },
    // CSP meta tag to prevent eval warnings in development
    {
      tagName: 'meta',
      attributes: {
        'http-equiv': 'Content-Security-Policy',
        content: process.env.NODE_ENV === 'development' 
          ? "default-src * 'unsafe-inline' data: blob:;" 
          : "default-src 'self' data: blob:; script-src 'self' 'unsafe-inline' https:; style-src 'self' 'unsafe-inline' https:; img-src 'self' data: https: blob:; font-src 'self' data: https:; connect-src 'self' https:; frame-src 'self' https:;",
      },
    },
    // Preload fonts CSS file
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/fonts.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
        fetchpriority: 'high',
      },
    },
    // Noscript fallback for fonts
    {
      tagName: 'noscript',
      attributes: {},
      innerHTML: '<link rel="stylesheet" href="/css/fonts.css">',
    },
    // Defer non-critical CSS loading
    {
      tagName: 'script',
      attributes: {
        type: 'module',
        defer: 'defer',
      },
      innerHTML: `
        // Defer non-critical CSS loading using requestIdleCallback
        (function() {
          var loadCSS = function(href) {
            var link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = href;
            link.media = 'print';
            link.onload = function() { this.media = 'all'; };
            document.head.appendChild(link);
          };
          
          var loadDeferredStyles = function() {
            var stylesheets = document.querySelectorAll('link[rel="preload"][as="style"]');
            stylesheets.forEach(function(link) {
              if (link.href && !link.rel.includes('stylesheet')) {
                loadCSS(link.href);
              }
            });
          };
          
          // Use requestIdleCallback if available, otherwise setTimeout
          if ('requestIdleCallback' in window) {
            requestIdleCallback(loadDeferredStyles);
          } else {
            setTimeout(loadDeferredStyles, 1);
          }
        })();
      `,
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/img/SELF-BLACK.png',
        as: 'image',
        fetchpriority: 'high',
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/img/SELFwhitelogo.webp',
        as: 'image',
        fetchpriority: 'high',
      },
    },
    // Preload galaxy background for hero section
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/img/self-galaxy-transparent-620kb-0c8f1dfb7fdbf680cc47164363e1e089.webp',
        as: 'image',
        fetchpriority: 'low',
      },
    },
    // Preload critical CSS
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/vendor/docsearch.min.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/vendor/fontawesome-minimal.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    // Fallback for no-JS
    {
      tagName: 'noscript',
      attributes: {},
      innerHTML: '<link rel="stylesheet" href="/css/vendor/docsearch.min.css"><link rel="stylesheet" href="/css/vendor/fontawesome-minimal.css">',
    },
    // Async load non-critical CSS files
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/category-pages.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/breadcrumb-fix.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/social-icons.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/footer-social.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/theme-toggle-fix.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/css/custom-theme-toggle.css',
        as: 'style',
        onload: "this.onload=null;this.rel='stylesheet'",
      },
    },
    // Preload critical THICCCBOI fonts
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/fonts/thicccboi/THICCCBOI-Regular.woff2',
        as: 'font',
        type: 'font/woff2',
        crossorigin: 'anonymous',
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/fonts/thicccboi/THICCCBOI-Bold.woff2',
        as: 'font',
        type: 'font/woff2',
        crossorigin: 'anonymous',
      },
    },
    // Preload Font Awesome font
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/webfonts/fa-brands-400.woff2',
        as: 'font',
        type: 'font/woff2',
        crossorigin: 'anonymous',
      },
    },
  ],

  presets: [
    [
      'classic',
      {
        docs: {
          path: "./docs",
          routeBasePath: "/",
          sidebarPath: require.resolve("./sidebars.js"),
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],

  plugins: [
    // Custom plugin to modify webpack config
    function customWebpackPlugin(context, options) {
      return {
        name: 'custom-webpack-plugin',
        configureWebpack(config, isServer, utils) {
          // Completely disable eval in all environments
          if (!isServer) {
            // Add CSS optimization rules
            const cssRule = config.module.rules.find(rule => 
              rule.test && rule.test.toString().includes('css')
            );
            
            if (cssRule && cssRule.oneOf) {
              cssRule.oneOf.forEach(rule => {
                if (rule.use) {
                  rule.use.forEach(loader => {
                    if (loader.loader && loader.loader.includes('css-loader')) {
                      loader.options = {
                        ...loader.options,
                        importLoaders: 1,
                        modules: false,
                        sourceMap: false, // Disable source maps for CSS in production
                      };
                    }
                    if (loader.loader && loader.loader.includes('postcss-loader')) {
                      loader.options = {
                        ...loader.options,
                        postcssOptions: {
                          plugins: [
                            require('postcss-import')({
                              // Process @import statements at build time
                              resolve: (id, basedir) => {
                                // Custom resolve logic for CSS imports
                                return id;
                              },
                            }),
                            require('cssnano')({
                              preset: ['default', {
                                discardComments: {
                                  removeAll: true,
                                },
                              }],
                            }),
                          ],
                        },
                      };
                    }
                  });
                }
              });
            }
            // Client-side specific config
            config.devtool = 'source-map'; // Never use eval
            
            // Disable webpack's eval-based hot module replacement
            if (config.optimization) {
              config.optimization.moduleIds = 'deterministic';
              config.optimization.chunkIds = 'deterministic';
              
              // CSS optimization - create larger chunks for better performance
              config.optimization.splitChunks = {
                ...config.optimization.splitChunks,
                chunks: 'all',
                maxAsyncRequests: 6,
                maxInitialRequests: 4,
                minSize: 30000, // Increase minimum chunk size
                maxSize: 244000, // Set max size just below warning threshold
                cacheGroups: {
                  // Disable default chunks to have more control
                  default: false,
                  defaultVendors: false,
                  // Main vendor bundle
                  vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendor',
                    priority: 20,
                    reuseExistingChunk: true,
                  },
                  // CSS bundle
                  styles: {
                    name: 'styles',
                    test: /\.(css|scss|sass)$/,
                    chunks: 'all',
                    enforce: true,
                    priority: 30,
                  },
                  // Common modules
                  common: {
                    name: 'common',
                    minChunks: 2,
                    priority: 10,
                    reuseExistingChunk: true,
                  },
                },
              };
            }
            
            // Ensure no eval in development
            if (config.mode === 'development') {
              config.devtool = 'cheap-module-source-map';
              // Remove any eval-based plugins
              if (config.plugins) {
                config.plugins = config.plugins.filter(plugin => {
                  const name = plugin.constructor.name;
                  return !name.includes('Eval');
                });
              }
            }
            
            // Add performance hints - adjusted for real-world usage
            config.performance = {
              ...config.performance,
              hints: process.env.NODE_ENV === 'production' ? 'warning' : false,
              maxAssetSize: 512000, // 500KB for assets
              maxEntrypointSize: 768000, // 750KB for entrypoints
              assetFilter: function(assetFilename) {
                // Only check JS and CSS files, ignore images and fonts
                return /\.(js|css)$/.test(assetFilename);
              },
            };
          }
          
          return {};
        },
      };
    },
    // Custom plugin to inline critical CSS
    function criticalCSSPlugin(context, options) {
      return {
        name: 'critical-css-plugin',
        async loadContent() {
          const fs = require('fs').promises;
          const path = require('path');
          try {
            const criticalCssPath = path.join(__dirname, 'src/css/critical.css');
            const criticalCss = await fs.readFile(criticalCssPath, 'utf8');
            return criticalCss;
          } catch (error) {
            console.warn('Critical CSS file not found:', error);
            return '';
          }
        },
        async contentLoaded({content, actions}) {
          const {setGlobalData} = actions;
          setGlobalData({criticalCss: content});
        },
        injectHtmlTags({content}) {
          if (!content) return {};
          return {
            headTags: [
              {
                tagName: 'style',
                attributes: {
                  'data-critical': 'true',
                },
                innerHTML: content,
              },
            ],
          };
        },
      };
    },
    // Custom plugin for lazy loading images
    function lazyLoadImagesPlugin(context, options) {
      return {
        name: 'lazy-load-images-plugin',
        injectHtmlTags() {
          return {
            postBodyTags: [
              {
                tagName: 'script',
                attributes: {
                  type: 'text/javascript',
                },
                innerHTML: `
                  (function() {
                    // Native lazy loading with Intersection Observer fallback
                    if ('loading' in HTMLImageElement.prototype) {
                      // Use native lazy loading
                      const setLazyLoading = function() {
                        const images = document.querySelectorAll('img:not([loading])');
                        images.forEach(function(img) {
                          img.loading = 'lazy';
                          // Add explicit dimensions to prevent layout shift
                          if (!img.width && img.naturalWidth) {
                            img.width = img.naturalWidth;
                          }
                          if (!img.height && img.naturalHeight) {
                            img.height = img.naturalHeight;
                          }
                        });
                      };
                      
                      // Run immediately for existing images
                      if (document.readyState !== 'loading') {
                        setLazyLoading();
                      }
                      
                      // Use MutationObserver for dynamically added images
                      const observer = new MutationObserver(setLazyLoading);
                      observer.observe(document.body, {
                        childList: true,
                        subtree: true
                      });
                    } else {
                      // Fallback: Load lazysizes only if needed
                      const script = document.createElement('script');
                      script.src = 'https://cdnjs.cloudflare.com/ajax/libs/lazysizes/5.3.2/lazysizes.min.js';
                      script.async = true;
                      document.body.appendChild(script);
                    }
                  })();
                `,
              },
            ],
          };
        },
      };
    },
    /* '@docusaurus/plugin-debug', // Already included in preset */
    /* [
      '@docusaurus/plugin-pwa',
      {
        offlineModeActivationStrategies: [
          'appInstalled',
          'standalone',
          'queryString',
        ],
        pwaHead: [
          {
            tagName: 'link',
            rel: 'manifest',
            href: '/manifest.json',
          },
          {
            tagName: 'meta',
            name: 'theme-color',
            content: '#1d263b',
          },
        ],
      },
    ], */
  ],

  themeConfig: {
    metadata: [
      {name: 'description', content: 'SELF Chain documentation - Build on the people\'s blockchain with Proof-of-AI consensus, post-quantum cryptography, and human-centric design. Open-source infrastructure for self-sovereign technology, privacy-first applications, and decentralized AI validation.'},
      {name: 'keywords', content: 'self chain, proof of ai blockchain, post quantum blockchain, self sovereign technology, decentralized ai validation, people\'s blockchain, privacy first blockchain, poai consensus, color marker system, hybrid cloud blockchain'},
      {property: 'og:title', content: 'SELF Documentation - The People\'s Blockchain'},
      {property: 'og:description', content: 'Build on SELF Chain with Proof-of-AI consensus, post-quantum cryptography, and human-centric design. Open-source infrastructure for self-sovereign technology.'},
      {property: 'og:type', content: 'website'},
      {name: 'twitter:card', content: 'summary_large_image'},
      {name: 'twitter:title', content: 'SELF Documentation - The People\'s Blockchain'},
      {name: 'twitter:description', content: 'Build on SELF Chain with Proof-of-AI consensus, post-quantum cryptography, and human-centric design.'},
    ],
    // Algolia search configuration
    algolia: process.env.ALGOLIA_APP_ID && process.env.ALGOLIA_SEARCH_API_KEY ? {
      appId: process.env.ALGOLIA_APP_ID,
      apiKey: process.env.ALGOLIA_SEARCH_API_KEY,
      indexName: 'self',
      contextualSearch: true,
    } : undefined,
    colorMode: {
      defaultMode: 'dark',
      disableSwitch: false, // Enable theme switching
      respectPrefersColorScheme: false,
    },
    navbar: {
      title: '',
      logo: {
        alt: 'SELF Logo',
        src: 'img/SELF-BLACK.png',
        srcDark: 'img/SELFwhitelogo.webp',
        href: 'https://self.app',
        target: '_self', // Opens in same tab
      },
      hideOnScroll: false,
      items: process.env.ALGOLIA_APP_ID && process.env.ALGOLIA_SEARCH_API_KEY ? [
        {
          type: 'search',
          position: 'right',
        },
      ] : [],
    },
    footer: {
      style: 'dark',
      copyright: `
        <div class="footer-content-wrapper">
          <div class="footer-social-icons">
            <a href="https://discord.gg/WdMdVpA4C8" target="_blank" rel="noopener noreferrer" aria-label="Discord">
              <i class="fab fa-discord"></i>
            </a>
            <a href="https://github.com/SELF-Technology/self-chain-public" target="_blank" rel="noopener noreferrer" aria-label="GitHub">
              <i class="fab fa-github"></i>
            </a>
            <a href="https://x.com/self_hq" target="_blank" rel="noopener noreferrer" aria-label="X">
              <i class="fab fa-x-twitter"></i>
            </a>
            <a href="https://t.me/selfcommunitychat" target="_blank" rel="noopener noreferrer" aria-label="Telegram">
              <i class="fab fa-telegram"></i>
            </a>
            <a href="https://instagram.com/selfappofficial" target="_blank" rel="noopener noreferrer" aria-label="Instagram">
              <i class="fab fa-instagram"></i>
            </a>
            <a href="https://www.youtube.com/@selfcommunityvideos" target="_blank" rel="noopener noreferrer" aria-label="YouTube">
              <i class="fab fa-youtube"></i>
            </a>
            <a href="https://www.linkedin.com/company/selftechnology/" target="_blank" rel="noopener noreferrer" aria-label="LinkedIn">
              <i class="fab fa-linkedin"></i>
            </a>
          </div>
          <div class="footer-theme-toggle-wrapper"></div>
        </div>
        <div class="footer-copyright-text">
          © SELF ${new Date().getFullYear()} | 
          <a href="https://self.app/terms" target="_blank">Terms & Conditions</a> | 
          <a href="https://self.app/cookies" target="_blank">Cookie Policy</a> | 
          <a href="https://self.app/privacy" target="_blank">Privacy Policy</a> | 
          <a href="#" onclick="window.location.href='mailto:' + 'info' + '@' + 'self.app' + '?subject=Mail-from-site'; return false;">Contact Us</a>
        </div>
      `,
    },
  },
};

module.exports = config;
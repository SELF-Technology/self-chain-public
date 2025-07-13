// Load environment variables
require('dotenv').config();

// console.log('Algolia Config:', {
//   appId: process.env.ALGOLIA_APP_ID,
//   apiKey: process.env.ALGOLIA_SEARCH_API_KEY ? 'Set' : 'Not set'
// });

const config = {
  title: 'SELF Documentation',
  tagline: 'Creating the infrastructure for self-sovereign technology, privacy-first applications, and human-centric AI',
  url: 'https://docs.self.app',
  baseUrl: '/',
  favicon: 'img/favicon.ico',
  organizationName: 'SELF Technology',
  projectName: 'self-docs',
  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',
  headTags: [
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
        href: '/img/SELF GALAXY transparent 620kb-0c8f1dfb7fdbf680cc47164363e1e089.webp',
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
          path: "../docs",
          routeBasePath: "/",
          sidebarPath: require.resolve("./sidebars.js"),
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],

  /* plugins: [
    [
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
    ],
  ], */

  themeConfig: {
    ...(process.env.ALGOLIA_APP_ID && process.env.ALGOLIA_SEARCH_API_KEY ? {
      algolia: {
        appId: process.env.ALGOLIA_APP_ID,
        apiKey: process.env.ALGOLIA_SEARCH_API_KEY,
        indexName: 'self',
      },
    } : {}),
    colorMode: {
      defaultMode: 'dark',
      disableSwitch: true, // Theme toggle now in footer only
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
      items: [
        {
          type: 'search',
          position: 'right',
        },
      ],
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
          <a href="mailto:info@self.app?subject=Mail%20from%20site">Contact Us</a>
        </div>
      `,
    },
  },
};

module.exports = config;
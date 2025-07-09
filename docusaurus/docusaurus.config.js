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
        href: '/img/SELFwhitelogo.png',
        as: 'image',
        fetchpriority: 'high',
      },
    },
    // Preconnect to external domains
    {
      tagName: 'link',
      attributes: {
        rel: 'preconnect',
        href: 'https://cdn.jsdelivr.net',
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preconnect',
        href: 'https://cdnjs.cloudflare.com',
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

  plugins: [
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
  ],

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
      disableSwitch: false,
      respectPrefersColorScheme: false,
    },
    navbar: {
      title: '',
      logo: {
        alt: 'SELF Logo',
        src: 'img/SELF-BLACK.png',
        srcDark: 'img/SELFwhitelogo.png',
        width: 150,
        height: 32,
      },
      hideOnScroll: false,
      items: [
        {
          type: 'search',
          position: 'right',
        },
        {
          href: 'https://github.com/SELF-Technology/self-chain-public',
          position: 'right',
          className: 'header-github-link',
          'aria-label': 'GitHub repository',
        },
      ],
    },
    footer: {
      style: 'dark',
      copyright: `
        <div class="custom-theme-toggle-wrapper"></div>
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
        <div class="footer-copyright-text">Copyright © ${new Date().getFullYear()} SELF</div>
      `,
    },
  },
};

module.exports = config;
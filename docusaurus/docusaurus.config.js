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
  scripts: [
    {
      src: '/custom-theme-toggle.js',
      async: false,
    }
  ],
  headTags: [
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/img/SELF-BLACK.png',
        as: 'image',
      },
    },
    {
      tagName: 'link',
      attributes: {
        rel: 'preload',
        href: '/img/SELFwhitelogo.png',
        as: 'image',
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
      disableSwitch: true,
      respectPrefersColorScheme: false,
    },
    navbar: {
      title: '',
      logo: {
        alt: 'SELF Logo',
        src: 'img/SELF-BLACK.png',
        srcDark: 'img/SELFwhitelogo.png',
      },
      items: [],
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
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
    colorMode: {
      defaultMode: 'dark',
      disableSwitch: false,
      respectPrefersColorScheme: false,
    },
    navbar: {
      logo: {
        alt: 'SELF Logo',
        src: 'img/SELF BLACK.png',
        srcDark: 'img/SELFwhitelogo.png',
        style: {
          height: '32px',
          marginRight: '8px',
        },
      },
      items: [],
    },
    footer: {
      copyright: `Copyright © ${new Date().getFullYear()} SELF`,
    },
  },
};

module.exports = config;
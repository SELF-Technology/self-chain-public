const config = {
  title: 'SELF Documentation',
  url: 'https://docs.self.app',
  baseUrl: '/',
  favicon: 'img/favicon.ico',
  organizationName: 'SELF Technology',
  projectName: 'self-docs',

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
    navbar: {
      title: 'SELF Documentation',
      items: [],
    },
    footer: {
      copyright: `Copyright © ${new Date().getFullYear()} SELF Technology`,
    },
  },
};

module.exports = config;
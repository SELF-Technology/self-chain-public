// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const { themes } = require("prism-react-renderer");
const lightTheme = themes.github;
const darkTheme = themes.dracula;

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "SELF Documentation",
  tagline: "Creating the future of self-sovereign technology",
  favicon: "img/favicon.png",

  // Set the production url of your site here
  url: "https://self-technology.github.io",
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: "/self-chain-public/",

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "SELF-Technology", // Usually your GitHub org/user name.
  projectName: "self-chain-public", // Usually your repo name.

  onBrokenLinks: "warn",
  onBrokenMarkdownLinks: "warn",

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          path: "../docs",
          routeBasePath: "/",
          sidebarPath: require.resolve("./sidebars.js"),
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      }),
    ],
  ],
  themeConfig:
  /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
  ({
    // algolia: {
    //   appId: process.env.ALGOLIA_APP_ID,
    //   apiKey: process.env.ALGOLIA_SEARCH_API_KEY,
    //   indexName: 'self',
    // },
    image: "img/SELFsocialcard.png",

    colorMode: {
      defaultMode: 'dark',
      disableSwitch: false, // set to true to force dark mode only
      respectPrefersColorScheme: false,
    },

    navbar: {
      logo: {
        alt: "SELF Logo",
        src: "img/SELFblacklogo.png",
        srcDark: "img/SELFwhitelogo.png",
        href: "/",
      },
      items: [
        { to: "/", label: "Documentation", position: "left" },
        {
          href: "https://github.com/self-technology/self-chain-public",
          label: "GitHub",
          position: "right",
        },
      ],
    },
    metadata: [
      {
        name: "keywords",
        content:
          "SELF, Developer Relations, Community Engagement, Documentation, Tutorial",
      },
    ],
    footer: {
      style: "dark",
      links: [
        {
          
          items: [
            {
              label: "Main Site",
                href: "https://self.app",
            },
            {
              label: "Launch App",
                href: "https://your.self.app",
            },
            {
              label: "Status",
              href: "https://status.self.app",
            },
          ],
        },
        {
          
          items: [
            {
              label: "X",
              href: "https://x.com/self_hq",
            },
            {
              label: "Telegram",
              href: "https://t.me/selfcommunitychat",
            },
            {
              label: "Discord",
              href: "https://discord.gg/selfcommunity",
            },
          ],
        },
        {
       
          items: [
            {
              label: "Instagram",
              href: "https://instagram.com/selfappofficial",
            },
            {
              label: "YouTube",
              href: "https://www.youtube.com/@selfcommunityvideos",
            },
              {
              label: "Linkedin",
              href: "https://www.linkedin.com/company/selftechnology/",
            },
          ],
        },
        {
          
          items: [
            {
              label: "Terms & Conditions",
              href: "https://www.self.app/terms.html",
            },
            {
              label: "Cookie Policy",
              href: "https://www.self.app/cookies.html",
            },
            {
              label: "Privacy Policy",
              href: "https://www.self.app/privacy.html",
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} SELF | info@self.app`,
    },
    prism: {
      theme: lightTheme,
      darkTheme: darkTheme,
    },
  }),
  plugins: [
    function (context, options) {
      return {
        name: 'custom-webpack-config',
        configureWebpack(config, isServer, utils) {
          return {
            output: {
              ...config.output,
              publicPath: '/self-chain-public/',
              chunkFilename: isServer ? 'chunks/[name].js' : 'assets/js/[name].[contenthash].js',
            },
          };
        },
      };
    },
  ],
};

module.exports = config;

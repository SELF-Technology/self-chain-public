module.exports = function (context, options) {
  return {
    name: 'webpack-chunk-fix-plugin',
    configureWebpack(config, isServer, utils) {
      if (!isServer) {
        return {
          output: {
            ...config.output,
            publicPath: '/self-chain-public/',
          },
        };
      }
      return {};
    },
  };
};
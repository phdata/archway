/* config-overrides.js */

const path = require('path');
const paths = require('react-scripts-ts/config/paths');
paths.appBuild = path.join(path.dirname(paths.appBuild), 'dist');

const tsImportPluginFactory = require('ts-import-plugin')
const { getLoader } = require("react-app-rewired");
const rewireLess = require('react-app-rewire-less');

module.exports = function override(config, env) {
  const tsLoader = getLoader(
    config.module.rules,
    rule =>
      rule.loader &&
      typeof rule.loader === 'string' &&
      rule.loader.includes('ts-loader')
  );

  tsLoader.options = {
    getCustomTransformers: () => ({
      before: [ tsImportPluginFactory({
        libraryDirectory: 'es',
        libraryName: 'antd',
        style: true,
      }) ]
    })
  };

  config = rewireLess.withLoaderOptions({
    javascriptEnabled: true,
    modifyVars: {
      "@font-family": "Roboto, sans-serif",
      "@layout-header-background": "#2E4052",
      "@menu-dark-color": "#F0F3F5",
      "@menu-dark-submenu-bg": "#F0F3F5",
      "@primary-color": "#1DA57A",
      "@tabs-highlight-color": "#333333",
      "@tabs-ink-bar-color": "#333333"
    },
  })(config, env);
    
  return config;
}

/* config-overrides.js */
const { override, fixBabelImports, addLessLoader } = require('customize-cra');

const path = require('path');
const paths = require('react-scripts/config/paths');
paths.appBuild = path.join(path.dirname(paths.appBuild), 'dist');

module.exports = override(
  fixBabelImports('import', {
    libraryName: 'antd',
    libraryDirectory: 'es',
    style: true, // change importing css to less
  }),
  addLessLoader({
    javascriptEnabled: true,
    modifyVars: {
      '@font-family': 'Roboto, sans-serif',
      '@layout-header-background': '#2E4052',
      '@menu-dark-color': '#F0F3F5',
      '@menu-dark-submenu-bg': '#F0F3F5',
      '@primary-color': '#2D7493',
      '@tabs-highlight-color': '#333333',
      '@tabs-ink-bar-color': '#333333',
    },
  })
);

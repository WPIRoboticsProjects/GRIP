var webpack = require('webpack');
var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var autoprefixer = require('autoprefixer');

var APP_DIR = path.join(__dirname, '..', 'app');

module.exports = {
  debug: true,
  devtool: 'inline-source-map',
  entry: ['webpack-hot-middleware/client', './app/index.tsx'],
  module: {
    preLoaders: [{
      test: /\.tsx?$/,
      loader: 'tslint',
      include: APP_DIR
    }],
    loaders: [{
      test: /\.tsx?$/,
      loaders: ['babel', 'ts'],
      include: APP_DIR
    },
    {
      test: /\.jsx?$/,
      loader: 'babel-loader',
      exclude: /node_modules/,
      query: {
        presets: ['es2015']
      }
    },
    {
      test: /\.scss$/,
      loader: ExtractTextPlugin.extract('typings-for-css-modules?modules&sass&namedExport'),
    },
    {
      test: /\.css$/,
      loader: ExtractTextPlugin.extract('typings-for-css-modules?modules&namedExport'),
      include: /flexboxgrid/,
    }],
  },
  output: {
    filename: 'app.js',
    path: path.join(__dirname, '..', 'build'),
    publicPath: '/static/'
  },
  postcss: [autoprefixer],
  plugins: [
    // compiled css (single file only)
    new ExtractTextPlugin('appStyles.css', { allChunks: true }),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NoErrorsPlugin()
  ],
  resolve: {
    root: [path.resolve('../app')],
    // along the way, subsequent file(s) to be consumed by webpack
    extensions: ['', '.jsx', '.js', '.tsx', '.ts', '.css', '.scss', '.json'],
    modulesDirectories: [
      'node_modules',
      path.resolve(__dirname, '..', './node_modules')
    ]
  }
};

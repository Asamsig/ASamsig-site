const webpack = require("webpack");
const path = require("path");

const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const StaticSiteGeneratorPlugin = require('static-site-generator-webpack-plugin');
const OfflinePlugin = require('offline-plugin');

module.exports = {
  mode: "production",
  entry: {
    "site-opt": [path.resolve(__dirname, "./opt-launcher.js")]
  },
  output: {
    "path": path.resolve(__dirname, "../../../../build"),
    publicPath: '/',
    "filename": "[name]-bundle.js",
    libraryTarget: 'umd'
  },
  resolve: {
    alias: {
      "resources": path.resolve(__dirname, "../../../../src/main/resources")
    }
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [{
          loader: 'css-loader',
          options: {minimize: true}
        }]
      },
      // url loader for images
      {
        test: /\.(jpg|png|svg)$/,
        use: ['url-loader']
      }
    ]
  },
  plugins: [
    new CopyWebpackPlugin({
      patterns: [
        {
          from: path.resolve(__dirname, "../../../../public"),
          globOptions: {
            ignore: ["**/404.html", "**/404-fastopt.html"]
          }
        }
      ]
    }),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, "../../../../public/404.html"),
      filename: "404.html"
    }),
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('production')
      }
    }),
    new StaticSiteGeneratorPlugin({
      crawl: true,
      globals: {
        window: {},
        ssr: true,
        fs: require('fs'),
        __dirname: __dirname
      }
    }),
    new OfflinePlugin()
  ]
};

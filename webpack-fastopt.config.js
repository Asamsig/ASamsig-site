const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  mode: "development",
  entry: {
    "dependencies": ["./asamsig-site-fastopt-entrypoint.js"],
    "asamsig-site-fastopt": ["./hot-launcher.js"]
  },
  output: {
    path: __dirname,
    publicPath: '/',
    filename: "[name]-library.js",
    library: "appLibrary",
    libraryTarget: "var"
  },
  devtool: "source-map",
  resolve: {
    alias: {
      "resources": path.resolve(__dirname, "../../../../src/main/resources")
    }
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['css-loader']
      },
      // url loader for images
      {
        test: /\.(jpg|png|svg)$/,
        use: ['url-loader']
      }
    ],
    noParse: function (content) {
      return content.endsWith("-fastopt.js");
    }
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
      template: path.resolve(__dirname, "../../../../public/404-fastopt.html"),
      inject: false,
      filename: "404.html"
    })
  ],
  devServer: {
    historyApiFallback: {
      index: '404.html'
    }
  }
};

var path = require('path');

const sjsConfig = require('./scalajs.webpack.config');
sjsConfig.target = 'node';
sjsConfig.mode = 'development'
sjsConfig.output.libraryTarget = 'commonjs2';
module.exports = sjsConfig
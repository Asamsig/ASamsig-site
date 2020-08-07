var opt = require("./asamsig-site-opt.js");

if (typeof ssr !== 'undefined') {
    module.exports = function render(locals, callback) {
      callback(null, opt.ssr(locals.path));
    };
} else {
    opt.hydrate();
}
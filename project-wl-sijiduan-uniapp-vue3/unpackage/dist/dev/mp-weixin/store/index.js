"use strict";
const common_vendor = require("../common/vendor.js");
const store_modules_global = require("./modules/global.js");
const store_modules_user = require("./modules/user.js");
const store = common_vendor.createStore({
  ...store_modules_global.global,
  modules: {
    user: store_modules_user.user
  }
});
exports.store = store;
//# sourceMappingURL=../../.sourcemap/mp-weixin/store/index.js.map

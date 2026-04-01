"use strict";
const common_vendor = require("./common/vendor.js");
const store_index = require("./store/index.js");
if (!Math) {
  "./pages/pickup/index.js";
  "./pages/delivery/index.js";
  "./pages/my/index.js";
  "./pages/login/index.js";
  "./subPages/pickup-detail/index.js";
  "./subPages/delivery-detail/index.js";
}
const _sfc_main = {
  __name: "App",
  setup(__props) {
    common_vendor.onLaunch(() => {
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        setTimeout(() => {
          common_vendor.index.reLaunch({ url: "/pages/login/index" });
        }, 100);
      }
    });
    return () => {
    };
  }
};
const NavBar = () => "./components/Navbar/index.js";
const app = common_vendor.createApp(_sfc_main);
app.use(store_index.store);
app.mount("#app");
app.component("nav-bar", NavBar);
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map

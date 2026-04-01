"use strict";
const common_vendor = require("../../common/vendor.js");
const pages_api_login = require("../api/login.js");
if (!Math) {
  NavBar();
}
const NavBar = () => "../../components/Navbar/index.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const loading = common_vendor.ref(false);
    const store = common_vendor.useStore();
    const handleWxLogin = () => {
      if (loading.value)
        return;
      loading.value = true;
      common_vendor.index.login({
        provider: "weixin",
        success: (loginRes) => {
          if (!loginRes.code) {
            loading.value = false;
            common_vendor.index.showToast({ title: "获取登录凭证失败", icon: "none" });
            return;
          }
          pages_api_login.wxLogin({ code: loginRes.code, role: "courier" }).then((data) => {
            const token = data && data.token;
            if (!token) {
              common_vendor.index.showToast({ title: "登录失败：无 token", icon: "none" });
              return;
            }
            common_vendor.index.setStorageSync("token", token);
            const info = data.userInfo || {};
            if (info.name)
              common_vendor.index.setStorageSync("nickName", info.name);
            if (info.phone)
              common_vendor.index.setStorageSync("phone", info.phone);
            if (info.avatar)
              common_vendor.index.setStorageSync("avatarUrl", info.avatar);
            store.commit("user/setIsLoginSuccess", true);
            common_vendor.index.switchTab({ url: "/pages/pickup/index" });
          }).catch((err) => {
            const msg = err && err.message || "登录失败";
            common_vendor.index.showToast({ title: msg, icon: "none", duration: 2500 });
          }).finally(() => {
            loading.value = false;
          });
        },
        fail: () => {
          loading.value = false;
          common_vendor.index.showToast({ title: "微信登录不可用", icon: "none" });
        }
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.p({
          title: "登录"
        }),
        b: loading.value,
        c: common_vendor.o(handleWxLogin, "5b")
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d08ef7d4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/index.js.map

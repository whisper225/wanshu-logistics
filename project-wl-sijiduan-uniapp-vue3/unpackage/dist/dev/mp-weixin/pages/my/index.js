"use strict";
const common_vendor = require("../../common/vendor.js");
const pages_api_login = require("../api/login.js");
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const capsuleBottom = common_vendor.ref(0);
    const nickName = common_vendor.ref("");
    const phone = common_vendor.ref("");
    common_vendor.onLoad(() => {
      common_vendor.index.getSystemInfo({
        success: (res) => {
          capsuleBottom.value = common_vendor.index.getMenuButtonBoundingClientRect().bottom;
        }
      });
    });
    common_vendor.onShow(() => {
      nickName.value = common_vendor.index.getStorageSync("nickName") || "";
      phone.value = common_vendor.index.getStorageSync("phone") || "";
    });
    const handleAbout = () => {
      common_vendor.index.showModal({
        title: "关于",
        content: "万枢物流 司机端 v1.0.0",
        showCancel: false
      });
    };
    const handleLogout = () => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定退出登录？",
        success: (res) => {
          if (res.confirm) {
            pages_api_login.logout().catch(() => {
            });
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.removeStorageSync("nickName");
            common_vendor.index.removeStorageSync("phone");
            common_vendor.index.removeStorageSync("avatarUrl");
            common_vendor.index.reLaunch({ url: "/pages/login/index" });
          }
        }
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(nickName.value || "司机"),
        b: phone.value
      }, phone.value ? {
        c: common_vendor.t(phone.value)
      } : {}, {
        d: capsuleBottom.value + "px",
        e: common_vendor.o(handleAbout, "cc"),
        f: common_vendor.o(handleLogout, "d9")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f97bc692"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/my/index.js.map

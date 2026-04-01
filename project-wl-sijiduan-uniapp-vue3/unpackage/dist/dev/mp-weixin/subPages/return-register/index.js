"use strict";
const common_vendor = require("../../common/vendor.js");
const pages_api_driver = require("../../pages/api/driver.js");
if (!Math) {
  NavBar();
}
const NavBar = () => "../../components/Navbar/index.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const description = common_vendor.ref("");
    const imageList = common_vendor.ref([]);
    const submitting = common_vendor.ref(false);
    let taskId = null;
    common_vendor.onLoad((options) => {
      taskId = options.taskId;
    });
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 9 - imageList.value.length,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          imageList.value = [...imageList.value, ...res.tempFilePaths];
        }
      });
    };
    const removeImage = (idx) => {
      imageList.value.splice(idx, 1);
    };
    const handleSubmit = async () => {
      if (submitting.value)
        return;
      submitting.value = true;
      try {
        const images = imageList.value.length ? imageList.value.join(",") : void 0;
        await pages_api_driver.createReturnRegister(taskId, description.value || void 0, images);
        common_vendor.index.showToast({ title: "登记成功", icon: "success" });
        setTimeout(() => common_vendor.index.navigateBack(), 1500);
      } catch (e) {
        common_vendor.index.showToast({ title: e.message || "提交失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          title: "回车登记"
        }),
        b: description.value,
        c: common_vendor.o(($event) => description.value = $event.detail.value, "44"),
        d: common_vendor.f(imageList.value, (img, idx, i0) => {
          return {
            a: img,
            b: common_vendor.o(($event) => removeImage(idx), idx),
            c: idx
          };
        }),
        e: imageList.value.length < 9
      }, imageList.value.length < 9 ? {
        f: common_vendor.o(chooseImage, "68")
      } : {}, {
        g: submitting.value,
        h: common_vendor.o(handleSubmit, "7b")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d59a5010"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subPages/return-register/index.js.map

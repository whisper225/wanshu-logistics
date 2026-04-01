"use strict";
const common_vendor = require("../../common/vendor.js");
const pages_api_courier = require("../../pages/api/courier.js");
if (!Math) {
  NavBar();
}
const NavBar = () => "../../components/Navbar/index.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const task = common_vendor.ref(null);
    const remark = common_vendor.ref("");
    const submitting = common_vendor.ref(false);
    let taskId = null;
    common_vendor.onLoad((options) => {
      taskId = options.id;
      loadDetail();
    });
    const loadDetail = async () => {
      try {
        const list = await pages_api_courier.getPickupTasks();
        task.value = (list || []).find((item) => String(item.id) === String(taskId)) || null;
      } catch (e) {
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      }
    };
    const callPhone = (phone) => {
      if (phone)
        common_vendor.index.makePhoneCall({ phoneNumber: phone });
    };
    const handleComplete = async () => {
      if (submitting.value)
        return;
      common_vendor.index.showModal({
        title: "确认揽收",
        content: "确定已完成揽收？",
        success: async (res) => {
          if (!res.confirm)
            return;
          submitting.value = true;
          try {
            await pages_api_courier.completePickup(taskId, remark.value || void 0);
            common_vendor.index.showToast({ title: "揽收成功", icon: "success" });
            setTimeout(() => common_vendor.index.navigateBack(), 1500);
          } catch (e) {
            common_vendor.index.showToast({ title: e.message || "操作失败", icon: "none" });
          } finally {
            submitting.value = false;
          }
        }
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          title: "揽收详情"
        }),
        b: task.value
      }, task.value ? common_vendor.e({
        c: common_vendor.t(task.value.statusText),
        d: common_vendor.t(task.value.taskNumber),
        e: task.value.assignTime
      }, task.value.assignTime ? {
        f: common_vendor.t(task.value.assignTime)
      } : {}, {
        g: task.value.senderName
      }, task.value.senderName ? {
        h: common_vendor.t(task.value.senderName),
        i: common_vendor.t(task.value.senderPhone),
        j: common_vendor.o(($event) => callPhone(task.value.senderPhone), "9b"),
        k: common_vendor.t(task.value.senderAddress)
      } : {}, {
        l: task.value.goodsName
      }, task.value.goodsName ? common_vendor.e({
        m: common_vendor.t(task.value.goodsName),
        n: task.value.weight
      }, task.value.weight ? {
        o: common_vendor.t(task.value.weight)
      } : {}) : {}, {
        p: task.value.status === 1
      }, task.value.status === 1 ? {
        q: remark.value,
        r: common_vendor.o(($event) => remark.value = $event.detail.value, "03")
      } : {}) : {}, {
        s: task.value && task.value.status === 1
      }, task.value && task.value.status === 1 ? {
        t: submitting.value,
        v: common_vendor.o(handleComplete, "03")
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-29b6b251"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subPages/pickup-detail/index.js.map

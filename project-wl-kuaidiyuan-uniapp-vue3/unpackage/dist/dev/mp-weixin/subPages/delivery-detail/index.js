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
    const submitting = common_vendor.ref(false);
    const showRejectInput = common_vendor.ref(false);
    const rejectReason = common_vendor.ref("");
    let taskId = null;
    common_vendor.onLoad((options) => {
      taskId = options.id;
      loadDetail();
    });
    const loadDetail = async () => {
      try {
        const list = await pages_api_courier.getDeliveryTasks();
        task.value = (list || []).find((item) => String(item.id) === String(taskId)) || null;
      } catch (e) {
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      }
    };
    const callPhone = (phone) => {
      if (phone)
        common_vendor.index.makePhoneCall({ phoneNumber: phone });
    };
    const handleSign = async () => {
      if (submitting.value)
        return;
      common_vendor.index.showModal({
        title: "确认签收",
        content: "确认收件人已签收？",
        success: async (res) => {
          if (!res.confirm)
            return;
          submitting.value = true;
          try {
            await pages_api_courier.signDelivery(taskId);
            common_vendor.index.showToast({ title: "签收成功", icon: "success" });
            setTimeout(() => common_vendor.index.navigateBack(), 1500);
          } catch (e) {
            common_vendor.index.showToast({ title: e.message || "操作失败", icon: "none" });
          } finally {
            submitting.value = false;
          }
        }
      });
    };
    const handleReject = async () => {
      if (submitting.value)
        return;
      submitting.value = true;
      try {
        await pages_api_courier.rejectDelivery(taskId, rejectReason.value || void 0);
        common_vendor.index.showToast({ title: "已拒收", icon: "success" });
        setTimeout(() => common_vendor.index.navigateBack(), 1500);
      } catch (e) {
        common_vendor.index.showToast({ title: e.message || "操作失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          title: "派件详情"
        }),
        b: task.value
      }, task.value ? common_vendor.e({
        c: common_vendor.t(task.value.statusText),
        d: common_vendor.t(task.value.taskNumber),
        e: task.value.waybillNumber
      }, task.value.waybillNumber ? {
        f: common_vendor.t(task.value.waybillNumber)
      } : {}, {
        g: task.value.assignTime
      }, task.value.assignTime ? {
        h: common_vendor.t(task.value.assignTime)
      } : {}, {
        i: task.value.receiverName
      }, task.value.receiverName ? {
        j: common_vendor.t(task.value.receiverName),
        k: common_vendor.t(task.value.receiverPhone),
        l: common_vendor.o(($event) => callPhone(task.value.receiverPhone), "0f"),
        m: common_vendor.t(task.value.receiverAddress)
      } : {}, {
        n: task.value.goodsName
      }, task.value.goodsName ? common_vendor.e({
        o: common_vendor.t(task.value.goodsName),
        p: task.value.weight
      }, task.value.weight ? {
        q: common_vendor.t(task.value.weight)
      } : {}) : {}, {
        r: showRejectInput.value
      }, showRejectInput.value ? {
        s: rejectReason.value,
        t: common_vendor.o(($event) => rejectReason.value = $event.detail.value, "c4")
      } : {}) : {}, {
        v: task.value && task.value.status === 1
      }, task.value && task.value.status === 1 ? common_vendor.e({
        w: !showRejectInput.value
      }, !showRejectInput.value ? {
        x: common_vendor.o(($event) => showRejectInput.value = true, "b4"),
        y: submitting.value,
        z: common_vendor.o(handleSign, "c9")
      } : {
        A: common_vendor.o(($event) => {
          showRejectInput.value = false;
          rejectReason.value = "";
        }, "27"),
        B: submitting.value,
        C: common_vendor.o(handleReject, "e3")
      }) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c9fcc13c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subPages/delivery-detail/index.js.map

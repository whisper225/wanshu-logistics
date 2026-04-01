"use strict";
const common_vendor = require("../../../common/vendor.js");
const pages_api_courier = require("../../api/courier.js");
const _sfc_main = {
  __name: "taskList",
  props: {
    status: {
      type: Number,
      default: void 0
    }
  },
  emits: ["refresh-done"],
  setup(__props, { expose: __expose, emit: __emit }) {
    const props = __props;
    const emit = __emit;
    const list = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const loadTasks = async () => {
      loading.value = true;
      try {
        const data = await pages_api_courier.getPickupTasks(props.status);
        list.value = data || [];
      } catch (e) {
        common_vendor.index.showToast({ title: e.message || "加载失败", icon: "none" });
        list.value = [];
      } finally {
        loading.value = false;
        emit("refresh-done");
      }
    };
    const toDetail = (item) => {
      common_vendor.index.navigateTo({
        url: `/subPages/pickup-detail/index?id=${item.id}`
      });
    };
    __expose({ loadTasks });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: list.value.length === 0 && !loading.value
      }, list.value.length === 0 && !loading.value ? {} : {}, {
        b: common_vendor.f(list.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.taskNumber),
            b: common_vendor.t(item.statusText),
            c: common_vendor.n("status-" + item.status),
            d: item.senderName
          }, item.senderName ? common_vendor.e({
            e: common_vendor.t(item.senderName),
            f: common_vendor.t(item.senderPhone),
            g: common_vendor.t(item.senderAddress),
            h: item.goodsName
          }, item.goodsName ? {
            i: common_vendor.t(item.goodsName),
            j: common_vendor.t(item.weight ? item.weight + "kg" : "")
          } : {}) : {}, {
            k: item.assignTime
          }, item.assignTime ? {
            l: common_vendor.t(item.assignTime)
          } : {}, {
            m: item.id,
            n: common_vendor.o(($event) => toDetail(item), item.id)
          });
        })
      });
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a14ad68a"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/pickup/components/taskList.js.map

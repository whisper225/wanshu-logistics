"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Math) {
  TaskList();
}
const TaskList = () => "./components/taskList.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const tabs = [
      { label: "全部", value: void 0 },
      { label: "已分配", value: 1 },
      { label: "已完成", value: 2 },
      { label: "已取消", value: 3 }
    ];
    const currentTab = common_vendor.ref(0);
    const tabCounts = common_vendor.ref([0, 0, 0, 0]);
    const taskListRef = common_vendor.ref();
    const capsuleBottom = common_vendor.ref(0);
    const listPadTop = common_vendor.computed(() => capsuleBottom.value + 90);
    common_vendor.onLoad(() => {
      common_vendor.index.getSystemInfo({
        success: (res) => {
          capsuleBottom.value = common_vendor.index.getMenuButtonBoundingClientRect().bottom;
        }
      });
    });
    common_vendor.onShow(() => {
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        common_vendor.index.reLaunch({ url: "/pages/login/index" });
        return;
      }
      taskListRef.value && taskListRef.value.loadTasks();
    });
    common_vendor.onPullDownRefresh(() => {
      taskListRef.value && taskListRef.value.loadTasks();
    });
    const switchTab = (idx) => {
      currentTab.value = idx;
      taskListRef.value && taskListRef.value.loadTasks();
    };
    const onRefreshDone = () => {
      common_vendor.index.stopPullDownRefresh();
    };
    return (_ctx, _cache) => {
      return {
        a: capsuleBottom.value + "px",
        b: common_vendor.f(tabs, (tab, idx, i0) => {
          return common_vendor.e({
            a: common_vendor.t(tab.label),
            b: tabCounts.value[idx]
          }, tabCounts.value[idx] ? {
            c: common_vendor.t(tabCounts.value[idx])
          } : {}, {
            d: currentTab.value === idx
          }, currentTab.value === idx ? {} : {}, {
            e: tab.value,
            f: currentTab.value === idx ? 1 : "",
            g: common_vendor.o(($event) => switchTab(idx), tab.value)
          });
        }),
        c: common_vendor.sr(taskListRef, "003520e8-0", {
          "k": "taskListRef"
        }),
        d: common_vendor.o(onRefreshDone, "61"),
        e: common_vendor.p({
          status: tabs[currentTab.value].value
        }),
        f: listPadTop.value + "px"
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-003520e8"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/pickup/index.js.map

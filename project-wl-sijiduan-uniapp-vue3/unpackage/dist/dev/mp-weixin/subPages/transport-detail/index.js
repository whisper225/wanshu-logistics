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
    const task = common_vendor.ref(null);
    const submitting = common_vendor.ref(false);
    let taskId = null;
    common_vendor.onLoad((options) => {
      taskId = options.id;
      loadDetail();
    });
    const loadDetail = async () => {
      try {
        const data = await pages_api_driver.getTransportDetail(taskId);
        task.value = data;
      } catch (e) {
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      }
    };
    const handleDepart = () => {
      if (submitting.value)
        return;
      common_vendor.index.showModal({
        title: "出库确认",
        content: "确认已完成出库，开始运输？",
        success: async (res) => {
          if (!res.confirm)
            return;
          submitting.value = true;
          try {
            await pages_api_driver.confirmDeparture(taskId);
            common_vendor.index.showToast({ title: "出库成功", icon: "success" });
            loadDetail();
          } catch (e) {
            common_vendor.index.showToast({ title: e.message || "操作失败", icon: "none" });
          } finally {
            submitting.value = false;
          }
        }
      });
    };
    const handleArrive = () => {
      if (submitting.value)
        return;
      common_vendor.index.showModal({
        title: "入库确认",
        content: "确认已到达目的地，完成运输？",
        success: async (res) => {
          if (!res.confirm)
            return;
          submitting.value = true;
          try {
            await pages_api_driver.confirmArrival(taskId);
            common_vendor.index.showToast({ title: "入库成功", icon: "success" });
            loadDetail();
          } catch (e) {
            common_vendor.index.showToast({ title: e.message || "操作失败", icon: "none" });
          } finally {
            submitting.value = false;
          }
        }
      });
    };
    const toReturnRegister = () => {
      common_vendor.index.navigateTo({
        url: `/subPages/return-register/index?taskId=${taskId}`
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.p({
          title: "运输详情"
        }),
        b: task.value
      }, task.value ? common_vendor.e({
        c: common_vendor.t(task.value.statusText),
        d: common_vendor.n("status-bg-" + task.value.status),
        e: common_vendor.t(task.value.taskNumber),
        f: common_vendor.t(task.value.startOrganId),
        g: common_vendor.t(task.value.endOrganId),
        h: task.value.planDepartTime
      }, task.value.planDepartTime ? {
        i: common_vendor.t(task.value.planDepartTime)
      } : {}, {
        j: task.value.actualDepartTime
      }, task.value.actualDepartTime ? {
        k: common_vendor.t(task.value.actualDepartTime)
      } : {}, {
        l: task.value.planArriveTime
      }, task.value.planArriveTime ? {
        m: common_vendor.t(task.value.planArriveTime)
      } : {}, {
        n: task.value.actualArriveTime
      }, task.value.actualArriveTime ? {
        o: common_vendor.t(task.value.actualArriveTime)
      } : {}, {
        p: common_vendor.t(task.value.waybillCount || 0),
        q: task.value.loadWeight
      }, task.value.loadWeight ? {
        r: common_vendor.t(task.value.loadWeight)
      } : {}, {
        s: task.value.loadVolume
      }, task.value.loadVolume ? {
        t: common_vendor.t(task.value.loadVolume)
      } : {}, {
        v: task.value.waybills && task.value.waybills.length
      }, task.value.waybills && task.value.waybills.length ? {
        w: common_vendor.t(task.value.waybills.length),
        x: common_vendor.f(task.value.waybills, (wb, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(wb.waybillNumber),
            b: common_vendor.t(wb.status),
            c: wb.senderName
          }, wb.senderName ? {
            d: common_vendor.t(wb.senderName),
            e: common_vendor.t(wb.senderPhone)
          } : {}, {
            f: wb.receiverName
          }, wb.receiverName ? {
            g: common_vendor.t(wb.receiverName),
            h: common_vendor.t(wb.receiverPhone)
          } : {}, {
            i: wb.goodsName
          }, wb.goodsName ? {
            j: common_vendor.t(wb.goodsName),
            k: common_vendor.t(wb.weight ? wb.weight + "kg" : "")
          } : {}, {
            l: wb.id
          });
        })
      } : {}) : {}, {
        y: task.value
      }, task.value ? common_vendor.e({
        z: task.value.status === 0
      }, task.value.status === 0 ? {
        A: submitting.value,
        B: common_vendor.o(handleDepart, "c4")
      } : {}, {
        C: task.value.status === 1
      }, task.value.status === 1 ? {
        D: submitting.value,
        E: common_vendor.o(handleArrive, "5b")
      } : {}, {
        F: task.value.status === 2
      }, task.value.status === 2 ? {
        G: common_vendor.o(toReturnRegister, "a4")
      } : {}) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d3be48ed"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subPages/transport-detail/index.js.map

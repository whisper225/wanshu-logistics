"use strict";
const utils_request = require("../../utils/request.js");
const getPickupTasks = (status) => utils_request.request({
  url: "/courier/pickup",
  method: "GET",
  params: { status }
});
const completePickup = (taskId, remark) => utils_request.request({
  url: `/courier/pickup/${taskId}/complete`,
  method: "POST",
  params: remark ? { remark } : {}
});
const getDeliveryTasks = (status) => utils_request.request({
  url: "/courier/delivery",
  method: "GET",
  params: { status }
});
const signDelivery = (taskId, signType = 0, signImage) => utils_request.request({
  url: `/courier/delivery/${taskId}/sign`,
  method: "POST",
  params: { signType, ...signImage ? { signImage } : {} }
});
const rejectDelivery = (taskId, reason) => utils_request.request({
  url: `/courier/delivery/${taskId}/reject`,
  method: "POST",
  params: reason ? { reason } : {}
});
exports.completePickup = completePickup;
exports.getDeliveryTasks = getDeliveryTasks;
exports.getPickupTasks = getPickupTasks;
exports.rejectDelivery = rejectDelivery;
exports.signDelivery = signDelivery;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/api/courier.js.map

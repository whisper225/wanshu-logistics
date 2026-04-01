"use strict";
const utils_request = require("../../utils/request.js");
const getTransportTasks = (status) => utils_request.request({
  url: "/driver/transport",
  method: "GET",
  params: { status }
});
const getTransportDetail = (taskId) => utils_request.request({
  url: `/driver/transport/${taskId}`,
  method: "GET",
  params: {}
});
const confirmDeparture = (taskId) => utils_request.request({
  url: `/driver/transport/${taskId}/depart`,
  method: "POST",
  params: {}
});
const confirmArrival = (taskId) => utils_request.request({
  url: `/driver/transport/${taskId}/arrive`,
  method: "POST",
  params: {}
});
const createReturnRegister = (taskId, description, images) => utils_request.request({
  url: `/driver/transport/${taskId}/return`,
  method: "POST",
  params: {
    ...description ? { description } : {},
    ...images ? { images } : {}
  }
});
exports.confirmArrival = confirmArrival;
exports.confirmDeparture = confirmDeparture;
exports.createReturnRegister = createReturnRegister;
exports.getTransportDetail = getTransportDetail;
exports.getTransportTasks = getTransportTasks;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/api/driver.js.map

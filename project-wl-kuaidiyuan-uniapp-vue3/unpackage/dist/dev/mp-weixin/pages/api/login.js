"use strict";
const utils_request = require("../../utils/request.js");
const wxLogin = (params) => utils_request.request({
  url: "/auth/wx/login",
  method: "POST",
  params: {
    code: params.code,
    role: params.role || "courier"
  }
});
const logout = () => utils_request.request({
  url: "/auth/logout",
  method: "POST",
  params: {}
});
exports.logout = logout;
exports.wxLogin = wxLogin;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/api/login.js.map

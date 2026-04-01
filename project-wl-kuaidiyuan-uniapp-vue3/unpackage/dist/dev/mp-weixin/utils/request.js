"use strict";
const common_vendor = require("../common/vendor.js");
const utils_env = require("./env.js");
function trimBase() {
  return String(utils_env.baseUrl).replace(/\/$/, "");
}
function omitEmpty(data) {
  if (!data || typeof data !== "object")
    return void 0;
  const out = {};
  for (const key of Object.keys(data)) {
    const v = data[key];
    if (v !== void 0 && v !== null && v !== "") {
      out[key] = v;
    }
  }
  return Object.keys(out).length ? out : void 0;
}
function request({ url = "", params = {}, method = "GET", header = {} }) {
  const token = common_vendor.index.getStorageSync("token");
  const isGet = !method || method.toUpperCase() === "GET";
  const data = isGet ? omitEmpty(params) : params;
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      url: trimBase() + url,
      method,
      data,
      header: {
        "Content-Type": "application/json",
        ...token ? { Authorization: token } : {},
        ...header
      },
      timeout: 2e4,
      success: (res) => {
        if (res.statusCode === 401) {
          try {
            common_vendor.index.removeStorageSync("token");
          } catch (e) {
          }
          reject(new Error("登录已失效，请重新登录"));
          return;
        }
        const body = res.data;
        if (body && typeof body === "object" && "code" in body) {
          if (body.code === 200) {
            resolve(body.data);
            return;
          }
          reject(new Error(body.message || `请求失败(${body.code})`));
          return;
        }
        resolve(body);
      },
      fail: (err) => reject(err)
    });
  });
}
exports.request = request;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/request.js.map

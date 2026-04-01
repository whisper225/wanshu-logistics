"use strict";
const user = {
  namespaced: true,
  state() {
    return {
      isLoginSuccess: true
    };
  },
  mutations: {
    setIsLoginSuccess(state, provider) {
      state.isLoginSuccess = provider;
    }
  },
  actions: {},
  getters: {}
};
exports.user = user;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/store/modules/user.js.map

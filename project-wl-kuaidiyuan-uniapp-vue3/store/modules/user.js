export default {
	namespaced: true,
	state() {
		return {
			isLoginSuccess: true
		}
	},
	mutations: {
		setIsLoginSuccess(state, provider) {
			state.isLoginSuccess = provider;
		}
	},
	actions: {},
	getters: {}
}

import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import NavBar from '@/components/Navbar/index.vue'

const app = createApp(App)
app.use(store)
app.mount('#app')

app.component('nav-bar', NavBar)

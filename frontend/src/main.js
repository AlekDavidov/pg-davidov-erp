import { createApp } from 'vue'
import { createPinia } from 'pinia'

import PrimeVue from 'primevue/config'
import ConfirmationService from 'primevue/confirmationservice'
import ToastService from 'primevue/toastservice'

import { definePreset } from '@primevue/themes'
import Aura from '@primevue/themes/aura'

import 'primeicons/primeicons.css'

import App from './App.vue'
import router from './router'
import './style.css'

const PgDavidovPreset = definePreset(Aura, {
    primitive: {
        davidovBlue: {
            50: '#eef6ff',
            100: '#d9eaff',
            200: '#bddcff',
            300: '#90c6ff',
            400: '#5ca7f5',
            500: '#2d73c8',
            600: '#245fa8',
            700: '#204f87',
            800: '#1e446f',
            900: '#1d3a5d',
            950: '#13263e'
        }
    },

    semantic: {
        primary: {
            50: '{davidovBlue.50}',
            100: '{davidovBlue.100}',
            200: '{davidovBlue.200}',
            300: '{davidovBlue.300}',
            400: '{davidovBlue.400}',
            500: '{davidovBlue.500}',
            600: '{davidovBlue.600}',
            700: '{davidovBlue.700}',
            800: '{davidovBlue.800}',
            900: '{davidovBlue.900}',
            950: '{davidovBlue.950}'
        },

        focusRing: {
            width: '2px',
            style: 'solid',
            color: '{primary.400}',
            offset: '2px'
        }
    }
})

createApp(App)
    .use(createPinia())
    .use(router)
    .use(PrimeVue, {
        theme: {
            preset: PgDavidovPreset,
            options: {
                darkModeSelector: '.app-dark',
                cssLayer: false
            }
        }
    })
    .use(ConfirmationService)
    .use(ToastService)
    .mount('#app')
import {
    createRouter,
    createWebHistory
} from 'vue-router'

import DashboardView from '../views/DashboardView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'
import SuppliersView from '../views/SuppliersView.vue'

const placeholderRoutes = [
    {
        path: '/transactions',
        title: 'Transactions'
    },
    {
        path: '/invoices',
        title: 'Invoices'
    },
    {
        path: '/documents',
        title: 'Documents'
    },
    {
        path: '/categories',
        title: 'Categories'
    },
    {
        path: '/bank-accounts',
        title: 'Bank Accounts'
    },
    {
        path: '/bank-import',
        title: 'Bank Import'
    },
    {
        path: '/reports',
        title: 'Reports'
    },
    {
        path: '/settings',
        title: 'Settings'
    }
]

const router = createRouter({
    history: createWebHistory(),

    routes: [
        {
            path: '/',
            name: 'dashboard',
            component: DashboardView
        },
        {
            path: '/suppliers',
            name: 'suppliers',
            component: SuppliersView
        },
        ...placeholderRoutes.map(route => ({
            path: route.path,
            component: PlaceholderView,
            props: {
                title: route.title
            }
        }))
    ]
})

export default router
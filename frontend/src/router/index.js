import { createRouter, createWebHistory } from 'vue-router'

import DashboardView from '../views/DashboardView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'
import TransactionsView from '../views/TransactionsView.vue'

const placeholderRoutes = [
    { path: 'invoices', title: 'Invoices' },
    { path: 'documents', title: 'Documents' },
    { path: 'suppliers', title: 'Suppliers' },
    { path: 'categories', title: 'Categories' },
    { path: 'bank-accounts', title: 'Bank Accounts' },
    { path: 'bank-import', title: 'Bank Import' },
    { path: 'reports', title: 'Reports' },
    { path: 'settings', title: 'Settings' }
]

export default createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: DashboardView
        },
        {
            path: '/transactions',
            component: TransactionsView
        },
        ...placeholderRoutes.map(route => ({
            path: `/${route.path}`,
            component: PlaceholderView,
            props: {
                title: route.title
            }
        }))
    ]
})
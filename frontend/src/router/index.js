import { createRouter, createWebHistory } from 'vue-router'

import CategoriesView from '../views/CategoriesView.vue'
import DashboardView from '../views/DashboardView.vue'
import PaymentMethodsView from '../views/PaymentMethodsView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'
import SuppliersView from '../views/SuppliersView.vue'
import TransactionsView from '../views/TransactionsView.vue'

const placeholderRoutes = [
    { path: 'invoices', title: 'Invoices' },
    { path: 'documents', title: 'Documents' },
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
            name: 'dashboard',
            component: DashboardView
        },
        {
            path: '/suppliers',
            name: 'suppliers',
            component: SuppliersView
        },
        {
            path: '/transactions',
            name: 'transactions',
            component: TransactionsView
        },
        {
            path: '/categories',
            name: 'categories',
            component: CategoriesView
        },
        {
            path: '/payment-methods',
            name: 'payment-methods',
            component: PaymentMethodsView
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
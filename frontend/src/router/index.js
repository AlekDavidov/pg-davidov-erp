import { createRouter, createWebHistory } from 'vue-router'

import BankAccountsView from '../views/BankAccountsView.vue'
import BankImportView from '../views/BankImportView.vue'
import CategoriesView from '../views/CategoriesView.vue'
import DashboardView from '../views/DashboardView.vue'
import InvoicesView from '../views/InvoicesView.vue'
import PaymentMethodsView from '../views/PaymentMethodsView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'
import SuppliersView from '../views/SuppliersView.vue'
import TransactionsView from '../views/TransactionsView.vue'

const placeholderRoutes = [
    { path: 'documents', title: 'Documents' },
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
            path: '/invoices',
            name: 'invoices',
            component: InvoicesView
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
        {
            path: '/bank-accounts',
            name: 'bank-accounts',
            component: BankAccountsView
        },
        {
            path: '/bank-import',
            name: 'bank-import',
            component: BankImportView
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
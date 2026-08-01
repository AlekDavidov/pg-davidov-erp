import { defineStore } from 'pinia'

import { dashboardApi } from '../api/dashboardApi'

const currentDate = new Date()

const createEmptyDashboard = () => ({
    income: {
        currentAmount: 0,
        previousAmount: 0,
        difference: 0,
        percentageChange: 0
    },

    expense: {
        currentAmount: 0,
        previousAmount: 0,
        difference: 0,
        percentageChange: 0
    },

    cashFlow: [],
    expenseCategories: [],
    recentTransactions: [],
    dueInvoices: [],
    bankAccounts: []
})

export const useDashboardStore = defineStore(
    'dashboard',
    {
        state: () => ({
            dashboard: createEmptyDashboard(),

            selectedMonth:
                currentDate.getMonth() + 1,

            selectedYear:
                currentDate.getFullYear(),

            loading: false,
            error: null
        }),

        getters: {
            income: state =>
                state.dashboard.income,

            expense: state =>
                state.dashboard.expense,

            cashFlow: state =>
                state.dashboard.cashFlow,

            expenseCategories: state =>
                state.dashboard.expenseCategories,

            recentTransactions: state =>
                state.dashboard.recentTransactions,

            dueInvoices: state =>
                state.dashboard.dueInvoices,

            bankAccounts: state =>
                state.dashboard.bankAccounts
        },

        actions: {
            async fetchDashboard({
                                     month = this.selectedMonth,
                                     year = this.selectedYear
                                 } = {}) {
                this.loading = true
                this.error = null

                this.selectedMonth =
                    Number(month)

                this.selectedYear =
                    Number(year)

                try {
                    const response =
                        await dashboardApi.getDashboard({
                            month:
                            this.selectedMonth,

                            year:
                            this.selectedYear
                        })

                    this.dashboard = {
                        ...createEmptyDashboard(),
                        ...response
                    }
                } catch (error) {
                    this.dashboard =
                        createEmptyDashboard()

                    this.error =
                        error.message ||
                        'Dashboard nije mogao da bude učitan.'
                } finally {
                    this.loading = false
                }
            },

            setSelectedMonth(month) {
                this.selectedMonth =
                    Number(month)
            },

            setSelectedYear(year) {
                this.selectedYear =
                    Number(year)
            },

            clearError() {
                this.error = null
            }
        }
    }
)
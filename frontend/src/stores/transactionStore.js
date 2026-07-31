import { defineStore } from 'pinia'
import { transactionApi } from '../api/transactionApi'

export const useTransactionStore = defineStore('transactions', {
    state: () => ({
        transactions: [],
        loading: false,
        error: null
    }),

    getters: {
        isEmpty: state =>
            !state.loading &&
            state.transactions.length === 0
    },

    actions: {
        async fetchTransactions() {
            this.loading = true
            this.error = null

            try {
                this.transactions =
                    await transactionApi.findAll()
            } catch (error) {
                this.transactions = []
                this.error =
                    error.message ||
                    'Transakcije nisu mogle biti učitane.'
            } finally {
                this.loading = false
            }
        },

        clearError() {
            this.error = null
        }
    }
})
import { defineStore } from 'pinia'

import { transactionApi } from '../api/transactionApi'

export const useTransactionStore = defineStore('transactions', {
    state: () => ({
        transactions: [],
        loading: false,
        saving: false,
        deleting: false,
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

        async createTransaction(request) {
            this.saving = true
            this.error = null

            try {
                const transaction =
                    await transactionApi.create(request)

                await this.fetchTransactions()

                return transaction
            } catch (error) {
                this.error =
                    error.message ||
                    'Transakcija nije mogla biti kreirana.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async updateTransaction(id, request) {
            this.saving = true
            this.error = null

            try {
                const transaction =
                    await transactionApi.update(
                        id,
                        request
                    )

                await this.fetchTransactions()

                return transaction
            } catch (error) {
                this.error =
                    error.message ||
                    'Transakcija nije mogla biti izmenjena.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async deleteTransaction(id) {
            this.deleting = true
            this.error = null

            try {
                await transactionApi.remove(id)
                await this.fetchTransactions()
            } catch (error) {
                this.error =
                    error.message ||
                    'Transakcija nije mogla biti obrisana.'

                throw error
            } finally {
                this.deleting = false
            }
        },

        clearError() {
            this.error = null
        }
    }
})
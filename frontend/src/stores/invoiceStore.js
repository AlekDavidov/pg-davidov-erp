import { defineStore } from 'pinia'

import { invoiceApi } from '../api/invoiceApi'

export const useInvoiceStore = defineStore('invoices', {
    state: () => ({
        invoices: [],
        loading: false,
        saving: false,
        deleting: false,
        error: null,

        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,

        sortBy: 'invoiceDate',
        sortDirection: 'desc'
    }),

    getters: {
        isEmpty: state =>
            !state.loading &&
            state.invoices.length === 0
    },

    actions: {
        async fetchInvoices({
                                page = this.page,
                                size = this.size,
                                sortBy = this.sortBy,
                                sortDirection = this.sortDirection
                            } = {}) {
            this.loading = true
            this.error = null

            try {
                const response = await invoiceApi.findAll({
                    page,
                    size,
                    sortBy,
                    sortDirection
                })

                this.invoices = response.items
                this.page = response.page
                this.size = response.size
                this.totalElements = response.totalElements
                this.totalPages = response.totalPages
                this.sortBy = sortBy
                this.sortDirection = sortDirection
            } catch (error) {
                this.invoices = []
                this.totalElements = 0
                this.totalPages = 0
                this.error =
                    error.message ||
                    'Invoices could not be loaded.'
            } finally {
                this.loading = false
            }
        },

        async createInvoice(request) {
            this.saving = true
            this.error = null

            try {
                const invoice = await invoiceApi.create(request)

                await this.fetchInvoices({
                    page: 0,
                    size: this.size,
                    sortBy: this.sortBy,
                    sortDirection: this.sortDirection
                })

                return invoice
            } catch (error) {
                this.error =
                    error.message ||
                    'Invoice could not be created.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async updateInvoice(id, request) {
            this.saving = true
            this.error = null

            try {
                const invoice = await invoiceApi.update(
                    id,
                    request
                )

                await this.fetchInvoices({
                    page: this.page,
                    size: this.size,
                    sortBy: this.sortBy,
                    sortDirection: this.sortDirection
                })

                return invoice
            } catch (error) {
                this.error =
                    error.message ||
                    'Invoice could not be updated.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async deleteInvoice(id) {
            this.deleting = true
            this.error = null

            try {
                await invoiceApi.remove(id)

                const targetPage =
                    this.invoices.length === 1 &&
                    this.page > 0
                        ? this.page - 1
                        : this.page

                await this.fetchInvoices({
                    page: targetPage,
                    size: this.size,
                    sortBy: this.sortBy,
                    sortDirection: this.sortDirection
                })
            } catch (error) {
                this.error =
                    error.message ||
                    'Invoice could not be deleted.'

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
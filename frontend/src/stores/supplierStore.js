import { defineStore } from 'pinia'
import { supplierApi } from '../api/supplierApi'

export const useSupplierStore = defineStore('suppliers', {
    state: () => ({
        suppliers: [],
        loading: false,
        saving: false,
        deleting: false,
        error: null,

        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,

        sortBy: 'name',
        direction: 'asc'
    }),

    getters: {
        isEmpty: state =>
            !state.loading &&
            state.suppliers.length === 0
    },

    actions: {
        async fetchSuppliers({
                                 page = this.page,
                                 size = this.size,
                                 sortBy = this.sortBy,
                                 direction = this.direction
                             } = {}) {
            this.loading = true
            this.error = null

            try {
                const response = await supplierApi.findAll({
                    page,
                    size,
                    sortBy,
                    direction
                })

                this.suppliers = response.items
                this.page = response.page
                this.size = response.size
                this.totalElements = response.totalElements
                this.totalPages = response.totalPages
                this.sortBy = sortBy
                this.direction = direction
            } catch (error) {
                this.suppliers = []
                this.totalElements = 0
                this.totalPages = 0
                this.error =
                    error.message ||
                    'Suppliers could not be loaded.'
            } finally {
                this.loading = false
            }
        },

        async createSupplier(request) {
            this.saving = true
            this.error = null

            try {
                const supplier = await supplierApi.create(request)

                await this.fetchSuppliers({
                    page: 0,
                    size: this.size,
                    sortBy: this.sortBy,
                    direction: this.direction
                })

                return supplier
            } catch (error) {
                this.error =
                    error.message ||
                    'Supplier could not be created.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async updateSupplier(id, request) {
            this.saving = true
            this.error = null

            try {
                const supplier = await supplierApi.update(
                    id,
                    request
                )

                await this.fetchSuppliers({
                    page: this.page,
                    size: this.size,
                    sortBy: this.sortBy,
                    direction: this.direction
                })

                return supplier
            } catch (error) {
                this.error =
                    error.message ||
                    'Supplier could not be updated.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async deleteSupplier(id) {
            this.deleting = true
            this.error = null

            try {
                await supplierApi.remove(id)

                const targetPage =
                    this.suppliers.length === 1 && this.page > 0
                        ? this.page - 1
                        : this.page

                await this.fetchSuppliers({
                    page: targetPage,
                    size: this.size,
                    sortBy: this.sortBy,
                    direction: this.direction
                })
            } catch (error) {
                this.error =
                    error.message ||
                    'Supplier could not be deleted.'

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
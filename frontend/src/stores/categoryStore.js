import { defineStore } from 'pinia'

import { categoryApi } from '../api/categoryApi'

export const useCategoryStore = defineStore('categories', {
    state: () => ({
        categories: [],
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
            state.categories.length === 0
    },

    actions: {
        async fetchCategories({
                                  page = this.page,
                                  size = this.size,
                                  sortBy = this.sortBy,
                                  direction = this.direction
                              } = {}) {
            this.loading = true
            this.error = null

            try {
                const response = await categoryApi.findAll({
                    page,
                    size,
                    sortBy,
                    direction
                })

                this.categories = response.items
                this.page = response.page
                this.size = response.size
                this.totalElements = response.totalElements
                this.totalPages = response.totalPages
                this.sortBy = sortBy
                this.direction = direction
            } catch (error) {
                this.categories = []
                this.totalElements = 0
                this.totalPages = 0
                this.error =
                    error.message ||
                    'Categories could not be loaded.'
            } finally {
                this.loading = false
            }
        },

        async createCategory(request) {
            this.saving = true
            this.error = null

            try {
                const category = await categoryApi.create(request)

                await this.fetchCategories({
                    page: 0,
                    size: this.size,
                    sortBy: this.sortBy,
                    direction: this.direction
                })

                return category
            } catch (error) {
                this.error =
                    error.message ||
                    'Category could not be created.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async updateCategory(id, request) {
            this.saving = true
            this.error = null

            try {
                const category = await categoryApi.update(
                    id,
                    request
                )

                await this.fetchCategories({
                    page: this.page,
                    size: this.size,
                    sortBy: this.sortBy,
                    direction: this.direction
                })

                return category
            } catch (error) {
                this.error =
                    error.message ||
                    'Category could not be updated.'

                throw error
            } finally {
                this.saving = false
            }
        },

        async deleteCategory(id) {
            this.deleting = true
            this.error = null

            try {
                await categoryApi.remove(id)

                const targetPage =
                    this.categories.length === 1 &&
                    this.page > 0
                        ? this.page - 1
                        : this.page

                await this.fetchCategories({
                    page: targetPage,
                    size: this.size,
                    sortBy: this.sortBy,
                    direction: this.direction
                })
            } catch (error) {
                this.error =
                    error.message ||
                    'Category could not be deleted.'

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
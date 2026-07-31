import { defineStore } from 'pinia'

import { paymentMethodApi } from '../api/paymentMethodApi'

export const usePaymentMethodStore = defineStore(
    'paymentMethods',
    {
        state: () => ({
            paymentMethods: [],
            loading: false,
            saving: false,
            deleting: false,
            error: null
        }),

        getters: {
            isEmpty: state =>
                !state.loading &&
                state.paymentMethods.length === 0
        },

        actions: {
            async fetchPaymentMethods() {
                this.loading = true
                this.error = null

                try {
                    this.paymentMethods =
                        await paymentMethodApi.findAll()
                } catch (error) {
                    this.paymentMethods = []
                    this.error =
                        error.message ||
                        'Payment methods could not be loaded.'
                } finally {
                    this.loading = false
                }
            },

            async createPaymentMethod(request) {
                this.saving = true
                this.error = null

                try {
                    const paymentMethod =
                        await paymentMethodApi.create(request)

                    await this.fetchPaymentMethods()

                    return paymentMethod
                } catch (error) {
                    this.error =
                        error.message ||
                        'Payment method could not be created.'

                    throw error
                } finally {
                    this.saving = false
                }
            },

            async updatePaymentMethod(id, request) {
                this.saving = true
                this.error = null

                try {
                    const paymentMethod =
                        await paymentMethodApi.update(
                            id,
                            request
                        )

                    await this.fetchPaymentMethods()

                    return paymentMethod
                } catch (error) {
                    this.error =
                        error.message ||
                        'Payment method could not be updated.'

                    throw error
                } finally {
                    this.saving = false
                }
            },

            async deletePaymentMethod(id) {
                this.deleting = true
                this.error = null

                try {
                    await paymentMethodApi.remove(id)
                    await this.fetchPaymentMethods()
                } catch (error) {
                    this.error =
                        error.message ||
                        'Payment method could not be deleted.'

                    throw error
                } finally {
                    this.deleting = false
                }
            },

            clearError() {
                this.error = null
            }
        }
    }
)
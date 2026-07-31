import { defineStore } from 'pinia'

import { bankAccountApi } from '../api/bankAccountApi'

export const useBankAccountStore = defineStore(
    'bankAccounts',
    {
        state: () => ({
            bankAccounts: [],
            loading: false,
            saving: false,
            deleting: false,
            error: null
        }),

        getters: {
            isEmpty: state =>
                !state.loading &&
                state.bankAccounts.length === 0
        },

        actions: {
            async fetchBankAccounts() {
                this.loading = true
                this.error = null

                try {
                    this.bankAccounts =
                        await bankAccountApi.findAll()
                } catch (error) {
                    this.bankAccounts = []
                    this.error =
                        error.message ||
                        'Bank accounts could not be loaded.'
                } finally {
                    this.loading = false
                }
            },

            async createBankAccount(request) {
                this.saving = true
                this.error = null

                try {
                    const bankAccount =
                        await bankAccountApi.create(request)

                    await this.fetchBankAccounts()

                    return bankAccount
                } catch (error) {
                    this.error =
                        error.message ||
                        'Bank account could not be created.'

                    throw error
                } finally {
                    this.saving = false
                }
            },

            async updateBankAccount(id, request) {
                this.saving = true
                this.error = null

                try {
                    const bankAccount =
                        await bankAccountApi.update(
                            id,
                            request
                        )

                    await this.fetchBankAccounts()

                    return bankAccount
                } catch (error) {
                    this.error =
                        error.message ||
                        'Bank account could not be updated.'

                    throw error
                } finally {
                    this.saving = false
                }
            },

            async deleteBankAccount(id) {
                this.deleting = true
                this.error = null

                try {
                    await bankAccountApi.remove(id)
                    await this.fetchBankAccounts()
                } catch (error) {
                    this.error =
                        error.message ||
                        'Bank account could not be deleted.'

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
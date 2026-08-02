import { defineStore } from 'pinia'

import { bankImportApi } from '../api/bankImportApi'

const createEmptyState = () => ({
    selectedFile: null,
    preview: null,
    loading: false,
    error: null
})

export const useBankImportStore = defineStore(
    'bankImport',
    {
        state: () => createEmptyState(),

        getters: {
            hasFile: state =>
                state.selectedFile !== null,

            hasPreview: state =>
                state.preview !== null,

            transactions: state =>
                state.preview?.transactions || [],

            transactionCount: state =>
                state.preview?.transactionCount || 0,

            bankName: state =>
                state.preview?.bankName || '',

            statementId: state =>
                state.preview?.statementId || '',

            periodFrom: state =>
                state.preview?.periodFrom || null,

            periodTo: state =>
                state.preview?.periodTo || null,

            accountNumber: state =>
                state.preview?.accountNumber || ''
        },

        actions: {
            setFile(file) {
                this.selectedFile =
                    file || null

                this.preview = null
                this.error = null
            },

            async loadPreview() {
                if (!this.selectedFile) {
                    this.error =
                        'Izaberite PDF izvod.'

                    return
                }

                this.loading = true
                this.error = null
                this.preview = null

                try {
                    this.preview =
                        await bankImportApi.preview(
                            this.selectedFile
                        )
                } catch (error) {
                    this.error =
                        error.response?.data?.message ||
                        error.response?.data?.detail ||
                        error.message ||
                        'Izvod nije mogao da bude obrađen.'
                } finally {
                    this.loading = false
                }
            },

            clearPreview() {
                this.preview = null
                this.error = null
            },

            reset() {
                Object.assign(
                    this,
                    createEmptyState()
                )
            }
        }
    }
)
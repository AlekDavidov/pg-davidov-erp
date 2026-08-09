import { defineStore } from 'pinia'

import { bankImportApi } from '../api/bankImportApi'
import { categoryApi } from '../api/categoryApi'

const createEmptyState = () => ({
    selectedFile: null,
    preview: null,
    loading: false,
    error: null,

    supplierOptions: [],
    suppliersLoading: false,
    suppliersError: null,

    categoryOptions: [],
    categoriesLoading: false,
    categoriesError: null
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
                state.preview?.accountNumber || '',

            hasSupplierOptions: state =>
                state.supplierOptions.length > 0,

            hasCategoryOptions: state =>
                state.categoryOptions.length > 0,

            matchedTransactionCount: state =>
                (
                    state.preview?.transactions || []
                ).filter(
                    transaction =>
                        transaction.matchStatus ===
                        'MATCHED'
                ).length,

            unmatchedTransactionCount: state =>
                (
                    state.preview?.transactions || []
                ).filter(
                    transaction =>
                        transaction.matchStatus ===
                        'UNMATCHED'
                ).length,

            ambiguousTransactionCount: state =>
                (
                    state.preview?.transactions || []
                ).filter(
                    transaction =>
                        transaction.matchStatus ===
                        'AMBIGUOUS'
                ).length
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
                    const [
                        preview,
                        supplierOptions,
                        categoryOptions
                    ] = await Promise.all([
                        bankImportApi.preview(
                            this.selectedFile
                        ),
                        this.fetchSupplierOptions(),
                        this.fetchCategoryOptions()
                    ])

                    this.preview = preview

                    if (supplierOptions) {
                        this.supplierOptions =
                            supplierOptions
                    }

                    if (categoryOptions) {
                        this.categoryOptions =
                            categoryOptions
                    }
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

            async loadSupplierOptions() {
                this.suppliersLoading = true
                this.suppliersError = null

                try {
                    this.supplierOptions =
                        await bankImportApi
                            .getSupplierOptions()
                } catch (error) {
                    this.supplierOptions = []

                    this.suppliersError =
                        error.response?.data?.message ||
                        error.response?.data?.detail ||
                        error.message ||
                        'Dobavljači nisu mogli da budu učitani.'
                } finally {
                    this.suppliersLoading = false
                }
            },

            async fetchSupplierOptions() {
                this.suppliersLoading = true
                this.suppliersError = null

                try {
                    return await bankImportApi
                        .getSupplierOptions()
                } catch (error) {
                    this.suppliersError =
                        error.response?.data?.message ||
                        error.response?.data?.detail ||
                        error.message ||
                        'Dobavljači nisu mogli da budu učitani.'

                    return []
                } finally {
                    this.suppliersLoading = false
                }
            },

            async loadCategoryOptions() {
                this.categoriesLoading = true
                this.categoriesError = null

                try {
                    this.categoryOptions =
                        await categoryApi.findOptions()
                } catch (error) {
                    this.categoryOptions = []

                    this.categoriesError =
                        error.response?.data?.message ||
                        error.response?.data?.detail ||
                        error.message ||
                        'Kategorije nisu mogle da budu učitane.'
                } finally {
                    this.categoriesLoading = false
                }
            },

            async fetchCategoryOptions() {
                this.categoriesLoading = true
                this.categoriesError = null

                try {
                    return await categoryApi.findOptions()
                } catch (error) {
                    this.categoriesError =
                        error.response?.data?.message ||
                        error.response?.data?.detail ||
                        error.message ||
                        'Kategorije nisu mogle da budu učitane.'

                    return []
                } finally {
                    this.categoriesLoading = false
                }
            },

            applySupplierSelection(
                transaction,
                supplierId
            ) {
                if (!transaction) {
                    return
                }

                const supplier =
                    this.supplierOptions.find(
                        option =>
                            option.id === supplierId
                    )

                if (!supplier) {
                    this.clearSupplierSelection(
                        transaction
                    )

                    return
                }

                transaction.supplierId =
                    supplier.id

                transaction.supplierName =
                    supplier.name

                transaction.categoryId =
                    supplier.defaultCategoryId ||
                    null

                transaction.categoryName =
                    supplier.defaultCategoryName ||
                    null

                transaction.matchStatus =
                    'MATCHED'
            },

            clearSupplierSelection(
                transaction
            ) {
                if (!transaction) {
                    return
                }

                transaction.supplierId = null
                transaction.supplierName = null
                transaction.categoryId = null
                transaction.categoryName = null
                transaction.matchStatus =
                    'UNMATCHED'
            },

            applyCategorySelection(
                transaction,
                categoryId
            ) {
                if (!transaction) {
                    return
                }

                if (!categoryId) {
                    transaction.categoryId = null
                    transaction.categoryName = null

                    return
                }

                const category =
                    this.categoryOptions.find(
                        option =>
                            option.id === categoryId
                    )

                if (!category) {
                    transaction.categoryId = null
                    transaction.categoryName = null

                    return
                }

                transaction.categoryId =
                    category.id

                transaction.categoryName =
                    category.name
            },

            clearPreview() {
                this.preview = null
                this.error = null
            },

            reset() {
                const supplierOptions =
                    this.supplierOptions

                const categoryOptions =
                    this.categoryOptions

                Object.assign(
                    this,
                    createEmptyState()
                )

                this.supplierOptions =
                    supplierOptions

                this.categoryOptions =
                    categoryOptions
            }
        }
    }
)
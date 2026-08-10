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
    categoriesError: null,

    importing: false,
    importError: null,
    importResult: null
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
                ).length,

            duplicateTransactionCount: state =>
                (
                    state.preview?.transactions || []
                ).filter(
                    transaction =>
                        transaction.duplicate
                ).length,

            importableTransactions: state =>
                (
                    state.preview?.transactions || []
                ).filter(
                    transaction =>
                        !transaction.duplicate
                ),

            importableTransactionCount() {
                return this.importableTransactions.length
            },

            canImport() {
                return (
                    this.hasPreview &&
                    this.importableTransactionCount > 0 &&
                    !this.importing
                )
            }
        },

        actions: {
            setFile(file) {
                this.selectedFile =
                    file || null

                this.preview = null
                this.error = null
                this.importError = null
                this.importResult = null
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
                this.importError = null
                this.importResult = null

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

            async importTransactions(
                bankAccountId
            ) {
                if (!this.preview) {
                    this.importError =
                        'Izvod nije učitan.'

                    return null
                }

                if (!bankAccountId) {
                    this.importError =
                        'Bankovni račun nije izabran.'

                    return null
                }

                const transactions =
                    this.importableTransactions

                if (transactions.length === 0) {
                    this.importError =
                        'Nema novih transakcija za uvoz.'

                    return null
                }

                this.importing = true
                this.importError = null
                this.importResult = null

                try {
                    const request = {
                        bankCode:
                        this.preview.bankCode,

                        accountNumber:
                        this.preview.accountNumber,

                        statementId:
                        this.preview.statementId,

                        originalFilename:
                            this.selectedFile?.name ||
                            'bank-statement.pdf',

                        bankAccountId,

                        periodFrom:
                        this.preview.periodFrom,

                        periodTo:
                        this.preview.periodTo,

                        openingBalance:
                        this.preview.openingBalance,

                        totalIncome:
                        this.preview.totalIncome,

                        totalExpenses:
                        this.preview.totalExpenses,

                        closingBalance:
                        this.preview.closingBalance,

                        transactions:
                            transactions.map(
                                transaction => ({
                                    entryNumber:
                                    transaction.entryNumber,

                                    transactionDate:
                                    transaction.transactionDate,

                                    executionDate:
                                    transaction.executionDate,

                                    currencyCode:
                                    transaction.currencyCode,

                                    debit:
                                    transaction.debit,

                                    credit:
                                    transaction.credit,

                                    counterparty:
                                    transaction.counterparty,

                                    description:
                                    transaction.description,

                                    reference:
                                    transaction.reference,

                                    orderType:
                                    transaction.orderType,

                                    orderReference:
                                    transaction.orderReference,

                                    sourcePage:
                                    transaction.sourcePage,

                                    supplierId:
                                    transaction.supplierId,

                                    categoryId:
                                    transaction.categoryId
                                })
                            )
                    }

                    const result =
                        await bankImportApi.importTransactions(
                            request
                        )

                    this.importResult = result

                    await this.refreshPreview()

                    return result
                } catch (error) {
                    this.importError =
                        error.response?.data?.message ||
                        error.response?.data?.detail ||
                        error.message ||
                        'Transakcije nisu mogle da budu uvezene.'

                    return null
                } finally {
                    this.importing = false
                }
            },

            async refreshPreview() {
                if (!this.selectedFile) {
                    return
                }

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
                        'Pregled izvoda nije mogao da bude osvežen.'
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
                this.importError = null
                this.importResult = null
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
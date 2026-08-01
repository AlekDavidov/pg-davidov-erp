import httpClient from './httpClient'

const INVOICES_PATH = '/invoices'

export const invoiceApi = {
    async findAll({
                      page = 0,
                      size = 20,
                      sortBy = 'invoiceDate',
                      sortDirection = 'desc'
                  } = {}) {
        const response = await httpClient.get(
            INVOICES_PATH,
            {
                params: {
                    page,
                    size,
                    sortBy,
                    sortDirection
                }
            }
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${INVOICES_PATH}/${id}`
        )

        return response.data
    },

    async create(request) {
        const response = await httpClient.post(
            INVOICES_PATH,
            request
        )

        return response.data
    },

    async update(id, request) {
        const response = await httpClient.put(
            `${INVOICES_PATH}/${id}`,
            request
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${INVOICES_PATH}/${id}`
        )
    },

    async getDocuments(invoiceId) {
        const response = await httpClient.get(
            `${INVOICES_PATH}/${invoiceId}/documents`
        )

        return response.data
    },

    async uploadDocument(
        invoiceId,
        documentCode,
        file
    ) {
        const formData = new FormData()

        formData.append('file', file)

        const response = await httpClient.post(
            `${INVOICES_PATH}/${invoiceId}/documents`,
            formData,
            {
                params: {
                    documentCode
                },
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }
        )

        return response.data
    },

    async removeDocument(
        invoiceId,
        documentId
    ) {
        await httpClient.delete(
            `${INVOICES_PATH}/${invoiceId}/documents/${documentId}`
        )
    },

    async getPayments(invoiceId) {
        const response = await httpClient.get(
            `${INVOICES_PATH}/${invoiceId}/payments`
        )

        return response.data
    },

    async attachPayment(
        invoiceId,
        request
    ) {
        const response = await httpClient.post(
            `${INVOICES_PATH}/${invoiceId}/payments`,
            request
        )

        return response.data
    },

    async detachPayment(
        invoiceId,
        paymentId
    ) {
        await httpClient.delete(
            `${INVOICES_PATH}/${invoiceId}/payments/${paymentId}`
        )
    }
}
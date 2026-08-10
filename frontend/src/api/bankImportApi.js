import httpClient from './httpClient'

const BANK_IMPORT_PATH = '/bank-import'

export const bankImportApi = {
    async preview(file) {
        const formData = new FormData()

        formData.append(
            'file',
            file
        )

        const response = await httpClient.post(
            `${BANK_IMPORT_PATH}/preview`,
            formData,
            {
                headers: {
                    'Content-Type':
                        'multipart/form-data'
                }
            }
        )

        return response.data
    },

    async importTransactions(request) {
        const response = await httpClient.post(
            `${BANK_IMPORT_PATH}/import`,
            request
        )

        return response.data
    },

    async getSupplierOptions() {
        const response = await httpClient.get(
            `${BANK_IMPORT_PATH}/suppliers`
        )

        return response.data
    }
}
import httpClient from './httpClient'

const DOCUMENTS_PATH = '/documents'

export const documentApi = {
    async findAll() {
        const response = await httpClient.get(
            DOCUMENTS_PATH
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${DOCUMENTS_PATH}/${id}`
        )

        return response.data
    },

    async upload(
        displayName,
        file
    ) {
        const formData = new FormData()

        formData.append('file', file)

        const response = await httpClient.post(
            DOCUMENTS_PATH,
            formData,
            {
                params: {
                    displayName
                },
                headers: {
                    'Content-Type':
                        'multipart/form-data'
                }
            }
        )

        return response.data
    },

    async download(id) {
        const response = await httpClient.get(
            `${DOCUMENTS_PATH}/${id}/download`,
            {
                responseType: 'blob'
            }
        )

        return response
    },

    async remove(id) {
        await httpClient.delete(
            `${DOCUMENTS_PATH}/${id}`
        )
    }
}
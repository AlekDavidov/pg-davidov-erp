import httpClient from './httpClient'

const DOCUMENT_RECORDS_PATH =
    '/document-records'

export const documentRecordApi = {

    async findAll() {
        const response = await httpClient.get(
            DOCUMENT_RECORDS_PATH
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${DOCUMENT_RECORDS_PATH}/${id}`
        )

        return response.data
    },

    async create(data) {
        const response = await httpClient.post(
            DOCUMENT_RECORDS_PATH,
            data
        )

        return response.data
    },

    async update(
        id,
        data
    ) {
        const response = await httpClient.put(
            `${DOCUMENT_RECORDS_PATH}/${id}`,
            data
        )

        return response.data
    },

    async addDocument(
        recordId,
        displayName,
        file
    ) {
        const formData = new FormData()

        formData.append(
            'file',
            file
        )

        const response = await httpClient.post(
            `${DOCUMENT_RECORDS_PATH}/${recordId}/documents`,
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

    async removeDocument(
        recordId,
        documentId
    ) {
        const response = await httpClient.delete(
            `${DOCUMENT_RECORDS_PATH}/${recordId}/documents/${documentId}`
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${DOCUMENT_RECORDS_PATH}/${id}`
        )
    }
}
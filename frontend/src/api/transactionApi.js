import httpClient from './httpClient'

const TRANSACTIONS_PATH = '/transactions'

export const transactionApi = {
    async findAll() {
        const response = await httpClient.get(
            TRANSACTIONS_PATH
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${TRANSACTIONS_PATH}/${id}`
        )

        return response.data
    },

    async create(request) {
        const response = await httpClient.post(
            TRANSACTIONS_PATH,
            request
        )

        return response.data
    },

    async update(id, request) {
        const response = await httpClient.put(
            `${TRANSACTIONS_PATH}/${id}`,
            request
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${TRANSACTIONS_PATH}/${id}`
        )
    }
}
import httpClient from './httpClient'

const BANK_ACCOUNTS_PATH = '/bank-accounts'

export const bankAccountApi = {
    async findAll() {
        const response = await httpClient.get(
            BANK_ACCOUNTS_PATH
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${BANK_ACCOUNTS_PATH}/${id}`
        )

        return response.data
    },

    async create(request) {
        const response = await httpClient.post(
            BANK_ACCOUNTS_PATH,
            request
        )

        return response.data
    },

    async update(id, request) {
        const response = await httpClient.put(
            `${BANK_ACCOUNTS_PATH}/${id}`,
            request
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${BANK_ACCOUNTS_PATH}/${id}`
        )
    }
}
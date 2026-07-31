import httpClient from './httpClient'

const PAYMENT_METHODS_PATH = '/payment-methods'

export const paymentMethodApi = {
    async findAll() {
        const response = await httpClient.get(
            PAYMENT_METHODS_PATH
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${PAYMENT_METHODS_PATH}/${id}`
        )

        return response.data
    },

    async create(request) {
        const response = await httpClient.post(
            PAYMENT_METHODS_PATH,
            request
        )

        return response.data
    },

    async update(id, request) {
        const response = await httpClient.put(
            `${PAYMENT_METHODS_PATH}/${id}`,
            request
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${PAYMENT_METHODS_PATH}/${id}`
        )
    }
}
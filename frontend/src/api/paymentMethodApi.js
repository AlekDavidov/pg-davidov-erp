import httpClient from './httpClient'

const PAYMENT_METHODS_PATH = '/payment-methods'

export const paymentMethodApi = {
    async findAll() {
        const response = await httpClient.get(
            PAYMENT_METHODS_PATH
        )

        return response.data
    }
}
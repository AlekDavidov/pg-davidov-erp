import httpClient from './httpClient'

const COMPANY_PROFILE_PATH =
    '/company-profile'

export const companyProfileApi = {

    async getProfile() {
        const response = await httpClient.get(
            COMPANY_PROFILE_PATH
        )

        if (response.status === 204) {
            return null
        }

        return response.data
    },

    async save(request) {
        const response = await httpClient.put(
            COMPANY_PROFILE_PATH,
            request
        )

        return response.data
    },

    async getLogo() {
        const response = await httpClient.get(
            `${COMPANY_PROFILE_PATH}/logo`,
            {
                responseType: 'blob'
            }
        )

        return response.data
    },

    async uploadLogo(file) {
        const formData =
            new FormData()

        formData.append(
            'file',
            file
        )

        await httpClient.post(
            `${COMPANY_PROFILE_PATH}/logo`,
            formData,
            {
                headers: {
                    'Content-Type':
                        'multipart/form-data'
                }
            }
        )
    },

    async deleteLogo() {
        await httpClient.delete(
            `${COMPANY_PROFILE_PATH}/logo`
        )
    }
}
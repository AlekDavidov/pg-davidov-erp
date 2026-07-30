import httpClient from './httpClient'

const SUPPLIERS_PATH = '/suppliers'

export const supplierApi = {
    async findAll({
                      page = 0,
                      size = 20,
                      sortBy = 'name',
                      direction = 'asc'
                  } = {}) {
        const response = await httpClient.get(SUPPLIERS_PATH, {
            params: {
                page,
                size,
                sortBy,
                direction
            }
        })

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${SUPPLIERS_PATH}/${id}`
        )

        return response.data
    },

    async create(request) {
        const response = await httpClient.post(
            SUPPLIERS_PATH,
            request
        )

        return response.data
    },

    async update(id, request) {
        const response = await httpClient.put(
            `${SUPPLIERS_PATH}/${id}`,
            request
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${SUPPLIERS_PATH}/${id}`
        )
    }
}
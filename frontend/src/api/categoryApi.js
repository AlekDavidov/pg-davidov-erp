import httpClient from './httpClient'

const CATEGORIES_PATH = '/categories'

export const categoryApi = {
    async findAll({
                      page = 0,
                      size = 20,
                      sortBy = 'name',
                      direction = 'asc'
                  } = {}) {
        const response = await httpClient.get(CATEGORIES_PATH, {
            params: {
                page,
                size,
                sortBy,
                direction
            }
        })

        return response.data
    },

    async findOptions() {
        const response = await httpClient.get(
            `${CATEGORIES_PATH}/options`
        )

        return response.data
    },

    async findById(id) {
        const response = await httpClient.get(
            `${CATEGORIES_PATH}/${id}`
        )

        return response.data
    },

    async create(request) {
        const response = await httpClient.post(
            CATEGORIES_PATH,
            request
        )

        return response.data
    },

    async update(id, request) {
        const response = await httpClient.put(
            `${CATEGORIES_PATH}/${id}`,
            request
        )

        return response.data
    },

    async remove(id) {
        await httpClient.delete(
            `${CATEGORIES_PATH}/${id}`
        )
    }
}
import httpClient from './httpClient'

const CATEGORIES_PATH = '/categories'

export const categoryApi = {
    async findAll({
                      page = 0,
                      size = 1000,
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
    }
}
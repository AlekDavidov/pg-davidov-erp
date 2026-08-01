import httpClient from './httpClient'

const DASHBOARD_PATH = '/dashboard'

export const dashboardApi = {
    async getDashboard({
                           month,
                           year
                       } = {}) {
        const response = await httpClient.get(
            DASHBOARD_PATH,
            {
                params: {
                    month,
                    year
                }
            }
        )

        return response.data
    }
}
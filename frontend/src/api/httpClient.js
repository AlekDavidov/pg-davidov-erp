import axios from 'axios'

const httpClient = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

httpClient.interceptors.response.use(
    response => response,
    error => {
        const response = error.response

        if (!response) {
            return Promise.reject(
                new Error('Backend service is currently unavailable.')
            )
        }

        const backendMessage =
            response.data?.message ||
            response.data?.detail ||
            response.data?.error

        return Promise.reject(
            new Error(
                backendMessage ||
                `Request failed with status ${response.status}.`
            )
        )
    }
)

export default httpClient
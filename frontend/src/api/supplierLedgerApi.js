import axios from 'axios'

const BASE_URL =
    '/api/reports/suppliers'

export const supplierLedgerApi = {

    async getLedger(
        supplierId,
        periodFrom,
        periodTo
    ) {
        const response = await axios.get(
            `${BASE_URL}/${supplierId}/ledger`,
            {
                params: {
                    periodFrom,
                    periodTo
                }
            }
        )

        return response.data
    }
}
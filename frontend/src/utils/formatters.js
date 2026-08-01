export const formatDate = value => {
    if (!value) {
        return '—'
    }

    return new Intl.DateTimeFormat('sr-RS').format(
        new Date(`${value}T00:00:00`)
    )
}

export const formatDateTime = value => {
    if (!value) {
        return '—'
    }

    return new Intl.DateTimeFormat('sr-RS', {
        dateStyle: 'short',
        timeStyle: 'short'
    }).format(new Date(value))
}

export const formatAmount = (
    value,
    currencyCode = 'RSD'
) => {
    if (value === null || value === undefined) {
        return '—'
    }

    return new Intl.NumberFormat('sr-RS', {
        style: 'currency',
        currency: currencyCode,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(value)
}

export const formatInvoiceStatus = status => {
    const labels = {
        OPEN: 'Otvorena',
        OVERDUE: 'Dospela',
        PARTIALLY_PAID: 'Delimično plaćena',
        PARTIALLY_PAID_OVERDUE:
            'Delimično plaćena - dospela',
        PAID: 'Plaćena',
        OVERPAID: 'Preplaćena'
    }

    return labels[status] || status || '—'
}

export const getInvoiceStatusSeverity = status => {
    const severities = {
        OPEN: 'info',
        OVERDUE: 'danger',
        PARTIALLY_PAID: 'warn',
        PARTIALLY_PAID_OVERDUE: 'danger',
        PAID: 'success',
        OVERPAID: 'secondary'
    }

    return severities[status] || 'secondary'
}
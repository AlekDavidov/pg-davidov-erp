<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import InvoiceDialog from '../components/invoices/InvoiceDialog.vue'
import InvoicePaymentsDialog from '../components/invoices/InvoicePaymentsDialog.vue'
import InvoiceTable from '../components/invoices/InvoiceTable.vue'
import { useInvoiceStore } from '../stores/invoiceStore'

const toast = useToast()
const invoiceStore = useInvoiceStore()

const dialogVisible = ref(false)
const paymentsDialogVisible = ref(false)

const selectedInvoice = ref(null)
const selectedPaymentInvoice = ref(null)

const openCreateDialog = () => {
  selectedInvoice.value = null
  dialogVisible.value = true
}

const openEditDialog = invoice => {
  selectedInvoice.value = invoice
  dialogVisible.value = true
}

const openPaymentsDialog = invoice => {
  selectedPaymentInvoice.value = invoice
  paymentsDialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedInvoice.value = null
  }
}

const handlePaymentsDialogVisibility = visible => {
  paymentsDialogVisible.value = visible

  if (!visible) {
    selectedPaymentInvoice.value = null
  }
}

const handleSaved = event => {
  toast.add({
    severity: 'success',
    summary:
        event.mode === 'create'
            ? 'Faktura je kreirana'
            : 'Faktura je izmenjena',
    detail: event.invoiceNumber,
    life: 3000
  })

  selectedInvoice.value = null
}

const handlePaymentsChanged = async () => {
  await invoiceStore.fetchInvoices({
    page: invoiceStore.page,
    size: invoiceStore.size,
    sortBy: invoiceStore.sortBy,
    sortDirection: invoiceStore.sortDirection
  })

  if (!selectedPaymentInvoice.value?.id) {
    return
  }

  const refreshedInvoice = invoiceStore.invoices.find(
      invoice =>
          invoice.id === selectedPaymentInvoice.value.id
  )

  if (refreshedInvoice) {
    selectedPaymentInvoice.value = refreshedInvoice
  }
}
</script>

<template>
  <div class="invoices-view">
    <Toast />

    <div class="page-header">
      <div>
        <h2>Fakture</h2>

        <p>
          Upravljanje ulaznim fakturama, iznosima i statusima plaćanja.
        </p>
      </div>
    </div>

    <InvoiceTable
        @create="openCreateDialog"
        @edit="openEditDialog"
        @payments="openPaymentsDialog"
    />

    <InvoiceDialog
        :visible="dialogVisible"
        :invoice="selectedInvoice"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
    />

    <InvoicePaymentsDialog
        :visible="paymentsDialogVisible"
        :invoice="selectedPaymentInvoice"
        @update:visible="handlePaymentsDialogVisibility"
        @changed="handlePaymentsChanged"
    />
  </div>
</template>

<style scoped>
.invoices-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h2 {
  margin: 0;
  font-size: 1.75rem;
}

.page-header p {
  margin: 0.5rem 0 0;
  opacity: 0.7;
}
</style>
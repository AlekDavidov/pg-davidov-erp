<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import InvoiceDialog from '../components/invoices/InvoiceDialog.vue'
import InvoiceDocumentsDialog from '../components/invoices/InvoiceDocumentsDialog.vue'
import InvoicePaymentsDialog from '../components/invoices/InvoicePaymentsDialog.vue'
import InvoiceTable from '../components/invoices/InvoiceTable.vue'
import { useInvoiceStore } from '../stores/invoiceStore'

const toast = useToast()
const invoiceStore = useInvoiceStore()

const dialogVisible = ref(false)
const paymentsDialogVisible = ref(false)
const documentsDialogVisible = ref(false)

const selectedInvoice = ref(null)
const selectedPaymentInvoice = ref(null)
const selectedDocumentInvoice = ref(null)

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

const openDocumentsDialog = invoice => {
  selectedDocumentInvoice.value = invoice
  documentsDialogVisible.value = true
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

const handleDocumentsDialogVisibility = visible => {
  documentsDialogVisible.value = visible

  if (!visible) {
    selectedDocumentInvoice.value = null
  }
}

const refreshInvoices = async () => {
  await invoiceStore.fetchInvoices({
    page: invoiceStore.page,
    size: invoiceStore.size,
    sortBy: invoiceStore.sortBy,
    sortDirection: invoiceStore.sortDirection
  })
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
  await refreshInvoices()

  if (!selectedPaymentInvoice.value?.id) {
    return
  }

  const refreshedInvoice =
      invoiceStore.invoices.find(
          invoice =>
              invoice.id ===
              selectedPaymentInvoice.value.id
      )

  if (refreshedInvoice) {
    selectedPaymentInvoice.value =
        refreshedInvoice
  }
}

const handleDocumentsChanged = async () => {
  await refreshInvoices()

  if (!selectedDocumentInvoice.value?.id) {
    return
  }

  const refreshedInvoice =
      invoiceStore.invoices.find(
          invoice =>
              invoice.id ===
              selectedDocumentInvoice.value.id
      )

  if (refreshedInvoice) {
    selectedDocumentInvoice.value =
        refreshedInvoice
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
          Upravljanje ulaznim fakturama,
          iznosima, dokumentima i statusima
          plaćanja.
        </p>
      </div>
    </div>

    <InvoiceTable
        @create="openCreateDialog"
        @edit="openEditDialog"
        @payments="openPaymentsDialog"
        @documents="openDocumentsDialog"
    />

    <InvoiceDialog
        :visible="dialogVisible"
        :invoice="selectedInvoice"
        @update:visible="
        handleDialogVisibility
      "
        @saved="handleSaved"
    />

    <InvoicePaymentsDialog
        :visible="paymentsDialogVisible"
        :invoice="selectedPaymentInvoice"
        @update:visible="
        handlePaymentsDialogVisibility
      "
        @changed="
        handlePaymentsChanged
      "
    />

    <InvoiceDocumentsDialog
        :visible="
        documentsDialogVisible
      "
        :invoice="
        selectedDocumentInvoice
      "
        @update:visible="
        handleDocumentsDialogVisibility
      "
        @changed="
        handleDocumentsChanged
      "
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
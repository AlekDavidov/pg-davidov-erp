<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import InvoiceDialog from '../components/invoices/InvoiceDialog.vue'
import InvoiceTable from '../components/invoices/InvoiceTable.vue'

const toast = useToast()

const dialogVisible = ref(false)
const selectedInvoice = ref(null)

const openCreateDialog = () => {
  selectedInvoice.value = null
  dialogVisible.value = true
}

const openEditDialog = invoice => {
  selectedInvoice.value = invoice
  dialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedInvoice.value = null
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
    />

    <InvoiceDialog
        :visible="dialogVisible"
        :invoice="selectedInvoice"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
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
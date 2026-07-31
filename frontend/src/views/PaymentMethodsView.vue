<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import PaymentMethodDialog from '../components/payment-methods/PaymentMethodDialog.vue'
import PaymentMethodTable from '../components/payment-methods/PaymentMethodTable.vue'

const toast = useToast()

const dialogVisible = ref(false)
const selectedPaymentMethod = ref(null)

const openCreateDialog = () => {
  selectedPaymentMethod.value = null
  dialogVisible.value = true
}

const openEditDialog = paymentMethod => {
  selectedPaymentMethod.value = paymentMethod
  dialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedPaymentMethod.value = null
  }
}

const handleSaved = event => {
  toast.add({
    severity: 'success',
    summary:
        event.mode === 'create'
            ? 'Način plaćanja je kreiran'
            : 'Način plaćanja je izmenjen',
    detail: event.name,
    life: 3000
  })

  selectedPaymentMethod.value = null
}
</script>

<template>
  <div class="payment-methods-view">
    <Toast />

    <div class="page-header">
      <div>
        <h2>Načini plaćanja</h2>

        <p>
          Upravljanje načinima plaćanja.
        </p>
      </div>
    </div>

    <PaymentMethodTable
        @create="openCreateDialog"
        @edit="openEditDialog"
    />

    <PaymentMethodDialog
        :visible="dialogVisible"
        :payment-method="selectedPaymentMethod"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.payment-methods-view {
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
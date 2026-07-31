<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import BankAccountDialog from '../components/bank-accounts/BankAccountDialog.vue'
import BankAccountTable from '../components/bank-accounts/BankAccountTable.vue'

const toast = useToast()

const dialogVisible = ref(false)
const selectedBankAccount = ref(null)

const openCreateDialog = () => {
  selectedBankAccount.value = null
  dialogVisible.value = true
}

const openEditDialog = bankAccount => {
  selectedBankAccount.value = bankAccount
  dialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedBankAccount.value = null
  }
}

const handleSaved = event => {
  toast.add({
    severity: 'success',
    summary:
        event.mode === 'create'
            ? 'Bankovni račun je kreiran'
            : 'Bankovni račun je izmenjen',
    detail: event.name,
    life: 3000
  })

  selectedBankAccount.value = null
}
</script>

<template>
  <div class="bank-accounts-view">
    <Toast />

    <div class="page-header">
      <div>
        <h2>Bankovni računi</h2>

        <p>
          Upravljanje bankovnim računima.
        </p>
      </div>
    </div>

    <BankAccountTable
        @create="openCreateDialog"
        @edit="openEditDialog"
    />

    <BankAccountDialog
        :visible="dialogVisible"
        :bank-account="selectedBankAccount"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.bank-accounts-view {
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
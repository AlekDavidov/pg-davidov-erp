<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import SupplierDialog from '../components/suppliers/SupplierDialog.vue'
import SupplierTable from '../components/suppliers/SupplierTable.vue'

const toast = useToast()

const dialogVisible = ref(false)
const selectedSupplier = ref(null)

const openCreateDialog = () => {
  selectedSupplier.value = null
  dialogVisible.value = true
}

const openEditDialog = supplier => {
  selectedSupplier.value = supplier
  dialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedSupplier.value = null
  }
}

const handleSaved = event => {
  toast.add({
    severity: 'success',
    summary:
        event.mode === 'create'
            ? 'Dobavljač je kreiran'
            : 'Dobavljač je izmenjen',
    detail: event.name,
    life: 3000
  })

  selectedSupplier.value = null
}
</script>

<template>
  <div class="suppliers-view">
    <Toast />

    <div class="page-header">
      <div>
        <h2>Dobavljači</h2>

        <p>
          Upravljanje dobavljačima i njihovim podrazumevanim
          uslovima plaćanja.
        </p>
      </div>
    </div>

    <SupplierTable
        @create="openCreateDialog"
        @edit="openEditDialog"
    />

    <SupplierDialog
        :visible="dialogVisible"
        :supplier="selectedSupplier"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.suppliers-view {
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
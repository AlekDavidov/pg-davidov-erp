<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'

import Toast from 'primevue/toast'

import CategoryDialog from '../components/categories/CategoryDialog.vue'
import CategoryTable from '../components/categories/CategoryTable.vue'

const toast = useToast()

const dialogVisible = ref(false)
const selectedCategory = ref(null)

const openCreateDialog = () => {
  selectedCategory.value = null
  dialogVisible.value = true
}

const openEditDialog = category => {
  selectedCategory.value = category
  dialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedCategory.value = null
  }
}

const handleSaved = event => {
  toast.add({
    severity: 'success',
    summary:
        event.mode === 'create'
            ? 'Kategorija je kreirana'
            : 'Kategorija je izmenjena',
    detail: event.name,
    life: 3000
  })

  selectedCategory.value = null
}
</script>

<template>
  <div class="categories-view">
    <Toast />

    <div class="page-header">
      <div>
        <h2>Kategorije</h2>

        <p>
          Upravljanje kategorijama prihoda i rashoda.
        </p>
      </div>
    </div>

    <CategoryTable
        @create="openCreateDialog"
        @edit="openEditDialog"
    />

    <CategoryDialog
        :visible="dialogVisible"
        :category="selectedCategory"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.categories-view {
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
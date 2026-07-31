<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'

import Button from 'primevue/button'
import Column from 'primevue/column'
import ConfirmDialog from 'primevue/confirmdialog'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import Tag from 'primevue/tag'
import Tooltip from 'primevue/tooltip'

import { useCategoryStore } from '../../stores/categoryStore'

const vTooltip = Tooltip

const emit = defineEmits([
  'create',
  'edit'
])

const categoryStore = useCategoryStore()
const confirm = useConfirm()
const toast = useToast()

const {
  categories,
  loading,
  deleting,
  error,
  page,
  size,
  totalElements,
  sortBy,
  direction
} = storeToRefs(categoryStore)

const loadCategories = async () => {
  await categoryStore.fetchCategories()
}

const handlePage = async event => {
  await categoryStore.fetchCategories({
    page: event.page,
    size: event.rows
  })
}

const handleSort = async event => {
  if (!event.sortField) {
    await categoryStore.fetchCategories({
      page: 0,
      size: size.value,
      sortBy: 'name',
      direction: 'asc'
    })

    return
  }

  await categoryStore.fetchCategories({
    page: 0,
    size: size.value,
    sortBy: event.sortField,
    direction: event.sortOrder === -1
        ? 'desc'
        : 'asc'
  })
}

const formatCategoryType = categoryType => {
  if (categoryType === 'INCOME') {
    return 'Prihod'
  }

  if (categoryType === 'EXPENSE') {
    return 'Rashod'
  }

  return categoryType || '—'
}

const getCategoryTypeSeverity = categoryType => {
  if (categoryType === 'INCOME') {
    return 'success'
  }

  if (categoryType === 'EXPENSE') {
    return 'warn'
  }

  return 'secondary'
}

const formatDateTime = value => {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('sr-RS', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(new Date(value))
}

const confirmDelete = category => {
  confirm.require({
    header: 'Brisanje kategorije',
    message:
        `Da li ste sigurni da želite da obrišete kategoriju „${category.name}“?`,
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Otkaži',
    acceptLabel: 'Obriši',
    rejectProps: {
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      severity: 'danger'
    },
    accept: async () => {
      try {
        await categoryStore.deleteCategory(category.id)

        toast.add({
          severity: 'success',
          summary: 'Kategorija je obrisana',
          detail: category.name,
          life: 3000
        })
      } catch {
        toast.add({
          severity: 'error',
          summary: 'Brisanje nije uspelo',
          detail:
              'Kategorija nije mogla da bude obrisana.',
          life: 4000
        })
      }
    }
  })
}

onMounted(loadCategories)
</script>

<template>
  <div class="category-table">
    <ConfirmDialog />

    <Message
        v-if="error"
        severity="error"
        closable
        @close="categoryStore.clearError()"
    >
      {{ error }}
    </Message>

    <div class="table-toolbar">
      <Button
          label="Nova kategorija"
          icon="pi pi-plus"
          @click="emit('create')"
      />

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadCategories"
      />
    </div>

    <DataTable
        :value="categories"
        :loading="loading"
        :lazy="true"
        :paginator="true"
        :rows="size"
        :first="page * size"
        :total-records="totalElements"
        :rows-per-page-options="[10, 20, 50]"
        :sort-field="sortBy"
        :sort-order="direction === 'desc' ? -1 : 1"
        data-key="id"
        striped-rows
        removable-sort
        paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown CurrentPageReport"
        current-page-report-template="{first}–{last} od ukupno {totalRecords}"
        @page="handlePage"
        @sort="handleSort"
        @row-dblclick="emit('edit', $event.data)"
    >
      <template #empty>
        Nema pronađenih kategorija.
      </template>

      <Column
          field="code"
          header="Šifra"
          sortable
      />

      <Column
          field="name"
          header="Naziv"
          sortable
      >
        <template #body="{ data }">
          <button
              class="category-name-button"
              type="button"
              @click="emit('edit', data)"
          >
            {{ data.name }}
          </button>
        </template>
      </Column>

      <Column
          field="categoryType"
          header="Tip"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="formatCategoryType(data.categoryType)"
              :severity="getCategoryTypeSeverity(data.categoryType)"
          />
        </template>
      </Column>

      <Column
          field="active"
          header="Status"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="data.active ? 'Aktivna' : 'Neaktivna'"
              :severity="
                            data.active
                                ? 'success'
                                : 'secondary'
                        "
          />
        </template>
      </Column>

      <Column header="Kreirana">
        <template #body="{ data }">
          {{ formatDateTime(data.createdAt) }}
        </template>
      </Column>

      <Column header="Akcije">
        <template #body="{ data }">
          <div class="row-actions">
            <Button
                v-tooltip.top="'Izmeni kategoriju'"
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                aria-label="Izmeni kategoriju"
                @click="emit('edit', data)"
            />

            <Button
                v-tooltip.top="'Obriši kategoriju'"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                aria-label="Obriši kategoriju"
                :disabled="deleting"
                @click="confirmDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
.category-table {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.category-name-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.category-name-button:hover {
  text-decoration: underline;
}

.category-table :deep(.p-datatable) {
  width: 100%;
}
</style>
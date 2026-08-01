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

import { useSupplierStore } from '../../stores/supplierStore'
import { formatDateTime } from '../../utils/formatters'

const vTooltip = Tooltip

const emit = defineEmits([
  'create',
  'edit'
])

const supplierStore = useSupplierStore()
const confirm = useConfirm()
const toast = useToast()

const {
  suppliers,
  loading,
  deleting,
  error,
  page,
  size,
  totalElements,
  sortBy,
  direction
} = storeToRefs(supplierStore)

const loadSuppliers = async () => {
  await supplierStore.fetchSuppliers()
}

const handlePage = async event => {
  await supplierStore.fetchSuppliers({
    page: event.page,
    size: event.rows
  })
}

const handleSort = async event => {
  if (!event.sortField) {
    await supplierStore.fetchSuppliers({
      page: 0,
      size: size.value,
      sortBy: 'name',
      direction: 'asc'
    })

    return
  }

  await supplierStore.fetchSuppliers({
    page: 0,
    size: size.value,
    sortBy: event.sortField,
    direction: event.sortOrder === -1
        ? 'desc'
        : 'asc'
  })
}

const formatPaymentTerms = value => {
  if (value === null || value === undefined) {
    return '—'
  }

  return `${value} dana`
}

const confirmDelete = supplier => {
  confirm.require({
    header: 'Brisanje dobavljača',
    message:
        `Da li ste sigurni da želite da obrišete dobavljača „${supplier.name}”?`,
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
        await supplierStore.deleteSupplier(
            supplier.id
        )

        toast.add({
          severity: 'success',
          summary: 'Dobavljač je obrisan',
          detail: supplier.name,
          life: 3000
        })
      } catch {
        toast.add({
          severity: 'error',
          summary: 'Brisanje nije uspelo',
          detail:
              'Dobavljač nije mogao da bude obrisan.',
          life: 4000
        })
      }
    }
  })
}

onMounted(loadSuppliers)
</script>

<template>
  <div class="supplier-table">
    <ConfirmDialog />

    <Message
        v-if="error"
        severity="error"
        closable
        @close="supplierStore.clearError()"
    >
      {{ error }}
    </Message>

    <div class="table-toolbar">
      <Button
          label="Novi dobavljač"
          icon="pi pi-plus"
          @click="emit('create')"
      />

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadSuppliers"
      />
    </div>

    <DataTable
        :value="suppliers"
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
        Nema pronađenih dobavljača.
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
              class="supplier-name-button"
              type="button"
              @click="emit('edit', data)"
          >
            {{ data.name }}
          </button>
        </template>
      </Column>

      <Column
          field="defaultCategoryName"
          header="Kategorija"
      >
        <template #body="{ data }">
          {{ data.defaultCategoryName || '—' }}
        </template>
      </Column>

      <Column
          field="paymentMethodName"
          header="Način plaćanja"
      >
        <template #body="{ data }">
          {{ data.paymentMethodName || '—' }}
        </template>
      </Column>

      <Column
          field="paymentTerms"
          header="Rok plaćanja"
          sortable
      >
        <template #body="{ data }">
          {{ formatPaymentTerms(data.paymentTerms) }}
        </template>
      </Column>

      <Column
          field="contactPerson"
          header="Kontakt osoba"
      >
        <template #body="{ data }">
          {{ data.contactPerson || '—' }}
        </template>
      </Column>

      <Column
          field="active"
          header="Status"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="data.active ? 'Aktivan' : 'Neaktivan'"
              :severity="
              data.active
                ? 'success'
                : 'secondary'
            "
          />
        </template>
      </Column>

      <Column header="Kreiran">
        <template #body="{ data }">
          {{ formatDateTime(data.createdAt) }}
        </template>
      </Column>

      <Column header="Akcije">
        <template #body="{ data }">
          <div class="row-actions">
            <Button
                v-tooltip.top="'Izmeni dobavljača'"
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                aria-label="Izmeni dobavljača"
                @click="emit('edit', data)"
            />

            <Button
                v-tooltip.top="'Obriši dobavljača'"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                aria-label="Obriši dobavljača"
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
.supplier-table {
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

.supplier-name-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.supplier-name-button:hover {
  text-decoration: underline;
}

.supplier-table :deep(.p-datatable) {
  width: 100%;
}
</style>
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

import { useInvoiceStore } from '../../stores/invoiceStore'

const vTooltip = Tooltip

const emit = defineEmits([
  'create',
  'edit'
])

const invoiceStore = useInvoiceStore()
const confirm = useConfirm()
const toast = useToast()

const {
  invoices,
  loading,
  deleting,
  error,
  page,
  size,
  totalElements,
  sortBy,
  sortDirection
} = storeToRefs(invoiceStore)

const loadInvoices = async () => {
  await invoiceStore.fetchInvoices()
}

const handlePage = async event => {
  await invoiceStore.fetchInvoices({
    page: event.page,
    size: event.rows
  })
}

const handleSort = async event => {
  if (!event.sortField) {
    await invoiceStore.fetchInvoices({
      page: 0,
      size: size.value,
      sortBy: 'invoiceDate',
      sortDirection: 'desc'
    })

    return
  }

  await invoiceStore.fetchInvoices({
    page: 0,
    size: size.value,
    sortBy: event.sortField,
    sortDirection:
        event.sortOrder === -1
            ? 'desc'
            : 'asc'
  })
}

const formatDate = value => {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('sr-RS').format(
      new Date(`${value}T00:00:00`)
  )
}

const formatAmount = (value, currencyCode) => {
  if (value === null || value === undefined) {
    return '—'
  }

  return new Intl.NumberFormat('sr-RS', {
    style: 'currency',
    currency: currencyCode || 'RSD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

const formatStatus = status => {
  const labels = {
    OPEN: 'Otvorena',
    OVERDUE: 'Dospela',
    PARTIALLY_PAID: 'Delimično plaćena',
    PARTIALLY_PAID_OVERDUE: 'Delimično plaćena - dospela',
    PAID: 'Plaćena',
    OVERPAID: 'Preplaćena'
  }

  return labels[status] || status || '—'
}

const getStatusSeverity = status => {
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

const confirmDelete = invoice => {
  confirm.require({
    header: 'Brisanje fakture',
    message:
        `Da li ste sigurni da želite da obrišete fakturu „${invoice.invoiceNumber}“?`,
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
        await invoiceStore.deleteInvoice(invoice.id)

        toast.add({
          severity: 'success',
          summary: 'Faktura je obrisana',
          detail: invoice.invoiceNumber,
          life: 3000
        })
      } catch {
        toast.add({
          severity: 'error',
          summary: 'Brisanje nije uspelo',
          detail:
              'Faktura nije mogla da bude obrisana.',
          life: 4000
        })
      }
    }
  })
}

onMounted(loadInvoices)
</script>

<template>
  <div class="invoice-table">
    <ConfirmDialog />

    <Message
        v-if="error"
        severity="error"
        closable
        @close="invoiceStore.clearError()"
    >
      {{ error }}
    </Message>

    <div class="table-toolbar">
      <Button
          label="Nova faktura"
          icon="pi pi-plus"
          @click="emit('create')"
      />

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadInvoices"
      />
    </div>

    <DataTable
        :value="invoices"
        :loading="loading"
        :lazy="true"
        :paginator="true"
        :rows="size"
        :first="page * size"
        :total-records="totalElements"
        :rows-per-page-options="[10, 20, 50]"
        :sort-field="sortBy"
        :sort-order="sortDirection === 'desc' ? -1 : 1"
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
        Nema pronađenih faktura.
      </template>

      <Column
          field="invoiceCode"
          header="Šifra"
          sortable
      />

      <Column
          field="invoiceNumber"
          header="Broj fakture"
          sortable
      >
        <template #body="{ data }">
          <button
              class="invoice-number-button"
              type="button"
              @click="emit('edit', data)"
          >
            {{ data.invoiceNumber }}
          </button>
        </template>
      </Column>

      <Column
          field="supplierName"
          header="Dobavljač"
      />

      <Column
          field="invoiceDate"
          header="Datum fakture"
          sortable
      >
        <template #body="{ data }">
          {{ formatDate(data.invoiceDate) }}
        </template>
      </Column>

      <Column
          field="dueDate"
          header="Dospeće"
          sortable
      >
        <template #body="{ data }">
          {{ formatDate(data.dueDate) }}
        </template>
      </Column>

      <Column
          field="amount"
          header="Iznos"
          sortable
      >
        <template #body="{ data }">
          {{ formatAmount(data.amount, data.currencyCode) }}
        </template>
      </Column>

      <Column
          field="paidAmount"
          header="Plaćeno"
      >
        <template #body="{ data }">
          {{ formatAmount(data.paidAmount, data.currencyCode) }}
        </template>
      </Column>

      <Column
          field="remainingAmount"
          header="Preostalo"
      >
        <template #body="{ data }">
          {{ formatAmount(data.remainingAmount, data.currencyCode) }}
        </template>
      </Column>

      <Column
          field="status"
          header="Status"
      >
        <template #body="{ data }">
          <Tag
              :value="formatStatus(data.status)"
              :severity="getStatusSeverity(data.status)"
          />
        </template>
      </Column>

      <Column header="Akcije">
        <template #body="{ data }">
          <div class="row-actions">
            <Button
                v-tooltip.top="'Izmeni fakturu'"
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                aria-label="Izmeni fakturu"
                @click="emit('edit', data)"
            />

            <Button
                v-tooltip.top="'Obriši fakturu'"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                aria-label="Obriši fakturu"
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
.invoice-table {
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

.invoice-number-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.invoice-number-button:hover {
  text-decoration: underline;
}

.invoice-table :deep(.p-datatable) {
  width: 100%;
}
</style>
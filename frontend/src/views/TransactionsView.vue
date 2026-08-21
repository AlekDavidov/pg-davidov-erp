<script setup>
import { onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'

import Button from 'primevue/button'
import Column from 'primevue/column'
import ConfirmDialog from 'primevue/confirmdialog'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import Tag from 'primevue/tag'
import Toast from 'primevue/toast'
import Tooltip from 'primevue/tooltip'

import TransactionDialog from '../components/transactions/TransactionDialog.vue'
import { useTransactionStore } from '../stores/transactionStore'
import {
  formatAmount,
  formatDate
} from '../utils/formatters'

const vTooltip = Tooltip

const transactionStore = useTransactionStore()
const confirm = useConfirm()
const toast = useToast()

const {
  transactions,
  loading,
  deleting,
  error
} = storeToRefs(transactionStore)

const dialogVisible = ref(false)
const selectedTransaction = ref(null)

const loadTransactions = async () => {
  await transactionStore.fetchTransactions()
}

const openCreateDialog = () => {
  selectedTransaction.value = null
  dialogVisible.value = true
}

const openEditDialog = transaction => {
  selectedTransaction.value = transaction
  dialogVisible.value = true
}

const handleDialogVisibility = visible => {
  dialogVisible.value = visible

  if (!visible) {
    selectedTransaction.value = null
  }
}

const handleSaved = event => {
  toast.add({
    severity: 'success',
    summary:
        event.mode === 'create'
            ? 'Transakcija je kreirana'
            : 'Transakcija je izmenjena',
    detail: event.description,
    life: 3000
  })

  selectedTransaction.value = null
}

const getStatusLabel = status => {
  const labels = {
    NEW: 'Nova',
    VERIFIED: 'Proverena',
    MATCHED: 'Povezana',
    CANCELLED: 'Stornirana'
  }

  return labels[status] || status || '—'
}

const getStatusSeverity = status => {
  const severities = {
    NEW: 'info',
    VERIFIED: 'success',
    MATCHED: 'warn',
    CANCELLED: 'danger'
  }

  return severities[status] || 'secondary'
}

const getSourceLabel = source => {
  const labels = {
    MANUAL: 'Ručno',
    BANK_IMPORT: 'Bankovni izvod',
    MIGRATION: 'Migracija'
  }

  return labels[source] || source || '—'
}

const confirmDelete = transaction => {
  confirm.require({
    header: 'Brisanje transakcije',
    message:
        `Da li ste sigurni da želite da obrišete transakciju „${transaction.transactionCode}“?`,
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
        await transactionStore.deleteTransaction(
            transaction.id
        )

        toast.add({
          severity: 'success',
          summary: 'Transakcija je obrisana',
          detail: transaction.transactionCode,
          life: 3000
        })
      } catch {
        toast.add({
          severity: 'error',
          summary: 'Brisanje nije uspelo',
          detail:
              'Transakcija nije mogla da bude obrisana.',
          life: 4000
        })
      }
    }
  })
}

onMounted(loadTransactions)
</script>

<template>
  <div class="transactions-view">
    <Toast />
    <ConfirmDialog />

    <div class="page-header">
      <div>
        <h2>Transakcije</h2>

        <p>
          Pregled prihoda, rashoda i ostalih finansijskih
          transakcija.
        </p>
      </div>
    </div>

    <Message
        v-if="error"
        severity="error"
        closable
        @close="transactionStore.clearError()"
    >
      {{ error }}
    </Message>

    <div class="table-toolbar">
      <Button
          label="Nova transakcija"
          icon="pi pi-plus"
          @click="openCreateDialog"
      />

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadTransactions"
      />
    </div>

    <DataTable
        :value="transactions"
        :loading="loading"
        data-key="id"
        striped-rows
        paginator
        :rows="10"
        :rows-per-page-options="[10, 20, 50]"
        paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown CurrentPageReport"
        current-page-report-template="{first}–{last} od ukupno {totalRecords}"
        @row-dblclick="openEditDialog($event.data)"
    >
      <template #empty>
        Nema evidentiranih transakcija.
      </template>

      <Column
          field="transactionCode"
          header="Šifra"
          sortable
      >
        <template #body="{ data }">
          <button
              class="transaction-code-button"
              type="button"
              @click="openEditDialog(data)"
          >
            {{ data.transactionCode }}
          </button>
        </template>
      </Column>

      <Column
          field="transactionDate"
          header="Datum"
          sortable
      >
        <template #body="{ data }">
          {{ formatDate(data.transactionDate) }}
        </template>
      </Column>

      <Column
          field="description"
          header="Opis"
          sortable
      >
        <template #body="{ data }">
          {{ data.description || '—' }}
        </template>
      </Column>

      <Column
          field="rawCounterparty"
          header="Druga strana"
          sortable
      >
        <template #body="{ data }">
          {{ data.rawCounterparty || '—' }}
        </template>
      </Column>

      <Column
          field="debit"
          header="Rashod"
          sortable
      >
        <template #body="{ data }">
          <span
              :class="{
              'amount-expense':
                data.debit !== null &&
                Number(data.debit) !== 0
            }"
          >
            {{
              data.debit !== null &&
              Number(data.debit) !== 0
                  ? formatAmount(
                      data.debit,
                      data.currencyCode
                  )
                  : '—'
            }}
          </span>
        </template>
      </Column>

      <Column
          field="credit"
          header="Prihod"
          sortable
      >
        <template #body="{ data }">
          <span
              :class="{
              'amount-income':
                data.credit !== null &&
                Number(data.credit) !== 0
            }"
          >
            {{
              data.credit !== null &&
              Number(data.credit) !== 0
                  ? formatAmount(
                      data.credit,
                      data.currencyCode
                  )
                  : '—'
            }}
          </span>
        </template>
      </Column>

      <Column
          field="status"
          header="Status"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="getStatusLabel(data.status)"
              :severity="getStatusSeverity(data.status)"
          />
        </template>
      </Column>

      <Column
          field="source"
          header="Izvor"
          sortable
      >
        <template #body="{ data }">
          {{ getSourceLabel(data.source) }}
        </template>
      </Column>

      <Column header="Akcije">
        <template #body="{ data }">
          <div class="row-actions">
            <Button
                v-tooltip.top="'Izmeni transakciju'"
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                aria-label="Izmeni transakciju"
                @click="openEditDialog(data)"
            />

            <Button
                v-tooltip.top="'Obriši transakciju'"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                aria-label="Obriši transakciju"
                :disabled="deleting"
                @click="confirmDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>

    <TransactionDialog
        :visible="dialogVisible"
        :transaction="selectedTransaction"
        @update:visible="handleDialogVisibility"
        @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.transactions-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.page-header h2 {
  margin: 0;
  font-size: 1.75rem;
}

.page-header p {
  margin: 0.5rem 0 0;
  opacity: 0.7;
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

.transaction-code-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.transaction-code-button:hover {
  text-decoration: underline;
}

.amount-expense {
  font-weight: 600;
  color: #dc2626;
}

.amount-income {
  font-weight: 600;
  color: #16a34a;
}

.transactions-view :deep(.p-datatable) {
  width: 100%;
}

@media (max-width: 800px) {
  .page-header,
  .table-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
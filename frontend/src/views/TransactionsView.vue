<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import Tag from 'primevue/tag'

import { useTransactionStore } from '../stores/transactionStore'

const transactionStore = useTransactionStore()

const {
  transactions,
  loading,
  error
} = storeToRefs(transactionStore)

const loadTransactions = async () => {
  await transactionStore.fetchTransactions()
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
    minimumFractionDigits: 2
  }).format(value)
}

const getStatusLabel = status => {
  const labels = {
    DRAFT: 'Nacrt',
    POSTED: 'Proknjižena',
    CANCELLED: 'Stornirana'
  }

  return labels[status] || status || '—'
}

const getStatusSeverity = status => {
  const severities = {
    DRAFT: 'secondary',
    POSTED: 'success',
    CANCELLED: 'danger'
  }

  return severities[status] || 'info'
}

const getSourceLabel = source => {
  const labels = {
    MANUAL: 'Ručno',
    IMPORT: 'Uvoz',
    BANK_IMPORT: 'Bankovni izvod'
  }

  return labels[source] || source || '—'
}

onMounted(loadTransactions)
</script>

<template>
  <div class="transactions-view">
    <div class="page-header">
      <div>
        <h2>Transakcije</h2>

        <p>
          Pregled prihoda, rashoda i ostalih finansijskih
          transakcija.
        </p>
      </div>

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadTransactions"
      />
    </div>

    <Message
        v-if="error"
        severity="error"
        closable
        @close="transactionStore.clearError()"
    >
      {{ error }}
    </Message>

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
    >
      <template #empty>
        Nema evidentiranih transakcija.
      </template>

      <Column
          field="transactionCode"
          header="Šifra"
          sortable
      />

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

      <Column
          field="verified"
          header="Proverena"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="data.verified ? 'Da' : 'Ne'"
              :severity="
                data.verified
                    ? 'success'
                    : 'secondary'
              "
          />
        </template>
      </Column>
    </DataTable>
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
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
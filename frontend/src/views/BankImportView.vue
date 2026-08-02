<script setup>
import { storeToRefs } from 'pinia'

import Message from 'primevue/message'

import StatementSummary from '../components/bank-import/StatementSummary.vue'
import TransactionsTable from '../components/bank-import/TransactionsTable.vue'
import UploadZone from '../components/bank-import/UploadZone.vue'
import { useBankImportStore } from '../stores/bankImportStore'

const bankImportStore =
    useBankImportStore()

const {
  preview,
  transactions,
  transactionCount,
  error
} = storeToRefs(bankImportStore)

const clearError = () => {
  bankImportStore.error = null
}

const resetImport = () => {
  bankImportStore.reset()
}
</script>

<template>
  <div class="bank-import-view">
    <div class="page-header">
      <div>
        <span class="page-eyebrow">
          Bankovni izvodi
        </span>

        <h2>Uvoz bankarskog izvoda</h2>

        <p>
          Dodajte bankarski izvod, proverite
          automatski prepoznate transakcije i
          pripremite ih za uvoz.
        </p>
      </div>
    </div>

    <Message
        v-if="error"
        severity="error"
        closable
        @close="clearError"
    >
      {{ error }}
    </Message>

    <UploadZone
        v-if="!preview"
    />

    <template v-else>
      <StatementSummary
          :preview="preview"
          @reset="resetImport"
      />

      <TransactionsTable
          :transactions="transactions"
          :transaction-count="transactionCount"
      />
    </template>
  </div>
</template>

<style scoped>
.bank-import-view {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.page-eyebrow {
  color: var(--brand-blue);
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.page-header h2 {
  margin: 0.4rem 0 0;
  color: var(--app-text);
  font-size: 1.65rem;
}

.page-header p {
  max-width: 44rem;
  margin: 0.55rem 0 0;
  color: var(--app-text-muted);
  line-height: 1.6;
}

@media (max-width: 520px) {
  .page-header h2 {
    font-size: 1.35rem;
  }
}
</style>
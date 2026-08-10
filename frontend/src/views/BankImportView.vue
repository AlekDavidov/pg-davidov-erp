<script setup>
import {
  computed,
  onMounted,
  ref
} from 'vue'
import { storeToRefs } from 'pinia'

import Message from 'primevue/message'
import Select from 'primevue/select'

import StatementSummary from '../components/bank-import/StatementSummary.vue'
import TransactionsTable from '../components/bank-import/TransactionsTable.vue'
import UploadZone from '../components/bank-import/UploadZone.vue'
import { useBankAccountStore } from '../stores/bankAccountStore'
import { useBankImportStore } from '../stores/bankImportStore'

const bankImportStore =
    useBankImportStore()

const bankAccountStore =
    useBankAccountStore()

const {
  preview,
  transactions,
  transactionCount,
  error,
  importError,
  importResult
} = storeToRefs(bankImportStore)

const {
  bankAccounts,
  loading: bankAccountsLoading
} = storeToRefs(bankAccountStore)

const selectedBankAccountId =
    ref(null)

const activeBankAccounts = computed(() =>
    bankAccounts.value.filter(
        bankAccount =>
            bankAccount.active
    )
)

const resolveBankAccount = () => {
  if (!preview.value) {
    selectedBankAccountId.value = null

    return
  }

  const matchingBankAccount =
      activeBankAccounts.value.find(
          bankAccount =>
              bankAccount.accountNumber ===
              preview.value.accountNumber
      )

  selectedBankAccountId.value =
      matchingBankAccount?.id || null
}

const handleImport = async () => {
  await bankImportStore.importTransactions(
      selectedBankAccountId.value
  )
}

const clearError = () => {
  bankImportStore.error = null
}

const clearImportError = () => {
  bankImportStore.importError = null
}

const resetImport = () => {
  selectedBankAccountId.value = null
  bankImportStore.reset()
}

onMounted(async () => {
  await bankAccountStore.fetchBankAccounts()
})
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

    <Message
        v-if="importError"
        severity="error"
        closable
        @close="clearImportError"
    >
      {{ importError }}
    </Message>

    <Message
        v-if="importResult"
        severity="success"
        :closable="false"
    >
      Uvezeno:
      {{ importResult.importedCount }}.

      Preskočeno kao duplikat:
      {{ importResult.skippedDuplicateCount }}.
    </Message>

    <UploadZone
        v-if="!preview"
    />

    <template v-else>
      <StatementSummary
          :preview="preview"
          @reset="resetImport"
      />

      <section class="bank-account-panel">
        <div>
          <h3>Bankovni račun</h3>

          <p>
            Izaberite račun na koji se odnosi
            bankarski izvod.
          </p>
        </div>

        <Select
            v-model="selectedBankAccountId"
            :options="activeBankAccounts"
            option-label="accountNumber"
            option-value="id"
            placeholder="Izaberite bankovni račun"
            :loading="bankAccountsLoading"
            class="bank-account-select"
            @show="resolveBankAccount"
        >
          <template #option="{ option }">
            <div class="bank-account-option">
              <strong>
                {{ option.bankName }}
              </strong>

              <span>
                {{ option.accountNumber }}
              </span>

              <small>
                {{ option.currencyCode }}
              </small>
            </div>
          </template>

          <template #value="{ value }">
            <span v-if="value">
              {{
                activeBankAccounts.find(
                    bankAccount =>
                        bankAccount.id === value
                )?.accountNumber
              }}
            </span>

            <span v-else>
              Izaberite bankovni račun
            </span>
          </template>
        </Select>
      </section>

      <TransactionsTable
          :transactions="transactions"
          :transaction-count="transactionCount"
          :bank-account-id="selectedBankAccountId"
          @import="handleImport"
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

.bank-account-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  padding: 1.25rem;
  border: 1px solid var(--app-border);
  border-radius: 1rem;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
}

.bank-account-panel h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 1rem;
}

.bank-account-panel p {
  margin: 0.35rem 0 0;
  color: var(--app-text-muted);
  font-size: 0.8rem;
}

.bank-account-select {
  width: min(26rem, 100%);
}

.bank-account-option {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.bank-account-option strong {
  font-size: 0.8rem;
}

.bank-account-option span,
.bank-account-option small {
  color: var(--app-text-muted);
  font-size: 0.7rem;
}

@media (max-width: 800px) {
  .bank-account-panel {
    align-items: stretch;
    flex-direction: column;
  }

  .bank-account-select {
    width: 100%;
  }
}

@media (max-width: 520px) {
  .page-header h2 {
    font-size: 1.35rem;
  }
}
</style>
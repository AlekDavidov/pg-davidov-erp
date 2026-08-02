<script setup>
import {
  computed
} from 'vue'

import Button from 'primevue/button'
import Tag from 'primevue/tag'

import {
  formatAmount,
  formatDate
} from '../../utils/formatters'

const props = defineProps({
  transactions: {
    type: Array,
    default: () => []
  },

  transactionCount: {
    type: Number,
    default: 0
  }
})

const resolvedTransactionCount = computed(() =>
    props.transactionCount ||
    props.transactions.length
)

const getTransactionType = transaction => {
  if (
      Number(transaction.credit || 0) > 0
  ) {
    return {
      label: 'Prihod',
      severity: 'success',
      icon: 'pi pi-arrow-down-left'
    }
  }

  return {
    label: 'Rashod',
    severity: 'danger',
    icon: 'pi pi-arrow-up-right'
  }
}

const getTransactionDescription =
    transaction =>
        transaction.description ||
        transaction.counterparty ||
        `Stavka ${transaction.entryNumber}`

const getCounterparty =
    transaction =>
        transaction.counterparty ||
        'Nije prepoznato'

const formatReference = transaction =>
    transaction.reference ||
    transaction.orderReference ||
    '—'
</script>

<template>
  <section class="preview-panel">
    <div class="preview-header">
      <div>
        <h3>Pregled transakcija</h3>

        <p>
          Proverite pročitane podatke pre
          konačnog uvoza u sistem.
        </p>
      </div>

      <Tag
          :value="
          `${resolvedTransactionCount} stavki`
        "
          severity="info"
      />
    </div>

    <div class="preview-table-wrapper">
      <table class="preview-table">
        <thead>
        <tr>
          <th class="entry-column">
            #
          </th>

          <th>Datum</th>

          <th>Tip</th>

          <th>Druga strana</th>

          <th>Opis</th>

          <th>Referenca</th>

          <th class="amount-column">
            Prihod
          </th>

          <th class="amount-column">
            Rashod
          </th>

          <th class="amount-column">
            Stanje
          </th>
        </tr>
        </thead>

        <tbody>
        <tr
            v-for="transaction in transactions"
            :key="
              `${transaction.sourcePage}-${transaction.entryNumber}`
            "
        >
          <td class="entry-cell">
            {{ transaction.entryNumber }}
          </td>

          <td class="date-cell">
            <strong>
              {{
                formatDate(
                    transaction.transactionDate
                )
              }}
            </strong>

            <small
                v-if="
                  transaction.executionDate &&
                  transaction.executionDate !==
                    transaction.transactionDate
                "
            >
              Izvršeno:
              {{
                formatDate(
                    transaction.executionDate
                )
              }}
            </small>
          </td>

          <td>
            <Tag
                :value="
                  getTransactionType(
                    transaction
                  ).label
                "
                :severity="
                  getTransactionType(
                    transaction
                  ).severity
                "
                :icon="
                  getTransactionType(
                    transaction
                  ).icon
                "
            />
          </td>

          <td class="counterparty-cell">
            <strong>
              {{
                getCounterparty(
                    transaction
                )
              }}
            </strong>

            <small
                v-if="transaction.orderType"
            >
              {{ transaction.orderType }}
              {{ transaction.orderReference }}
            </small>
          </td>

          <td class="description-cell">
              <span>
                {{
                  getTransactionDescription(
                      transaction
                  )
                }}
              </span>
          </td>

          <td class="reference-cell">
            {{ formatReference(transaction) }}
          </td>

          <td
              class="
                amount-cell
                amount-income
              "
          >
              <span
                  v-if="
                  Number(
                    transaction.credit || 0
                  ) > 0
                "
              >
                {{
                  formatAmount(
                      transaction.credit,
                      transaction.currencyCode
                  )
                }}
              </span>

            <span v-else>—</span>
          </td>

          <td
              class="
                amount-cell
                amount-expense
              "
          >
              <span
                  v-if="
                  Number(
                    transaction.debit || 0
                  ) > 0
                "
              >
                {{
                  formatAmount(
                      transaction.debit,
                      transaction.currencyCode
                  )
                }}
              </span>

            <span v-else>—</span>
          </td>

          <td class="amount-cell balance-cell">
            {{
              transaction.balance !== null
                  ? formatAmount(
                      transaction.balance,
                      transaction.currencyCode
                  )
                  : '—'
            }}
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="preview-footer">
      <div class="preview-footer-message">
        <i class="pi pi-info-circle" />

        <span>
          Povezivanje dobavljača,
          kategorija i provera duplikata
          biće dostupni u sledećem koraku.
        </span>
      </div>

      <Button
          label="Uvezi transakcije"
          icon="pi pi-check"
          disabled
      />
    </div>
  </section>
</template>

<style scoped>
.preview-panel {
  width: 100%;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 1rem;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
}

.preview-header,
.preview-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.25rem;
}

.preview-header {
  border-bottom: 1px solid var(--app-border);
}

.preview-header h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 1.05rem;
}

.preview-header p {
  margin: 0.35rem 0 0;
  color: var(--app-text-muted);
  font-size: 0.8rem;
}

.preview-table-wrapper {
  display: block;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-gutter: stable;
  -webkit-overflow-scrolling: touch;
}

.preview-table-wrapper::-webkit-scrollbar {
  height: 12px;
}

.preview-table-wrapper::-webkit-scrollbar-track {
  background: var(--app-surface-soft);
}

.preview-table-wrapper::-webkit-scrollbar-thumb {
  border: 3px solid var(--app-surface-soft);
  border-radius: 999px;
  background: var(--app-border);
}

.preview-table-wrapper::-webkit-scrollbar-thumb:hover {
  background: var(--app-text-muted);
}

.preview-table {
  width: max-content;
  min-width: 1700px;
  border-collapse: collapse;
  table-layout: auto;
}

.preview-table th,
.preview-table td {
  padding: 0.8rem 0.9rem;
  border-bottom: 1px solid var(--app-border);
  text-align: left;
  vertical-align: middle;
  white-space: nowrap;
}

.preview-table th {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--app-surface-soft);
  color: var(--app-text-muted);
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.preview-table tbody tr {
  transition:
      background-color 140ms ease;
}

.preview-table tbody tr:hover {
  background: var(--app-surface-soft);
}

.preview-table tbody tr:last-child td {
  border-bottom: 0;
}

.entry-column,
.entry-cell {
  width: 3rem;
  min-width: 3rem;
  text-align: center !important;
}

.entry-cell {
  color: var(--app-text-muted);
  font-size: 0.75rem;
  font-weight: 700;
}

.date-cell {
  min-width: 8rem;
}

.date-cell,
.counterparty-cell {
  display: table-cell;
}

.date-cell strong,
.counterparty-cell strong {
  display: block;
  color: var(--app-text);
  font-size: 0.78rem;
}

.date-cell small,
.counterparty-cell small {
  display: block;
  margin-top: 0.22rem;
  color: var(--app-text-muted);
  font-size: 0.67rem;
}

.counterparty-cell {
  width: 17rem;
  min-width: 17rem;
  max-width: 17rem;
}

.counterparty-cell strong,
.counterparty-cell small,
.description-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
}

.counterparty-cell strong,
.counterparty-cell small {
  white-space: nowrap;
}

.description-cell {
  width: 25rem;
  min-width: 25rem;
  max-width: 25rem;
  color: var(--app-text);
  font-size: 0.77rem;
  white-space: normal !important;
}

.description-cell span {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.35;
  white-space: normal;
}

.reference-cell {
  width: 14rem;
  min-width: 14rem;
  max-width: 14rem;
  overflow: hidden;
  color: var(--app-text-muted);
  font-family: monospace;
  font-size: 0.7rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.amount-column {
  text-align: right !important;
}

.amount-cell {
  width: 10rem;
  min-width: 10rem;
  text-align: right !important;
  font-size: 0.77rem;
  font-weight: 700;
  white-space: nowrap;
}

.amount-income {
  color: var(--brand-green) !important;
}

.amount-expense {
  color: #c43b3b !important;
}

.balance-cell {
  color: var(--app-text);
}

.preview-footer {
  border-top: 1px solid var(--app-border);
  background: var(--app-surface-soft);
}

.preview-footer-message {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  color: var(--app-text-muted);
  font-size: 0.77rem;
}

.preview-footer-message i {
  color: var(--brand-blue);
}

@media (max-width: 800px) {
  .preview-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .preview-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .preview-footer :deep(.p-button) {
    width: 100%;
  }

  .preview-footer-message {
    align-items: flex-start;
  }

  .preview-table {
    min-width: 1550px;
  }
}
</style>
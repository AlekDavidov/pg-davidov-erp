<script setup>
import {
  computed
} from 'vue'
import { storeToRefs } from 'pinia'

import Button from 'primevue/button'
import Select from 'primevue/select'
import Tag from 'primevue/tag'

import {
  formatAmount,
  formatDate
} from '../../utils/formatters'
import { useBankImportStore } from '../../stores/bankImportStore'

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

const bankImportStore =
    useBankImportStore()

const {
  supplierOptions,
  suppliersLoading
} = storeToRefs(bankImportStore)

const resolvedTransactionCount = computed(() =>
    props.transactionCount ||
    props.transactions.length
)

const matchedTransactionCount = computed(() =>
    props.transactions.filter(
        transaction =>
            transaction.matchStatus === 'MATCHED'
    ).length
)

const unmatchedTransactionCount = computed(() =>
    props.transactions.filter(
        transaction =>
            transaction.matchStatus === 'UNMATCHED'
    ).length
)

const ambiguousTransactionCount = computed(() =>
    props.transactions.filter(
        transaction =>
            transaction.matchStatus === 'AMBIGUOUS'
    ).length
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

const getMatchStatus = transaction => {
  switch (transaction.matchStatus) {
    case 'MATCHED':
      return {
        label: 'Povezano',
        severity: 'success',
        icon: 'pi pi-check-circle'
      }

    case 'AMBIGUOUS':
      return {
        label: 'Više mogućnosti',
        severity: 'danger',
        icon: 'pi pi-exclamation-circle'
      }

    case 'UNMATCHED':
    default:
      return {
        label: 'Nije povezano',
        severity: 'warn',
        icon: 'pi pi-exclamation-triangle'
      }
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

const getCategoryName =
    transaction =>
        transaction.categoryName ||
        'Nije dodeljena'

const formatReference = transaction =>
    transaction.reference ||
    transaction.orderReference ||
    '—'

const handleSupplierChange = (
    transaction,
    supplierId
) => {
  if (!supplierId) {
    bankImportStore
        .clearSupplierSelection(
            transaction
        )

    return
  }

  bankImportStore
      .applySupplierSelection(
          transaction,
          supplierId
      )
}
</script>

<template>
  <section class="preview-panel">
    <div class="preview-header">
      <div>
        <h3>Pregled transakcija</h3>

        <p>
          Proverite automatski prepoznate
          podatke i ručno povežite stavke
          koje nisu prepoznate.
        </p>
      </div>

      <div class="preview-header-statuses">
        <Tag
            :value="
            `${resolvedTransactionCount} stavki`
          "
            severity="info"
        />

        <Tag
            :value="
            `${matchedTransactionCount} povezano`
          "
            severity="success"
        />

        <Tag
            v-if="unmatchedTransactionCount > 0"
            :value="
            `${unmatchedTransactionCount} nepovezano`
          "
            severity="warn"
        />

        <Tag
            v-if="ambiguousTransactionCount > 0"
            :value="
            `${ambiguousTransactionCount} nejasno`
          "
            severity="danger"
        />
      </div>
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

          <th>Dobavljač</th>

          <th>Kategorija</th>

          <th>Status</th>

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
            :class="{
              'row-unmatched':
                transaction.matchStatus ===
                'UNMATCHED',

              'row-ambiguous':
                transaction.matchStatus ===
                'AMBIGUOUS'
            }"
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

          <td class="supplier-cell">
            <Select
                :model-value="
                  transaction.supplierId
                "
                :options="
                  supplierOptions
                "
                option-label="name"
                option-value="id"
                placeholder="Izaberite dobavljača"
                filter
                show-clear
                :loading="
                  suppliersLoading
                "
                class="supplier-select"
                @update:model-value="
                  handleSupplierChange(
                    transaction,
                    $event
                  )
                "
            >
              <template #option="{ option }">
                <div class="supplier-option">
                  <strong>
                    {{ option.name }}
                  </strong>

                  <small>
                    {{ option.code }}
                  </small>
                </div>
              </template>

              <template #value="{ value }">
                  <span
                      v-if="value"
                      class="supplier-selected-value"
                  >
                    {{
                      supplierOptions.find(
                          option =>
                              option.id === value
                      )?.name ||
                      transaction.supplierName ||
                      'Dobavljač'
                    }}
                  </span>

                <span
                    v-else
                    class="supplier-placeholder"
                >
                    Izaberite dobavljača
                  </span>
              </template>
            </Select>
          </td>

          <td
              class="category-cell"
              :class="{
                'empty-match-cell':
                  !transaction.categoryName
              }"
          >
              <span class="match-cell-icon">
                <i
                    :class="
                    transaction.categoryName
                      ? 'pi pi-tag'
                      : 'pi pi-question-circle'
                  "
                />
              </span>

            <div class="match-cell-content">
              <strong>
                {{
                  getCategoryName(
                      transaction
                  )
                }}
              </strong>

              <small
                  v-if="transaction.categoryId"
              >
                Podrazumevana kategorija
              </small>

              <small v-else>
                Kategorija nije određena
              </small>
            </div>
          </td>

          <td class="status-cell">
            <Tag
                :value="
                  getMatchStatus(
                    transaction
                  ).label
                "
                :severity="
                  getMatchStatus(
                    transaction
                  ).severity
                "
                :icon="
                  getMatchStatus(
                    transaction
                  ).icon
                "
            />
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

            <span v-else>
                —
              </span>
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

            <span v-else>
                —
              </span>
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
          Izbor dobavljača automatski
          postavlja njegovu podrazumevanu
          kategoriju.
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

.preview-header-statuses {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 0.45rem;
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
  min-width: 2400px;
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

.preview-table tbody tr.row-unmatched {
  background:
      color-mix(
          in srgb,
          #f4b740 4%,
          transparent
      );
}

.preview-table tbody tr.row-ambiguous {
  background:
      color-mix(
          in srgb,
          #c43b3b 5%,
          transparent
      );
}

.preview-table tbody tr.row-unmatched:hover,
.preview-table tbody tr.row-ambiguous:hover {
  background: var(--app-surface-soft);
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

.supplier-cell {
  width: 18rem;
  min-width: 18rem;
  max-width: 18rem;
}

.supplier-select {
  width: 100%;
}

.supplier-select :deep(.p-select-label) {
  overflow: hidden;
  font-size: 0.76rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.supplier-selected-value {
  color: var(--app-text);
  font-weight: 600;
}

.supplier-placeholder {
  color: var(--app-text-muted);
}

.supplier-option {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.supplier-option strong {
  color: var(--app-text);
  font-size: 0.8rem;
}

.supplier-option small {
  color: var(--app-text-muted);
  font-size: 0.68rem;
}

.category-cell {
  display: flex;
  width: 15rem;
  min-width: 15rem;
  max-width: 15rem;
  align-items: center;
  gap: 0.55rem;
}

.match-cell-icon {
  display: inline-flex;
  width: 2rem;
  height: 2rem;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 0.65rem;
  background: var(--brand-green-soft);
  color: var(--brand-green);
}

.empty-match-cell .match-cell-icon {
  background:
      color-mix(
          in srgb,
          #f4b740 14%,
          transparent
      );
  color: #a66a00;
}

.match-cell-content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.18rem;
}

.match-cell-content strong {
  overflow: hidden;
  color: var(--app-text);
  font-size: 0.77rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.match-cell-content small {
  overflow: hidden;
  color: var(--app-text-muted);
  font-size: 0.65rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-match-cell .match-cell-content strong {
  color: var(--app-text-muted);
  font-weight: 500;
}

.status-cell {
  min-width: 10rem;
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

  .preview-header-statuses {
    justify-content: flex-start;
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
    min-width: 2250px;
  }
}
</style>
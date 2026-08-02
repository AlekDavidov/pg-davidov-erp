<script setup>
import {
  computed
} from 'vue'

import Button from 'primevue/button'

import {
  formatAmount,
  formatDate
} from '../../utils/formatters'

const props = defineProps({
  preview: {
    type: Object,
    required: true
  }
})

const emit = defineEmits([
  'reset'
])

const transactions = computed(() =>
    props.preview?.transactions || []
)

const totalIncome = computed(() =>
    transactions.value.reduce(
        (total, transaction) =>
            total +
            Number(transaction.credit || 0),
        0
    )
)

const totalExpense = computed(() =>
    transactions.value.reduce(
        (total, transaction) =>
            total +
            Number(transaction.debit || 0),
        0
    )
)

const currencyCode = computed(() =>
    props.preview?.currencyCode || 'RSD'
)

const bankName = computed(() =>
    props.preview?.bankName || '—'
)

const statementId = computed(() =>
    props.preview?.statementId || '—'
)

const accountNumber = computed(() =>
    props.preview?.accountNumber || '—'
)

const transactionCount = computed(() =>
    props.preview?.transactionCount ??
    transactions.value.length
)

const periodFrom = computed(() =>
    props.preview?.periodFrom || null
)

const periodTo = computed(() =>
    props.preview?.periodTo || null
)

const resetStatement = () => {
  emit('reset')
}
</script>

<template>
  <section class="statement-summary">
    <div class="statement-summary-main">
      <div class="bank-logo-placeholder">
        <i class="pi pi-building-columns" />
      </div>

      <div class="statement-identity">
        <span>Prepoznata banka</span>

        <strong>
          {{ bankName }}
        </strong>

        <small>
          {{ statementId }}
        </small>
      </div>
    </div>

    <div class="statement-summary-grid">
      <div class="summary-item">
        <span>Broj računa</span>

        <strong>
          {{ accountNumber }}
        </strong>
      </div>

      <div class="summary-item">
        <span>Period izvoda</span>

        <strong>
          {{ formatDate(periodFrom) }}
          –
          {{ formatDate(periodTo) }}
        </strong>
      </div>

      <div class="summary-item">
        <span>Broj transakcija</span>

        <strong>
          {{ transactionCount }}
        </strong>
      </div>

      <div class="summary-item">
        <span>Ukupni prihodi</span>

        <strong class="amount-income">
          {{
            formatAmount(
                totalIncome,
                currencyCode
            )
          }}
        </strong>
      </div>

      <div class="summary-item">
        <span>Ukupni rashodi</span>

        <strong class="amount-expense">
          {{
            formatAmount(
                totalExpense,
                currencyCode
            )
          }}
        </strong>
      </div>
    </div>

    <div class="statement-summary-actions">
      <Button
          label="Izaberi drugi izvod"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          @click="resetStatement"
      />

      <Button
          label="Uvezi transakcije"
          icon="pi pi-check"
          disabled
      />
    </div>
  </section>
</template>

<style scoped>
.statement-summary {
  display: grid;
  grid-template-columns:
    minmax(15rem, 1fr)
    minmax(0, 3fr)
    auto;
  align-items: center;
  gap: 1.5rem;
  padding: 1.25rem;
  border: 1px solid var(--app-border);
  border-radius: 1rem;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
}

.statement-summary-main {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 0.85rem;
}

.bank-logo-placeholder {
  display: inline-flex;
  width: 3.5rem;
  height: 3.5rem;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 1rem;
  background: var(--brand-blue-soft);
  color: var(--brand-blue);
  font-size: 1.4rem;
}

.statement-identity {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.15rem;
}

.statement-identity span,
.summary-item span {
  color: var(--app-text-muted);
  font-size: 0.72rem;
}

.statement-identity strong {
  overflow: hidden;
  color: var(--app-text);
  font-size: 1rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.statement-identity small {
  overflow: hidden;
  color: var(--app-text-muted);
  font-size: 0.7rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.statement-summary-grid {
  display: grid;
  min-width: 0;
  grid-template-columns:
    repeat(5, minmax(0, 1fr));
  gap: 1rem;
}

.summary-item {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.25rem;
}

.summary-item strong {
  overflow: hidden;
  color: var(--app-text);
  font-size: 0.8rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.amount-income {
  color: var(--brand-green) !important;
}

.amount-expense {
  color: #c43b3b !important;
}

.statement-summary-actions {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

@media (max-width: 1300px) {
  .statement-summary {
    grid-template-columns:
      minmax(15rem, 1fr)
      minmax(0, 2fr);
  }

  .statement-summary-actions {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }

  .statement-summary-grid {
    grid-template-columns:
      repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 800px) {
  .statement-summary {
    grid-template-columns: 1fr;
  }

  .statement-summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .statement-summary-actions {
    grid-column: auto;
    align-items: stretch;
    flex-direction: column;
  }

  .statement-summary-actions :deep(.p-button) {
    width: 100%;
  }
}

@media (max-width: 520px) {
  .statement-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
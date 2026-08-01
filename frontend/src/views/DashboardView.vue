<script setup>
import {
  computed,
  onMounted
} from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'

import Button from 'primevue/button'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import Select from 'primevue/select'
import Tag from 'primevue/tag'

import { useDashboardStore } from '../stores/dashboardStore'
import {
  formatAmount,
  formatDate,
  formatInvoiceStatus,
  getInvoiceStatusSeverity
} from '../utils/formatters'

const router = useRouter()
const dashboardStore = useDashboardStore()

const {
  income,
  expense,
  cashFlow,
  expenseCategories,
  recentTransactions,
  dueInvoices,
  bankAccounts,
  selectedMonth,
  selectedYear,
  loading,
  error
} = storeToRefs(dashboardStore)

const monthOptions = [
  {
    label: 'Januar',
    value: 1
  },
  {
    label: 'Februar',
    value: 2
  },
  {
    label: 'Mart',
    value: 3
  },
  {
    label: 'April',
    value: 4
  },
  {
    label: 'Maj',
    value: 5
  },
  {
    label: 'Jun',
    value: 6
  },
  {
    label: 'Jul',
    value: 7
  },
  {
    label: 'Avgust',
    value: 8
  },
  {
    label: 'Septembar',
    value: 9
  },
  {
    label: 'Oktobar',
    value: 10
  },
  {
    label: 'Novembar',
    value: 11
  },
  {
    label: 'Decembar',
    value: 12
  }
]

const currentYear = new Date().getFullYear()

const yearOptions = Array.from(
    {
      length: 8
    },
    (_, index) => {
      const year =
          currentYear - index

      return {
        label: String(year),
        value: year
      }
    }
)

const monthFormatter = new Intl.DateTimeFormat(
    'sr-RS',
    {
      month: 'short'
    }
)

const selectedPeriodLabel = computed(() =>
    new Intl.DateTimeFormat(
        'sr-RS',
        {
          month: 'long',
          year: 'numeric'
        }
    ).format(
        new Date(
            selectedYear.value,
            selectedMonth.value - 1,
            1
        )
    )
)

const formatPercentage = value => {
  const number = Number(value || 0)

  return new Intl.NumberFormat(
      'sr-RS',
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
      }
  ).format(
      Math.abs(number)
  )
}

const getKpiTrendClass = value => {
  const number = Number(value || 0)

  if (number > 0) {
    return 'trend-positive'
  }

  if (number < 0) {
    return 'trend-negative'
  }

  return 'trend-neutral'
}

const getKpiTrendIcon = value => {
  const number = Number(value || 0)

  if (number > 0) {
    return 'pi pi-arrow-up-right'
  }

  if (number < 0) {
    return 'pi pi-arrow-down-right'
  }

  return 'pi pi-minus'
}

const formatMonth = period => {
  if (!period) {
    return '—'
  }

  const [year, month] = period
      .split('-')
      .map(Number)

  return monthFormatter.format(
      new Date(
          year,
          month - 1,
          1
      )
  )
}

const chartWidth = 720
const chartHeight = 220
const chartPadding = 20

const maximumCashFlowValue = computed(() => {
  const values = cashFlow.value.flatMap(
      point => [
        Number(point.income || 0),
        Number(point.expense || 0)
      ]
  )

  return Math.max(
      ...values,
      1
  )
})

const createChartPoints = field => {
  if (cashFlow.value.length === 0) {
    return ''
  }

  const usableWidth =
      chartWidth -
      chartPadding * 2

  const usableHeight =
      chartHeight -
      chartPadding * 2

  const step =
      cashFlow.value.length > 1
          ? usableWidth /
          (cashFlow.value.length - 1)
          : 0

  return cashFlow.value
      .map((point, index) => {
        const value =
            Number(point[field] || 0)

        const x =
            chartPadding +
            index * step

        const y =
            chartHeight -
            chartPadding -
            (
                value /
                maximumCashFlowValue.value
            ) *
            usableHeight

        return `${x},${y}`
      })
      .join(' ')
}

const incomeChartPoints = computed(() =>
    createChartPoints('income')
)

const expenseChartPoints = computed(() =>
    createChartPoints('expense')
)

const categoryColors = [
  '#2d73c8',
  '#5b8c2a',
  '#d98a19',
  '#c43b3b',
  '#7c5db5',
  '#3b8b8b',
  '#e8b8d0',
  '#68727d'
]

const expenseChartStyle = computed(() => {
  if (
      expenseCategories.value.length === 0
  ) {
    return {
      background:
          'var(--app-surface-soft)'
    }
  }

  let accumulatedPercentage = 0

  const segments =
      expenseCategories.value.map(
          (category, index) => {
            const percentage =
                Number(
                    category.percentage || 0
                )

            const start =
                accumulatedPercentage

            accumulatedPercentage +=
                percentage

            return `${
                categoryColors[
                index %
                categoryColors.length
                    ]
            } ${start}% ${accumulatedPercentage}%`
          }
      )

  return {
    background:
        `conic-gradient(${segments.join(', ')})`
  }
})

const totalCategoryExpense = computed(() =>
    expenseCategories.value.reduce(
        (total, category) =>
            total +
            Number(category.amount || 0),
        0
    )
)

const getTransactionAmount = transaction => {
  const credit =
      Number(transaction.credit || 0)

  if (credit > 0) {
    return {
      value: credit,
      type: 'income'
    }
  }

  return {
    value:
        Number(transaction.debit || 0),
    type: 'expense'
  }
}

const getTransactionDescription = transaction =>
    transaction.description ||
    transaction.counterparty ||
    transaction.transactionCode

const getInvoiceStatusLabel = status =>
    formatInvoiceStatus(status)

const openTransactions = () => {
  router.push('/transactions')
}

const openInvoices = () => {
  router.push('/invoices')
}

const refreshDashboard = async () => {
  await dashboardStore.fetchDashboard({
    month: selectedMonth.value,
    year: selectedYear.value
  })
}

onMounted(refreshDashboard)
</script>

<template>
  <div class="dashboard-view">
    <div class="dashboard-header">
      <div class="dashboard-heading">
        <span class="dashboard-eyebrow">
          Pregled poslovanja
        </span>

        <p>
          Finansijski pregled za
          {{ selectedPeriodLabel }}.
        </p>
      </div>

      <div class="period-controls">
        <div class="period-field">
          <label for="dashboard-month">
            Mesec
          </label>

          <Select
              id="dashboard-month"
              v-model="selectedMonth"
              :options="monthOptions"
              option-label="label"
              option-value="value"
              :disabled="loading"
          />
        </div>

        <div class="period-field year-field">
          <label for="dashboard-year">
            Godina
          </label>

          <Select
              id="dashboard-year"
              v-model="selectedYear"
              :options="yearOptions"
              option-label="label"
              option-value="value"
              :disabled="loading"
          />
        </div>

        <Button
            label="Primeni"
            icon="pi pi-filter"
            :loading="loading"
            @click="refreshDashboard"
        />

        <Button
            v-tooltip.top="'Osveži podatke'"
            icon="pi pi-refresh"
            severity="secondary"
            outlined
            aria-label="Osveži podatke"
            :loading="loading"
            @click="refreshDashboard"
        />
      </div>
    </div>

    <Message
        v-if="error"
        severity="error"
        closable
        @close="dashboardStore.clearError()"
    >
      {{ error }}
    </Message>

    <div
        v-if="loading && !cashFlow.length"
        class="dashboard-loading"
    >
      <ProgressSpinner />

      <span>
        Učitavanje pregleda poslovanja...
      </span>
    </div>

    <template v-else>
      <div class="kpi-grid">
        <article class="kpi-card income-card">
          <div class="kpi-card-header">
            <div class="kpi-icon income-icon">
              <i class="pi pi-arrow-down-left" />
            </div>

            <span>Prihodi</span>
          </div>

          <strong class="kpi-value">
            {{
              formatAmount(
                  income.currentAmount,
                  'RSD'
              )
            }}
          </strong>

          <div
              class="kpi-trend"
              :class="
              getKpiTrendClass(
                income.percentageChange
              )
            "
          >
            <i
                :class="
                getKpiTrendIcon(
                  income.percentageChange
                )
              "
            />

            <span>
              {{
                formatPercentage(
                    income.percentageChange
                )
              }}%
              u odnosu na prethodni mesec
            </span>
          </div>

          <div class="kpi-footer">
            Prethodni mesec:

            <strong>
              {{
                formatAmount(
                    income.previousAmount,
                    'RSD'
                )
              }}
            </strong>
          </div>
        </article>

        <article class="kpi-card expense-card">
          <div class="kpi-card-header">
            <div class="kpi-icon expense-icon">
              <i class="pi pi-arrow-up-right" />
            </div>

            <span>Rashodi</span>
          </div>

          <strong class="kpi-value">
            {{
              formatAmount(
                  expense.currentAmount,
                  'RSD'
              )
            }}
          </strong>

          <div
              class="kpi-trend"
              :class="
              getKpiTrendClass(
                expense.percentageChange
              )
            "
          >
            <i
                :class="
                getKpiTrendIcon(
                  expense.percentageChange
                )
              "
            />

            <span>
              {{
                formatPercentage(
                    expense.percentageChange
                )
              }}%
              u odnosu na prethodni mesec
            </span>
          </div>

          <div class="kpi-footer">
            Prethodni mesec:

            <strong>
              {{
                formatAmount(
                    expense.previousAmount,
                    'RSD'
                )
              }}
            </strong>
          </div>
        </article>
      </div>

      <div class="dashboard-grid chart-grid">
        <article class="dashboard-panel cash-flow-panel">
          <div class="panel-header">
            <div>
              <h3>Cash Flow</h3>

              <p>
                Prihodi i rashodi u poslednjih
                12 meseci, zaključno sa izabranim
                periodom.
              </p>
            </div>

            <div class="chart-legend">
              <span>
                <i class="legend-dot income-dot" />
                Prihodi
              </span>

              <span>
                <i class="legend-dot expense-dot" />
                Rashodi
              </span>
            </div>
          </div>

          <div
              v-if="cashFlow.length"
              class="cash-flow-chart"
          >
            <svg
                :viewBox="
                `0 0 ${chartWidth} ${chartHeight}`
              "
                role="img"
                aria-label="Cash Flow grafikon"
            >
              <line
                  v-for="index in 5"
                  :key="index"
                  :x1="chartPadding"
                  :x2="
                  chartWidth -
                  chartPadding
                "
                  :y1="
                  chartPadding +
                  ((index - 1) *
                    (chartHeight -
                      chartPadding * 2)) /
                    4
                "
                  :y2="
                  chartPadding +
                  ((index - 1) *
                    (chartHeight -
                      chartPadding * 2)) /
                    4
                "
                  class="chart-grid-line"
              />

              <polyline
                  :points="incomeChartPoints"
                  class="chart-line income-line"
              />

              <polyline
                  :points="expenseChartPoints"
                  class="chart-line expense-line"
              />
            </svg>

            <div class="chart-months">
              <span
                  v-for="point in cashFlow"
                  :key="point.period"
              >
                {{ formatMonth(point.period) }}
              </span>
            </div>
          </div>

          <div
              v-else
              class="panel-empty"
          >
            Nema podataka za Cash Flow.
          </div>
        </article>

        <article class="dashboard-panel category-panel">
          <div class="panel-header">
            <div>
              <h3>Rashodi po kategorijama</h3>

              <p>
                Raspodela rashoda za
                {{ selectedPeriodLabel }}.
              </p>
            </div>
          </div>

          <div
              v-if="expenseCategories.length"
              class="category-content"
          >
            <div
                class="donut-chart"
                :style="expenseChartStyle"
            >
              <div class="donut-center">
                <span>Ukupno</span>

                <strong>
                  {{
                    formatAmount(
                        totalCategoryExpense,
                        'RSD'
                    )
                  }}
                </strong>
              </div>
            </div>

            <div class="category-list">
              <div
                  v-for="(
                  category,
                  index
                ) in expenseCategories"
                  :key="category.categoryCode"
                  class="category-row"
              >
                <div class="category-name">
                  <span
                      class="category-color"
                      :style="{
                      background:
                        categoryColors[
                          index %
                          categoryColors.length
                        ]
                    }"
                  />

                  <span>
                    {{ category.categoryName }}
                  </span>
                </div>

                <div class="category-value">
                  <strong>
                    {{
                      formatAmount(
                          category.amount,
                          'RSD'
                      )
                    }}
                  </strong>

                  <small>
                    {{
                      formatPercentage(
                          category.percentage
                      )
                    }}%
                  </small>
                </div>
              </div>
            </div>
          </div>

          <div
              v-else
              class="panel-empty"
          >
            Nema evidentiranih rashoda po
            kategorijama.
          </div>
        </article>
      </div>

      <div class="dashboard-grid detail-grid">
        <article class="dashboard-panel">
          <div class="panel-header">
            <div>
              <h3>Poslednje transakcije</h3>

              <p>
                Poslednjih deset transakcija do
                kraja izabranog perioda.
              </p>
            </div>

            <Button
                label="Sve transakcije"
                icon="pi pi-arrow-right"
                icon-pos="right"
                text
                @click="openTransactions"
            />
          </div>

          <div
              v-if="recentTransactions.length"
              class="dashboard-list"
          >
            <button
                v-for="transaction in recentTransactions"
                :key="transaction.id"
                type="button"
                class="dashboard-list-row"
                @click="openTransactions"
            >
              <span
                  class="list-icon"
                  :class="
                  getTransactionAmount(
                    transaction
                  ).type === 'income'
                    ? 'list-icon-income'
                    : 'list-icon-expense'
                "
              >
                <i
                    :class="
                    getTransactionAmount(
                      transaction
                    ).type === 'income'
                      ? 'pi pi-arrow-down-left'
                      : 'pi pi-arrow-up-right'
                  "
                />
              </span>

              <span class="list-main">
                <strong>
                  {{
                    getTransactionDescription(
                        transaction
                    )
                  }}
                </strong>

                <small>
                  {{
                    formatDate(
                        transaction.transactionDate
                    )
                  }}
                  ·
                  {{ transaction.transactionCode }}
                </small>
              </span>

              <strong
                  class="transaction-amount"
                  :class="
                  getTransactionAmount(
                    transaction
                  ).type === 'income'
                    ? 'amount-income'
                    : 'amount-expense'
                "
              >
                {{
                  getTransactionAmount(
                      transaction
                  ).type === 'income'
                      ? '+'
                      : '-'
                }}

                {{
                  formatAmount(
                      getTransactionAmount(
                          transaction
                      ).value,
                      transaction.currencyCode
                  )
                }}
              </strong>
            </button>
          </div>

          <div
              v-else
              class="panel-empty"
          >
            Nema evidentiranih transakcija.
          </div>
        </article>

        <article class="dashboard-panel">
          <div class="panel-header">
            <div>
              <h3>Fakture za plaćanje</h3>

              <p>
                Otvorene, delimično plaćene i
                dospele fakture.
              </p>
            </div>

            <Button
                label="Sve fakture"
                icon="pi pi-arrow-right"
                icon-pos="right"
                text
                @click="openInvoices"
            />
          </div>

          <div
              v-if="dueInvoices.length"
              class="dashboard-list"
          >
            <button
                v-for="invoice in dueInvoices"
                :key="invoice.id"
                type="button"
                class="dashboard-list-row invoice-row"
                @click="openInvoices"
            >
              <span class="list-icon invoice-icon">
                <i class="pi pi-file" />
              </span>

              <span class="list-main">
                <strong>
                  {{ invoice.invoiceNumber }}
                </strong>

                <small>
                  {{ invoice.supplierName }}
                  · dospeće
                  {{ formatDate(invoice.dueDate) }}
                </small>
              </span>

              <span class="invoice-summary">
                <strong>
                  {{
                    formatAmount(
                        invoice.remainingAmount,
                        invoice.currencyCode
                    )
                  }}
                </strong>

                <Tag
                    :value="
                    getInvoiceStatusLabel(
                      invoice.status
                    )
                  "
                    :severity="
                    getInvoiceStatusSeverity(
                      invoice.status
                    )
                  "
                />
              </span>
            </button>
          </div>

          <div
              v-else
              class="panel-empty panel-success"
          >
            <i class="pi pi-check-circle" />

            <span>
              Nema faktura koje čekaju plaćanje.
            </span>
          </div>
        </article>
      </div>

      <article class="dashboard-panel bank-panel">
        <div class="panel-header">
          <div>
            <h3>Bankovni računi</h3>

            <p>
              Evidentirano stanje zaključno sa
              krajem izabranog perioda.
            </p>
          </div>
        </div>

        <div
            v-if="bankAccounts.length"
            class="bank-account-grid"
        >
          <div
              v-for="account in bankAccounts"
              :key="account.id"
              class="bank-account-card"
          >
            <div class="bank-account-header">
              <span class="bank-icon">
                <i class="pi pi-building-columns" />
              </span>

              <div>
                <strong>
                  {{ account.bankName }}
                </strong>

                <small>
                  {{ account.accountNumber }}
                </small>
              </div>
            </div>

            <div class="bank-account-balance">
              <span>Stanje</span>

              <strong
                  :class="{
                  'negative-balance':
                    Number(account.balance) < 0
                }"
              >
                {{
                  formatAmount(
                      account.balance,
                      account.currencyCode
                  )
                }}
              </strong>
            </div>
          </div>
        </div>

        <div
            v-else
            class="panel-empty"
        >
          Nema aktivnih bankovnih računa.
        </div>
      </article>
    </template>
  </div>
</template>

<style scoped>
.dashboard-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.dashboard-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
}

.dashboard-heading {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.dashboard-eyebrow {
  color: var(--brand-blue);
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.dashboard-header p {
  margin: 0;
  color: var(--app-text-muted);
  font-size: 1rem;
  text-transform: capitalize;
}

.period-controls {
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  gap: 0.75rem;
}

.period-field {
  display: flex;
  min-width: 11rem;
  flex-direction: column;
  gap: 0.4rem;
}

.year-field {
  min-width: 7rem;
}

.period-field label {
  color: var(--app-text-muted);
  font-size: 0.75rem;
  font-weight: 600;
}

.period-field :deep(.p-select) {
  width: 100%;
  min-height: 2.65rem;
}

.dashboard-loading {
  display: flex;
  min-height: 24rem;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 1rem;
  color: var(--app-text-muted);
}

.kpi-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 1.25rem;
}

.kpi-card,
.dashboard-panel {
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
}

.kpi-card {
  position: relative;
  display: flex;
  min-height: 12.5rem;
  flex-direction: column;
  gap: 1rem;
  overflow: hidden;
  padding: 1.5rem;
  border-radius: 1rem;
}

.kpi-card::after {
  position: absolute;
  right: -2rem;
  bottom: -4rem;
  width: 11rem;
  height: 11rem;
  border-radius: 50%;
  content: '';
  opacity: 0.12;
}

.income-card::after {
  background: var(--brand-green);
}

.expense-card::after {
  background: #c43b3b;
}

.kpi-card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--app-text-muted);
  font-weight: 600;
}

.kpi-icon,
.list-icon,
.bank-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.kpi-icon {
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 0.85rem;
  font-size: 1.1rem;
}

.income-icon {
  background: var(--brand-green-soft);
  color: var(--brand-green);
}

.expense-icon {
  background: #fdecec;
  color: #c43b3b;
}

.app-dark .expense-icon {
  background: #3d2023;
}

.kpi-value {
  color: var(--app-text);
  font-size: clamp(1.8rem, 3vw, 2.65rem);
  line-height: 1;
}

.kpi-trend {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.82rem;
  font-weight: 600;
}

.trend-positive {
  color: var(--brand-green);
}

.trend-negative {
  color: #c43b3b;
}

.trend-neutral {
  color: var(--app-text-muted);
}

.kpi-footer {
  margin-top: auto;
  color: var(--app-text-muted);
  font-size: 0.82rem;
}

.kpi-footer strong {
  color: var(--app-text);
}

.dashboard-grid {
  display: grid;
  gap: 1.25rem;
}

.chart-grid {
  grid-template-columns:
    minmax(0, 1.65fr)
    minmax(20rem, 1fr);
}

.detail-grid {
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
}

.dashboard-panel {
  min-width: 0;
  overflow: hidden;
  padding: 1.35rem;
  border-radius: 1rem;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.panel-header h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 1.05rem;
}

.panel-header p {
  margin: 0.35rem 0 0;
  color: var(--app-text-muted);
  font-size: 0.82rem;
}

.chart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 0.85rem;
  color: var(--app-text-muted);
  font-size: 0.75rem;
}

.chart-legend span {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}

.legend-dot,
.category-color {
  display: inline-block;
  border-radius: 999px;
}

.legend-dot {
  width: 0.55rem;
  height: 0.55rem;
}

.income-dot {
  background: var(--brand-green);
}

.expense-dot {
  background: #c43b3b;
}

.cash-flow-chart svg {
  display: block;
  width: 100%;
  min-height: 14rem;
  overflow: visible;
}

.chart-grid-line {
  stroke: var(--app-border);
  stroke-width: 1;
}

.chart-line {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 4;
}

.income-line {
  stroke: var(--brand-green);
}

.expense-line {
  stroke: #c43b3b;
}

.chart-months {
  display: grid;
  grid-template-columns:
    repeat(12, minmax(0, 1fr));
  gap: 0.25rem;
  margin-top: 0.4rem;
  color: var(--app-text-muted);
  font-size: 0.68rem;
  text-align: center;
  text-transform: capitalize;
}

.category-content {
  display: grid;
  grid-template-columns:
    12rem minmax(0, 1fr);
  align-items: center;
  gap: 1.5rem;
}

.donut-chart {
  display: flex;
  width: 12rem;
  height: 12rem;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.donut-center {
  display: flex;
  width: 7.5rem;
  height: 7.5rem;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 0.3rem;
  border-radius: 50%;
  background: var(--app-surface);
  text-align: center;
}

.donut-center span {
  color: var(--app-text-muted);
  font-size: 0.72rem;
}

.donut-center strong {
  color: var(--app-text);
  font-size: 0.95rem;
}

.category-list {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.75rem;
}

.category-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.category-name {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 0.5rem;
  color: var(--app-text);
  font-size: 0.82rem;
}

.category-color {
  width: 0.65rem;
  height: 0.65rem;
  flex: 0 0 auto;
}

.category-value {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 0.1rem;
  white-space: nowrap;
}

.category-value strong {
  color: var(--app-text);
  font-size: 0.8rem;
}

.category-value small {
  color: var(--app-text-muted);
}

.dashboard-list {
  display: flex;
  flex-direction: column;
}

.dashboard-list-row {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 0;
  border: 0;
  border-bottom: 1px solid var(--app-border);
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.dashboard-list-row:first-child {
  padding-top: 0;
}

.dashboard-list-row:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.dashboard-list-row:hover .list-main strong {
  color: var(--brand-blue);
}

.list-icon,
.bank-icon {
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 0.75rem;
}

.list-icon-income {
  background: var(--brand-green-soft);
  color: var(--brand-green);
}

.list-icon-expense {
  background: #fdecec;
  color: #c43b3b;
}

.invoice-icon {
  background: var(--brand-blue-soft);
  color: var(--brand-blue);
}

.list-main {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 0.25rem;
}

.list-main strong,
.list-main small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-main strong {
  color: var(--app-text);
  font-size: 0.86rem;
}

.list-main small {
  color: var(--app-text-muted);
  font-size: 0.74rem;
}

.transaction-amount {
  flex: 0 0 auto;
  font-size: 0.82rem;
  white-space: nowrap;
}

.amount-income {
  color: var(--brand-green);
}

.amount-expense,
.negative-balance {
  color: #c43b3b !important;
}

.invoice-summary {
  display: flex;
  flex: 0 0 auto;
  align-items: flex-end;
  flex-direction: column;
  gap: 0.35rem;
  white-space: nowrap;
}

.invoice-summary > strong {
  color: var(--app-text);
  font-size: 0.82rem;
}

.bank-account-grid {
  display: grid;
  grid-template-columns:
    repeat(
      auto-fit,
      minmax(16rem, 1fr)
    );
  gap: 1rem;
}

.bank-account-card {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 1rem;
  border: 1px solid var(--app-border);
  border-radius: 0.85rem;
  background: var(--app-surface-soft);
}

.bank-account-header {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 0.75rem;
}

.bank-icon {
  background: var(--brand-gold-soft);
  color: var(--brand-gold);
}

.bank-account-header > div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.2rem;
}

.bank-account-header strong,
.bank-account-header small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-account-header strong {
  color: var(--app-text);
}

.bank-account-header small {
  color: var(--app-text-muted);
  font-size: 0.72rem;
}

.bank-account-balance {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 0.75rem;
}

.bank-account-balance span {
  color: var(--app-text-muted);
  font-size: 0.75rem;
}

.bank-account-balance strong {
  color: var(--app-text);
  font-size: 1.15rem;
}

.panel-empty {
  display: flex;
  min-height: 10rem;
  align-items: center;
  justify-content: center;
  color: var(--app-text-muted);
  text-align: center;
}

.panel-success {
  flex-direction: column;
  gap: 0.75rem;
  color: var(--brand-green);
}

.panel-success i {
  font-size: 2rem;
}

@media (max-width: 1200px) {
  .dashboard-header {
    align-items: stretch;
    flex-direction: column;
  }

  .period-controls {
    justify-content: flex-start;
  }

  .chart-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .category-content {
    grid-template-columns:
      auto minmax(0, 1fr);
  }
}

@media (max-width: 760px) {
  .period-controls {
    display: grid;
    grid-template-columns:
      minmax(0, 1fr)
      minmax(7rem, 0.5fr);
  }

  .period-field,
  .year-field {
    min-width: 0;
  }

  .period-controls > .p-button {
    width: 100%;
  }

  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .category-content {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .category-list {
    width: 100%;
  }

  .chart-legend {
    display: none;
  }

  .chart-months span:nth-child(even) {
    display: none;
  }

  .invoice-row {
    align-items: flex-start;
  }

  .invoice-summary {
    align-items: flex-end;
  }
}

@media (max-width: 520px) {
  .period-controls {
    grid-template-columns: 1fr;
  }

  .dashboard-panel,
  .kpi-card {
    padding: 1rem;
  }

  .panel-header {
    align-items: stretch;
    flex-direction: column;
  }

  .dashboard-list-row {
    flex-wrap: wrap;
  }

  .transaction-amount,
  .invoice-summary {
    width: 100%;
    align-items: flex-end;
    margin-left: 3.15rem;
  }

  .donut-chart {
    width: 10rem;
    height: 10rem;
  }

  .donut-center {
    width: 6.3rem;
    height: 6.3rem;
  }
}
</style>
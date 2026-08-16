<script setup>
import { computed, onMounted, ref } from 'vue'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DatePicker from 'primevue/datepicker'
import Message from 'primevue/message'
import Select from 'primevue/select'

import { supplierApi } from '../api/supplierApi'
import { supplierLedgerApi } from '../api/supplierLedgerApi'
import { formatAmount } from '../utils/formatters'

const TAB_OUTSTANDING = 'outstanding'
const TAB_LEDGER = 'ledger'

const activeTab = ref(TAB_OUTSTANDING)

const suppliers = ref([])
const selectedSupplierId = ref(null)

const periodFrom = ref(
    new Date(new Date().getFullYear(), 0, 1)
)

const periodTo = ref(
    new Date(new Date().getFullYear(), 11, 31)
)

const onlyOutstanding = ref(true)

const outstandingBalances = ref(null)
const ledger = ref(null)

const suppliersLoading = ref(false)
const outstandingLoading = ref(false)
const outstandingExportLoading = ref(false)
const ledgerLoading = ref(false)
const exportLoading = ref(false)

const error = ref(null)

const canLoadPeriod = computed(() =>
    Boolean(
        periodFrom.value &&
        periodTo.value
    )
)

const canLoadLedger = computed(() =>
    Boolean(
        selectedSupplierId.value &&
        canLoadPeriod.value
    )
)

const supplierOptions = computed(() =>
    suppliers.value.map(supplier => ({
      label: supplier.name,
      value: supplier.id
    }))
)

const outstandingRows = computed(() =>
    outstandingBalances.value?.suppliers || []
)

const formatDateForApi = date => {
  if (!date) {
    return null
  }

  const year = date.getFullYear()

  const month = String(
      date.getMonth() + 1
  ).padStart(2, '0')

  const day = String(
      date.getDate()
  ).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const formatDate = value => {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat(
      'sr-RS'
  ).format(
      new Date(`${value}T00:00:00`)
  )
}

const formatLedgerAmount = value => {
  if (
      value === null ||
      value === undefined
  ) {
    return '—'
  }

  return formatAmount(
      value,
      ledger.value?.currencyCode || 'RSD'
  )
}

const formatOutstandingAmount = (
    value,
    currencyCode
) => {
  if (
      value === null ||
      value === undefined
  ) {
    return '—'
  }

  return formatAmount(
      value,
      currencyCode || 'RSD'
  )
}

const validatePeriod = () => {
  if (!canLoadPeriod.value) {
    error.value =
        'Izaberite period.'

    return false
  }

  if (
      periodFrom.value >
      periodTo.value
  ) {
    error.value =
        'Početni datum ne može biti posle krajnjeg datuma.'

    return false
  }

  return true
}

const loadSuppliers = async () => {
  suppliersLoading.value = true
  error.value = null

  try {
    const response =
        await supplierApi.findAll({
          page: 0,
          size: 500,
          sortBy: 'name',
          direction: 'asc'
        })

    suppliers.value =
        response.items || []
  } catch (loadError) {
    suppliers.value = []

    error.value =
        loadError.message ||
        'Dobavljači nisu mogli da budu učitani.'
  } finally {
    suppliersLoading.value = false
  }
}

const loadOutstandingBalances = async () => {
  error.value = null
  outstandingBalances.value = null

  if (!validatePeriod()) {
    return
  }

  outstandingLoading.value = true

  try {
    outstandingBalances.value =
        await supplierLedgerApi
            .getOutstandingBalances(
                formatDateForApi(
                    periodFrom.value
                ),
                formatDateForApi(
                    periodTo.value
                ),
                onlyOutstanding.value
            )
  } catch (loadError) {
    error.value =
        loadError.response?.data?.message ||
        loadError.response?.data?.detail ||
        loadError.message ||
        'Pregled otvorenih obaveza nije mogao da bude učitan.'
  } finally {
    outstandingLoading.value = false
  }
}

const loadLedger = async () => {
  error.value = null
  ledger.value = null

  if (!canLoadLedger.value) {
    error.value =
        'Izaberite dobavljača i period.'

    return
  }

  if (!validatePeriod()) {
    return
  }

  ledgerLoading.value = true

  try {
    ledger.value =
        await supplierLedgerApi.getLedger(
            selectedSupplierId.value,
            formatDateForApi(
                periodFrom.value
            ),
            formatDateForApi(
                periodTo.value
            )
        )
  } catch (loadError) {
    error.value =
        loadError.response?.data?.message ||
        loadError.response?.data?.detail ||
        loadError.message ||
        'Kartica dobavljača nije mogla da bude učitana.'
  } finally {
    ledgerLoading.value = false
  }
}

const openSupplierLedger = async supplier => {
  selectedSupplierId.value =
      supplier.supplierId

  activeTab.value =
      TAB_LEDGER

  ledger.value = null
  error.value = null

  await loadLedger()
}

const switchTab = tab => {
  activeTab.value = tab
  error.value = null
}

const handlePeriodChange = () => {
  outstandingBalances.value = null
  ledger.value = null
  error.value = null
}

const handleOutstandingFilterChange = () => {
  outstandingBalances.value = null
  error.value = null
}

const clearLedger = () => {
  ledger.value = null
  error.value = null
}

const resolveExportFilename = (
    contentDisposition,
    fallbackFilename
) => {
  if (!contentDisposition) {
    return fallbackFilename
  }

  const encodedFilenameMatch =
      contentDisposition.match(
          /filename\*=UTF-8''([^;]+)/
      )

  if (encodedFilenameMatch) {
    try {
      return decodeURIComponent(
          encodedFilenameMatch[1]
      )
    } catch {
      return encodedFilenameMatch[1]
    }
  }

  const filenameMatch =
      contentDisposition.match(
          /filename="?([^";]+)"?/
      )

  if (filenameMatch) {
    return filenameMatch[1]
  }

  return fallbackFilename
}

const downloadBlob = (
    blob,
    contentDisposition,
    fallbackFilename
) => {
  const filename =
      resolveExportFilename(
          contentDisposition,
          fallbackFilename
      )

  const blobUrl =
      window.URL.createObjectURL(
          blob
      )

  const link =
      window.document.createElement('a')

  link.href = blobUrl
  link.download = filename

  window.document.body.appendChild(
      link
  )

  link.click()
  link.remove()

  window.URL.revokeObjectURL(
      blobUrl
  )
}

const exportOutstandingBalances = async () => {
  error.value = null

  if (!validatePeriod()) {
    return
  }

  outstandingExportLoading.value = true

  try {
    const result =
        await supplierLedgerApi
            .exportOutstandingBalances(
                formatDateForApi(
                    periodFrom.value
                ),
                formatDateForApi(
                    periodTo.value
                ),
                onlyOutstanding.value
            )

    downloadBlob(
        result.blob,
        result.contentDisposition,
        'otvorene-obaveze.xlsx'
    )
  } catch (exportError) {
    error.value =
        exportError.response?.data?.message ||
        exportError.response?.data?.detail ||
        exportError.message ||
        'Excel pregled obaveza nije mogao da bude preuzet.'
  } finally {
    outstandingExportLoading.value = false
  }
}

const exportLedger = async () => {
  error.value = null

  if (!ledger.value) {
    error.value =
        'Prvo prikažite karticu dobavljača.'

    return
  }

  exportLoading.value = true

  try {
    const result =
        await supplierLedgerApi.exportLedger(
            selectedSupplierId.value,
            formatDateForApi(
                periodFrom.value
            ),
            formatDateForApi(
                periodTo.value
            )
        )

    downloadBlob(
        result.blob,
        result.contentDisposition,
        'kartica-dobavljaca.xlsx'
    )
  } catch (exportError) {
    error.value =
        exportError.response?.data?.message ||
        exportError.response?.data?.detail ||
        exportError.message ||
        'Excel kartica dobavljača nije mogla da bude preuzeta.'
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([
    loadSuppliers(),
    loadOutstandingBalances()
  ])
})
</script>

<template>
  <div class="supplier-reports-view">
    <div class="page-header">
      <div>
        <h2>Izveštaji dobavljača</h2>

        <p>
          Pregled otvorenih obaveza i
          knjigovodstvenih kartica dobavljača.
        </p>
      </div>
    </div>

    <div class="report-tabs">
      <button
          type="button"
          class="report-tab"
          :class="{
            active:
              activeTab === TAB_OUTSTANDING
          }"
          @click="
            switchTab(TAB_OUTSTANDING)
          "
      >
        <i class="pi pi-wallet" />
        Otvorene obaveze
      </button>

      <button
          type="button"
          class="report-tab"
          :class="{
            active:
              activeTab === TAB_LEDGER
          }"
          @click="
            switchTab(TAB_LEDGER)
          "
      >
        <i class="pi pi-book" />
        Kartica dobavljača
      </button>
    </div>

    <Message
        v-if="error"
        severity="error"
        closable
        @close="error = null"
    >
      {{ error }}
    </Message>

    <template
        v-if="
          activeTab === TAB_OUTSTANDING
        "
    >
      <div class="filters-card outstanding-filters">
        <div class="filter-field">
          <label for="outstanding-period-from">
            Period od
          </label>

          <DatePicker
              id="outstanding-period-from"
              v-model="periodFrom"
              date-format="dd.mm.yy"
              show-icon
              fluid
              @update:model-value="
                handlePeriodChange
              "
          />
        </div>

        <div class="filter-field">
          <label for="outstanding-period-to">
            Period do
          </label>

          <DatePicker
              id="outstanding-period-to"
              v-model="periodTo"
              date-format="dd.mm.yy"
              show-icon
              fluid
              @update:model-value="
                handlePeriodChange
              "
          />
        </div>

        <label class="checkbox-field">
          <input
              v-model="onlyOutstanding"
              type="checkbox"
              @change="
                handleOutstandingFilterChange
              "
          />

          <span>
            Samo otvorene obaveze
          </span>
        </label>

        <div class="filter-action">
          <Button
              label="Prikaži"
              icon="pi pi-search"
              :loading="outstandingLoading"
              :disabled="
                !canLoadPeriod ||
                outstandingLoading ||
                outstandingExportLoading
              "
              @click="loadOutstandingBalances"
          />
        </div>
      </div>

      <div class="report-card">
        <div class="table-title">
          <div>
            <h3>Otvorene obaveze</h3>

            <p>
              Pregled salda po dobavljačima
              za izabrani period.
              Klik na dobavljača otvara
              njegovu karticu.
            </p>
          </div>

          <div class="table-actions">
            <div
                v-if="outstandingBalances"
                class="result-count"
            >
              {{
                outstandingRows.length
              }}
              dobavljača
            </div>

            <Button
                label="Export Excel"
                icon="pi pi-file-excel"
                severity="success"
                outlined
                :loading="outstandingExportLoading"
                :disabled="
                  !canLoadPeriod ||
                  outstandingLoading ||
                  outstandingExportLoading
                "
                @click="exportOutstandingBalances"
            />
          </div>
        </div>

        <DataTable
            :value="outstandingRows"
            :loading="outstandingLoading"
            data-key="supplierId"
            striped-rows
            row-hover
            responsive-layout="scroll"
            class="outstanding-table"
            @row-click="
              openSupplierLedger(
                $event.data
              )
            "
        >
          <template #empty>
            <span
                v-if="
                  outstandingBalances &&
                  onlyOutstanding
                "
            >
              Nema otvorenih obaveza
              za izabrani period.
            </span>

            <span v-else>
              Nema podataka za izabrani period.
            </span>
          </template>

          <Column
              field="supplierName"
              header="Dobavljač"
              style="min-width: 14rem"
          >
            <template #body="{ data }">
              <div class="supplier-cell">
                <strong>
                  {{ data.supplierName }}
                </strong>

                <small>
                  {{ data.supplierCode }}
                </small>
              </div>
            </template>
          </Column>

          <Column
              field="pib"
              header="PIB"
              style="min-width: 9rem"
          >
            <template #body="{ data }">
              {{ data.pib || '—' }}
            </template>
          </Column>

          <Column
              field="openingBalance"
              header="Početno stanje"
              style="min-width: 11rem"
              body-style="text-align: right"
              header-style="text-align: right"
          >
            <template #body="{ data }">
              {{
                formatOutstandingAmount(
                    data.openingBalance,
                    data.currencyCode
                )
              }}
            </template>
          </Column>

          <Column
              field="totalInvoiced"
              header="Fakturisano"
              style="min-width: 11rem"
              body-style="text-align: right"
              header-style="text-align: right"
          >
            <template #body="{ data }">
              {{
                formatOutstandingAmount(
                    data.totalInvoiced,
                    data.currencyCode
                )
              }}
            </template>
          </Column>

          <Column
              field="totalPaid"
              header="Plaćeno"
              style="min-width: 11rem"
              body-style="text-align: right"
              header-style="text-align: right"
          >
            <template #body="{ data }">
              {{
                formatOutstandingAmount(
                    data.totalPaid,
                    data.currencyCode
                )
              }}
            </template>
          </Column>

          <Column
              field="closingBalance"
              header="Saldo"
              style="min-width: 11rem"
              body-style="text-align: right"
              header-style="text-align: right"
          >
            <template #body="{ data }">
              <strong>
                {{
                  formatOutstandingAmount(
                      data.closingBalance,
                      data.currencyCode
                  )
                }}
              </strong>
            </template>
          </Column>
        </DataTable>
      </div>
    </template>

    <template v-else>
      <div class="filters-card ledger-filters">
        <div class="filter-field supplier-filter">
          <label for="supplier">
            Dobavljač
          </label>

          <Select
              id="supplier"
              v-model="selectedSupplierId"
              :options="supplierOptions"
              option-label="label"
              option-value="value"
              placeholder="Izaberite dobavljača"
              filter
              :loading="suppliersLoading"
              :disabled="suppliersLoading"
              fluid
              @change="clearLedger"
          />
        </div>

        <div class="filter-field">
          <label for="ledger-period-from">
            Period od
          </label>

          <DatePicker
              id="ledger-period-from"
              v-model="periodFrom"
              date-format="dd.mm.yy"
              show-icon
              fluid
              @update:model-value="
                handlePeriodChange
              "
          />
        </div>

        <div class="filter-field">
          <label for="ledger-period-to">
            Period do
          </label>

          <DatePicker
              id="ledger-period-to"
              v-model="periodTo"
              date-format="dd.mm.yy"
              show-icon
              fluid
              @update:model-value="
                handlePeriodChange
              "
          />
        </div>

        <div class="filter-action">
          <Button
              label="Prikaži"
              icon="pi pi-search"
              :loading="ledgerLoading"
              :disabled="
                !canLoadLedger ||
                suppliersLoading ||
                exportLoading
              "
              @click="loadLedger"
          />
        </div>
      </div>

      <div
          v-if="ledger"
          class="ledger"
      >
        <div class="ledger-header">
          <div>
            <div class="ledger-caption">
              Dobavljač
            </div>

            <h3>
              {{ ledger.supplierName }}
            </h3>

            <div class="supplier-meta">
              <span>
                Šifra:
                <strong>
                  {{ ledger.supplierCode }}
                </strong>
              </span>

              <span v-if="ledger.pib">
                PIB:
                <strong>
                  {{ ledger.pib }}
                </strong>
              </span>
            </div>
          </div>

          <div class="ledger-period">
            <span>Period</span>

            <strong>
              {{ formatDate(ledger.periodFrom) }}
              —
              {{ formatDate(ledger.periodTo) }}
            </strong>
          </div>
        </div>

        <div class="summary-grid">
          <div class="summary-card">
            <span>Početno stanje</span>

            <strong>
              {{
                formatLedgerAmount(
                    ledger.openingBalance
                )
              }}
            </strong>
          </div>

          <div class="summary-card">
            <span>Fakturisano</span>

            <strong>
              {{
                formatLedgerAmount(
                    ledger.totalInvoiced
                )
              }}
            </strong>
          </div>

          <div class="summary-card">
            <span>Plaćeno</span>

            <strong>
              {{
                formatLedgerAmount(
                    ledger.totalPaid
                )
              }}
            </strong>
          </div>

          <div class="summary-card balance-card">
            <span>Saldo</span>

            <strong>
              {{
                formatLedgerAmount(
                    ledger.closingBalance
                )
              }}
            </strong>
          </div>
        </div>

        <div class="report-card">
          <div class="table-title">
            <div>
              <h3>Promet</h3>

              <p>
                Hronološki pregled faktura
                i povezanih plaćanja.
              </p>
            </div>

            <Button
                label="Export Excel"
                icon="pi pi-file-excel"
                severity="success"
                outlined
                :loading="exportLoading"
                :disabled="
                  exportLoading ||
                  ledgerLoading
                "
                @click="exportLedger"
            />
          </div>

          <DataTable
              :value="ledger.entries"
              :loading="ledgerLoading"
              striped-rows
              responsive-layout="scroll"
              class="ledger-table"
          >
            <template #empty>
              Za izabrani period nema prometa.
            </template>

            <Column
                field="date"
                header="Datum"
                style="min-width: 8rem"
            >
              <template #body="{ data }">
                {{ formatDate(data.date) }}
              </template>
            </Column>

            <Column
                field="invoiceNumber"
                header="Broj fakture"
                style="min-width: 11rem"
            >
              <template #body="{ data }">
                {{
                  data.invoiceNumber || '—'
                }}
              </template>
            </Column>

            <Column
                field="statementCode"
                header="Br. izvoda"
                style="min-width: 11rem"
            >
              <template #body="{ data }">
                {{
                  data.statementCode || '—'
                }}
              </template>
            </Column>

            <Column
                field="transactionReference"
                header="Referenca transakcije"
                style="min-width: 13rem"
            >
              <template #body="{ data }">
                {{
                  data.transactionReference || '—'
                }}
              </template>
            </Column>

            <Column
                field="paidAmount"
                header="Plaćeno"
                style="min-width: 10rem"
                body-style="text-align: right"
                header-style="text-align: right"
            >
              <template #body="{ data }">
                {{
                  formatLedgerAmount(
                      data.paidAmount
                  )
                }}
              </template>
            </Column>

            <Column
                field="invoiceAmount"
                header="Iznos fakture"
                style="min-width: 10rem"
                body-style="text-align: right"
                header-style="text-align: right"
            >
              <template #body="{ data }">
                {{
                  formatLedgerAmount(
                      data.invoiceAmount
                  )
                }}
              </template>
            </Column>

            <Column
                field="balance"
                header="Saldo"
                style="min-width: 10rem"
                body-style="text-align: right"
                header-style="text-align: right"
            >
              <template #body="{ data }">
                <strong>
                  {{
                    formatLedgerAmount(
                        data.balance
                    )
                  }}
                </strong>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>

      <div
          v-else-if="
            !ledgerLoading &&
            !error
          "
          class="empty-state"
      >
        <i class="pi pi-book" />

        <h3>Kartica dobavljača</h3>

        <p>
          Izaberite dobavljača i period,
          pa kliknite na „Prikaži“.
        </p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.supplier-reports-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header h2 {
  margin: 0;
  font-size: 1.75rem;
}

.page-header p {
  margin: 0.5rem 0 0;
  opacity: 0.7;
}

.report-tabs {
  display: flex;
  gap: 0.5rem;
  border-bottom: 1px solid
  var(--p-content-border-color);
}

.report-tab {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem 1.1rem;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--p-text-muted-color);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.report-tab:hover {
  color: var(--p-text-color);
}

.report-tab.active {
  border-bottom-color:
      var(--p-primary-color);
  color:
      var(--p-primary-color);
}

.filters-card {
  display: grid;
  gap: 1rem;
  align-items: end;
  padding: 1.25rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.outstanding-filters {
  grid-template-columns:
    minmax(11rem, 1fr)
    minmax(11rem, 1fr)
    minmax(14rem, auto)
    auto;
}

.ledger-filters {
  grid-template-columns:
    minmax(18rem, 2fr)
    minmax(11rem, 1fr)
    minmax(11rem, 1fr)
    auto;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-field label {
  font-weight: 600;
}

.checkbox-field {
  display: flex;
  min-height: 2.75rem;
  align-items: center;
  gap: 0.6rem;
  font-weight: 600;
  cursor: pointer;
}

.checkbox-field input {
  width: 1.1rem;
  height: 1.1rem;
  accent-color:
      var(--p-primary-color);
}

.filter-action {
  display: flex;
  align-items: flex-end;
}

.report-card {
  padding: 1.25rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.table-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.table-title h3 {
  margin: 0;
}

.table-title p {
  margin: 0.35rem 0 0;
  color:
      var(--p-text-muted-color);
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.result-count {
  flex-shrink: 0;
  color:
      var(--p-text-muted-color);
  font-size: 0.9rem;
}

.supplier-cell {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.supplier-cell small {
  color:
      var(--p-text-muted-color);
}

.outstanding-table {
  width: 100%;
}

.outstanding-table
:deep(.p-datatable-tbody > tr) {
  cursor: pointer;
}

.ledger {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.ledger-header {
  display: flex;
  justify-content: space-between;
  gap: 2rem;
  padding: 1.5rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.ledger-caption,
.ledger-period span,
.summary-card span {
  color:
      var(--p-text-muted-color);
  font-size: 0.875rem;
}

.ledger-header h3 {
  margin: 0.25rem 0 0.5rem;
  font-size: 1.5rem;
}

.supplier-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
}

.ledger-period {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  text-align: right;
}

.summary-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 1rem;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1.25rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.summary-card strong {
  font-size: 1.25rem;
}

.balance-card {
  border-width: 2px;
}

.ledger-table {
  width: 100%;
}

.empty-state {
  display: flex;
  min-height: 18rem;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  border: 1px dashed
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  text-align: center;
  color:
      var(--p-text-muted-color);
}

.empty-state i {
  margin-bottom: 1rem;
  font-size: 2.5rem;
}

.empty-state h3 {
  margin: 0;
  color:
      var(--p-text-color);
}

.empty-state p {
  margin: 0.5rem 0 0;
}

@media (max-width: 960px) {
  .outstanding-filters,
  .ledger-filters {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .supplier-filter {
    grid-column: 1 / -1;
  }

  .summary-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .report-tabs {
    flex-direction: column;
    border-bottom: 0;
  }

  .report-tab {
    justify-content: flex-start;
    border: 1px solid
    var(--p-content-border-color);
    border-radius:
        var(--p-border-radius-md);
  }

  .report-tab.active {
    border-color:
        var(--p-primary-color);
  }

  .outstanding-filters,
  .ledger-filters,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .supplier-filter {
    grid-column: auto;
  }

  .filter-action :deep(.p-button) {
    width: 100%;
  }

  .table-title {
    flex-direction: column;
    align-items: stretch;
  }

  .table-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .table-actions :deep(.p-button),
  .table-title > :deep(.p-button) {
    width: 100%;
  }

  .ledger-header {
    flex-direction: column;
  }

  .ledger-period {
    text-align: left;
  }
}
</style>
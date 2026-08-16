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

const suppliers = ref([])
const selectedSupplierId = ref(null)

const periodFrom = ref(
    new Date(new Date().getFullYear(), 0, 1)
)

const periodTo = ref(
    new Date(new Date().getFullYear(), 11, 31)
)

const ledger = ref(null)

const suppliersLoading = ref(false)
const ledgerLoading = ref(false)
const exportLoading = ref(false)

const error = ref(null)

const canLoadLedger = computed(() =>
    Boolean(
        selectedSupplierId.value &&
        periodFrom.value &&
        periodTo.value
    )
)

const supplierOptions = computed(() =>
    suppliers.value.map(supplier => ({
      label: supplier.name,
      value: supplier.id
    }))
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

const loadLedger = async () => {
  error.value = null
  ledger.value = null

  if (!canLoadLedger.value) {
    error.value =
        'Izaberite dobavljača i period.'

    return
  }

  if (
      periodFrom.value >
      periodTo.value
  ) {
    error.value =
        'Početni datum ne može biti posle krajnjeg datuma.'

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

const resolveExportFilename = contentDisposition => {
  if (!contentDisposition) {
    return 'kartica-dobavljaca.xlsx'
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

  return 'kartica-dobavljaca.xlsx'
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

    const filename =
        resolveExportFilename(
            result.contentDisposition
        )

    const blobUrl =
        window.URL.createObjectURL(
            result.blob
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

const clearLedger = () => {
  ledger.value = null
  error.value = null
}

onMounted(
    loadSuppliers
)
</script>

<template>
  <div class="supplier-ledger-view">
    <div class="page-header">
      <div>
        <h2>Kartica dobavljača</h2>

        <p>
          Pregled faktura, plaćanja i salda
          dobavljača za izabrani period.
        </p>
      </div>
    </div>

    <div class="filters-card">
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
        <label for="period-from">
          Period od
        </label>

        <DatePicker
            id="period-from"
            v-model="periodFrom"
            date-format="dd.mm.yy"
            show-icon
            fluid
            @update:model-value="clearLedger"
        />
      </div>

      <div class="filter-field">
        <label for="period-to">
          Period do
        </label>

        <DatePicker
            id="period-to"
            v-model="periodTo"
            date-format="dd.mm.yy"
            show-icon
            fluid
            @update:model-value="clearLedger"
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

    <Message
        v-if="error"
        severity="error"
        closable
        @close="error = null"
    >
      {{ error }}
    </Message>

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

      <div class="ledger-table-card">
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
  </div>
</template>

<style scoped>
.supplier-ledger-view {
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

.filters-card {
  display: grid;
  grid-template-columns:
    minmax(18rem, 2fr)
    minmax(11rem, 1fr)
    minmax(11rem, 1fr)
    auto;
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

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-field label {
  font-weight: 600;
}

.filter-action {
  display: flex;
  align-items: flex-end;
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

.ledger-table-card {
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
  .filters-card {
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
  .filters-card,
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

  .table-title :deep(.p-button) {
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
<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DatePicker from 'primevue/datepicker'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'

import { documentApi } from '../api/documentApi'
import { documentRecordApi } from '../api/documentRecordApi'
import { supplierApi } from '../api/supplierApi'

const documentRecords = ref([])
const suppliers = ref([])

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)

const error = ref(null)
const dialogError = ref(null)

const dialogVisible = ref(false)
const selectedRecord = ref(null)

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewUrl = ref(null)
const previewDocument = ref(null)
const previewError = ref(null)

const search = ref('')
const selectedType = ref(null)
const selectedSupplierId = ref(null)

const uploadDisplayName = ref('')
const uploadFile = ref(null)

const documentTypes = [
  {
    value: 'CONTRACT',
    label: 'Ugovor'
  },
  {
    value: 'WARRANTY',
    label: 'Garancija'
  },
  {
    value: 'DECISION',
    label: 'Rešenje'
  },
  {
    value: 'CERTIFICATE',
    label: 'Potvrda'
  },
  {
    value: 'INSURANCE_POLICY',
    label: 'Polisa osiguranja'
  },
  {
    value: 'OTHER',
    label: 'Ostalo'
  }
]

const form = ref({
  title: '',
  documentType: 'CONTRACT',
  documentNumber: '',
  supplierId: null,
  documentDate: null,
  validFrom: null,
  validUntil: null,
  notes: ''
})

const supplierOptions = computed(() =>
    suppliers.value.map(supplier => ({
      label: supplier.name,
      value: supplier.id
    }))
)

const typeFilterOptions = computed(() => [
  {
    label: 'Svi tipovi',
    value: null
  },
  ...documentTypes
])

const supplierFilterOptions = computed(() => [
  {
    label: 'Svi dobavljači',
    value: null
  },
  ...supplierOptions.value
])

const filteredRecords = computed(() => {
  const normalizedSearch =
      search.value
          .trim()
          .toLowerCase()

  return documentRecords.value.filter(record => {
    if (
        selectedType.value &&
        record.documentType !== selectedType.value
    ) {
      return false
    }

    if (
        selectedSupplierId.value &&
        record.supplierId !==
        selectedSupplierId.value
    ) {
      return false
    }

    if (!normalizedSearch) {
      return true
    }

    const searchableValues = [
      record.title,
      record.documentNumber,
      record.supplierName,
      record.supplierCode
    ]

    return searchableValues.some(value =>
        value
            ?.toLowerCase()
            .includes(normalizedSearch)
    )
  })
})

const dialogTitle = computed(() =>
    selectedRecord.value
        ? 'Izmena dokumenta'
        : 'Novi dokument'
)

const canUpload = computed(() =>
    Boolean(
        selectedRecord.value?.id &&
        uploadDisplayName.value.trim() &&
        uploadFile.value
    )
)

const isPreviewImage = computed(() =>
    previewDocument.value
        ?.contentType
        ?.startsWith('image/')
)

const isPreviewPdf = computed(() =>
    previewDocument.value
        ?.contentType === 'application/pdf'
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

const parseDate = value => {
  if (!value) {
    return null
  }

  return new Date(
      `${value}T00:00:00`
  )
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

const formatFileSize = sizeBytes => {
  if (
      sizeBytes === null ||
      sizeBytes === undefined
  ) {
    return '—'
  }

  if (sizeBytes < 1024) {
    return `${sizeBytes} B`
  }

  const kilobytes =
      sizeBytes / 1024

  if (kilobytes < 1024) {
    return `${kilobytes.toFixed(1)} KB`
  }

  const megabytes =
      kilobytes / 1024

  return `${megabytes.toFixed(1)} MB`
}

const resolveTypeLabel = type => {
  return documentTypes
      .find(option =>
          option.value === type
      )
      ?.label || type
}

const getValidityClass = record => {
  if (!record.validUntil) {
    return ''
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const validUntil =
      parseDate(record.validUntil)

  if (validUntil < today) {
    return 'validity-expired'
  }

  const warningDate =
      new Date(today)

  warningDate.setDate(
      warningDate.getDate() + 30
  )

  if (validUntil <= warningDate) {
    return 'validity-warning'
  }

  return ''
}

const loadRecords = async () => {
  loading.value = true
  error.value = null

  try {
    documentRecords.value =
        await documentRecordApi.findAll()
  } catch (loadError) {
    documentRecords.value = []

    error.value =
        loadError.message ||
        'Dokumenti nisu mogli da budu učitani.'
  } finally {
    loading.value = false
  }
}

const loadSuppliers = async () => {
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
  } catch {
    suppliers.value = []
  }
}

const resetUpload = () => {
  uploadDisplayName.value = ''
  uploadFile.value = null

  const input =
      document.getElementById(
          'document-upload-file'
      )

  if (input) {
    input.value = ''
  }
}

const resetForm = () => {
  form.value = {
    title: '',
    documentType: 'CONTRACT',
    documentNumber: '',
    supplierId: null,
    documentDate: null,
    validFrom: null,
    validUntil: null,
    notes: ''
  }
}

const populateForm = record => {
  form.value = {
    title:
        record.title || '',
    documentType:
        record.documentType || 'OTHER',
    documentNumber:
        record.documentNumber || '',
    supplierId:
        record.supplierId || null,
    documentDate:
        parseDate(record.documentDate),
    validFrom:
        parseDate(record.validFrom),
    validUntil:
        parseDate(record.validUntil),
    notes:
        record.notes || ''
  }
}

const openCreateDialog = () => {
  selectedRecord.value = null
  dialogError.value = null

  resetForm()
  resetUpload()

  dialogVisible.value = true
}

const openEditDialog = record => {
  selectedRecord.value = record
  dialogError.value = null

  populateForm(record)
  resetUpload()

  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  selectedRecord.value = null
  dialogError.value = null

  resetForm()
  resetUpload()
}

const createRequest = () => ({
  title:
  form.value.title,
  documentType:
  form.value.documentType,
  documentNumber:
      form.value.documentNumber || null,
  supplierId:
  form.value.supplierId,
  documentDate:
      formatDateForApi(
          form.value.documentDate
      ),
  validFrom:
      formatDateForApi(
          form.value.validFrom
      ),
  validUntil:
      formatDateForApi(
          form.value.validUntil
      ),
  notes:
      form.value.notes || null
})

const validateForm = () => {
  if (!form.value.title.trim()) {
    dialogError.value =
        'Naziv dokumenta je obavezan.'

    return false
  }

  if (!form.value.documentType) {
    dialogError.value =
        'Tip dokumenta je obavezan.'

    return false
  }

  if (
      form.value.validFrom &&
      form.value.validUntil &&
      form.value.validUntil <
      form.value.validFrom
  ) {
    dialogError.value =
        'Datum „Važi do“ ne može biti pre datuma „Važi od“.'

    return false
  }

  return true
}

const saveRecord = async () => {
  dialogError.value = null

  if (!validateForm()) {
    return
  }

  saving.value = true

  try {
    const request =
        createRequest()

    let savedRecord

    if (selectedRecord.value?.id) {
      savedRecord =
          await documentRecordApi.update(
              selectedRecord.value.id,
              request
          )
    } else {
      savedRecord =
          await documentRecordApi.create(
              request
          )
    }

    selectedRecord.value =
        savedRecord

    populateForm(
        savedRecord
    )

    await loadRecords()

    const refreshedRecord =
        documentRecords.value.find(record =>
            record.id === savedRecord.id
        )

    if (refreshedRecord) {
      selectedRecord.value =
          refreshedRecord
    }
  } catch (saveError) {
    dialogError.value =
        saveError.message ||
        'Dokument nije mogao da bude sačuvan.'
  } finally {
    saving.value = false
  }
}

const handleFileSelected = event => {
  const file =
      event.target.files?.[0]

  uploadFile.value =
      file || null

  if (
      file &&
      !uploadDisplayName.value.trim()
  ) {
    uploadDisplayName.value =
        file.name
  }
}

const uploadDocument = async () => {
  dialogError.value = null

  if (!canUpload.value) {
    dialogError.value =
        'Unesite naziv fajla i izaberite fajl.'

    return
  }

  uploading.value = true

  try {
    const updatedRecord =
        await documentRecordApi.addDocument(
            selectedRecord.value.id,
            uploadDisplayName.value,
            uploadFile.value
        )

    selectedRecord.value =
        updatedRecord

    resetUpload()

    await loadRecords()
  } catch (uploadError) {
    dialogError.value =
        uploadError.message ||
        'Fajl nije mogao da bude dodat.'
  } finally {
    uploading.value = false
  }
}

const closePreview = () => {
  previewVisible.value = false
  previewDocument.value = null
  previewError.value = null

  if (previewUrl.value) {
    window.URL.revokeObjectURL(
        previewUrl.value
    )

    previewUrl.value = null
  }
}

const previewDocumentFile = async documentItem => {
  previewLoading.value = true
  previewError.value = null
  previewDocument.value = documentItem
  previewVisible.value = true

  if (previewUrl.value) {
    window.URL.revokeObjectURL(
        previewUrl.value
    )

    previewUrl.value = null
  }

  try {
    const response =
        await documentApi.download(
            documentItem.id
        )

    previewUrl.value =
        window.URL.createObjectURL(
            response.data
        )
  } catch (previewLoadError) {
    previewError.value =
        previewLoadError.message ||
        'Pregled fajla nije mogao da bude učitan.'
  } finally {
    previewLoading.value = false
  }
}

const downloadDocument = async documentItem => {
  dialogError.value = null

  try {
    const response =
        await documentApi.download(
            documentItem.id
        )

    const blobUrl =
        window.URL.createObjectURL(
            response.data
        )

    const link =
        window.document.createElement('a')

    link.href = blobUrl

    link.download =
        documentItem.filename ||
        documentItem.displayName ||
        'document'

    window.document.body.appendChild(
        link
    )

    link.click()
    link.remove()

    window.URL.revokeObjectURL(
        blobUrl
    )
  } catch (downloadError) {
    dialogError.value =
        downloadError.message ||
        'Fajl nije mogao da bude preuzet.'
  }
}

const removeAttachment = async documentItem => {
  if (
      !window.confirm(
          `Obrisati fajl "${documentItem.displayName}"?`
      )
  ) {
    return
  }

  dialogError.value = null

  try {
    const updatedRecord =
        await documentRecordApi.removeDocument(
            selectedRecord.value.id,
            documentItem.id
        )

    selectedRecord.value =
        updatedRecord

    await loadRecords()
  } catch (removeError) {
    dialogError.value =
        removeError.message ||
        'Fajl nije mogao da bude obrisan.'
  }
}

const removeRecord = async record => {
  if (
      !window.confirm(
          `Obrisati dokument "${record.title}" i sve njegove fajlove?`
      )
  ) {
    return
  }

  error.value = null

  try {
    await documentRecordApi.remove(
        record.id
    )

    await loadRecords()
  } catch (removeError) {
    error.value =
        removeError.message ||
        'Dokument nije mogao da bude obrisan.'
  }
}

onMounted(async () => {
  await Promise.all([
    loadRecords(),
    loadSuppliers()
  ])
})

onUnmounted(() => {
  closePreview()
})
</script>

<template>
  <div class="documents-view">
    <div class="page-header">
      <div>
        <h2>Dokumenti</h2>

        <p>
          Arhiva ugovora, garancija,
          rešenja, potvrda i ostale dokumentacije.
        </p>
      </div>

      <Button
          label="Novi dokument"
          icon="pi pi-plus"
          @click="openCreateDialog"
      />
    </div>

    <Message
        v-if="error"
        severity="error"
        closable
        @close="error = null"
    >
      {{ error }}
    </Message>

    <div class="filters-card">
      <div class="filter-field search-field">
        <label for="document-search">
          Pretraga
        </label>

        <InputText
            id="document-search"
            v-model="search"
            placeholder="Naziv, broj ili dobavljač..."
        />
      </div>

      <div class="filter-field">
        <label for="document-type-filter">
          Tip
        </label>

        <Select
            id="document-type-filter"
            v-model="selectedType"
            :options="typeFilterOptions"
            option-label="label"
            option-value="value"
            fluid
        />
      </div>

      <div class="filter-field">
        <label for="supplier-filter">
          Dobavljač
        </label>

        <Select
            id="supplier-filter"
            v-model="selectedSupplierId"
            :options="supplierFilterOptions"
            option-label="label"
            option-value="value"
            filter
            fluid
        />
      </div>
    </div>

    <div class="table-card">
      <div class="table-header">
        <div>
          <h3>Arhiva dokumenata</h3>

          <p>
            {{ filteredRecords.length }}
            dokumenata
          </p>
        </div>
      </div>

      <DataTable
          :value="filteredRecords"
          :loading="loading"
          data-key="id"
          striped-rows
          responsive-layout="scroll"
      >
        <template #empty>
          Nema dokumenata.
        </template>

        <Column
            field="documentType"
            header="Tip"
            style="min-width: 10rem"
        >
          <template #body="{ data }">
            {{ resolveTypeLabel(data.documentType) }}
          </template>
        </Column>

        <Column
            field="title"
            header="Naziv"
            style="min-width: 16rem"
        >
          <template #body="{ data }">
            <strong>
              {{ data.title }}
            </strong>
          </template>
        </Column>

        <Column
            field="supplierName"
            header="Dobavljač"
            style="min-width: 13rem"
        >
          <template #body="{ data }">
            {{ data.supplierName || '—' }}
          </template>
        </Column>

        <Column
            field="documentNumber"
            header="Broj"
            style="min-width: 11rem"
        >
          <template #body="{ data }">
            {{ data.documentNumber || '—' }}
          </template>
        </Column>

        <Column
            field="documentDate"
            header="Datum"
            style="min-width: 9rem"
        >
          <template #body="{ data }">
            {{ formatDate(data.documentDate) }}
          </template>
        </Column>

        <Column
            field="validUntil"
            header="Važi do"
            style="min-width: 9rem"
        >
          <template #body="{ data }">
            <span
                :class="
                  getValidityClass(data)
                "
            >
              {{ formatDate(data.validUntil) }}
            </span>
          </template>
        </Column>

        <Column
            header="Fajlovi"
            style="width: 7rem"
            body-style="text-align: center"
            header-style="text-align: center"
        >
          <template #body="{ data }">
            {{ data.documents?.length || 0 }}
          </template>
        </Column>

        <Column
            header="Akcije"
            style="width: 9rem"
        >
          <template #body="{ data }">
            <div class="row-actions">
              <Button
                  icon="pi pi-pencil"
                  text
                  rounded
                  aria-label="Izmeni dokument"
                  @click="
                    openEditDialog(data)
                  "
              />

              <Button
                  icon="pi pi-trash"
                  text
                  rounded
                  severity="danger"
                  aria-label="Obriši dokument"
                  @click="
                    removeRecord(data)
                  "
              />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <Dialog
        v-model:visible="dialogVisible"
        :header="dialogTitle"
        modal
        :style="{ width: '64rem' }"
        :breakpoints="{
          '960px': '90vw',
          '640px': '96vw'
        }"
        @hide="closeDialog"
    >
      <div class="dialog-content">
        <Message
            v-if="dialogError"
            severity="error"
            closable
            @close="dialogError = null"
        >
          {{ dialogError }}
        </Message>

        <div class="form-grid">
          <div class="form-field full-width">
            <label for="document-title">
              Naziv *
            </label>

            <InputText
                id="document-title"
                v-model="form.title"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="document-type">
              Tip *
            </label>

            <Select
                id="document-type"
                v-model="form.documentType"
                :options="documentTypes"
                option-label="label"
                option-value="value"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="document-number">
              Broj dokumenta
            </label>

            <InputText
                id="document-number"
                v-model="form.documentNumber"
                fluid
            />
          </div>

          <div class="form-field full-width">
            <label for="document-supplier">
              Dobavljač
            </label>

            <Select
                id="document-supplier"
                v-model="form.supplierId"
                :options="supplierOptions"
                option-label="label"
                option-value="value"
                placeholder="Bez dobavljača"
                show-clear
                filter
                fluid
            />
          </div>

          <div class="dates-grid full-width">
            <div class="form-field">
              <label for="document-date">
                Datum dokumenta
              </label>

              <DatePicker
                  id="document-date"
                  v-model="form.documentDate"
                  date-format="dd.mm.yy"
                  show-icon
                  fluid
              />
            </div>

            <div class="form-field">
              <label for="valid-from">
                Važi od
              </label>

              <DatePicker
                  id="valid-from"
                  v-model="form.validFrom"
                  date-format="dd.mm.yy"
                  show-icon
                  fluid
              />
            </div>

            <div class="form-field">
              <label for="valid-until">
                Važi do
              </label>

              <DatePicker
                  id="valid-until"
                  v-model="form.validUntil"
                  date-format="dd.mm.yy"
                  show-icon
                  fluid
              />
            </div>
          </div>

          <div class="form-field full-width">
            <label for="document-notes">
              Napomena
            </label>

            <Textarea
                id="document-notes"
                v-model="form.notes"
                rows="3"
                auto-resize
                fluid
            />
          </div>
        </div>

        <div class="save-section">
          <Button
              :label="
                selectedRecord
                    ? 'Sačuvaj izmene'
                    : 'Kreiraj dokument'
              "
              icon="pi pi-save"
              :loading="saving"
              @click="saveRecord"
          />
        </div>

        <div
            v-if="selectedRecord"
            class="attachments-section"
        >
          <div class="section-header">
            <div>
              <h3>Fajlovi</h3>

              <p>
                PDF, JPG ili PNG dokumenti.
              </p>
            </div>
          </div>

          <div class="upload-grid">
            <div class="form-field">
              <label for="upload-display-name">
                Naziv fajla
              </label>

              <InputText
                  id="upload-display-name"
                  v-model="uploadDisplayName"
                  fluid
              />
            </div>

            <div class="form-field">
              <label for="document-upload-file">
                Fajl
              </label>

              <input
                  id="document-upload-file"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  @change="handleFileSelected"
              />
            </div>

            <div class="upload-action">
              <Button
                  label="Dodaj fajl"
                  icon="pi pi-upload"
                  :loading="uploading"
                  :disabled="!canUpload"
                  @click="uploadDocument"
              />
            </div>
          </div>

          <DataTable
              :value="
                selectedRecord.documents || []
              "
              data-key="id"
              striped-rows
              responsive-layout="scroll"
              class="attachments-table"
          >
            <template #empty>
              Dokument još nema fajlova.
            </template>

            <Column
                field="displayName"
                header="Naziv"
                style="min-width: 14rem"
            />

            <Column
                field="filename"
                header="Originalni fajl"
                style="min-width: 14rem"
            />

            <Column
                field="sizeBytes"
                header="Veličina"
                style="min-width: 8rem"
            >
              <template #body="{ data }">
                {{ formatFileSize(data.sizeBytes) }}
              </template>
            </Column>

            <Column
                header="Akcije"
                style="width: 11rem"
            >
              <template #body="{ data }">
                <div class="row-actions">
                  <Button
                      icon="pi pi-eye"
                      text
                      rounded
                      aria-label="Pregledaj fajl"
                      @click="
                        previewDocumentFile(data)
                      "
                  />

                  <Button
                      icon="pi pi-download"
                      text
                      rounded
                      aria-label="Preuzmi fajl"
                      @click="
                        downloadDocument(data)
                      "
                  />

                  <Button
                      icon="pi pi-trash"
                      text
                      rounded
                      severity="danger"
                      aria-label="Obriši fajl"
                      @click="
                        removeAttachment(data)
                      "
                  />
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>
    </Dialog>

    <Dialog
        v-model:visible="previewVisible"
        :header="
          previewDocument?.displayName ||
          previewDocument?.filename ||
          'Pregled dokumenta'
        "
        modal
        :style="{ width: '82vw' }"
        :breakpoints="{
          '960px': '94vw',
          '640px': '98vw'
        }"
        @hide="closePreview"
    >
      <div class="preview-content">
        <Message
            v-if="previewError"
            severity="error"
        >
          {{ previewError }}
        </Message>

        <div
            v-if="previewLoading"
            class="preview-loading"
        >
          <i class="pi pi-spin pi-spinner" />
          <span>Učitavanje dokumenta...</span>
        </div>

        <template
            v-else-if="previewUrl"
        >
          <img
              v-if="isPreviewImage"
              :src="previewUrl"
              :alt="
                previewDocument?.displayName ||
                previewDocument?.filename ||
                'Dokument'
              "
              class="preview-image"
          />

          <iframe
              v-else-if="isPreviewPdf"
              :src="previewUrl"
              class="preview-pdf"
              title="Pregled PDF dokumenta"
          />

          <div
              v-else
              class="preview-unsupported"
          >
            <i class="pi pi-file" />

            <p>
              Pregled ovog tipa fajla nije podržan.
            </p>
          </div>
        </template>

        <div class="preview-actions">
          <Button
              v-if="previewDocument"
              label="Preuzmi"
              icon="pi pi-download"
              outlined
              @click="
                downloadDocument(previewDocument)
              "
          />
        </div>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
.documents-view {
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

.page-header h2,
.table-header h3,
.attachments-section h3 {
  margin: 0;
}

.page-header p,
.table-header p,
.attachments-section p {
  margin: 0.4rem 0 0;
  color:
      var(--p-text-muted-color);
}

.filters-card {
  display: grid;
  grid-template-columns:
    minmax(18rem, 2fr)
    minmax(12rem, 1fr)
    minmax(14rem, 1fr);
  gap: 1rem;
  padding: 1.25rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.filter-field,
.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-field label,
.form-field label {
  font-weight: 600;
}

.table-card {
  padding: 1.25rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.table-header {
  margin-bottom: 1rem;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.validity-expired {
  font-weight: 700;
  color:
      var(--p-red-500);
}

.validity-warning {
  font-weight: 700;
  color:
      var(--p-orange-500);
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.dates-grid {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.full-width {
  grid-column: 1 / -1;
}

.save-section {
  display: flex;
  justify-content: flex-end;
}

.attachments-section {
  padding-top: 1.5rem;
  border-top: 1px solid
  var(--p-content-border-color);
}

.section-header {
  margin-bottom: 1rem;
}

.upload-grid {
  display: grid;
  grid-template-columns:
    minmax(14rem, 1fr)
    minmax(16rem, 1fr)
    auto;
  gap: 1rem;
  align-items: end;
  margin-bottom: 1.25rem;
}

.upload-action {
  display: flex;
  align-items: flex-end;
}

#document-upload-file {
  min-height: 2.75rem;
  padding: 0.45rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
  color:
      var(--p-text-color);
}

.attachments-table {
  width: 100%;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preview-loading,
.preview-unsupported {
  display: flex;
  min-height: 24rem;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  color:
      var(--p-text-muted-color);
}

.preview-loading i,
.preview-unsupported i {
  font-size: 2rem;
}

.preview-image {
  display: block;
  max-width: 100%;
  max-height: 70vh;
  margin: 0 auto;
  object-fit: contain;
}

.preview-pdf {
  width: 100%;
  height: 70vh;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background: white;
}

.preview-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .filters-card,
  .upload-grid,
  .dates-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .search-field {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header :deep(.p-button) {
    width: 100%;
  }

  .filters-card,
  .form-grid,
  .upload-grid,
  .dates-grid {
    grid-template-columns: 1fr;
  }

  .search-field,
  .full-width {
    grid-column: auto;
  }

  .save-section :deep(.p-button),
  .upload-action :deep(.p-button) {
    width: 100%;
  }
}
</style>
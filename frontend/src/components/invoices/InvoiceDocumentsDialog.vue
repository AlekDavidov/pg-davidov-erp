<script setup>
import {
  computed,
  onBeforeUnmount,
  ref,
  watch
} from 'vue'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'

import Button from 'primevue/button'
import Column from 'primevue/column'
import ConfirmDialog from 'primevue/confirmdialog'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import FileUpload from 'primevue/fileupload'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Tag from 'primevue/tag'
import Tooltip from 'primevue/tooltip'

import { documentApi } from '../../api/documentApi'
import { invoiceApi } from '../../api/invoiceApi'
import { formatDateTime } from '../../utils/formatters'

const vTooltip = Tooltip

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },

  invoice: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:visible',
  'changed'
])

const confirm = useConfirm()
const toast = useToast()

const documents = ref([])
const displayName = ref('')
const loading = ref(false)
const uploading = ref(false)
const deleting = ref(false)
const previewLoading = ref(false)
const error = ref(null)
const submitted = ref(false)

const previewVisible = ref(false)
const previewUrl = ref(null)
const previewDocument = ref(null)

const dialogTitle = computed(() => {
  if (!props.invoice) {
    return 'Dokumenti fakture'
  }

  return `Dokumenti fakture ${props.invoice.invoiceNumber}`
})

const previewTitle = computed(() =>
    previewDocument.value?.displayName ||
    previewDocument.value?.filename ||
    previewDocument.value?.documentCode ||
    'Pregled dokumenta'
)

const acceptedFileTypes =
    'application/pdf,image/jpeg,image/png'

const maxFileSize = 10 * 1024 * 1024

const normalizedDisplayName = computed(() =>
    displayName.value.trim()
)

const displayNameInvalid = computed(() =>
    submitted.value &&
    !normalizedDisplayName.value
)

const isPdfPreview = computed(() =>
    previewDocument.value?.contentType ===
    'application/pdf'
)

const isImagePreview = computed(() =>
    previewDocument.value?.contentType ===
    'image/jpeg' ||
    previewDocument.value?.contentType ===
    'image/png'
)

const formatFileSize = bytes => {
  if (
      bytes === null ||
      bytes === undefined
  ) {
    return '—'
  }

  const numericBytes = Number(bytes)

  if (numericBytes < 1024) {
    return `${numericBytes} B`
  }

  if (numericBytes < 1024 * 1024) {
    return `${(
        numericBytes / 1024
    ).toFixed(1)} KB`
  }

  return `${(
      numericBytes /
      (1024 * 1024)
  ).toFixed(1)} MB`
}

const getFileTypeLabel = contentType => {
  const labels = {
    'application/pdf': 'PDF',
    'image/jpeg': 'JPEG',
    'image/png': 'PNG'
  }

  return labels[contentType] ||
      contentType ||
      '—'
}

const getFileTypeSeverity = contentType => {
  const severities = {
    'application/pdf': 'danger',
    'image/jpeg': 'info',
    'image/png': 'success'
  }

  return severities[contentType] ||
      'secondary'
}

const getFileIcon = contentType => {
  if (contentType === 'application/pdf') {
    return 'pi pi-file-pdf'
  }

  if (
      contentType === 'image/jpeg' ||
      contentType === 'image/png'
  ) {
    return 'pi pi-image'
  }

  return 'pi pi-file'
}

const resetUploadForm = () => {
  displayName.value = ''
  submitted.value = false
}

const revokePreviewUrl = () => {
  if (!previewUrl.value) {
    return
  }

  window.URL.revokeObjectURL(
      previewUrl.value
  )

  previewUrl.value = null
}

const resetPreview = () => {
  revokePreviewUrl()

  previewVisible.value = false
  previewDocument.value = null
  previewLoading.value = false
}

const loadDocuments = async () => {
  if (!props.invoice?.id) {
    return
  }

  loading.value = true
  error.value = null

  try {
    const response =
        await invoiceApi.getDocuments(
            props.invoice.id
        )

    documents.value =
        Array.isArray(response)
            ? response
            : []
  } catch (loadError) {
    documents.value = []

    error.value =
        loadError.message ||
        'Dokumenti nisu mogli da budu učitani.'
  } finally {
    loading.value = false
  }
}

const uploadDocument = async event => {
  submitted.value = true
  error.value = null

  const file = event.files?.[0]

  if (
      !file ||
      !props.invoice?.id ||
      !normalizedDisplayName.value
  ) {
    event.options?.clear?.()
    return
  }

  uploading.value = true

  try {
    await invoiceApi.uploadDocument(
        props.invoice.id,
        normalizedDisplayName.value,
        file
    )

    toast.add({
      severity: 'success',
      summary: 'Dokument je dodat',
      detail: normalizedDisplayName.value,
      life: 3000
    })

    event.options?.clear?.()
    resetUploadForm()

    await loadDocuments()
    emit('changed')
  } catch (uploadError) {
    error.value =
        uploadError.message ||
        'Dokument nije mogao da bude dodat.'
  } finally {
    uploading.value = false
  }
}

const previewDocumentFile = async document => {
  if (!document?.id) {
    return
  }

  error.value = null
  previewLoading.value = true

  revokePreviewUrl()

  previewDocument.value = document
  previewVisible.value = true

  try {
    const response =
        await documentApi.download(
            document.id
        )

    previewUrl.value =
        window.URL.createObjectURL(
            response.data
        )
  } catch (previewError) {
    error.value =
        previewError.message ||
        'Pregled dokumenta nije mogao da bude učitan.'

    resetPreview()
  } finally {
    previewLoading.value = false
  }
}

const downloadDocument = async document => {
  if (!document?.id) {
    return
  }

  error.value = null

  try {
    const response =
        await documentApi.download(
            document.id
        )

    const blobUrl =
        window.URL.createObjectURL(
            response.data
        )

    const link =
        window.document.createElement('a')

    link.href = blobUrl
    link.download =
        document.filename ||
        document.documentCode

    window.document.body.appendChild(link)
    link.click()
    link.remove()

    window.URL.revokeObjectURL(blobUrl)
  } catch (downloadError) {
    error.value =
        downloadError.message ||
        'Dokument nije mogao da bude preuzet.'
  }
}

const confirmRemove = document => {
  const documentName =
      document.displayName ||
      document.filename ||
      document.documentCode

  confirm.require({
    header: 'Uklanjanje dokumenta',
    message:
        `Da li ste sigurni da želite da uklonite dokument „${documentName}“ sa fakture?`,
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Otkaži',
    acceptLabel: 'Ukloni',
    rejectProps: {
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      severity: 'danger'
    },
    accept: async () => {
      deleting.value = true
      error.value = null

      try {
        await invoiceApi.removeDocument(
            props.invoice.id,
            document.id
        )

        if (
            previewDocument.value?.id ===
            document.id
        ) {
          resetPreview()
        }

        toast.add({
          severity: 'success',
          summary: 'Dokument je uklonjen',
          detail: documentName,
          life: 3000
        })

        await loadDocuments()
        emit('changed')
      } catch (removeError) {
        error.value =
            removeError.message ||
            'Dokument nije mogao da bude uklonjen.'
      } finally {
        deleting.value = false
      }
    }
  })
}

const closePreview = () => {
  if (previewLoading.value) {
    return
  }

  resetPreview()
}

const closeDialog = () => {
  if (
      uploading.value ||
      deleting.value ||
      previewLoading.value
  ) {
    return
  }

  resetPreview()

  emit('update:visible', false)
}

watch(
    () => props.visible,
    async visible => {
      if (!visible) {
        resetUploadForm()
        resetPreview()
        return
      }

      resetUploadForm()
      resetPreview()

      await loadDocuments()
    }
)

onBeforeUnmount(() => {
  revokePreviewUrl()
})
</script>

<template>
  <Dialog
      :visible="visible"
      :header="dialogTitle"
      modal
      :closable="
      !uploading &&
      !deleting &&
      !previewLoading
    "
      :dismissable-mask="
      !uploading &&
      !deleting &&
      !previewLoading
    "
      :style="{ width: '64rem' }"
      :breakpoints="{
      '1024px': '95vw'
    }"
      @update:visible="
      emit('update:visible', $event)
    "
  >
    <ConfirmDialog />

    <div class="documents-dialog">
      <Message
          v-if="error"
          severity="error"
          closable
          @close="error = null"
      >
        {{ error }}
      </Message>

      <div class="upload-section">
        <div class="upload-info">
          <h3>Dodavanje dokumenta</h3>

          <p>
            Unesite naziv pod kojim će dokument biti prikazan,
            pa izaberite PDF, JPEG ili PNG fajl do 10 MB.
          </p>
        </div>

        <div class="upload-actions">
          <div class="display-name-field">
            <label for="document-display-name">
              Naziv dokumenta
              <span class="required">*</span>
            </label>

            <InputText
                id="document-display-name"
                v-model="displayName"
                placeholder="Na primer: Faktura za stočnu hranu"
                maxlength="255"
                :invalid="displayNameInvalid"
                :disabled="uploading || deleting"
                fluid
            />

            <small
                v-if="displayNameInvalid"
                class="field-error"
            >
              Naziv dokumenta je obavezan.
            </small>
          </div>

          <div class="upload-button-field">
            <FileUpload
                mode="basic"
                name="file"
                choose-label="Izaberi dokument"
                choose-icon="pi pi-upload"
                :accept="acceptedFileTypes"
                :max-file-size="maxFileSize"
                :auto="true"
                custom-upload
                :disabled="
                uploading ||
                deleting ||
                !normalizedDisplayName
              "
                @uploader="uploadDocument"
            />
          </div>
        </div>
      </div>

      <DataTable
          :value="documents"
          :loading="loading"
          data-key="id"
          striped-rows
          responsive-layout="scroll"
          @row-dblclick="
          previewDocumentFile($event.data)
        "
      >
        <template #empty>
          Faktura nema povezanih dokumenata.
        </template>

        <Column
            field="documentCode"
            header="Šifra"
            style="min-width: 9rem"
        />

        <Column
            field="displayName"
            header="Naziv dokumenta"
            style="min-width: 18rem"
        >
          <template #body="{ data }">
            <button
                v-tooltip.top="
                data.filename
                  ? `Originalni fajl: ${data.filename}`
                  : null
              "
                class="document-name-button"
                type="button"
                @click="previewDocumentFile(data)"
            >
              <i
                  :class="
                  getFileIcon(data.contentType)
                "
              />

              <span class="document-name-content">
                <strong>
                  {{
                    data.displayName ||
                    data.filename ||
                    data.documentCode
                  }}
                </strong>

                <small v-if="data.filename">
                  {{ data.filename }}
                </small>
              </span>
            </button>
          </template>
        </Column>

        <Column
            field="contentType"
            header="Tip"
            style="min-width: 7rem"
        >
          <template #body="{ data }">
            <Tag
                :value="
                getFileTypeLabel(
                  data.contentType
                )
              "
                :severity="
                getFileTypeSeverity(
                  data.contentType
                )
              "
            />
          </template>
        </Column>

        <Column
            field="sizeBytes"
            header="Veličina"
            style="min-width: 8rem"
        >
          <template #body="{ data }">
            {{
              formatFileSize(
                  data.sizeBytes
              )
            }}
          </template>
        </Column>

        <Column
            field="createdAt"
            header="Dodat"
            style="min-width: 10rem"
        >
          <template #body="{ data }">
            {{
              formatDateTime(
                  data.createdAt
              )
            }}
          </template>
        </Column>

        <Column
            header="Akcije"
            style="width: 10rem; min-width: 10rem"
            body-style="text-align: center"
        >
          <template #body="{ data }">
            <div class="row-actions">
              <Button
                  v-tooltip.top="'Pregledaj dokument'"
                  icon="pi pi-eye"
                  severity="secondary"
                  text
                  rounded
                  aria-label="Pregledaj dokument"
                  @click="previewDocumentFile(data)"
              />

              <Button
                  v-tooltip.top="'Preuzmi dokument'"
                  icon="pi pi-download"
                  severity="info"
                  text
                  rounded
                  aria-label="Preuzmi dokument"
                  @click="downloadDocument(data)"
              />

              <Button
                  v-tooltip.top="'Ukloni dokument sa fakture'"
                  icon="pi pi-trash"
                  severity="danger"
                  text
                  rounded
                  aria-label="Ukloni dokument sa fakture"
                  :loading="deleting"
                  :disabled="deleting"
                  @click="confirmRemove(data)"
              />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <template #footer>
      <Button
          label="Zatvori"
          severity="secondary"
          outlined
          :disabled="
          uploading ||
          deleting ||
          previewLoading
        "
          @click="closeDialog"
      />
    </template>
  </Dialog>

  <Dialog
      :visible="previewVisible"
      :header="previewTitle"
      modal
      maximizable
      :closable="!previewLoading"
      :dismissable-mask="!previewLoading"
      :style="{ width: '85vw' }"
      :breakpoints="{
      '1024px': '96vw'
    }"
      @update:visible="
      $event
        ? previewVisible = true
        : closePreview()
    "
  >
    <div class="preview-dialog-content">
      <div
          v-if="previewLoading"
          class="preview-loading"
      >
        <i class="pi pi-spin pi-spinner" />

        <span>
          Učitavanje dokumenta...
        </span>
      </div>

      <iframe
          v-else-if="
          isPdfPreview &&
          previewUrl
        "
          :src="previewUrl"
          class="preview-frame"
          title="Pregled PDF dokumenta"
      />

      <div
          v-else-if="
          isImagePreview &&
          previewUrl
        "
          class="preview-image-wrapper"
      >
        <img
            :src="previewUrl"
            :alt="previewTitle"
            class="preview-image"
        />
      </div>

      <Message
          v-else-if="previewDocument"
          severity="warn"
          :closable="false"
      >
        Pregled nije dostupan za ovaj tip dokumenta.
        Dokument možete preuzeti pomoću dugmeta ispod.
      </Message>
    </div>

    <template #footer>
      <Button
          label="Preuzmi"
          icon="pi pi-download"
          severity="info"
          :disabled="
          previewLoading ||
          !previewDocument
        "
          @click="
          downloadDocument(
            previewDocument
          )
        "
      />

      <Button
          label="Zatvori"
          severity="secondary"
          outlined
          :disabled="previewLoading"
          @click="closePreview"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.documents-dialog {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius: var(--p-border-radius-md);
}

.upload-info h3 {
  margin: 0;
  font-size: 1rem;
}

.upload-info p {
  margin: 0.35rem 0 0;
  color: var(--p-text-muted-color);
}

.upload-actions {
  display: grid;
  grid-template-columns:
    minmax(0, 1fr)
    auto;
  align-items: end;
  gap: 1rem;
}

.display-name-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.display-name-field label {
  font-weight: 600;
}

.upload-button-field {
  display: flex;
  align-items: flex-end;
  min-height: 2.75rem;
}

.required,
.field-error {
  color: var(--p-red-500);
}

.document-name-button {
  display: inline-flex;
  align-items: center;
  gap: 0.65rem;
  max-width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.document-name-content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0.15rem;
}

.document-name-content strong,
.document-name-content small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-name-content strong {
  font-weight: 600;
}

.document-name-content small {
  color: var(--p-text-muted-color);
  font-size: 0.78rem;
}

.document-name-button:hover strong {
  text-decoration: underline;
}

.row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
}

.documents-dialog :deep(.p-datatable) {
  width: 100%;
}

.documents-dialog :deep(.p-datatable-table) {
  min-width: 60rem;
}

.preview-dialog-content {
  min-height: 72vh;
}

.preview-loading {
  display: flex;
  min-height: 72vh;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  color: var(--p-text-muted-color);
}

.preview-loading i {
  font-size: 1.5rem;
}

.preview-frame {
  display: block;
  width: 100%;
  height: 72vh;
  border: 0;
  border-radius: var(--p-border-radius-md);
  background: white;
}

.preview-image-wrapper {
  display: flex;
  min-height: 72vh;
  align-items: center;
  justify-content: center;
  overflow: auto;
  border-radius: var(--p-border-radius-md);
  background:
      var(--p-surface-100);
}

.preview-image {
  display: block;
  max-width: 100%;
  max-height: 72vh;
  object-fit: contain;
}

@media (max-width: 640px) {
  .upload-actions {
    grid-template-columns: 1fr;
  }

  .upload-button-field,
  .upload-button-field :deep(.p-fileupload),
  .upload-button-field :deep(.p-button) {
    width: 100%;
  }

  .preview-dialog-content,
  .preview-loading {
    min-height: 65vh;
  }

  .preview-frame,
  .preview-image {
    height: 65vh;
    max-height: 65vh;
  }
}
</style>
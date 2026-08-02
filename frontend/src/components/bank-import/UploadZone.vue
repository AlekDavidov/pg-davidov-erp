<script setup>
import {
  computed,
  ref
} from 'vue'
import { storeToRefs } from 'pinia'

import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'

import { useBankImportStore } from '../../stores/bankImportStore'

const bankImportStore =
    useBankImportStore()

const {
  selectedFile,
  loading
} = storeToRefs(bankImportStore)

const fileInput = ref(null)
const dragActive = ref(false)

const acceptedExtensions = [
  '.pdf'
]

const selectedFileName = computed(() =>
    selectedFile.value?.name || ''
)

const formattedFileSize = computed(() => {
  if (!selectedFile.value) {
    return ''
  }

  const size =
      selectedFile.value.size

  if (size < 1024) {
    return `${size} B`
  }

  if (size < 1024 * 1024) {
    return `${(
        size / 1024
    ).toFixed(1)} KB`
  }

  return `${(
      size /
      (1024 * 1024)
  ).toFixed(1)} MB`
})

const openFileDialog = () => {
  fileInput.value?.click()
}

const isValidFile = file => {
  if (!file) {
    return false
  }

  const filename =
      file.name.toLowerCase()

  return acceptedExtensions.some(
      extension =>
          filename.endsWith(extension)
  )
}

const selectFile = file => {
  if (!file) {
    return
  }

  if (!isValidFile(file)) {
    bankImportStore.error =
        'Trenutno je podržan samo PDF format.'

    return
  }

  bankImportStore.setFile(file)
}

const handleFileChange = event => {
  const [file] =
  event.target.files || []

  selectFile(file)

  event.target.value = ''
}

const handleDrop = event => {
  dragActive.value = false

  const [file] =
  event.dataTransfer?.files || []

  selectFile(file)
}

const handleDragEnter = () => {
  dragActive.value = true
}

const handleDragLeave = event => {
  if (
      event.currentTarget.contains(
          event.relatedTarget
      )
  ) {
    return
  }

  dragActive.value = false
}

const loadPreview = async () => {
  await bankImportStore.loadPreview()
}

const removeFile = () => {
  bankImportStore.reset()
}
</script>

<template>
  <section class="upload-panel">
    <input
        ref="fileInput"
        type="file"
        accept=".pdf,application/pdf"
        class="hidden-file-input"
        @change="handleFileChange"
    />

    <div
        class="upload-dropzone"
        :class="{
        'drag-active': dragActive,
        'has-file': selectedFile
      }"
        role="button"
        tabindex="0"
        @click="openFileDialog"
        @keydown.enter="openFileDialog"
        @keydown.space.prevent="openFileDialog"
        @dragenter.prevent="handleDragEnter"
        @dragover.prevent
        @dragleave.prevent="handleDragLeave"
        @drop.prevent="handleDrop"
    >
      <div class="upload-icon">
        <i
            :class="
            selectedFile
              ? 'pi pi-file-pdf'
              : 'pi pi-cloud-upload'
          "
        />
      </div>

      <template v-if="selectedFile">
        <strong class="upload-title">
          {{ selectedFileName }}
        </strong>

        <span class="upload-subtitle">
          {{ formattedFileSize }}
          · PDF dokument
        </span>

        <span class="upload-hint">
          Kliknite ili prevucite drugi PDF
          da biste promenili fajl.
        </span>
      </template>

      <template v-else>
        <strong class="upload-title">
          Prevucite bankarski izvod ovde
        </strong>

        <span class="upload-subtitle">
          ili kliknite da izaberete fajl
        </span>

        <span class="upload-hint">
          Trenutno je podržan AIK PDF
          format.
        </span>
      </template>
    </div>

    <div
        v-if="selectedFile"
        class="selected-file-actions"
    >
      <Button
          label="Ukloni fajl"
          icon="pi pi-trash"
          severity="secondary"
          text
          :disabled="loading"
          @click="removeFile"
      />

      <Button
          label="Obradi izvod"
          icon="pi pi-search"
          :loading="loading"
          @click="loadPreview"
      />
    </div>

    <div
        v-if="loading"
        class="preview-loading"
    >
      <ProgressSpinner
          stroke-width="4"
      />

      <div>
        <strong>
          Obrada bankarskog izvoda
        </strong>

        <span>
          Prepoznajemo banku i čitamo
          transakcije iz dokumenta.
        </span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.upload-panel {
  padding: 1.5rem;
  border: 1px solid var(--app-border);
  border-radius: 1rem;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
}

.hidden-file-input {
  display: none;
}

.upload-dropzone {
  display: flex;
  min-height: 24rem;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 0.8rem;
  padding: 2rem;
  border: 2px dashed var(--app-border);
  border-radius: 1rem;
  background: var(--app-surface-soft);
  text-align: center;
  cursor: pointer;

  transition:
      border-color 160ms ease,
      background-color 160ms ease,
      transform 160ms ease;
}

.upload-dropzone:hover,
.upload-dropzone.drag-active {
  border-color: var(--brand-blue);
  background: var(--brand-blue-soft);
}

.upload-dropzone.drag-active {
  transform: scale(1.005);
}

.upload-dropzone.has-file {
  border-style: solid;
  border-color: var(--brand-green);
}

.upload-icon {
  display: inline-flex;
  width: 5rem;
  height: 5rem;
  align-items: center;
  justify-content: center;
  border-radius: 1.4rem;
  background: var(--brand-blue-soft);
  color: var(--brand-blue);
  font-size: 2.1rem;
}

.has-file .upload-icon {
  background: var(--brand-green-soft);
  color: var(--brand-green);
}

.upload-title {
  max-width: 100%;
  overflow: hidden;
  color: var(--app-text);
  font-size: 1.15rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-subtitle {
  color: var(--app-text-muted);
}

.upload-hint {
  margin-top: 0.5rem;
  color: var(--app-text-muted);
  font-size: 0.78rem;
}

.selected-file-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.25rem;
}

.preview-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 2rem 0 0.5rem;
}

.preview-loading :deep(.p-progressspinner) {
  width: 2.5rem;
  height: 2.5rem;
}

.preview-loading > div {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.preview-loading strong {
  color: var(--app-text);
}

.preview-loading span {
  color: var(--app-text-muted);
  font-size: 0.8rem;
}

@media (max-width: 800px) {
  .upload-panel {
    padding: 1rem;
  }

  .upload-dropzone {
    min-height: 20rem;
    padding: 1.25rem;
  }

  .selected-file-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .selected-file-actions :deep(.p-button) {
    width: 100%;
  }

  .preview-loading {
    align-items: flex-start;
  }
}
</style>
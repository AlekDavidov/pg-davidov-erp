<script setup>
import {
  onBeforeUnmount,
  onMounted,
  reactive,
  ref
} from 'vue'
import { useToast } from 'primevue/usetoast'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Toast from 'primevue/toast'

import { companyProfileApi } from '../api/companyProfileApi'

const toast = useToast()

const loading = ref(false)
const saving = ref(false)
const error = ref(null)
const submitted = ref(false)

const logoLoading = ref(false)
const logoUploading = ref(false)
const logoDeleting = ref(false)
const logoUrl = ref(null)
const logoInput = ref(null)

const form = reactive({
  name: '',
  pib: '',
  registrationNumber: '',
  address: '',
  city: '',
  postalCode: '',
  phone: '',
  email: '',
  bankName: '',
  bankAccountNumber: ''
})

const resetForm = () => {
  form.name = ''
  form.pib = ''
  form.registrationNumber = ''
  form.address = ''
  form.city = ''
  form.postalCode = ''
  form.phone = ''
  form.email = ''
  form.bankName = ''
  form.bankAccountNumber = ''

  submitted.value = false
  error.value = null
}

const populateForm = profile => {
  form.name = profile.name || ''
  form.pib = profile.pib || ''
  form.registrationNumber =
      profile.registrationNumber || ''
  form.address = profile.address || ''
  form.city = profile.city || ''
  form.postalCode = profile.postalCode || ''
  form.phone = profile.phone || ''
  form.email = profile.email || ''
  form.bankName = profile.bankName || ''
  form.bankAccountNumber =
      profile.bankAccountNumber || ''

  submitted.value = false
  error.value = null
}

const revokeLogoUrl = () => {
  if (logoUrl.value) {
    window.URL.revokeObjectURL(
        logoUrl.value
    )

    logoUrl.value = null
  }
}

const loadLogo = async () => {
  logoLoading.value = true

  revokeLogoUrl()

  try {
    const blob =
        await companyProfileApi.getLogo()

    logoUrl.value =
        window.URL.createObjectURL(
            blob
        )
  } catch (loadError) {
    if (
        loadError.response?.status !== 404
    ) {
      error.value =
          loadError.response?.data?.message ||
          loadError.response?.data?.detail ||
          loadError.message ||
          'Logo nije mogao da bude učitan.'
    }
  } finally {
    logoLoading.value = false
  }
}

const loadProfile = async () => {
  loading.value = true
  error.value = null

  try {
    const profile =
        await companyProfileApi.getProfile()

    if (profile) {
      populateForm(profile)

      await loadLogo()
    } else {
      resetForm()
      revokeLogoUrl()
    }
  } catch (loadError) {
    error.value =
        loadError.response?.data?.message ||
        loadError.response?.data?.detail ||
        loadError.message ||
        'Podaci gazdinstva nisu mogli da budu učitani.'
  } finally {
    loading.value = false
  }
}

const normalizeOptionalText = value => {
  const normalizedValue = value.trim()

  return normalizedValue || null
}

const isFormValid = () =>
    Boolean(
        form.name.trim()
    )

const buildRequest = () => ({
  name: form.name.trim(),

  pib:
      normalizeOptionalText(
          form.pib
      ),

  registrationNumber:
      normalizeOptionalText(
          form.registrationNumber
      ),

  address:
      normalizeOptionalText(
          form.address
      ),

  city:
      normalizeOptionalText(
          form.city
      ),

  postalCode:
      normalizeOptionalText(
          form.postalCode
      ),

  phone:
      normalizeOptionalText(
          form.phone
      ),

  email:
      normalizeOptionalText(
          form.email
      ),

  bankName:
      normalizeOptionalText(
          form.bankName
      ),

  bankAccountNumber:
      normalizeOptionalText(
          form.bankAccountNumber
      )
})

const saveProfile = async () => {
  submitted.value = true
  error.value = null

  if (!isFormValid()) {
    return
  }

  saving.value = true

  try {
    const savedProfile =
        await companyProfileApi.save(
            buildRequest()
        )

    populateForm(
        savedProfile
    )

    toast.add({
      severity: 'success',
      summary: 'Podaci su sačuvani',
      detail:
          'Podaci gazdinstva su uspešno sačuvani.',
      life: 3000
    })
  } catch (saveError) {
    error.value =
        saveError.response?.data?.message ||
        saveError.response?.data?.detail ||
        saveError.message ||
        'Podaci gazdinstva nisu mogli da budu sačuvani.'
  } finally {
    saving.value = false
  }
}

const openLogoPicker = () => {
  logoInput.value?.click()
}

const uploadLogo = async event => {
  const file =
      event.target.files?.[0]

  event.target.value = ''

  if (!file) {
    return
  }

  const supportedTypes = [
    'image/png',
    'image/jpeg'
  ]

  if (
      !supportedTypes.includes(
          file.type
      )
  ) {
    error.value =
        'Logo mora biti PNG, JPG ili JPEG fajl.'

    return
  }

  const maxSize =
      5 * 1024 * 1024

  if (
      file.size >
      maxSize
  ) {
    error.value =
        'Logo ne može biti veći od 5 MB.'

    return
  }

  logoUploading.value = true
  error.value = null

  try {
    await companyProfileApi.uploadLogo(
        file
    )

    await loadLogo()

    toast.add({
      severity: 'success',
      summary: 'Logo je sačuvan',
      detail:
          'Logo gazdinstva je uspešno sačuvan.',
      life: 3000
    })
  } catch (uploadError) {
    error.value =
        uploadError.response?.data?.message ||
        uploadError.response?.data?.detail ||
        uploadError.message ||
        'Logo nije mogao da bude sačuvan.'
  } finally {
    logoUploading.value = false
  }
}

const deleteLogo = async () => {
  logoDeleting.value = true
  error.value = null

  try {
    await companyProfileApi.deleteLogo()

    revokeLogoUrl()

    toast.add({
      severity: 'success',
      summary: 'Logo je obrisan',
      detail:
          'Logo gazdinstva je uspešno obrisan.',
      life: 3000
    })
  } catch (deleteError) {
    error.value =
        deleteError.response?.data?.message ||
        deleteError.response?.data?.detail ||
        deleteError.message ||
        'Logo nije mogao da bude obrisan.'
  } finally {
    logoDeleting.value = false
  }
}

onMounted(
    loadProfile
)

onBeforeUnmount(
    revokeLogoUrl
)
</script>

<template>
  <div class="company-settings-view">
    <Toast />

    <div class="page-header">
      <div>
        <h2>Podešavanja</h2>

        <p>
          Osnovni podaci gazdinstva koji se koriste
          u izveštajima i dokumentima.
        </p>
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

    <Message
        v-if="loading"
        severity="info"
        :closable="false"
    >
      Učitavanje podataka gazdinstva.
    </Message>

    <div
        v-else
        class="settings-card"
    >
      <section class="settings-section">
        <div class="section-header">
          <div>
            <h3>Logo gazdinstva</h3>

            <p>
              Logo koji se koristi u izveštajima
              i dokumentima.
            </p>
          </div>
        </div>

        <div class="logo-section">
          <div class="logo-preview">
            <img
                v-if="logoUrl"
                :src="logoUrl"
                alt="Logo gazdinstva"
            />

            <div
                v-else-if="logoLoading"
                class="logo-placeholder"
            >
              <i class="pi pi-spin pi-spinner" />

              <span>
                Učitavanje...
              </span>
            </div>

            <div
                v-else
                class="logo-placeholder"
            >
              <i class="pi pi-image" />

              <span>
                Logo nije postavljen
              </span>
            </div>
          </div>

          <div class="logo-controls">
            <input
                ref="logoInput"
                type="file"
                accept=".png,.jpg,.jpeg,image/png,image/jpeg"
                class="hidden-file-input"
                @change="uploadLogo"
            />

            <Button
                :label="
                  logoUrl
                    ? 'Promeni logo'
                    : 'Dodaj logo'
                "
                icon="pi pi-upload"
                :loading="logoUploading"
                :disabled="
                  logoUploading ||
                  logoDeleting ||
                  logoLoading
                "
                @click="openLogoPicker"
            />

            <Button
                v-if="logoUrl"
                label="Obriši logo"
                icon="pi pi-trash"
                severity="danger"
                outlined
                :loading="logoDeleting"
                :disabled="
                  logoUploading ||
                  logoDeleting
                "
                @click="deleteLogo"
            />

            <small>
              PNG, JPG ili JPEG. Maksimalno 5 MB.
            </small>
          </div>
        </div>
      </section>

      <section class="settings-section">
        <div class="section-header">
          <div>
            <h3>Osnovni podaci</h3>

            <p>
              Podaci koji identifikuju gazdinstvo.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-field full-width">
            <label for="company-name">
              Naziv
              <span class="required">*</span>
            </label>

            <InputText
                id="company-name"
                v-model="form.name"
                maxlength="255"
                :invalid="
                  submitted &&
                  !form.name.trim()
                "
                :disabled="saving"
                fluid
            />

            <small
                v-if="
                  submitted &&
                  !form.name.trim()
                "
                class="field-error"
            >
              Naziv je obavezan.
            </small>
          </div>

          <div class="form-field">
            <label for="company-pib">
              PIB
            </label>

            <InputText
                id="company-pib"
                v-model="form.pib"
                maxlength="50"
                :disabled="saving"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="company-registration-number">
              Matični broj
            </label>

            <InputText
                id="company-registration-number"
                v-model="form.registrationNumber"
                maxlength="50"
                :disabled="saving"
                fluid
            />
          </div>
        </div>
      </section>

      <section class="settings-section">
        <div class="section-header">
          <div>
            <h3>Adresa i kontakt</h3>

            <p>
              Kontakt i lokacijski podaci gazdinstva.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-field full-width">
            <label for="company-address">
              Adresa
            </label>

            <InputText
                id="company-address"
                v-model="form.address"
                maxlength="255"
                :disabled="saving"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="company-city">
              Mesto
            </label>

            <InputText
                id="company-city"
                v-model="form.city"
                maxlength="120"
                :disabled="saving"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="company-postal-code">
              Poštanski broj
            </label>

            <InputText
                id="company-postal-code"
                v-model="form.postalCode"
                maxlength="20"
                :disabled="saving"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="company-phone">
              Telefon
            </label>

            <InputText
                id="company-phone"
                v-model="form.phone"
                maxlength="50"
                :disabled="saving"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="company-email">
              Email
            </label>

            <InputText
                id="company-email"
                v-model="form.email"
                type="email"
                maxlength="255"
                :disabled="saving"
                fluid
            />
          </div>
        </div>
      </section>

      <section class="settings-section">
        <div class="section-header">
          <div>
            <h3>Bankovni podaci</h3>

            <p>
              Podaci koji će se koristiti
              u finansijskim dokumentima.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-field">
            <label for="company-bank-name">
              Banka
            </label>

            <InputText
                id="company-bank-name"
                v-model="form.bankName"
                maxlength="255"
                :disabled="saving"
                fluid
            />
          </div>

          <div class="form-field">
            <label for="company-bank-account-number">
              Broj računa
            </label>

            <InputText
                id="company-bank-account-number"
                v-model="form.bankAccountNumber"
                maxlength="100"
                :disabled="saving"
                fluid
            />
          </div>
        </div>
      </section>

      <div class="form-actions">
        <Button
            label="Sačuvaj"
            icon="pi pi-save"
            :loading="saving"
            :disabled="
              saving ||
              logoUploading ||
              logoDeleting
            "
            @click="saveProfile"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.company-settings-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h2 {
  margin: 0;
  font-size: 1.75rem;
}

.page-header p {
  margin: 0.5rem 0 0;
  opacity: 0.7;
}

.settings-card {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 1.5rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-content-background);
}

.settings-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.settings-section + .settings-section {
  padding-top: 1.5rem;
  border-top: 1px solid
  var(--p-content-border-color);
}

.section-header h3 {
  margin: 0;
}

.section-header p {
  margin: 0.35rem 0 0;
  color:
      var(--p-text-muted-color);
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.logo-preview {
  display: flex;
  width: 220px;
  height: 140px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 0.75rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius:
      var(--p-border-radius-md);
  background:
      var(--p-surface-50);
}

.logo-preview img {
  display: block;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.logo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  color:
      var(--p-text-muted-color);
  text-align: center;
}

.logo-placeholder i {
  font-size: 2rem;
}

.logo-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}

.logo-controls small {
  flex-basis: 100%;
  color:
      var(--p-text-muted-color);
}

.hidden-file-input {
  display: none;
}

.form-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.full-width {
  grid-column: 1 / -1;
}

.form-field label {
  font-weight: 600;
}

.required {
  color:
      var(--p-red-500);
}

.field-error {
  color:
      var(--p-red-500);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid
  var(--p-content-border-color);
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }

  .logo-section {
    flex-direction: column;
    align-items: stretch;
  }

  .logo-preview {
    width: 100%;
  }

  .logo-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .logo-controls :deep(.p-button) {
    width: 100%;
  }

  .form-actions :deep(.p-button) {
    width: 100%;
  }
}
</style>
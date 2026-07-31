<script setup>
import {
  computed,
  reactive,
  ref,
  watch
} from 'vue'
import { storeToRefs } from 'pinia'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import ToggleSwitch from 'primevue/toggleswitch'

import { useBankAccountStore } from '../../stores/bankAccountStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },

  bankAccount: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:visible',
  'saved'
])

const bankAccountStore = useBankAccountStore()
const { saving } = storeToRefs(bankAccountStore)

const dialogError = ref(null)
const submitted = ref(false)

const currencies = [
  { label: 'RSD', value: 'RSD' },
  { label: 'EUR', value: 'EUR' },
  { label: 'USD', value: 'USD' }
]

const form = reactive({
  code: '',
  bankName: '',
  accountNumber: '',
  currencyCode: 'RSD',
  active: true
})

const isEditMode = computed(() =>
    Boolean(props.bankAccount?.id)
)

const dialogTitle = computed(() =>
    isEditMode.value
        ? 'Izmena bankovnog računa'
        : 'Novi bankovni račun'
)

const resetForm = () => {
  form.code = ''
  form.bankName = ''
  form.accountNumber = ''
  form.currencyCode = 'RSD'
  form.active = true

  submitted.value = false
  dialogError.value = null
}

const populateForm = bankAccount => {
  form.code = bankAccount.code || ''
  form.bankName = bankAccount.bankName || ''
  form.accountNumber = bankAccount.accountNumber || ''
  form.currencyCode = bankAccount.currencyCode || 'RSD'
  form.active = bankAccount.active

  submitted.value = false
  dialogError.value = null
}

const closeDialog = () => {
  if (saving.value) {
    return
  }

  emit('update:visible', false)
}

const isFormValid = () =>
    Boolean(
        form.bankName.trim() &&
        form.accountNumber.trim() &&
        form.currencyCode
    )

const buildRequest = () => ({
  bankName: form.bankName.trim(),
  accountNumber: form.accountNumber.trim(),
  currencyCode: form.currencyCode,
  active: form.active
})

const saveBankAccount = async () => {
  submitted.value = true
  dialogError.value = null

  if (!isFormValid()) {
    return
  }

  try {
    const request = buildRequest()

    if (isEditMode.value) {
      await bankAccountStore.updateBankAccount(
          props.bankAccount.id,
          request
      )
    } else {
      await bankAccountStore.createBankAccount(request)
    }

    emit('saved', {
      mode: isEditMode.value ? 'edit' : 'create',
      name:
          `${form.bankName.trim()} - ${form.accountNumber.trim()}`
    })

    emit('update:visible', false)
  } catch (error) {
    dialogError.value =
        error.message ||
        'Bankovni račun nije sačuvan.'
  }
}

watch(
    () => props.visible,
    visible => {
      if (!visible) {
        return
      }

      if (props.bankAccount) {
        populateForm(props.bankAccount)
      } else {
        resetForm()
      }
    }
)
</script>

<template>
  <Dialog
      :visible="visible"
      :header="dialogTitle"
      modal
      :closable="!saving"
      :dismissable-mask="!saving"
      :style="{ width: '40rem' }"
      :breakpoints="{
      '640px': '95vw'
    }"
      @update:visible="emit('update:visible', $event)"
  >
    <div class="bank-account-form">
      <Message
          v-if="dialogError"
          severity="error"
          :closable="false"
      >
        {{ dialogError }}
      </Message>

      <div class="form-grid">
        <div
            v-if="isEditMode"
            class="form-field"
        >
          <label for="bank-account-code">
            Šifra
          </label>

          <InputText
              id="bank-account-code"
              v-model="form.code"
              disabled
              fluid
          />

          <small class="field-help">
            Šifru automatski generiše sistem.
          </small>
        </div>

        <div class="form-field">
          <label for="bank-account-bank-name">
            Banka
            <span class="required">*</span>
          </label>

          <InputText
              id="bank-account-bank-name"
              v-model="form.bankName"
              :invalid="
              submitted &&
              !form.bankName.trim()
            "
              :disabled="saving"
              maxlength="150"
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.bankName.trim()
            "
              class="field-error"
          >
            Naziv banke je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="bank-account-number">
            Broj računa
            <span class="required">*</span>
          </label>

          <InputText
              id="bank-account-number"
              v-model="form.accountNumber"
              :invalid="
              submitted &&
              !form.accountNumber.trim()
            "
              :disabled="saving"
              maxlength="100"
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.accountNumber.trim()
            "
              class="field-error"
          >
            Broj računa je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="bank-account-currency">
            Valuta
            <span class="required">*</span>
          </label>

          <Select
              id="bank-account-currency"
              v-model="form.currencyCode"
              :options="currencies"
              option-label="label"
              option-value="value"
              placeholder="Izaberite valutu"
              :invalid="
              submitted &&
              !form.currencyCode
            "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.currencyCode
            "
              class="field-error"
          >
            Valuta je obavezna.
          </small>
        </div>

        <div class="form-field">
          <div class="active-field">
            <ToggleSwitch
                input-id="bank-account-active"
                v-model="form.active"
                :disabled="saving"
            />

            <label for="bank-account-active">
              Aktivan bankovni račun
            </label>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <Button
          label="Otkaži"
          severity="secondary"
          outlined
          :disabled="saving"
          @click="closeDialog"
      />

      <Button
          label="Sačuvaj"
          icon="pi pi-check"
          :loading="saving"
          @click="saveBankAccount"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.bank-account-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-field label {
  font-weight: 600;
}

.required,
.field-error {
  color: var(--p-red-500);
}

.field-help {
  color: var(--p-text-muted-color);
}

.active-field {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
</style>
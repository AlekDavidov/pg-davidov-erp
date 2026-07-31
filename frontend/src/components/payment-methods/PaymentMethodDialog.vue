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
import ToggleSwitch from 'primevue/toggleswitch'

import { usePaymentMethodStore } from '../../stores/paymentMethodStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },

  paymentMethod: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:visible',
  'saved'
])

const paymentMethodStore = usePaymentMethodStore()
const { saving } = storeToRefs(paymentMethodStore)

const dialogError = ref(null)
const submitted = ref(false)

const form = reactive({
  code: '',
  name: '',
  active: true
})

const isEditMode = computed(() =>
    Boolean(props.paymentMethod?.id)
)

const dialogTitle = computed(() =>
    isEditMode.value
        ? 'Izmena načina plaćanja'
        : 'Novi način plaćanja'
)

const resetForm = () => {
  form.code = ''
  form.name = ''
  form.active = true

  submitted.value = false
  dialogError.value = null
}

const populateForm = paymentMethod => {
  form.code = paymentMethod.code || ''
  form.name = paymentMethod.name || ''
  form.active = paymentMethod.active

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
    Boolean(form.name.trim())

const buildRequest = () => ({
  name: form.name.trim(),
  active: form.active
})

const savePaymentMethod = async () => {
  submitted.value = true
  dialogError.value = null

  if (!isFormValid()) {
    return
  }

  try {
    const request = buildRequest()

    if (isEditMode.value) {
      await paymentMethodStore.updatePaymentMethod(
          props.paymentMethod.id,
          request
      )
    } else {
      await paymentMethodStore.createPaymentMethod(
          request
      )
    }

    emit('saved', {
      mode: isEditMode.value
          ? 'edit'
          : 'create',
      name: form.name.trim()
    })

    emit('update:visible', false)
  } catch (error) {
    dialogError.value =
        error.message ||
        'Način plaćanja nije sačuvan.'
  }
}

watch(
    () => props.visible,
    visible => {
      if (!visible) {
        return
      }

      if (props.paymentMethod) {
        populateForm(props.paymentMethod)
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
      :style="{ width: '36rem' }"
      :breakpoints="{
      '640px': '95vw'
    }"
      @update:visible="emit('update:visible', $event)"
  >
    <div class="payment-method-form">
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
          <label for="payment-method-code">
            Šifra
          </label>

          <InputText
              id="payment-method-code"
              v-model="form.code"
              disabled
              fluid
          />

          <small class="field-help">
            Šifru automatski generiše sistem.
          </small>
        </div>

        <div class="form-field">
          <label for="payment-method-name">
            Naziv
            <span class="required">*</span>
          </label>

          <InputText
              id="payment-method-name"
              v-model="form.name"
              :invalid="
              submitted &&
              !form.name.trim()
            "
              :disabled="saving"
              maxlength="100"
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
          <div class="active-field">
            <ToggleSwitch
                input-id="payment-method-active"
                v-model="form.active"
                :disabled="saving"
            />

            <label for="payment-method-active">
              Aktivan način plaćanja
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
          @click="savePaymentMethod"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.payment-method-form {
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
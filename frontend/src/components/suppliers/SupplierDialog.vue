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
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import ToggleSwitch from 'primevue/toggleswitch'

import { categoryApi } from '../../api/categoryApi'
import { paymentMethodApi } from '../../api/paymentMethodApi'
import { useSupplierStore } from '../../stores/supplierStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },

  supplier: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:visible',
  'saved'
])

const supplierStore = useSupplierStore()
const { saving } = storeToRefs(supplierStore)

const categories = ref([])
const paymentMethods = ref([])
const optionsLoading = ref(false)
const dialogError = ref(null)
const submitted = ref(false)

const form = reactive({
  name: '',
  defaultCategoryId: null,
  paymentMethodId: null,
  paymentTerms: 0,
  pib: '',
  registrationNumber: '',
  phone: '',
  email: '',
  contactPerson: '',
  notes: '',
  active: true
})

const isEditMode = computed(() =>
    Boolean(props.supplier?.id)
)

const dialogTitle = computed(() =>
    isEditMode.value
        ? 'Izmena dobavljača'
        : 'Novi dobavljač'
)

const availableCategories = computed(() =>
    categories.value.filter(category =>
        category.active ||
        category.id === form.defaultCategoryId
    )
)

const availablePaymentMethods = computed(() =>
    paymentMethods.value.filter(method =>
        method.active ||
        method.id === form.paymentMethodId
    )
)

const resetForm = () => {
  form.name = ''
  form.defaultCategoryId = null
  form.paymentMethodId = null
  form.paymentTerms = 0
  form.pib = ''
  form.registrationNumber = ''
  form.phone = ''
  form.email = ''
  form.contactPerson = ''
  form.notes = ''
  form.active = true

  submitted.value = false
  dialogError.value = null
}

const populateForm = supplier => {
  form.name = supplier.name || ''
  form.defaultCategoryId =
      supplier.defaultCategoryId || null
  form.paymentMethodId =
      supplier.paymentMethodId || null
  form.paymentTerms =
      supplier.paymentTerms ?? 0
  form.pib = supplier.pib || ''
  form.registrationNumber =
      supplier.registrationNumber || ''
  form.phone = supplier.phone || ''
  form.email = supplier.email || ''
  form.contactPerson =
      supplier.contactPerson || ''
  form.notes = supplier.notes || ''
  form.active = supplier.active

  submitted.value = false
  dialogError.value = null
}

const loadOptions = async () => {
  optionsLoading.value = true
  dialogError.value = null

  try {
    const [
      categoryResponse,
      paymentMethodResponse
    ] = await Promise.all([
      categoryApi.findAll(),
      paymentMethodApi.findAll()
    ])

    categories.value =
        categoryResponse.items || []

    paymentMethods.value =
        paymentMethodResponse || []
  } catch (error) {
    categories.value = []
    paymentMethods.value = []

    dialogError.value =
        error.message ||
        'Podaci za padajuće liste nisu učitani.'
  } finally {
    optionsLoading.value = false
  }
}

const closeDialog = () => {
  if (saving.value) {
    return
  }

  emit('update:visible', false)
}

const isFormValid = () =>
    Boolean(
        form.name.trim() &&
        form.defaultCategoryId &&
        form.paymentMethodId &&
        form.paymentTerms !== null &&
        form.paymentTerms >= 0
    )

const normalizeOptionalText = value => {
  const normalizedValue = value.trim()

  return normalizedValue || null
}

const buildRequest = () => ({
  name: form.name.trim(),
  defaultCategoryId: form.defaultCategoryId,
  paymentMethodId: form.paymentMethodId,
  paymentTerms: form.paymentTerms,
  pib: normalizeOptionalText(form.pib),
  registrationNumber: normalizeOptionalText(
      form.registrationNumber
  ),
  phone: normalizeOptionalText(form.phone),
  email: normalizeOptionalText(form.email),
  contactPerson: normalizeOptionalText(
      form.contactPerson
  ),
  notes: normalizeOptionalText(form.notes),
  active: form.active
})

const saveSupplier = async () => {
  submitted.value = true
  dialogError.value = null

  if (!isFormValid()) {
    return
  }

  try {
    const request = buildRequest()

    if (isEditMode.value) {
      await supplierStore.updateSupplier(
          props.supplier.id,
          request
      )
    } else {
      await supplierStore.createSupplier(request)
    }

    emit('saved', {
      mode: isEditMode.value ? 'edit' : 'create',
      name: form.name.trim()
    })

    emit('update:visible', false)
  } catch (error) {
    dialogError.value =
        error.message ||
        'Dobavljač nije sačuvan.'
  }
}

watch(
    () => props.visible,
    async visible => {
      if (!visible) {
        return
      }

      if (props.supplier) {
        populateForm(props.supplier)
      } else {
        resetForm()
      }

      await loadOptions()
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
      :style="{ width: '58rem' }"
      :breakpoints="{
      '960px': '85vw',
      '640px': '95vw'
    }"
      @update:visible="emit('update:visible', $event)"
  >
    <div class="supplier-form">
      <Message
          v-if="dialogError"
          severity="error"
          :closable="false"
      >
        {{ dialogError }}
      </Message>

      <Message
          v-if="optionsLoading"
          severity="info"
          :closable="false"
      >
        Učitavanje kategorija i načina plaćanja...
      </Message>

      <div class="form-grid">
        <div
            v-if="isEditMode"
            class="form-field"
        >
          <label for="supplier-code">
            Šifra
          </label>

          <InputText
              id="supplier-code"
              :model-value="supplier?.code || ''"
              disabled
              fluid
          />

          <small class="field-help">
            Šifra se automatski generiše i ne može se menjati.
          </small>
        </div>

        <div class="form-field">
          <label for="supplier-name">
            Naziv
            <span class="required">*</span>
          </label>

          <InputText
              id="supplier-name"
              v-model="form.name"
              :invalid="submitted && !form.name.trim()"
              :disabled="saving"
              fluid
          />

          <small
              v-if="submitted && !form.name.trim()"
              class="field-error"
          >
            Naziv je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="supplier-category">
            Podrazumevana kategorija
            <span class="required">*</span>
          </label>

          <Select
              id="supplier-category"
              v-model="form.defaultCategoryId"
              :options="availableCategories"
              option-label="name"
              option-value="id"
              placeholder="Izaberite kategoriju"
              filter
              show-clear
              :loading="optionsLoading"
              :disabled="saving || optionsLoading"
              :invalid="
              submitted &&
              !form.defaultCategoryId
            "
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.defaultCategoryId
            "
              class="field-error"
          >
            Kategorija je obavezna.
          </small>
        </div>

        <div class="form-field">
          <label for="supplier-payment-method">
            Način plaćanja
            <span class="required">*</span>
          </label>

          <Select
              id="supplier-payment-method"
              v-model="form.paymentMethodId"
              :options="availablePaymentMethods"
              option-label="name"
              option-value="id"
              placeholder="Izaberite način plaćanja"
              filter
              show-clear
              :loading="optionsLoading"
              :disabled="saving || optionsLoading"
              :invalid="
              submitted &&
              !form.paymentMethodId
            "
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.paymentMethodId
            "
              class="field-error"
          >
            Način plaćanja je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="supplier-payment-terms">
            Rok plaćanja (dana)
            <span class="required">*</span>
          </label>

          <InputNumber
              id="supplier-payment-terms"
              v-model="form.paymentTerms"
              :min="0"
              :max="3650"
              placeholder="30"
              :invalid="
              submitted &&
              (
                form.paymentTerms === null ||
                form.paymentTerms < 0
              )
            "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
              submitted &&
              (
                form.paymentTerms === null ||
                form.paymentTerms < 0
              )
            "
              class="field-error"
          >
            Rok plaćanja je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="supplier-pib">
            PIB
          </label>

          <InputText
              id="supplier-pib"
              v-model="form.pib"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="supplier-registration-number">
            Matični broj
          </label>

          <InputText
              id="supplier-registration-number"
              v-model="form.registrationNumber"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="supplier-contact-person">
            Kontakt osoba
          </label>

          <InputText
              id="supplier-contact-person"
              v-model="form.contactPerson"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="supplier-phone">
            Telefon
          </label>

          <InputText
              id="supplier-phone"
              v-model="form.phone"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="supplier-email">
            Email
          </label>

          <InputText
              id="supplier-email"
              v-model="form.email"
              type="email"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field form-field-full">
          <label for="supplier-notes">
            Napomena
          </label>

          <Textarea
              id="supplier-notes"
              v-model="form.notes"
              rows="3"
              auto-resize
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field form-field-full">
          <div class="active-field">
            <ToggleSwitch
                input-id="supplier-active"
                v-model="form.active"
                :disabled="saving"
            />

            <label for="supplier-active">
              Aktivan dobavljač
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
          :disabled="optionsLoading"
          @click="saveSupplier"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.supplier-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 1rem 1.25rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-field-full {
  grid-column: 1 / -1;
}

.form-field label {
  font-weight: 600;
}

.required,
.field-error {
  color: var(--p-red-500);
}

.field-help {
  opacity: 0.65;
}

.active-field {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-field-full {
    grid-column: auto;
  }
}
</style>
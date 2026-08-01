<script setup>
import {
  computed,
  reactive,
  ref,
  watch
} from 'vue'
import { storeToRefs } from 'pinia'

import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'

import { supplierApi } from '../../api/supplierApi'
import { useInvoiceStore } from '../../stores/invoiceStore'

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
  'saved'
])

const invoiceStore = useInvoiceStore()
const { saving } = storeToRefs(invoiceStore)

const suppliers = ref([])
const optionsLoading = ref(false)
const dialogError = ref(null)
const submitted = ref(false)

const currencies = [
  {
    label: 'RSD',
    value: 'RSD'
  },
  {
    label: 'EUR',
    value: 'EUR'
  },
  {
    label: 'USD',
    value: 'USD'
  }
]

const form = reactive({
  invoiceCode: '',
  invoiceNumber: '',
  supplierId: null,
  invoiceDate: null,
  dueDate: null,
  amount: null,
  currencyCode: 'RSD',
  notes: ''
})

const isEditMode = computed(() =>
    Boolean(props.invoice?.id)
)

const dialogTitle = computed(() =>
    isEditMode.value
        ? 'Izmena fakture'
        : 'Nova faktura'
)

const availableSuppliers = computed(() =>
    suppliers.value.filter(supplier =>
        supplier.active ||
        supplier.id === form.supplierId
    )
)

const dueDateInvalid = computed(() => {
  if (!form.invoiceDate || !form.dueDate) {
    return false
  }

  return form.dueDate < form.invoiceDate
})

const resetForm = () => {
  form.invoiceCode = ''
  form.invoiceNumber = ''
  form.supplierId = null
  form.invoiceDate = new Date()
  form.dueDate = null
  form.amount = null
  form.currencyCode = 'RSD'
  form.notes = ''

  submitted.value = false
  dialogError.value = null
}

const parseDate = value => {
  if (!value) {
    return null
  }

  const [year, month, day] = value
      .split('-')
      .map(Number)

  return new Date(
      year,
      month - 1,
      day
  )
}

const formatDateForRequest = value => {
  if (!value) {
    return null
  }

  const year = value.getFullYear()
  const month = String(
      value.getMonth() + 1
  ).padStart(2, '0')
  const day = String(
      value.getDate()
  ).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const populateForm = invoice => {
  form.invoiceCode =
      invoice.invoiceCode || ''
  form.invoiceNumber =
      invoice.invoiceNumber || ''
  form.supplierId =
      invoice.supplierId || null
  form.invoiceDate =
      parseDate(invoice.invoiceDate)
  form.dueDate =
      parseDate(invoice.dueDate)
  form.amount =
      invoice.amount ?? null
  form.currencyCode =
      invoice.currencyCode || 'RSD'
  form.notes =
      invoice.notes || ''

  submitted.value = false
  dialogError.value = null
}

const loadSuppliers = async () => {
  optionsLoading.value = true
  dialogError.value = null

  try {
    const response = await supplierApi.findAll({
      page: 0,
      size: 1000,
      sortBy: 'name',
      direction: 'asc'
    })

    suppliers.value = response.items || []
  } catch (error) {
    suppliers.value = []
    dialogError.value =
        error.message ||
        'Dobavljači nisu mogli da budu učitani.'
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
        form.invoiceNumber.trim() &&
        form.supplierId &&
        form.invoiceDate &&
        form.amount !== null &&
        form.amount > 0 &&
        form.currencyCode &&
        !dueDateInvalid.value
    )

const buildRequest = () => ({
  invoiceNumber:
      form.invoiceNumber.trim(),
  supplierId:
  form.supplierId,
  invoiceDate:
      formatDateForRequest(form.invoiceDate),
  dueDate:
      formatDateForRequest(form.dueDate),
  amount:
  form.amount,
  currencyCode:
  form.currencyCode,
  notes:
      form.notes.trim() || null
})

const saveInvoice = async () => {
  submitted.value = true
  dialogError.value = null

  if (!isFormValid()) {
    return
  }

  try {
    const request = buildRequest()

    if (isEditMode.value) {
      await invoiceStore.updateInvoice(
          props.invoice.id,
          request
      )
    } else {
      await invoiceStore.createInvoice(request)
    }

    emit('saved', {
      mode: isEditMode.value
          ? 'edit'
          : 'create',
      invoiceNumber:
          form.invoiceNumber.trim()
    })

    emit('update:visible', false)
  } catch (error) {
    dialogError.value =
        error.message ||
        'Faktura nije sačuvana.'
  }
}

watch(
    () => props.visible,
    async visible => {
      if (!visible) {
        return
      }

      if (props.invoice) {
        populateForm(props.invoice)
      } else {
        resetForm()
      }

      await loadSuppliers()
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
      :style="{ width: '46rem' }"
      :breakpoints="{
      '768px': '95vw'
    }"
      @update:visible="
      emit('update:visible', $event)
    "
  >
    <div class="invoice-form">
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
            class="form-field full-width"
        >
          <label for="invoice-code">
            Šifra
          </label>

          <InputText
              id="invoice-code"
              v-model="form.invoiceCode"
              disabled
              fluid
          />

          <small class="field-help">
            Šifru automatski generiše sistem.
          </small>
        </div>

        <div class="form-field">
          <label for="invoice-number">
            Broj fakture
            <span class="required">*</span>
          </label>

          <InputText
              id="invoice-number"
              v-model="form.invoiceNumber"
              :invalid="
              submitted &&
              !form.invoiceNumber.trim()
            "
              :disabled="saving"
              maxlength="150"
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.invoiceNumber.trim()
            "
              class="field-error"
          >
            Broj fakture je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="invoice-supplier">
            Dobavljač
            <span class="required">*</span>
          </label>

          <Select
              id="invoice-supplier"
              v-model="form.supplierId"
              :options="availableSuppliers"
              option-label="name"
              option-value="id"
              placeholder="Izaberite dobavljača"
              filter
              :loading="optionsLoading"
              :invalid="
              submitted &&
              !form.supplierId
            "
              :disabled="
              saving ||
              optionsLoading
            "
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.supplierId
            "
              class="field-error"
          >
            Dobavljač je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="invoice-date">
            Datum fakture
            <span class="required">*</span>
          </label>

          <DatePicker
              id="invoice-date"
              v-model="form.invoiceDate"
              date-format="dd.mm.yy"
              show-icon
              :manual-input="false"
              :invalid="
              submitted &&
              !form.invoiceDate
            "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.invoiceDate
            "
              class="field-error"
          >
            Datum fakture je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="invoice-due-date">
            Datum dospeća
          </label>

          <DatePicker
              id="invoice-due-date"
              v-model="form.dueDate"
              date-format="dd.mm.yy"
              show-icon
              show-button-bar
              :manual-input="false"
              :min-date="form.invoiceDate"
              :invalid="
              submitted &&
              dueDateInvalid
            "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
              submitted &&
              dueDateInvalid
            "
              class="field-error"
          >
            Datum dospeća ne može biti pre datuma fakture.
          </small>
        </div>

        <div class="form-field">
          <label for="invoice-amount">
            Iznos
            <span class="required">*</span>
          </label>

          <InputNumber
              id="invoice-amount"
              v-model="form.amount"
              mode="decimal"
              locale="sr-RS"
              :min="0.01"
              :min-fraction-digits="2"
              :max-fraction-digits="2"
              :invalid="
              submitted &&
              (
                form.amount === null ||
                form.amount <= 0
              )
            "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
              submitted &&
              (
                form.amount === null ||
                form.amount <= 0
              )
            "
              class="field-error"
          >
            Iznos mora biti veći od nule.
          </small>
        </div>

        <div class="form-field">
          <label for="invoice-currency">
            Valuta
            <span class="required">*</span>
          </label>

          <Select
              id="invoice-currency"
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

        <div class="form-field full-width">
          <label for="invoice-notes">
            Napomena
          </label>

          <Textarea
              id="invoice-notes"
              v-model="form.notes"
              rows="4"
              maxlength="2000"
              auto-resize
              :disabled="saving"
              fluid
          />

          <small class="field-help">
            Maksimalno 2.000 karaktera.
          </small>
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
          @click="saveInvoice"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.invoice-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
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
  gap: 0.4rem;
}

.full-width {
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
  color: var(--p-text-muted-color);
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }
}
</style>
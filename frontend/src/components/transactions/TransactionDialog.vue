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
import ToggleSwitch from 'primevue/toggleswitch'

import { bankAccountApi } from '../../api/bankAccountApi'
import { categoryApi } from '../../api/categoryApi'
import { supplierApi } from '../../api/supplierApi'
import { useTransactionStore } from '../../stores/transactionStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },

  transaction: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:visible',
  'saved'
])

const transactionStore = useTransactionStore()
const { saving } = storeToRefs(transactionStore)

const suppliers = ref([])
const categories = ref([])
const bankAccounts = ref([])

const optionsLoading = ref(false)
const dialogError = ref(null)
const submitted = ref(false)

const transactionTypes = [
  {
    label: 'Rashod',
    value: 'EXPENSE'
  },
  {
    label: 'Prihod',
    value: 'INCOME'
  }
]

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

const statuses = [
  {
    label: 'Nova',
    value: 'NEW'
  },
  {
    label: 'Proverena',
    value: 'VERIFIED'
  },
  {
    label: 'Povezana',
    value: 'MATCHED'
  },
  {
    label: 'Stornirana',
    value: 'CANCELLED'
  }
]

const sources = [
  {
    label: 'Ručno',
    value: 'MANUAL'
  },
  {
    label: 'Bankovni izvod',
    value: 'BANK_IMPORT'
  },
  {
    label: 'Migracija',
    value: 'MIGRATION'
  }
]

const form = reactive({
  transactionCode: '',
  transactionDate: null,
  transactionType: 'EXPENSE',
  amount: null,
  currencyCode: 'RSD',
  description: '',
  rawCounterparty: '',
  bankAccountId: null,
  supplierId: null,
  categoryId: null,
  bankStatementRowId: null,
  reference: '',
  status: 'NEW',
  source: 'MANUAL',
  verified: false,
  notes: ''
})

const isEditMode = computed(() =>
    Boolean(props.transaction?.id)
)

const dialogTitle = computed(() =>
    isEditMode.value
        ? 'Izmena transakcije'
        : 'Nova transakcija'
)

const availableSuppliers = computed(() =>
    suppliers.value.filter(supplier =>
        supplier.active ||
        supplier.id === form.supplierId
    )
)

const availableCategories = computed(() => {
  const expectedCategoryType =
      form.transactionType === 'EXPENSE'
          ? 'EXPENSE'
          : 'INCOME'

  return categories.value.filter(category =>
      (
          category.active ||
          category.id === form.categoryId
      ) &&
      (
          category.categoryType === expectedCategoryType ||
          category.includeInFinancialReport === false
      )
  )
})

const availableBankAccounts = computed(() =>
    bankAccounts.value.filter(bankAccount =>
        bankAccount.active ||
        bankAccount.id === form.bankAccountId
    )
)

const resetForm = () => {
  form.transactionCode = ''
  form.transactionDate = new Date()
  form.transactionType = 'EXPENSE'
  form.amount = null
  form.currencyCode = 'RSD'
  form.description = ''
  form.rawCounterparty = ''
  form.bankAccountId = null
  form.supplierId = null
  form.categoryId = null
  form.bankStatementRowId = null
  form.reference = ''
  form.status = 'NEW'
  form.source = 'MANUAL'
  form.verified = false
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

const populateForm = transaction => {
  const debit = Number(transaction.debit || 0)
  const credit = Number(transaction.credit || 0)

  form.transactionCode =
      transaction.transactionCode || ''

  form.transactionDate =
      parseDate(transaction.transactionDate)

  form.transactionType =
      debit > 0
          ? 'EXPENSE'
          : 'INCOME'

  form.amount =
      debit > 0
          ? debit
          : credit

  form.currencyCode =
      transaction.currencyCode || 'RSD'

  form.description =
      transaction.description || ''

  form.rawCounterparty =
      transaction.rawCounterparty || ''

  form.bankAccountId =
      transaction.bankAccountId || null

  form.supplierId =
      transaction.supplierId || null

  form.categoryId =
      transaction.categoryId || null

  form.bankStatementRowId =
      transaction.bankStatementRowId || null

  form.reference =
      transaction.reference || ''

  form.status =
      transaction.status || 'NEW'

  form.source =
      transaction.source || 'MANUAL'

  form.verified =
      Boolean(transaction.verified)

  form.notes =
      transaction.notes || ''

  submitted.value = false
  dialogError.value = null
}

const loadOptions = async () => {
  optionsLoading.value = true
  dialogError.value = null

  try {
    const [
      supplierResponse,
      categoryResponse,
      bankAccountResponse
    ] = await Promise.all([
      supplierApi.findAll({
        page: 0,
        size: 1000,
        sortBy: 'name',
        direction: 'asc'
      }),
      categoryApi.findAll({
        page: 0,
        size: 1000,
        sortBy: 'name',
        direction: 'asc'
      }),
      bankAccountApi.findAll()
    ])

    suppliers.value =
        supplierResponse.items || []

    categories.value =
        categoryResponse.items || []

    bankAccounts.value =
        bankAccountResponse || []
  } catch (error) {
    suppliers.value = []
    categories.value = []
    bankAccounts.value = []

    dialogError.value =
        error.message ||
        'Podaci za unos transakcije nisu mogli da budu učitani.'
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
        form.transactionDate &&
        form.transactionType &&
        form.amount !== null &&
        Number(form.amount) > 0 &&
        form.currencyCode &&
        form.status &&
        form.source
    )

const buildRequest = () => {
  const amount = Number(form.amount)

  return {
    transactionDate:
        formatDateForRequest(form.transactionDate),

    currencyCode:
    form.currencyCode,

    debit:
        form.transactionType === 'EXPENSE'
            ? amount
            : 0,

    credit:
        form.transactionType === 'INCOME'
            ? amount
            : 0,

    description:
        form.description.trim() || null,

    rawCounterparty:
        form.rawCounterparty.trim() || null,

    bankAccountId:
    form.bankAccountId,

    supplierId:
    form.supplierId,

    categoryId:
    form.categoryId,

    bankStatementRowId:
    form.bankStatementRowId,

    reference:
        form.reference.trim() || null,

    status:
    form.status,

    source:
    form.source,

    verified:
    form.verified,

    notes:
        form.notes.trim() || null
  }
}

const saveTransaction = async () => {
  submitted.value = true
  dialogError.value = null

  if (!isFormValid()) {
    return
  }

  try {
    const request = buildRequest()

    if (isEditMode.value) {
      await transactionStore.updateTransaction(
          props.transaction.id,
          request
      )
    } else {
      await transactionStore.createTransaction(
          request
      )
    }

    emit('saved', {
      mode:
          isEditMode.value
              ? 'edit'
              : 'create',

      description:
          form.description.trim() ||
          form.rawCounterparty.trim() ||
          'Transakcija'
    })

    emit('update:visible', false)
  } catch (error) {
    dialogError.value =
        error.message ||
        'Transakcija nije sačuvana.'
  }
}

watch(
    () => props.visible,
    async visible => {
      if (!visible) {
        return
      }

      if (props.transaction) {
        populateForm(props.transaction)
      } else {
        resetForm()
      }

      await loadOptions()
    }
)

watch(
    () => form.transactionType,
    () => {
      const selectedCategory =
          categories.value.find(category =>
              category.id === form.categoryId
          )

      if (!selectedCategory) {
        return
      }

      const expectedType =
          form.transactionType === 'EXPENSE'
              ? 'EXPENSE'
              : 'INCOME'

      if (
          selectedCategory.categoryType !==
          expectedType &&
          selectedCategory.includeInFinancialReport !== false
      ) {
        form.categoryId = null
      }
    }
)

watch(
    () => form.supplierId,
    supplierId => {
      if (!supplierId) {
        return
      }

      const selectedSupplier =
          suppliers.value.find(supplier =>
              supplier.id === supplierId
          )

      if (!selectedSupplier?.defaultCategoryId) {
        return
      }

      const defaultCategory =
          categories.value.find(category =>
              category.id ===
              selectedSupplier.defaultCategoryId
          )

      if (!defaultCategory) {
        return
      }

      const expectedType =
          form.transactionType === 'EXPENSE'
              ? 'EXPENSE'
              : 'INCOME'

      if (
          defaultCategory.categoryType ===
          expectedType ||
          defaultCategory.includeInFinancialReport === false
      ) {
        form.categoryId =
            selectedSupplier.defaultCategoryId
      }
    }
)

watch(
    () => form.bankAccountId,
    bankAccountId => {
      if (!bankAccountId) {
        return
      }

      const selectedBankAccount =
          bankAccounts.value.find(bankAccount =>
              bankAccount.id === bankAccountId
          )

      if (!selectedBankAccount?.currencyCode) {
        return
      }

      form.currencyCode =
          selectedBankAccount.currencyCode
    }
)

watch(
    () => form.status,
    status => {
      if (status === 'VERIFIED') {
        form.verified = true
      }

      if (status === 'NEW') {
        form.verified = false
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
      :style="{ width: '54rem' }"
      :breakpoints="{
      '900px': '95vw'
    }"
      @update:visible="
      emit('update:visible', $event)
    "
  >
    <div class="transaction-form">
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
          <label for="transaction-code">
            Šifra
          </label>

          <InputText
              id="transaction-code"
              v-model="form.transactionCode"
              disabled
              fluid
          />

          <small class="field-help">
            Šifru automatski generiše sistem.
          </small>
        </div>

        <div class="form-field">
          <label for="transaction-date">
            Datum
            <span class="required">*</span>
          </label>

          <DatePicker
              id="transaction-date"
              v-model="form.transactionDate"
              date-format="dd.mm.yy"
              show-icon
              :manual-input="false"
              :invalid="
              submitted &&
              !form.transactionDate
            "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.transactionDate
            "
              class="field-error"
          >
            Datum je obavezan.
          </small>
        </div>

        <div class="form-field">
          <label for="transaction-type">
            Tip transakcije
            <span class="required">*</span>
          </label>

          <Select
              id="transaction-type"
              v-model="form.transactionType"
              :options="transactionTypes"
              option-label="label"
              option-value="value"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-amount">
            Iznos
            <span class="required">*</span>
          </label>

          <InputNumber
              id="transaction-amount"
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
          <label for="transaction-currency">
            Valuta
            <span class="required">*</span>
          </label>

          <Select
              id="transaction-currency"
              v-model="form.currencyCode"
              :options="currencies"
              option-label="label"
              option-value="value"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field full-width">
          <label for="transaction-description">
            Opis
          </label>

          <InputText
              id="transaction-description"
              v-model="form.description"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-counterparty">
            Naziv dobavljača sa izvoda
          </label>

          <InputText
              id="transaction-counterparty"
              v-model="form.rawCounterparty"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-reference">
            Referenca
          </label>

          <InputText
              id="transaction-reference"
              v-model="form.reference"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-bank-account">
            Bankovni račun
          </label>

          <Select
              id="transaction-bank-account"
              v-model="form.bankAccountId"
              :options="availableBankAccounts"
              option-label="bankName"
              option-value="id"
              placeholder="Izaberite bankovni račun"
              show-clear
              filter
              :loading="optionsLoading"
              :disabled="saving || optionsLoading"
              fluid
          >
            <template #option="{ option }">
              {{ option.bankName }}
              — {{ option.accountNumber }}
              — {{ option.currencyCode }}
            </template>

            <template #value="{ value, placeholder }">
              <span v-if="value">
                {{
                  availableBankAccounts.find(
                      account =>
                          account.id === value
                  )?.bankName
                }}
              </span>

              <span v-else>
                {{ placeholder }}
              </span>
            </template>
          </Select>
        </div>

        <div class="form-field">
          <label for="transaction-supplier">
            Dobavljač
          </label>

          <Select
              id="transaction-supplier"
              v-model="form.supplierId"
              :options="availableSuppliers"
              option-label="name"
              option-value="id"
              placeholder="Izaberite dobavljača"
              show-clear
              filter
              :loading="optionsLoading"
              :disabled="saving || optionsLoading"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-category">
            Kategorija
          </label>

          <Select
              id="transaction-category"
              v-model="form.categoryId"
              :options="availableCategories"
              option-label="name"
              option-value="id"
              placeholder="Izaberite kategoriju"
              show-clear
              filter
              :loading="optionsLoading"
              :disabled="saving || optionsLoading"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-status">
            Status
            <span class="required">*</span>
          </label>

          <Select
              id="transaction-status"
              v-model="form.status"
              :options="statuses"
              option-label="label"
              option-value="value"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <label for="transaction-source">
            Izvor
            <span class="required">*</span>
          </label>

          <Select
              id="transaction-source"
              v-model="form.source"
              :options="sources"
              option-label="label"
              option-value="value"
              :disabled="saving"
              fluid
          />
        </div>

        <div class="form-field">
          <div class="verified-field">
            <ToggleSwitch
                input-id="transaction-verified"
                v-model="form.verified"
                :disabled="saving"
            />

            <label for="transaction-verified">
              Proverena transakcija
            </label>
          </div>
        </div>

        <div class="form-field full-width">
          <label for="transaction-notes">
            Napomena
          </label>

          <Textarea
              id="transaction-notes"
              v-model="form.notes"
              rows="4"
              auto-resize
              :disabled="saving"
              fluid
          />
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
          @click="saveTransaction"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.transaction-form {
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

.verified-field {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-height: 2.75rem;
}

@media (max-width: 700px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }
}
</style>
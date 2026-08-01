<script setup>
import {
  computed,
  reactive,
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
import InputNumber from 'primevue/inputnumber'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Tooltip from 'primevue/tooltip'

import { invoiceApi } from '../../api/invoiceApi'
import { transactionApi } from '../../api/transactionApi'
import {
  formatAmount,
  formatDate,
  formatDateTime
} from '../../utils/formatters'

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

const payments = ref([])
const transactions = ref([])
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const error = ref(null)
const submitted = ref(false)

const form = reactive({
  transactionId: null,
  amount: null
})

const dialogTitle = computed(() => {
  if (!props.invoice) {
    return 'Uplate fakture'
  }

  return `Uplate fakture ${props.invoice.invoiceNumber}`
})

const linkedTransactionIds = computed(() =>
    new Set(
        payments.value.map(payment =>
            payment.transactionId
        )
    )
)

const availableTransactions = computed(() => {
  if (!props.invoice) {
    return []
  }

  return transactions.value
      .filter(transaction =>
          transaction.debit !== null &&
          transaction.debit !== undefined &&
          Number(transaction.debit) > 0
      )
      .filter(transaction =>
          transaction.currencyCode ===
          props.invoice.currencyCode
      )
      .filter(transaction =>
          !linkedTransactionIds.value.has(
              transaction.id
          )
      )
})

const selectedTransaction = computed(() =>
    transactions.value.find(transaction =>
        transaction.id === form.transactionId
    ) || null
)

const selectedTransactionAmount = computed(() => {
  if (!selectedTransaction.value) {
    return null
  }

  return Number(selectedTransaction.value.debit)
})

const transactionLabel = transaction => {
  const parts = [
    transaction.transactionCode
  ]

  const description =
      transaction.description ||
      transaction.rawCounterparty

  if (description) {
    parts.push(description)
  } else if (transaction.transactionDate) {
    parts.push(
        formatDate(transaction.transactionDate)
    )
  }

  parts.push(
      formatAmount(
          transaction.debit,
          transaction.currencyCode
      )
  )

  return parts.join(' • ')
}

const findTransaction = transactionId =>
    transactions.value.find(transaction =>
        transaction.id === transactionId
    ) || null

const getPaymentTransactionCode = payment => {
  const transaction =
      findTransaction(payment.transactionId)

  return transaction?.transactionCode ||
      payment.transactionId ||
      '—'
}

const getPaymentTransactionDescription = payment => {
  const transaction =
      findTransaction(payment.transactionId)

  if (!transaction) {
    return null
  }

  return (
      transaction.description ||
      transaction.rawCounterparty ||
      null
  )
}

const getPaymentTransactionDate = payment => {
  const transaction =
      findTransaction(payment.transactionId)

  return transaction?.transactionDate || null
}

const resetForm = () => {
  form.transactionId = null
  form.amount = null
  submitted.value = false
}

const loadData = async () => {
  if (!props.invoice?.id) {
    return
  }

  loading.value = true
  error.value = null

  try {
    const [
      loadedPayments,
      loadedTransactions
    ] = await Promise.all([
      invoiceApi.getPayments(
          props.invoice.id
      ),
      transactionApi.findAll()
    ])

    payments.value =
        Array.isArray(loadedPayments)
            ? loadedPayments
            : []

    transactions.value =
        Array.isArray(loadedTransactions)
            ? loadedTransactions
            : []
  } catch (loadError) {
    payments.value = []
    transactions.value = []

    error.value =
        loadError.message ||
        'Uplate i transakcije nisu mogle da budu učitane.'
  } finally {
    loading.value = false
  }
}

const amountExceedsTransaction = computed(() => {
  if (
      form.amount === null ||
      selectedTransactionAmount.value === null
  ) {
    return false
  }

  return (
      Number(form.amount) >
      selectedTransactionAmount.value
  )
})

const isFormValid = () =>
    Boolean(
        form.transactionId &&
        form.amount !== null &&
        Number(form.amount) > 0 &&
        !amountExceedsTransaction.value
    )

const attachPayment = async () => {
  submitted.value = true
  error.value = null

  if (!isFormValid()) {
    return
  }

  saving.value = true

  try {
    await invoiceApi.attachPayment(
        props.invoice.id,
        {
          transactionId:
          form.transactionId,
          amount:
              Number(form.amount)
        }
    )

    toast.add({
      severity: 'success',
      summary: 'Uplata je povezana',
      detail:
          `Povezano ${formatAmount(
              form.amount,
              props.invoice.currencyCode
          )}`,
      life: 3000
    })

    resetForm()
    await loadData()
    emit('changed')
  } catch (saveError) {
    error.value =
        saveError.message ||
        'Uplata nije mogla da bude povezana.'
  } finally {
    saving.value = false
  }
}

const confirmDetach = payment => {
  const transactionCode =
      getPaymentTransactionCode(payment)

  confirm.require({
    header: 'Uklanjanje uplate',
    message:
        `Da li ste sigurni da želite da uklonite vezu sa transakcijom „${transactionCode}“?`,
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Otkaži',
    acceptLabel: 'Ukloni vezu',
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
        await invoiceApi.detachPayment(
            props.invoice.id,
            payment.id
        )

        toast.add({
          severity: 'success',
          summary: 'Veza sa uplatom je uklonjena',
          detail:
              `${transactionCode} — ${formatAmount(
                  payment.amount,
                  props.invoice.currencyCode
              )}`,
          life: 3000
        })

        await loadData()
        emit('changed')
      } catch (deleteError) {
        error.value =
            deleteError.message ||
            'Veza sa uplatom nije mogla da bude uklonjena.'
      } finally {
        deleting.value = false
      }
    }
  })
}

const closeDialog = () => {
  if (saving.value || deleting.value) {
    return
  }

  emit('update:visible', false)
}

watch(
    () => props.visible,
    async visible => {
      if (!visible) {
        return
      }

      resetForm()
      await loadData()
    }
)

watch(
    selectedTransaction,
    transaction => {
      if (!transaction) {
        form.amount = null
        return
      }

      form.amount =
          Number(transaction.debit)
    }
)
</script>

<template>
  <Dialog
      :visible="visible"
      :header="dialogTitle"
      modal
      :closable="!saving && !deleting"
      :dismissable-mask="!saving && !deleting"
      :style="{ width: '64rem' }"
      :breakpoints="{
      '1024px': '95vw'
    }"
      @update:visible="
      emit('update:visible', $event)
    "
  >
    <ConfirmDialog />

    <div class="payments-dialog">
      <Message
          v-if="error"
          severity="error"
          closable
          @close="error = null"
      >
        {{ error }}
      </Message>

      <div class="invoice-summary">
        <div>
          <span>Ukupan iznos</span>

          <strong>
            {{
              formatAmount(
                  invoice?.amount,
                  invoice?.currencyCode
              )
            }}
          </strong>
        </div>

        <div>
          <span>Plaćeno</span>

          <strong>
            {{
              formatAmount(
                  invoice?.paidAmount,
                  invoice?.currencyCode
              )
            }}
          </strong>
        </div>

        <div>
          <span>Preostalo</span>

          <strong>
            {{
              formatAmount(
                  invoice?.remainingAmount,
                  invoice?.currencyCode
              )
            }}
          </strong>
        </div>
      </div>

      <div class="payment-form">
        <div class="form-field transaction-field">
          <label for="payment-transaction">
            Transakcija
            <span class="required">*</span>
          </label>

          <Select
              id="payment-transaction"
              v-model="form.transactionId"
              :options="availableTransactions"
              option-value="id"
              :option-label="transactionLabel"
              placeholder="Izaberite rashodnu transakciju"
              filter
              :loading="loading"
              :disabled="loading || saving"
              :invalid="
              submitted &&
              !form.transactionId
            "
              fluid
          />

          <small
              v-if="
              submitted &&
              !form.transactionId
            "
              class="field-error"
          >
            Transakcija je obavezna.
          </small>

          <small
              v-if="
              !loading &&
              availableTransactions.length === 0
            "
              class="field-help"
          >
            Nema dostupnih rashodnih transakcija u valuti fakture.
          </small>
        </div>

        <div class="form-field amount-field">
          <label for="payment-amount">
            Iznos
            <span class="required">*</span>
          </label>

          <InputNumber
              id="payment-amount"
              v-model="form.amount"
              mode="decimal"
              locale="sr-RS"
              :min="0.01"
              :max="selectedTransactionAmount"
              :min-fraction-digits="2"
              :max-fraction-digits="2"
              :disabled="
              saving ||
              !form.transactionId
            "
              :invalid="
              submitted &&
              (
                form.amount === null ||
                form.amount <= 0 ||
                amountExceedsTransaction
              )
            "
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

          <small
              v-else-if="
              submitted &&
              amountExceedsTransaction
            "
              class="field-error"
          >
            Iznos ne može biti veći od iznosa transakcije.
          </small>

          <small
              v-else-if="selectedTransactionAmount !== null"
              class="field-help"
          >
            Maksimalno:
            {{
              formatAmount(
                  selectedTransactionAmount,
                  invoice?.currencyCode
              )
            }}
          </small>
        </div>

        <div class="button-field">
          <Button
              label="Poveži uplatu"
              icon="pi pi-link"
              :loading="saving"
              :disabled="
              loading ||
              availableTransactions.length === 0
            "
              @click="attachPayment"
          />
        </div>
      </div>

      <DataTable
          :value="payments"
          :loading="loading"
          data-key="id"
          striped-rows
          responsive-layout="scroll"
      >
        <template #empty>
          Faktura nema povezanih uplata.
        </template>

        <Column
            header="Transakcija"
            style="min-width: 14rem"
        >
          <template #body="{ data }">
            <div class="transaction-info">
              <strong>
                {{ getPaymentTransactionCode(data) }}
              </strong>

              <small
                  v-if="getPaymentTransactionDescription(data)"
              >
                {{ getPaymentTransactionDescription(data) }}
              </small>
            </div>
          </template>
        </Column>

        <Column
            header="Datum transakcije"
            style="min-width: 10rem"
        >
          <template #body="{ data }">
            {{
              formatDate(
                  getPaymentTransactionDate(data)
              )
            }}
          </template>
        </Column>

        <Column
            field="amount"
            header="Povezani iznos"
            style="min-width: 10rem"
        >
          <template #body="{ data }">
            {{
              formatAmount(
                  data.amount,
                  invoice?.currencyCode
              )
            }}
          </template>
        </Column>

        <Column
            field="createdAt"
            header="Povezana"
            style="min-width: 10rem"
        >
          <template #body="{ data }">
            {{ formatDateTime(data.createdAt) }}
          </template>
        </Column>

        <Column
            header="Akcije"
            style="width: 7rem; min-width: 7rem"
            body-style="text-align: center"
        >
          <template #body="{ data }">
            <Button
                v-tooltip.top="'Ukloni vezu sa uplatom'"
                icon="pi pi-unlink"
                severity="danger"
                outlined
                rounded
                aria-label="Ukloni vezu sa uplatom"
                :loading="deleting"
                :disabled="deleting"
                @click="confirmDetach(data)"
            />
          </template>
        </Column>
      </DataTable>
    </div>

    <template #footer>
      <Button
          label="Zatvori"
          severity="secondary"
          outlined
          :disabled="saving || deleting"
          @click="closeDialog"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.payments-dialog {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.invoice-summary {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.invoice-summary div {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 1rem;
  border: 1px solid
  var(--p-content-border-color);
  border-radius: var(--p-border-radius-md);
}

.invoice-summary span {
  opacity: 0.7;
}

.invoice-summary strong {
  font-size: 1.1rem;
}

.payment-form {
  display: grid;
  grid-template-columns:
    minmax(0, 1fr)
    13rem
    auto;
  align-items: start;
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

.button-field {
  display: flex;
  align-items: flex-start;
  padding-top: 1.9rem;
}

.button-field :deep(.p-button) {
  min-height: 2.75rem;
  white-space: nowrap;
}

.required,
.field-error {
  color: var(--p-red-500);
}

.field-help {
  min-height: 1.1rem;
  color: var(--p-text-muted-color);
}

.transaction-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.transaction-info strong {
  font-weight: 600;
}

.transaction-info small {
  max-width: 20rem;
  overflow: hidden;
  color: var(--p-text-muted-color);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.payments-dialog :deep(.p-datatable) {
  width: 100%;
}

.payments-dialog :deep(.p-datatable-table) {
  min-width: 52rem;
}

@media (max-width: 768px) {
  .invoice-summary,
  .payment-form {
    grid-template-columns: 1fr;
  }

  .button-field {
    padding-top: 0;
  }

  .button-field :deep(.p-button) {
    width: 100%;
  }
}
</style>
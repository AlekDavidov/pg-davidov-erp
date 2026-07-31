<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'

import Button from 'primevue/button'
import Column from 'primevue/column'
import ConfirmDialog from 'primevue/confirmdialog'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import Tag from 'primevue/tag'
import Tooltip from 'primevue/tooltip'

import { usePaymentMethodStore } from '../../stores/paymentMethodStore'

const vTooltip = Tooltip

const emit = defineEmits([
  'create',
  'edit'
])

const paymentMethodStore = usePaymentMethodStore()
const confirm = useConfirm()
const toast = useToast()

const {
  paymentMethods,
  loading,
  deleting,
  error
} = storeToRefs(paymentMethodStore)

const loadPaymentMethods = async () => {
  await paymentMethodStore.fetchPaymentMethods()
}

const formatDateTime = value => {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('sr-RS', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(new Date(value))
}

const confirmDelete = paymentMethod => {
  confirm.require({
    header: 'Brisanje načina plaćanja',
    message:
        `Da li ste sigurni da želite da obrišete način plaćanja „${paymentMethod.name}“?`,
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Otkaži',
    acceptLabel: 'Obriši',
    rejectProps: {
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      severity: 'danger'
    },
    accept: async () => {
      try {
        await paymentMethodStore.deletePaymentMethod(
            paymentMethod.id
        )

        toast.add({
          severity: 'success',
          summary: 'Način plaćanja je obrisan',
          detail: paymentMethod.name,
          life: 3000
        })
      } catch {
        toast.add({
          severity: 'error',
          summary: 'Brisanje nije uspelo',
          detail:
              'Način plaćanja nije mogao da bude obrisan.',
          life: 4000
        })
      }
    }
  })
}

onMounted(loadPaymentMethods)
</script>

<template>
  <div class="payment-method-table">
    <ConfirmDialog />

    <Message
        v-if="error"
        severity="error"
        closable
        @close="paymentMethodStore.clearError()"
    >
      {{ error }}
    </Message>

    <div class="table-toolbar">
      <Button
          label="Novi način plaćanja"
          icon="pi pi-plus"
          @click="emit('create')"
      />

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadPaymentMethods"
      />
    </div>

    <DataTable
        :value="paymentMethods"
        :loading="loading"
        data-key="id"
        striped-rows
        removable-sort
        @row-dblclick="emit('edit', $event.data)"
    >
      <template #empty>
        Nema pronađenih načina plaćanja.
      </template>

      <Column
          field="code"
          header="Šifra"
          sortable
      />

      <Column
          field="name"
          header="Naziv"
          sortable
      >
        <template #body="{ data }">
          <button
              class="payment-method-name-button"
              type="button"
              @click="emit('edit', data)"
          >
            {{ data.name }}
          </button>
        </template>
      </Column>

      <Column
          field="active"
          header="Status"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="data.active ? 'Aktivan' : 'Neaktivan'"
              :severity="
                data.active
                    ? 'success'
                    : 'secondary'
              "
          />
        </template>
      </Column>

      <Column
          field="createdAt"
          header="Kreiran"
          sortable
      >
        <template #body="{ data }">
          {{ formatDateTime(data.createdAt) }}
        </template>
      </Column>

      <Column header="Akcije">
        <template #body="{ data }">
          <div class="row-actions">
            <Button
                v-tooltip.top="'Izmeni način plaćanja'"
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                aria-label="Izmeni način plaćanja"
                @click="emit('edit', data)"
            />

            <Button
                v-tooltip.top="'Obriši način plaćanja'"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                aria-label="Obriši način plaćanja"
                :disabled="deleting"
                @click="confirmDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
.payment-method-table {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.payment-method-name-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.payment-method-name-button:hover {
  text-decoration: underline;
}

.payment-method-table :deep(.p-datatable) {
  width: 100%;
}
</style>
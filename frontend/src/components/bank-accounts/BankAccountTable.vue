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

import { useBankAccountStore } from '../../stores/bankAccountStore'

const vTooltip = Tooltip

const emit = defineEmits([
  'create',
  'edit'
])

const bankAccountStore = useBankAccountStore()
const confirm = useConfirm()
const toast = useToast()

const {
  bankAccounts,
  loading,
  deleting,
  error
} = storeToRefs(bankAccountStore)

const loadBankAccounts = async () => {
  await bankAccountStore.fetchBankAccounts()
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

const confirmDelete = bankAccount => {
  confirm.require({
    header: 'Brisanje bankovnog računa',
    message:
        `Da li ste sigurni da želite da obrišete račun „${bankAccount.bankName} - ${bankAccount.accountNumber}“?`,
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
        await bankAccountStore.deleteBankAccount(
            bankAccount.id
        )

        toast.add({
          severity: 'success',
          summary: 'Bankovni račun je obrisan',
          detail:
              `${bankAccount.bankName} - ${bankAccount.accountNumber}`,
          life: 3000
        })
      } catch {
        toast.add({
          severity: 'error',
          summary: 'Brisanje nije uspelo',
          detail:
              'Bankovni račun nije mogao da bude obrisan.',
          life: 4000
        })
      }
    }
  })
}

onMounted(loadBankAccounts)
</script>

<template>
  <div class="bank-account-table">
    <ConfirmDialog />

    <Message
        v-if="error"
        severity="error"
        closable
        @close="bankAccountStore.clearError()"
    >
      {{ error }}
    </Message>

    <div class="table-toolbar">
      <Button
          label="Novi bankovni račun"
          icon="pi pi-plus"
          @click="emit('create')"
      />

      <Button
          label="Osveži"
          icon="pi pi-refresh"
          severity="secondary"
          outlined
          :loading="loading"
          @click="loadBankAccounts"
      />
    </div>

    <DataTable
        :value="bankAccounts"
        :loading="loading"
        data-key="id"
        striped-rows
        removable-sort
        @row-dblclick="emit('edit', $event.data)"
    >
      <template #empty>
        Nema pronađenih bankovnih računa.
      </template>

      <Column
          field="code"
          header="Šifra"
          sortable
      />

      <Column
          field="bankName"
          header="Banka"
          sortable
      >
        <template #body="{ data }">
          <button
              class="bank-name-button"
              type="button"
              @click="emit('edit', data)"
          >
            {{ data.bankName }}
          </button>
        </template>
      </Column>

      <Column
          field="accountNumber"
          header="Broj računa"
          sortable
      />

      <Column
          field="currencyCode"
          header="Valuta"
          sortable
      >
        <template #body="{ data }">
          <Tag
              :value="data.currencyCode"
              severity="info"
          />
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
                v-tooltip.top="'Izmeni bankovni račun'"
                icon="pi pi-pencil"
                severity="secondary"
                text
                rounded
                aria-label="Izmeni bankovni račun"
                @click="emit('edit', data)"
            />

            <Button
                v-tooltip.top="'Obriši bankovni račun'"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                aria-label="Obriši bankovni račun"
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
.bank-account-table {
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

.bank-name-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.bank-name-button:hover {
  text-decoration: underline;
}

.bank-account-table :deep(.p-datatable) {
  width: 100%;
}
</style>
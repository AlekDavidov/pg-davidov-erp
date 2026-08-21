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

import { useCategoryStore } from '../../stores/categoryStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },

  category: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:visible',
  'saved'
])

const categoryStore = useCategoryStore()
const { saving } = storeToRefs(categoryStore)

const dialogError = ref(null)
const submitted = ref(false)

const categoryTypes = [
  {
    label: 'Prihod',
    value: 'INCOME'
  },
  {
    label: 'Rashod',
    value: 'EXPENSE'
  }
]

const form = reactive({
  name: '',
  categoryType: null,
  active: true,
  includeInFinancialReport: true
})

const isEditMode = computed(() =>
    Boolean(props.category?.id)
)

const dialogTitle = computed(() =>
    isEditMode.value
        ? 'Izmena kategorije'
        : 'Nova kategorija'
)

const resetForm = () => {
  form.name = ''
  form.categoryType = null
  form.active = true
  form.includeInFinancialReport = true

  submitted.value = false
  dialogError.value = null
}

const populateForm = category => {
  form.name = category.name || ''
  form.categoryType = category.categoryType || null
  form.active = category.active
  form.includeInFinancialReport =
      category.includeInFinancialReport ?? true

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
        form.name.trim() &&
        form.categoryType
    )

const buildRequest = () => ({
  name: form.name.trim(),
  categoryType: form.categoryType,
  active: form.active,
  includeInFinancialReport:
  form.includeInFinancialReport
})

const saveCategory = async () => {
  submitted.value = true
  dialogError.value = null

  if (!isFormValid()) {
    return
  }

  try {
    const request = buildRequest()

    if (isEditMode.value) {
      await categoryStore.updateCategory(
          props.category.id,
          request
      )
    } else {
      await categoryStore.createCategory(request)
    }

    emit('saved', {
      mode: isEditMode.value ? 'edit' : 'create',
      name: form.name.trim()
    })

    emit('update:visible', false)
  } catch (error) {
    dialogError.value =
        error.message ||
        'Kategorija nije sačuvana.'
  }
}

watch(
    () => props.visible,
    visible => {
      if (!visible) {
        return
      }

      if (props.category) {
        populateForm(props.category)
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
    <div class="category-form">
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
          <label for="category-code">
            Šifra
          </label>

          <InputText
              id="category-code"
              :model-value="category?.code || ''"
              disabled
              fluid
          />

          <small class="field-help">
            Šifra se automatski generiše i ne može se menjati.
          </small>
        </div>

        <div class="form-field">
          <label for="category-name">
            Naziv
            <span class="required">*</span>
          </label>

          <InputText
              id="category-name"
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
          <label for="category-type">
            Tip kategorije
            <span class="required">*</span>
          </label>

          <Select
              id="category-type"
              v-model="form.categoryType"
              :options="categoryTypes"
              option-label="label"
              option-value="value"
              placeholder="Izaberite tip"
              :invalid="
                submitted &&
                !form.categoryType
              "
              :disabled="saving"
              fluid
          />

          <small
              v-if="
                submitted &&
                !form.categoryType
              "
              class="field-error"
          >
            Tip kategorije je obavezan.
          </small>
        </div>

        <div class="form-field">
          <div class="active-field">
            <ToggleSwitch
                input-id="category-active"
                v-model="form.active"
                :disabled="saving"
            />

            <label for="category-active">
              Aktivna kategorija
            </label>
          </div>
        </div>

        <div class="form-field">
          <div class="active-field">
            <ToggleSwitch
                input-id="category-financial-report"
                v-model="form.includeInFinancialReport"
                :disabled="saving"
            />

            <label for="category-financial-report">
              Uključi u finansijske izveštaje
            </label>
          </div>

          <small class="field-help">
            Isključite za kategorije kao što su interni prenosi
            između sopstvenih računa.
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
          @click="saveCategory"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.category-form {
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
  opacity: 0.65;
}

.active-field {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
</style>
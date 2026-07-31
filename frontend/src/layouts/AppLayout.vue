<script setup>
import { ref } from 'vue'

const dark = ref(true)

const menu = [
  ['pi-home', 'Dashboard', '/'],
  ['pi-wallet', 'Transactions', '/transactions'],
  ['pi-file', 'Invoices', '/invoices'],
  ['pi-folder', 'Documents', '/documents'],
  ['pi-building', 'Suppliers', '/suppliers'],
  ['pi-tags', 'Categories', '/categories'],
  ['pi-credit-card', 'Payment Methods', '/payment-methods'],
  ['pi-building-columns', 'Bank Accounts', '/bank-accounts'],
  ['pi-upload', 'Bank Import', '/bank-import'],
  ['pi-chart-bar', 'Reports', '/reports'],
  ['pi-cog', 'Settings', '/settings']
]
</script>

<template>
  <div
      class="shell"
      :class="{ dark }"
  >
    <aside>
      <h1>PG Davidov ERP</h1>

      <nav>
        <RouterLink
            v-for="item in menu"
            :key="item[2]"
            :to="item[2]"
        >
          <i :class="`pi ${item[0]}`" />

          {{ item[1] }}
        </RouterLink>
      </nav>
    </aside>

    <main>
      <header>
        <span>PG Davidov</span>

        <button @click="dark = !dark">
          <i
              :class="`
                pi
                ${dark ? 'pi-sun' : 'pi-moon'}
              `"
          />
        </button>
      </header>

      <section>
        <RouterView />
      </section>
    </main>
  </div>
</template>
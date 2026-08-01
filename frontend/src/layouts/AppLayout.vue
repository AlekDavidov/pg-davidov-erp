<script setup>
import {
  onMounted,
  ref,
  watch
} from 'vue'

import logoUrl from '../assets/pg-davidov-logo.png'

const THEME_STORAGE_KEY =
    'pg-davidov-erp-theme'

const dark = ref(false)
const mobileMenuVisible = ref(false)

const menu = [
  {
    icon: 'pi-home',
    label: 'Dashboard',
    path: '/'
  },
  {
    icon: 'pi-wallet',
    label: 'Transactions',
    path: '/transactions'
  },
  {
    icon: 'pi-file',
    label: 'Invoices',
    path: '/invoices'
  },
  {
    icon: 'pi-folder',
    label: 'Documents',
    path: '/documents'
  },
  {
    icon: 'pi-building',
    label: 'Suppliers',
    path: '/suppliers'
  },
  {
    icon: 'pi-tags',
    label: 'Categories',
    path: '/categories'
  },
  {
    icon: 'pi-credit-card',
    label: 'Payment Methods',
    path: '/payment-methods'
  },
  {
    icon: 'pi-building-columns',
    label: 'Bank Accounts',
    path: '/bank-accounts'
  },
  {
    icon: 'pi-upload',
    label: 'Bank Import',
    path: '/bank-import'
  },
  {
    icon: 'pi-chart-bar',
    label: 'Reports',
    path: '/reports'
  },
  {
    icon: 'pi-cog',
    label: 'Settings',
    path: '/settings'
  }
]

const applyTheme = isDark => {
  document.documentElement.classList.toggle(
      'app-dark',
      isDark
  )

  document.documentElement.style.colorScheme =
      isDark
          ? 'dark'
          : 'light'
}

const toggleTheme = () => {
  dark.value = !dark.value
}

const closeMobileMenu = () => {
  mobileMenuVisible.value = false
}

onMounted(() => {
  const savedTheme =
      window.localStorage.getItem(
          THEME_STORAGE_KEY
      )

  dark.value = savedTheme === 'dark'

  applyTheme(dark.value)
})

watch(
    dark,
    isDark => {
      applyTheme(isDark)

      window.localStorage.setItem(
          THEME_STORAGE_KEY,
          isDark
              ? 'dark'
              : 'light'
      )
    }
)
</script>

<template>
  <div
      class="app-shell"
      :class="{
      'app-shell-dark': dark,
      'mobile-menu-open': mobileMenuVisible
    }"
  >
    <div
        v-if="mobileMenuVisible"
        class="sidebar-backdrop"
        @click="closeMobileMenu"
    />

    <aside class="app-sidebar">
      <div class="brand">
        <img
            :src="logoUrl"
            alt="Poljoprivredno gazdinstvo Davidov"
            class="brand-logo"
        />

        <div class="brand-text">
          <strong>PG Davidov</strong>
          <span>ERP sistem</span>
        </div>
      </div>

      <div class="sidebar-divider" />

      <nav
          class="app-navigation"
          aria-label="Glavna navigacija"
      >
        <RouterLink
            v-for="item in menu"
            :key="item.path"
            :to="item.path"
            class="navigation-link"
            @click="closeMobileMenu"
        >
          <span class="navigation-icon">
            <i
                :class="[
                'pi',
                item.icon
              ]"
            />
          </span>

          <span>
            {{ item.label }}
          </span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div class="farm-badge">
          <span class="farm-badge-icon">
            <i class="pi pi-sparkles" />
          </span>

          <span>
            Poljoprivredno gazdinstvo
          </span>
        </div>
      </div>
    </aside>

    <main class="app-main">
      <header class="app-header">
        <div class="header-left">
          <button
              type="button"
              class="mobile-menu-button"
              aria-label="Otvori navigaciju"
              @click="
              mobileMenuVisible =
                !mobileMenuVisible
            "
          >
            <i class="pi pi-bars" />
          </button>

          <div class="header-title">
            <strong>PG Davidov</strong>
            <span>Poslovni informacioni sistem</span>
          </div>
        </div>

        <div class="header-actions">
          <span class="header-status">
            <span class="status-dot" />
            Sistem aktivan
          </span>

          <button
              type="button"
              class="theme-toggle"
              :aria-label="
              dark
                ? 'Uključi svetlu temu'
                : 'Uključi tamnu temu'
            "
              :title="
              dark
                ? 'Svetla tema'
                : 'Tamna tema'
            "
              @click="toggleTheme"
          >
            <i
                :class="[
                'pi',
                dark
                  ? 'pi-sun'
                  : 'pi-moon'
              ]"
            />
          </button>
        </div>
      </header>

      <section class="app-content">
        <RouterView />
      </section>
    </main>
  </div>
</template>
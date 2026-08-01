<script setup>
import {
  onMounted,
  ref,
  watch
} from 'vue'

import logoUrl from '../assets/pg-davidov-logo.png'

const THEME_STORAGE_KEY =
    'pg-davidov-erp-theme'

const SIDEBAR_STORAGE_KEY =
    'pg-davidov-erp-sidebar-collapsed'

const dark = ref(false)
const sidebarCollapsed = ref(false)
const mobileMenuVisible = ref(false)

const menu = [
  {
    icon: 'pi-home',
    label: 'Pregled poslovanja',
    path: '/'
  },
  {
    icon: 'pi-wallet',
    label: 'Transakcije',
    path: '/transactions'
  },
  {
    icon: 'pi-file',
    label: 'Fakture',
    path: '/invoices'
  },
  {
    icon: 'pi-folder',
    label: 'Dokumenti',
    path: '/documents'
  },
  {
    icon: 'pi-building',
    label: 'Dobavljači',
    path: '/suppliers'
  },
  {
    icon: 'pi-tags',
    label: 'Kategorije',
    path: '/categories'
  },
  {
    icon: 'pi-credit-card',
    label: 'Načini plaćanja',
    path: '/payment-methods'
  },
  {
    icon: 'pi-building-columns',
    label: 'Bankovni računi',
    path: '/bank-accounts'
  },
  {
    icon: 'pi-upload',
    label: 'Uvoz izvoda',
    path: '/bank-import'
  },
  {
    icon: 'pi-chart-bar',
    label: 'Izveštaji',
    path: '/reports'
  },
  {
    icon: 'pi-cog',
    label: 'Podešavanja',
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

const toggleSidebar = () => {
  sidebarCollapsed.value =
      !sidebarCollapsed.value
}

const closeMobileMenu = () => {
  mobileMenuVisible.value = false
}

onMounted(() => {
  const savedTheme =
      window.localStorage.getItem(
          THEME_STORAGE_KEY
      )

  const savedSidebarState =
      window.localStorage.getItem(
          SIDEBAR_STORAGE_KEY
      )

  dark.value =
      savedTheme === 'dark'

  sidebarCollapsed.value =
      savedSidebarState === 'true'

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

watch(
    sidebarCollapsed,
    collapsed => {
      window.localStorage.setItem(
          SIDEBAR_STORAGE_KEY,
          String(collapsed)
      )
    }
)
</script>

<template>
  <div
      class="app-shell"
      :class="{
      'app-shell-dark': dark,
      'sidebar-collapsed': sidebarCollapsed,
      'mobile-menu-open': mobileMenuVisible
    }"
      :style="{
      '--sidebar-width':
        sidebarCollapsed
          ? '88px'
          : '272px'
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

      <button
          type="button"
          class="sidebar-toggle"
          :aria-label="
          sidebarCollapsed
            ? 'Proširi navigaciju'
            : 'Skupi navigaciju'
        "
          :title="
          sidebarCollapsed
            ? 'Proširi meni'
            : 'Skupi meni'
        "
          @click="toggleSidebar"
      >
        <i
            :class="[
            'pi',
            sidebarCollapsed
              ? 'pi-angle-double-right'
              : 'pi-angle-double-left'
          ]"
        />
      </button>

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
            :title="
            sidebarCollapsed
              ? item.label
              : undefined
          "
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

          <span class="navigation-label">
            {{ item.label }}
          </span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div class="farm-badge">
          <span class="farm-badge-icon">
            <i class="pi pi-sparkles" />
          </span>

          <span class="farm-badge-label">
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

            <span>
              Poslovni informacioni sistem
            </span>
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

<style scoped>
.app-shell {
  transition:
      grid-template-columns 180ms ease;
}

.app-sidebar {
  position: sticky;
  overflow: visible;

  transition:
      width 180ms ease,
      padding 180ms ease;
}

.brand {
  position: relative;

  padding-right: 3rem;

  transition:
      padding 180ms ease,
      justify-content 180ms ease;
}

.brand-logo {
  transition:
      width 180ms ease,
      height 180ms ease,
      flex-basis 180ms ease;
}

.brand-text,
.navigation-label,
.farm-badge-label {
  overflow: hidden;
  white-space: nowrap;

  transition:
      opacity 120ms ease,
      width 180ms ease;
}

.app-navigation {
  overflow-x: hidden;
  overflow-y: auto;
}

.sidebar-toggle {
  position: absolute;
  top: 38px;
  right: 16px;
  z-index: 60;

  display: inline-flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;

  padding: 0;

  border: 2px solid
  rgb(255 255 255 / 42%);
  border-radius: 12px;

  background:
      linear-gradient(
          135deg,
          var(--brand-blue),
          var(--brand-blue-hover)
      );

  color: #ffffff;
  font-size: 1rem;

  box-shadow:
      0 7px 18px
      rgb(0 0 0 / 34%),
      inset 0 1px 0
      rgb(255 255 255 / 20%);

  cursor: pointer;

  transition:
      border-color 150ms ease,
      background 150ms ease,
      box-shadow 150ms ease,
      color 150ms ease,
      transform 150ms ease;
}

.sidebar-toggle:hover {
  border-color: #f3b451;

  background:
      linear-gradient(
          135deg,
          var(--brand-gold),
          #b96e0e
      );

  box-shadow:
      0 9px 22px
      rgb(0 0 0 / 40%);

  transform: translateY(-1px);
}

.sidebar-toggle:active {
  transform: translateY(0);
}

.sidebar-toggle:focus-visible {
  outline: 3px solid
  rgb(217 138 25 / 40%);
  outline-offset: 3px;
}

.sidebar-toggle i {
  font-size: 1rem;
  font-weight: 700;
}

.sidebar-collapsed .app-sidebar {
  padding-right: 10px;
  padding-left: 10px;
}

.sidebar-collapsed .brand {
  justify-content: center;

  min-height: 112px;

  padding-right: 0;
  padding-left: 0;
}

.sidebar-collapsed .brand-logo {
  width: 54px;
  height: 54px;
  flex-basis: 54px;
}

.sidebar-collapsed .brand-text,
.sidebar-collapsed .navigation-label,
.sidebar-collapsed .farm-badge-label {
  width: 0;
  opacity: 0;
  pointer-events: none;
}

.sidebar-collapsed .sidebar-toggle {
  top: 82px;
  right: 25px;

  width: 38px;
  height: 32px;

  border-radius: 10px;
}

.sidebar-collapsed .navigation-link {
  justify-content: center;
  gap: 0;

  padding-right: 0;
  padding-left: 0;
}

.sidebar-collapsed .navigation-icon {
  width: 100%;
}

.sidebar-collapsed
.navigation-link.router-link-active::before {
  left: -1px;
}

.sidebar-collapsed .sidebar-footer {
  padding-right: 0;
  padding-left: 0;
}

.sidebar-collapsed .farm-badge {
  justify-content: center;
  gap: 0;

  padding-right: 0;
  padding-left: 0;
}

@media (max-width: 800px) {
  .sidebar-toggle {
    display: none;
  }

  .sidebar-collapsed .app-sidebar {
    width: min(
        82vw,
        290px
    );

    padding: 16px 14px;
  }

  .sidebar-collapsed .brand {
    justify-content: flex-start;

    min-height: 82px;

    padding:
        6px
        8px
        12px;
  }

  .sidebar-collapsed .brand-logo {
    width: 64px;
    height: 64px;
    flex-basis: 64px;
  }

  .sidebar-collapsed .brand-text,
  .sidebar-collapsed .navigation-label,
  .sidebar-collapsed .farm-badge-label {
    width: auto;
    opacity: 1;
    pointer-events: auto;
  }

  .sidebar-collapsed .navigation-link {
    justify-content: flex-start;
    gap: 12px;

    padding: 8px 12px;
  }

  .sidebar-collapsed .navigation-icon {
    width: 24px;
  }

  .sidebar-collapsed .farm-badge {
    justify-content: flex-start;
    gap: 9px;

    padding: 10px 11px;
  }
}
</style>
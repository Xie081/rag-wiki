<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="toast-item"
          :class="'toast-' + t.type"
          @click="remove(t.id)"
        >
          <span class="toast-icon">
            {{ t.type === 'success' ? '✓' : t.type === 'error' ? '✕' : 'ℹ' }}
          </span>
          <span class="toast-msg">{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 380px;
}
.toast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.toast-success { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; }
.toast-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.toast-info { background: #dbeafe; color: #1e40af; border: 1px solid #bfdbfe; }
.toast-icon { font-weight: 700; font-size: 1rem; flex-shrink: 0; }
.toast-msg { flex: 1; }

.toast-enter-active { transition: all 0.3s ease-out; }
.toast-leave-active { transition: all 0.2s ease-in; }
.toast-enter-from { transform: translateX(100%); opacity: 0; }
.toast-leave-to { transform: translateX(100%); opacity: 0; }
</style>

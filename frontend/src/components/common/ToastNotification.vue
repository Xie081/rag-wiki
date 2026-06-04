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
            <template v-if="t.type === 'success'">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
            </template>
            <template v-else-if="t.type === 'error'">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            </template>
            <template v-else>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
            </template>
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
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 380px;
}
.toast-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-radius: var(--radius);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  border: 1px solid;
  backdrop-filter: blur(12px);
  animation: fadeIn 0.3s var(--ease);
}
.toast-success { background: #ecf7e7; color: #3d6b32; border-color: #c3e6b8; }
.toast-error   { background: #fef0ed; color: #933b2e; border-color: #f5c6bd; }
.toast-info    { background: #eef3f7; color: #3d5b76; border-color: #c3d8ec; }
.toast-icon { flex-shrink: 0; }
.toast-msg  { flex: 1; }

.toast-enter-active { transition: all 0.35s var(--ease-spring); }
.toast-leave-active { transition: all 0.2s ease-in; }
.toast-enter-from { transform: translateY(40px) scale(0.9); opacity: 0; }
.toast-leave-to   { transform: translateX(60px); opacity: 0; }
</style>

<template>
  <Teleport to="body">
    <div v-if="visible" class="confirm-overlay" @click.self="cancel">
      <div class="confirm-dialog">
        <p class="confirm-message" style="white-space:pre-line">{{ message }}</p>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="cancel">取消</button>
          <button class="btn-confirm" @click="confirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  message: string
  confirmText?: string
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

function confirm() { emit('confirm') }
function cancel() { emit('cancel') }
</script>

<style scoped>
.confirm-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999;
}
.confirm-dialog {
  background: white;
  border-radius: 12px;
  padding: 28px 32px;
  min-width: 340px;
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.confirm-message {
  font-size: 15px;
  color: #333;
  margin: 0 0 24px;
  line-height: 1.6;
}
.confirm-actions {
  display: flex; gap: 12px; justify-content: flex-end;
}
.btn-cancel, .btn-confirm {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: background .2s;
}
.btn-cancel {
  background: #f0f0f0;
  color: #666;
}
.btn-cancel:hover { background: #e0e0e0; }
.btn-confirm {
  background: #e74c3c;
  color: white;
}
.btn-confirm:hover { background: #c0392b; }
</style>

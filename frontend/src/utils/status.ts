export function getStatusText(status: string): string {
  return { UPLOADED: '待处理', PROCESSING: '处理中', COMPLETED: '已完成', FAILED: '失败' }[status] || status
}

export function getStatusClass(status: string): string {
  return { UPLOADED: 'badge-pending', PROCESSING: 'badge-processing', COMPLETED: 'badge-done', FAILED: 'badge-fail' }[status] || ''
}

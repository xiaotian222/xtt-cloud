export default {
  mounted(el, binding) {
    const { value } = binding
    // 这里可以根据实际权限系统实现权限检查逻辑
    // 暂时简单实现，后续可以根据需要扩展
    if (value && !checkPermission(value)) {
      el.style.display = 'none'
    }
  },
  updated(el, binding) {
    const { value } = binding
    if (value && !checkPermission(value)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

function checkPermission(permission) {
  // 权限检查逻辑，可以根据实际需求实现
  // 这里暂时返回 true，实际应该从 store 或 API 获取权限列表进行判断
  return true
}


import { hasPerm } from '@/utils/permissions'

export default {
  mounted(el, binding) {
    const code = binding.value
    if (!hasPerm(code)) {
      // 若无权限，直接移除元素
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}



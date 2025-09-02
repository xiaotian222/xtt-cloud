export const getPerms = () => {
  try {
    const raw = localStorage.getItem('perms')
    const arr = raw ? JSON.parse(raw) : []
    return new Set(arr)
  } catch {
    return new Set()
  }
}

export const hasPerm = (code) => {
  if (!code) return true
  return getPerms().has(code)
}

export const requirePerm = (code) => {
  if (!hasPerm(code)) {
    throw new Error('no-permission')
  }
}



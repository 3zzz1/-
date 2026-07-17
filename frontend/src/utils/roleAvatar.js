const roleAvatarStyles = {
  admin: { label: 'ADMIN', background: '#dbeafe', foreground: '#1d4ed8' },
  audit_director: { label: '处长', background: '#dcfce7', foreground: '#15803d' },
  audit_lead: { label: '主审', background: '#fef3c7', foreground: '#a16207' },
  audit_staff: { label: '审计', background: '#e0e7ff', foreground: '#4338ca' },
  school_leader: { label: '校领导', background: '#fee2e2', foreground: '#b91c1c' },
  audited_unit_leader: { label: '单位', background: '#cffafe', foreground: '#0e7490' },
  audited_unit_liaison: { label: '联络', background: '#fce7f3', foreground: '#be185d' },
  rect_responsible: { label: '整改', background: '#ffedd5', foreground: '#c2410c' },
  external_auditor: { label: '中介', background: '#f3e8ff', foreground: '#7e22ce' }
}

const defaultStyle = { label: '用户', background: '#e5e7eb', foreground: '#374151' }

function displayLabel(role, nickName, userName, fallback) {
  if (role === 'admin') return 'ADMIN'

  const name = String(nickName || userName || '').replace(/\s+/g, '')
  if (!name) return fallback

  const characters = Array.from(name)
  if (/^[\u3400-\u9fff]+$/.test(name)) {
    return characters.length <= 3 ? name : characters.slice(-2).join('')
  }
  return characters.slice(0, 2).join('').toUpperCase()
}

function escapeXml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;')
}

export function createRoleAvatar(roles = [], nickName = '', userName = '') {
  const role = roles.find(item => roleAvatarStyles[item])
  const style = roleAvatarStyles[role] || defaultStyle
  const label = displayLabel(role, nickName, userName, style.label)
  const labelLength = Array.from(label).length
  const fontSize = labelLength > 4 ? 22 : labelLength > 2 ? 26 : 32
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
      <rect width="96" height="96" rx="48" fill="${style.background}"/>
      <text x="48" y="51" text-anchor="middle" dominant-baseline="middle"
        font-family="Arial, Microsoft YaHei, sans-serif" font-size="${fontSize}"
        font-weight="700" fill="${style.foreground}">${escapeXml(label)}</text>
    </svg>`

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

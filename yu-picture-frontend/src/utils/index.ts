import { saveAs } from 'file-saver'

/**
 * 将 COS/CDN 等外链转为浏览器可用的绝对 URL。
 * 若后端只存了裸域名或 IP（无协议），浏览器会按当前站点解析，导致裂图。
 * 同时修正历史脏数据：如 host//path、https://host//path 的多余斜杠。
 */
export function toAbsolutePictureUrl(url?: string | null): string | undefined {
  if (url == null || url === '') return undefined
  let u = url.trim()
  if (u.startsWith('//')) {
    u = 'https:' + u
  } else if (!/^https?:\/\//i.test(u)) {
    u = u.replace(/^([^/]+)\/{2,}/, '$1/')
    u = u.replace(/^\/+/, '')
    u = 'https://' + u
  }
  return u.replace(/^(https?:\/\/[^/?#]+)\/{2,}/i, '$1/')
}

/**
 * 格式化文件大小
 * @param size
 */
export const formatSize = (size?: number) => {
  if (!size) return '未知'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

/**
 * 下载图片
 * @param url 图片下载地址
 * @param fileName 要保存为的文件名
 */
export function downloadImage(url?: string, fileName?: string) {
  const abs = toAbsolutePictureUrl(url)
  if (!abs) {
    return
  }
  saveAs(abs, fileName)
}

/**
 * 将颜色值转换为标准 #RRGGBB 格式
 * @param input
 */
export function toHexColor(input: string) {
  // 去掉 0x 前缀
  const colorValue = input.startsWith('0x') ? input.slice(2) : input

  // 将剩余部分解析为十六进制数，再转成 6 位十六进制字符串
  const hexColor = parseInt(colorValue, 16).toString(16).padStart(6, '0')

  // 返回标准 #RRGGBB 格式
  return `#${hexColor}`
}


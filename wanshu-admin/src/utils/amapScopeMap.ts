import type { OrganScope } from '@/api/organization'
import { scopeToDistrictAdcode } from '@/utils/regionScope'

type AMapWindow = Window & {
  _AMapSecurityConfig?: { securityJsCode?: string }
  AMap?: unknown
}

/** 2021-12-02 起申请的 Key 必须配安全密钥，且须在加载 maps 脚本之前设置 */
function setAmapSecurityConfig(securityJsCode: string | undefined) {
  const code = securityJsCode?.trim()
  if (!code || typeof window === 'undefined') return
  ;(window as AMapWindow)._AMapSecurityConfig = { securityJsCode: code }
}

/** 动态加载高德 JS API 2.0（含 DistrictSearch） */
export function loadAmapScript(key: string, securityJsCode?: string): Promise<void> {
  if (!key) return Promise.reject(new Error('AMap key empty'))
  setAmapSecurityConfig(securityJsCode)
  if (typeof window !== 'undefined' && (window as AMapWindow).AMap) return Promise.resolve()

  const existing = document.getElementById('wanshu-amap-sdk')
  if (existing) {
    return new Promise((resolve, reject) => {
      let n = 0
      const t = setInterval(() => {
        if ((window as AMapWindow).AMap) {
          clearInterval(t)
          resolve()
        } else if (++n > 100) {
          clearInterval(t)
          reject(new Error('AMap timeout'))
        }
      }, 50)
    })
  }

  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.id = 'wanshu-amap-sdk'
    s.async = true
    s.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(key)}&plugin=AMap.DistrictSearch`
    s.onload = () => resolve()
    s.onerror = () => reject(new Error('AMap script load failed'))
    document.head.appendChild(s)
  })
}

export type AmapScopeMapHandle = { destroy: () => void }

/**
 * 在容器内初始化地图，并按作业范围（adcode）绘制行政区多边形填充。
 */
export async function mountScopePolygons(
  container: HTMLElement,
  key: string,
  scopes: OrganScope[],
  style?: { fillColor?: string; fillOpacity?: number; strokeColor?: string },
  securityJsCode?: string
): Promise<AmapScopeMapHandle> {
  await loadAmapScript(key, securityJsCode)
  const AMap = (window as AMapWindow).AMap as any
  const map = new AMap.Map(container, {
    zoom: 4,
    center: [105.602725, 37.076636],
    viewMode: '2D',
  })
  requestAnimationFrame(() => map.resize())
  const polygons: unknown[] = []

  await new Promise<void>((resolve) => {
    if (typeof AMap.plugin === 'function') {
      AMap.plugin(['AMap.DistrictSearch'], () => resolve())
    } else {
      resolve()
    }
  })

  const district = new AMap.DistrictSearch({
    extensions: 'all',
    subdistrict: 0,
  })

  const codes = [...new Set(scopes.map(scopeToDistrictAdcode).filter(Boolean))]
  const fillColor = style?.fillColor ?? '#409eff'
  const fillOpacity = style?.fillOpacity ?? 0.35
  const strokeColor = style?.strokeColor ?? '#1e6fd9'

  for (const code of codes) {
    await new Promise<void>((resolve) => {
      district.search(code, (status: string, result: { districtList?: Array<{ boundaries?: unknown[] }> }) => {
        if (status !== 'complete' || !result?.districtList?.length) {
          resolve()
          return
        }
        const boundaries = result.districtList[0].boundaries as number[][][] | undefined
        if (!boundaries?.length) {
          resolve()
          return
        }
        for (const path of boundaries) {
          const poly = new AMap.Polygon({
            path,
            strokeColor,
            strokeWeight: 1,
            fillColor,
            fillOpacity,
          })
          map.add(poly)
          polygons.push(poly)
        }
        resolve()
      })
    })
  }

  if (polygons.length) {
    map.setFitView(polygons, false, [60, 60, 60, 60])
  }

  return {
    destroy: () => {
      try {
        map.destroy()
      } catch {
        /* ignore */
      }
    },
  }
}

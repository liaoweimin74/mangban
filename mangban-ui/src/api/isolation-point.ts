import http from '@/utils/http'
import type { R } from '@/types/common'
import type { PageResult } from '@/types/common'
import type { IsolationPointVO, IsolationPointCreateForm, IsolationPointUpdateForm, IsolationPointQueryParams } from '@/types/isolation-point'

export function getIsolationPointList(params: IsolationPointQueryParams) {
  return http.get<any, R<PageResult<IsolationPointVO>>>('/isolation-points', { params })
}

export function getIsolationPointById(id: number) {
  return http.get<any, R<IsolationPointVO>>(`/isolation-points/${id}`)
}

export function createIsolationPoint(data: IsolationPointCreateForm) {
  return http.post<any, R<IsolationPointVO>>('/isolation-points', data)
}

export function updateIsolationPoint(id: number, data: IsolationPointUpdateForm) {
  return http.put<any, R<IsolationPointVO>>(`/isolation-points/${id}`, data)
}

export function deleteIsolationPoint(id: number) {
  return http.delete<any, R<null>>(`/isolation-points/${id}`)
}

export function updateIsolationPointStatus(id: number, data: { status: string }) {
  return http.put<any, R<IsolationPointVO>>(`/isolation-points/${id}/status`, data)
}

export function updateIsolationPointOccupy(id: number, data: { occupyStatus: string }) {
  return http.put<any, R<IsolationPointVO>>(`/isolation-points/${id}/occupy`, data)
}
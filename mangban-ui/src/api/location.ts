import http from '@/utils/http'
import type { R } from '@/types/common'
import type { LocationTreeNode, LocationCreateForm, LocationUpdateForm } from '@/types/location'

export function getLocationTree() {
  return http.get<any, R<LocationTreeNode[]>>('/locations/tree')
}

export function createLocation(data: LocationCreateForm) {
  return http.post<any, R<LocationTreeNode>>('/locations', data)
}

export function updateLocation(id: number, data: LocationUpdateForm) {
  return http.put<any, R<LocationTreeNode>>(`/locations/${id}`, data)
}

export function deleteLocation(id: number) {
  return http.delete<any, R<null>>(`/locations/${id}`)
}
export interface IsolationPointVO {
  id: number
  unitId: number
  unitName: string
  plantName: string
  factoryName: string
  code: string
  name: string
  medium: string
  pressureRating: string
  temperatureRating: string
  hazardLevel: string
  pointType: string
  blindSpec: string
  equipmentTag: string
  pipelineNo: string
  status: string
  occupyStatus: string
  remark: string
  createdAt: string
  updatedAt: string
}

export interface IsolationPointCreateForm {
  unitId: number
  code: string
  name: string
  medium?: string
  pressureRating?: string
  temperatureRating?: string
  hazardLevel?: string
  pointType?: string
  blindSpec?: string
  equipmentTag?: string
  pipelineNo?: string
  remark?: string
}

export interface IsolationPointUpdateForm {
  unitId?: number
  code?: string
  name?: string
  medium?: string
  pressureRating?: string
  temperatureRating?: string
  hazardLevel?: string
  pointType?: string
  blindSpec?: string
  equipmentTag?: string
  pipelineNo?: string
  remark?: string
}

export interface IsolationPointQueryParams {
  unitId?: number
  plantId?: number
  code?: string
  name?: string
  medium?: string
  hazardLevel?: string
  status?: string
  occupyStatus?: string
  page?: number
  size?: number
}
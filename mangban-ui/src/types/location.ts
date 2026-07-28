export interface LocationTreeNode {
  id: number
  parentId: number | null
  name: string
  code: string
  type: 'FACTORY' | 'PLANT' | 'UNIT'
  sortOrder: number
  remark: string
  children: LocationTreeNode[] | null
}

export interface LocationCreateForm {
  parentId?: number | null
  name: string
  code: string
  type: string
  sortOrder?: number
  remark?: string
}

export interface LocationUpdateForm {
  name?: string
  code?: string
  sortOrder?: number
  remark?: string
}
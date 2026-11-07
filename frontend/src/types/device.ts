export interface Device {
  id: number
  code: string
  name: string
  status: 'NORMAL' | 'DISABLED' | string
  disableReason?: string | null
  disabledAt?: string | null
  createdAt: string
}

export interface DeviceForm {
  code: string
  name: string
}

export interface DeviceDisableForm {
  reason: string
}

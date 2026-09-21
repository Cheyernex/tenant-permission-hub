export type Accion = {
  id: number
  nombre: string
  descripcion?: string
  activo: boolean
}

export type Submodulo = {
  id: number
  nombre: string
  descripcion?: string
  orden: number
  activo: boolean
  acciones: Accion[]
}

export type Modulo = {
  id: number
  nombre: string
  descripcion?: string
  orden: number
  activo: boolean
  submodulos: Submodulo[]
}

export type Permiso = {
  id: number
  key: string
  submoduloId: number
  submoduloNombre: string
  accionId: number
  accionNombre: string
}

export type Tenant = {
  id: number
  nombre: string
  codigo: string
  activo: boolean
}

export type Usuario = {
  id: number
  email: string
  nombre: string
  esAdmin: boolean
  activo: boolean
}

export type Plantilla = {
  id: number
  nombre: string
  descripcion?: string
  activa: boolean
  permisos: Permiso[]
}

export type AuthState = {
  token: string | null
  tenantId: number | null
  userId: number | null
  roles: string[]
  email: string | null
}

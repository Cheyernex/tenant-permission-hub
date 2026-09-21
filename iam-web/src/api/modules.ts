import { apiFetch } from './client'
import type { Modulo } from '../types/iam'

export async function fetchModulos(): Promise<Modulo[]> {
  const data = await apiFetch('/api/v1/modulos?page=0&size=100')
  const list = Array.isArray(data) ? data : data?.content
  if (!Array.isArray(list)) throw new Error('Respuesta inesperada de /modulos')
  return list.map((m: any) => ({
    ...m,
    submodulos: (m.submodulos || []).map((s: any) => ({
      ...s,
      acciones: s.acciones || [],
    })),
  }))
}

export async function createModulo(payload: { nombre: string, descripcion?: string, orden?: number }) {
  return apiFetch('/api/v1/modulos', { method: 'POST', body: JSON.stringify(payload) })
}

export async function createSubmodulo(moduloId: number, payload: { nombre: string, descripcion?: string, orden?: number }) {
  return apiFetch(`/api/v1/modulos/${moduloId}/submodulos`, { method: 'POST', body: JSON.stringify(payload) })
}

export async function createAccion(submoduloId: number, payload: { nombre: string, descripcion?: string }) {
  return apiFetch(`/api/v1/submodulos/${submoduloId}/acciones`, { method: 'POST', body: JSON.stringify(payload) })
}

export async function toggleModulo(moduloId: number, activo: boolean) {
  const path = activo ? `/api/v1/modulos/${moduloId}/activar` : `/api/v1/modulos/${moduloId}/desactivar`
  return apiFetch(path, { method: 'PATCH' })
}

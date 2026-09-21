import type { Modulo } from '../types/iam'

// Datos semilla — idénticos a V1__global_schema.sql para demo sin backend
export const seedModulos: Modulo[] = [
  {
    id: 1, nombre: 'Catalogo', descripcion: 'Gestión de catálogo', orden: 1, activo: true,
    submodulos: [
      { id: 1, nombre: 'Productos', descripcion: 'Administración de productos', orden: 1, activo: true, acciones: [
        { id: 1, nombre: 'CREAR', descripcion: 'Crear productos', activo: true },
        { id: 2, nombre: 'EDITAR', descripcion: 'Editar productos', activo: true },
        { id: 3, nombre: 'ELIMINAR', descripcion: 'Eliminar productos', activo: true },
        { id: 4, nombre: 'VER', descripcion: 'Ver productos', activo: true },
        { id: 5, nombre: 'EXPORTAR', descripcion: 'Exportar', activo: true },
      ]},
      { id: 2, nombre: 'Categorias', orden: 2, activo: true, acciones: [
        { id: 6, nombre: 'CREAR', activo: true }, { id: 7, nombre: 'EDITAR', activo: true }, { id: 8, nombre: 'VER', activo: true },
      ]},
    ]
  },
  {
    id: 2, nombre: 'Pedidos', orden: 2, activo: true,
    submodulos: [
      { id: 3, nombre: 'Ordenes', orden: 1, activo: true, acciones: [
        { id: 12, nombre: 'CREAR', activo: true }, { id: 14, nombre: 'VER', activo: true }, { id: 15, nombre: 'CANCELAR', activo: true },
      ]},
      { id: 4, nombre: 'Historial', orden: 2, activo: true, acciones: [
        { id: 16, nombre: 'VER', activo: true }, { id: 17, nombre: 'EXPORTAR', activo: true },
      ]},
    ]
  },
  {
    id: 3, nombre: 'Pagos', orden: 3, activo: true,
    submodulos: [
      { id: 5, nombre: 'Transacciones', orden: 1, activo: true, acciones: [
        { id: 18, nombre: 'VER', activo: true }, { id: 19, nombre: 'REEMBOLSAR', activo: true },
      ]},
      { id: 6, nombre: 'Metodos', orden: 2, activo: true, acciones: [
        { id: 20, nombre: 'CREAR', activo: true }, { id: 22, nombre: 'VER', activo: true },
      ]},
    ]
  },
]

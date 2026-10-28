export type Sesion = {
  usuarioId: string
  nombreUsuario: string
  nombres: string
  apellidos: string
  roles: string[]
  permisos: string[]
  debeCambiarPassword: boolean
}

export class ErrorApi extends Error {
  readonly estado: number

  constructor(estado: number, mensaje: string) {
    super(mensaje)
    this.name = 'ErrorApi'
    this.estado = estado
  }
}

async function comprobarRespuesta(respuesta: Response): Promise<void> {
  if (respuesta.ok) return

  let mensaje =
    respuesta.status === 401
      ? 'No se pudo autenticar la cuenta. Comprueba tus datos o inicia sesión nuevamente.'
      : respuesta.status === 403
        ? 'No tienes permiso para realizar esta operación.'
        : 'No se pudo completar la solicitud.'

  if (respuesta.headers.get('content-type')?.includes('application/json')) {
    const error = await respuesta.json().catch(() => null)

    if (error && typeof error.mensaje === 'string') {
      mensaje = error.mensaje

      if (error.detalles && typeof error.detalles === 'object') {
        const detalles = Object.values(error.detalles)
          .filter((valor): valor is string => typeof valor === 'string')

        if (respuesta.status === 400 && detalles.length > 0) {
          mensaje += ` ${detalles.join(' ')}`
        }
      }
    }
  }

  throw new ErrorApi(respuesta.status, mensaje)
}

async function obtenerCsrf(): Promise<string> {
  const respuesta = await fetch('/api/auth/csrf', {
    credentials: 'include',
    cache: 'no-store',
  })

  await comprobarRespuesta(respuesta)

  const cookie = document.cookie
    .split('; ')
    .find((valor) => valor.startsWith('XSRF-TOKEN='))

  if (!cookie) {
    throw new Error('No se pudo preparar la sesión. Recarga la página.')
  }

  return decodeURIComponent(cookie.slice('XSRF-TOKEN='.length))
}

export async function consultarSesion(): Promise<Sesion | null> {
  const respuesta = await fetch('/api/auth/me', {
    credentials: 'include',
    cache: 'no-store',
  })

  if (respuesta.status === 401) return null

  await comprobarRespuesta(respuesta)
  return respuesta.json() as Promise<Sesion>
}

export async function enviar<T>(
  ruta: string,
  datos?: unknown,
  metodo: 'POST' | 'PUT' = 'POST',
): Promise<T> {
  const token = await obtenerCsrf()

  const respuesta = await fetch(ruta, {
    method: metodo,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': token,
    },
    body: datos === undefined ? undefined : JSON.stringify(datos),
  })

  await comprobarRespuesta(respuesta)

  if (respuesta.status === 204) return undefined as T

  return respuesta.json() as Promise<T>
}

export async function consultar<T>(
  ruta: string,
  signal?: AbortSignal,
): Promise<T> {
  const respuesta = await fetch(ruta, {
    credentials: 'include',
    cache: 'no-store',
    signal,
  })

  await comprobarRespuesta(respuesta)

  return respuesta.json() as Promise<T>
}
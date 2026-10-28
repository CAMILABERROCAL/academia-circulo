import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { createPortal } from 'react-dom'
import { enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'


type RespuestaRegistro = {
  apoderadoId: string
  usuarioId: string
  nombres: string
  apellidos: string
  celular: string
  debeCambiarPassword: boolean
}

export type ApoderadoNuevo = RespuestaRegistro & {
  dni: string
}

type Props = {
  onCerrar: () => void
  onCreado: (apoderado: ApoderadoNuevo) => void
  seleccionarAlCrear?: boolean
}

export default function RegistrarApoderado({
  onCerrar,
  onCreado,
  seleccionarAlCrear = true,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const passwordInput = useRef<HTMLInputElement>(null)
  const enviando = useRef(false)
  const modificado = useRef(false)

  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const elemento = dialogo.current

    if (elemento && !elemento.open) elemento.showModal()

    return () => elemento?.close()
  }, [])

  function cerrar() {
    if (enviando.current) return

    if (
      modificado.current &&
      !window.confirm('¿Quieres descartar los datos del nuevo apoderado?')
    ) {
      return
    }

    onCerrar()
  }

  async function registrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    evento.stopPropagation()

    if (enviando.current) return

    const formulario = evento.currentTarget
    const datos = new FormData(formulario)
    const texto = (campo: string) => String(datos.get(campo) ?? '').trim()

    const solicitud = {
      nombres: texto('nombres'),
      apellidos: texto('apellidos'),
      dni: texto('dni'),
      celular: texto('celular'),
      passwordTemporal: String(datos.get('passwordTemporal') ?? ''),
    }

    setError('')

    if (!solicitud.nombres || !solicitud.apellidos) {
      setError('Los nombres y apellidos no pueden quedar vacíos.')
      return
    }

    if (
      new TextEncoder().encode(solicitud.passwordTemporal).length > 72
    ) {
      setError('La contraseña temporal no debe superar 72 bytes UTF-8.')
      return
    }

    enviando.current = true
    setGuardando(true)

    try {
      const resultado = await enviar<RespuestaRegistro>(
        '/api/secretaria/apoderados',
        solicitud,
      )

      formulario.reset()
      modificado.current = false

      onCreado({
        ...resultado,
        dni: solicitud.dni,
      })
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.estado === 401) {
        setError('Tu sesión ha finalizado. Debes volver a iniciar sesión.')
      } else if (fallo instanceof TypeError) {
        setError(
          'No se pudo confirmar el registro. Antes de repetirlo, cierra esta ventana y busca al apoderado por DNI.',
        )
      } else {
        setError(
          fallo instanceof Error
            ? fallo.message
            : 'No se pudo registrar al apoderado.',
        )
      }
    } finally {
      if (passwordInput.current) passwordInput.current.value = ''
      enviando.current = false
      setGuardando(false)
    }
  }

  return createPortal(
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="rap-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        evento.stopPropagation()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">CUENTA DEL APODERADO</p>
          <h2 id="rap-titulo">Registrar apoderado</h2>
        </div>

        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar registro del apoderado"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido">
                <p className="fe-ayuda">
          {seleccionarAlCrear
            ? 'Al guardar, el apoderado quedará seleccionado en el formulario del alumno. Su cuenta permanecerá registrada aunque después canceles el registro del alumno.'
            : 'Al guardar, se creará la cuenta del apoderado. Podrás seleccionarlo al registrar un alumno; este registro no lo vincula automáticamente a ningún estudiante.'}
        </p>

        {error && (
          <div className="ps-error" role="alert">{error}</div>
        )}

        <form
          id="registro-apoderado"
          onSubmit={registrar}
          onChange={() => { modificado.current = true }}
          aria-busy={guardando}
        >
          <fieldset disabled={guardando} className="fe-formulario">
            <div>
              <label htmlFor="rap-nombres">Nombres</label>
              <input
                id="rap-nombres"
                name="nombres"
                required
                maxLength={120}
              />
            </div>

            <div>
              <label htmlFor="rap-apellidos">Apellidos</label>
              <input
                id="rap-apellidos"
                name="apellidos"
                required
                maxLength={120}
              />
            </div>

            <div>
              <label htmlFor="rap-dni">DNI</label>
              <input
                id="rap-dni"
                name="dni"
                type="text"
                inputMode="numeric"
                pattern="[0-9]{8}"
                minLength={8}
                maxLength={8}
                required
              />
            </div>

            <div>
              <label htmlFor="rap-celular">Celular</label>
              <input
                id="rap-celular"
                name="celular"
                type="tel"
                pattern={String.raw`(?:\+51)?9[0-9]{8}`}
                maxLength={12}
                required
              />
            </div>

            <div className="fe-ancho-completo">
              <label htmlFor="rap-password">Contraseña temporal</label>
              <input
                ref={passwordInput}
                id="rap-password"
                name="passwordTemporal"
                type="password"
                autoComplete="new-password"
                minLength={12}
                maxLength={64}
                required
                aria-describedby="rap-password-ayuda"
              />
              <small id="rap-password-ayuda" className="ayuda">
                Entre 12 y 64 caracteres. El apoderado deberá cambiarla
                al ingresar.
              </small>
            </div>
          </fieldset>
        </form>
      </div>

      <footer className="fe-pie">
        <button
          type="button"
          className="ps-boton-secundario"
          disabled={guardando}
          onClick={cerrar}
        >
          Cancelar
        </button>

        <button
          type="submit"
          form="registro-apoderado"
          className="ps-boton-primario"
          disabled={guardando}
        >
                    {guardando
            ? 'Registrando…'
            : seleccionarAlCrear
              ? 'Registrar y seleccionar'
              : 'Registrar apoderado'}
        </button>
      </footer>
    </dialog>,
    document.body,
  )
}
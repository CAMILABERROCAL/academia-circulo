import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type CicloCreado = {
  cicloId: string
  codigo: string
  nombre: string
}

type Props = {
  onCerrar: () => void
  onCreado: (ciclo: CicloCreado) => void
}

export default function CrearCiclo({ onCerrar, onCreado }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const enviando = useRef(false)
  const modificado = useRef(false)

  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [resultadoIncierto, setResultadoIncierto] = useState(false)

  useEffect(() => {
    const elemento = dialogo.current
    if (elemento && !elemento.open) elemento.showModal()
    return () => elemento?.close()
  }, [])

  function cerrar() {
    if (enviando.current) return

    if (
      !resultadoIncierto &&
      modificado.current &&
      !window.confirm('¿Quieres descartar los datos del ciclo?')
    ) {
      return
    }

    onCerrar()
  }

  async function guardar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando.current || resultadoIncierto) return

    const datos = new FormData(evento.currentTarget)
    const texto = (campo: string) => String(datos.get(campo) ?? '').trim()

    const solicitud = {
      codigo: texto('codigo').toUpperCase(),
      nombre: texto('nombre'),
      fechaInicio: texto('fechaInicio'),
      fechaFin: texto('fechaFin'),
      duracionMeses: Number(texto('duracionMeses')),
    }

    setError('')

    if (!/^[A-Z0-9_-]{1,30}$/.test(solicitud.codigo)) {
      setError('El código admite letras, números, guion y guion bajo.')
      return
    }

    if (!solicitud.nombre || solicitud.nombre.length > 120) {
      setError('Ingresa un nombre de hasta 120 caracteres.')
      return
    }

    if (!solicitud.fechaInicio || !solicitud.fechaFin) {
      setError('Completa ambas fechas.')
      return
    }

    if (solicitud.fechaFin < solicitud.fechaInicio) {
      setError('La fecha de fin no puede ser anterior al inicio.')
      return
    }

    if (
      !Number.isInteger(solicitud.duracionMeses) ||
      solicitud.duracionMeses < 1 ||
      solicitud.duracionMeses > 24
    ) {
      setError('La duración debe ser un número entero entre 1 y 24 meses.')
      return
    }

    enviando.current = true
    setGuardando(true)

    let creado: CicloCreado

    try {
      creado = await enviar<CicloCreado>('/api/admin/ciclos', solicitud)
    } catch (fallo) {
      if (fallo instanceof TypeError) {
        setResultadoIncierto(true)
        setError(
          'No se pudo confirmar el resultado. Cierra esta ventana y revisa el listado antes de intentar crear el ciclo otra vez.',
        )
      } else {
        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Vuelve a iniciar sesión.'
            : fallo instanceof Error
              ? fallo.message
              : 'No se pudo crear el ciclo.',
        )
      }
      return
    } finally {
      enviando.current = false
      setGuardando(false)
    }

    modificado.current = false
    onCreado(creado)
  }

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="cc-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">ADMINISTRACIÓN</p>
          <h2 id="cc-titulo">Crear ciclo</h2>
        </div>
        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar creación de ciclo"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido">
        <p className="fe-ayuda">
          Todos los campos son obligatorios. El ciclo se creará en estado
          Planificado.
        </p>

        {error && (
          <p className="ps-error" role="alert">{error}</p>
        )}

        <form
          id="crear-ciclo"
          onSubmit={guardar}
          onChange={() => { modificado.current = true }}
          aria-busy={guardando}
        >
          <fieldset
            className="fe-formulario"
            disabled={guardando || resultadoIncierto}
          >
            <div>
              <label htmlFor="cc-codigo">Código</label>
              <input
                id="cc-codigo"
                name="codigo"
                required
                maxLength={30}
                placeholder="Ej. PRUEBA-CICLO-002"
              />
            </div>

            <div>
              <label htmlFor="cc-nombre">Nombre</label>
              <input
                id="cc-nombre"
                name="nombre"
                required
                maxLength={120}
              />
            </div>

            <div>
              <label htmlFor="cc-inicio">Fecha de inicio</label>
              <input
                id="cc-inicio"
                name="fechaInicio"
                type="date"
                required
              />
            </div>

            <div>
              <label htmlFor="cc-fin">Fecha de fin</label>
              <input
                id="cc-fin"
                name="fechaFin"
                type="date"
                required
              />
            </div>

            <div>
              <label htmlFor="cc-duracion">Duración en meses</label>
              <input
                id="cc-duracion"
                name="duracionMeses"
                type="number"
                min={1}
                max={24}
                step={1}
                required
              />
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
          {resultadoIncierto ? 'Cerrar y consultar listado' : 'Cancelar'}
        </button>
        <button
          type="submit"
          form="crear-ciclo"
          className="ps-boton-primario"
          disabled={guardando || resultadoIncierto}
        >
          {guardando ? 'Creando…' : 'Crear ciclo'}
        </button>
      </footer>
    </dialog>
  )
}
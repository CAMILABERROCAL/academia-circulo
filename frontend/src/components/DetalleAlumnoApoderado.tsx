import { useEffect, useRef, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'
import MatriculaAcademica from './MatriculaAcademica'

type Alumno = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  estado: string
  parentesco: string
}

type Props = {
  estudianteId: string
  onCerrar: () => void
}

function mostrarParentesco(valor: string): string {
  switch (valor) {
    case 'PADRE':
      return 'Padre'
    case 'MADRE':
      return 'Madre'
    case 'TUTOR':
      return 'Tutor'
    case 'OTRO':
      return 'Otro'
    default:
      return valor
  }
}

export default function DetalleAlumnoApoderado({
  estudianteId,
  onCerrar,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const [alumno, setAlumno] = useState<Alumno | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const elemento = dialogo.current
    if (elemento && !elemento.open) elemento.showModal()

    return () => elemento?.close()
  }, [])

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setAlumno(null)

      try {
        const datos = await consultar<Alumno>(
          `/api/apoderado/hijos/${encodeURIComponent(estudianteId)}`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setAlumno(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        if (fallo instanceof ErrorApi && fallo.estado === 401) {
          setError(
            'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.',
          )
        } else if (fallo instanceof ErrorApi && fallo.estado === 404) {
          setError(
            'El alumno no está disponible para tu cuenta. Actualiza el listado o consulta con secretaría.',
          )
        } else if (fallo instanceof TypeError) {
          setError('No se pudo conectar con el servidor.')
        } else {
          setError(
            fallo instanceof Error
              ? fallo.message
              : 'No se pudo consultar al alumno.',
          )
        }
      } finally {
        if (!controlador.signal.aborted) {
          setCargando(false)
        }
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [estudianteId, intento])

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="da-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        onCerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">MIS ALUMNOS</p>
          <h2 id="da-titulo">Detalle del alumno</h2>
        </div>

        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar detalle del alumno"
          onClick={onCerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido" aria-busy={cargando}>
        {cargando ? (
          <p className="fe-estado" role="status">Consultando alumno…</p>
        ) : error ? (
          <div className="fe-estado">
            <p className="ps-error" role="alert">{error}</p>
            <button
              type="button"
              className="ps-boton-secundario"
              onClick={() => setIntento((valor) => valor + 1)}
            >
              Reintentar
            </button>
          </div>
        ) : alumno ? (
          <>
            <div className="fe-identidad">
              <span className="fe-avatar" aria-hidden="true">
                {alumno.nombres.charAt(0)}
                {alumno.apellidos.charAt(0)}
              </span>
              <div>
                <h3>{alumno.nombres} {alumno.apellidos}</h3>
                <span className="ps-codigo">
                  {alumno.codigoEstudiante}
                </span>
              </div>
            </div>

            <dl className="fe-datos">
              <div>
                <dt>Nombres</dt>
                <dd>{alumno.nombres}</dd>
              </div>
              <div>
                <dt>Apellidos</dt>
                <dd>{alumno.apellidos}</dd>
              </div>
              <div>
                <dt>Estado del alumno</dt>
                <dd>{alumno.estado}</dd>
              </div>
              <div>
                <dt>Tu parentesco con el alumno</dt>
                <dd>{mostrarParentesco(alumno.parentesco)}</dd>
              </div>
            </dl>

            <p className="fe-ayuda">
              Si algún dato necesita corregirse, comunícate con secretaría.
            </p>

            <MatriculaAcademica
              key={`matricula-${estudianteId}-${intento}`}
              ruta={`/api/apoderado/hijos/${encodeURIComponent(estudianteId)}/matricula-activa`}
            />
          </>
        ) : null}
      </div>

      <footer className="fe-pie">
        <button
          type="button"
          className="ps-boton-secundario"
          onClick={onCerrar}
        >
          Cerrar
        </button>
      </footer>
    </dialog>
  )
}
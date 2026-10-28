import { useEffect, useRef, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Alumno = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  estado: string
  parentesco: string
}

type Props = {
  apoderadoId: string
  nombreApoderado: string
  onCerrar: () => void
  onVerFicha: (estudianteId: string) => void
}

export default function AlumnosDelApoderado({
  apoderadoId,
  nombreApoderado,
  onCerrar,
  onVerFicha,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const [alumnos, setAlumnos] = useState<Alumno[]>([])
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
      setAlumnos([])

      try {
        const datos = await consultar<Alumno[]>(
          `/api/secretaria/apoderados/${encodeURIComponent(apoderadoId)}/alumnos`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setAlumnos(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.'
            : fallo instanceof TypeError
              ? 'No se pudo conectar con el servidor.'
              : fallo instanceof Error
                ? fallo.message
                : 'No se pudieron consultar los alumnos.',
        )
      } finally {
        if (!controlador.signal.aborted) {
          setCargando(false)
        }
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [apoderadoId, intento])

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="aa-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        onCerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">APODERADO</p>
          <h2 id="aa-titulo">Alumnos vinculados</h2>
        </div>

        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar alumnos vinculados"
          onClick={onCerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido" aria-busy={cargando}>
        <p className="fe-ayuda">
          Apoderado: <strong>{nombreApoderado}</strong>
        </p>

        {cargando ? (
          <p role="status">Consultando alumnos…</p>
        ) : error ? (
          <div>
            <p className="ps-error" role="alert">{error}</p>
            <button
              type="button"
              className="ps-boton-secundario"
              onClick={() => setIntento((valor) => valor + 1)}
            >
              Reintentar
            </button>
          </div>
        ) : alumnos.length === 0 ? (
          <p>Este apoderado todavía no tiene alumnos vinculados.</p>
        ) : (
          <>
            <p role="status">
              {alumnos.length}{' '}
              {alumnos.length === 1 ? 'alumno vinculado' : 'alumnos vinculados'}
            </p>

            <div
              className="ps-tabla-contenedor"
              role="region"
              aria-label="Alumnos del apoderado"
              tabIndex={0}
            >
              <table className="ps-tabla">
                <thead>
                  <tr>
                    <th scope="col">Código</th>
                    <th scope="col">Alumno</th>
                    <th scope="col">Estado</th>
                    <th scope="col">Parentesco del apoderado</th>
                    <th scope="col">Acciones</th>
                  </tr>
                </thead>
                                <tbody>
                  {alumnos.map((alumno) => (
                    <tr key={alumno.estudianteId}>
                      <td>{alumno.codigoEstudiante}</td>
                      <td>{alumno.nombres} {alumno.apellidos}</td>
                      <td>{alumno.estado}</td>
                      <td>{alumno.parentesco}</td>
                      <td>
                        <button
                          type="button"
                          className="ps-boton-secundario"
                          aria-label={`Ver ficha de ${alumno.nombres} ${alumno.apellidos}`}
                          onClick={() => onVerFicha(alumno.estudianteId)}
                        >
                          Ver ficha
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
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
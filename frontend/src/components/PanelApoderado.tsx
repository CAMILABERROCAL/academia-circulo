import { useEffect, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import type { Sesion } from '../lib/api'
import './PanelSecretaria.css'
import DetalleAlumnoApoderado from './DetalleAlumnoApoderado'

type Alumno = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  estado: string
  parentesco: string
}

type Props = {
  sesion: Sesion
  ocupado: boolean
  errorSesion: string
  onSalir: () => Promise<void>
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

export default function PanelApoderado({
  sesion,
  ocupado,
  errorSesion,
  onSalir,
}: Props) {
  const [alumnos, setAlumnos] = useState<Alumno[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)
    const [estudianteSeleccionado, setEstudianteSeleccionado] =
    useState<string | null>(null)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setAlumnos([])

      try {
        const datos = await consultar<Alumno[]>(
          '/api/apoderado/hijos',
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setAlumnos(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Pulsa Cerrar sesión y vuelve a ingresar.'
            : fallo instanceof TypeError
              ? 'No se pudo conectar con el servidor.'
              : fallo instanceof Error
                ? fallo.message
                : 'No se pudieron consultar tus alumnos vinculados.',
        )
      } finally {
        if (!controlador.signal.aborted) {
          setCargando(false)
        }
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [sesion.usuarioId, intento])

  const iniciales = [
    sesion.nombres.trim().charAt(0),
    sesion.apellidos.trim().charAt(0),
  ].join('')

  return (
    <div className="panel-secretaria">
      {estudianteSeleccionado && (
        <DetalleAlumnoApoderado
          key={estudianteSeleccionado}
          estudianteId={estudianteSeleccionado}
          onCerrar={() => setEstudianteSeleccionado(null)}
        />
      )}
      <aside className="ps-lateral" aria-label="Menú del apoderado">
        <div className="ps-marca">
          <span className="ps-marca-icono" aria-hidden="true">C</span>
          <div>
            <small>ACADEMIA</small>
            <strong>CÍRCULO</strong>
          </div>
        </div>

        <p className="ps-menu-titulo">APODERADO</p>

        <nav className="ps-navegacion" aria-label="Secciones disponibles">
          <a
            className="ps-menu-opcion ps-menu-activo"
            href="#mis-alumnos"
            aria-current="page"
          >
            <span aria-hidden="true">▦</span>
            Mis alumnos
          </a>
        </nav>

        <div className="ps-lateral-pie">
          <span>Acompañando su aprendizaje</span>
          <small>Academia Círculo</small>
        </div>
      </aside>

      <div className="ps-cuerpo">
        <header className="ps-cabecera">
          <span className="ps-ubicacion">Área del apoderado</span>

          <div className="ps-cuenta">
            <span className="ps-avatar" aria-hidden="true">{iniciales}</span>
            <div className="ps-nombre-cuenta">
              <strong>{sesion.nombres}</strong>
              <small>Apoderado</small>
            </div>
            <button
              type="button"
              className="ps-boton-secundario"
              disabled={ocupado}
              onClick={() => void onSalir()}
            >
              {ocupado ? 'Cerrando…' : 'Cerrar sesión'}
            </button>
          </div>
        </header>

        <main className="ps-contenido" id="mis-alumnos">
          <div className="ps-titulo">
            <p className="ps-antetitulo">ACOMPAÑAMIENTO ACADÉMICO</p>
            <h1>Mis alumnos</h1>
            <p>Consulta los estudiantes vinculados a tu cuenta.</p>
          </div>

          {errorSesion && (
            <div className="ps-error" role="alert">{errorSesion}</div>
          )}

          <section className="ps-tarjeta" aria-labelledby="pa-listado">
            <div className="ps-tarjeta-cabecera">
              <div>
                <h2 id="pa-listado">Alumnos vinculados</h2>
                <p>
                  {cargando
                    ? 'Consultando registros…'
                    : error
                      ? 'Consulta pendiente'
                      : `${alumnos.length} ${
                          alumnos.length === 1
                            ? 'alumno vinculado'
                            : 'alumnos vinculados'
                        }`}
                </p>
              </div>

              <button
                type="button"
                className="ps-boton-secundario"
                disabled={cargando}
                onClick={() => setIntento((valor) => valor + 1)}
              >
                Actualizar
              </button>
            </div>

            <div aria-busy={cargando}>
              {cargando ? (
                <div className="ps-estado" role="status">
                  <span className="ps-cargando" aria-hidden="true" />
                  Cargando alumnos…
                </div>
              ) : error ? (
                <div className="ps-estado">
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
                <div className="ps-estado">
                  <strong>Todavía no tienes alumnos vinculados</strong>
                  <p>
                    Comunícate con secretaría para revisar la vinculación
                    de tu cuenta.
                  </p>
                </div>
              ) : (
                <div
                  className="ps-tabla-contenedor"
                  role="region"
                  aria-label="Mis alumnos vinculados"
                  tabIndex={0}
                >
                  <table className="ps-tabla">
                                        <thead>
                      <tr>
                        <th scope="col">Código</th>
                        <th scope="col">Alumno</th>
                        <th scope="col">Estado del alumno</th>
                        <th scope="col">Tu parentesco</th>
                        <th scope="col">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {alumnos.map((alumno) => (
                        <tr key={alumno.estudianteId}>
                          <td>
                            <span className="ps-codigo">
                              {alumno.codigoEstudiante}
                            </span>
                          </td>
                          <td>{alumno.nombres} {alumno.apellidos}</td>
                          <td>{alumno.estado}</td>
                          <td>{mostrarParentesco(alumno.parentesco)}</td>
                          <td>
                            <button
                              type="button"
                              className="ps-boton-secundario"
                              aria-label={`Ver detalle de ${alumno.nombres} ${alumno.apellidos}`}
                              onClick={() =>
                                setEstudianteSeleccionado(alumno.estudianteId)
                              }
                            >
                              Ver detalle
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </section>

          <footer className="ps-pie">
            Academia Círculo · Sistema académico
          </footer>
        </main>
      </div>
    </div>
  )
}
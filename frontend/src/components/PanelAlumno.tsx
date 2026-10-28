import { useEffect, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import type { Sesion } from '../lib/api'
import './PanelSecretaria.css'
import './FichaEstudiante.css'
import MatriculaAcademica from './MatriculaAcademica'

type Perfil = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  fechaNacimiento: string | null
  correo: string | null
  celular: string | null
  direccionReferencial: string | null
  estado: string
}

type Props = {
  sesion: Sesion
  ocupado: boolean
  errorSesion: string
  onSalir: () => Promise<void>
}

function mostrarTexto(valor: string | null): string {
  return valor?.trim() || 'Sin registrar'
}

function mostrarFecha(valor: string | null): string {
  if (!valor) return 'Sin registrar'

  const partes = valor.split('-')
  return partes.length === 3
    ? `${partes[2]}/${partes[1]}/${partes[0]}`
    : valor
}

export default function PanelAlumno({
  sesion,
  ocupado,
  errorSesion,
  onSalir,
}: Props) {
  const [perfil, setPerfil] = useState<Perfil | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setPerfil(null)

      try {
        const datos = await consultar<Perfil>(
          '/api/estudiante/mi-perfil',
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setPerfil(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Pulsa Cerrar sesión y vuelve a ingresar.'
            : fallo instanceof ErrorApi && fallo.estado === 404
              ? 'No se encontró tu perfil de alumno. Comunícate con secretaría.'
              : fallo instanceof TypeError
                ? 'No se pudo conectar con el servidor.'
                : fallo instanceof Error
                  ? fallo.message
                  : 'No se pudo consultar tu perfil.',
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
      <aside className="ps-lateral" aria-label="Menú del alumno">
        <div className="ps-marca">
          <span className="ps-marca-icono" aria-hidden="true">C</span>
          <div>
            <small>ACADEMIA</small>
            <strong>CÍRCULO</strong>
          </div>
        </div>

        <p className="ps-menu-titulo">ALUMNO</p>

        <nav className="ps-navegacion" aria-label="Secciones disponibles">
          <a
            className="ps-menu-opcion ps-menu-activo"
            href="#mi-perfil"
            aria-current="page"
          >
            <span aria-hidden="true">▦</span>
            Mi perfil
          </a>
        </nav>

        <div className="ps-lateral-pie">
          <span>Tu aprendizaje, cada día más cerca</span>
          <small>Academia Círculo</small>
        </div>
      </aside>

      <div className="ps-cuerpo">
        <header className="ps-cabecera">
          <span className="ps-ubicacion">Área del alumno</span>

          <div className="ps-cuenta">
            <span className="ps-avatar" aria-hidden="true">{iniciales}</span>
            <div className="ps-nombre-cuenta">
              <strong>{sesion.nombres}</strong>
              <small>Alumno</small>
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

        <main className="ps-contenido" id="mi-perfil">
          <div className="ps-titulo">
            <p className="ps-antetitulo">MIS DATOS</p>
            <h1>Mi perfil</h1>
            <p>Consulta tu información registrada en la academia.</p>
          </div>

          {errorSesion && (
            <div className="ps-error" role="alert">{errorSesion}</div>
          )}

          <section className="ps-tarjeta" aria-labelledby="pa-perfil">
            <div className="ps-tarjeta-cabecera">
              <div>
                <h2 id="pa-perfil">Información del alumno</h2>
                <p>
                  Si necesitas corregir algún dato, comunícate con secretaría.
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

            <div className="fe-contenido" aria-busy={cargando}>
              {cargando ? (
                <div className="ps-estado" role="status">
                  <span className="ps-cargando" aria-hidden="true" />
                  Cargando perfil…
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
              ) : perfil ? (
                <dl className="fe-datos">
                  <div>
                    <dt>Código de estudiante</dt>
                    <dd>{perfil.codigoEstudiante}</dd>
                  </div>
                  <div>
                    <dt>Estado</dt>
                    <dd>{perfil.estado}</dd>
                  </div>
                  <div>
                    <dt>Nombres</dt>
                    <dd>{perfil.nombres}</dd>
                  </div>
                  <div>
                    <dt>Apellidos</dt>
                    <dd>{perfil.apellidos}</dd>
                  </div>
                  <div>
                    <dt>Fecha de nacimiento</dt>
                    <dd>{mostrarFecha(perfil.fechaNacimiento)}</dd>
                  </div>
                  <div>
                    <dt>Celular</dt>
                    <dd>{mostrarTexto(perfil.celular)}</dd>
                  </div>
                  <div className="fe-ancho-completo">
                    <dt>Correo electrónico</dt>
                    <dd>{mostrarTexto(perfil.correo)}</dd>
                  </div>
                  <div className="fe-ancho-completo">
                    <dt>Dirección referencial</dt>
                    <dd>{mostrarTexto(perfil.direccionReferencial)}</dd>
                  </div>
                </dl>
              ) : null}

              {perfil && !cargando && !error && (
                <MatriculaAcademica
                  key={`matricula-${sesion.usuarioId}-${intento}`}
                  ruta="/api/estudiante/mi-matricula"
                />
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
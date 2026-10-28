import FichaEstudiante from './FichaEstudiante'
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import type { Sesion } from '../lib/api'
import './PanelSecretaria.css'
import RegistrarAlumno from './RegistrarAlumno'
import ListadoApoderados from './ListadoApoderados'
import ListadoCiclos from './ListadoCiclos'
import RegistrarMatricula from './RegistrarMatricula'
import InicioSecretaria from './InicioSecretaria'


type EstudianteResumen = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  tieneMatriculaActiva: boolean
}

type PaginaEstudiantes = {
  estudiantes: EstudianteResumen[]
  pagina: number
  tamanio: number
  totalElementos: number
  totalPaginas: number
}

type Props = {
  sesion: Sesion
  ocupado: boolean
  errorSesion: string
  onSalir: () => Promise<void>
}

const TAMANIO = 10

export default function PanelSecretaria({
  sesion,
  ocupado,
  errorSesion,
  onSalir,
}: Props) {
  const [texto, setTexto] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [pagina, setPagina] = useState(0)
  const [resultado, setResultado] = useState<PaginaEstudiantes | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)
  const [estudianteSeleccionado, setEstudianteSeleccionado] =
  useState<string | null>(null)
  const [registrandoAlumno, setRegistrandoAlumno] = useState(false)
const [alumnoParaMatricular, setAlumnoParaMatricular] =
  useState<EstudianteResumen | null>(null)
const [seccion, setSeccion] =
  useState<'inicio' | 'alumnos' | 'apoderados' | 'ciclos'>('inicio')

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')

      const parametros = new URLSearchParams({
        pagina: String(pagina),
        tamanio: String(TAMANIO),
        busqueda,
      })

      try {
        const datos = await consultar<PaginaEstudiantes>(
          `/api/secretaria/estudiantes?${parametros}`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setResultado(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setResultado(null)

        if (fallo instanceof ErrorApi && fallo.estado === 401) {
          setError(
            'Tu sesión ha finalizado. Pulsa Cerrar sesión y vuelve a ingresar.',
          )
        } else if (fallo instanceof TypeError) {
          setError('No se pudo conectar con el servidor.')
        } else {
          setError(
            fallo instanceof Error
              ? fallo.message
              : 'No se pudo cargar el listado.',
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
  }, [pagina, busqueda, intento])

  function buscar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setPagina(0)
    setBusqueda(texto.trim())
    setIntento((valor) => valor + 1)
  }

  function limpiar() {
    setTexto('')
    setBusqueda('')
    setPagina(0)
    setIntento((valor) => valor + 1)
  }

  const iniciales = [
    sesion.nombres.trim().charAt(0),
    sesion.apellidos.trim().charAt(0),
  ].join('')

  const hayResultados = (resultado?.estudiantes.length ?? 0) > 0


  
 return (
  <div className="panel-secretaria">
{alumnoParaMatricular && (
  <RegistrarMatricula
    key={alumnoParaMatricular.estudianteId}
    alumno={alumnoParaMatricular}
    onCerrar={() => {
      setAlumnoParaMatricular(null)
      setIntento((valor) => valor + 1)
    }}
  />
)}
      
      {registrandoAlumno && (
  <RegistrarAlumno
    onCerrar={() => setRegistrandoAlumno(false)}
    onRegistrado={() => {
      setTexto('')
      setBusqueda('')
      setPagina(0)
      setIntento((valor) => valor + 1)
    }}
  />
)}
        {estudianteSeleccionado && (
  <FichaEstudiante
    key={estudianteSeleccionado}
    estudianteId={estudianteSeleccionado}
    onCerrar={() => setEstudianteSeleccionado(null)}
    onGuardado={() => setIntento((valor) => valor + 1)}
  />
)}
      <aside className="ps-lateral" aria-label="Menú de secretaría">
        <div className="ps-marca">
          <span className="ps-marca-icono" aria-hidden="true">C</span>
          <div>
            <small>ACADEMIA</small>
            <strong>CÍRCULO</strong>
          </div>
        </div>

        <p className="ps-menu-titulo">SECRETARÍA</p>

                <nav className="ps-navegacion" aria-label="Secciones disponibles">
                  <button
  type="button"
  className={`ps-menu-opcion ${
    seccion === 'inicio' ? 'ps-menu-activo' : ''
  }`}
  aria-current={seccion === 'inicio' ? 'page' : undefined}
  onClick={() => setSeccion('inicio')}
>
  <span aria-hidden="true">⌂</span>
  Inicio
</button>
          <button
            type="button"
            className={`ps-menu-opcion ${
              seccion === 'alumnos' ? 'ps-menu-activo' : ''
            }`}
            aria-current={seccion === 'alumnos' ? 'page' : undefined}
            onClick={() => setSeccion('alumnos')}
          >
            <span aria-hidden="true">▦</span>
            Alumnos
          </button>

          <button
            type="button"
            className={`ps-menu-opcion ${
              seccion === 'apoderados' ? 'ps-menu-activo' : ''
            }`}
            aria-current={seccion === 'apoderados' ? 'page' : undefined}
            onClick={() => setSeccion('apoderados')}
          >
            <span aria-hidden="true">♙</span>
            Apoderados
          </button>
                      <button
            type="button"
            className={`ps-menu-opcion ${
              seccion === 'ciclos' ? 'ps-menu-activo' : ''
            }`}
            aria-current={seccion === 'ciclos' ? 'page' : undefined}
            onClick={() => setSeccion('ciclos')}
          >
            <span aria-hidden="true">▤</span>
            Ciclos
          </button>

        </nav>

        <div className="ps-lateral-pie">
          <span>Gestión académica</span>
          <small>Academia Círculo</small>
        </div>
      </aside>

      <div className="ps-cuerpo">
        <header className="ps-cabecera">
          <span className="ps-ubicacion">Área de secretaría</span>

          <div className="ps-cuenta">
            <span className="ps-avatar" aria-hidden="true">{iniciales}</span>
            <div className="ps-nombre-cuenta">
              <strong>{sesion.nombres}</strong>
              <small>Secretaría</small>
            </div>
            <button
              className="ps-boton-secundario"
              type="button"
              disabled={ocupado}
              onClick={() => void onSalir()}
            >
              {ocupado ? 'Cerrando…' : 'Cerrar sesión'}
            </button>
          </div>
        </header>

        {seccion === 'inicio' && (
  <main className="ps-contenido" id="inicio-secretaria">
    {errorSesion && (
      <div className="ps-error" role="alert">
        {errorSesion}
      </div>
    )}

<InicioSecretaria
  onRegistrarAlumno={() => setRegistrandoAlumno(true)}
  onMatricular={() => {
    limpiar()
    setSeccion('alumnos')
  }}
  onVerAlumnos={() => setSeccion('alumnos')}
  onVerApoderados={() => setSeccion('apoderados')}
  onVerCiclos={() => setSeccion('ciclos')}
  onVerFicha={(estudianteId) =>
    setEstudianteSeleccionado(estudianteId)
  }
  versionAlumnos={intento}
/>

    <footer className="ps-pie">
      Academia Círculo · Sistema académico
    </footer>
  </main>
)}

        <main
          className="ps-contenido"
          id="alumnos"
          hidden={seccion !== 'alumnos'}
        >

          <div className="ps-titulo">
            <p className="ps-antetitulo">GESTIÓN DE ESTUDIANTES</p>
            <h1>Alumnos</h1>
            <p>Encuentra a cada estudiante por su código, nombre o apellido.</p>
          </div>

          {errorSesion && (
            <div className="ps-error" role="alert">{errorSesion}</div>
          )}

          <section className="ps-tarjeta" aria-labelledby="titulo-listado">
            <div className="ps-tarjeta-cabecera">
              <div>
                <h2 id="titulo-listado">Listado de alumnos</h2>
                <p>
                  {cargando
                    ? 'Consultando registros…'
                    : resultado
                      ? `${resultado.totalElementos} ${
                          busqueda ? 'coincidencias' : 'alumnos registrados'
                        }`
                      : 'Consulta los registros de la academia'}
                </p>
              </div>
              <button
  type="button"
  className="ps-boton-primario"
  onClick={() => setRegistrandoAlumno(true)}
>
  + Registrar alumno
</button>
            </div>

            <form className="ps-busqueda" onSubmit={buscar}>
              <div className="ps-busqueda-campo">
                <label htmlFor="buscar-alumno">Buscar alumno</label>
                <input
                  id="buscar-alumno"
                  type="search"
                  placeholder="Ej. PRUEBA-ALU-001 o Alumno Uno"
                  maxLength={120}
                  value={texto}
                  onChange={(evento) => setTexto(evento.target.value)}
                />
              </div>

              <button className="ps-boton-primario" type="submit">
                Buscar
              </button>

              {(texto || busqueda) && (
                <button
                  className="ps-boton-secundario"
                  type="button"
                  onClick={limpiar}
                >
                  Limpiar
                </button>
              )}
            </form>

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
                    className="ps-boton-secundario"
                    type="button"
                    onClick={() => setIntento((valor) => valor + 1)}
                  >
                    Reintentar
                  </button>
                </div>
              ) : !hayResultados ? (
                <div className="ps-estado">
                  <strong>
                    {busqueda ? 'Sin coincidencias' : 'Todavía no hay alumnos'}
                  </strong>
                  <p>
                    {busqueda
                      ? 'Revisa lo que escribiste o prueba con otro nombre o código.'
                      : 'Los alumnos registrados aparecerán aquí.'}
                  </p>
                </div>
              ) : (
                <div
                  className="ps-tabla-contenedor"
                  role="region"
                  aria-label="Listado de alumnos"
                  tabIndex={0}
                >
                 <table className="ps-tabla">
  <thead>
    <tr>
      <th scope="col">Código</th>
      <th scope="col">Nombres</th>
      <th scope="col">Apellidos</th>
      <th scope="col">Matrícula</th>
      <th scope="col">Acciones</th>
    </tr>
  </thead>

  <tbody>
    {resultado?.estudiantes.map((alumno) => (
      <tr key={alumno.estudianteId}>
        <td>
          <span className="ps-codigo">
            {alumno.codigoEstudiante}
          </span>
        </td>

        <td>{alumno.nombres}</td>
        <td>{alumno.apellidos}</td>

        <td>
          <span
            style={{
              display: 'inline-block',
              padding: '6px 10px',
              borderRadius: 12,
              fontSize: 12,
              fontWeight: 700,
              backgroundColor: alumno.tieneMatriculaActiva
                ? '#e5f5ec'
                : '#eef2f7',
              color: alumno.tieneMatriculaActiva
                ? '#226442'
                : '#53657d',
            }}
          >
            {alumno.tieneMatriculaActiva
              ? 'Matriculado'
              : 'Sin matrícula activa'}
          </span>
        </td>

        <td>
          <div
            style={{
              display: 'flex',
              gap: '8px',
              flexWrap: 'wrap',
            }}
          >
            <button
              type="button"
              className="ps-boton-secundario"
              aria-label={`Ver ficha de ${alumno.nombres} ${alumno.apellidos}`}
              onClick={() =>
                setEstudianteSeleccionado(alumno.estudianteId)
              }
            >
              Ver ficha
            </button>

            <button
              type="button"
              className="ps-boton-primario"
              disabled={alumno.tieneMatriculaActiva}
              aria-label={
                alumno.tieneMatriculaActiva
                  ? `${alumno.nombres} ${alumno.apellidos} ya tiene matrícula activa`
                  : `Matricular a ${alumno.nombres} ${alumno.apellidos}`
              }
              title={
                alumno.tieneMatriculaActiva
                  ? 'El alumno ya tiene una matrícula activa.'
                  : 'Registrar matrícula'
              }
              onClick={() => setAlumnoParaMatricular(alumno)}
            >
              Matricular
            </button>
          </div>
        </td>
      </tr>
    ))}
  </tbody>
</table>
                </div>
              )}
            </div>

            <div className="ps-paginacion">
              <span aria-live="polite">
                {!cargando && resultado
                  ? resultado.totalPaginas > 0
                    ? `Página ${pagina + 1} de ${resultado.totalPaginas}`
                    : 'Sin resultados'
                  : '—'}
              </span>

              <div>
                <button
                  className="ps-boton-secundario"
                  type="button"
                  disabled={cargando || pagina === 0}
                  onClick={() => setPagina((valor) => valor - 1)}
                >
                  ← Anterior
                </button>
                <button
                  className="ps-boton-secundario"
                  type="button"
                  disabled={
                    cargando ||
                    !resultado ||
                    pagina + 1 >= resultado.totalPaginas
                  }
                  onClick={() => setPagina((valor) => valor + 1)}
                >
                  Siguiente →
                </button>
              </div>
            </div>
          </section>

          <footer className="ps-pie">Academia Círculo · Sistema académico</footer>
        </main>
                  {seccion === 'apoderados' && (
          <main className="ps-contenido" id="apoderados">
            {errorSesion && (
              <div className="ps-error" role="alert">
                {errorSesion}
              </div>
            )}

            <ListadoApoderados />

            <footer className="ps-pie">
              Academia Círculo · Sistema académico
            </footer>
          </main>
        )}

                {seccion === 'ciclos' && (
          <main className="ps-contenido" id="ciclos">
            {errorSesion && (
              <div className="ps-error" role="alert">
                {errorSesion}
              </div>
            )}

            <ListadoCiclos />

            <footer className="ps-pie">
              Academia Círculo · Sistema académico
            </footer>
          </main>
        )}



      </div>
    </div>
  )
}
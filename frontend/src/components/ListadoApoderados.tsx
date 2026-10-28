import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import RegistrarApoderado from './RegistrarApoderado'
import AlumnosDelApoderado from './AlumnosDelApoderado'
import FichaEstudiante from './FichaEstudiante'

type Apoderado = {
  apoderadoId: string
  nombres: string
  apellidos: string
  dni: string | null
  celular: string | null
  estado: string
}

const TAMANIO = 10

export default function ListadoApoderados() {
  const [texto, setTexto] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [pagina, setPagina] = useState(0)
  const [apoderados, setApoderados] = useState<Apoderado[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [intento, setIntento] = useState(0)
  const [registrando, setRegistrando] = useState(false)
  const [apoderadoSeleccionado, setApoderadoSeleccionado] =
    useState<Apoderado | null>(null)

  const [estudianteSeleccionado, setEstudianteSeleccionado] =
    useState<string | null>(null)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')

      const parametros = new URLSearchParams({
        busqueda,
        pagina: String(pagina),
        tamanio: String(TAMANIO),
      })

      try {
        const datos = await consultar<Apoderado[]>(
          `/api/secretaria/apoderados?${parametros}`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setApoderados(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setApoderados([])

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
              : 'No se pudo consultar a los apoderados.',
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
  }, [busqueda, pagina, intento])

  function buscar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setPagina(0)
    setBusqueda(texto.trim())
    setAviso('')
    setIntento((valor) => valor + 1)
  }

  function limpiar() {
    setTexto('')
    setBusqueda('')
    setPagina(0)
    setAviso('')
    setIntento((valor) => valor + 1)
  }

   return (
    <>
            {apoderadoSeleccionado && !estudianteSeleccionado && (
        <AlumnosDelApoderado
          key={apoderadoSeleccionado.apoderadoId}
          apoderadoId={apoderadoSeleccionado.apoderadoId}
          nombreApoderado={`${apoderadoSeleccionado.nombres} ${apoderadoSeleccionado.apellidos}`}
          onCerrar={() => setApoderadoSeleccionado(null)}
          onVerFicha={(estudianteId) => {
            setEstudianteSeleccionado(estudianteId)
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

      {registrando && (
                <RegistrarApoderado
          seleccionarAlCrear={false}
          onCerrar={() => setRegistrando(false)}
          onCreado={(apoderado) => {
            setRegistrando(false)
            setTexto(apoderado.dni)
            setBusqueda(apoderado.dni)
            setPagina(0)
            setAviso(
              `${apoderado.nombres} ${apoderado.apellidos} fue registrado correctamente. Puedes seleccionarlo al registrar un alumno.`,
            )
            setIntento((valor) => valor + 1)
          }}
        />
      )}

      <div className="ps-titulo">
        <p className="ps-antetitulo">GESTIÓN DE APODERADOS</p>
        <h1>Apoderados</h1>
        <p>Busca por nombre, apellido, DNI o número de celular.</p>
      </div>

      {aviso && (
        <div className="mensaje mensaje-aviso" role="status">
          {aviso}
        </div>
      )}

      <section className="ps-tarjeta" aria-labelledby="titulo-apoderados">
        <div className="ps-tarjeta-cabecera">
          <div>
            <h2 id="titulo-apoderados">Listado de apoderados</h2>
            <p>Antes de crear una cuenta, comprueba si ya está registrada.</p>
          </div>

          <button
            type="button"
            className="ps-boton-primario"
            onClick={() => {
              setAviso('')
              setRegistrando(true)
            }}
          >
            + Registrar apoderado
          </button>
        </div>

        <form className="ps-busqueda" onSubmit={buscar}>
          <div className="ps-busqueda-campo">
            <label htmlFor="buscar-apoderado-listado">
              Buscar apoderado
            </label>
            <input
              id="buscar-apoderado-listado"
              type="search"
              placeholder="Nombre, DNI o celular"
              maxLength={120}
              value={texto}
              onChange={(evento) => setTexto(evento.target.value)}
            />
          </div>

          <button type="submit" className="ps-boton-primario">
            Buscar
          </button>

          {(texto || busqueda) && (
            <button
              type="button"
              className="ps-boton-secundario"
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
              Cargando apoderados…
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
          ) : apoderados.length === 0 ? (
            <div className="ps-estado">
              <strong>
                {pagina > 0
                  ? 'No hay más resultados'
                  : busqueda
                    ? 'Sin coincidencias'
                    : 'Todavía no hay apoderados'}
              </strong>
              <p>
                {pagina > 0
                  ? 'Puedes volver a la página anterior.'
                  : 'Prueba otra búsqueda o registra un nuevo apoderado.'}
              </p>
            </div>
          ) : (
            <div
              className="ps-tabla-contenedor"
              role="region"
              aria-label="Listado de apoderados"
              tabIndex={0}
            >
              <table className="ps-tabla">
                <thead>
                  <tr>
                    <th scope="col">Nombres</th>
                    <th scope="col">Apellidos</th>
                    <th scope="col">DNI</th>
                    <th scope="col">Celular</th>
                    <th scope="col">Estado</th>
                    <th scope="col">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {apoderados.map((apoderado) => (
                    <tr key={apoderado.apoderadoId}>
                      <td>{apoderado.nombres}</td>
                      <td>{apoderado.apellidos}</td>
                      <td>{apoderado.dni || 'Sin registrar'}</td>
                      <td>{apoderado.celular || 'Sin registrar'}</td>
                      <td>{apoderado.estado}</td>
                                            <td>
                        <button
                          type="button"
                          className="ps-boton-secundario"
                          aria-label={`Ver alumnos vinculados a ${apoderado.nombres} ${apoderado.apellidos}`}
                          onClick={() => setApoderadoSeleccionado(apoderado)}
                        >
                          Ver alumnos vinculados
                        </button>
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
            {cargando
              ? 'Consultando…'
              : error
                ? 'Consulta pendiente'
                : `Página ${pagina + 1} · ${apoderados.length} resultados en esta página`}
          </span>

          <div>
            <button
              type="button"
              className="ps-boton-secundario"
              disabled={cargando || pagina === 0}
              onClick={() => setPagina((valor) => valor - 1)}
            >
              ← Anterior
            </button>

            <button
              type="button"
              className="ps-boton-secundario"
              disabled={
                cargando || Boolean(error) || apoderados.length < TAMANIO
              }
              onClick={() => setPagina((valor) => valor + 1)}
            >
              Siguiente →
            </button>
          </div>
        </div>
      </section>
    </>
  )
}
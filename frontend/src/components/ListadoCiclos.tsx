import { useEffect, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'

type Ciclo = {
  cicloId: string
  codigo: string
  nombre: string
  fechaInicio: string
  fechaFin: string
  duracionMeses: number
  estado: string
}

const TAMANIO = 10

function mostrarFecha(fecha: string): string {
  const partes = fecha.split('-')
  return partes.length === 3
    ? `${partes[2]}/${partes[1]}/${partes[0]}`
    : fecha
}

function mostrarEstado(estado: string): string {
  switch (estado) {
    case 'PLANIFICADO':
      return 'Planificado'
    case 'ACTIVO':
      return 'Activo'
    case 'FINALIZADO':
      return 'Finalizado'
    case 'CANCELADO':
      return 'Cancelado'
    default:
      return estado
  }
}

type Props = {
  ruta?:
    | '/api/secretaria/ciclos'
    | '/api/admin/ciclos'
    | '/api/director/ciclos'
  onVerTurnos?: (ciclo: Ciclo) => void
}

export default function ListadoCiclos({
  ruta = '/api/secretaria/ciclos',
  onVerTurnos,
}: Props) {
  const [ciclos, setCiclos] = useState<Ciclo[]>([])
  const [pagina, setPagina] = useState(0)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setCiclos([])

      const parametros = new URLSearchParams({
        pagina: String(pagina),
        tamanio: String(TAMANIO),
      })

      try {
                const datos = await consultar<Ciclo[]>(
          `${ruta}?${parametros}`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setCiclos(datos)
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
                : 'No se pudieron consultar los ciclos.',
        )
      } finally {
        if (!controlador.signal.aborted) {
          setCargando(false)
        }
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [pagina, intento, ruta])

  return (
    <>
      <div className="ps-titulo">
        <p className="ps-antetitulo">OFERTA ACADÉMICA</p>
        <h1>Ciclos</h1>
        <p>Consulta las fechas, duración y estado de los ciclos registrados.</p>
      </div>

      <section className="ps-tarjeta" aria-labelledby="titulo-ciclos">
        <div className="ps-tarjeta-cabecera">
          <div>
            <h2 id="titulo-ciclos">Listado de ciclos</h2>
            <p>Los ciclos son creados por el administrador.</p>
          </div>
          <button
            type="button"
            className="ps-boton-secundario"
            disabled={cargando}
            onClick={() => {
              setPagina(0)
              setIntento((valor) => valor + 1)
            }}
          >
            Actualizar
          </button>
        </div>

        <div aria-busy={cargando}>
          {cargando ? (
            <div className="ps-estado" role="status">
              <span className="ps-cargando" aria-hidden="true" />
              Cargando ciclos…
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
          ) : ciclos.length === 0 ? (
            <div className="ps-estado">
              <strong>
                {pagina === 0
                  ? 'Todavía no hay ciclos registrados'
                  : 'No hay más ciclos en esta página'}
              </strong>
              <p>
                {pagina === 0
                  ? 'Los ciclos aparecerán cuando el administrador los registre.'
                  : 'Puedes volver a la página anterior.'}
              </p>
            </div>
          ) : (
            <div
              className="ps-tabla-contenedor"
              role="region"
              aria-label="Listado de ciclos"
              tabIndex={0}
            >
              <table className="ps-tabla">
                                <thead>
                  <tr>
                    <th scope="col">Código</th>
                    <th scope="col">Nombre</th>
                    <th scope="col">Inicio</th>
                    <th scope="col">Fin</th>
                    <th scope="col">Duración</th>
                    <th scope="col">Estado</th>
                    {onVerTurnos && <th scope="col">Acciones</th>}
                  </tr>
                </thead>
                <tbody>
                  {ciclos.map((ciclo) => (
                    <tr key={ciclo.cicloId}>
                      <td>
                        <span className="ps-codigo">{ciclo.codigo}</span>
                      </td>
                      <td>{ciclo.nombre}</td>
                      <td>{mostrarFecha(ciclo.fechaInicio)}</td>
                      <td>{mostrarFecha(ciclo.fechaFin)}</td>
                      <td>
                        {ciclo.duracionMeses}{' '}
                        {ciclo.duracionMeses === 1 ? 'mes' : 'meses'}
                      </td>
                      <td>{mostrarEstado(ciclo.estado)}</td>
                      {onVerTurnos && (
                        <td>
                          <button
                            type="button"
                            className="ps-boton-secundario"
                            aria-label={`Ver turnos de ${ciclo.nombre}`}
                            onClick={() => onVerTurnos(ciclo)}
                          >
                            Ver turnos
                          </button>
                        </td>
                      )}
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
                : `Página ${pagina + 1} · ${ciclos.length} ciclos en esta página`}
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
                cargando || Boolean(error) || ciclos.length < TAMANIO
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
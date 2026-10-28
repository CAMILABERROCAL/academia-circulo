import { useEffect, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import './HistorialFicha.css'

type EventoFicha = {
  eventoId: string
  realizadoPor: string | null
  accion: string
  ocurridoEn: string
}

type Props = {
  estudianteId: string
}

const TAMANIO = 5

const formatoFecha = new Intl.DateTimeFormat('es-PE', {
  timeZone: 'America/Lima',
  dateStyle: 'medium',
  timeStyle: 'short',
})

function mostrarFecha(valor: string): string {
  const fecha = new Date(valor)

  return Number.isNaN(fecha.getTime())
    ? 'Fecha no disponible'
    : formatoFecha.format(fecha)
}

export default function HistorialFicha({ estudianteId }: Props) {
  const [pagina, setPagina] = useState(0)
  const [eventos, setEventos] = useState<EventoFicha[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setEventos([])

      const parametros = new URLSearchParams({
        pagina: String(pagina),
        tamanio: String(TAMANIO),
      })

      try {
        const datos = await consultar<EventoFicha[]>(
          `/api/secretaria/estudiantes/${encodeURIComponent(estudianteId)}/ficha/historial?${parametros}`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setEventos(datos)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        if (fallo instanceof ErrorApi && fallo.estado === 401) {
          setError('Tu sesión ha finalizado. Vuelve a iniciar sesión.')
        } else if (fallo instanceof TypeError) {
          setError('No se pudo conectar con el servidor.')
        } else {
          setError(
            fallo instanceof Error
              ? fallo.message
              : 'No se pudo consultar el historial.',
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
  }, [estudianteId, pagina, intento])

  return (
    <section className="hf-seccion" aria-labelledby="hf-titulo">
      <div className="hf-cabecera">
        <div>
          <h3 id="hf-titulo">Historial de actualizaciones</h3>
          <p>Más recientes primero · Hora de Lima</p>
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
          <p className="hf-estado" role="status">Cargando historial…</p>
        ) : error ? (
          <div className="ps-error" role="alert">{error}</div>
        ) : eventos.length === 0 ? (
          <p className="hf-estado">
            {pagina === 0
              ? 'Todavía no hay actualizaciones registradas en esta ficha.'
              : 'No hay más actualizaciones. Puedes volver a la página anterior.'}
          </p>
        ) : (
          <ol className="hf-lista">
            {eventos.map((evento) => (
              <li key={evento.eventoId}>
                <span className="hf-punto" aria-hidden="true" />
                <div className="hf-evento">
                  <strong>
                    {evento.accion === 'ACTUALIZAR_FICHA'
                      ? 'Ficha actualizada'
                      : 'Operación registrada'}
                  </strong>
                  <p>
                    Por {evento.realizadoPor || 'Usuario no disponible'}
                  </p>
                  <time dateTime={evento.ocurridoEn}>
                    {mostrarFecha(evento.ocurridoEn)}
                  </time>
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>

      <div className="hf-paginacion">
        <span>Página {pagina + 1}</span>

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
            disabled={cargando || Boolean(error) || eventos.length < TAMANIO}
            onClick={() => setPagina((valor) => valor + 1)}
          >
            Siguiente →
          </button>
        </div>
      </div>
    </section>
  )
}
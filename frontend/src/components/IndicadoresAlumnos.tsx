import { useEffect, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'

type Indicadores = {
  totalAlumnos: number
  conMatriculaActiva: number
  sinMatriculaActiva: number
}

export default function IndicadoresAlumnos() {
  const [datos, setDatos] = useState<Indicadores | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setDatos(null)

      try {
        const respuesta = await consultar<Indicadores>(
          '/api/secretaria/estudiantes/indicadores',
          controlador.signal,
        )

        if (!controlador.signal.aborted) setDatos(respuesta)
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Vuelve a iniciar sesión.'
            : fallo instanceof TypeError
              ? 'No se pudo conectar con el servidor.'
              : fallo instanceof Error
                ? fallo.message
                : 'No se pudieron consultar los indicadores.',
        )
      } finally {
        if (!controlador.signal.aborted) setCargando(false)
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [intento])

  const numero = (valor: number | undefined) =>
    valor === undefined ? '—' : valor.toLocaleString('es-PE')

  return (
    <section aria-label="Resumen de alumnos" aria-busy={cargando}>
      <div className="is-resumen-cabecera">
        <p>Resumen general · Todos los alumnos registrados</p>
        <button
          type="button"
          className="ps-boton-secundario"
          disabled={cargando}
          onClick={() => setIntento((valor) => valor + 1)}
        >
          {cargando ? 'Consultando…' : 'Actualizar resumen'}
        </button>
      </div>

      {error ? (
        <p className="ps-error" role="alert">{error}</p>
      ) : (
        <>
          {cargando && (
            <p className="fe-ayuda" role="status">
              Consultando indicadores…
            </p>
          )}

          <dl className="is-indicadores">
            <div className="is-indicador">
              <dt>Alumnos registrados</dt>
              <dd>{numero(datos?.totalAlumnos)}</dd>
            </div>

            <div className="is-indicador is-indicador-verde">
              <dt>Con matrícula activa</dt>
              <dd>{numero(datos?.conMatriculaActiva)}</dd>
            </div>

            <div className="is-indicador is-indicador-ambar">
              <dt>Sin matrícula activa</dt>
              <dd>{numero(datos?.sinMatriculaActiva)}</dd>
            </div>
          </dl>
        </>
      )}
    </section>
  )
}
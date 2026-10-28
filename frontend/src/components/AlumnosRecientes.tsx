import { useEffect, useId, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'

type Alumno = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  registradoEn: string
}

type Props = {
  onVerFicha: (estudianteId: string) => void
  onVerTodos: () => void
}

function mostrarFecha(valor: string) {
  const fecha = new Date(valor)

  if (Number.isNaN(fecha.getTime())) return 'Fecha no disponible'

  return new Intl.DateTimeFormat('es-PE', {
    timeZone: 'America/Lima',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).format(fecha)
}

export default function AlumnosRecientes({
  onVerFicha,
  onVerTodos,
}: Props) {
  const tituloId = useId()
  const [alumnos, setAlumnos] = useState<Alumno[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setAlumnos([])

      try {
        const datos = await consultar<Alumno[]>(
          '/api/secretaria/estudiantes/recientes',
          controlador.signal,
        )

        if (!controlador.signal.aborted) setAlumnos(datos)
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Vuelve a iniciar sesión.'
            : fallo instanceof TypeError
              ? 'No se pudo conectar con el servidor.'
              : fallo instanceof Error
                ? fallo.message
                : 'No se pudieron consultar los alumnos.',
        )
      } finally {
        if (!controlador.signal.aborted) setCargando(false)
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [intento])

  return (
    <section
      className="is-tarjeta"
      aria-labelledby={tituloId}
      aria-busy={cargando}
    >
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 12,
          flexWrap: 'wrap',
          marginBottom: 18,
        }}
      >
        <div className="is-tarjeta-titulo">
          <span className="is-simbolo" aria-hidden="true">▦</span>
          <div>
            <h2 id={tituloId}>Últimos alumnos registrados</h2>
            <p>Los cinco registros más recientes · Hora de Lima</p>
          </div>
        </div>

        <button
          type="button"
          className="ps-boton-secundario"
          disabled={cargando}
          onClick={() => setIntento((valor) => valor + 1)}
        >
          {cargando ? 'Consultando…' : 'Actualizar'}
        </button>
      </div>

      {cargando ? (
        <p role="status">Cargando alumnos…</p>
      ) : error ? (
        <p className="ps-error" role="alert">{error}</p>
      ) : alumnos.length === 0 ? (
        <p>Todavía no hay alumnos registrados.</p>
      ) : (
        <div
          className="ps-tabla-contenedor"
          role="region"
          aria-label="Últimos alumnos registrados"
          tabIndex={0}
        >
          <table className="ps-tabla">
            <thead>
              <tr>
                <th scope="col">Código</th>
                <th scope="col">Alumno</th>
                <th scope="col">Fecha de registro</th>
                <th scope="col">Acción</th>
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
                  <td>{mostrarFecha(alumno.registradoEn)}</td>
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
      )}

      <button
        type="button"
        className="ps-boton-secundario"
        onClick={onVerTodos}
        style={{ marginTop: 18 }}
      >
        Ver todos los alumnos →
      </button>
    </section>
  )
}
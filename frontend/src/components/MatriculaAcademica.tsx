import { useEffect, useId, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Horario = {
  horarioId: string
  diaSemana: number
  horaEntrada: string
  horaSalida: string
}

type Matricula = {
  codigoMatricula: string
  estado: string
  fechaMatricula: string
  codigoCiclo: string
  nombreCiclo: string
  fechaInicio: string
  fechaFin: string
  codigoTurno: string
  nombreTurno: string
  horarios: Horario[]
}

type Respuesta = {
  matricula: Matricula | null
}

type Props = {
  ruta: string
}

const DIAS = [
  '',
  'Lunes',
  'Martes',
  'Miércoles',
  'Jueves',
  'Viernes',
  'Sábado',
  'Domingo',
]

function mostrarFecha(fecha: string) {
  return fecha.split('-').reverse().join('/')
}

export default function MatriculaAcademica({ ruta }: Props) {
  const tituloId = useId()
  const [matricula, setMatricula] = useState<Matricula | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setMatricula(null)

      try {
        const respuesta = await consultar<Respuesta>(
          ruta,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setMatricula(respuesta.matricula)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        if (fallo instanceof ErrorApi && fallo.estado === 401) {
          setError('Tu sesión ha finalizado. Vuelve a iniciar sesión.')
        } else if (
          fallo instanceof ErrorApi &&
          (fallo.estado === 403 || fallo.estado === 404)
        ) {
          setError(
            'Esta información no está disponible para tu cuenta. Consulta con secretaría.',
          )
        } else if (fallo instanceof TypeError) {
          setError('No se pudo conectar con el servidor.')
        } else {
          setError(
            fallo instanceof Error
              ? fallo.message
              : 'No se pudo consultar la matrícula.',
          )
        }
      } finally {
        if (!controlador.signal.aborted) setCargando(false)
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [ruta, intento])

  return (
    <section
      aria-labelledby={tituloId}
      aria-busy={cargando}
      style={{
        marginTop: 24,
        paddingTop: 24,
        borderTop: '1px solid #e6eef8',
      }}
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
        <div>
          <h3 id={tituloId} style={{ margin: '0 0 6px' }}>
            Matrícula y horarios
          </h3>
          <p className="fe-ayuda" style={{ margin: 0 }}>
            Información del ciclo y turno asignados.
          </p>
        </div>

        <button
          type="button"
          className="ps-boton-secundario"
          disabled={cargando}
          onClick={() => setIntento((valor) => valor + 1)}
        >
          {cargando ? 'Consultando…' : 'Actualizar matrícula'}
        </button>
      </div>

      {cargando ? (
        <p className="fe-estado" role="status">
          Consultando matrícula…
        </p>
      ) : error ? (
        <p className="ps-error" role="alert">{error}</p>
      ) : !matricula ? (
        <p className="fe-estado">
          No hay una matrícula activa registrada.
          Para más información, comunícate con secretaría.
        </p>
      ) : (
        <>
          <dl className="fe-datos">
            <div className="fe-ancho-completo">
              <dt>Código de matrícula</dt>
              <dd>{matricula.codigoMatricula}</dd>
            </div>

            <div>
              <dt>Estado de la matrícula</dt>
              <dd>
                {matricula.estado === 'ACTIVA'
                  ? 'Activa'
                  : matricula.estado}
              </dd>
            </div>

            <div>
              <dt>Fecha de matrícula</dt>
              <dd>{mostrarFecha(matricula.fechaMatricula)}</dd>
            </div>

            <div className="fe-ancho-completo">
              <dt>Ciclo</dt>
              <dd>
                {matricula.nombreCiclo} — {matricula.codigoCiclo}
              </dd>
            </div>

            <div>
              <dt>Inicio del ciclo</dt>
              <dd>{mostrarFecha(matricula.fechaInicio)}</dd>
            </div>

            <div>
              <dt>Fin del ciclo</dt>
              <dd>{mostrarFecha(matricula.fechaFin)}</dd>
            </div>

            <div className="fe-ancho-completo">
              <dt>Turno</dt>
              <dd>
                {matricula.nombreTurno} — {matricula.codigoTurno}
              </dd>
            </div>
          </dl>

          <h4>Horarios actuales del turno</h4>
          <p className="fe-ayuda">
            Horas de entrada y salida de la academia · Hora de Lima.
          </p>

          {matricula.horarios.length === 0 ? (
            <p>No hay horarios configurados. Consulta con secretaría.</p>
          ) : (
            <div className="ps-tabla-contenedor">
              <table className="ps-tabla">
                <thead>
                  <tr>
                    <th scope="col">Día</th>
                    <th scope="col">Entrada</th>
                    <th scope="col">Salida</th>
                  </tr>
                </thead>

                <tbody>
                  {[...matricula.horarios]
                    .sort((a, b) => a.diaSemana - b.diaSemana)
                    .map((horario) => (
                      <tr key={horario.horarioId}>
                        <td>{DIAS[horario.diaSemana]}</td>
                        <td>{horario.horaEntrada.slice(0, 5)}</td>
                        <td>{horario.horaSalida.slice(0, 5)}</td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </section>
  )
}
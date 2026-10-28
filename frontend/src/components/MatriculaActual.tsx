import { useEffect, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Horario = {
  horarioId: string
  diaSemana: number
  horaEntrada: string
  horaSalida: string
}

type Matricula = {
  matriculaId: string
  codigoMatricula: string
  estado: string
  fechaMatricula: string
  codigoCiclo: string
  nombreCiclo: string
  fechaInicio: string
  fechaFin: string
  codigoTurno: string
  nombreTurno: string
  registradoPor: string | null
  registradoEn: string | null
  horarios: Horario[]
}

type Respuesta = {
  matricula: Matricula | null
}

type Props = {
  estudianteId: string
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

function mostrarRegistro(fecha: string | null) {
  if (!fecha) return 'Sin información de auditoría'

  const instante = new Date(fecha)
  if (Number.isNaN(instante.getTime())) return 'Fecha no disponible'

  return new Intl.DateTimeFormat('es-PE', {
    timeZone: 'America/Lima',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
  }).format(instante)
}

export default function MatriculaActual({ estudianteId }: Props) {
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
          `/api/secretaria/estudiantes/${encodeURIComponent(estudianteId)}/matricula-activa`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setMatricula(respuesta.matricula)
        }
      } catch (fallo) {
        if (controlador.signal.aborted) return

        if (fallo instanceof ErrorApi && fallo.estado === 401) {
          setError(
            'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.',
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
  }, [estudianteId, intento])

  return (
    <section
      aria-labelledby="ma-titulo"
      aria-busy={cargando}
      style={{
        marginTop: 28,
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
          <h3 id="ma-titulo" style={{ margin: '0 0 6px' }}>
            Matrícula actual
          </h3>
          <p className="fe-ayuda" style={{ margin: 0 }}>
            Ciclo, turno y datos del registro.
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
          Este alumno no tiene una matrícula activa.
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

            <div>
              <dt>Registrada por</dt>
              <dd>
                {matricula.registradoPor ?? 'Usuario no disponible'}
              </dd>
            </div>

            <div>
              <dt>Fecha y hora del registro · Lima</dt>
              <dd>{mostrarRegistro(matricula.registradoEn)}</dd>
            </div>
          </dl>

          <h4>Horarios actuales del turno</h4>

          {matricula.horarios.length === 0 ? (
            <p>El turno no tiene horarios configurados actualmente.</p>
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
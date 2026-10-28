import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { consultar, enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Alumno = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
}

type Ciclo = {
  cicloId: string
  codigo: string
  nombre: string
  fechaInicio: string
  fechaFin: string
  estado: string
}

type Horario = {
  horarioId: string
  diaSemana: number
  horaEntrada: string
  horaSalida: string
}

type Turno = {
  turnoId: string
  codigo: string
  nombre: string
  estado: string
  horarios: Horario[]
}

type Matricula = {
  matriculaId: string
  codigoMatricula: string
  estado: string
}

type Props = {
  alumno: Alumno
  onCerrar: () => void
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

function fechaVisible(fecha: string) {
  return fecha.split('-').reverse().join('/')
}

function mensajeError(fallo: unknown) {
  if (fallo instanceof ErrorApi && fallo.estado === 401) {
    return 'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.'
  }

  return fallo instanceof Error
    ? fallo.message
    : 'No se pudo completar la operación.'
}

export default function RegistrarMatricula({ alumno, onCerrar }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const enviando = useRef(false)

  const [ciclos, setCiclos] = useState<Ciclo[]>([])
  const [cicloId, setCicloId] = useState('')
  const [turnos, setTurnos] = useState<Turno[]>([])
  const [turnoId, setTurnoId] = useState('')

  const [cargandoCiclos, setCargandoCiclos] = useState(true)
  const [cargandoTurnos, setCargandoTurnos] = useState(false)
  const [errorCiclos, setErrorCiclos] = useState('')
  const [errorTurnos, setErrorTurnos] = useState('')
  const [intentoCiclos, setIntentoCiclos] = useState(0)
  const [intentoTurnos, setIntentoTurnos] = useState(0)

  const [guardando, setGuardando] = useState(false)
  const [errorGuardado, setErrorGuardado] = useState('')
  const [resultadoIncierto, setResultadoIncierto] = useState(false)
  const [matricula, setMatricula] = useState<Matricula | null>(null)

  useEffect(() => {
    const elemento = dialogo.current
    if (elemento && !elemento.open) elemento.showModal()
    return () => elemento?.close()
  }, [])

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargandoCiclos(true)
      setErrorCiclos('')

      try {
        const todos: Ciclo[] = []
        let pagina = 0

        // La consulta del backend es paginada.
        while (!controlador.signal.aborted) {
          const lote = await consultar<Ciclo[]>(
            `/api/secretaria/ciclos?pagina=${pagina}&tamanio=100`,
            controlador.signal,
          )

          todos.push(...lote)
          if (lote.length < 100) break
          pagina += 1
        }

        if (!controlador.signal.aborted) {
          const unicos = [...new Map(
            todos.map((ciclo) => [ciclo.cicloId, ciclo]),
          ).values()]

          setCiclos(
            unicos.filter(
              (ciclo) =>
                ciclo.estado === 'PLANIFICADO' || ciclo.estado === 'ACTIVO',
            ),
          )
        }
      } catch (fallo) {
        if (!controlador.signal.aborted) {
          setCiclos([])
          setErrorCiclos(mensajeError(fallo))
        }
      } finally {
        if (!controlador.signal.aborted) setCargandoCiclos(false)
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [intentoCiclos])

  useEffect(() => {
    if (!cicloId) return

    const controlador = new AbortController()

    async function cargar() {
      setCargandoTurnos(true)
      setErrorTurnos('')

      try {
        const datos = await consultar<Turno[]>(
          `/api/secretaria/ciclos/${cicloId}/turnos`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setTurnos(
            datos.filter(
              (turno) =>
                turno.estado === 'ACTIVO' && turno.horarios.length > 0,
            ),
          )
        }
      } catch (fallo) {
        if (!controlador.signal.aborted) {
          setTurnos([])
          setErrorTurnos(mensajeError(fallo))
        }
      } finally {
        if (!controlador.signal.aborted) setCargandoTurnos(false)
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [cicloId, intentoTurnos])

  const ciclo = ciclos.find((elemento) => elemento.cicloId === cicloId)
  const turno = turnos.find((elemento) => elemento.turnoId === turnoId)

  function cambiarCiclo(valor: string) {
    setCicloId(valor)
    setTurnoId('')
    setTurnos([])
    setErrorTurnos('')
    setErrorGuardado('')
    setCargandoTurnos(Boolean(valor))
  }

  function cerrar() {
    if (enviando.current) return

    if (
      !matricula &&
      !resultadoIncierto &&
      cicloId &&
      !window.confirm('¿Quieres cerrar sin registrar la matrícula?')
    ) {
      return
    }

    onCerrar()
  }

  async function guardar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()

    if (enviando.current || matricula || resultadoIncierto) return

    if (
      !ciclo ||
      !turno ||
      cargandoCiclos ||
      cargandoTurnos ||
      errorCiclos ||
      errorTurnos
    ) {
      setErrorGuardado('Selecciona un ciclo y un turno disponibles.')
      return
    }

    enviando.current = true
    setGuardando(true)
    setErrorGuardado('')

    try {
      const creada = await enviar<Matricula>(
        '/api/secretaria/matriculas',
        {
          estudianteId: alumno.estudianteId,
          cicloId: ciclo.cicloId,
          turnoId: turno.turnoId,
        },
      )

      setMatricula(creada)
    } catch (fallo) {
      if (!(fallo instanceof ErrorApi) || fallo.estado >= 500) {
        setResultadoIncierto(true)
        setErrorGuardado(
          'No se pudo confirmar el resultado. Comprueba en la base de datos si la matrícula se registró antes de volver a intentarlo.',
        )
      } else {
        setErrorGuardado(mensajeError(fallo))
      }
    } finally {
      enviando.current = false
      setGuardando(false)
    }
  }

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="rm-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">SECRETARÍA</p>
          <h2 id="rm-titulo">
            {matricula ? 'Matrícula registrada' : 'Matricular alumno'}
          </h2>
        </div>

        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar matrícula"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido">
        <div className="fe-identidad">
          <div>
            <h3>{alumno.nombres} {alumno.apellidos}</h3>
            <span className="ps-codigo">{alumno.codigoEstudiante}</span>
          </div>
        </div>

        {matricula ? (
          <div role="status">
            <p>La matrícula se registró correctamente.</p>
            <dl className="fe-datos">
              <div className="fe-ancho-completo">
                <dt>Código de matrícula</dt>
                <dd>{matricula.codigoMatricula}</dd>
              </div>
              <div>
                <dt>Ciclo</dt>
                <dd>{ciclo?.nombre}</dd>
              </div>
              <div>
                <dt>Turno</dt>
                <dd>{turno?.nombre}</dd>
              </div>
              <div>
                <dt>Estado</dt>
                <dd>{matricula.estado}</dd>
              </div>
            </dl>
          </div>
        ) : (
          <form id="registrar-matricula" onSubmit={guardar}>
            <p className="fe-ayuda">
              El alumno solo puede tener una matrícula activa.
              Revisa el ciclo y los horarios antes de confirmar.
            </p>

            {errorGuardado && (
              <p className="ps-error" role="alert">{errorGuardado}</p>
            )}

            <fieldset
              className="fe-formulario"
              disabled={guardando || resultadoIncierto}
            >
              <div className="fe-ancho-completo">
                <label htmlFor="rm-ciclo">Ciclo</label>
                <select
                  id="rm-ciclo"
                  required
                  value={cicloId}
                  disabled={cargandoCiclos || Boolean(errorCiclos)}
                  onChange={(evento) => cambiarCiclo(evento.target.value)}
                >
                  <option value="">
                    {cargandoCiclos ? 'Cargando ciclos…' : 'Selecciona un ciclo'}
                  </option>
                  {ciclos.map((elemento) => (
                    <option key={elemento.cicloId} value={elemento.cicloId}>
                      {elemento.codigo} — {elemento.nombre}
                    </option>
                  ))}
                </select>

                {errorCiclos && (
                  <>
                    <p className="ps-error" role="alert">{errorCiclos}</p>
                    <button
                      type="button"
                      className="ps-boton-secundario"
                      onClick={() => setIntentoCiclos((valor) => valor + 1)}
                    >
                      Reintentar carga de ciclos
                    </button>
                  </>
                )}

                {!cargandoCiclos && !errorCiclos && ciclos.length === 0 && (
                  <p>No hay ciclos planificados o activos disponibles.</p>
                )}

                {ciclo && (
                  <p>
                    Del {fechaVisible(ciclo.fechaInicio)} al{' '}
                    {fechaVisible(ciclo.fechaFin)}.
                  </p>
                )}
              </div>

              <div className="fe-ancho-completo">
                <label htmlFor="rm-turno">Turno</label>
                <select
                  id="rm-turno"
                  required
                  value={turnoId}
                  disabled={
                    !cicloId || cargandoTurnos || Boolean(errorTurnos)
                  }
                  onChange={(evento) => {
                    setTurnoId(evento.target.value)
                    setErrorGuardado('')
                  }}
                >
                  <option value="">
                    {cargandoTurnos ? 'Cargando turnos…' : 'Selecciona un turno'}
                  </option>
                  {turnos.map((elemento) => (
                    <option key={elemento.turnoId} value={elemento.turnoId}>
                      {elemento.codigo} — {elemento.nombre}
                    </option>
                  ))}
                </select>

                {errorTurnos && (
                  <>
                    <p className="ps-error" role="alert">{errorTurnos}</p>
                    <button
                      type="button"
                      className="ps-boton-secundario"
                      onClick={() => setIntentoTurnos((valor) => valor + 1)}
                    >
                      Reintentar carga de turnos
                    </button>
                  </>
                )}

                {cicloId && !cargandoTurnos && !errorTurnos &&
                  turnos.length === 0 && (
                    <p>
                      Este ciclo no tiene turnos activos con horarios.
                      El director debe configurarlos primero.
                    </p>
                  )}
              </div>
            </fieldset>

            {turno && (
              <section aria-labelledby="rm-horarios">
                <h3 id="rm-horarios">Horarios del turno</h3>
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
                      {[...turno.horarios]
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
              </section>
            )}
          </form>
        )}
      </div>

      <footer className="fe-pie">
        <button
          key="cerrar"
          type="button"
          className="ps-boton-secundario"
          disabled={guardando}
          onClick={cerrar}
        >
          {matricula || resultadoIncierto ? 'Cerrar' : 'Cancelar'}
        </button>

        {!matricula && (
          <button
            key="confirmar"
            type="submit"
            form="registrar-matricula"
            className="ps-boton-primario"
            disabled={
              guardando ||
              resultadoIncierto ||
              cargandoCiclos ||
              cargandoTurnos ||
              Boolean(errorCiclos || errorTurnos) ||
              !ciclo ||
              !turno
            }
          >
            {guardando ? 'Registrando…' : 'Confirmar matrícula'}
          </button>
        )}
      </footer>
    </dialog>
  )
}
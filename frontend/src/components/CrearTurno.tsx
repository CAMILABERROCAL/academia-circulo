import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Props = {
  cicloId: string
  nombreCiclo: string
  onCerrar: () => void
  onCreado: () => void
}

const DIAS = [
  'Lunes',
  'Martes',
  'Miércoles',
  'Jueves',
  'Viernes',
  'Sábado',
  'Domingo',
]

type Dia = {
  diaSemana: number
  seleccionado: boolean
  horaEntrada: string
  horaSalida: string
}

export default function CrearTurno({
  cicloId,
  nombreCiclo,
  onCerrar,
  onCreado,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const enviando = useRef(false)
  const modificado = useRef(false)

  const [codigo, setCodigo] = useState('')
  const [nombre, setNombre] = useState('')
  const [dias, setDias] = useState<Dia[]>(() =>
    DIAS.map((_, indice) => ({
      diaSemana: indice + 1,
      seleccionado: false,
      horaEntrada: '',
      horaSalida: '',
    })),
  )
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [resultadoIncierto, setResultadoIncierto] = useState(false)

  useEffect(() => {
    const elemento = dialogo.current
    if (elemento && !elemento.open) elemento.showModal()
    return () => elemento?.close()
  }, [])

  function cambiarDia(
    diaSemana: number,
    cambios: Partial<Omit<Dia, 'diaSemana'>>,
  ) {
    modificado.current = true
    setDias((anteriores) =>
      anteriores.map((dia) =>
        dia.diaSemana === diaSemana ? { ...dia, ...cambios } : dia,
      ),
    )
  }

  function cerrar() {
    if (enviando.current) return

    if (
      !resultadoIncierto &&
      modificado.current &&
      !window.confirm('¿Quieres descartar los datos del turno?')
    ) {
      return
    }

    onCerrar()
  }

  async function guardar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando.current || resultadoIncierto) return

    setError('')

    const codigoNormalizado = codigo.trim().toUpperCase()
    const nombreNormalizado = nombre.trim()
    const seleccionados = dias.filter((dia) => dia.seleccionado)

    if (!/^[A-Z0-9_-]{1,30}$/.test(codigoNormalizado)) {
      setError('El código admite letras, números, guion y guion bajo.')
      return
    }

    if (!nombreNormalizado || nombreNormalizado.length > 100) {
      setError('Ingresa un nombre de hasta 100 caracteres.')
      return
    }

    if (seleccionados.length === 0) {
      setError('Selecciona al menos un día y completa su horario.')
      return
    }

    for (const dia of seleccionados) {
      if (!dia.horaEntrada || !dia.horaSalida) {
        setError(`Completa las horas del ${DIAS[dia.diaSemana - 1]}.`)
        return
      }

      if (dia.horaSalida <= dia.horaEntrada) {
        setError(
          `La salida del ${DIAS[dia.diaSemana - 1]} debe ser posterior a la entrada.`,
        )
        return
      }
    }

    enviando.current = true
    setGuardando(true)

    try {
      await enviar<{ turnoId: string }>(
        `/api/director/ciclos/${encodeURIComponent(cicloId)}/turnos`,
        {
          codigo: codigoNormalizado,
          nombre: nombreNormalizado,
          horarios: seleccionados.map((dia) => ({
            diaSemana: dia.diaSemana,
            horaEntrada: dia.horaEntrada,
            horaSalida: dia.horaSalida,
          })),
        },
      )
    } catch (fallo) {
      if (fallo instanceof TypeError) {
        setResultadoIncierto(true)
        setError(
          'No se pudo confirmar el resultado. Cierra esta ventana y revisa los turnos antes de intentar registrarlo otra vez.',
        )
      } else {
        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Vuelve a iniciar sesión.'
            : fallo instanceof Error
              ? fallo.message
              : 'No se pudo crear el turno.',
        )
      }
      return
    } finally {
      enviando.current = false
      setGuardando(false)
    }

    modificado.current = false
    onCreado()
  }

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="ct-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">DIRECCIÓN</p>
          <h2 id="ct-titulo">Crear turno</h2>
        </div>
        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar creación de turno"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido">
        <p className="fe-ayuda">
          Ciclo: <strong>{nombreCiclo}</strong>.
          Selecciona los días de asistencia y sus horas de entrada y salida.
          Horas de Lima.
        </p>

        {error && <p className="ps-error" role="alert">{error}</p>}

        <form
          id="crear-turno"
          onSubmit={guardar}
          aria-busy={guardando}
        >
          <fieldset disabled={guardando || resultadoIncierto}>
            <div className="fe-formulario">
              <div>
                <label htmlFor="ct-codigo">Código del turno</label>
                <input
                  id="ct-codigo"
                  required
                  maxLength={30}
                  placeholder="Ej. TARDE"
                  value={codigo}
                  onChange={(evento) => {
                    modificado.current = true
                    setCodigo(evento.target.value)
                  }}
                />
              </div>

              <div>
                <label htmlFor="ct-nombre">Nombre</label>
                <input
                  id="ct-nombre"
                  required
                  maxLength={100}
                  placeholder="Ej. Turno tarde"
                  value={nombre}
                  onChange={(evento) => {
                    modificado.current = true
                    setNombre(evento.target.value)
                  }}
                />
              </div>
            </div>

            <p className="fe-ayuda">
              Los días sin seleccionar no tendrán horario asignado.
            </p>

            {dias.map((dia) => (
              <fieldset
                key={dia.diaSemana}
                style={{ marginBottom: 16 }}
              >
                <legend>{DIAS[dia.diaSemana - 1]}</legend>

                <label
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 10,
                  }}
                >
                  <input
                    type="checkbox"
                    style={{ width: 'auto' }}
                    checked={dia.seleccionado}
                    onChange={(evento) =>
                      cambiarDia(dia.diaSemana, {
                        seleccionado: evento.target.checked,
                      })
                    }
                  />
                  Asignar horario
                </label>

                {dia.seleccionado && (
                  <div className="fe-formulario">
                    <div>
                      <label htmlFor={`ct-entrada-${dia.diaSemana}`}>
                        Entrada
                      </label>
                      <input
                        id={`ct-entrada-${dia.diaSemana}`}
                        type="time"
                        step={60}
                        required
                        value={dia.horaEntrada}
                        onChange={(evento) =>
                          cambiarDia(dia.diaSemana, {
                            horaEntrada: evento.target.value,
                          })
                        }
                      />
                    </div>
                    <div>
                      <label htmlFor={`ct-salida-${dia.diaSemana}`}>
                        Salida
                      </label>
                      <input
                        id={`ct-salida-${dia.diaSemana}`}
                        type="time"
                        step={60}
                        required
                        value={dia.horaSalida}
                        onChange={(evento) =>
                          cambiarDia(dia.diaSemana, {
                            horaSalida: evento.target.value,
                          })
                        }
                      />
                    </div>
                  </div>
                )}
              </fieldset>
            ))}
          </fieldset>
        </form>
      </div>

      <footer className="fe-pie">
        <button
          type="button"
          className="ps-boton-secundario"
          disabled={guardando}
          onClick={cerrar}
        >
          {resultadoIncierto ? 'Volver a consultar turnos' : 'Cancelar'}
        </button>
        <button
          type="submit"
          form="crear-turno"
          className="ps-boton-primario"
          disabled={guardando || resultadoIncierto}
        >
          {guardando ? 'Creando…' : 'Crear turno'}
        </button>
      </footer>
    </dialog>
  )
}
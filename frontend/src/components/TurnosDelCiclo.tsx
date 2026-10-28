import { useEffect, useRef, useState } from 'react'
import { consultar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Horario = {
  horarioId: string
  diaSemana: number
  horaEntrada: string
  horaSalida: string
}

type Turno = {
  turnoId: string
  cicloId: string
  codigo: string
  nombre: string
  estado: string
  horarios: Horario[]
}

type Props = {
  cicloId: string
  nombreCiclo: string
  onCerrar: () => void
  onCrear?: () => void
  aviso?: string
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

export default function TurnosDelCiclo({
  cicloId,
  nombreCiclo,
  onCerrar,
  onCrear,
  aviso,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const [turnos, setTurnos] = useState<Turno[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [intento, setIntento] = useState(0)

  useEffect(() => {
    const elemento = dialogo.current
    if (elemento && !elemento.open) elemento.showModal()
    return () => elemento?.close()
  }, [])

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setError('')
      setTurnos([])

      try {
        const datos = await consultar<Turno[]>(
          `/api/director/ciclos/${encodeURIComponent(cicloId)}/turnos`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) setTurnos(datos)
      } catch (fallo) {
        if (controlador.signal.aborted) return

        setError(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.'
            : fallo instanceof TypeError
              ? 'No se pudo conectar con el servidor.'
              : fallo instanceof Error
                ? fallo.message
                : 'No se pudieron consultar los turnos.',
        )
      } finally {
        if (!controlador.signal.aborted) setCargando(false)
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [cicloId, intento])

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="tc-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        onCerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">DIRECCIÓN</p>
          <h2 id="tc-titulo">Turnos y horarios</h2>
        </div>
        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar turnos y horarios"
          onClick={onCerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido" aria-busy={cargando}>
                {aviso && (
          <div className="mensaje mensaje-aviso" role="status">
            {aviso}
          </div>
        )}
        <p className="fe-ayuda">
          Ciclo: <strong>{nombreCiclo}</strong>. Horas de Lima.
        </p>

        {cargando ? (
          <p role="status">Consultando turnos…</p>
        ) : error ? (
          <div>
            <p className="ps-error" role="alert">{error}</p>
            <button
              type="button"
              className="ps-boton-secundario"
              onClick={() => setIntento((valor) => valor + 1)}
            >
              Reintentar
            </button>
          </div>
        ) : turnos.length === 0 ? (
          <p>Este ciclo todavía no tiene turnos registrados.</p>
        ) : (
          turnos.map((turno) => (
            <section
              key={turno.turnoId}
              aria-labelledby={`turno-${turno.turnoId}`}
              style={{ marginBottom: 24 }}
            >
              <h3 id={`turno-${turno.turnoId}`}>{turno.nombre}</h3>
              <p>
                Código: <strong>{turno.codigo}</strong> · Estado:{' '}
                {turno.estado === 'ACTIVO'
                  ? 'Activo'
                  : turno.estado === 'INACTIVO'
                    ? 'Inactivo'
                    : turno.estado}
              </p>

              {turno.horarios.length === 0 ? (
                <p>Este turno no tiene horarios asignados.</p>
              ) : (
                <div
                  className="ps-tabla-contenedor"
                  role="region"
                  aria-label={`Horarios de ${turno.nombre}`}
                  tabIndex={0}
                >
                  <table className="ps-tabla">
                    <thead>
                      <tr>
                        <th scope="col">Día</th>
                        <th scope="col">Entrada</th>
                        <th scope="col">Salida</th>
                      </tr>
                    </thead>
                    <tbody>
                      {turno.horarios.map((horario) => (
                        <tr key={horario.horarioId}>
                          <td>{DIAS[horario.diaSemana] || 'Día desconocido'}</td>
                          <td>{horario.horaEntrada.slice(0, 5)}</td>
                          <td>{horario.horaSalida.slice(0, 5)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          ))
        )}
      </div>

      <footer className="fe-pie">
                {onCrear && (
          <button
            type="button"
            className="ps-boton-primario"
            disabled={cargando || Boolean(error)}
            onClick={onCrear}
          >
            + Crear turno
          </button>
        )}
        <button
          type="button"
          className="ps-boton-secundario"
          disabled={cargando}
          onClick={() => setIntento((valor) => valor + 1)}
        >
          Actualizar
        </button>
        <button
          type="button"
          className="ps-boton-secundario"
          onClick={onCerrar}
        >
          Cerrar
        </button>
      </footer>
    </dialog>
  )
}
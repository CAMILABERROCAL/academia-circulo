import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { createPortal } from 'react-dom'
import { consultar, enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'

type Apoderado = {
  apoderadoId: string
  nombres: string
  apellidos: string
  dni: string | null
  celular: string | null
  estado: string
}

type Props = {
  estudianteId: string
  nombreAlumno: string
  onCerrar: () => void
  onVinculado: () => void
}

export default function VincularApoderado({
  estudianteId,
  nombreAlumno,
  onCerrar,
  onVinculado,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const enviando = useRef(false)
  const [texto, setTexto] = useState('')
  const [consulta, setConsulta] = useState<{ texto: string } | null>(null)
  const [resultados, setResultados] = useState<Apoderado[]>([])
  const [seleccionado, setSeleccionado] = useState<Apoderado | null>(null)
  const [parentesco, setParentesco] = useState('')
  const [cargando, setCargando] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [errorBusqueda, setErrorBusqueda] = useState('')
  const [errorGuardado, setErrorGuardado] = useState('')
  const [revisarFicha, setRevisarFicha] = useState(false)

  useEffect(() => {
    const elemento = dialogo.current
    if (elemento && !elemento.open) elemento.showModal()
    return () => elemento?.close()
  }, [])

  useEffect(() => {
    if (!consulta) return

    const controlador = new AbortController()

    async function buscar() {
      setCargando(true)
      setErrorBusqueda('')
      setResultados([])

      const parametros = new URLSearchParams({
        busqueda: consulta!.texto,
        pagina: '0',
        tamanio: '10',
      })

      try {
        const datos = await consultar<Apoderado[]>(
          `/api/secretaria/apoderados?${parametros}`,
          controlador.signal,
        )
        if (!controlador.signal.aborted) setResultados(datos)
      } catch (fallo) {
        if (!controlador.signal.aborted) {
          setErrorBusqueda(
            fallo instanceof ErrorApi && fallo.estado === 401
              ? 'Tu sesión ha finalizado. Vuelve a iniciar sesión.'
              : fallo instanceof TypeError
                ? 'No se pudo conectar con el servidor.'
                : fallo instanceof Error
                  ? fallo.message
                  : 'No se pudo buscar al apoderado.',
          )
        }
      } finally {
        if (!controlador.signal.aborted) setCargando(false)
      }
    }

    void buscar()
    return () => controlador.abort()
  }, [consulta])

  function cerrar() {
    if (!enviando.current) onCerrar()
  }

  function buscar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    evento.stopPropagation()
    if (enviando.current || revisarFicha || !texto.trim()) return
    setSeleccionado(null)
    setParentesco('')
    setErrorGuardado('')
    setCargando(true)
    setConsulta({ texto: texto.trim() })
  }

  async function vincular(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    evento.stopPropagation()

    if (enviando.current || revisarFicha || !seleccionado || !parentesco) {
      return
    }

    enviando.current = true
    setGuardando(true)
    setErrorGuardado('')

    try {
      await enviar<void>(
        `/api/secretaria/estudiantes/${encodeURIComponent(estudianteId)}/apoderado`,
        {
          apoderadoId: seleccionado.apoderadoId,
          parentesco,
        },
      )
    } catch (fallo) {
      if (fallo instanceof TypeError) {
        setRevisarFicha(true)
        setErrorGuardado(
          'No se pudo confirmar el resultado. Cierra esta ventana y vuelve a consultar la ficha antes de intentar otra vinculación.',
        )
      } else {
        if (fallo instanceof ErrorApi && fallo.estado === 409) {
          setRevisarFicha(true)
        }
        setErrorGuardado(
          fallo instanceof ErrorApi && fallo.estado === 401
            ? 'Tu sesión ha finalizado. Vuelve a iniciar sesión.'
            : fallo instanceof Error
              ? fallo.message
              : 'No se pudo vincular al apoderado.',
        )
      }
      return
    } finally {
      enviando.current = false
      setGuardando(false)
    }

    onVinculado()
  }

  return createPortal(
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="va-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        evento.stopPropagation()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">APODERADO DEL ALUMNO</p>
          <h2 id="va-titulo">Vincular apoderado</h2>
        </div>
        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar vinculación"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido">
        <p className="fe-ayuda">
          Alumno: <strong>{nombreAlumno}</strong>.
          Busca una cuenta existente y revisa sus datos antes de vincularla.
        </p>

        <form onSubmit={buscar} className="ps-busqueda">
          <div className="ps-busqueda-campo">
            <label htmlFor="va-busqueda">Nombre, DNI o celular</label>
            <input
              id="va-busqueda"
              type="search"
              required
              maxLength={120}
              value={texto}
              disabled={guardando || revisarFicha}
              onChange={(evento) => setTexto(evento.target.value)}
            />
          </div>
          <button
            type="submit"
            className="ps-boton-secundario"
            disabled={cargando || guardando || revisarFicha}
          >
            Buscar
          </button>
        </form>

        {errorBusqueda && (
          <p className="ps-error" role="alert">{errorBusqueda}</p>
        )}

        {cargando && <p role="status">Buscando apoderados…</p>}

        {consulta && !cargando && !errorBusqueda && (
          <>
            {resultados.length === 0 ? (
              <p>No se encontraron apoderados con esa búsqueda.</p>
            ) : (
              <fieldset disabled={guardando || revisarFicha}>
                <legend>Selecciona al apoderado</legend>
                {resultados.map((apoderado) => (
                  <label
                    key={apoderado.apoderadoId}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 12,
                      padding: '12px 0',
                    }}
                  >
                    <input
                      type="radio"
                      name="va-apoderado"
                      style={{ width: 'auto' }}
                      disabled={apoderado.estado !== 'ACTIVO'}
                      checked={
                        seleccionado?.apoderadoId === apoderado.apoderadoId
                      }
                      onChange={() => {
                        setSeleccionado(apoderado)
                        setParentesco('')
                        setErrorGuardado('')
                      }}
                    />
                    <span>
                      <strong>
                        {apoderado.nombres} {apoderado.apellidos}
                      </strong>
                      <br />
                      DNI: {apoderado.dni || 'Sin registrar'} · Celular:{' '}
                      {apoderado.celular || 'Sin registrar'}
                      {apoderado.estado !== 'ACTIVO' && (
                        <> · No disponible ({apoderado.estado})</>
                      )}
                    </span>
                  </label>
                ))}
              </fieldset>
            )}

            {resultados.length === 10 && (
              <p className="fe-ayuda">
                Se muestran hasta diez resultados. Busca por DNI o celular
                para encontrar una cuenta específica.
              </p>
            )}
          </>
        )}

        {seleccionado && (
          <form id="vincular-apoderado" onSubmit={vincular}>
            <p>
              Vas a vincular a <strong>{seleccionado.nombres}{' '}
              {seleccionado.apellidos}</strong> con {nombreAlumno}.
            </p>
            <label htmlFor="va-parentesco">Parentesco con el alumno</label>
            <select
              id="va-parentesco"
              required
              value={parentesco}
              disabled={guardando || revisarFicha}
              onChange={(evento) => setParentesco(evento.target.value)}
            >
              <option value="">Selecciona el parentesco</option>
              <option value="PADRE">Padre</option>
              <option value="MADRE">Madre</option>
              <option value="TUTOR">Tutor</option>
              <option value="OTRO">Otro</option>
            </select>
          </form>
        )}

        {errorGuardado && (
          <p className="ps-error" role="alert">{errorGuardado}</p>
        )}
        {revisarFicha && (
          <p>Cierra esta ventana para volver a consultar la ficha.</p>
        )}
      </div>

      <footer className="fe-pie">
        <button
          type="button"
          className="ps-boton-secundario"
          disabled={guardando}
          onClick={cerrar}
        >
          {revisarFicha ? 'Cerrar y consultar ficha' : 'Cancelar'}
        </button>
        <button
          type="submit"
          form="vincular-apoderado"
          className="ps-boton-primario"
          disabled={
            guardando || revisarFicha || !seleccionado || !parentesco
          }
        >
          {guardando ? 'Vinculando…' : 'Vincular apoderado'}
        </button>
      </footer>
    </dialog>,
    document.body,
  )
}
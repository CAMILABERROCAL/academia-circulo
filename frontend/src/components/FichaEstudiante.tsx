import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { consultar, enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'
import HistorialFicha from './HistorialFicha'
import VincularApoderado from './VincularApoderado'
import MatriculaActual from './MatriculaActual'

type Ficha = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
  fechaNacimiento: string | null
  correo: string | null
  celular: string | null
  direccionReferencial: string | null
  sigueTratamientoMedico: boolean | null
  tieneApoderado: boolean
  nombreApoderado: string | null
  celularApoderado: string | null
  parentescoApoderado: string | null
}

type Borrador = {
  nombres: string
  apellidos: string
  fechaNacimiento: string
  correo: string
  celular: string
  direccionReferencial: string
  tratamiento: string
}

type Props = {
  estudianteId: string
  onCerrar: () => void
  onGuardado: () => void
}

const VACIO: Borrador = {
  nombres: '',
  apellidos: '',
  fechaNacimiento: '',
  correo: '',
  celular: '',
  direccionReferencial: '',
  tratamiento: '',
}

function crearBorrador(ficha: Ficha): Borrador {
  return {
    nombres: ficha.nombres,
    apellidos: ficha.apellidos,
    fechaNacimiento: ficha.fechaNacimiento ?? '',
    correo: ficha.correo ?? '',
    celular: ficha.celular ?? '',
    direccionReferencial: ficha.direccionReferencial ?? '',
    tratamiento:
      ficha.sigueTratamientoMedico === null
        ? ''
        : ficha.sigueTratamientoMedico
          ? 'si'
          : 'no',
  }
}

function mostrarTexto(valor: string | null): string {
  return valor?.trim() || 'Sin registrar'
}

function mostrarFecha(valor: string | null): string {
  if (!valor) return 'Sin registrar'
  const partes = valor.split('-')
  return partes.length === 3
    ? `${partes[2]}/${partes[1]}/${partes[0]}`
    : valor
}

function obtenerMensaje(fallo: unknown): string {
  if (fallo instanceof ErrorApi && fallo.estado === 401) {
    return 'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.'
  }

  if (fallo instanceof TypeError) {
    return 'No se pudo confirmar la operación por un problema de conexión. Consulta la ficha antes de repetir el guardado.'
  }

  return fallo instanceof Error
    ? fallo.message
    : 'No se pudo completar la operación.'
}

export default function FichaEstudiante({
  estudianteId,
  onCerrar,
  onGuardado,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const enviando = useRef(false)

  const [ficha, setFicha] = useState<Ficha | null>(null)
  const [borrador, setBorrador] = useState<Borrador>(VACIO)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [editando, setEditando] = useState(false)
  const [errorCarga, setErrorCarga] = useState('')
  const [errorGuardado, setErrorGuardado] = useState('')
  const [aviso, setAviso] = useState('')
  const [intento, setIntento] = useState(0)
    const [vinculandoApoderado, setVinculandoApoderado] = useState(false)

  useEffect(() => {
    const elemento = dialogo.current

    if (elemento && !elemento.open) elemento.showModal()

    return () => elemento?.close()
  }, [])

  useEffect(() => {
    const controlador = new AbortController()

    async function cargar() {
      setCargando(true)
      setErrorCarga('')
      setFicha(null)

      try {
        const datos = await consultar<Ficha>(
          `/api/secretaria/estudiantes/${encodeURIComponent(estudianteId)}/ficha`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setFicha(datos)
          setBorrador(crearBorrador(datos))
        }
      } catch (fallo) {
        if (!controlador.signal.aborted) {
          setErrorCarga(obtenerMensaje(fallo))
        }
      } finally {
        if (!controlador.signal.aborted) {
          setCargando(false)
        }
      }
    }

    void cargar()
    return () => controlador.abort()
  }, [estudianteId, intento])

  function cambiar(campo: keyof Borrador, valor: string) {
    setBorrador((anterior) => ({ ...anterior, [campo]: valor }))
  }

  function cerrar() {
        if (enviando.current || vinculandoApoderado) return

    if (
      editando &&
      ficha &&
      JSON.stringify(borrador) !== JSON.stringify(crearBorrador(ficha)) &&
      !window.confirm('Tienes cambios sin guardar. ¿Quieres descartarlos?')
    ) {
      return
    }

    onCerrar()
  }

  function cancelarEdicion() {
    if (!ficha || enviando.current) return

    if (
      JSON.stringify(borrador) !== JSON.stringify(crearBorrador(ficha)) &&
      !window.confirm('¿Quieres descartar los cambios de esta ficha?')
    ) {
      return
    }

    setBorrador(crearBorrador(ficha))
    setErrorGuardado('')
    setEditando(false)
  }

  async function guardar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando.current || !ficha) return

    const solicitud = {
      nombres: borrador.nombres.trim(),
      apellidos: borrador.apellidos.trim(),
      fechaNacimiento: borrador.fechaNacimiento,
      correo: borrador.correo.trim(),
      celular: borrador.celular.trim(),
      direccionReferencial: borrador.direccionReferencial.trim(),
      sigueTratamientoMedico: borrador.tratamiento === 'si',
    }

    if (
      !solicitud.nombres ||
      !solicitud.apellidos ||
      !solicitud.correo ||
      !solicitud.direccionReferencial ||
      !['si', 'no'].includes(borrador.tratamiento)
    ) {
      setErrorGuardado('Completa los campos obligatorios con datos válidos.')
      return
    }

    enviando.current = true
    setGuardando(true)
    setErrorGuardado('')
    setAviso('')

    try {
      await enviar<void>(
        `/api/secretaria/estudiantes/${encodeURIComponent(estudianteId)}/ficha`,
        solicitud,
        'PUT',
      )

      setEditando(false)
      setAviso('Ficha actualizada correctamente.')
      onGuardado()
      setIntento((valor) => valor + 1)
    } catch (fallo) {
      setErrorGuardado(obtenerMensaje(fallo))
    } finally {
      enviando.current = false
      setGuardando(false)
    }
  }

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="fe-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">INFORMACIÓN DEL ESTUDIANTE</p>
          <h2 id="fe-titulo">
            {editando ? 'Editar ficha' : 'Ficha del alumno'}
          </h2>
        </div>

        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar ficha del alumno"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido" aria-busy={cargando || guardando}>
        {aviso && (
          <div className="mensaje mensaje-aviso" role="status">{aviso}</div>
        )}

        {cargando ? (
          <p className="fe-estado" role="status">Cargando ficha…</p>
        ) : errorCarga ? (
          <div className="fe-estado">
            <p className="ps-error" role="alert">{errorCarga}</p>
            <button
              type="button"
              className="ps-boton-secundario"
              onClick={() => setIntento((valor) => valor + 1)}
            >
              Reintentar consulta
            </button>
          </div>
        ) : ficha ? (
          <>
            <div className="fe-identidad">
              <span className="fe-avatar" aria-hidden="true">
                {ficha.nombres.charAt(0)}
                {ficha.apellidos.charAt(0)}
              </span>
              <div>
                <h3>{ficha.nombres} {ficha.apellidos}</h3>
                <span className="ps-codigo">{ficha.codigoEstudiante}</span>
              </div>
            </div>

            {editando ? (
              <form id="formulario-ficha" onSubmit={guardar}>
                <p className="fe-ayuda">Todos los campos son obligatorios.</p>

                {errorGuardado && (
                  <div className="ps-error" role="alert">{errorGuardado}</div>
                )}

                <fieldset disabled={guardando} className="fe-formulario">
                  <div>
                    <label htmlFor="fe-nombres">Nombres</label>
                    <input
                      id="fe-nombres"
                      required
                      maxLength={120}
                      value={borrador.nombres}
                      onChange={(e) => cambiar('nombres', e.target.value)}
                    />
                  </div>

                  <div>
                    <label htmlFor="fe-apellidos">Apellidos</label>
                    <input
                      id="fe-apellidos"
                      required
                      maxLength={120}
                      value={borrador.apellidos}
                      onChange={(e) => cambiar('apellidos', e.target.value)}
                    />
                  </div>

                  <div>
                    <label htmlFor="fe-nacimiento">Fecha de nacimiento</label>
                    <input
                      id="fe-nacimiento"
                      type="date"
                      required
                      value={borrador.fechaNacimiento}
                      onChange={(e) => cambiar('fechaNacimiento', e.target.value)}
                    />
                  </div>

                  <div>
                    <label htmlFor="fe-celular">Celular</label>
                    <input
                      id="fe-celular"
                      type="tel"
                      required
                      maxLength={12}
                      pattern={String.raw`(?:\+51)?9[0-9]{8}`}
                      value={borrador.celular}
                      onChange={(e) => cambiar('celular', e.target.value)}
                    />
                  </div>

                  <div className="fe-ancho-completo">
                    <label htmlFor="fe-correo">Correo electrónico</label>
                    <input
                      id="fe-correo"
                      type="email"
                      required
                      maxLength={254}
                      value={borrador.correo}
                      onChange={(e) => cambiar('correo', e.target.value)}
                    />
                  </div>

                  <div className="fe-ancho-completo">
                    <label htmlFor="fe-direccion">Dirección referencial</label>
                    <input
                      id="fe-direccion"
                      required
                      maxLength={255}
                      value={borrador.direccionReferencial}
                      onChange={(e) => cambiar('direccionReferencial', e.target.value)}
                    />
                  </div>

                  <div className="fe-ancho-completo">
                    <label htmlFor="fe-tratamiento">¿Sigue tratamiento médico?</label>
                    <select
                      id="fe-tratamiento"
                      required
                      value={borrador.tratamiento}
                      onChange={(e) => cambiar('tratamiento', e.target.value)}
                    >
                      <option value="">Selecciona una respuesta</option>
                      <option value="si">Sí</option>
                      <option value="no">No</option>
                    </select>
                  </div>
                </fieldset>
              </form>
            ) : (
              <dl className="fe-datos">
                <div>
                  <dt>Nombres</dt>
                  <dd>{ficha.nombres}</dd>
                </div>
                <div>
                  <dt>Apellidos</dt>
                  <dd>{ficha.apellidos}</dd>
                </div>
                <div>
                  <dt>Fecha de nacimiento</dt>
                  <dd>{mostrarFecha(ficha.fechaNacimiento)}</dd>
                </div>
                <div>
                  <dt>Celular</dt>
                  <dd>{mostrarTexto(ficha.celular)}</dd>
                </div>
                <div className="fe-ancho-completo">
                  <dt>Correo electrónico</dt>
                  <dd>{mostrarTexto(ficha.correo)}</dd>
                </div>
                <div className="fe-ancho-completo">
                  <dt>Dirección referencial</dt>
                  <dd>{mostrarTexto(ficha.direccionReferencial)}</dd>
                </div>
                <div>
                  <dt>Sigue tratamiento médico</dt>
                  <dd>
                    {ficha.sigueTratamientoMedico === null
                      ? 'Sin registrar'
                      : ficha.sigueTratamientoMedico ? 'Sí' : 'No'}
                  </dd>
                </div>
                                                <div>
                  <dt>Apoderado vinculado</dt>
                  <dd>
                    {ficha.tieneApoderado ? 'Sí' : 'No'}

                    {!ficha.tieneApoderado && (
                      <div style={{ marginTop: 12 }}>
                        <button
                          type="button"
                          className="ps-boton-secundario"
                          onClick={() => {
                            setAviso('')
                            setVinculandoApoderado(true)
                          }}
                        >
                          Vincular apoderado
                        </button>
                      </div>
                    )}
                  </dd>
                </div>

                {ficha.tieneApoderado && (
                  <>
                    <div className="fe-ancho-completo">
                      <dt>Nombre completo del apoderado</dt>
                      <dd>{mostrarTexto(ficha.nombreApoderado)}</dd>
                    </div>

                    <div>
                      <dt>Celular del apoderado</dt>
                      <dd>{mostrarTexto(ficha.celularApoderado)}</dd>
                    </div>

                    <div>
                      <dt>Parentesco con el alumno</dt>
                      <dd>
                        {ficha.parentescoApoderado === 'PADRE'
                          ? 'Padre'
                          : ficha.parentescoApoderado === 'MADRE'
                            ? 'Madre'
                            : ficha.parentescoApoderado === 'TUTOR'
                              ? 'Tutor'
                              : ficha.parentescoApoderado === 'OTRO'
                                ? 'Otro'
                                : mostrarTexto(ficha.parentescoApoderado)}
                      </dd>
                    </div>
                  </>
                )}
              </dl>
            )}
                    </>
        ) : null}

{ficha && !cargando && !errorCarga && !editando && (
  <>
    <MatriculaActual
      key={`matricula-${estudianteId}-${intento}`}
      estudianteId={estudianteId}
    />

    <HistorialFicha
      key={`${estudianteId}-${intento}`}
      estudianteId={estudianteId}
    />
  </>
)}
      </div>

      <footer className="fe-pie">
  {editando ? (
    <div key="acciones-edicion" className="fe-acciones">
      <button
        type="button"
        className="ps-boton-secundario"
        disabled={guardando}
        onClick={cancelarEdicion}
      >
        Cancelar
      </button>

      <button
        type="submit"
        form="formulario-ficha"
        className="ps-boton-primario"
        disabled={guardando}
      >
        {guardando ? 'Guardando…' : 'Guardar cambios'}
      </button>
    </div>
  ) : (
    <div key="acciones-consulta" className="fe-acciones">
      <button
        type="button"
        className="ps-boton-secundario"
        onClick={cerrar}
      >
        Cerrar
      </button>

      {ficha && !cargando && !errorCarga && (
        <button
          type="button"
          className="ps-boton-primario"
          onClick={(evento) => {
            evento.preventDefault()
            setBorrador(crearBorrador(ficha))
            setErrorGuardado('')
            setAviso('')
            setEditando(true)
          }}
        >
          Editar ficha
        </button>
      )}
    </div>
  )}
</footer>

      {vinculandoApoderado && ficha && (
        <VincularApoderado
          estudianteId={estudianteId}
          nombreAlumno={`${ficha.nombres} ${ficha.apellidos}`}
          onCerrar={() => {
            setVinculandoApoderado(false)
            setIntento((valor) => valor + 1)
          }}
          onVinculado={() => {
            setVinculandoApoderado(false)
            setAviso('Apoderado vinculado correctamente.')
            setIntento((valor) => valor + 1)
            onGuardado()
          }}
        />
      )}
    </dialog>
  )
}
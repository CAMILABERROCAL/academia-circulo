import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { consultar, enviar, ErrorApi } from '../lib/api'
import './FichaEstudiante.css'
import './RegistrarAlumno.css'
import RegistrarApoderado from './RegistrarApoderado'

type Apoderado = {
  apoderadoId: string
  nombres: string
  apellidos: string
  dni: string | null
  celular: string | null
  estado: string
}

type AlumnoRegistrado = {
  estudianteId: string
  codigoEstudiante: string
  nombres: string
  apellidos: string
}

type Props = {
  onCerrar: () => void
  onRegistrado: () => void
}

const CAMPOS = [
  {
    nombre: 'codigoEstudiante',
    etiqueta: 'Código del alumno',
    tipo: 'text',
    maximo: 30,
  },
  {
    nombre: 'nombres',
    etiqueta: 'Nombres',
    tipo: 'text',
    maximo: 120,
  },
  {
    nombre: 'apellidos',
    etiqueta: 'Apellidos',
    tipo: 'text',
    maximo: 120,
  },
  {
    nombre: 'fechaNacimiento',
    etiqueta: 'Fecha de nacimiento',
    tipo: 'date',
    maximo: undefined,
  },
  {
    nombre: 'correo',
    etiqueta: 'Correo electrónico',
    tipo: 'email',
    maximo: 254,
  },
  {
    nombre: 'celular',
    etiqueta: 'Celular',
    tipo: 'tel',
    maximo: 12,
  },
  {
    nombre: 'direccionReferencial',
    etiqueta: 'Dirección referencial',
    tipo: 'text',
    maximo: 255,
  },
] as const

function mensajeError(fallo: unknown): string {
  if (fallo instanceof ErrorApi && fallo.estado === 401) {
    return 'Tu sesión ha finalizado. Cierra esta ventana y vuelve a ingresar.'
  }

  if (fallo instanceof TypeError) {
    return 'No se pudo conectar con el servidor.'
  }

  return fallo instanceof Error
    ? fallo.message
    : 'No se pudo completar la operación.'
}

export default function RegistrarAlumno({
  onCerrar,
  onRegistrado,
}: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)
  const formulario = useRef<HTMLFormElement>(null)
  const passwordInput = useRef<HTMLInputElement>(null)
  const enviando = useRef(false)
  const modificado = useRef(false)

  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [registrado, setRegistrado] = useState<AlumnoRegistrado | null>(null)

  const [textoBusqueda, setTextoBusqueda] = useState('')
  const [busqueda, setBusqueda] = useState<string | null>(null)
  const [intentoBusqueda, setIntentoBusqueda] = useState(0)
  const [buscando, setBuscando] = useState(false)
  const [errorBusqueda, setErrorBusqueda] = useState('')
  const [resultados, setResultados] = useState<Apoderado[]>([])
  const [seleccionado, setSeleccionado] = useState<Apoderado | null>(null)
  const [parentesco, setParentesco] = useState('')

  const [creandoApoderado, setCreandoApoderado] = useState(false)
const [avisoApoderado, setAvisoApoderado] = useState('')

  useEffect(() => {
    const elemento = dialogo.current

    if (elemento && !elemento.open) elemento.showModal()

    return () => elemento?.close()
  }, [])

  useEffect(() => {
    if (busqueda === null || registrado) return

    const controlador = new AbortController()

    async function buscar() {
      setBuscando(true)
      setErrorBusqueda('')
      setResultados([])

      const parametros = new URLSearchParams({
        busqueda: busqueda ?? '',
        pagina: '0',
        tamanio: '10',
      })

      try {
        const datos = await consultar<Apoderado[]>(
          `/api/secretaria/apoderados?${parametros}`,
          controlador.signal,
        )

        if (!controlador.signal.aborted) {
          setResultados(datos)
        }
      } catch (fallo) {
        if (!controlador.signal.aborted) {
          setErrorBusqueda(mensajeError(fallo))
        }
      } finally {
        if (!controlador.signal.aborted) setBuscando(false)
      }
    }

    void buscar()
    return () => controlador.abort()
  }, [busqueda, intentoBusqueda, registrado])

  function buscarApoderados() {
    if (enviando.current) return

    const texto = textoBusqueda.trim()

    if (!texto) {
      setErrorBusqueda('Escribe un DNI, celular o nombre para buscar.')
      return
    }

    setBusqueda(texto)
    setIntentoBusqueda((valor) => valor + 1)
  }

  function seleccionar(apoderado: Apoderado) {
    if (enviando.current || apoderado.estado !== 'ACTIVO') return

    setSeleccionado(apoderado)
    setParentesco('')
    modificado.current = true
  }

  function cerrar() {
    if (enviando.current || creandoApoderado) return

    if (
      !registrado &&
      modificado.current &&
      !window.confirm('¿Quieres cerrar y descartar los datos del registro?')
    ) {
      return
    }

    onCerrar()
  }

  async function registrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando.current || registrado) return

    const datos = new FormData(evento.currentTarget)
    const texto = (nombre: string) => String(datos.get(nombre) ?? '').trim()

    const passwordTemporal = String(datos.get('passwordTemporal') ?? '')
    const ficha = {
      nombres: texto('nombres'),
      apellidos: texto('apellidos'),
      fechaNacimiento: texto('fechaNacimiento'),
      correo: texto('correo'),
      celular: texto('celular'),
      direccionReferencial: texto('direccionReferencial'),
      sigueTratamientoMedico: texto('tratamiento') === 'si',
    }

    setError('')

    if (
      !ficha.nombres ||
      !ficha.apellidos ||
      !ficha.direccionReferencial
    ) {
      setError('Los nombres, apellidos y dirección no pueden quedar vacíos.')
      return
    }

    if (new TextEncoder().encode(passwordTemporal).length > 72) {
      setError('La contraseña temporal no debe superar 72 bytes UTF-8.')
      return
    }

    if (seleccionado && !parentesco) {
      setError('Selecciona el parentesco del apoderado.')
      return
    }

    const solicitud = {
      codigoEstudiante: texto('codigoEstudiante'),
      passwordTemporal,
      ficha,
      ...(seleccionado
        ? {
            apoderado: {
              apoderadoId: seleccionado.apoderadoId,
              parentesco,
            },
          }
        : {}),
    }

    enviando.current = true
    setGuardando(true)

    try {
      const resultado = await enviar<AlumnoRegistrado>(
        '/api/secretaria/estudiantes',
        solicitud,
      )

      // Limpiar los datos del formulario después del registro.
      formulario.current?.reset()
      modificado.current = false
      setRegistrado(resultado)
      onRegistrado()
    } catch (fallo) {
      setError(
        fallo instanceof TypeError
          ? 'No se pudo confirmar el registro por un problema de conexión. Busca el código del alumno en el listado antes de repetirlo.'
          : mensajeError(fallo),
      )
    } finally {
      if (passwordInput.current) passwordInput.current.value = ''
      enviando.current = false
      setGuardando(false)
    }
  }

  return (
    <dialog
      ref={dialogo}
      className="fe-dialogo"
      aria-labelledby="ra-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        cerrar()
      }}
    >
      <header className="fe-cabecera">
        <div>
          <p className="fe-antetitulo">GESTIÓN DE ESTUDIANTES</p>
          <h2 id="ra-titulo">
            {registrado ? 'Alumno registrado' : 'Registrar alumno'}
          </h2>
        </div>

        <button
          type="button"
          className="fe-cerrar"
          aria-label="Cerrar registro"
          disabled={guardando}
          onClick={cerrar}
          autoFocus
        >
          ×
        </button>
      </header>

      <div className="fe-contenido">
        {registrado ? (
          <div key="registro-completo" className="ra-confirmacion" role="status">
            <span className="ra-exito" aria-hidden="true">✓</span>
            <h3>{registrado.nombres} {registrado.apellidos}</h3>
            <p>
              Código: <strong>{registrado.codigoEstudiante}</strong>
            </p>
            <p>La cuenta y la ficha se guardaron correctamente.</p>
            <p>
              El alumno deberá cambiar su contraseña temporal al ingresar.
            </p>
          </div>
        ) : (
          <form
            key="formulario-registro"
            id="registro-alumno"
            ref={formulario}
            onSubmit={registrar}
            onChange={() => { modificado.current = true }}
          >
            <p className="fe-ayuda">
              Todos los datos del alumno son obligatorios.
              El apoderado es obligatorio para menores de edad.
            </p>

            {error && (
              <div className="ps-error" role="alert">{error}</div>
            )}

            <fieldset disabled={guardando} className="fe-formulario">
              {CAMPOS.map((campo) => (
                <div
                  key={campo.nombre}
                  className={
                    campo.nombre === 'direccionReferencial'
                      ? 'fe-ancho-completo'
                      : undefined
                  }
                >
                  <label htmlFor={`ra-${campo.nombre}`}>{campo.etiqueta}</label>
                  <input
                    id={`ra-${campo.nombre}`}
                    name={campo.nombre}
                    type={campo.tipo}
                    maxLength={campo.maximo}
                    required
                    pattern={
                      campo.nombre === 'codigoEstudiante'
                        ? '[A-Za-z0-9_\\-]+'
                        : campo.nombre === 'celular'
                          ? String.raw`(?:\+51)?9[0-9]{8}`
                          : undefined
                    }
                  />
                </div>
              ))}

              <div>
                <label htmlFor="ra-tratamiento">¿Sigue tratamiento médico?</label>
                <select
                  id="ra-tratamiento"
                  name="tratamiento"
                  defaultValue=""
                  required
                >
                  <option value="">Selecciona una respuesta</option>
                  <option value="si">Sí</option>
                  <option value="no">No</option>
                </select>
              </div>

              <div>
                <label htmlFor="ra-password">Contraseña temporal</label>
                <input
                  ref={passwordInput}
                  id="ra-password"
                  name="passwordTemporal"
                  type="password"
                  autoComplete="new-password"
                  minLength={12}
                  maxLength={64}
                  required
                  aria-describedby="ra-password-ayuda"
                />
                <small id="ra-password-ayuda" className="ayuda">
                  Entre 12 y 64 caracteres.
                </small>
              </div>

              <section className="ra-apoderado fe-ancho-completo">
                <h3>Apoderado</h3>

<p className="fe-ayuda">
  Busca primero por DNI para comprobar si ya tiene cuenta.
  Si no existe, puedes registrarlo aquí.
  Los menores de edad deben tener apoderado.
</p>

<button
  type="button"
  className="ps-boton-secundario"
  disabled={guardando}
  onClick={(evento) => {
    evento.preventDefault()
    setAvisoApoderado('')
    setCreandoApoderado(true)
  }}
>
  + Registrar apoderado
</button>

{avisoApoderado && (
  <div className="mensaje mensaje-aviso" role="status">
    {avisoApoderado}
  </div>
)}

                <label htmlFor="ra-busqueda">DNI, celular o nombre</label>
                <div className="ra-busqueda">
                  <input
                    id="ra-busqueda"
                    type="search"
                    maxLength={120}
                    value={textoBusqueda}
                    onChange={(e) => setTextoBusqueda(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault()
                        buscarApoderados()
                      }
                    }}
                  />
                  <button
                    type="button"
                    className="ps-boton-secundario"
                    disabled={buscando}
                    onClick={buscarApoderados}
                  >
                    {buscando ? 'Buscando…' : 'Buscar'}
                  </button>
                </div>

                {errorBusqueda && (
                  <p className="ps-error" role="alert">{errorBusqueda}</p>
                )}

                {buscando ? (
                  <p role="status" className="fe-ayuda">Buscando apoderados…</p>
                ) : busqueda !== null && !errorBusqueda ? (
                  <>
                    {resultados.length === 0 && (
                      <p className="fe-ayuda">
                        No se encontraron apoderados. Prueba con otro dato.
                      </p>
                    )}

                    <ul className="ra-resultados">
                      {resultados.map((apoderado) => (
                        <li key={apoderado.apoderadoId}>
                          <div>
                            <strong>
                              {apoderado.nombres} {apoderado.apellidos}
                            </strong>
                            <small>
                              DNI: {apoderado.dni || 'Sin registrar'}
                              {' · '}
                              Celular: {apoderado.celular || 'Sin registrar'}
                            </small>
                            <small>Estado: {apoderado.estado}</small>
                          </div>

                          <button
                            type="button"
                            className="ps-boton-secundario"
                            disabled={
                              apoderado.estado !== 'ACTIVO' ||
                              seleccionado?.apoderadoId === apoderado.apoderadoId
                            }
                            onClick={() => seleccionar(apoderado)}
                          >
                            {seleccionado?.apoderadoId === apoderado.apoderadoId
                              ? 'Seleccionado'
                              : 'Seleccionar'}
                          </button>
                        </li>
                      ))}
                    </ul>

                    {resultados.length === 10 && (
                      <p className="fe-ayuda">
                        Mostramos hasta 10 coincidencias. Usa el DNI o un nombre
                        más específico si no encuentras al apoderado.
                      </p>
                    )}
                  </>
                ) : null}

                {seleccionado ? (
                  <div className="ra-seleccionado">
                    <strong>Apoderado seleccionado</strong>
                    <p>{seleccionado.nombres} {seleccionado.apellidos}</p>

                    <label htmlFor="ra-parentesco">Parentesco con el alumno</label>
                    <select
                      id="ra-parentesco"
                      required
                      value={parentesco}
                      onChange={(e) => setParentesco(e.target.value)}
                    >
                      <option value="">Selecciona el parentesco</option>
                      <option value="PADRE">Padre</option>
                      <option value="MADRE">Madre</option>
                      <option value="TUTOR">Tutor</option>
                      <option value="OTRO">Otro</option>
                    </select>

                    <button
                      type="button"
                      className="boton-texto"
                      onClick={() => {
                        setSeleccionado(null)
                        setParentesco('')
                        modificado.current = true
                      }}
                    >
                      Quitar selección
                    </button>
                  </div>
                ) : (
                  <p className="fe-ayuda">No has seleccionado un apoderado.</p>
                )}
              </section>
            </fieldset>
          </form>
        )}
      </div>

      <footer className="fe-pie">
        <button
          type="button"
          className="ps-boton-secundario"
          disabled={guardando}
          onClick={cerrar}
        >
          {registrado ? 'Cerrar' : 'Cancelar'}
        </button>

        {!registrado && (
          <button
            type="submit"
            form="registro-alumno"
            className="ps-boton-primario"
            disabled={guardando}
          >
            {guardando ? 'Registrando…' : 'Registrar alumno'}
          </button>
        )}
            </footer>

      {creandoApoderado && (
        <RegistrarApoderado
          onCerrar={() => setCreandoApoderado(false)}
          onCreado={(apoderado) => {
            setSeleccionado({
              apoderadoId: apoderado.apoderadoId,
              nombres: apoderado.nombres,
              apellidos: apoderado.apellidos,
              dni: apoderado.dni,
              celular: apoderado.celular,
              estado: 'ACTIVO',
            })

            setParentesco('')
            setTextoBusqueda('')
            setBusqueda(null)
            setResultados([])
            setErrorBusqueda('')
            setError('')
            setAvisoApoderado(
              'Apoderado registrado y seleccionado. Elige su parentesco con el alumno.',
            )

            modificado.current = true
            setCreandoApoderado(false)
          }}
        />
      )}
    </dialog>
  )
}
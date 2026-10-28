import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { consultarSesion, enviar, ErrorApi } from './lib/api'
import type { Sesion } from './lib/api'
import PanelSecretaria from './components/PanelSecretaria'
import PanelAdministrador from './components/PanelAdministrador'
import PanelApoderado from './components/PanelApoderado'
import PanelAlumno from './components/PanelAlumno'
import PanelDirector from './components/PanelDirector'


type TipoAcceso = 'ALUMNO' | 'APODERADO' | 'PERSONAL'

function mensajeError(error: unknown): string {
  if (error instanceof TypeError) {
    return 'No se pudo conectar con el servidor. Comprueba que esté encendido.'
  }

  return error instanceof Error
    ? error.message
    : 'Ocurrió un problema. Inténtalo nuevamente.'
}

export default function App() {
  const [sesion, setSesion] = useState<Sesion | null>(null)
  const [cargando, setCargando] = useState(true)
  const [ocupado, setOcupado] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  const [tipoAcceso, setTipoAcceso] = useState<TipoAcceso>('ALUMNO')
  const [usarUsuario, setUsarUsuario] = useState(false)
  const [identificador, setIdentificador] = useState('')
  const [codigoAlumno, setCodigoAlumno] = useState('')
  const [password, setPassword] = useState('')
  const [visible, setVisible] = useState(false)

  const [actual, setActual] = useState('')
  const [nueva, setNueva] = useState('')
  const [repetida, setRepetida] = useState('')

  useEffect(() => {
    let vigente = true

    consultarSesion()
      .then((datos) => {
        if (vigente) setSesion(datos)
      })
      .catch((fallo: unknown) => {
        if (vigente) setError(mensajeError(fallo))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })

    return () => {
      vigente = false
    }
  }, [])

  function cambiarTipo(tipo: TipoAcceso) {
    setTipoAcceso(tipo)
    setUsarUsuario(false)
    setIdentificador('')
    setCodigoAlumno('')
    setPassword('')
    setVisible(false)
    setError('')
    setAviso('')
  }

  async function ingresar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setError('')
    setAviso('')
    setOcupado(true)

    try {
      const accesoConUsuario = tipoAcceso === 'PERSONAL' && usarUsuario

      const datos = accesoConUsuario
        ? {
            identificador: identificador.trim(),
            password,
          }
        : {
            tipoAcceso,
            celular: identificador.trim(),
            password,
            ...(tipoAcceso === 'ALUMNO'
              ? { codigoAlumno: codigoAlumno.trim() }
              : {}),
          }

      const resultado = await enviar<Sesion>(
        accesoConUsuario
          ? '/api/auth/login'
          : '/api/auth/login-celular',
        datos,
      )

      setSesion(resultado)
    } catch (fallo) {
      setError(mensajeError(fallo))
    } finally {
      setPassword('')
      setVisible(false)
      setOcupado(false)
    }
  }

  async function cambiarPassword(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setError('')
    setAviso('')

    if (nueva !== repetida) {
      setError('Las contraseñas nuevas no coinciden.')
      return
    }

    if (nueva === actual) {
      setError('La nueva contraseña debe ser distinta de la actual.')
      return
    }

    if (new TextEncoder().encode(nueva).length > 72) {
      setError('La nueva contraseña no debe superar 72 bytes UTF-8.')
      return
    }

    setOcupado(true)

    try {
      await enviar<void>('/api/auth/password', {
        passwordActual: actual,
        passwordNueva: nueva,
      })

      setSesion(null)
      setAviso('Contraseña actualizada. Inicia sesión con tu nueva contraseña.')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.estado === 401) {
        setSesion(null)
      }

      setError(mensajeError(fallo))
    } finally {
      setActual('')
      setNueva('')
      setRepetida('')
      setOcupado(false)
    }
  }

  async function salir() {
    setError('')
    setAviso('')
    setOcupado(true)

    try {
      await enviar<void>('/api/auth/logout')
      setSesion(null)
      setActual('')
      setNueva('')
      setRepetida('')
      setAviso('Sesión cerrada.')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.estado === 401) {
        setSesion(null)
        setAviso('La sesión ya había finalizado.')
      } else {
        setError(mensajeError(fallo))
      }
    } finally {
      setOcupado(false)
    }
  }

  const accesoConUsuario = tipoAcceso === 'PERSONAL' && usarUsuario

  if (
    !cargando &&
    sesion &&
    !sesion.debeCambiarPassword &&
    sesion.roles.includes('ROLE_ADMIN')
  ) {
    return (
      <PanelAdministrador
        sesion={sesion}
        ocupado={ocupado}
        errorSesion={error}
        onSalir={salir}
      />
    )
  }

  if (
    !cargando &&
    sesion &&
    !sesion.debeCambiarPassword &&
    sesion.roles.includes('ROLE_SECRETARIA')
  ) {
    return (
      <PanelSecretaria
        sesion={sesion}
        ocupado={ocupado}
        errorSesion={error}
        onSalir={salir}
      />
    )
  }

  if (
    !cargando &&
    sesion &&
    !sesion.debeCambiarPassword &&
    sesion.roles.includes('ROLE_APODERADO')
  ) {
    return (
      <PanelApoderado
        key={sesion.usuarioId}
        sesion={sesion}
        ocupado={ocupado}
        errorSesion={error}
        onSalir={salir}
      />
    )
  }

  if (
    !cargando &&
    sesion &&
    !sesion.debeCambiarPassword &&
    sesion.roles.includes('ROLE_ESTUDIANTE')
  ) {
    return (
      <PanelAlumno
        key={sesion.usuarioId}
        sesion={sesion}
        ocupado={ocupado}
        errorSesion={error}
        onSalir={salir}
      />
    )
  }

  if (
    !cargando &&
    sesion &&
    !sesion.debeCambiarPassword &&
    sesion.roles.includes('ROLE_DIRECTOR')
  ) {
    return (
      <PanelDirector
        key={sesion.usuarioId}
        sesion={sesion}
        ocupado={ocupado}
        errorSesion={error}
        onSalir={salir}
      />
    )
  }

  return (
    <div className="pagina-acceso">
      <div className="fondo-azul" aria-hidden="true" />

      <header className="cabecera">
        <a className="marca" href="/" aria-label="Academia Círculo, inicio">
          <span className="marca-simbolo" aria-hidden="true">C</span>
          <span>
            <small>ACADEMIA</small>
            <strong>CÍRCULO</strong>
          </span>
        </a>

        <div className="lema">
          <strong>SISTEMA ACADÉMICO</strong>
          <span>Tu aprendizaje, cada día más cerca</span>
        </div>
      </header>

      <main className="contenido-acceso">
        <section className="tarjeta-acceso" aria-labelledby="titulo-acceso">
          <div className="emblema" aria-hidden="true">C</div>

          <h1 id="titulo-acceso">
            {cargando
              ? 'Preparando tu acceso'
              : sesion?.debeCambiarPassword
                ? 'Actualiza tu contraseña'
                : sesion
                  ? `Hola, ${sesion.nombres}`
                  : 'Sistema Académico'}
          </h1>

          <p className="subtitulo">
            {sesion?.debeCambiarPassword
              ? 'Elige una contraseña personal para continuar.'
              : sesion
                ? 'Tu sesión está activa.'
                : 'Bienvenido a Academia Círculo'}
          </p>

          <div className="separador" />

          {error && (
            <div className="mensaje mensaje-error" role="alert">
              {error}
            </div>
          )}

          {aviso && (
            <div className="mensaje mensaje-aviso" role="status">
              {aviso}
            </div>
          )}

          {cargando ? (
            <p className="estado-carga" role="status">
              Consultando tu sesión…
            </p>
          ) : sesion?.debeCambiarPassword ? (
            <form onSubmit={cambiarPassword}>
              <fieldset disabled={ocupado}>
                <label htmlFor="password-actual">Contraseña actual</label>
                <input
                  id="password-actual"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={actual}
                  onChange={(evento) => setActual(evento.target.value)}
                />

                <label htmlFor="password-nueva">Nueva contraseña</label>
                <input
                  id="password-nueva"
                  type="password"
                  autoComplete="new-password"
                  minLength={12}
                  maxLength={64}
                  required
                  value={nueva}
                  onChange={(evento) => setNueva(evento.target.value)}
                  aria-describedby="ayuda-password"
                />
                <small id="ayuda-password" className="ayuda">
                  Usa entre 12 y 64 caracteres.
                </small>

                <label htmlFor="password-repetida">
                  Repite la nueva contraseña
                </label>
                <input
                  id="password-repetida"
                  type="password"
                  autoComplete="new-password"
                  minLength={12}
                  maxLength={64}
                  required
                  value={repetida}
                  onChange={(evento) => setRepetida(evento.target.value)}
                />

                <button className="boton-principal" type="submit">
                  {ocupado ? 'Guardando…' : 'Guardar contraseña'}
                </button>
              </fieldset>

              <button
                className="boton-texto"
                type="button"
                disabled={ocupado}
                onClick={salir}
              >
                Cerrar sesión
              </button>
            </form>
          ) : sesion ? (
            <div className="sesion-confirmada">
              <div className="confirmacion-icono" aria-hidden="true">✓</div>
              <p>Has ingresado correctamente a tu cuenta.</p>
              <button
                className="boton-principal"
                type="button"
                disabled={ocupado}
                onClick={salir}
              >
                {ocupado ? 'Cerrando…' : 'Cerrar sesión'}
              </button>
            </div>
          ) : (
            <form onSubmit={ingresar}>
              <fieldset disabled={ocupado}>
                <label htmlFor="tipo-acceso">Tipo de acceso</label>
                <select
                  id="tipo-acceso"
                  value={tipoAcceso}
                  onChange={(evento) =>
                    cambiarTipo(evento.target.value as TipoAcceso)
                  }
                >
                  <option value="ALUMNO">Alumno</option>
                  <option value="APODERADO">Apoderado</option>
                  <option value="PERSONAL">Personal de la academia</option>
                </select>

                {tipoAcceso === 'PERSONAL' && (
                  <label className="opcion-usuario">
                    <input
                      type="checkbox"
                      checked={usarUsuario}
                      onChange={(evento) => {
                        setUsarUsuario(evento.target.checked)
                        setIdentificador('')
                        setPassword('')
                        setError('')
                      }}
                    />
                    Ingresar con nombre de usuario
                  </label>
                )}

                <label htmlFor="identificador">
                  {accesoConUsuario ? 'Nombre de usuario' : 'Número de celular'}
                </label>
                <input
                  id="identificador"
                  type={accesoConUsuario ? 'text' : 'tel'}
                  autoComplete="username"
                  placeholder={accesoConUsuario ? 'Tu usuario' : '9 dígitos o +51'}
                  pattern={
                    accesoConUsuario
                      ? undefined
                      : String.raw`(?:\+51)?9[0-9]{8}`
                  }
                  maxLength={accesoConUsuario ? 80 : 12}
                  required
                  value={identificador}
                  onChange={(evento) => setIdentificador(evento.target.value)}
                />

                {tipoAcceso === 'ALUMNO' && (
                  <>
                    <label htmlFor="codigo-alumno">Código del alumno</label>
                    <input
                      id="codigo-alumno"
                      type="text"
                      maxLength={30}
                      required
                      placeholder="Tu código de estudiante"
                      value={codigoAlumno}
                      onChange={(evento) => setCodigoAlumno(evento.target.value)}
                    />
                  </>
                )}

                <label htmlFor="password">Contraseña</label>
                <div className="campo-password">
                  <input
                    id="password"
                    type={visible ? 'text' : 'password'}
                    autoComplete="current-password"
                    maxLength={64}
                    required
                    value={password}
                    onChange={(evento) => setPassword(evento.target.value)}
                  />
                  <button
                    type="button"
                    aria-label={
                      visible ? 'Ocultar contraseña' : 'Mostrar contraseña'
                    }
                    aria-pressed={visible}
                    onClick={() => setVisible(!visible)}
                  >
                    {visible ? 'Ocultar' : 'Mostrar'}
                  </button>
                </div>

                <button className="boton-principal" type="submit">
                  {ocupado ? 'Ingresando…' : 'Iniciar sesión →'}
                </button>
              </fieldset>

              <p className="ayuda-acceso">
                Si necesitas ayuda para ingresar, comunícate con secretaría.
              </p>
            </form>
          )}
        </section>
      </main>

      <footer className="pie">
        <span>Academia Círculo</span>
        <span>Aprendiendo para tu futuro</span>
      </footer>
    </div>
  )
}
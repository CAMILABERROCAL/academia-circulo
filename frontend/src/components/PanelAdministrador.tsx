import type { Sesion } from '../lib/api'
import ListadoCiclos from './ListadoCiclos'
import './PanelSecretaria.css'
import { useState } from 'react'
import CrearCiclo from './CrearCiclo'

type Props = {
  sesion: Sesion
  ocupado: boolean
  errorSesion: string
  onSalir: () => Promise<void>
}

export default function PanelAdministrador({
  sesion,
  ocupado,
  errorSesion,
  onSalir,
}: Props) {
      const [creandoCiclo, setCreandoCiclo] = useState(false)
  const [versionListado, setVersionListado] = useState(0)
  const [aviso, setAviso] = useState('')
  const iniciales = [
    sesion.nombres.trim().charAt(0),
    sesion.apellidos.trim().charAt(0),
  ].join('')

  return (
    <div className="panel-secretaria">
      <aside className="ps-lateral" aria-label="Menú de administración">
        <div className="ps-marca">
          <span className="ps-marca-icono" aria-hidden="true">C</span>
          <div>
            <small>ACADEMIA</small>
            <strong>CÍRCULO</strong>
          </div>
        </div>

        <p className="ps-menu-titulo">ADMINISTRACIÓN</p>

        <nav className="ps-navegacion" aria-label="Secciones disponibles">
          <a
            className="ps-menu-opcion ps-menu-activo"
            href="#ciclos-admin"
            aria-current="page"
          >
            <span aria-hidden="true">▤</span>
            Ciclos
          </a>
        </nav>

        <div className="ps-lateral-pie">
          <span>Gestión académica</span>
          <small>Academia Círculo</small>
        </div>
      </aside>

      <div className="ps-cuerpo">
        <header className="ps-cabecera">
          <span className="ps-ubicacion">Área de administración</span>

          <div className="ps-cuenta">
            <span className="ps-avatar" aria-hidden="true">
              {iniciales}
            </span>
            <div className="ps-nombre-cuenta">
              <strong>{sesion.nombres}</strong>
              <small>Administrador</small>
            </div>
            <button
              type="button"
              className="ps-boton-secundario"
              disabled={ocupado}
              onClick={() => void onSalir()}
            >
              {ocupado ? 'Cerrando…' : 'Cerrar sesión'}
            </button>
          </div>
        </header>

        <main className="ps-contenido" id="ciclos-admin">
          {errorSesion && (
            <div className="ps-error" role="alert">
              {errorSesion}
            </div>
          )}

                    {creandoCiclo && (
            <CrearCiclo
              onCerrar={() => {
                setCreandoCiclo(false)
                setVersionListado((valor) => valor + 1)
              }}
              onCreado={(ciclo) => {
                setCreandoCiclo(false)
                setAviso(
                  `Ciclo ${ciclo.codigo} — ${ciclo.nombre} creado correctamente.`,
                )
                setVersionListado((valor) => valor + 1)
              }}
            />
          )}

          {aviso && (
            <div className="mensaje mensaje-aviso" role="status">
              {aviso}
            </div>
          )}

          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              marginBottom: 16,
            }}
          >
            <button
              type="button"
              className="ps-boton-primario"
              onClick={() => {
                setAviso('')
                setCreandoCiclo(true)
              }}
            >
              + Crear ciclo
            </button>
          </div>

          <ListadoCiclos
            key={versionListado}
            ruta="/api/admin/ciclos"
          />

          <footer className="ps-pie">
            Academia Círculo · Sistema académico
          </footer>
        </main>
      </div>
    </div>
  )
}
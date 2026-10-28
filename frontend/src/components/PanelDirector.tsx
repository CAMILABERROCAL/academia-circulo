import { useState } from 'react'
import type { Sesion } from '../lib/api'
import ListadoCiclos from './ListadoCiclos'
import TurnosDelCiclo from './TurnosDelCiclo'
import './PanelSecretaria.css'
import CrearTurno from './CrearTurno'

type Props = {
  sesion: Sesion
  ocupado: boolean
  errorSesion: string
  onSalir: () => Promise<void>
}

type CicloSeleccionado = {
  cicloId: string
  codigo: string
  nombre: string
  estado: string
}

export default function PanelDirector({
  sesion,
  ocupado,
  errorSesion,
  onSalir,
}: Props) {
  const [cicloSeleccionado, setCicloSeleccionado] =
    useState<CicloSeleccionado | null>(null)
  const [creandoTurno, setCreandoTurno] = useState(false)
  const [avisoTurno, setAvisoTurno] = useState('')

  const iniciales = [
    sesion.nombres.trim().charAt(0),
    sesion.apellidos.trim().charAt(0),
  ].join('')

  return (
    <div className="panel-secretaria">
      {cicloSeleccionado && !creandoTurno && (
        <TurnosDelCiclo
          key={cicloSeleccionado.cicloId}
          cicloId={cicloSeleccionado.cicloId}
          nombreCiclo={`${cicloSeleccionado.codigo} — ${cicloSeleccionado.nombre}`}
          aviso={avisoTurno}
          onCerrar={() => {
            setCicloSeleccionado(null)
            setAvisoTurno('')
          }}
          onCrear={
            ['PLANIFICADO', 'ACTIVO'].includes(cicloSeleccionado.estado)
              ? () => {
                  setAvisoTurno('')
                  setCreandoTurno(true)
                }
              : undefined
          }
        />
      )}

      {cicloSeleccionado && creandoTurno && (
        <CrearTurno
          key={cicloSeleccionado.cicloId}
          cicloId={cicloSeleccionado.cicloId}
          nombreCiclo={`${cicloSeleccionado.codigo} — ${cicloSeleccionado.nombre}`}
          onCerrar={() => setCreandoTurno(false)}
          onCreado={() => {
            setAvisoTurno('Turno y horarios creados correctamente.')
            setCreandoTurno(false)
          }}
        />
      )}

      <aside className="ps-lateral" aria-label="Menú de dirección">
        <div className="ps-marca">
          <span className="ps-marca-icono" aria-hidden="true">C</span>
          <div>
            <small>ACADEMIA</small>
            <strong>CÍRCULO</strong>
          </div>
        </div>

        <p className="ps-menu-titulo">DIRECCIÓN</p>

        <nav className="ps-navegacion" aria-label="Secciones disponibles">
          <a
            className="ps-menu-opcion ps-menu-activo"
            href="#ciclos-director"
            aria-current="page"
          >
            <span aria-hidden="true">▤</span>
            Ciclos y horarios
          </a>
        </nav>

        <div className="ps-lateral-pie">
          <span>Coordinación académica</span>
          <small>Academia Círculo</small>
        </div>
      </aside>

      <div className="ps-cuerpo">
        <header className="ps-cabecera">
          <span className="ps-ubicacion">Área de dirección</span>

          <div className="ps-cuenta">
            <span className="ps-avatar" aria-hidden="true">{iniciales}</span>
            <div className="ps-nombre-cuenta">
              <strong>{sesion.nombres}</strong>
              <small>Director</small>
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

        <main className="ps-contenido" id="ciclos-director">
          {errorSesion && (
            <div className="ps-error" role="alert">{errorSesion}</div>
          )}

          <ListadoCiclos
            ruta="/api/director/ciclos"
            onVerTurnos={(ciclo) => setCicloSeleccionado(ciclo)}
          />

          <footer className="ps-pie">
            Academia Círculo · Sistema académico
          </footer>
        </main>
      </div>
    </div>
  )
}
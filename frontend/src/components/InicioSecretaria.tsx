import { useState } from 'react'
import RegistrarApoderado from './RegistrarApoderado'
import './InicioSecretaria.css'
import AlumnosRecientes from './AlumnosRecientes'
import IndicadoresAlumnos from './IndicadoresAlumnos'

type Props = {
  onRegistrarAlumno: () => void
  onMatricular: () => void
  onVerAlumnos: () => void
  onVerApoderados: () => void
  onVerCiclos: () => void
  onVerFicha: (estudianteId: string) => void
  versionAlumnos: number
}

export default function InicioSecretaria({
  onRegistrarAlumno,
  onMatricular,
  onVerAlumnos,
  onVerApoderados,
  onVerCiclos,
  onVerFicha,
  versionAlumnos,
}: Props) {
  const [registrandoApoderado, setRegistrandoApoderado] = useState(false)
  const [aviso, setAviso] = useState('')

  return (
    <div className="inicio-secretaria">
      {registrandoApoderado && (
        <RegistrarApoderado
          seleccionarAlCrear={false}
          onCerrar={() => setRegistrandoApoderado(false)}
          onCreado={(apoderado) => {
            setRegistrandoApoderado(false)
            setAviso(
              `Se registró a ${apoderado.nombres} ${apoderado.apellidos}. Puedes consultarlo en Apoderados.`,
            )
          }}
        />
      )}

      <div className="ps-titulo">
        <p className="ps-antetitulo">GESTIÓN ACADÉMICA</p>
        <h1>Panel de Secretaría</h1>
        <p>Gestiona alumnos, apoderados y matrículas desde un solo lugar.</p>
      </div>

      {aviso && (
        <div className="is-aviso" role="status">
          <p>{aviso}</p>
          <button
            type="button"
            className="ps-boton-secundario"
            onClick={onVerApoderados}
          >
            Ver apoderados
          </button>
        </div>
      )}

      <section className="is-accesos" aria-label="Accesos rápidos">
        <button
          type="button"
          className="is-acceso is-azul"
          onClick={onRegistrarAlumno}
        >
          <span className="is-icono" aria-hidden="true">＋</span>
          <span>
            <strong>Registrar alumno</strong>
            <small>Agrega un nuevo alumno</small>
          </span>
          <span className="is-flecha" aria-hidden="true">→</span>
        </button>

        <button
          type="button"
          className="is-acceso is-verde"
          onClick={onMatricular}
        >
          <span className="is-icono" aria-hidden="true">✓</span>
          <span>
            <strong>Registrar matrícula</strong>
            <small>Elige un alumno, ciclo y turno</small>
          </span>
          <span className="is-flecha" aria-hidden="true">→</span>
        </button>

        <button
          type="button"
          className="is-acceso is-violeta"
          onClick={() => {
            setAviso('')
            setRegistrandoApoderado(true)
          }}
        >
          <span className="is-icono" aria-hidden="true">＋</span>
          <span>
            <strong>Registrar apoderado</strong>
            <small>Crea su cuenta de acceso</small>
          </span>
          <span className="is-flecha" aria-hidden="true">→</span>
        </button>
      </section>
<IndicadoresAlumnos key={`indicadores-${versionAlumnos}`} />

<AlumnosRecientes
  key={versionAlumnos}
  onVerFicha={onVerFicha}
  onVerTodos={onVerAlumnos}
/>
      <div className="is-secciones">
        <section className="is-tarjeta" aria-labelledby="is-alumnos">
          <div className="is-tarjeta-titulo">
            <span className="is-simbolo" aria-hidden="true">▦</span>
            <div>
              <h2 id="is-alumnos">Alumnos y matrículas</h2>
              <p>Consulta las fichas y el estado de matrícula.</p>
            </div>
          </div>

          <p>
            Busca por código, nombre o apellido. Desde el listado puedes
            editar la ficha, consultar los horarios o registrar una matrícula.
          </p>

          <button
            type="button"
            className="ps-boton-primario"
            onClick={onVerAlumnos}
          >
            Ver alumnos →
          </button>
        </section>

        <section className="is-tarjeta" aria-labelledby="is-apoderados">
          <div className="is-tarjeta-titulo">
            <span className="is-simbolo" aria-hidden="true">♙</span>
            <div>
              <h2 id="is-apoderados">Apoderados</h2>
              <p>Encuentra sus cuentas y alumnos vinculados.</p>
            </div>
          </div>

          <p>
            Consulta los datos del apoderado y abre las fichas de los alumnos
            vinculados a su cuenta.
          </p>

          <button
            type="button"
            className="ps-boton-secundario"
            onClick={onVerApoderados}
          >
            Ver apoderados →
          </button>
        </section>

        <section className="is-tarjeta is-ciclos" aria-labelledby="is-ciclos">
          <div className="is-tarjeta-titulo">
            <span className="is-simbolo" aria-hidden="true">▤</span>
            <div>
              <h2 id="is-ciclos">Ciclos académicos</h2>
              <p>Consulta los ciclos registrados y sus fechas.</p>
            </div>
          </div>

          <button
            type="button"
            className="ps-boton-secundario"
            onClick={onVerCiclos}
          >
            Ver ciclos →
          </button>
        </section>
      </div>
    </div>
  )
}
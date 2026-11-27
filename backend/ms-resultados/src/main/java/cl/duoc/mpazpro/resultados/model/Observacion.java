package cl.duoc.mpazpro.resultados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * La observación que el docente escribe en el informe de un estudiante, para una unidad.
 * Hay una sola por estudiante y unidad. El docente la «valida» cuando el informe está listo.
 */
@Entity
@Table(
    name = "observacion",
    uniqueConstraints = @UniqueConstraint(columnNames = {"estudianteNombreUsuario", "unidadTitulo"})
)
public class Observacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String estudianteNombreUsuario;
    @Column(nullable = false, length = 100)
    private String unidadTitulo;
    @Column(nullable = false, length = 500)
    private String texto;
    @Column(nullable = false)
    private boolean validado;
    @Column(nullable = false, length = 50)
    private String docenteNombreUsuario;   // quien la escribió o la actualizó por última vez (del token)
    @Column(nullable = false)
    private long fechaActualizacion;       // epoch en milisegundos

    protected Observacion() {
        // Lo necesita JPA
    }

    public Observacion(String estudianteNombreUsuario, String unidadTitulo, String texto,
                       String docenteNombreUsuario, long fechaActualizacion) {
        this.estudianteNombreUsuario = estudianteNombreUsuario;
        this.unidadTitulo = unidadTitulo;
        this.texto = texto;
        this.validado = false;
        this.docenteNombreUsuario = docenteNombreUsuario;
        this.fechaActualizacion = fechaActualizacion;
    }

    /**
     * Aplica un cambio. Si el texto cambia, la validación se pierde (igual que en la app):
     * para validar se envía el mismo texto con validado = true.
     */
    public void actualizar(String nuevoTexto, boolean nuevoValidado, String docente, long ahora) {
        boolean cambioTexto = !texto.equals(nuevoTexto);
        this.texto = nuevoTexto;
        this.validado = !cambioTexto && nuevoValidado;
        this.docenteNombreUsuario = docente;
        this.fechaActualizacion = ahora;
    }

    public Long getId() { return id; }
    public String getEstudianteNombreUsuario() { return estudianteNombreUsuario; }
    public String getUnidadTitulo() { return unidadTitulo; }
    public String getTexto() { return texto; }
    public boolean isValidado() { return validado; }
    public String getDocenteNombreUsuario() { return docenteNombreUsuario; }
    public long getFechaActualizacion() { return fechaActualizacion; }
}

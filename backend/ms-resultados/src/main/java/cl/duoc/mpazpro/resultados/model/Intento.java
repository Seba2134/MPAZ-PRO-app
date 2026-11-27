package cl.duoc.mpazpro.resultados.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Copia de un intento terminado en la app (desafío o prueba final), con sus respuestas.
 * Guarda nombres en vez de ids de la app, porque el servidor no conoce sus tablas.
 * Un mismo intento (estudiante + actividad + número) se guarda una sola vez.
 */
@Entity
@Table(
    name = "intento",
    uniqueConstraints = @UniqueConstraint(columnNames = {"estudianteNombreUsuario", "actividadId", "numeroIntento"})
)
public class Intento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String estudianteNombreUsuario;
    @Column(nullable = false, length = 100)
    private String estudianteNombre;
    @Column(nullable = false, length = 50)
    private String cursoNombre;
    @Column(nullable = false, length = 100)
    private String asignaturaNombre;
    @Column(nullable = false, length = 100)
    private String unidadTitulo;

    @Column(nullable = false)
    private Long actividadId;
    @Column(nullable = false, length = 100)
    private String actividadTitulo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoActividad tipo;

    @Column(nullable = false)
    private int numeroIntento;
    @Column(nullable = false)
    private long fechaInicio;   // epoch en milisegundos, como en la app
    @Column(nullable = false)
    private long fechaFin;
    @Column(nullable = false)
    private int correctas;
    @Column(nullable = false)
    private int total;

    @Column(nullable = false, length = 50)
    private String enviadoPor;  // el "sub" del token de quien lo envió
    @Column(nullable = false)
    private long fechaRecepcion;

    @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Respuesta> respuestas = new ArrayList<>();

    protected Intento() {
        // Lo necesita JPA
    }

    public Intento(String estudianteNombreUsuario, String estudianteNombre, String cursoNombre,
                   String asignaturaNombre, String unidadTitulo, Long actividadId, String actividadTitulo,
                   TipoActividad tipo, int numeroIntento, long fechaInicio, long fechaFin,
                   int correctas, int total, String enviadoPor, long fechaRecepcion) {
        this.estudianteNombreUsuario = estudianteNombreUsuario;
        this.estudianteNombre = estudianteNombre;
        this.cursoNombre = cursoNombre;
        this.asignaturaNombre = asignaturaNombre;
        this.unidadTitulo = unidadTitulo;
        this.actividadId = actividadId;
        this.actividadTitulo = actividadTitulo;
        this.tipo = tipo;
        this.numeroIntento = numeroIntento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.correctas = correctas;
        this.total = total;
        this.enviadoPor = enviadoPor;
        this.fechaRecepcion = fechaRecepcion;
    }

    /** Agrega una respuesta y deja enlazados los dos lados de la relación. */
    public void agregarRespuesta(Respuesta respuesta) {
        respuesta.setIntento(this);
        respuestas.add(respuesta);
    }

    public Long getId() { return id; }
    public String getEstudianteNombreUsuario() { return estudianteNombreUsuario; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getCursoNombre() { return cursoNombre; }
    public String getAsignaturaNombre() { return asignaturaNombre; }
    public String getUnidadTitulo() { return unidadTitulo; }
    public Long getActividadId() { return actividadId; }
    public String getActividadTitulo() { return actividadTitulo; }
    public TipoActividad getTipo() { return tipo; }
    public int getNumeroIntento() { return numeroIntento; }
    public long getFechaInicio() { return fechaInicio; }
    public long getFechaFin() { return fechaFin; }
    public int getCorrectas() { return correctas; }
    public int getTotal() { return total; }
    public String getEnviadoPor() { return enviadoPor; }
    public long getFechaRecepcion() { return fechaRecepcion; }
    public List<Respuesta> getRespuestas() { return respuestas; }
}

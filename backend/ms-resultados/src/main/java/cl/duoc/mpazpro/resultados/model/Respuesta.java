package cl.duoc.mpazpro.resultados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Una respuesta de un intento: la pregunta, su OA y si fue correcta. */
@Entity
@Table(name = "respuesta")
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id")
    private Intento intento;

    @Column(nullable = false)
    private Long preguntaId;
    @Column(nullable = false, length = 300)
    private String enunciado;
    @Column(nullable = false, length = 20)
    private String oaCodigo;
    @Column(length = 300)
    private String oaDescripcion;
    @Column(nullable = false)
    private boolean esCorrecta;

    protected Respuesta() {
        // Lo necesita JPA
    }

    public Respuesta(Long preguntaId, String enunciado, String oaCodigo, String oaDescripcion, boolean esCorrecta) {
        this.preguntaId = preguntaId;
        this.enunciado = enunciado;
        this.oaCodigo = oaCodigo;
        this.oaDescripcion = oaDescripcion;
        this.esCorrecta = esCorrecta;
    }

    void setIntento(Intento intento) { this.intento = intento; }

    public Long getId() { return id; }
    public Intento getIntento() { return intento; }
    public Long getPreguntaId() { return preguntaId; }
    public String getEnunciado() { return enunciado; }
    public String getOaCodigo() { return oaCodigo; }
    public String getOaDescripcion() { return oaDescripcion; }
    public boolean isEsCorrecta() { return esCorrecta; }
}

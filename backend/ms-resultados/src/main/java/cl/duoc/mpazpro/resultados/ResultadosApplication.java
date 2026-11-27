package cl.duoc.mpazpro.resultados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio de resultados: intentos, informes por OA y observaciones del docente. */
@SpringBootApplication
public class ResultadosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResultadosApplication.class, args);
    }
}

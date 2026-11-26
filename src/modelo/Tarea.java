
package modelo;

// Permite trabajar con fechas y horas.
import java.time.LocalDateTime;

/**
 * Representa una tarea dentro de la aplicacion ORDINATIO.
 *
 * Hereda los atributos y comportamientos generales
 * de la clase abstracta Recordatorio.
 */
public class Tarea extends Recordatorio {

    // Atributo propio de Tarea.
    private String prioridad;

    /**
     * Constructor de Tarea.
     *
     * Utiliza super() para inicializar los atributos
     * heredados de Recordatorio.
     *
     * Despues establece la prioridad de la tarea.
     */
    public Tarea(String titulo, String descripcion,
                 LocalDateTime fechaHora, String prioridad) {

        // Invoca el constructor de la clase padre.
        super(titulo, descripcion, fechaHora);

        // Inicializa y valida la prioridad.
        cambiarPrioridad(prioridad);
    }

    // Devuelve la prioridad actual de la tarea.
    public String getPrioridad() {
        return prioridad;
    }

    /**
     * Cambia la prioridad de la tarea.
     *
     * Solo acepta tres valores: baja, media o alta.
     */
    public void cambiarPrioridad(String prioridad) {

        if (prioridad == null) {
            throw new IllegalArgumentException(
                "La prioridad es obligatoria."
            );
        }

        // Normaliza el texto para evitar diferencias
        // entre mayusculas y minusculas.
        String valor = prioridad.trim().toLowerCase();

        // Comprueba que la prioridad sea valida.
        if (!valor.equals("baja")
                && !valor.equals("media")
                && !valor.equals("alta")) {

            throw new IllegalArgumentException(
                "La prioridad debe ser baja, media o alta."
            );
        }

        this.prioridad = valor;
    }

    /**
     * Implementa el metodo abstracto de Recordatorio.
     *
     * Devuelve el tipo especifico de esta clase.
     */
    @Override
    public String getTipo() {
        return "Tarea";
    }
}


package modelo;

// Permite trabajar con fechas y horas.
import java.time.LocalDateTime;

/**
 * Representa un evento programado dentro de la aplicacion DIA.
 *
 * Hereda los atributos y comportamientos generales de
 * Recordatorio y agrega informacion sobre el lugar y
 * la duracion de la actividad.
 */
public class Evento extends Recordatorio {

    // Atributos propios de la clase Evento.
    private String lugar;
    private int duracionMinutos;

    /**
     * Constructor de Evento.
     *
     * Utiliza super() para inicializar los atributos
     * heredados de Recordatorio.
     *
     * Posteriormente inicializa el lugar y la duracion.
     */
    public Evento(String titulo, String descripcion,
                  LocalDateTime fechaHora, String lugar,
                  int duracionMinutos) {

        // Invoca el constructor de la clase padre.
        super(titulo, descripcion, fechaHora);

        // Inicializa los atributos propios del evento.
        actualizarLugar(lugar);
        actualizarDuracion(duracionMinutos);
    }

    // Devuelve el lugar donde se realizara el evento.
    public String getLugar() {
        return lugar;
    }

    // Devuelve la duracion del evento en minutos.
    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    /**
     * Actualiza el lugar del evento.
     *
     * Valida que el lugar no sea nulo ni este vacio.
     */
    public void actualizarLugar(String lugar) {

        if (lugar == null || lugar.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El lugar del evento es obligatorio."
            );
        }

        this.lugar = lugar.trim();
    }

    /**
     * Actualiza la duracion del evento.
     *
     * La duracion debe ser un numero entero mayor que cero.
     */
    public void actualizarDuracion(int duracionMinutos) {

        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException(
                "La duracion debe ser mayor que cero."
            );
        }

        this.duracionMinutos = duracionMinutos;
    }

    /**
     * Implementa el metodo abstracto de Recordatorio.
     *
     * Devuelve el tipo especifico de esta clase.
     */
    @Override
    public String getTipo() {
        return "Evento";
    }
}


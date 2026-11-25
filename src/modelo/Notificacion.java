package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un aviso programado y administrado por un Recordatorio.
 * Su constructor tiene acceso de paquete para que la creación se realice
 * desde el modelo y no desde cualquier clase de la aplicación.
 */
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String mensaje;
    private LocalDateTime fechaHoraProgramada;
    private boolean mostrada;
    private boolean activa;

    // Acceso de paquete: Recordatorio puede crearla; las vistas no.
    Notificacion(String mensaje, LocalDateTime fechaHoraProgramada) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje es obligatorio.");
        }
        if (fechaHoraProgramada == null) {
            throw new IllegalArgumentException("La fecha de notificacion es obligatoria.");
        }
        this.id = UUID.randomUUID().toString();
        this.mensaje = mensaje.trim();
        this.fechaHoraProgramada = fechaHoraProgramada;
        this.mostrada = false;
        this.activa = true;
    }

    public String getId() { return id; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFechaHoraProgramada() { return fechaHoraProgramada; }
    public boolean isMostrada() { return mostrada; }
    public boolean isActiva() { return activa; }

    public void mostrar() { this.mostrada = true; }
    public void activar() { this.activa = true; }
    public void desactivar() { this.activa = false; }

    /** Indica si ya llegó la fecha y el aviso sigue activo y sin atender. */
    public boolean estaPendiente(LocalDateTime ahora) {
        return activa && !mostrada && !fechaHoraProgramada.isAfter(ahora);
    }
}

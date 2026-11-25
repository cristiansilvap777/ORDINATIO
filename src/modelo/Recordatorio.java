package modelo;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Clase abstracta que representa un recordatorio general.
 * Define los datos y comportamientos comunes de Tarea y Evento.
 * Cada recordatorio es propietario de sus notificaciones.
 */
public abstract class Recordatorio implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String titulo;
    protected String descripcion;
    protected LocalDateTime fechaHora;
    protected boolean completado;
    protected boolean activo;
    protected Categoria categoria;

    // Composición: esta colección pertenece exclusivamente al recordatorio.
    private ArrayList<Notificacion> notificaciones;

    public Recordatorio(String titulo, String descripcion,
                        LocalDateTime fechaHora) {
        this.id = UUID.randomUUID().toString();
        actualizarDatos(titulo, descripcion, fechaHora);
        this.completado = false;
        this.activo = true;
        this.categoria = null;
        this.notificaciones = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public boolean isCompletado() { return completado; }
    public boolean isActivo() { return activo; }
    public Categoria getCategoria() { return categoria; }

    /** Devuelve una copia para impedir modificaciones externas de la colección. */
    public ArrayList<Notificacion> getNotificaciones() {
        return new ArrayList<>(notificaciones);
    }

    public void actualizarDatos(String titulo, String descripcion,
                                LocalDateTime fechaHora) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El titulo es obligatorio.");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora son obligatorias.");
        }
        this.titulo = titulo.trim();
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.fechaHora = fechaHora;
    }

    public void marcarCompletado() { this.completado = true; }

    public boolean estaVencido(LocalDateTime ahora) {
        return activo && !completado && fechaHora.isBefore(ahora);
    }

    public void cambiarEstado(boolean activo) { this.activo = activo; }

    public void asignarCategoria(Categoria categoria) { this.categoria = categoria; }

    /**
     * Crea una notificación nueva que queda bajo la propiedad de este recordatorio.
     * No recibe una Notificacion externa: así se evita compartir la misma instancia.
     */
    public void programarNotificacion(String mensaje,
                                      LocalDateTime fechaHoraProgramada) {
        Notificacion nueva = new Notificacion(mensaje, fechaHoraProgramada);
        notificaciones.add(nueva);
    }

    /** Elimina una notificación perteneciente a este recordatorio. */
    public boolean eliminarNotificacion(String idNotificacion) {
        if (idNotificacion == null) return false;
        return notificaciones.removeIf(n -> n.getId().equals(idNotificacion));
    }

    public abstract String getTipo();

    /** Recupera correctamente objetos serializados de versiones anteriores. */
    private void readObject(ObjectInputStream entrada)
            throws IOException, ClassNotFoundException {
        entrada.defaultReadObject();
        if (notificaciones == null) {
            notificaciones = new ArrayList<>();
        }
    }
}

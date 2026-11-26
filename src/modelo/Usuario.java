
package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Representa al usuario de la aplicacion ORDINATIO.
 *
 * Permite almacenar sus datos personales y administrar
 * la lista de recordatorios registrados.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    // Datos personales del usuario.
    private String id;
    private String nombre;
    private String correo;

    // Lista de recordatorios asociados al usuario.
    private ArrayList<Recordatorio> recordatorios;
    
    // Lista de categorías creadas por el usuario.
    private ArrayList<Categoria> categorias;

    /**
     * Constructor de Usuario.
     *
     * Genera un identificador unico e inicializa
     * la lista de recordatorios.
     */
    public Usuario(String nombre, String correo) {

        this.id = UUID.randomUUID().toString();

        actualizarNombre(nombre);
        actualizarCorreo(correo);

        this.recordatorios = new ArrayList<>();
        // Inicializa la colección independiente de categorías.
        this.categorias = new ArrayList<>();
    }

    // Devuelve el identificador del usuario.
    public String getId() {
        return id;
    }

    // Devuelve el nombre del usuario.
    public String getNombre() {
        return nombre;
    }

    // Devuelve el correo del usuario.
    public String getCorreo() {
        return correo;
    }

    /**
     * Devuelve una copia de los recordatorios registrados.
     */
    public ArrayList<Recordatorio> getRecordatorios() {
        return new ArrayList<>(recordatorios);
    }
    
    /**
     * Devuelve una copia de las categorías registradas. Evita que otras clases
     * modifiquen directamente la colección.
     */
    public ArrayList<Categoria> getCategorias() {
        return new ArrayList<>(categorias);
    }

    /**
     * Registra una nueva categoría para el usuario. Evita agregar categorías
     * nulas o duplicadas.
     */
    public void agregarCategoria(Categoria categoria) {

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoría no puede ser nula."
            );
        }

        if (buscarCategoria(categoria.getId()) != null) {
            throw new IllegalArgumentException(
                    "La categoría ya está registrada."
            );
        }

        categorias.add(categoria);
    }

    /**
     * Busca una categoría mediante su identificador único. Devuelve null si no
     * encuentra coincidencias.
     */
    public Categoria buscarCategoria(String id) {

        if (id == null) {
            return null;
        }

        for (Categoria categoria : categorias) {

            if (categoria.getId().equals(id)) {
                return categoria;
            }
        }

        return null;
    }

    /**
     * Elimina una categoría mediante su identificador. Devuelve true si la
     * eliminación fue exitosa.
     */
    public boolean eliminarCategoria(String id) {

        if (id == null) {
            return false;
        }

        return categorias.removeIf(
                categoria -> categoria.getId().equals(id)
        );
    }

    /**
     * Actualiza el nombre del usuario.
     *
     * Valida que no sea nulo ni este vacio.
     */
    public void actualizarNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El nombre es obligatorio."
            );
        }

        this.nombre = nombre.trim();
    }

    /**
     * Actualiza y valida el correo electronico.
     */
    public void actualizarCorreo(String correo) {

        if (correo == null ||
                !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new IllegalArgumentException(
                "El correo electronico no es valido."
            );
        }

        this.correo = correo.trim();
    }

    /**
     * Agrega un recordatorio a la lista del usuario.
     */
    public void agregarRecordatorio(Recordatorio recordatorio) {

        if (recordatorio == null) {
            throw new IllegalArgumentException(
                "El recordatorio no puede ser nulo."
            );
        }

        recordatorios.add(recordatorio);
    }

    /**
     * Elimina un recordatorio mediante su identificador.
     *
     * Devuelve true si se elimino correctamente.
     */
    public boolean eliminarRecordatorio(String id) {

        if (id == null) {
            return false;
        }

        return recordatorios.removeIf(
            r -> r.getId().equals(id)
        );
    }

    /**
     * Busca un recordatorio mediante su identificador.
     *
     * Devuelve el objeto encontrado o null si no existe.
     */
    public Recordatorio buscarRecordatorio(String id) {

        if (id == null) {
            return null;
        }

        for (Recordatorio recordatorio : recordatorios) {

            if (recordatorio.getId().equals(id)) {
                return recordatorio;
            }
        }

        return null;
    }
    /**
     * Recupera el objeto Usuario desde el archivo serializado.
     *
     * Inicializa las colecciones que no existían en versiones anteriores del
     * archivo para evitar valores nulos.
     */
    private void readObject(java.io.ObjectInputStream entrada)
            throws java.io.IOException, ClassNotFoundException {

        entrada.defaultReadObject();

        // Garantiza que la lista de recordatorios esté inicializada.
        if (recordatorios == null) {
            recordatorios = new ArrayList<>();
        }

        // Garantiza que la lista de categorías esté inicializada.
        if (categorias == null) {
            categorias = new ArrayList<>();
        }
    }
    
}


package modelo;

// Permite almacenar objetos Categoria en archivos.
import java.io.Serializable;

// Permite generar identificadores unicos.
import java.util.UUID;

/**
 * Representa una categoria utilizada para clasificar
 * las tareas y eventos de la aplicacion ORDINATIO.
 *
 * Implementa Serializable para permitir su almacenamiento
 * permanente mediante archivos.
 */
public class Categoria implements Serializable {

    // Identificador de version para la serializacion.
    private static final long serialVersionUID = 1L;

    // Atributos privados: aplicacion del encapsulamiento.
    private String id;
    private String nombre;
    private String descripcion;
    private String colorHex;

    /**
     * Constructor de la clase Categoria.
     *
     * Genera un identificador unico e inicializa
     * los datos mediante sus respectivos metodos.
     */
    public Categoria(String nombre, String descripcion,
                     String colorHex) {

        this.id = UUID.randomUUID().toString();

        actualizarNombre(nombre);
        actualizarDescripcion(descripcion);
        actualizarColor(colorHex);
    }

    // Devuelve el identificador unico de la categoria.
    public String getId() {
        return id;
    }

    // Devuelve el nombre de la categoria.
    public String getNombre() {
        return nombre;
    }

    // Devuelve la descripcion de la categoria.
    public String getDescripcion() {
        return descripcion;
    }

    // Devuelve el color hexadecimal de la categoria.
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Actualiza el nombre de la categoria.
     * Valida que no sea nulo ni contenga solamente espacios.
     */
    public void actualizarNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El nombre de la categoria es obligatorio."
            );
        }

        this.nombre = nombre.trim();
    }

    /**
     * Actualiza la descripcion.
     * Si recibe null, establece una cadena vacia.
     */
    public void actualizarDescripcion(String descripcion) {

        this.descripcion = descripcion == null
                ? "" : descripcion.trim();
    }

    /**
     * Actualiza el color de la categoria.
     * Comprueba que tenga el formato hexadecimal #RRGGBB.
     */
    public void actualizarColor(String colorHex) {

        if (colorHex == null ||
                !colorHex.matches("#[0-9A-Fa-f]{6}")) {

            throw new IllegalArgumentException(
                "El color debe tener formato hexadecimal."
            );
        }

        this.colorHex = colorHex;
    }
}

package controlador;

// Importa las clases necesarias del modelo.
import modelo.Usuario;
import modelo.Recordatorio;
import persistencia.ArchivoDatos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Controlador principal de la aplicacion DIA.
 *
 * Coordina las operaciones entre el modelo y la persistencia.
 * Mantiene activa una sola sesion a la vez.
 */
public class ControladorDIA {

    // Usuario que utiliza actualmente la aplicacion.
    private Usuario usuario;

    // Objeto encargado de guardar y recuperar informacion.
    private final ArchivoDatos archivoDatos;

    public ControladorDIA() {
        this.archivoDatos = new ArchivoDatos();
        this.usuario = null;
    }

    /**
     * Devuelve los perfiles disponibles para iniciar sesion.
     */
    public ArrayList<Usuario> listarUsuarios()
            throws IOException, ClassNotFoundException {

        Map<String, Usuario> usuarios = archivoDatos.cargarUsuarios();
        return new ArrayList<>(usuarios.values());
    }

    /**
     * Metodo de compatibilidad con la inicializacion anterior.
     * Carga el primer perfil disponible. La interfaz nueva utilizara
     * iniciarSesion(correo) para permitir elegir un perfil concreto.
     */
    public void cargarDatos()
            throws IOException, ClassNotFoundException {

        ArrayList<Usuario> usuarios = listarUsuarios();
        this.usuario = usuarios.isEmpty() ? null : usuarios.get(0);
    }

    /**
     * Inicia sesion mediante el correo electronico.
     *
     * @return true si el perfil existe y se activo.
     */
    public boolean iniciarSesion(String correo)
            throws IOException, ClassNotFoundException {

        Usuario encontrado = archivoDatos.buscarUsuario(correo);

        if (encontrado == null) {
            return false;
        }

        this.usuario = encontrado;
        return true;
    }

    /**
     * Crea un nuevo perfil. No permite correos duplicados.
     */
    public void crearUsuario(String nombre, String correo)
            throws IOException {

        if (usuario != null) {
            throw new IllegalStateException(
                    "Primero debe cerrarse la sesion actual."
            );
        }

        try {
            if (archivoDatos.existeCorreo(correo)) {
                throw new IllegalArgumentException(
                        "Ya existe un perfil registrado con ese correo."
                );
            }
        } catch (ClassNotFoundException ex) {
            throw new IOException(
                    "No fue posible interpretar los perfiles guardados.", ex
            );
        }

        this.usuario = new Usuario(nombre, correo);
    }

    /**
     * Guarda los datos del usuario activo sin afectar otros perfiles.
     */
    public void guardarDatos() throws IOException {

        if (usuario == null) {
            throw new IllegalStateException(
                    "No existe un usuario activo para guardar."
            );
        }

        archivoDatos.guardarUsuario(usuario);
    }

    /**
     * Cierra la sesion actual. No elimina ni modifica dia.dat.
     */
    public void cerrarSesion() throws IOException {

        if (usuario != null) {
            guardarDatos();
            usuario = null;
        }
    }

    /**
     * Agrega una tarea o evento al usuario actual.
     */
    public void agregarRecordatorio(Recordatorio recordatorio) {

        if (usuario == null) {
            throw new IllegalStateException(
                    "Primero debe existir una sesion activa."
            );
        }

        usuario.agregarRecordatorio(recordatorio);
    }

    public boolean eliminarRecordatorio(String id) {
        return usuario != null && usuario.eliminarRecordatorio(id);
    }

    public Recordatorio buscarRecordatorio(String id) {
        return usuario == null ? null : usuario.buscarRecordatorio(id);
    }

    /**
     * Registra una categoria en el usuario actual.
     */
    public void agregarCategoria(modelo.Categoria categoria) {

        if (usuario == null) {
            throw new IllegalStateException(
                    "Primero debe existir una sesion activa."
            );
        }

        usuario.agregarCategoria(categoria);
    }

    public boolean eliminarCategoria(String id) {
        return usuario != null && usuario.eliminarCategoria(id);
    }

    public modelo.Categoria buscarCategoria(String id) {
        return usuario == null ? null : usuario.buscarCategoria(id);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean hayUsuario() {
        return usuario != null;
    }

    /**
     * Descarta una sesión que todavía no pudo persistirse.
     * Se utiliza únicamente cuando falla el guardado inicial de un perfil.
     */
    public void descartarSesion() {
        usuario = null;
    }
}

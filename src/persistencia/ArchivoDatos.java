package persistencia;

// Permite utilizar la clase Usuario del modelo.
import modelo.Usuario;

// Clases necesarias para leer y escribir archivos.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Clase encargada de la persistencia de datos de ORDINATIO.
 *
 * Almacena varios perfiles Usuario en un unico archivo dia.dat.
 * Tambien permite recuperar el formato anterior, que contenia
 * directamente un solo objeto Usuario.
 */
public class ArchivoDatos {

    // Nombre del archivo utilizado para guardar los datos.
    private static final String NOMBRE_ARCHIVO = "dia.dat";

    /**
     * Recupera todos los perfiles almacenados.
     *
     * Si el archivo pertenece a una version anterior y contiene
     * directamente un Usuario, lo incorpora a la coleccion.
     *
     * @return Mapa de usuarios indexado por correo normalizado.
     */
    public Map<String, Usuario> cargarUsuarios()
            throws IOException, ClassNotFoundException {

        File archivo = new File(NOMBRE_ARCHIVO);
        Map<String, Usuario> usuarios = new LinkedHashMap<>();

        if (!archivo.exists()) {
            return usuarios;
        }

        try (ObjectInputStream entrada =
                new ObjectInputStream(new FileInputStream(archivo))) {

            Object objeto = entrada.readObject();

            // Compatibilidad con el archivo de la version anterior.
            if (objeto instanceof Usuario) {
                Usuario usuario = (Usuario) objeto;
                usuarios.put(normalizarCorreo(usuario.getCorreo()), usuario);
                return usuarios;
            }

            // Formato actual: coleccion de perfiles.
            if (objeto instanceof Map<?, ?>) {
                Map<?, ?> datos = (Map<?, ?>) objeto;

                for (Object valor : datos.values()) {
                    if (!(valor instanceof Usuario)) {
                        throw new IOException(
                                "El archivo contiene un perfil no valido."
                        );
                    }

                    Usuario usuario = (Usuario) valor;
                    usuarios.put(
                            normalizarCorreo(usuario.getCorreo()),
                            usuario
                    );
                }

                return usuarios;
            }

            throw new IOException(
                    "El archivo no contiene perfiles de usuario validos."
            );
        }
    }

    /**
     * Busca un usuario por su correo electronico.
     *
     * @param correo Correo utilizado como identificador de acceso.
     * @return Usuario encontrado o null si no existe.
     */
    public Usuario buscarUsuario(String correo)
            throws IOException, ClassNotFoundException {

        if (correo == null) {
            return null;
        }

        return cargarUsuarios().get(normalizarCorreo(correo));
    }

    /**
     * Comprueba si un correo ya esta registrado.
     */
    public boolean existeCorreo(String correo)
            throws IOException, ClassNotFoundException {

        return buscarUsuario(correo) != null;
    }

    /**
     * Guarda o actualiza un perfil sin eliminar los demas.
     *
     * Los perfiles se identifican por su correo electronico
     * normalizado. El archivo anterior se migra al nuevo formato
     * cuando se realiza el primer guardado.
     *
     * @param usuario Usuario que se desea guardar.
     */
    public void guardarUsuario(Usuario usuario) throws IOException {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no puede ser nulo."
            );
        }

        try {
            Map<String, Usuario> usuarios = cargarUsuarios();
            String correo = normalizarCorreo(usuario.getCorreo());

            Usuario existente = usuarios.get(correo);

            // Evita reemplazar accidentalmente otro perfil.
            if (existente != null
                    && !existente.getId().equals(usuario.getId())) {
                throw new IllegalArgumentException(
                        "Ya existe un perfil registrado con ese correo."
                );
            }

            usuarios.put(correo, usuario);

            // Se escribe primero un archivo temporal para reducir
            // el riesgo de perder el archivo anterior durante el guardado.
            File temporal = new File(NOMBRE_ARCHIVO + ".tmp");

            try (ObjectOutputStream salida =
                    new ObjectOutputStream(new FileOutputStream(temporal))) {
                salida.writeObject(usuarios);
            }

            File destino = new File(NOMBRE_ARCHIVO);

            Files.move(
                    temporal.toPath(),
                    destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (ClassNotFoundException ex) {
            throw new IOException(
                    "No fue posible interpretar los perfiles guardados.", ex
            );
        }
    }

    /**
     * Normaliza el correo para que las busquedas no dependan
     * de mayusculas o espacios externos.
     */
    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    /**
     * Comprueba si existe un archivo de datos guardado.
     */
    public boolean existeArchivo() {
        return new File(NOMBRE_ARCHIVO).isFile();
    }
}

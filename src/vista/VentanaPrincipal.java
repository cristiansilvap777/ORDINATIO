package vista;

import controlador.ControladorDIA;

/**
 * Ventana principal de ORDINATIO.
 *
 * Administra el acceso de usuarios y la navegación entre los formularios
 * creados con el GUI Builder de NetBeans.
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());

    // Un único controlador se comparte con todas las vistas.
    private final ControladorDIA controlador = new ControladorDIA();

    // Formularios independientes diseñados con NetBeans GUI Builder.
    private PanelInicio vistaInicio;
    private PanelRecordatorios vistaRecordatorios;
    private PanelTareas vistaTareas;
    private PanelEventos vistaEventos;
    private PanelCategorias vistaCategorias;

    public VentanaPrincipal() {
        initComponents();
        configurarIdentidadVisual();
        configurarBotonCerrarSesion();
        configurarBarraMenu();

        // El panel principal ocupa el espacio de la ventana.
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(panelPrincipal, java.awt.BorderLayout.CENTER);

        // Amplía el área de contenido para mostrar los formularios completos.
        configurarDistribucionPrincipal();

        configurarNavegacion();
        configurarEstadoInicial();
        cargarPerfilGuardado();

        setTitle("ORDINATIO — Personal Organizer");
        setSize(1100, 800);
        setMinimumSize(new java.awt.Dimension(950, 680));
        setResizable(true);
        setLocationRelativeTo(null);
    }

    /**
     * Ajusta el contenedor principal para dar espacio a las vistas
     * construidas mediante el GUI Builder.
     */
    private void configurarDistribucionPrincipal() {
        panelPrincipal.setPreferredSize(new java.awt.Dimension(1050, 720));
        panelContenido.setPreferredSize(new java.awt.Dimension(850, 620));

        javax.swing.GroupLayout distribucion =
                (javax.swing.GroupLayout) panelPrincipal.getLayout();

        distribucion.setHorizontalGroup(
                distribucion.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(distribucion.createSequentialGroup()
                        .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE,
                                165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE,
                                850, Short.MAX_VALUE))
        );

        distribucion.setVerticalGroup(
                distribucion.createSequentialGroup()
                .addComponent(panelEncabezado, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(distribucion.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE,
                                620, Short.MAX_VALUE)
                        .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE,
                                620, Short.MAX_VALUE))
        );
    }

    /**
     * Registra en CardLayout los cinco formularios independientes.
     * El formulario de registro también es una tarjeta del contenedor.
     */
    private void configurarNavegacion() {
        java.awt.CardLayout tarjetas = new java.awt.CardLayout();
        panelContenido.removeAll();
        panelContenido.setLayout(tarjetas);

        panelContenido.add(panelRegistro, "registro");

        // Cada vista recibe la misma instancia del controlador.
        vistaInicio = new PanelInicio(controlador);
        vistaRecordatorios = new PanelRecordatorios(controlador);
        vistaTareas = new PanelTareas(controlador);
        vistaEventos = new PanelEventos(controlador);
        vistaCategorias = new PanelCategorias(controlador);

        panelContenido.add(vistaInicio, "inicio");
        panelContenido.add(vistaRecordatorios, "recordatorios");

        // Tareas y Eventos conservan íntegros sus formularios de NetBeans Design.
        // Se envuelven en un JScrollPane exterior para poder recorrer verticalmente
        // todo el contenido cuando la resolución de pantalla sea reducida.
        panelContenido.add(crearPanelDesplazable(vistaTareas), "tareas");
        panelContenido.add(crearPanelDesplazable(vistaEventos), "eventos");

        panelContenido.add(vistaCategorias, "categorias");

        // Los botones que no tienen evento generado por el diseñador
        // se conectan aquí con las tarjetas correspondientes.
        btnRecordatorios.addActionListener(e -> mostrarVista("recordatorios"));
        btnTareas.addActionListener(e -> mostrarVista("tareas"));

        tarjetas.show(panelContenido, "registro");
        panelContenido.revalidate();
        panelContenido.repaint();
    }


    /** Configura el icono de ORDINATIO en el encabezado y como icono de ventana. */
    private void configurarIdentidadVisual() {
        java.net.URL recursoLogo = getClass().getResource("/recursos/logo_dia_icono.png");
        if (recursoLogo != null) {
            javax.swing.ImageIcon original = new javax.swing.ImageIcon(recursoLogo);
            java.awt.Image imagen = original.getImage().getScaledInstance(
                    34, 34, java.awt.Image.SCALE_SMOOTH);
            lblTitulo.setIcon(new javax.swing.ImageIcon(imagen));
            lblTitulo.setIconTextGap(10);
            setIconImage(original.getImage());
        }
        lblTitulo.setText("ORDINATIO");
    }

    /**
     * Conecta las opciones de la barra de menú creada en Design.
     * Se reutilizan los componentes del GUI Builder para mantener editable
     * la estructura visual desde NetBeans.
     */
    private void configurarBarraMenu() {
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        itemSalir.addActionListener(e -> dispose());

        itemInicio.addActionListener(e -> mostrarVista("inicio"));
        itemRecordatorios.addActionListener(e -> mostrarVista("recordatorios"));
        itemTareas.addActionListener(e -> mostrarVista("tareas"));
        itemEventos.addActionListener(e -> mostrarVista("eventos"));
        itemCategorias.addActionListener(e -> mostrarVista("categorias"));

        itemAcercaDe.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(
                this,
                "ORDINATIO — Personal Organizer\\nOrganiza tareas, eventos y recordatorios.",
                "Acerca de ORDINATIO", javax.swing.JOptionPane.INFORMATION_MESSAGE));

        setJMenuBar(jMenuBar1);
    }

    /** Añade una opción al menú que muestra la tarjeta solicitada. */
    private void agregarOpcionMenu(javax.swing.JMenu menu, String texto, String vista) {
        javax.swing.JMenuItem opcion = new javax.swing.JMenuItem(texto);
        opcion.addActionListener(e -> mostrarVista(vista));
        menu.add(opcion);
    }

    /** Establece la interfaz inicial sin seleccionar automáticamente un perfil. */
    private void configurarEstadoInicial() {
        actualizarEstadoSesion();
        lblSubtitulo.setText(
                "Crea tu perfil o inicia sesión con tu correo electrónico.");
    }

    /**
     * En esta versión se muestran las opciones de acceso al iniciar.
     * El usuario elige explícitamente qué perfil desea abrir.
     */
    private void cargarPerfilGuardado() {
        mostrarVista("registro");
    }

    /**
     * Configura la apariencia y el tamaño del botón de cierre de sesión.
     * Se conserva el componente creado en el GUI Builder.
     */
    private void configurarBotonCerrarSesion() {
        configurarBotonMenu(btnCerrarSesion);

        btnCerrarSesion.setText("Cerrar sesión");
        btnCerrarSesion.setToolTipText(
                "Guardar los cambios y cerrar la sesión actual");

        btnCerrarSesion.setPreferredSize(new java.awt.Dimension(140, 32));
        btnCerrarSesion.setMinimumSize(new java.awt.Dimension(140, 32));
        btnCerrarSesion.setVisible(false);
    }


    /**
     * Envuelve una vista completa en un JScrollPane sin modificar su diseño
     * interno generado por NetBeans Design. Permite desplazamiento vertical
     * con la barra o la rueda del mouse en pantallas de menor resolución.
     */
    private javax.swing.JScrollPane crearPanelDesplazable(javax.swing.JPanel vista) {
        javax.swing.JScrollPane desplazamiento =
                new javax.swing.JScrollPane(vista);

        desplazamiento.setBorder(null);
        desplazamiento.setVerticalScrollBarPolicy(
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        desplazamiento.setHorizontalScrollBarPolicy(
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        desplazamiento.setWheelScrollingEnabled(true);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(20);

        return desplazamiento;
    }

    /** Activa o desactiva las opciones que requieren una sesión. */
    private void setNavegacionActiva(boolean activa) {
        btnInicio.setEnabled(activa);
        btnRecordatorios.setEnabled(activa);
        btnTareas.setEnabled(activa);
        btnEventos.setEnabled(activa);
        btnCategorias.setEnabled(activa);

        // La visibilidad se actualiza explícitamente al cambiar de sesión.
        btnCerrarSesion.setEnabled(activa);
        btnCerrarSesion.setVisible(activa);

        // Fuerza a Swing a recalcular el espacio del menú lateral.
        panelMenu.revalidate();
        panelMenu.repaint();
    }

    /** Actualiza de forma sincronizada el encabezado y el menú de sesión. */
    private void actualizarEstadoSesion() {
        boolean sesionActiva = controlador.hayUsuario();
        if (sesionActiva) {
            lblUsuarioActivo.setText(
                    "Usuario: " + controlador.getUsuario().getNombre());
        } else {
            lblUsuarioActivo.setText("Usuario: No identificado");
        }
        setNavegacionActiva(sesionActiva);
    }

    /** Refresca las tablas y los indicadores de las vistas. */
    private void actualizarVistas() {
        vistaInicio.refrescarDatos();
        vistaRecordatorios.refrescarDatos();
        vistaTareas.refrescarDatos();
        vistaEventos.refrescarDatos();
        vistaCategorias.refrescarDatos();
    }

    /** Cambia de tarjeta y actualiza la información de la sección seleccionada. */
    private void mostrarVista(String nombre) {
        if (!"registro".equals(nombre) && !controlador.hayUsuario()) {
            java.awt.CardLayout tarjetas =
                    (java.awt.CardLayout) panelContenido.getLayout();
            tarjetas.show(panelContenido, "registro");
            return;
        }

        switch (nombre) {
            case "inicio":
                vistaInicio.refrescarDatos();
                break;
            case "recordatorios":
                vistaRecordatorios.refrescarDatos();
                break;
            case "tareas":
                vistaTareas.refrescarDatos();
                break;
            case "eventos":
                vistaEventos.refrescarDatos();
                break;
            case "categorias":
                vistaCategorias.refrescarDatos();
                break;
            default:
                break;
        }

        java.awt.CardLayout tarjetas =
                (java.awt.CardLayout) panelContenido.getLayout();
        tarjetas.show(panelContenido, nombre);
    }

    /**
     * Configura la identidad gráfica de ORDINATIO. Centraliza los colores,
     * tipografías y elementos visuales del encabezado, menú lateral y pantalla
     * de acceso.
     */
    

    /**
     * Aplica el estilo compartido a los botones de navegación.
     */
    private void configurarBotonMenu(javax.swing.JButton boton) {
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        boton.setBackground(EstiloVisualDIA.FONDO_PANEL);
        boton.setForeground(EstiloVisualDIA.AZUL_MARINO);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);
        boton.setCursor(new java.awt.Cursor(
                java.awt.Cursor.HAND_CURSOR));
        boton.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloVisualDIA.LINEA),
                javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    /**
     * Destaca la acción principal de creación de perfil.
     */
    private void configurarBotonPrincipal(javax.swing.JButton boton) {
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        boton.setBackground(EstiloVisualDIA.AZUL_ACENTO);
        boton.setForeground(java.awt.Color.WHITE);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setCursor(new java.awt.Cursor(
                java.awt.Cursor.HAND_CURSOR));
    }

    /**
     * Unifica la apariencia de los campos de texto.
     */
    private void configurarCampoTexto(javax.swing.JTextField campo) {
        campo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        campo.setForeground(EstiloVisualDIA.AZUL_MARINO);
        campo.setBackground(java.awt.Color.WHITE);
        campo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloVisualDIA.LINEA),
                javax.swing.BorderFactory.createEmptyBorder(4, 7, 4, 7)
        ));
    }


    private void btnInicioActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarVista("inicio");
    }

    private void btnEventosActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarVista("eventos");
    }

    private void btnCategoriasActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarVista("categorias");
    }

    /**
     * Guarda los datos del perfil activo, limpia la sesión en memoria
     * y regresa a la pantalla de acceso sin eliminar información persistida.
     */
    private void cerrarSesion() {
        if (!controlador.hayUsuario()) {
            mostrarVista("registro");
            return;
        }

        try {
            controlador.cerrarSesion();
            configurarEstadoInicial();
            actualizarVistas();
            mostrarVista("registro");
            javax.swing.JOptionPane.showMessageDialog(this,
                    "La sesión se cerró correctamente.",
                    "Sesión cerrada", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No fue posible guardar los datos antes de cerrar sesión: "
                    + ex.getMessage(),
                    "Error de almacenamiento", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {
        txtCorreo.requestFocusInWindow();
    }

    /** Crea y persiste un perfil nuevo sin reemplazar los demás perfiles. */
    private void btnCrearPerfilActionPerformed(java.awt.event.ActionEvent evt) {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();

        if (nombre.isEmpty() || correo.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Debes ingresar tu nombre y correo electrónico.",
                    "Datos incompletos", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controlador.crearUsuario(nombre, correo);
            controlador.guardarDatos();

            actualizarEstadoSesion();
            txtNombre.setText("");
            txtCorreo.setText("");
            actualizarVistas();
            mostrarVista("inicio");

            javax.swing.JOptionPane.showMessageDialog(
                    this, "Tu perfil ha sido creado correctamente.",
                    "Registro exitoso", javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            controlador.descartarSesion();
            javax.swing.JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "No fue posible crear el perfil",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (java.io.IOException ex) {
            controlador.descartarSesion();
            javax.swing.JOptionPane.showMessageDialog(
                    this, "No fue posible guardar el perfil: " + ex.getMessage(),
                    "Error de almacenamiento", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Busca el perfil por correo y abre únicamente sus propios datos. */
    private void btnIniciarSesionActionPerformed(java.awt.event.ActionEvent evt) {
        String correo = txtCorreo.getText().trim();

        if (correo.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Ingresa el correo electrónico del perfil.",
                    "Dato requerido", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!controlador.iniciarSesion(correo)) {
                javax.swing.JOptionPane.showMessageDialog(
                        this, "No existe un perfil registrado con ese correo.",
                        "Perfil no encontrado", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            actualizarEstadoSesion();
            txtNombre.setText("");
            txtCorreo.setText("");
            actualizarVistas();
            mostrarVista("inicio");

        } catch (java.io.IOException | ClassNotFoundException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "No fue posible cargar el perfil: " + ex.getMessage(),
                    "Error de lectura", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Evento del botón lateral: delega el cierre al método centralizado. */
    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {
        cerrarSesion();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        panelEncabezado = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        lblUsuarioActivo = new javax.swing.JLabel();
        panelMenu = new javax.swing.JPanel();
        lblMenu = new javax.swing.JLabel();
        btnInicio = new javax.swing.JButton();
        btnRecordatorios = new javax.swing.JButton();
        btnTareas = new javax.swing.JButton();
        btnEventos = new javax.swing.JButton();
        btnCategorias = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        panelContenido = new javax.swing.JPanel();
        panelRegistro = new javax.swing.JPanel();
        lblTituloRegistro = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        btnCrearPerfil = new javax.swing.JButton();
        btnIniciarSesion = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        itemCerrarSesion = new javax.swing.JMenuItem();
        itemSalir = new javax.swing.JMenuItem();
        menuNavegacion = new javax.swing.JMenu();
        itemInicio = new javax.swing.JMenuItem();
        itemRecordatorios = new javax.swing.JMenuItem();
        itemTareas = new javax.swing.JMenuItem();
        itemEventos = new javax.swing.JMenuItem();
        itemCategorias = new javax.swing.JMenuItem();
        menuAyuda = new javax.swing.JMenu();
        itemAcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ORDINATIO — Personal Organizer");
        setName("VentanaPrincipal"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1000, 650));

        panelPrincipal.setBackground(new java.awt.Color(243, 245, 248));
        panelPrincipal.setName(""); // NOI18N
        panelPrincipal.setPreferredSize(new java.awt.Dimension(1000, 650));

        panelEncabezado.setBackground(new java.awt.Color(36, 52, 75));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText("ORDINATIO");

        lblDescripcion.setForeground(new java.awt.Color(255, 255, 255));
        lblDescripcion.setText("Organiza tu tiempo, cumple tus objetivos.");

        lblUsuarioActivo.setForeground(new java.awt.Color(255, 255, 255));
        lblUsuarioActivo.setText("Usuario: No identificado");

        javax.swing.GroupLayout panelEncabezadoLayout = new javax.swing.GroupLayout(panelEncabezado);
        panelEncabezado.setLayout(panelEncabezadoLayout);
        panelEncabezadoLayout.setHorizontalGroup(
            panelEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo)
                    .addGroup(panelEncabezadoLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panelEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDescripcion)
                            .addComponent(lblUsuarioActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncabezadoLayout.setVerticalGroup(
            panelEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDescripcion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblUsuarioActivo)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        panelMenu.setBackground(new java.awt.Color(226, 232, 240));
        panelMenu.setPreferredSize(new java.awt.Dimension(150, 175));

        lblMenu.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMenu.setForeground(new java.awt.Color(36, 52, 75));
        lblMenu.setText("NAVEGACIÓN");

        btnInicio.setBackground(new java.awt.Color(226, 232, 240));
        btnInicio.setForeground(new java.awt.Color(36, 52, 75));
        btnInicio.setText("Inicio");
        btnInicio.setBorder(null);
        btnInicio.setFocusable(false);
        btnInicio.setPreferredSize(new java.awt.Dimension(140, 32));
        btnInicio.addActionListener(this::btnInicioActionPerformed);

        btnRecordatorios.setBackground(new java.awt.Color(226, 232, 240));
        btnRecordatorios.setForeground(new java.awt.Color(36, 52, 75));
        btnRecordatorios.setText("Recordatorios");
        btnRecordatorios.setBorder(null);
        btnRecordatorios.setFocusable(false);
        btnRecordatorios.setPreferredSize(new java.awt.Dimension(140, 32));

        btnTareas.setBackground(new java.awt.Color(226, 232, 240));
        btnTareas.setForeground(new java.awt.Color(36, 52, 75));
        btnTareas.setText("Tareas");
        btnTareas.setBorder(null);
        btnTareas.setFocusable(false);
        btnTareas.setPreferredSize(new java.awt.Dimension(140, 32));

        btnEventos.setBackground(new java.awt.Color(226, 232, 240));
        btnEventos.setForeground(new java.awt.Color(36, 52, 75));
        btnEventos.setText("Eventos");
        btnEventos.setBorder(null);
        btnEventos.setFocusable(false);
        btnEventos.setPreferredSize(new java.awt.Dimension(140, 32));
        btnEventos.addActionListener(this::btnEventosActionPerformed);

        btnCategorias.setBackground(new java.awt.Color(226, 232, 240));
        btnCategorias.setForeground(new java.awt.Color(36, 52, 75));
        btnCategorias.setText("Categorías");
        btnCategorias.setBorder(null);
        btnCategorias.setFocusable(false);
        btnCategorias.setPreferredSize(new java.awt.Dimension(140, 32));
        btnCategorias.addActionListener(this::btnCategoriasActionPerformed);

        btnCerrarSesion.setBackground(new java.awt.Color(226, 232, 240));
        btnCerrarSesion.setForeground(new java.awt.Color(36, 52, 75));
        btnCerrarSesion.setText("Cerrar sesión");
        btnCerrarSesion.setFocusable(false);
        btnCerrarSesion.setPreferredSize(new java.awt.Dimension(140, 32));
        btnCerrarSesion.addActionListener(this::btnCerrarSesionActionPerformed);

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenuLayout.createSequentialGroup()
                        .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMenu)
                            .addComponent(btnTareas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEventos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenuLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnInicio, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRecordatorios, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCategorias, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMenuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCerrarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        panelMenuLayout.setVerticalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMenu)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRecordatorios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTareas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEventos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCerrarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelContenido.setPreferredSize(new java.awt.Dimension(407, 248));
        panelContenido.setLayout(new java.awt.CardLayout());

        lblTituloRegistro.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTituloRegistro.setText("Bienvenido a DÍA");

        lblSubtitulo.setText("Registra tus datos para comenzar a organizar tus actividades.");

        lblNombre.setText("Nombre completo:");

        txtNombre.addActionListener(this::txtNombreActionPerformed);

        lblCorreo.setText("Correo electrónico:");

        btnCrearPerfil.setText("Crear perfil");
        btnCrearPerfil.addActionListener(this::btnCrearPerfilActionPerformed);

        btnIniciarSesion.setText("Iniciar sesión");
        btnIniciarSesion.addActionListener(this::btnIniciarSesionActionPerformed);

        javax.swing.GroupLayout panelRegistroLayout = new javax.swing.GroupLayout(panelRegistro);
        panelRegistro.setLayout(panelRegistroLayout);
        panelRegistroLayout.setHorizontalGroup(
            panelRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .addGroup(panelRegistroLayout.createSequentialGroup()
                        .addGroup(panelRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNombre)
                            .addComponent(lblCorreo)
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTituloRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelRegistroLayout.createSequentialGroup()
                                    .addComponent(btnCrearPerfil, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(29, 29, 29)
                                    .addComponent(btnIniciarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(txtCorreo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRegistroLayout.setVerticalGroup(
            panelRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegistroLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloRegistro)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNombre)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCorreo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCrearPerfil)
                    .addComponent(btnIniciarSesion))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        panelContenido.add(panelRegistro, "card2");

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelContenido, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 39, Short.MAX_VALUE))
        );
        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addComponent(panelEncabezado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipalLayout.createSequentialGroup()
                        .addComponent(panelContenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(21, Short.MAX_VALUE))
                    .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)))
        );

        menuArchivo.setText("Archivo");

        itemCerrarSesion.setText("Cerrar Sesión");
        menuArchivo.add(itemCerrarSesion);

        itemSalir.setText("Salir");
        itemSalir.addActionListener(this::itemSalirActionPerformed);
        menuArchivo.add(itemSalir);

        jMenuBar1.add(menuArchivo);

        menuNavegacion.setText("Navegación");

        itemInicio.setText("Inicio");
        menuNavegacion.add(itemInicio);

        itemRecordatorios.setText("Recordatorios");
        menuNavegacion.add(itemRecordatorios);

        itemTareas.setText("Tareas");
        menuNavegacion.add(itemTareas);

        itemEventos.setText("Eventos");
        menuNavegacion.add(itemEventos);

        itemCategorias.setText("Categorias");
        menuNavegacion.add(itemCategorias);

        jMenuBar1.add(menuNavegacion);

        menuAyuda.setText("Ayuda");

        itemAcercaDe.setText("Acerca de DÍA");
        itemAcercaDe.addActionListener(this::itemAcercaDeActionPerformed);
        menuAyuda.add(itemAcercaDe);

        jMenuBar1.add(menuAyuda);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelPrincipal.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void itemSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemSalirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_itemSalirActionPerformed

    private void itemAcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemAcercaDeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_itemAcercaDeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCategorias;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnCrearPerfil;
    private javax.swing.JButton btnEventos;
    private javax.swing.JButton btnIniciarSesion;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnRecordatorios;
    private javax.swing.JButton btnTareas;
    private javax.swing.JMenuItem itemAcercaDe;
    private javax.swing.JMenuItem itemCategorias;
    private javax.swing.JMenuItem itemCerrarSesion;
    private javax.swing.JMenuItem itemEventos;
    private javax.swing.JMenuItem itemInicio;
    private javax.swing.JMenuItem itemRecordatorios;
    private javax.swing.JMenuItem itemSalir;
    private javax.swing.JMenuItem itemTareas;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblMenu;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTituloRegistro;
    private javax.swing.JLabel lblUsuarioActivo;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenu menuAyuda;
    private javax.swing.JMenu menuNavegacion;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelEncabezado;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelRegistro;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables

    /** Punto de entrada de la aplicación. */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() ->
                new VentanaPrincipal().setVisible(true));
    }
}

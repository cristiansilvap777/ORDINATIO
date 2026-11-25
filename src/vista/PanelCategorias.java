package vista;

import controlador.ControladorDIA;
import modelo.Categoria;
import javax.swing.table.DefaultTableModel;

/** Vista para crear, consultar y eliminar categorías del usuario activo. */
public class PanelCategorias extends javax.swing.JPanel {

    private final ControladorDIA controlador;
    private final DefaultTableModel modeloTabla;
    private final java.util.ArrayList<String> idsMostrados = new java.util.ArrayList<>();
    private String idCategoriaEnEdicion;

    public PanelCategorias() {
        this(null);
    }

    public PanelCategorias(ControladorDIA controlador) {
        initComponents();
        this.controlador = controlador;

        modeloTabla = new DefaultTableModel(
                new Object[]{"Nombre", "Descripción", "Color"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaCategorias.setModel(modeloTabla);
        EstiloVisualDIA.configurarTabla(tablaCategorias, -1);
        tablaCategorias.getColumnModel().getColumn(2)
                .setCellRenderer(new EstiloVisualDIA.RenderizadorColor());
        tablaCategorias.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        btnGuardarCategoria.addActionListener(e -> guardarCategoria());
        btnEditarCategoria.addActionListener(e -> cargarCategoriaSeleccionada());
        btnElegirColorCategoria.addActionListener(e -> seleccionarColorCategoria());
        configurarSelectorColor();
        refrescarDatos();
    }

    /**
     * Convierte el campo de color en un selector visual accesible.
     * El código hexadecimal se conserva internamente para la persistencia,
     * pero la persona usuaria interactúa directamente con una muestra de color.
     */
    private void configurarSelectorColor() {
        txtColorCategoria.setEditable(false);
        txtColorCategoria.setFocusable(true);
        txtColorCategoria.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        txtColorCategoria.setToolTipText(
                "Haz clic aquí para elegir el color de identificación de la categoría.");
        actualizarVistaPreviaColor(EstiloVisualDIA.AZUL_ACENTO);

        txtColorCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seleccionarColorCategoria();
            }
        });
        txtColorCategoria.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                        || evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    seleccionarColorCategoria();
                }
            }
        });
    }

    /** Abre el selector Swing y actualiza la muestra si se elige un color. */
    private void seleccionarColorCategoria() {
        java.awt.Color colorInicial = EstiloVisualDIA.AZUL_ACENTO;
        try {
            colorInicial = java.awt.Color.decode(txtColorCategoria.getText());
        } catch (NumberFormatException ex) {
            // Se conserva el azul de acento como color inicial seguro.
        }

        // Se crea el diálogo explícitamente para evitar que los botones
        // inferiores queden recortados en ventanas de tamaño predeterminado.
        javax.swing.JColorChooser selector =
                new javax.swing.JColorChooser(colorInicial);
        javax.swing.JDialog dialogo = javax.swing.JColorChooser.createDialog(
                this, "Seleccionar color de categoría", true, selector,
                evento -> actualizarVistaPreviaColor(selector.getColor()),
                evento -> { });

        dialogo.setResizable(true);
        dialogo.setMinimumSize(new java.awt.Dimension(700, 500));
        dialogo.setSize(740, 560);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    /** Actualiza el código almacenado y la muestra visible del color. */
    private void actualizarVistaPreviaColor(java.awt.Color color) {
        String hexadecimal = String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
        txtColorCategoria.setText(hexadecimal);
        txtColorCategoria.setBackground(color);
        double luminosidad = color.getRed() * 0.299
                + color.getGreen() * 0.587 + color.getBlue() * 0.114;
        txtColorCategoria.setForeground(luminosidad > 150
                ? EstiloVisualDIA.AZUL_MARINO : java.awt.Color.WHITE);
        txtColorCategoria.setCaretPosition(0);
    }

    /**
     * Valida y guarda una categoría nueva o actualiza la que está en edición.
     * Si falla la persistencia, restaura los valores anteriores.
     */
    private void guardarCategoria() {
        if (controlador == null || !controlador.hayUsuario()) {
            mostrarMensaje("Primero debes iniciar sesión.", "Sesión requerida",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = txtNombreCategoria.getText().trim();
        String descripcion = txtDescripcionCategoria.getText().trim();
        String color = txtColorCategoria.getText().trim();

        if (nombre.isEmpty() || color.isEmpty()) {
            mostrarMensaje("El nombre y el color de identificación son obligatorios.",
                    "Datos incompletos", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        Categoria categoria = null;
        String nombreAnterior = null;
        String descripcionAnterior = null;
        String colorAnterior = null;
        boolean esEdicion = idCategoriaEnEdicion != null;

        try {
            if (esEdicion) {
                categoria = controlador.buscarCategoria(idCategoriaEnEdicion);
                if (categoria == null) {
                    throw new IllegalStateException(
                            "La categoría seleccionada ya no está disponible.");
                }
                nombreAnterior = categoria.getNombre();
                descripcionAnterior = categoria.getDescripcion();
                colorAnterior = categoria.getColorHex();

                categoria.actualizarNombre(nombre);
                categoria.actualizarDescripcion(descripcion);
                categoria.actualizarColor(color);
            } else {
                categoria = new Categoria(nombre, descripcion, color);
                controlador.agregarCategoria(categoria);
            }

            try {
                controlador.guardarDatos();
            } catch (java.io.IOException ex) {
                if (esEdicion) {
                    categoria.actualizarNombre(nombreAnterior);
                    categoria.actualizarDescripcion(descripcionAnterior);
                    categoria.actualizarColor(colorAnterior);
                } else {
                    controlador.eliminarCategoria(categoria.getId());
                }
                throw ex;
            }

            limpiarFormulario();
            refrescarDatos();
            mostrarMensaje(esEdicion
                    ? "La categoría se actualizó correctamente."
                    : "La categoría se guardó correctamente.",
                    esEdicion ? "Actualización exitosa" : "Registro exitoso",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarMensaje(ex.getMessage(), "Datos inválidos",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (java.io.IOException ex) {
            mostrarMensaje("No fue posible guardar la categoría: " + ex.getMessage(),
                    "Error de almacenamiento", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Muestra las categorías registradas en el perfil activo. */
    public void refrescarDatos() {
        modeloTabla.setRowCount(0);
        idsMostrados.clear();

        if (controlador == null || !controlador.hayUsuario()) {
            return;
        }

        for (Categoria categoria : controlador.getUsuario().getCategorias()) {
            modeloTabla.addRow(new Object[]{
                categoria.getNombre(), categoria.getDescripcion(),
                categoria.getColorHex()
            });
            idsMostrados.add(categoria.getId());
        }
    }

    /** Carga en el formulario la categoría seleccionada para poder editarla. */
    private void cargarCategoriaSeleccionada() {
        int fila = tablaCategorias.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una categoría de la tabla.",
                    "Selección requerida", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaCategorias.convertRowIndexToModel(fila);
        idCategoriaEnEdicion = idsMostrados.get(filaModelo);
        Categoria categoria = controlador.buscarCategoria(idCategoriaEnEdicion);
        if (categoria == null) {
            mostrarMensaje("No se encontró la categoría seleccionada.",
                    "Categoría no disponible", javax.swing.JOptionPane.WARNING_MESSAGE);
            idCategoriaEnEdicion = null;
            return;
        }

        txtNombreCategoria.setText(categoria.getNombre());
        txtDescripcionCategoria.setText(categoria.getDescripcion());
        actualizarVistaPreviaColor(java.awt.Color.decode(categoria.getColorHex()));
        btnGuardarCategoria.setText("Guardar cambios");
        txtNombreCategoria.requestFocusInWindow();
    }

    private void eliminarCategoriaSeleccionada() {
        if (controlador == null || !controlador.hayUsuario()) {
            return;
        }

        int fila = tablaCategorias.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una categoría de la tabla.",
                    "Selección requerida", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(
                this, "¿Deseas eliminar la categoría seleccionada?",
                "Confirmar eliminación", javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            String id = idsMostrados.get(tablaCategorias.convertRowIndexToModel(fila));
            Categoria categoria = controlador.buscarCategoria(id);

            if (controlador.eliminarCategoria(id)) {
                try {
                    controlador.guardarDatos();
                    refrescarDatos();
                } catch (java.io.IOException ex) {
                    // Restaura la categoría en memoria si el archivo no pudo actualizarse.
                    controlador.agregarCategoria(categoria);
                    refrescarDatos();
                    mostrarMensaje("No fue posible guardar el cambio: " + ex.getMessage(),
                            "Error de almacenamiento", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void limpiarFormulario() {
        idCategoriaEnEdicion = null;
        btnGuardarCategoria.setText("Guardar");
        txtNombreCategoria.setText("");
        txtDescripcionCategoria.setText("");
        actualizarVistaPreviaColor(EstiloVisualDIA.AZUL_ACENTO);
        txtNombreCategoria.requestFocusInWindow();
    }

    private void mostrarMensaje(String mensaje, String titulo, int tipo) {
        javax.swing.JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelEncabezadoCategorias = new javax.swing.JPanel();
        lblTituloCategorias = new javax.swing.JLabel();
        lblSubtituloCategorias = new javax.swing.JLabel();
        panelFormularioCategoria = new javax.swing.JPanel();
        lblTituloNombreCategoria = new javax.swing.JLabel();
        lblNombreCategoria = new javax.swing.JLabel();
        txtNombreCategoria = new javax.swing.JTextField();
        lblDescripcionCategoria = new javax.swing.JLabel();
        scrollDescripcionCategoria = new javax.swing.JScrollPane();
        txtDescripcionCategoria = new javax.swing.JTextArea();
        lblColorCategoria = new javax.swing.JLabel();
        txtColorCategoria = new javax.swing.JTextField();
        btnGuardarCategoria = new javax.swing.JButton();
        btnLimpiarCategoria = new javax.swing.JButton();
        btnElegirColorCategoria = new javax.swing.JButton();
        panelTablaCategorias = new javax.swing.JPanel();
        lblTituloTablaCategorias = new javax.swing.JLabel();
        scrollCategorias = new javax.swing.JScrollPane();
        tablaCategorias = new javax.swing.JTable();
        btnEliminarCategoria = new javax.swing.JButton();
        btnEditarCategoria = new javax.swing.JButton();

        setBackground(new java.awt.Color(243, 245, 248));

        panelEncabezadoCategorias.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloCategorias.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloCategorias.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloCategorias.setText("Categorías");

        lblSubtituloCategorias.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtituloCategorias.setText("Organiza tus tareas y eventos por categorías.");

        javax.swing.GroupLayout panelEncabezadoCategoriasLayout = new javax.swing.GroupLayout(panelEncabezadoCategorias);
        panelEncabezadoCategorias.setLayout(panelEncabezadoCategoriasLayout);
        panelEncabezadoCategoriasLayout.setHorizontalGroup(
            panelEncabezadoCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoCategoriasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEncabezadoCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloCategorias)
                    .addComponent(lblSubtituloCategorias))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncabezadoCategoriasLayout.setVerticalGroup(
            panelEncabezadoCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoCategoriasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloCategorias)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtituloCategorias)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelFormularioCategoria.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloNombreCategoria.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloNombreCategoria.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloNombreCategoria.setText("Crear categoría");

        lblNombreCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblNombreCategoria.setText("Nombre de la categoría");

        lblDescripcionCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDescripcionCategoria.setText("Descripción");

        txtDescripcionCategoria.setColumns(20);
        txtDescripcionCategoria.setRows(5);
        scrollDescripcionCategoria.setViewportView(txtDescripcionCategoria);

        lblColorCategoria.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblColorCategoria.setText("Color de identificación");

        btnGuardarCategoria.setText("Guardar");

        btnLimpiarCategoria.setText("Limpiar");
        btnLimpiarCategoria.addActionListener(this::btnLimpiarCategoriaActionPerformed);

        btnElegirColorCategoria.setText("Elegir color");

        javax.swing.GroupLayout panelFormularioCategoriaLayout = new javax.swing.GroupLayout(panelFormularioCategoria);
        panelFormularioCategoria.setLayout(panelFormularioCategoriaLayout);
        panelFormularioCategoriaLayout.setHorizontalGroup(
            panelFormularioCategoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioCategoriaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFormularioCategoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNombreCategoria)
                    .addComponent(scrollDescripcionCategoria)
                    .addGroup(panelFormularioCategoriaLayout.createSequentialGroup()
                        .addGroup(panelFormularioCategoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTituloNombreCategoria)
                            .addComponent(lblNombreCategoria)
                            .addComponent(lblDescripcionCategoria)
                            .addComponent(lblColorCategoria))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormularioCategoriaLayout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addComponent(btnGuardarCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiarCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormularioCategoriaLayout.createSequentialGroup()
                        .addComponent(txtColorCategoria)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnElegirColorCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelFormularioCategoriaLayout.setVerticalGroup(
            panelFormularioCategoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioCategoriaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloNombreCategoria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNombreCategoria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNombreCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDescripcionCategoria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollDescripcionCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblColorCategoria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioCategoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtColorCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnElegirColorCategoria))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFormularioCategoriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardarCategoria)
                    .addComponent(btnLimpiarCategoria))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelTablaCategorias.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloTablaCategorias.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloTablaCategorias.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloTablaCategorias.setText("Categorías registradas");

        tablaCategorias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre", "Descripción", "Color"
            }
        ));
        tablaCategorias.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollCategorias.setViewportView(tablaCategorias);

        btnEliminarCategoria.setText("Eliminar Categoria");
        btnEliminarCategoria.addActionListener(this::btnEliminarCategoriaActionPerformed);

        btnEditarCategoria.setText("Editar categoría");

        javax.swing.GroupLayout panelTablaCategoriasLayout = new javax.swing.GroupLayout(panelTablaCategorias);
        panelTablaCategorias.setLayout(panelTablaCategoriasLayout);
        panelTablaCategoriasLayout.setHorizontalGroup(
            panelTablaCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaCategoriasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollCategorias)
                    .addGroup(panelTablaCategoriasLayout.createSequentialGroup()
                        .addGroup(panelTablaCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTituloTablaCategorias)
                            .addGroup(panelTablaCategoriasLayout.createSequentialGroup()
                                .addComponent(btnEliminarCategoria)
                                .addGap(18, 18, 18)
                                .addComponent(btnEditarCategoria)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelTablaCategoriasLayout.setVerticalGroup(
            panelTablaCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaCategoriasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloTablaCategorias)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTablaCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEliminarCategoria)
                    .addComponent(btnEditarCategoria))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelEncabezadoCategorias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelFormularioCategoria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelTablaCategorias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelEncabezadoCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelFormularioCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTablaCategorias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarCategoriaActionPerformed(java.awt.event.ActionEvent evt) {
        limpiarFormulario();
    }

    private void btnEliminarCategoriaActionPerformed(java.awt.event.ActionEvent evt) {
        eliminarCategoriaSeleccionada();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditarCategoria;
    private javax.swing.JButton btnElegirColorCategoria;
    private javax.swing.JButton btnEliminarCategoria;
    private javax.swing.JButton btnGuardarCategoria;
    private javax.swing.JButton btnLimpiarCategoria;
    private javax.swing.JLabel lblColorCategoria;
    private javax.swing.JLabel lblDescripcionCategoria;
    private javax.swing.JLabel lblNombreCategoria;
    private javax.swing.JLabel lblSubtituloCategorias;
    private javax.swing.JLabel lblTituloCategorias;
    private javax.swing.JLabel lblTituloNombreCategoria;
    private javax.swing.JLabel lblTituloTablaCategorias;
    private javax.swing.JPanel panelEncabezadoCategorias;
    private javax.swing.JPanel panelFormularioCategoria;
    private javax.swing.JPanel panelTablaCategorias;
    private javax.swing.JScrollPane scrollCategorias;
    private javax.swing.JScrollPane scrollDescripcionCategoria;
    private javax.swing.JTable tablaCategorias;
    private javax.swing.JTextField txtColorCategoria;
    private javax.swing.JTextArea txtDescripcionCategoria;
    private javax.swing.JTextField txtNombreCategoria;
    // End of variables declaration//GEN-END:variables
}

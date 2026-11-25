package vista;

import controlador.ControladorDIA;
import modelo.Recordatorio;
import modelo.Tarea;
import modelo.Categoria;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

/**
 * Formulario y tabla de tareas.
 * Los controles visuales permanecen editables desde el GUI Builder.
 */
public class PanelTareas extends javax.swing.JPanel {

    private final ControladorDIA controlador;
    private final DefaultTableModel modeloTabla;

    public PanelTareas() {
        this(null);
    }

    public PanelTareas(ControladorDIA controlador) {
        initComponents();
        this.controlador = controlador;

        cmbPrioridadTarea.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Baja", "Media", "Alta"}));
        cmbEstadoTarea.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Pendiente", "Completada", "Inactiva"}));
        actualizarComboCategorias();

        modeloTabla = new DefaultTableModel(
                new Object[]{"Título", "Fecha", "Categoría", "Prioridad", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaTareas.setModel(modeloTabla);
        EstiloVisualDIA.configurarTabla(tablaTareas, 4);
        tablaTareas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configurarFiltroPorEstado();

        btnLimpiarTarea.addActionListener(e -> limpiarFormulario());
        establecerFechaHoraActual();
        txtDescripcionTarea.setText("");
        refrescarDatos();
    }


    // Controles de selección para filtrar la tabla sin modificar los registros.
    private javax.swing.JRadioButton radioTodasTareas;
    private javax.swing.JRadioButton radioPendientesTareas;
    private javax.swing.JRadioButton radioCompletadasTareas;
    private javax.swing.ButtonGroup grupoFiltroTareas;

    /** Agrupa los radio buttons para permitir una sola opción de filtro activa. */
    private void configurarFiltroPorEstado() {
        radioTodasTareas = new javax.swing.JRadioButton("Todas", true);
        radioPendientesTareas = new javax.swing.JRadioButton("Pendientes");
        radioCompletadasTareas = new javax.swing.JRadioButton("Completadas");
        grupoFiltroTareas = new javax.swing.ButtonGroup();
        grupoFiltroTareas.add(radioTodasTareas);
        grupoFiltroTareas.add(radioPendientesTareas);
        grupoFiltroTareas.add(radioCompletadasTareas);

        javax.swing.JPanel filtros = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 4));
        filtros.setBackground(new java.awt.Color(233, 239, 247));
        filtros.add(new javax.swing.JLabel("Filtrar:") );
        filtros.add(radioTodasTareas);
        filtros.add(radioPendientesTareas);
        filtros.add(radioCompletadasTareas);

        java.awt.event.ActionListener cambio = e -> refrescarDatos();
        radioTodasTareas.addActionListener(cambio);
        radioPendientesTareas.addActionListener(cambio);
        radioCompletadasTareas.addActionListener(cambio);

        javax.swing.JPanel encabezadoTabla = new javax.swing.JPanel(
                new java.awt.BorderLayout(4, 4));
        encabezadoTabla.setBackground(new java.awt.Color(243, 245, 248));
        encabezadoTabla.add(lblTituloTablaTareas, java.awt.BorderLayout.NORTH);
        encabezadoTabla.add(filtros, java.awt.BorderLayout.SOUTH);

        panelTablaTareas.removeAll();
        panelTablaTareas.setLayout(new java.awt.BorderLayout(4, 4));
        panelTablaTareas.add(encabezadoTabla, java.awt.BorderLayout.NORTH);
        panelTablaTareas.add(scrollTareas, java.awt.BorderLayout.CENTER);
    }

    /** Valida los campos, instancia Tarea y solicita su almacenamiento. */
    private void guardarTarea() {
        if (controlador == null || !controlador.hayUsuario()) {
            mostrarMensaje("Primero debes iniciar sesión.", "Sesión requerida",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String titulo = txtTituloTarea.getText().trim();
        String descripcion = txtDescripcionTarea.getText().trim();
        String fecha = txtFechaTarea.getText().trim();
        String hora = txtHoraTarea.getText().trim();

        if (titulo.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            mostrarMensaje("Completa el título, la fecha y la hora.",
                    "Datos incompletos", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime fechaHora = LocalDateTime.of(
                    LocalDate.parse(fecha, formatoFecha),
                    LocalTime.parse(hora, formatoHora));

            String prioridad = cmbPrioridadTarea.getSelectedItem().toString();
            Tarea tarea = new Tarea(titulo, descripcion, fechaHora, prioridad);
            Categoria categoria = obtenerCategoriaSeleccionada();
            if (categoria != null) {
                tarea.asignarCategoria(categoria);
            }

            String estado = cmbEstadoTarea.getSelectedItem().toString();
            if ("Completada".equals(estado)) {
                tarea.marcarCompletado();
            } else if ("Inactiva".equals(estado)) {
                tarea.cambiarEstado(false);
            }

            controlador.agregarRecordatorio(tarea);
            try {
                controlador.guardarDatos();
            } catch (java.io.IOException ex) {
                controlador.eliminarRecordatorio(tarea.getId());
                throw ex;
            }

            limpiarFormulario();
            refrescarDatos();
            mostrarMensaje("La tarea se guardó correctamente.", "Registro exitoso",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (java.time.format.DateTimeParseException ex) {
            mostrarMensaje("Verifica la fecha (dd/MM/aaaa) y la hora (HH:mm).",
                    "Formato incorrecto", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage(), "Datos inválidos",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (java.io.IOException ex) {
            mostrarMensaje("No fue posible guardar la tarea: " + ex.getMessage(),
                    "Error de almacenamiento", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Actualiza la tabla utilizando solamente tareas del perfil activo. */
    public void refrescarDatos() {
        actualizarComboCategorias();
        modeloTabla.setRowCount(0);
        if (controlador == null || !controlador.hayUsuario()) {
            return;
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Recordatorio r : controlador.getUsuario().getRecordatorios()) {
            if (r instanceof Tarea) {
                Tarea tarea = (Tarea) r;
                String estado = tarea.isCompletado() ? "Completada"
                        : (tarea.isActivo() ? "Pendiente" : "Inactiva");
                if (radioPendientesTareas.isSelected() && !"Pendiente".equals(estado)) {
                    continue;
                }
                if (radioCompletadasTareas.isSelected() && !"Completada".equals(estado)) {
                    continue;
                }
                String nombreCategoria = tarea.getCategoria() == null
                        ? "—" : tarea.getCategoria().getNombre();
                modeloTabla.addRow(new Object[]{
                    tarea.getTitulo(), tarea.getFechaHora().format(formato),
                    nombreCategoria, tarea.getPrioridad(), estado
                });
            }
        }
    }

    /**
     * Carga las categorías del perfil activo en el selector del formulario.
     * La primera opción permite registrar una tarea sin categoría.
     */
    private void actualizarComboCategorias() {
        String seleccionAnterior = (String) cmbCategoriaTarea.getSelectedItem();
        javax.swing.DefaultComboBoxModel<String> modelo =
                new javax.swing.DefaultComboBoxModel<>();
        modelo.addElement("Sin categoría");
        if (controlador != null && controlador.hayUsuario()) {
            for (Categoria categoria : controlador.getUsuario().getCategorias()) {
                modelo.addElement(categoria.getNombre());
            }
        }
        cmbCategoriaTarea.setModel(modelo);
        if (seleccionAnterior != null) {
            cmbCategoriaTarea.setSelectedItem(seleccionAnterior);
        }
    }

    /** Resuelve el nombre seleccionado al objeto Categoria del usuario activo. */
    private Categoria obtenerCategoriaSeleccionada() {
        Object seleccion = cmbCategoriaTarea.getSelectedItem();
        if (seleccion == null || "Sin categoría".equals(seleccion)
                || controlador == null || !controlador.hayUsuario()) {
            return null;
        }
        for (Categoria categoria : controlador.getUsuario().getCategorias()) {
            if (categoria.getNombre().equals(seleccion.toString())) {
                return categoria;
            }
        }
        return null;
    }

    private void limpiarFormulario() {
        txtTituloTarea.setText("");
        txtDescripcionTarea.setText("");
        establecerFechaHoraActual();
        cmbPrioridadTarea.setSelectedIndex(0);
        cmbEstadoTarea.setSelectedIndex(0);
        if (cmbCategoriaTarea.getItemCount() > 0) {
            cmbCategoriaTarea.setSelectedIndex(0);
        }
    }

    private void establecerFechaHoraActual() {
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        txtFechaTarea.setText(ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtHoraTarea.setText(ahora.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void mostrarMensaje(String mensaje, String titulo, int tipo) {
        javax.swing.JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelEncabezadoTareas = new javax.swing.JPanel();
        lblTituloTareas = new javax.swing.JLabel();
        lblSubtituloTareas = new javax.swing.JLabel();
        panelFormularioTarea = new javax.swing.JPanel();
        lblTituloTarea = new javax.swing.JLabel();
        txtTituloTarea = new javax.swing.JTextField();
        scrollDescripcionTarea = new javax.swing.JScrollPane();
        txtDescripcionTarea = new javax.swing.JTextArea();
        lblDescripcionTarea = new javax.swing.JLabel();
        lblFechaTarea = new javax.swing.JLabel();
        txtFechaTarea = new javax.swing.JTextField();
        lblHoraTarea = new javax.swing.JLabel();
        txtHoraTarea = new javax.swing.JTextField();
        lblPrioridadTarea = new javax.swing.JLabel();
        lblEstadoTarea = new javax.swing.JLabel();
        cmbPrioridadTarea = new javax.swing.JComboBox<>();
        cmbEstadoTarea = new javax.swing.JComboBox<>();
        btnGuardarTarea = new javax.swing.JButton();
        btnLimpiarTarea = new javax.swing.JButton();
        lblCategoriaTarea = new javax.swing.JLabel();
        cmbCategoriaTarea = new javax.swing.JComboBox<>();
        panelTablaTareas = new javax.swing.JPanel();
        lblTituloTablaTareas = new javax.swing.JLabel();
        scrollTareas = new javax.swing.JScrollPane();
        tablaTareas = new javax.swing.JTable();

        setBackground(new java.awt.Color(243, 245, 248));

        panelEncabezadoTareas.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloTareas.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloTareas.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloTareas.setText("Tareas");

        lblSubtituloTareas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtituloTareas.setText("Administra y organiza tus tareas.");

        javax.swing.GroupLayout panelEncabezadoTareasLayout = new javax.swing.GroupLayout(panelEncabezadoTareas);
        panelEncabezadoTareas.setLayout(panelEncabezadoTareasLayout);
        panelEncabezadoTareasLayout.setHorizontalGroup(
            panelEncabezadoTareasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoTareasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEncabezadoTareasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloTareas)
                    .addComponent(lblSubtituloTareas))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncabezadoTareasLayout.setVerticalGroup(
            panelEncabezadoTareasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoTareasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloTareas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSubtituloTareas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelFormularioTarea.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloTarea.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTituloTarea.setText("Titulo");

        txtTituloTarea.setText("Nombre de tarea");
        txtTituloTarea.setPreferredSize(new java.awt.Dimension(245, 22));
        txtTituloTarea.addActionListener(this::txtTituloTareaActionPerformed);

        txtDescripcionTarea.setColumns(20);
        txtDescripcionTarea.setRows(5);
        txtDescripcionTarea.setText("Descripción de tarea");
        scrollDescripcionTarea.setViewportView(txtDescripcionTarea);

        lblDescripcionTarea.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDescripcionTarea.setText("Descripción");

        lblFechaTarea.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblFechaTarea.setText("Fecha");

        txtFechaTarea.setText("Fecha");
        txtFechaTarea.addActionListener(this::txtFechaTareaActionPerformed);

        lblHoraTarea.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblHoraTarea.setText("Hora");

        txtHoraTarea.setText("Hora");

        lblPrioridadTarea.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrioridadTarea.setText("Prioridad");

        lblEstadoTarea.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblEstadoTarea.setText("Estado");

        cmbPrioridadTarea.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar" }));

        cmbEstadoTarea.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar" }));

        btnGuardarTarea.setText("Guardar");
        btnGuardarTarea.addActionListener(this::btnGuardarTareaActionPerformed);

        btnLimpiarTarea.setText("Limpiar");
        btnLimpiarTarea.addActionListener(this::btnLimpiarTareaActionPerformed);

        lblCategoriaTarea.setText("Categoría");

        cmbCategoriaTarea.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout panelFormularioTareaLayout = new javax.swing.GroupLayout(panelFormularioTarea);
        panelFormularioTarea.setLayout(panelFormularioTareaLayout);
        panelFormularioTareaLayout.setHorizontalGroup(
            panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioTareaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTituloTarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollDescripcionTarea)
                    .addComponent(cmbCategoriaTarea, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormularioTareaLayout.createSequentialGroup()
                        .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormularioTareaLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnGuardarTarea, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtFechaTarea)
                            .addComponent(cmbPrioridadTarea, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelFormularioTareaLayout.createSequentialGroup()
                                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblPrioridadTarea)
                                    .addComponent(lblFechaTarea)
                                    .addComponent(lblTituloTarea)
                                    .addComponent(lblDescripcionTarea)
                                    .addComponent(lblCategoriaTarea))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(32, 32, 32)
                        .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblHoraTarea)
                            .addComponent(txtHoraTarea)
                            .addComponent(lblEstadoTarea)
                            .addComponent(cmbEstadoTarea, 0, 224, Short.MAX_VALUE)
                            .addComponent(btnLimpiarTarea, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        panelFormularioTareaLayout.setVerticalGroup(
            panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioTareaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloTarea)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTituloTarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDescripcionTarea)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollDescripcionTarea, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCategoriaTarea)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbCategoriaTarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaTarea)
                    .addComponent(lblHoraTarea))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFechaTarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHoraTarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPrioridadTarea)
                    .addComponent(lblEstadoTarea))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbPrioridadTarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbEstadoTarea, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFormularioTareaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpiarTarea)
                    .addComponent(btnGuardarTarea)))
        );

        panelTablaTareas.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloTablaTareas.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloTablaTareas.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloTablaTareas.setText("Tareas Registradas");

        tablaTareas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Título", "Fecha", "Categoría", "Prioridad", "Estado"
            }
        ));
        tablaTareas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollTareas.setViewportView(tablaTareas);

        javax.swing.GroupLayout panelTablaTareasLayout = new javax.swing.GroupLayout(panelTablaTareas);
        panelTablaTareas.setLayout(panelTablaTareasLayout);
        panelTablaTareasLayout.setHorizontalGroup(
            panelTablaTareasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaTareasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaTareasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollTareas, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                    .addGroup(panelTablaTareasLayout.createSequentialGroup()
                        .addComponent(lblTituloTablaTareas)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelTablaTareasLayout.setVerticalGroup(
            panelTablaTareasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaTareasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTituloTablaTareas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollTareas, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelEncabezadoTareas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelFormularioTarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelTablaTareas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelEncabezadoTareas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelFormularioTarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTablaTareas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarTareaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarTareaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLimpiarTareaActionPerformed

    private void txtTituloTareaActionPerformed(java.awt.event.ActionEvent evt) {
        guardarTarea();
    }

    private void btnGuardarTareaActionPerformed(java.awt.event.ActionEvent evt) {
        guardarTarea();
    }

    private void txtFechaTareaActionPerformed(java.awt.event.ActionEvent evt) {
        guardarTarea();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardarTarea;
    private javax.swing.JButton btnLimpiarTarea;
    private javax.swing.JComboBox<String> cmbCategoriaTarea;
    private javax.swing.JComboBox<String> cmbEstadoTarea;
    private javax.swing.JComboBox<String> cmbPrioridadTarea;
    private javax.swing.JLabel lblCategoriaTarea;
    private javax.swing.JLabel lblDescripcionTarea;
    private javax.swing.JLabel lblEstadoTarea;
    private javax.swing.JLabel lblFechaTarea;
    private javax.swing.JLabel lblHoraTarea;
    private javax.swing.JLabel lblPrioridadTarea;
    private javax.swing.JLabel lblSubtituloTareas;
    private javax.swing.JLabel lblTituloTablaTareas;
    private javax.swing.JLabel lblTituloTarea;
    private javax.swing.JLabel lblTituloTareas;
    private javax.swing.JPanel panelEncabezadoTareas;
    private javax.swing.JPanel panelFormularioTarea;
    private javax.swing.JPanel panelTablaTareas;
    private javax.swing.JScrollPane scrollDescripcionTarea;
    private javax.swing.JScrollPane scrollTareas;
    private javax.swing.JTable tablaTareas;
    private javax.swing.JTextArea txtDescripcionTarea;
    private javax.swing.JTextField txtFechaTarea;
    private javax.swing.JTextField txtHoraTarea;
    private javax.swing.JTextField txtTituloTarea;
    // End of variables declaration//GEN-END:variables
}

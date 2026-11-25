package vista;

import controlador.ControladorDIA;
import modelo.Recordatorio;
import modelo.Evento;
import modelo.Categoria;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

/** Vista para registrar y consultar eventos del usuario activo. */
public class PanelEventos extends javax.swing.JPanel {

    private final ControladorDIA controlador;
    private final DefaultTableModel modeloTabla;

    public PanelEventos() {
        this(null);
    }

    public PanelEventos(ControladorDIA controlador) {
        initComponents();

        // Reserva una altura de contenido mayor que el área visible para que
        // el JScrollPane exterior pueda desplazarse hasta la tabla de Eventos.
        // No modifica el diseño interno creado en NetBeans Design.
        java.awt.Dimension tamanoContenido = getPreferredSize();
        setPreferredSize(new java.awt.Dimension(
                tamanoContenido.width,
                Math.max(tamanoContenido.height, 850)));

        this.controlador = controlador;

        cmbEstadoEvento.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Pendiente", "Completado", "Inactivo"}));
        actualizarComboCategorias();

        modeloTabla = new DefaultTableModel(
                new Object[]{"Título", "Categoría", "Fecha", "Hora", "Lugar", "Duración", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaEventos.setModel(modeloTabla);
        EstiloVisualDIA.configurarTabla(tablaEventos, 6);
        tablaEventos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configurarFiltroEventos();

        btnGuardarEvento.addActionListener(e -> guardarEvento());
        btnLimpiarEvento.addActionListener(e -> limpiarFormulario());
        establecerFechaHoraActual();
        txtDescripcionEvento.setText("");
        txtDuracionEvento.setText("60");
        refrescarDatos();
    }


    // Casilla de verificación para mostrar únicamente eventos pendientes.
    private javax.swing.JCheckBox chkSoloEventosPendientes;

    /** Incorpora un JCheckBox que filtra eventos activos y no completados. */
    private void configurarFiltroEventos() {
        chkSoloEventosPendientes = new javax.swing.JCheckBox(
                "Mostrar solo eventos pendientes");
        chkSoloEventosPendientes.setBackground(new java.awt.Color(233, 239, 247));
        chkSoloEventosPendientes.addActionListener(e -> refrescarDatos());

        javax.swing.JPanel encabezadoTabla = new javax.swing.JPanel(
                new java.awt.BorderLayout(4, 4));
        encabezadoTabla.setBackground(new java.awt.Color(243, 245, 248));
        encabezadoTabla.add(lblTituloTablaEventos, java.awt.BorderLayout.NORTH);
        encabezadoTabla.add(chkSoloEventosPendientes, java.awt.BorderLayout.SOUTH);

        panelTablaEventos.removeAll();
        panelTablaEventos.setLayout(new java.awt.BorderLayout(4, 4));
        panelTablaEventos.add(encabezadoTabla, java.awt.BorderLayout.NORTH);
        panelTablaEventos.add(scrollEventos, java.awt.BorderLayout.CENTER);
    }

    /** Lee el formulario, crea un Evento y guarda el cambio del perfil activo. */
    private void guardarEvento() {
        if (controlador == null || !controlador.hayUsuario()) {
            mostrarMensaje("Primero debes iniciar sesión.", "Sesión requerida",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String titulo = txtTituloEvento.getText().trim();
        String descripcion = txtDescripcionEvento.getText().trim();
        String lugar = txtLugarEvento.getText().trim();
        String fecha = txtFechaEvento.getText().trim();
        String hora = txtHoraEvento.getText().trim();
        String duracionTexto = txtDuracionEvento.getText().trim();

        if (titulo.isEmpty() || lugar.isEmpty() || fecha.isEmpty()
                || hora.isEmpty() || duracionTexto.isEmpty()) {
            mostrarMensaje("Completa título, lugar, fecha, hora y duración.",
                    "Datos incompletos", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime fechaHora = LocalDateTime.of(
                    LocalDate.parse(fecha, formatoFecha),
                    LocalTime.parse(hora, formatoHora));
            int duracion = Integer.parseInt(duracionTexto);

            Evento evento = new Evento(titulo, descripcion, fechaHora, lugar, duracion);
            Categoria categoria = obtenerCategoriaSeleccionada();
            if (categoria != null) {
                evento.asignarCategoria(categoria);
            }
            String estadoSeleccionado = String.valueOf(cmbEstadoEvento.getSelectedItem());
            if ("Completado".equals(estadoSeleccionado)) {
                evento.marcarCompletado();
            } else if ("Inactivo".equals(estadoSeleccionado)) {
                evento.cambiarEstado(false);
            }
            controlador.agregarRecordatorio(evento);
            try {
                controlador.guardarDatos();
            } catch (java.io.IOException ex) {
                controlador.eliminarRecordatorio(evento.getId());
                throw ex;
            }

            limpiarFormulario();
            refrescarDatos();
            mostrarMensaje("El evento se guardó correctamente.", "Registro exitoso",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (java.time.format.DateTimeParseException ex) {
            mostrarMensaje("Verifica la fecha (dd/MM/aaaa) y la hora (HH:mm).",
                    "Formato incorrecto", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            mostrarMensaje("La duración debe ser un número entero de minutos.",
                    "Dato incorrecto", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage(), "Datos inválidos",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (java.io.IOException ex) {
            mostrarMensaje("No fue posible guardar el evento: " + ex.getMessage(),
                    "Error de almacenamiento", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Carga en la tabla los eventos del usuario activo. */
    public void refrescarDatos() {
        actualizarComboCategorias();
        modeloTabla.setRowCount(0);
        if (controlador == null || !controlador.hayUsuario()) {
            return;
        }

        DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");
        for (Recordatorio r : controlador.getUsuario().getRecordatorios()) {
            if (r instanceof Evento) {
                Evento evento = (Evento) r;
                String estado = evento.isCompletado() ? "Completado"
                        : (evento.isActivo() ? "Pendiente" : "Inactivo");
                if (chkSoloEventosPendientes.isSelected()
                        && (evento.isCompletado() || !evento.isActivo())) {
                    continue;
                }
                String nombreCategoria = evento.getCategoria() == null
                        ? "—" : evento.getCategoria().getNombre();
                modeloTabla.addRow(new Object[]{
                    evento.getTitulo(), nombreCategoria,
                    evento.getFechaHora().format(fecha),
                    evento.getFechaHora().format(hora), evento.getLugar(),
                    evento.getDuracionMinutos() + " min", estado
                });
            }
        }
    }

    /**
     * Actualiza el selector con las categorías disponibles en el perfil activo.
     * Los eventos pueden registrarse sin categoría.
     */
    private void actualizarComboCategorias() {
        String seleccionAnterior = (String) cmbCategoriaEvento.getSelectedItem();
        javax.swing.DefaultComboBoxModel<String> modelo =
                new javax.swing.DefaultComboBoxModel<>();
        modelo.addElement("Sin categoría");
        if (controlador != null && controlador.hayUsuario()) {
            for (Categoria categoria : controlador.getUsuario().getCategorias()) {
                modelo.addElement(categoria.getNombre());
            }
        }
        cmbCategoriaEvento.setModel(modelo);
        if (seleccionAnterior != null) {
            cmbCategoriaEvento.setSelectedItem(seleccionAnterior);
        }
    }

    /** Obtiene la categoría elegida a partir de su nombre visible. */
    private Categoria obtenerCategoriaSeleccionada() {
        Object seleccion = cmbCategoriaEvento.getSelectedItem();
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
        txtTituloEvento.setText("");
        txtDescripcionEvento.setText("");
        txtLugarEvento.setText("");
        txtDuracionEvento.setText("60");
        if (cmbCategoriaEvento.getItemCount() > 0) {
            cmbCategoriaEvento.setSelectedIndex(0);
        }
        if (cmbEstadoEvento.getItemCount() > 0) {
            cmbEstadoEvento.setSelectedIndex(0);
        }
        establecerFechaHoraActual();
    }

    private void establecerFechaHoraActual() {
        LocalDateTime ahora = LocalDateTime.now();
        txtFechaEvento.setText(ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtHoraEvento.setText(ahora.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void mostrarMensaje(String mensaje, String titulo, int tipo) {
        javax.swing.JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelEncabezadoEventos = new javax.swing.JPanel();
        lblTituloEventos = new javax.swing.JLabel();
        lblSubtituloEventos = new javax.swing.JLabel();
        panelFormularioEvento = new javax.swing.JPanel();
        lblTituloPrincipalEvento = new javax.swing.JLabel();
        lblTituloEvento = new javax.swing.JLabel();
        txtTituloEvento = new javax.swing.JTextField();
        lblDescripcionEvento = new javax.swing.JLabel();
        scrollDescripcionEvento = new javax.swing.JScrollPane();
        txtDescripcionEvento = new javax.swing.JTextArea();
        lblFechaEvento = new javax.swing.JLabel();
        txtFechaEvento = new javax.swing.JTextField();
        lblHoraEvento = new javax.swing.JLabel();
        txtHoraEvento = new javax.swing.JTextField();
        lblLugarEvento = new javax.swing.JLabel();
        txtLugarEvento = new javax.swing.JTextField();
        lblDuracionEvento = new javax.swing.JLabel();
        txtDuracionEvento = new javax.swing.JTextField();
        btnGuardarEvento = new javax.swing.JButton();
        btnLimpiarEvento = new javax.swing.JButton();
        lblCategoriaEvento = new javax.swing.JLabel();
        lblEstadoEvento = new javax.swing.JLabel();
        cmbCategoriaEvento = new javax.swing.JComboBox<>();
        cmbEstadoEvento = new javax.swing.JComboBox<>();
        panelTablaEventos = new javax.swing.JPanel();
        lblTituloTablaEventos = new javax.swing.JLabel();
        scrollEventos = new javax.swing.JScrollPane();
        tablaEventos = new javax.swing.JTable();

        setBackground(new java.awt.Color(243, 245, 248));
        // Se deja que GroupLayout calcule la altura real del contenido.
        // Así el JScrollPane exterior puede desplazar toda la vista de Eventos.

        panelEncabezadoEventos.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloEventos.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloEventos.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloEventos.setText("Eventos");

        lblSubtituloEventos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtituloEventos.setText("Programa y administra tus próximos eventos.");

        javax.swing.GroupLayout panelEncabezadoEventosLayout = new javax.swing.GroupLayout(panelEncabezadoEventos);
        panelEncabezadoEventos.setLayout(panelEncabezadoEventosLayout);
        panelEncabezadoEventosLayout.setHorizontalGroup(
            panelEncabezadoEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoEventosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEncabezadoEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloEventos)
                    .addComponent(lblSubtituloEventos))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncabezadoEventosLayout.setVerticalGroup(
            panelEncabezadoEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoEventosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloEventos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtituloEventos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelFormularioEvento.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloPrincipalEvento.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloPrincipalEvento.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloPrincipalEvento.setText("Registrar evento");

        lblTituloEvento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTituloEvento.setText("Título del evento");

        lblDescripcionEvento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDescripcionEvento.setText("Descrición");

        txtDescripcionEvento.setColumns(20);
        txtDescripcionEvento.setRows(5);
        scrollDescripcionEvento.setViewportView(txtDescripcionEvento);

        lblFechaEvento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblFechaEvento.setText("Fecha");

        lblHoraEvento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblHoraEvento.setText("Hora");

        lblLugarEvento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLugarEvento.setText("Lugar");

        lblDuracionEvento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDuracionEvento.setText("Duración (minutos)");

        btnGuardarEvento.setText("Guardar evento");

        btnLimpiarEvento.setText("Limpiar");

        lblCategoriaEvento.setText("Categoría");

        lblEstadoEvento.setText("Estado");

        cmbCategoriaEvento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar" }));

        cmbEstadoEvento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccionar" }));
        cmbEstadoEvento.addActionListener(this::cmbEstadoEventoActionPerformed);

        javax.swing.GroupLayout panelFormularioEventoLayout = new javax.swing.GroupLayout(panelFormularioEvento);
        panelFormularioEvento.setLayout(panelFormularioEventoLayout);
        panelFormularioEventoLayout.setHorizontalGroup(
            panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTituloEvento)
                    .addComponent(scrollDescripcionEvento)
                    .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                        .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(lblFechaEvento, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtFechaEvento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                                .addComponent(txtLugarEvento, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(lblLugarEvento)
                            .addComponent(btnGuardarEvento, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblDuracionEvento)
                                    .addComponent(lblHoraEvento)
                                    .addComponent(txtHoraEvento, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                                    .addComponent(lblEstadoEvento)
                                    .addComponent(cmbEstadoEvento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtDuracionEvento)
                                    .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                                        .addComponent(btnLimpiarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))))
                    .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                        .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTituloPrincipalEvento)
                            .addComponent(lblTituloEvento)
                            .addComponent(lblDescripcionEvento)
                            .addComponent(lblCategoriaEvento)
                            .addComponent(cmbCategoriaEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelFormularioEventoLayout.setVerticalGroup(
            panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioEventoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloPrincipalEvento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTituloEvento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTituloEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDescripcionEvento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollDescripcionEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategoriaEvento)
                    .addComponent(lblEstadoEvento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCategoriaEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbEstadoEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaEvento)
                    .addComponent(lblHoraEvento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFechaEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHoraEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLugarEvento)
                    .addComponent(lblDuracionEvento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLugarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDuracionEvento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioEventoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardarEvento)
                    .addComponent(btnLimpiarEvento))
                .addContainerGap())
        );

        panelTablaEventos.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloTablaEventos.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloTablaEventos.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloTablaEventos.setText("Eventos registrados");

        tablaEventos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Título", "Categorìa", "Fecha", "Hora", "Lugar", "Duración", "Estado"
            }
        ));
        tablaEventos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollEventos.setViewportView(tablaEventos);

        javax.swing.GroupLayout panelTablaEventosLayout = new javax.swing.GroupLayout(panelTablaEventos);
        panelTablaEventos.setLayout(panelTablaEventosLayout);
        panelTablaEventosLayout.setHorizontalGroup(
            panelTablaEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaEventosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTablaEventosLayout.createSequentialGroup()
                        .addComponent(lblTituloTablaEventos)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrollEventos, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelTablaEventosLayout.setVerticalGroup(
            panelTablaEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaEventosLayout.createSequentialGroup()
                .addComponent(lblTituloTablaEventos)
                .addGap(1, 1, 1)
                .addComponent(scrollEventos, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelEncabezadoEventos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelFormularioEvento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelTablaEventos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelEncabezadoEventos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelFormularioEvento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelTablaEventos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbEstadoEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbEstadoEventoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbEstadoEventoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardarEvento;
    private javax.swing.JButton btnLimpiarEvento;
    private javax.swing.JComboBox<String> cmbCategoriaEvento;
    private javax.swing.JComboBox<String> cmbEstadoEvento;
    private javax.swing.JLabel lblCategoriaEvento;
    private javax.swing.JLabel lblDescripcionEvento;
    private javax.swing.JLabel lblDuracionEvento;
    private javax.swing.JLabel lblEstadoEvento;
    private javax.swing.JLabel lblFechaEvento;
    private javax.swing.JLabel lblHoraEvento;
    private javax.swing.JLabel lblLugarEvento;
    private javax.swing.JLabel lblSubtituloEventos;
    private javax.swing.JLabel lblTituloEvento;
    private javax.swing.JLabel lblTituloEventos;
    private javax.swing.JLabel lblTituloPrincipalEvento;
    private javax.swing.JLabel lblTituloTablaEventos;
    private javax.swing.JPanel panelEncabezadoEventos;
    private javax.swing.JPanel panelFormularioEvento;
    private javax.swing.JPanel panelTablaEventos;
    private javax.swing.JScrollPane scrollDescripcionEvento;
    private javax.swing.JScrollPane scrollEventos;
    private javax.swing.JTable tablaEventos;
    private javax.swing.JTextArea txtDescripcionEvento;
    private javax.swing.JTextField txtDuracionEvento;
    private javax.swing.JTextField txtFechaEvento;
    private javax.swing.JTextField txtHoraEvento;
    private javax.swing.JTextField txtLugarEvento;
    private javax.swing.JTextField txtTituloEvento;
    // End of variables declaration//GEN-END:variables
}

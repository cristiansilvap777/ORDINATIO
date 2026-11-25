package vista;

import controlador.ControladorDIA;
import modelo.Recordatorio;
import modelo.Notificacion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Consulta tareas y eventos y permite programar notificaciones asociadas.
 * La pestaña de notificaciones hace visible la composición entre Recordatorio
 * y Notificacion: el recordatorio crea y administra sus propios avisos.
 */
public class PanelRecordatorios extends javax.swing.JPanel {

    private final ControladorDIA controlador;
    private final DefaultTableModel modeloTabla;
    private final ArrayList<Recordatorio> registrosMostrados = new ArrayList<>();

    // Componentes de la pestaña de notificaciones, construidos por código.
    private JComboBox<Recordatorio> cmbRecordatorios;
    private JTextField txtMensajeNotificacion;
    private JSpinner spnFechaNotificacion;
    private JTable tablaNotificaciones;
    private DefaultTableModel modeloNotificaciones;
    private final ArrayList<RegistroNotificacion> notificacionesMostradas = new ArrayList<>();

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelRecordatorios() { this(null); }

    public PanelRecordatorios(ControladorDIA controlador) {
        initComponents();
        this.controlador = controlador;
        modeloTabla = new DefaultTableModel(
                new Object[]{"Título", "Tipo", "Fecha", "Hora", "Estado"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tablaRecordatorios.setModel(modeloTabla);
        EstiloVisualDIA.configurarTabla(tablaRecordatorios, 4);
        tablaRecordatorios.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configurarPestanas();

        btnLimpiarBusqueda.addActionListener(e -> txtBuscarRecordatorio.setText(""));
        txtBuscarRecordatorio.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refrescarDatos(); }
            @Override public void removeUpdate(DocumentEvent e) { refrescarDatos(); }
            @Override public void changedUpdate(DocumentEvent e) { refrescarDatos(); }
        });
        refrescarDatos();
    }

    /** Organiza los registros, la gestión de avisos y la guía de uso. */
    private void configurarPestanas() {
        // Se reutiliza el JTabbedPane creado desde Design en NetBeans.
        JTabbedPane pestanas = jTabbedPane1;
        pestanas.removeAll();
        pestanas.addTab("Registros", scrollRecordatorios);
        pestanas.addTab("Notificaciones", crearPanelNotificaciones());

        JTextArea ayuda = new JTextArea(
                "Consulta las tareas y eventos asociados a tu perfil.\\n\\n"
                + "Utiliza el campo Buscar para filtrar por título o tipo.\\n"
                + "En Notificaciones puedes seleccionar una actividad, escribir un mensaje "
                + "y programar la fecha y hora del aviso.\\n\\n"
                + "Los datos se guardan en el perfil activo.");
        ayuda.setEditable(false);
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setBackground(new Color(243, 245, 248));
        ayuda.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pestanas.addTab("Ayuda", new JScrollPane(ayuda));

        panelTablaRecordatorios.removeAll();
        panelTablaRecordatorios.setLayout(new BorderLayout());
        panelTablaRecordatorios.add(pestanas, BorderLayout.CENTER);
    }

    /** Construye la interfaz de programación y consulta de notificaciones. */
    private JPanel crearPanelNotificaciones() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(new Color(243, 245, 248));
        principal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(new Color(233, 239, 247));
        formulario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Programar una notificación"));

        cmbRecordatorios = new JComboBox<>();
        cmbRecordatorios.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    javax.swing.JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Recordatorio) {
                    Recordatorio r = (Recordatorio) valor;
                    setText(r.getTipo() + " | " + r.getTitulo());
                }
                return this;
            }
        });

        txtMensajeNotificacion = new JTextField(22);
        Date inicial = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        spnFechaNotificacion = new JSpinner(new SpinnerDateModel(
                inicial, null, null, Calendar.MINUTE));
        spnFechaNotificacion.setEditor(new JSpinner.DateEditor(
                spnFechaNotificacion, "dd/MM/yyyy HH:mm"));

        JButton btnProgramar = new JButton("Programar notificación");
        btnProgramar.addActionListener(e -> programarNotificacion());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 7, 6, 7);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0; c.gridy = 0; formulario.add(new JLabel("Actividad"), c);
        c.gridx = 1; formulario.add(new JLabel("Mensaje"), c);
        c.gridx = 2; formulario.add(new JLabel("Fecha y hora"), c);
        c.gridx = 0; c.gridy = 1; c.weightx = 1.2; formulario.add(cmbRecordatorios, c);
        c.gridx = 1; c.weightx = 2; formulario.add(txtMensajeNotificacion, c);
        c.gridx = 2; c.weightx = 1.2; formulario.add(spnFechaNotificacion, c);
        c.gridx = 2; c.gridy = 2; c.weightx = 0; c.anchor = GridBagConstraints.EAST;
        formulario.add(btnProgramar, c);

        modeloNotificaciones = new DefaultTableModel(
                new Object[]{"Actividad", "Tipo", "Mensaje", "Programada para", "Estado"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tablaNotificaciones = new JTable(modeloNotificaciones);
        EstiloVisualDIA.configurarTabla(tablaNotificaciones, 4);
        tablaNotificaciones.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tablaNotificaciones);
        scroll.setPreferredSize(new Dimension(600, 260));

        JButton btnAtendida = new JButton("Marcar como atendida");
        JButton btnEliminar = new JButton("Eliminar notificación");
        btnAtendida.addActionListener(e -> marcarNotificacionAtendida());
        btnEliminar.addActionListener(e -> eliminarNotificacion());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        acciones.add(btnAtendida);
        acciones.add(btnEliminar);

        JPanel inferior = new JPanel(new BorderLayout(5, 5));
        inferior.setOpaque(false);
        inferior.add(scroll, BorderLayout.CENTER);
        inferior.add(acciones, BorderLayout.SOUTH);

        principal.add(formulario, BorderLayout.NORTH);
        principal.add(inferior, BorderLayout.CENTER);
        return principal;
    }

    /** Actualiza el listado de actividades que pueden tener notificaciones. */
    private void actualizarComboRecordatorios() {
        if (cmbRecordatorios == null) return;
        String idSeleccionado = null;
        Object actual = cmbRecordatorios.getSelectedItem();
        if (actual instanceof Recordatorio) idSeleccionado = ((Recordatorio) actual).getId();

        DefaultComboBoxModel<Recordatorio> modelo = new DefaultComboBoxModel<>();
        if (controlador != null && controlador.hayUsuario()) {
            for (Recordatorio r : controlador.getUsuario().getRecordatorios()) modelo.addElement(r);
        }
        cmbRecordatorios.setModel(modelo);
        if (idSeleccionado != null) {
            for (int i = 0; i < modelo.getSize(); i++) {
                if (modelo.getElementAt(i).getId().equals(idSeleccionado)) {
                    cmbRecordatorios.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /** Programa un aviso a través del recordatorio seleccionado. */
    private void programarNotificacion() {
        if (controlador == null || !controlador.hayUsuario()) {
            JOptionPane.showMessageDialog(this, "Inicia sesión para programar notificaciones.",
                    "Sesión requerida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Recordatorio seleccionado = (Recordatorio) cmbRecordatorios.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero registra una tarea o un evento.",
                    "Sin actividades", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String mensaje = txtMensajeNotificacion.getText().trim();
        if (mensaje.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe el mensaje de la notificación.",
                    "Dato obligatorio", JOptionPane.WARNING_MESSAGE);
            txtMensajeNotificacion.requestFocus();
            return;
        }
        Date fecha = (Date) spnFechaNotificacion.getValue();
        LocalDateTime fechaHora = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        try {
            // La instancia Notificacion se crea dentro de Recordatorio.
            seleccionado.programarNotificacion(mensaje, fechaHora);
            controlador.guardarDatos();
            txtMensajeNotificacion.setText("");
            refrescarTablaNotificaciones();
            JOptionPane.showMessageDialog(this, "La notificación se programó correctamente.",
                    "Notificación registrada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo programar",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "La notificación se creó, pero no fue posible guardar los datos: " + ex.getMessage(),
                    "Error de persistencia", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Vuelve a cargar la tabla de notificaciones del perfil activo. */
    private void refrescarTablaNotificaciones() {
        if (modeloNotificaciones == null) return;
        modeloNotificaciones.setRowCount(0);
        notificacionesMostradas.clear();
        if (controlador == null || !controlador.hayUsuario()) return;

        LocalDateTime ahora = LocalDateTime.now();
        for (Recordatorio r : controlador.getUsuario().getRecordatorios()) {
            for (Notificacion n : r.getNotificaciones()) {
                String estado = !n.isActiva() ? "Inactiva"
                        : (n.isMostrada() ? "Atendida"
                        : (n.estaPendiente(ahora) ? "Pendiente" : "Programada"));
                modeloNotificaciones.addRow(new Object[]{
                    r.getTitulo(), r.getTipo(), n.getMensaje(),
                    n.getFechaHoraProgramada().format(FORMATO_FECHA_HORA), estado
                });
                notificacionesMostradas.add(new RegistroNotificacion(r, n));
            }
        }
    }

    private void marcarNotificacionAtendida() {
        int fila = tablaNotificaciones.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una notificación de la tabla.");
            return;
        }
        RegistroNotificacion registro = notificacionesMostradas.get(
                tablaNotificaciones.convertRowIndexToModel(fila));
        registro.notificacion.mostrar();
        guardarCambiosNotificacion();
    }

    private void eliminarNotificacion() {
        int fila = tablaNotificaciones.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una notificación de la tabla.");
            return;
        }
        RegistroNotificacion registro = notificacionesMostradas.get(
                tablaNotificaciones.convertRowIndexToModel(fila));
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Deseas eliminar la notificación seleccionada?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            registro.recordatorio.eliminarNotificacion(registro.notificacion.getId());
            guardarCambiosNotificacion();
        }
    }

    private void guardarCambiosNotificacion() {
        try {
            controlador.guardarDatos();
            refrescarTablaNotificaciones();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No fue posible guardar los cambios: " + ex.getMessage(),
                    "Error de persistencia", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Vuelve a cargar los registros de tareas y eventos. */
    public void refrescarDatos() {
        modeloTabla.setRowCount(0);
        registrosMostrados.clear();
        actualizarComboRecordatorios();
        refrescarTablaNotificaciones();

        if (controlador == null || !controlador.hayUsuario()) return;

        String filtro = txtBuscarRecordatorio.getText().trim().toLowerCase();
        for (Recordatorio r : controlador.getUsuario().getRecordatorios()) {
            if (!filtro.isEmpty()
                    && !r.getTitulo().toLowerCase().contains(filtro)
                    && !r.getTipo().toLowerCase().contains(filtro)) continue;

            String estado = r.isCompletado() ? "Completado"
                    : (r.isActivo() ? "Pendiente" : "Inactivo");
            modeloTabla.addRow(new Object[]{r.getTitulo(), r.getTipo(),
                    r.getFechaHora().format(FORMATO_FECHA),
                    r.getFechaHora().format(FORMATO_HORA), estado});
            registrosMostrados.add(r);
        }
    }

    /** Relaciona la notificación seleccionada con su recordatorio propietario. */
    private static class RegistroNotificacion {
        private final Recordatorio recordatorio;
        private final Notificacion notificacion;
        private RegistroNotificacion(Recordatorio recordatorio, Notificacion notificacion) {
            this.recordatorio = recordatorio;
            this.notificacion = notificacion;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelEncabezadoRecordatorios = new javax.swing.JPanel();
        lblTituloRecordatorios = new javax.swing.JLabel();
        lblSubtituloRecordatorios = new javax.swing.JLabel();
        panelBusqueda = new javax.swing.JPanel();
        lblBuscarRecordatorio = new javax.swing.JLabel();
        txtBuscarRecordatorio = new javax.swing.JTextField();
        btnLimpiarBusqueda = new javax.swing.JButton();
        panelTablaRecordatorios = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        scrollRecordatorios = new javax.swing.JScrollPane();
        tablaRecordatorios = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        lblTituloNotificaciones = new javax.swing.JLabel();
        lblMensaje = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMensajeNotificacion2 = new javax.swing.JTextArea();
        lblFechaNotificacion = new javax.swing.JLabel();
        ftfFechaNotificacion = new javax.swing.JTextField();
        lblHoraNotificacion = new javax.swing.JLabel();
        ftfHoraNotificacion = new javax.swing.JTextField();
        btnProgramarNotificacion = new javax.swing.JButton();
        btnLimpiarNotificacion = new javax.swing.JButton();
        scrollNotificaciones = new javax.swing.JScrollPane();
        tablaNotificaciones2 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        lblTituloAyuda = new javax.swing.JLabel();
        lblDescripcionAyuda = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtInstruccionesAyuda = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(243, 245, 248));

        panelEncabezadoRecordatorios.setBackground(new java.awt.Color(243, 245, 248));

        lblTituloRecordatorios.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloRecordatorios.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloRecordatorios.setText("Recordatorios");

        lblSubtituloRecordatorios.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtituloRecordatorios.setText("Consulta tus tareas y eventos registrados.");

        javax.swing.GroupLayout panelEncabezadoRecordatoriosLayout = new javax.swing.GroupLayout(panelEncabezadoRecordatorios);
        panelEncabezadoRecordatorios.setLayout(panelEncabezadoRecordatoriosLayout);
        panelEncabezadoRecordatoriosLayout.setHorizontalGroup(
            panelEncabezadoRecordatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoRecordatoriosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEncabezadoRecordatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloRecordatorios)
                    .addComponent(lblSubtituloRecordatorios))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncabezadoRecordatoriosLayout.setVerticalGroup(
            panelEncabezadoRecordatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoRecordatoriosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloRecordatorios)
                .addGap(18, 18, 18)
                .addComponent(lblSubtituloRecordatorios)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelBusqueda.setBackground(new java.awt.Color(243, 245, 248));

        lblBuscarRecordatorio.setText("Buscar:");

        btnLimpiarBusqueda.setText("Limpiar");

        panelTablaRecordatorios.setBackground(new java.awt.Color(243, 245, 248));

        scrollRecordatorios.setForeground(new java.awt.Color(36, 52, 75));

        tablaRecordatorios.setForeground(new java.awt.Color(36, 52, 75));
        tablaRecordatorios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Título", "Tipo", "Fecha", "Hora", "Estado"
            }
        ));
        tablaRecordatorios.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollRecordatorios.setViewportView(tablaRecordatorios);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollRecordatorios, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollRecordatorios, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Registros", jPanel1);

        lblTituloNotificaciones.setText("Nueva notificación");

        lblMensaje.setText("Mensaje");

        txtMensajeNotificacion2.setColumns(20);
        txtMensajeNotificacion2.setRows(5);
        jScrollPane1.setViewportView(txtMensajeNotificacion2);

        lblFechaNotificacion.setText("Fecha");

        lblHoraNotificacion.setText("Hora");

        btnProgramarNotificacion.setText("Programar");

        btnLimpiarNotificacion.setText("Limpiar");
        btnLimpiarNotificacion.addActionListener(this::btnLimpiarNotificacionActionPerformed);

        tablaNotificaciones2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Mensaje", "Fecha", "Hora", "Estado"
            }
        ));
        scrollNotificaciones.setViewportView(tablaNotificaciones2);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblTituloNotificaciones)
                                .addComponent(lblMensaje)
                                .addComponent(lblFechaNotificacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(ftfFechaNotificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHoraNotificacion)
                            .addComponent(ftfHoraNotificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(btnProgramarNotificacion)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiarNotificacion)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addComponent(scrollNotificaciones, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloNotificaciones)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMensaje)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechaNotificacion)
                    .addComponent(lblHoraNotificacion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftfFechaNotificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ftfHoraNotificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnProgramarNotificacion)
                    .addComponent(btnLimpiarNotificacion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(scrollNotificaciones, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Notificaciones", jPanel2);

        lblTituloAyuda.setText("Cómo utilizar las notificaciones.");

        lblDescripcionAyuda.setText("Aprende a programar y administrar tus avisos.");

        txtInstruccionesAyuda.setEditable(false);
        txtInstruccionesAyuda.setBackground(new java.awt.Color(204, 204, 204));
        txtInstruccionesAyuda.setColumns(20);
        txtInstruccionesAyuda.setLineWrap(true);
        txtInstruccionesAyuda.setRows(5);
        txtInstruccionesAyuda.setText("GUÍA DE USO – NOTIFICACIONES DÍA\n\n1. Crear una notificación\nEscribe el mensaje que deseas recordar en el campo correspondiente. Procura utilizar una descripción clara y breve.\n\n2. Establecer fecha y hora\nSelecciona la fecha y la hora en que deseas recibir el aviso. Verifica que los datos sean correctos antes de continuar.\n\n3. Programar el aviso\nHaz clic en el botón \"Programar\" para registrar la notificación.\n\n4. Consultar notificaciones\nEn la tabla inferior podrás visualizar los avisos registrados, su fecha programada y su estado.\n\n5. Limpiar campos\nUtiliza el botón \"Limpiar\" para borrar la información ingresada y comenzar una nueva notificación.\n\nIMPORTANTE:\nLas notificaciones permiten recordar actividades y compromisos. Mantén actualizada la información para organizar mejor tu tiempo.");
        txtInstruccionesAyuda.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtInstruccionesAyuda);

        jButton1.setText("Cerrar ayuda");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTituloAyuda)
                            .addComponent(lblDescripcionAyuda)
                            .addComponent(jButton1))
                        .addGap(0, 233, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloAyuda)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDescripcionAyuda)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Ayuda", jPanel3);

        javax.swing.GroupLayout panelTablaRecordatoriosLayout = new javax.swing.GroupLayout(panelTablaRecordatorios);
        panelTablaRecordatorios.setLayout(panelTablaRecordatoriosLayout);
        panelTablaRecordatoriosLayout.setHorizontalGroup(
            panelTablaRecordatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTablaRecordatoriosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        panelTablaRecordatoriosLayout.setVerticalGroup(
            panelTablaRecordatoriosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        javax.swing.GroupLayout panelBusquedaLayout = new javax.swing.GroupLayout(panelBusqueda);
        panelBusqueda.setLayout(panelBusquedaLayout);
        panelBusquedaLayout.setHorizontalGroup(
            panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBuscarRecordatorio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtBuscarRecordatorio, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLimpiarBusqueda)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panelTablaRecordatorios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBusquedaLayout.setVerticalGroup(
            panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtBuscarRecordatorio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLimpiarBusqueda))
                    .addComponent(lblBuscarRecordatorio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelTablaRecordatorios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelEncabezadoRecordatorios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelBusqueda, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelEncabezadoRecordatorios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelBusqueda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarNotificacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarNotificacionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLimpiarNotificacionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLimpiarBusqueda;
    private javax.swing.JButton btnLimpiarNotificacion;
    private javax.swing.JButton btnProgramarNotificacion;
    private javax.swing.JTextField ftfFechaNotificacion;
    private javax.swing.JTextField ftfHoraNotificacion;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblBuscarRecordatorio;
    private javax.swing.JLabel lblDescripcionAyuda;
    private javax.swing.JLabel lblFechaNotificacion;
    private javax.swing.JLabel lblHoraNotificacion;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblSubtituloRecordatorios;
    private javax.swing.JLabel lblTituloAyuda;
    private javax.swing.JLabel lblTituloNotificaciones;
    private javax.swing.JLabel lblTituloRecordatorios;
    private javax.swing.JPanel panelBusqueda;
    private javax.swing.JPanel panelEncabezadoRecordatorios;
    private javax.swing.JPanel panelTablaRecordatorios;
    private javax.swing.JScrollPane scrollNotificaciones;
    private javax.swing.JScrollPane scrollRecordatorios;
    private javax.swing.JTable tablaNotificaciones2;
    private javax.swing.JTable tablaRecordatorios;
    private javax.swing.JTextField txtBuscarRecordatorio;
    private javax.swing.JTextArea txtInstruccionesAyuda;
    private javax.swing.JTextArea txtMensajeNotificacion2;
    // End of variables declaration//GEN-END:variables
}

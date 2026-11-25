package vista;

import controlador.ControladorDIA;
import modelo.Recordatorio;
import modelo.Notificacion;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.DefaultListModel;

/**
 * Vista de resumen general del usuario activo.
 * Los componentes visuales se construyen con el GUI Builder de NetBeans.
 */
public class PanelInicio extends javax.swing.JPanel {

    private final ControladorDIA controlador;
    private final DefaultListModel<String> modeloActividades = new DefaultListModel<>();

    /** Constructor utilizado por el diseñador de NetBeans. */
    public PanelInicio() {
        this(null);
    }

    /** Recibe el controlador compartido por la ventana principal. */
    public PanelInicio(ControladorDIA controlador) {
        initComponents();
        this.controlador = controlador;
        configurarDistribucionResponsive();
        listaActividades.setModel(modeloActividades);
        refrescarDatos();
    }

    /**
     * Distribuye el resumen usando el ancho disponible del contenedor.
     * Evita que las tarjetas queden agrupadas a la derecha cuando la ventana
     * principal tiene más espacio horizontal.
     */
    private void configurarDistribucionResponsive() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGap(12)
                .addGroup(layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelEncabezadoInicio,
                            0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelTareasPendientes, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(12)
                        .addComponent(panelTareasCompletadas, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelProximosEventos, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(12)
                        .addComponent(panelNotificaciones, 0,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(panelActividades,
                            0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12)
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(12)
                .addComponent(panelEncabezadoInicio,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12)
                .addGroup(layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTareasPendientes,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 82,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelTareasCompletadas,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 82,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10)
                .addGroup(layout.createParallelGroup(
                        javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelProximosEventos,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 82,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelNotificaciones,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 82,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12)
                .addComponent(panelActividades,
                        javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12)
        );
    }

    /**
     * Actualiza los indicadores y las próximas actividades del usuario.
     * No modifica los recordatorios almacenados.
     */
    public void refrescarDatos() {
        modeloActividades.clear();

        int pendientes = 0;
        int completadas = 0;
        int eventos = 0;
        int notificaciones = 0;

        if (controlador == null || !controlador.hayUsuario()) {
            lblTareasPendientes.setText("0");
            lblTareasCompletadas.setText("0");
            lblProximosEventos.setText("0");
            lblNotificacionesPendientes.setText("0");
            modeloActividades.addElement("Inicia sesión para consultar tus actividades.");
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        java.util.ArrayList<Recordatorio> registros =
                controlador.getUsuario().getRecordatorios();

        for (Recordatorio r : registros) {
            if ("Tarea".equals(r.getTipo())) {
                if (r.isCompletado()) {
                    completadas++;
                } else if (r.isActivo()) {
                    pendientes++;
                }
            }

            if ("Evento".equals(r.getTipo()) && r.isActivo()
                    && !r.isCompletado() && !r.getFechaHora().isBefore(ahora)) {
                eventos++;
            }

            // El resumen cuenta todas las notificaciones activas y no atendidas,
            // incluidas las que todavía están programadas para una fecha futura.
            // estaPendiente(ahora) solo es true cuando ya llegó la hora del aviso,
            // por eso no debe utilizarse para este contador general.
            for (Notificacion n : r.getNotificaciones()) {
                if (n.isActiva() && !n.isMostrada()) {
                    notificaciones++;
                }
            }
        }

        lblTareasPendientes.setText(String.valueOf(pendientes));
        lblTareasCompletadas.setText(String.valueOf(completadas));
        lblProximosEventos.setText(String.valueOf(eventos));
        lblNotificacionesPendientes.setText(String.valueOf(notificaciones));

        registros.sort(java.util.Comparator.comparing(Recordatorio::getFechaHora));
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int cantidad = 0;

        for (Recordatorio r : registros) {
            if (r.isActivo() && !r.isCompletado()
                    && !r.getFechaHora().isBefore(ahora)) {
                modeloActividades.addElement(
                        r.getTipo() + " | " + r.getTitulo()
                        + " | " + r.getFechaHora().format(formato));
                if (++cantidad == 5) {
                    break;
                }
            }
        }

        if (cantidad == 0) {
            modeloActividades.addElement("No tienes actividades próximas.");
        }
    }

    private void initComponents() {//GEN-BEGIN:initComponents

        panelEncabezadoInicio = new javax.swing.JPanel();
        lblTituloInicio = new javax.swing.JLabel();
        lblSubtituloInicio = new javax.swing.JLabel();
        panelTareasPendientes = new javax.swing.JPanel();
        lblTextoPendientes = new javax.swing.JLabel();
        lblTareasPendientes = new javax.swing.JLabel();
        panelTareasCompletadas = new javax.swing.JPanel();
        lblTextoCompletadas = new javax.swing.JLabel();
        lblTareasCompletadas = new javax.swing.JLabel();
        panelNotificaciones = new javax.swing.JPanel();
        lblTextoNotificaciones = new javax.swing.JLabel();
        lblNotificacionesPendientes = new javax.swing.JLabel();
        panelProximosEventos = new javax.swing.JPanel();
        lblTextoEventos = new javax.swing.JLabel();
        lblProximosEventos = new javax.swing.JLabel();
        panelActividades = new javax.swing.JPanel();
        lblTituloActividades = new javax.swing.JLabel();
        scrollActividades = new javax.swing.JScrollPane();
        listaActividades = new javax.swing.JList<>();

        setBackground(new java.awt.Color(243, 245, 248));
        setName("panelInicio"); // NOI18N

        panelEncabezadoInicio.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloInicio.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloInicio.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloInicio.setText("Resumen general");

        lblSubtituloInicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtituloInicio.setText("Consulta el estado de tus actividades.");

        javax.swing.GroupLayout panelEncabezadoInicioLayout = new javax.swing.GroupLayout(panelEncabezadoInicio);
        panelEncabezadoInicio.setLayout(panelEncabezadoInicioLayout);
        panelEncabezadoInicioLayout.setHorizontalGroup(
            panelEncabezadoInicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoInicioLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelEncabezadoInicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloInicio)
                    .addComponent(lblSubtituloInicio))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEncabezadoInicioLayout.setVerticalGroup(
            panelEncabezadoInicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEncabezadoInicioLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblTituloInicio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSubtituloInicio)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        panelTareasPendientes.setBackground(new java.awt.Color(233, 239, 247));
        panelTareasPendientes.setPreferredSize(new java.awt.Dimension(195, 70));

        lblTextoPendientes.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblTextoPendientes.setText("Pendientes");

        lblTareasPendientes.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblTareasPendientes.setForeground(new java.awt.Color(36, 52, 75));
        lblTareasPendientes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTareasPendientes.setText("0");

        javax.swing.GroupLayout panelTareasPendientesLayout = new javax.swing.GroupLayout(panelTareasPendientes);
        panelTareasPendientes.setLayout(panelTareasPendientesLayout);
        panelTareasPendientesLayout.setHorizontalGroup(
            panelTareasPendientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTareasPendientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoPendientes)
                .addContainerGap(136, Short.MAX_VALUE))
            .addComponent(lblTareasPendientes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelTareasPendientesLayout.setVerticalGroup(
            panelTareasPendientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTareasPendientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoPendientes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTareasPendientes)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelTareasCompletadas.setBackground(new java.awt.Color(233, 239, 247));
        panelTareasCompletadas.setPreferredSize(new java.awt.Dimension(195, 70));

        lblTextoCompletadas.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblTextoCompletadas.setText("Completadas");

        lblTareasCompletadas.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblTareasCompletadas.setForeground(new java.awt.Color(36, 52, 75));
        lblTareasCompletadas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTareasCompletadas.setText("0");

        javax.swing.GroupLayout panelTareasCompletadasLayout = new javax.swing.GroupLayout(panelTareasCompletadas);
        panelTareasCompletadas.setLayout(panelTareasCompletadasLayout);
        panelTareasCompletadasLayout.setHorizontalGroup(
            panelTareasCompletadasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTareasCompletadasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoCompletadas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(lblTareasCompletadas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelTareasCompletadasLayout.setVerticalGroup(
            panelTareasCompletadasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTareasCompletadasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoCompletadas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTareasCompletadas)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        panelNotificaciones.setBackground(new java.awt.Color(233, 239, 247));
        panelNotificaciones.setPreferredSize(new java.awt.Dimension(195, 70));

        lblTextoNotificaciones.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblTextoNotificaciones.setText("Notificaciones");

        lblNotificacionesPendientes.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblNotificacionesPendientes.setForeground(new java.awt.Color(36, 52, 75));
        lblNotificacionesPendientes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNotificacionesPendientes.setText("0");

        javax.swing.GroupLayout panelNotificacionesLayout = new javax.swing.GroupLayout(panelNotificaciones);
        panelNotificaciones.setLayout(panelNotificacionesLayout);
        panelNotificacionesLayout.setHorizontalGroup(
            panelNotificacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNotificacionesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoNotificaciones)
                .addContainerGap(124, Short.MAX_VALUE))
            .addComponent(lblNotificacionesPendientes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelNotificacionesLayout.setVerticalGroup(
            panelNotificacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNotificacionesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoNotificaciones)
                .addGap(18, 18, 18)
                .addComponent(lblNotificacionesPendientes)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelProximosEventos.setBackground(new java.awt.Color(233, 239, 247));
        panelProximosEventos.setPreferredSize(new java.awt.Dimension(195, 70));

        lblTextoEventos.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblTextoEventos.setText("Próximos eventos");

        lblProximosEventos.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblProximosEventos.setForeground(new java.awt.Color(36, 52, 75));
        lblProximosEventos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblProximosEventos.setText("0");

        javax.swing.GroupLayout panelProximosEventosLayout = new javax.swing.GroupLayout(panelProximosEventos);
        panelProximosEventos.setLayout(panelProximosEventosLayout);
        panelProximosEventosLayout.setHorizontalGroup(
            panelProximosEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProximosEventosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoEventos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(lblProximosEventos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelProximosEventosLayout.setVerticalGroup(
            panelProximosEventosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProximosEventosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTextoEventos)
                .addGap(18, 18, 18)
                .addComponent(lblProximosEventos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelActividades.setBackground(new java.awt.Color(233, 239, 247));

        lblTituloActividades.setFont(new java.awt.Font("Segoe UI", 1, 23)); // NOI18N
        lblTituloActividades.setForeground(new java.awt.Color(36, 52, 75));
        lblTituloActividades.setText("Próximas actividades");

        scrollActividades.setViewportView(listaActividades);

        javax.swing.GroupLayout panelActividadesLayout = new javax.swing.GroupLayout(panelActividades);
        panelActividades.setLayout(panelActividadesLayout);
        panelActividadesLayout.setHorizontalGroup(
            panelActividadesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActividadesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelActividadesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelActividadesLayout.createSequentialGroup()
                        .addComponent(lblTituloActividades)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrollActividades, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelActividadesLayout.setVerticalGroup(
            panelActividadesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActividadesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloActividades)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(scrollActividades, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelEncabezadoInicio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(panelTareasPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(panelProximosEventos, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(panelNotificaciones, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                            .addComponent(panelTareasCompletadas, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)))
                    .addComponent(panelActividades, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelEncabezadoInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelTareasPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTareasCompletadas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelProximosEventos, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                    .addComponent(panelNotificaciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelActividades, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblNotificacionesPendientes;
    private javax.swing.JLabel lblProximosEventos;
    private javax.swing.JLabel lblSubtituloInicio;
    private javax.swing.JLabel lblTareasCompletadas;
    private javax.swing.JLabel lblTareasPendientes;
    private javax.swing.JLabel lblTextoCompletadas;
    private javax.swing.JLabel lblTextoEventos;
    private javax.swing.JLabel lblTextoNotificaciones;
    private javax.swing.JLabel lblTextoPendientes;
    private javax.swing.JLabel lblTituloActividades;
    private javax.swing.JLabel lblTituloInicio;
    private javax.swing.JList<String> listaActividades;
    private javax.swing.JPanel panelActividades;
    private javax.swing.JPanel panelEncabezadoInicio;
    private javax.swing.JPanel panelNotificaciones;
    private javax.swing.JPanel panelProximosEventos;
    private javax.swing.JPanel panelTareasCompletadas;
    private javax.swing.JPanel panelTareasPendientes;
    private javax.swing.JScrollPane scrollActividades;
    // End of variables declaration//GEN-END:variables
}

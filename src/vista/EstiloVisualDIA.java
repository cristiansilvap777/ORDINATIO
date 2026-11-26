package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Centraliza los criterios visuales compartidos por las tablas de ORDINATIO.
 * Utiliza una paleta suave para reducir el contraste excesivo y reserva
 * pequeños acentos cromáticos para comunicar estados y categorías.
 */
public final class EstiloVisualDIA {

    public static final Color AZUL_MARINO = new Color(36, 52, 75);
    public static final Color FONDO_GENERAL = new Color(243, 245, 248);
    public static final Color FONDO_PANEL = new Color(233, 239, 247);
    public static final Color AZUL_ACENTO = new Color(37, 99, 235);
    public static final Color SELECCION = new Color(219, 234, 254);
    public static final Color LINEA = new Color(226, 232, 240);

    private EstiloVisualDIA() {
        // Evita crear instancias de una clase que solo ofrece utilidades.
    }

    /** Aplica tipografía, selección y espaciado coherentes a una tabla. */
    public static void configurarTabla(JTable tabla, int columnaEstado) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(30);
        tabla.setFillsViewportHeight(true);
        tabla.setShowGrid(true);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(LINEA);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 1));
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(AZUL_MARINO);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(AZUL_MARINO);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        encabezado.setBackground(new Color(226, 232, 240));
        encabezado.setForeground(AZUL_MARINO);
        encabezado.setReorderingAllowed(false);

        if (columnaEstado >= 0 && columnaEstado < tabla.getColumnCount()) {
            tabla.getColumnModel().getColumn(columnaEstado)
                    .setCellRenderer(new RenderizadorEstado());
        }
    }

    /** Renderiza los estados con tonos suaves y texto legible. */
    public static class RenderizadorEstado extends DefaultTableCellRenderer {

        public RenderizadorEstado() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabla, Object valor, boolean seleccionado,
                boolean tieneFoco, int fila, int columna) {
            super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);

            String estado = valor == null ? "" : valor.toString();
            setText(estado);
            setToolTipText("Estado: " + estado);

            if (seleccionado) {
                setBackground(SELECCION);
                setForeground(AZUL_MARINO);
            } else if (estado.toLowerCase().contains("complet")) {
                setBackground(new Color(226, 243, 234));
                setForeground(new Color(35, 105, 68));
            } else if (estado.toLowerCase().contains("pendiente")) {
                setBackground(new Color(255, 246, 220));
                setForeground(new Color(125, 86, 20));
            } else {
                setBackground(new Color(238, 241, 245));
                setForeground(new Color(85, 96, 110));
            }
            return this;
        }
    }

    /** Muestra el color real de cada categoría y conserva el código como ayuda. */
    public static class RenderizadorColor extends DefaultTableCellRenderer {

        public RenderizadorColor() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabla, Object valor, boolean seleccionado,
                boolean tieneFoco, int fila, int columna) {
            super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);

            String hexadecimal = valor == null ? "" : valor.toString();
            try {
                Color color = Color.decode(hexadecimal);
                setBackground(color);
                setForeground(color.getRed() * 0.299
                        + color.getGreen() * 0.587
                        + color.getBlue() * 0.114 > 150
                        ? AZUL_MARINO : Color.WHITE);
                setText(hexadecimal.toUpperCase());
                setToolTipText("Color de identificación: " + hexadecimal);
            } catch (NumberFormatException ex) {
                setBackground(Color.WHITE);
                setForeground(AZUL_MARINO);
                setText(hexadecimal);
                setToolTipText("Código de color no disponible");
            }
            return this;
        }
    }
}

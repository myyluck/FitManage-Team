package fitmanage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * Ikon vektor yang digambar langsung dengan Graphics2D (tanpa file gambar & tanpa emoji),
 * jadi selalu tajam dan warnanya bisa diganti.
 */
public final class UiIcons {

    private UiIcons() { }

    public enum Type { BRAND, USER, CALENDAR, DUMBBELL, COIN }

    public static void paint(Graphics2D g, Type type, double x, double y, double s, Color warna) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(warna);
        g2.translate(x, y);

        switch (type) {
            case BRAND: {
                g2.translate(s * 0.12, 0);
                g2.transform(AffineTransform.getShearInstance(-0.18, 0));
                double r = s * 0.08;
                g2.fill(new RoundRectangle2D.Double(s * 0.14, s * 0.05, s * 0.22, s * 0.90, r, r));
                g2.fill(new RoundRectangle2D.Double(s * 0.14, s * 0.05, s * 0.74, s * 0.22, r, r));
                g2.fill(new RoundRectangle2D.Double(s * 0.14, s * 0.42, s * 0.52, s * 0.20, r, r));
                break;
            }
            case USER: {
                g2.fill(new Ellipse2D.Double(s * 0.29, s * 0.04, s * 0.42, s * 0.42));
                g2.fillArc((int) Math.round(s * 0.08), (int) Math.round(s * 0.54),
                        (int) Math.round(s * 0.84), (int) Math.round(s * 0.84), 0, 180);
                break;
            }
            case CALENDAR: {
                g2.setStroke(new BasicStroke((float) Math.max(1.6, s * 0.085),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new RoundRectangle2D.Double(s * 0.10, s * 0.16, s * 0.80, s * 0.76, s * 0.20, s * 0.20));
                g2.draw(new Line2D.Double(s * 0.10, s * 0.40, s * 0.90, s * 0.40));
                g2.draw(new Line2D.Double(s * 0.32, s * 0.06, s * 0.32, s * 0.26));
                g2.draw(new Line2D.Double(s * 0.68, s * 0.06, s * 0.68, s * 0.26));
                for (int r = 0; r < 2; r++) {
                    for (int c = 0; c < 3; c++) {
                        g2.fill(new Rectangle2D.Double(s * (0.24 + c * 0.22), s * (0.53 + r * 0.18), s * 0.12, s * 0.10));
                    }
                }
                break;
            }
            case DUMBBELL: {
                g2.fill(new Rectangle2D.Double(s * 0.22, s * 0.43, s * 0.56, s * 0.14));
                g2.fill(new RoundRectangle2D.Double(s * 0.12, s * 0.22, s * 0.14, s * 0.56, s * 0.06, s * 0.06));
                g2.fill(new RoundRectangle2D.Double(s * 0.74, s * 0.22, s * 0.14, s * 0.56, s * 0.06, s * 0.06));
                g2.fill(new RoundRectangle2D.Double(s * 0.01, s * 0.36, s * 0.09, s * 0.28, s * 0.05, s * 0.05));
                g2.fill(new RoundRectangle2D.Double(s * 0.90, s * 0.36, s * 0.09, s * 0.28, s * 0.05, s * 0.05));
                break;
            }
            case COIN: {
                for (int i = 0; i < 3; i++) {
                    g2.fill(new RoundRectangle2D.Double(s * 0.04, s * (0.50 - i * 0.17), s * 0.62, s * 0.20, s * 0.20, s * 0.20));
                }
                g2.fill(new Ellipse2D.Double(s * 0.50, s * 0.50, s * 0.48, s * 0.48));
                g2.setColor(Theme.CARD);
                Font f = Theme.font(Font.BOLD, (float) (s * 0.32));
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (int) Math.round(s * 0.74 - fm.stringWidth("$") / 2.0);
                int ty = (int) Math.round(s * 0.74 + (fm.getAscent() - fm.getDescent()) / 2.0);
                g2.drawString("$", tx, ty);
                break;
            }
            default:
                break;
        }
        g2.dispose();
    }

    /** Versi javax.swing.Icon (bisa dipasang di JLabel/JButton). */
    public static Icon icon(final Type type, final int size, final Color warna) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                paint((Graphics2D) g, type, x, y, size, warna);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
}

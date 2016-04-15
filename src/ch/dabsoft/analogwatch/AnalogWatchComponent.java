package ch.dabsoft.analogwatch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

public class AnalogWatchComponent extends JComponent {

    private static final long serialVersionUID = 1L;

    private Preferences prefs = Preferences.userNodeForPackage(AnalogWatch.class);
    private AnalogWatch frame;
    
    private boolean pressed = false;
    private int x, y;
    
    private Calendar cal = Calendar.getInstance();
    
    private BasicStroke stroke1;
    private BasicStroke stroke3;
    private BasicStroke stroke5;

    private double[] xarr = new double[360];
    private double[] yarr = new double[360];
    
    private Color background = new Color(137, 194, 228);
    private Color ticks = Color.BLACK;
    private Color hourHand = Color.BLACK;
    private Color minuteHand = Color.BLACK;
    private Color secondHand = Color.DARK_GRAY;
    private Color text = Color.DARK_GRAY;
    
    private int cp;
    
    private int size;
    private int radius;
    
    private int top;
    private int left;

    private int ticklen;
    private int hourlen;
    private int minutelen;
    private int secondlen;
    
    private int cx;
    private int cy;
    
    public boolean dayOfMonth = false;
    public boolean dayOfWeek = false;
    private DateFormat dayOfWeekFmt = new SimpleDateFormat("EE");
    private Font font;
    
    public AnalogWatchComponent(AnalogWatch frame) {
        setOpaque(false);
        this.frame = frame;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AnalogWatchComponent.this.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                AnalogWatchComponent.this.mouseReleased(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                AnalogWatchComponent.this.mouseDragged(e);
            }
        });
        for (int i = 0; i < 360; i++) {
            double rad = (Math.PI - i * Math.PI / 180) - Math.PI / 2;
            xarr[i] = Math.cos(rad);
            yarr[i] = Math.sin(rad);
        }
        recalculate();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculate();
            }
        });
    }
    
    public void recalculate() {
        size = getWidth() - 3;
        
        radius = size / 2;
        
        cp = size / 100;
        
        top = getY() + 1;
        left = getX() + 1;

        cx = left + radius;
        cy = top + radius;

        ticklen = 5 * cp;
        hourlen = radius / 2;
        minutelen = radius - 10 * cp;
        secondlen = radius - 10 * cp;

        int s = Math.max(1, cp / 2);
        stroke1 = new BasicStroke(s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        stroke3 = new BasicStroke(3 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        stroke5 = new BasicStroke(5 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        
        font = new Font(Font.SANS_SERIF, Font.PLAIN, cp * 10);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        
        drawBackgroundCircle(g2);
        drawMinuteTicks(g2);
        drawHourTicks(g2);
        drawDayOfMonth(g2, cal.get(Calendar.DAY_OF_MONTH));
        drawDayOfWeek(g2, dayOfWeekFmt.format(cal.getTime()));
        drawHourHand(g2, hour, minute);
        drawMinuteHand(g2, minute);
        drawSecondHand(g2, second);
    }

    private void drawBackgroundCircle(Graphics2D g) {
        g.setColor(background);
        g.fillOval(left, top, size, size);
        g.setColor(ticks);
        g.setStroke(stroke3);
        g.drawOval(left, top, size, size);
    }

    private void drawMinuteTicks(Graphics2D g) {
        g.setColor(ticks);
        g.setStroke(stroke1);
        for (int min = 0; min < 60; min++) {
            int x1 = (int) (radius * xarr[min * 6]);
            int y1 = (int) (radius * yarr[min * 6]);
            int x2 = (int) ((radius - ticklen) * xarr[min * 6]);
            int y2 = (int) ((radius - ticklen) * yarr[min * 6]);
            g.drawLine(cx + x1, cy - y1, cx + x2, cy - y2);
        }
    }

    private void drawHourTicks(Graphics2D g) {
        g.setColor(ticks);
        g.setStroke(stroke3);
        for (int hr = 0; hr < 12; hr++) {
            int x1 = (int) (radius * xarr[hr * 30]);
            int y1 = (int) (radius * yarr[hr * 30]);
            int x2 = (int) ((radius - ticklen) * xarr[hr * 30]);
            int y2 = (int) ((radius - ticklen) * yarr[hr * 30]);
            g.drawLine(cx + x1, cy - y1, cx + x2, cy - y2);
        }
    }

    private void drawDayOfMonth(Graphics2D g, int day) {
        if (!dayOfMonth) return;
        g.setColor(text);
        g.setFont(font);
        g.setStroke(stroke1);
        String s = Integer.toString(day);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, cy + radius - ticklen - fm.getDescent() - 3 * cp);
    }

    private void drawDayOfWeek(Graphics2D g, String day) {
        if (!dayOfWeek) return;
        g.setColor(text);
        g.setFont(font);
        g.setStroke(stroke1);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(day, cx - fm.stringWidth(day) / 2, cy - radius + ticklen + fm.getAscent() + 3 * cp);
    }

    private void drawHourHand(Graphics2D g, int hour, int minute) {
        g.setColor(hourHand);
        g.setStroke(stroke5);
        int hdeg = hour * 30 + minute / 2;
        if (hdeg >= 360) hdeg -= 360;
        int hx = (int) (hourlen * xarr[hdeg]);
        int hy = (int) (hourlen * yarr[hdeg]);
        g.drawLine(cx, cy, cx + hx, cy - hy);
    }

    private void drawMinuteHand(Graphics2D g, int minute) {
        g.setColor(minuteHand);
        g.setStroke(stroke3);
        int mdeg = minute * 6;
        if (mdeg >= 360) mdeg -= 360;
        int mx = (int) (minutelen * xarr[mdeg]);
        int my = (int) (minutelen * yarr[mdeg]);
        g.drawLine(cx, cy, cx + mx, cy - my);
    }

    private void drawSecondHand(Graphics2D g, int second) {
        g.setColor(secondHand);
        g.setStroke(stroke1);
        int sdeg = second * 6;
        if (sdeg >= 360) sdeg -= 360;
        int sx = (int) (secondlen * xarr[sdeg]);
        int sy = (int) (secondlen * yarr[sdeg]);
        g.drawLine(cx, cy, cx + sx, cy - sy);
    }

    private void mouseDragged(MouseEvent e) {
        if (pressed) {
            int newx = e.getXOnScreen();
            int newy = e.getYOnScreen();
            int fx = frame.getX();
            int fy = frame.getY();
            frame.setLocation(fx + newx - x, fy + newy - y);
            x = newx;
            y = newy;
        }
    }

    private void mousePressed(MouseEvent e) {
        pressed = true;
        x = e.getXOnScreen();
        y = e.getYOnScreen();
        if (e.isPopupTrigger()) {
            showSettings(e);
        }
    }

    private void mouseReleased(MouseEvent e) {
        pressed = false;
        prefs.putInt("x", frame.getX());
        prefs.putInt("y", frame.getY());
        if (e.isPopupTrigger()) {
            showSettings(e);
        }
    }

    private void showSettings(MouseEvent e) {
        // TODO show settings menu
        System.out.println("settings");
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 27) {
            frame.exitApp();
        }
        int size;
        switch (e.getKeyCode()) {
        case 107:
        case 49:
            size = frame.getWidth();
            size += 20;
            if (size > 2048) size = 2048;
            frame.setSize(size, size);
            prefs.putInt("size", frame.getWidth());
            break;
            
        case 109:
        case 45:
            size = frame.getWidth();
            size -= 20;
            if (size < 100) size = 100;
            frame.setSize(size, size);
            prefs.putInt("size", frame.getWidth());
            break;
            
        case 96:
        case 48:
            frame.setSize(400, 400);
            frame.setLocationRelativeTo(null);
            prefs.putInt("size", 400);
            prefs.remove("x");
            prefs.remove("y");
            break;
            
        case 68: // D - toggle day of month
            dayOfMonth = !dayOfMonth;
            prefs.putBoolean("day_of_month", dayOfMonth);
            repaint();
            break;
            
        case 87: // W - toggle week day
            dayOfWeek = !dayOfWeek;
            prefs.putBoolean("day_of_week", dayOfWeek);
            repaint();
            break;

        default:
            System.out.println(e.getKeyCode());
            break;
        }
    }
    
}

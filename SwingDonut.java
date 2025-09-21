import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SwingDonut extends JPanel {
    private double rotX = 0;
    private double rotY = 0;
    private double rotZ = 0;

    public SwingDonut() {
        Timer t = new Timer(16, e -> {
            rotX += 0.04;  // rotation speed
            rotY += 0.03;
            rotZ += 0.02;
            repaint();
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        double R = 1.5;  // big radius
        double r = 0.6;  // tube radius
        double scale = Math.min(w, h) / 4.0;
        List<Particle> particles = new ArrayList<>();

        // 🔹 Higher resolution torus sampling
        for (double theta = 0; theta < 2*Math.PI; theta += 0.08) {
            for (double phi = 0; phi < 2*Math.PI; phi += 0.08) {
                double cosT = Math.cos(theta), sinT = Math.sin(theta);
                double cosP = Math.cos(phi), sinP = Math.sin(phi);

                // Torus parametric eq.
                double x = (R + r * cosT) * cosP;
                double y = (R + r * cosT) * sinP;
                double z = r * sinT;

                // Rotate point
                double[] p = rotateXYZ(x, y, z, rotX, rotY, rotZ);

                particles.add(new Particle(p[0], p[1], p[2], theta, phi));
            }
        }

        // Light direction
        double lx = 0, ly = 1, lz = -1;
        double lenL = Math.sqrt(lx*lx + ly*ly + lz*lz);
        lx/=lenL; ly/=lenL; lz/=lenL;

        // 🔹 Sort by depth
        particles.sort((a,b) -> Double.compare(b.z, a.z));

        for (Particle p : particles) {
            // Normal vector of torus surface
            double nx = Math.cos(p.theta) * Math.cos(p.phi);
            double ny = Math.cos(p.theta) * Math.sin(p.phi);
            double nz = Math.sin(p.theta);

            // Rotate normal same as point
            double[] nrot = rotateXYZ(nx, ny, nz, rotX, rotY, rotZ);
            double ndot = nrot[0]*lx + nrot[1]*ly + nrot[2]*lz;
            ndot = Math.max(0, ndot);

            // 🔹 Perspective projection
            double perspective = 3.0 / (5.0 + p.z); // closer = bigger
            int sx = (int)(w/2 + p.x * scale * perspective);
            int sy = (int)(h/2 + p.y * scale * perspective);

            // Point size changes with perspective
            int size = (int)(4 * perspective + 1);

            // Color shading
            float brightness = (float)(0.2 + 0.8 * ndot);
            Color base = Color.getHSBColor(0.08f, 0.8f, brightness); // golden donut
            g.setColor(base);
            g.fillOval(sx - size/2, sy - size/2, size, size);
        }
    }

    private static double[] rotateXYZ(double x, double y, double z, double ax, double ay, double az) {
        // Rotate X
        double cosa = Math.cos(ax), sina = Math.sin(ax);
        double y1 = y * cosa - z * sina;
        double z1 = y * sina + z * cosa;

        // Rotate Y
        double cosb = Math.cos(ay), sinb = Math.sin(ay);
        double x2 = x * cosb + z1 * sinb;
        double z2 = -x * sinb + z1 * cosb;

        // Rotate Z
        double cosc = Math.cos(az), sinc = Math.sin(az);
        double x3 = x2 * cosc - y1 * sinc;
        double y3 = x2 * sinc + y1 * cosc;

        return new double[]{x3, y3, z2};
    }

    static class Particle {
        double x,y,z,theta,phi;
        Particle(double x,double y,double z,double theta,double phi){
            this.x=x;this.y=y;this.z=z;this.theta=theta;this.phi=phi;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("🍩 3D Spinning Donut");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(800, 600);
            f.setLocationRelativeTo(null);
            f.add(new SwingDonut());
            f.setVisible(true);
        });
    }
}
// This code was difficult to make but I managed to do this.
// This project is also unregistered trademark of Anshuman Jadiya 

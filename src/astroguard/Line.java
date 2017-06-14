package astroguard;

import java.awt.*;

public class Line {
    int x1, y1;
    int x2, y2;

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean intersects(Circle circle) {
        int x1 = this.x1 - circle.x;
        int y1 = this.y1 - circle.y;
        int x2 = this.x2 - circle.x;
        int y2 = this.y2 - circle.y;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dr = Math.sqrt(dx*dx + dy*dy);
        double D = x1*y2 - x2*y1;

        return circle.radius * circle.radius * dr * dr - D * D >= 0;
    }

    public float getAngle() {
        return (float)Math.atan2((y1 - y2), (x1 - x2));
    }

    public void draw(Graphics g) {
        g.drawLine(x1, y1, x2, y2);
    }
}

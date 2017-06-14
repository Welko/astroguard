package astroguard;

import java.awt.*;

public class Circle {
    int x;
    int y;
    int radius;

    public Circle(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public boolean intersects(Circle that) {
        int dx = this.x - that.x;
        int dy = this.y - that.y;

        float distance = (float)Math.sqrt(dx*dx + dy*dy);

        return distance < this.radius + that.radius;
    }

    public void draw(Graphics g) {
        int radius2 = radius << 1;
        g.drawOval(x - radius, y - radius, radius2, radius2);
    }
}

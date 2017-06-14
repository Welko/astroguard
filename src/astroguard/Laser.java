package astroguard;

import java.awt.*;

public class Laser {
    private Line line;
    private int lifetime;
    private int timeLeft;

    public Laser(Line line, int lifetime) {
        this.line = line;
        this.lifetime = lifetime;
        this.timeLeft = lifetime;
    }

    public void update(float ms) {
        timeLeft -= ms;
    }

    public void draw(Graphics g) {
        if (!isDead()) {
            Color c = g.getColor();
            int alpha = (int) (( (float)timeLeft / lifetime ) * 255);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));

            line.draw(g);
            g.setColor(c);
        }
    }

    public boolean isDead() {
        return timeLeft <= 0;
    }

    public boolean intersects(Enemy enemy) {
        return line.intersects(enemy.body);
    }
}

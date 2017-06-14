package astroguard;

import java.awt.*;

public class Enemy {
    Circle body;
    float speed;
    float x;
    float y;

    static final float PLAY_SOUND_START = 500;
    float playSound = 0;

    public Enemy(float x, float y, int radius, float speed) {
        this.body = new Circle((int)x, (int)y, radius);
        this.speed = speed;
        this.x = x;
        this.y = y;
    }

    public void update(int ms) {
        y += speed;
        playSound -= ms;
        body.y = (int)y;
    }

    public void draw(Graphics g) {
        body.draw(g);
    }
}

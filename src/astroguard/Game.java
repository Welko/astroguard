package astroguard;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class Game extends JPanel {

    private static final int WIDTH  = 500;
    private static final int WIDTH_HALF = WIDTH >> 1;

    private static final int HEIGHT = 500;
    private static final int HEIGHT_HALF = HEIGHT >> 1;

    private static final int PLAYER_Y = HEIGHT - 50;
    private static final int PLAYER_X_MIN = 50;
    private static final int PLAYER_X_MAX = WIDTH - 50;
    private static final int PLAYER_RADIUS = 25;

    private static final int EARTH_LENGTH = PLAYER_X_MAX - PLAYER_X_MIN;
    private static final int EARTH_HEIGHT = HEIGHT - PLAYER_Y;

    private static final int ENEMY_Y = 50;
    private static final int ENEMY_RADIUS = PLAYER_RADIUS;
    private static final float ENEMY_SPEED_START = 0.1f;
    private static final float ENEMY_SPEED_INCREMENT = 0.05f;
    private static final int ENEMY_FIELD_HEIGHT = PLAYER_Y - ENEMY_Y;
    private static final String STRING_ENEMIES_KILLED = "Enemies destroyed: ";
    private static final int STRING_ENEMIES_KILLED_POSITION = HEIGHT - 10;

    private static final int LASER_DURATION = 500;

    private static final int LIVES_LEFT_START = 3;
    private static final String STRING_LIVES_LEFT = "Lives left: ";
    private static final int STRING_LIVES_LEFT_POSITION = HEIGHT - 25;

    private static final String RESOURCE_LASER = "laser.wav";
    private static final String RESOURCE_EXPLOSION = "explosion.wav";
    private static final String RESOURCE_BEEP = "beep.wav";
    private static final String RESOURCE_DAMAGE = "damage.wav";

    private static final String STRING_GAME_PAUSED = "Game is paused. Press %s to start/resume";
    private static final String STRING_GAME_OVER = "Game is over. You destroyed %s enemies";

    private static final int KEYCODE_START_OR_PAUSE = KeyEvent.VK_SPACE;
    private static final String STRING_KEY_START_OR_PAUSE = "SPACEBAR";

    public static void main(String[] args) throws Exception {
        Game game = new Game();
        game.run();
    }

    public Game() {
        JFrame frame = new JFrame("Astroguard");

        this.setFocusable(true);

        Dimension size = new Dimension(WIDTH, HEIGHT);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.setPreferredSize(size);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                onKeyReleased(e);
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(this);
        frame.pack();
        frame.setVisible(true);
    }

    private Clip beepSound;
    private Clip explosionSound;
    private Clip laserSound;
    private Clip damageSound;

    public void init() throws Exception {
        laserSound = AudioSystem.getClip();
        explosionSound = AudioSystem.getClip();
        beepSound = AudioSystem.getClip();
        damageSound = AudioSystem.getClip();

        AudioInputStream audioInputStream;

        audioInputStream = AudioSystem.getAudioInputStream(
                Game.class.getResourceAsStream("../resources/" + RESOURCE_LASER)
        );
        laserSound.open(audioInputStream);

        audioInputStream = AudioSystem.getAudioInputStream(
                Game.class.getResourceAsStream("../resources/" + RESOURCE_EXPLOSION)
        );
        explosionSound.open(audioInputStream);

        audioInputStream = AudioSystem.getAudioInputStream(
                Game.class.getResourceAsStream("../resources/" + RESOURCE_BEEP)
        );
        beepSound.open(audioInputStream);

        audioInputStream = AudioSystem.getAudioInputStream(
                Game.class.getResourceAsStream("../resources/" + RESOURCE_DAMAGE)
        );
        damageSound.open(audioInputStream);
    }

    private enum State {
        PAUSED, PLAYING, OVER
    }

    private float enemySpeed = ENEMY_SPEED_START;
    private int livesLeft = LIVES_LEFT_START;
    private int enemiesKilled = 0;
    private State state = State.PAUSED;

    public void run() throws Exception {
        init();
        new Timer(5, e -> {
            if (state == State.PLAYING) {
                update(5);
            }
            repaint();
        }).start();
        spawnRandom();
    }

    private Circle player = new Circle(WIDTH_HALF, PLAYER_Y, PLAYER_RADIUS);
    private Line earth = new Line(PLAYER_X_MIN, PLAYER_Y, PLAYER_X_MAX, PLAYER_Y);
    private HashSet<Enemy> enemies = new HashSet<>();
    private LinkedList<Laser> lasers = new LinkedList<>();

    private void onMouseMoved(MouseEvent e) {
        player.x = e.getX() < PLAYER_X_MIN
                ? PLAYER_X_MIN
                : e.getX() > PLAYER_X_MAX
                    ? PLAYER_X_MAX
                    : e.getX();
    }

    private void onKeyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            switch (state) {
                case PLAYING:
                    state = State.PAUSED;
                    break;
                case PAUSED:
                    state = State.PLAYING;
                    break;
            }
        }
    }

    private void onMouseClicked(MouseEvent e) {
        playSound(laserSound, 0, 1);
        Laser laser = new Laser(new Line(player.x, player.y, player.x, 0), LASER_DURATION);
        lasers.addLast(laser);
        Iterator<Enemy> ite = enemies.iterator();
        while (ite.hasNext()) {
            Enemy enemy = ite.next();
            if (laser.intersects(enemy)) {
                ite.remove();
                playSound(explosionSound, getSoundPos(enemy.body.x), getSoundVol(enemy.body.y));
                enemiesKilled++;
                waitAndSpawnRandom(2000);
            }
        }
    }

    private void playSound(Clip clip, float pos, float vol) {
        if (clip == beepSound) {
            FloatControl position = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            if (pos >= position.getMinimum() && pos <= position.getMaximum()) {
                position.setValue(pos);
            }

            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(vol * 6.0206f);
        }

        if (clip.isActive()) {
            //clip.stop();
            clip.setMicrosecondPosition(0);
            //clip.start();
        } else {
            new Thread(() -> {
                clip.start();
                clip.setMicrosecondPosition(0);
            }).start();
        }
    }

    private void spawnRandom() {
        int x = (int) (Math.random() * (WIDTH - (PLAYER_X_MIN << 1)) + PLAYER_X_MIN);
        Enemy enemy = new Enemy(x, ENEMY_Y, ENEMY_RADIUS, enemySpeed);
        enemies.add(enemy);
    }

    private void waitAndSpawnRandom(int ms) {
        Timer t = new Timer(ms, event -> {
            spawnRandom();
        });
        t.setRepeats(false);
        t.start();
        enemySpeed += ENEMY_SPEED_INCREMENT;
    }

    private float getSoundPos(int x) {
        return (float)(x - player.x) / EARTH_LENGTH;
    }

    private float getSoundVol(int y) {
        float vol = (float) (y - ENEMY_Y) / ENEMY_FIELD_HEIGHT;
        return vol < 0 ? 0 : vol;
    }

    private void update(int ms) {
        Iterator<Enemy> ite = enemies.iterator();
        while (ite.hasNext()) {
            Enemy enemy = ite.next();
            enemy.update(ms);
            if (earth.intersects(enemy.body)) {
                playSound(damageSound, 0, 1);
                if (--livesLeft < 0) {
                    state = State.OVER;
                    return;
                }
                waitAndSpawnRandom(2000);
                ite.remove();
            }
            if (enemy.playSound <= 0) {
                enemy.playSound = Enemy.PLAY_SOUND_START;
                playSound(beepSound, getSoundPos(enemy.body.x), getSoundVol(enemy.body.y));
            }
        }

        while (!lasers.isEmpty() && lasers.getFirst().isDead()) {
            lasers.removeFirst();
        }
        if (!lasers.isEmpty()) {
            for (Laser laser : lasers) {
                laser.update(ms);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {
            case PLAYING:
                g.setColor(new Color(0, 150, 0));
                earth.draw(g);

                g.setColor(Color.BLUE);
                player.draw(g);

                g.setColor(Color.BLACK);
                for (Enemy enemy : enemies) {
                    enemy.draw(g);
                }

                g.setColor(Color.RED);
                for (Laser laser : lasers) {
                    laser.draw(g);
                }

                g.setColor(Color.BLACK);
                g.drawString(STRING_LIVES_LEFT + livesLeft, 10, STRING_LIVES_LEFT_POSITION);
                g.drawString(STRING_ENEMIES_KILLED + enemiesKilled, 10, STRING_ENEMIES_KILLED_POSITION);
                break;

            case PAUSED:
                g.drawString(String.format(STRING_GAME_PAUSED, STRING_KEY_START_OR_PAUSE), 50, 50);
                break;

            case OVER:
                g.drawString(String.format(STRING_GAME_OVER, enemiesKilled), 50, 50);
        }
    }
}

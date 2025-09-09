import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SpikeGame extends JPanel implements ActionListener, KeyListener {

    // Screen
    private static final int WIDTH = 800, HEIGHT = 600;
    private static final int TILE_SIZE = 40;

    private Timer timer;
    private BufferedImage stoneImg, spikeImg;
    private BufferedImage[][] animations; // animations[state][frame]
    private int[][] grid;

    // Player
    private int x = 100, y = 300;
    private int velocityX = 5;
    private int yVelocity = 0;
    private boolean jumping = false;
    private boolean facingRight = true;

    private int health = 3;
    private boolean alive = true;
    private boolean dying = false;
    private boolean attacking = false;
    private int damageCooldown = 0;
    private int hurtTimer = 0;

    // Animation control
    private String currentState = "idle";
    private int stateIndex = 0; // which row in animations
    private int frameIndex = 0;
    private int frameDelay = 8;
    private int frameTimer = 0;

    private String[] states = {"idle", "run", "jump", "attack", "hurt", "death"};

    public SpikeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(50, 50, 50));

        loadImages();
        loadLevel();

        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    private void loadImages() {
        try {
            stoneImg = ImageIO.read(new File("stone brick.png"));
            spikeImg = ImageIO.read(new File("spike.png"));

            animations = new BufferedImage[states.length][];

            animations[0] = new BufferedImage[]{
                ImageIO.read(new File("idle (1).png")),
                ImageIO.read(new File("idle (2).png")),
                ImageIO.read(new File("idle (3).png")),
                ImageIO.read(new File("idle (4).png"))
            };

            animations[1] = new BufferedImage[]{
                ImageIO.read(new File("run (1).png")),
                ImageIO.read(new File("run (2).png")),
                ImageIO.read(new File("run (3).png")),
                ImageIO.read(new File("run (4).png")),
                ImageIO.read(new File("run (5).png")),
                ImageIO.read(new File("run (6).png"))
            };

            animations[2] = new BufferedImage[]{
                ImageIO.read(new File("run (3).png"))
            };

            animations[3] = new BufferedImage[]{
                ImageIO.read(new File("attack1 (1).png")),
                ImageIO.read(new File("attack1 (2).png")),
                ImageIO.read(new File("attack1 (3).png")),
                ImageIO.read(new File("attack1 (4).png"))
            };

            animations[4] = new BufferedImage[]{
                ImageIO.read(new File("hurt (1).png")),
                ImageIO.read(new File("hurt (2).png"))
            };

            animations[5] = new BufferedImage[]{
                ImageIO.read(new File("death1.png")),
                ImageIO.read(new File("death2.png")),
                ImageIO.read(new File("death3.png")),
                ImageIO.read(new File("death4.png"))
            };

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLevel() {
        // simple grid (like your JSON)
        grid = new int[15][20];
        for (int col = 0; col < 20; col++) {
            grid[14][col] = 1; // stone floor
        }
        grid[13][5] = 2; // spike
        grid[13][10] = 2; // spike
    }

    private int getStateIndex(String state) {
        for (int i = 0; i < states.length; i++) {
            if (states[i].equals(state)) return i;
        }
        return 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw grid
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == 1) {
                    g.drawImage(stoneImg, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                } else if (grid[row][col] == 2) {
                    g.drawImage(spikeImg, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }

        // Current frame
        BufferedImage frame = animations[stateIndex][frameIndex];
        if (!facingRight) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(frame, x + frame.getWidth(), y, -frame.getWidth(), frame.getHeight(), null);
        } else {
            g.drawImage(frame, x, y, null);
        }

        // Health bar
        if (!dying) {
            g.setColor(Color.RED);
            for (int i = 0; i < health; i++) {
                g.fillRect(10 + i * 35, 10, 30, 30);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!alive) {
            currentState = "death";
            stateIndex = getStateIndex(currentState);
        }

        // Update animation
        frameTimer++;
        if (frameTimer >= frameDelay) {
            frameTimer = 0;
            frameIndex++;
            if (frameIndex >= animations[stateIndex].length) {
                if (currentState.equals("attack")) {
                    attacking = false;
                    currentState = "idle";
                    stateIndex = getStateIndex(currentState);
                } else if (currentState.equals("death")) {
                    frameIndex = animations[stateIndex].length - 1;
                } else {
                    frameIndex = 0;
                }
            }
        }

        // Apply gravity
        y += yVelocity;
        if (jumping) {
            currentState = "jump";
            stateIndex = getStateIndex("jump");
        }
        yVelocity += 1;

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!alive) return;

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            x -= velocityX;
            currentState = "run";
            stateIndex = getStateIndex("run");
            facingRight = false;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            x += velocityX;
            currentState = "run";
            stateIndex = getStateIndex("run");
            facingRight = true;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (!jumping) {
                jumping = true;
                yVelocity = -15;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!attacking) {
                attacking = true;
                currentState = "attack";
                stateIndex = getStateIndex("attack");
                frameIndex = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!alive) return;

        if (!attacking) {
            currentState = "idle";
            stateIndex = getStateIndex("idle");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Character + Level + Spikes");
        SpikeGame game = new SpikeGame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setVisible(true);
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class SpikeGame extends JPanel implements ActionListener, KeyListener {
    // Screen
    final int WIDTH = 800, HEIGHT = 600, TILE_SIZE = 40;
    Timer timer = new Timer(16, this); // ~60 FPS
    
    // Images
    BufferedImage stone, spike;
    ArrayList<BufferedImage> idleFrames = new ArrayList<>();
    ArrayList<BufferedImage> runFrames = new ArrayList<>();
    ArrayList<BufferedImage> jumpFrames = new ArrayList<>();
    ArrayList<BufferedImage> attackFrames = new ArrayList<>();
    ArrayList<BufferedImage> hurtFrames = new ArrayList<>();
    ArrayList<BufferedImage> deathFrames = new ArrayList<>();
    
    // Player
    int x = 100, y = 400;
    int vx = 0, vy = 0;
    boolean jumping = false;
    boolean facingRight = true;
    
    // State
    String state = "idle";
    int frameIndex = 0, frameDelay = 0;
    
    // Health
    int health = 3;
    
    public SpikeGame() {
        loadImages();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(50, 50, 50));
        timer.start();
        addKeyListener(this);
        setFocusable(true);
    }
    
    void loadImages() {
        try {
            stone = ImageIO.read(new File("resources/stone brick.png"));
            spike = ImageIO.read(new File("resources/spike.png"));
            
            for (int i = 1; i <= 4; i++)
                idleFrames.add(ImageIO.read(new File("resources/idle (" + i + ").png")));
            
            for (int i = 1; i <= 6; i++)
                runFrames.add(ImageIO.read(new File("resources/run (" + i + ").png")));
            
            jumpFrames.add(ImageIO.read(new File("resources/run (3).png")));
            
            for (int i = 1; i <= 4; i++)
                attackFrames.add(ImageIO.read(new File("resources/attack1 (" + i + ").png")));
            
            for (int i = 1; i <= 2; i++)
                hurtFrames.add(ImageIO.read(new File("resources/hurt (" + i + ").png")));
            
            for (int i = 1; i <= 4; i++)
                deathFrames.add(ImageIO.read(new File("resources/death" + i + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        // Physics
        y += vy;
        vy += 1; // gravity
        if (y > HEIGHT - 100) {
            y = HEIGHT - 100;
            vy = 0;
            jumping = false;
        }
        
        x += vx;
        
        // Animation
        frameDelay++;
        if (frameDelay > 8) {
            frameDelay = 0;
            frameIndex++;
        }
        
        repaint();
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw ground
        for (int i = 0; i < WIDTH / TILE_SIZE; i++) {
            g.drawImage(stone, i * TILE_SIZE, HEIGHT - TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }
        
        // Draw spikes
        g.drawImage(spike, 300, HEIGHT - TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        g.drawImage(spike, 500, HEIGHT - TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        
        // Select current frame
        BufferedImage frame = idleFrames.get(frameIndex % idleFrames.size());
        if (state.equals("run")) frame = runFrames.get(frameIndex % runFrames.size());
        if (state.equals("jump")) frame = jumpFrames.get(0);
        if (state.equals("attack")) frame = attackFrames.get(frameIndex % attackFrames.size());
        
        if (!facingRight) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(frame, x + frame.getWidth(), y, -frame.getWidth(), frame.getHeight(), null);
        } else {
            g.drawImage(frame, x, y, null);
        }
        
        // Draw health
        g.setColor(Color.RED);
        for (int i = 0; i < health; i++) {
            g.fillRect(10 + i * 35, 10, 30, 30);
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) { vx = -5; state = "run"; facingRight = false; }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) { vx = 5; state = "run"; facingRight = true; }
        if (e.getKeyCode() == KeyEvent.VK_UP && !jumping) { vy = -15; jumping = true; state = "jump"; }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) { state = "attack"; frameIndex = 0; }
    }
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            vx = 0;
            state = "idle";
        }
    }
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        JFrame window = new JFrame("Character + Level + Spikes");
        SpikeGame game = new SpikeGame();
        window.add(game);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}

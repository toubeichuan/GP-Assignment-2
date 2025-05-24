import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

public class Stickman_Badminton extends GameEngine {
    public static void main(String[] args) {
        createGame(new Stickman_Badminton());
    }

    // --------------------------------------------------------------------
    // Game State & Scoring
    // --------------------------------------------------------------------
    enum GameState { Menu, Option, PlayTwo }
    private GameState gameState = GameState.Menu;

    private int menuOption = 0;
    private int scoreLeft  = 0;
    private int scoreRight = 0;
    private boolean nextServerLeft = true;  // left first server

    private final boolean HitBoxVisualiaztion = false; // Hit box visualization

    // --------------------------------------------------------------------
    // Game Objects
    // --------------------------------------------------------------------
    private Image menu;
    private Image background;
    private Player leftPlayer, rightPlayer;
    private Birdie birdie;

    // --------------------------------------------------------------------
    // Constants
    // --------------------------------------------------------------------
    private static final int CANVAS_WIDTH  = 800;
    private static final int CANVAS_HEIGHT = 500;
    private static final double BIRDIE_BOUNCE_SPEED = 600;

    // --------------------------------------------------------------------
    // Audio
    // --------------------------------------------------------------------
    AudioClip bgMusic;       // 背景音乐
    AudioClip serveSfx;      // 发球音效
    AudioClip smashSfx;      // 击球音效
    AudioClip suddenTurnSfx; // 发球权切换提示
    AudioClip clearSfx;      // 清场音效
    AudioClip winningSfx;    // 胜利音效

    @Override
    public void init() {
        setupWindow(CANVAS_WIDTH, CANVAS_HEIGHT);
        setWindowSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        background = loadImage("img/background.png");
        menu = loadImage("img/menu.png");
        leftPlayer  = new Player(this, "img/left-player",  200 - 75, 350, +1);
        rightPlayer = new Player(this, "img/right-player", 600 - 75, 350, -1);
        leftPlayer.init();
        rightPlayer.init();

        birdie = new Birdie(this, "img/ball.png");

        // Audio
        bgMusic         = loadAudio("Audio/audio_background.wav");
        serveSfx        = loadAudio("Audio/audio_serve.wav");
        smashSfx        = loadAudio("Audio/audio_smash.wav");
        suddenTurnSfx   = loadAudio("Audio/audio_sudden-turn.wav");
        clearSfx        = loadAudio("Audio/audio_clear.wav");
        winningSfx      = loadAudio("Audio/audio_winning.wav");

        // startAudioLoop(bgMusic, 100);
    }

    @Override
    public void update(double dt) {

        // 1) 更新玩家（处理移动、挥拍、发球、跳跃）
        leftPlayer.update(dt);
        rightPlayer.update(dt);

        // 2) 如果球当前不在飞行中且发球动画完成，则发球
        if (!birdie.isInPlay()) {
            if (nextServerLeft && leftPlayer.consumeServeFinished()) {
                serveFrom(leftPlayer);
                // SuddenTurnSfx
                playAudio(suddenTurnSfx);
            }
            if (!nextServerLeft && rightPlayer.consumeServeFinished()) {
                serveFrom(rightPlayer);
                // SuddenTurnSfx
                playAudio(suddenTurnSfx);
            }
        }

        // 3) 碰撞前记录状态，用于落地判分
        boolean wasInPlay = birdie.isInPlay();

        // 4) 更新羽毛球物理（重力、边界反弹、落地）
        birdie.update(dt);

        // 5) 如果仍在飞行，检测挥拍命中并反弹
        if (birdie.isInPlay()) {
            if (leftPlayer.tryHit(birdie)) {
                // 1) 读取挥拍角度（degrees → radians）
                double angleRad = Math.toRadians(leftPlayer.getSwingAngle());
                // 2) 加一点随机加速度 (这里最多再+30%)
                double randomFactor = 1 + rand(0.5);
                double bounceSpeed = BIRDIE_BOUNCE_SPEED * randomFactor;
                // 3) 按角度给出 vx, vy （0° 对应正上方）
                birdie.vx = Math.sin(angleRad) * bounceSpeed;
                birdie.vy = -Math.cos(angleRad) * bounceSpeed;

                // smashSfx
                playAudio(smashSfx);
            }
            else if (rightPlayer.tryHit(birdie)) {
                double angleRad = Math.toRadians(rightPlayer.getSwingAngle());
                double randomFactor = 1 + Math.random() * 0.3;
                double bounceSpeed = BIRDIE_BOUNCE_SPEED * randomFactor;
                birdie.vx = - Math.sin(angleRad) * bounceSpeed;
                birdie.vy = -Math.cos(angleRad) * bounceSpeed;

                // smashSfx
                playAudio(smashSfx);
            }
        }


        // 6) 从飞行到不飞行：落地判分并切换发球权
        if (wasInPlay && !birdie.isInPlay()) {
            if (birdie.x < CANVAS_WIDTH / 2) {
                scoreRight++;
                nextServerLeft = false;
            } else {
                scoreLeft++;
                nextServerLeft = true;
            }

            leftPlayer.resetPosition();
            rightPlayer.resetPosition();

            leftPlayer.setSwingBounds();
            rightPlayer.setSwingBounds();
        }
    }

    /** 发球，从玩家中心（水平）+头顶以下若干像素发出 */
    private void serveFrom(Player p) {
        double startX = p.x + 75;
        double startY = p.y + 75;
        rightPlayer.setRestrictedBounds();
        leftPlayer.setRestrictedBounds();
        birdie.serve(startX, startY, p.getServeDir());

        // serveSfx
        playAudio(serveSfx);
    }

    @Override
    public void paintComponent() {
        clearBackground(CANVAS_WIDTH, CANVAS_HEIGHT);

        if (gameState == GameState.Menu) {
            drawMenu();
        }else if (gameState == GameState.PlayTwo) {
            drawPlayTwo();
        }

    }
    private void drawPlayTwo() {
        drawImage(background, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // 绘制玩家和羽毛球
        leftPlayer.draw();
        rightPlayer.draw();
        birdie.draw();

        if (HitBoxVisualiaztion){
            drawHitBox();
        }

        // 绘制分数
        changeColor(white);
        drawText(20, 30,                 "Left: "  + scoreLeft,  "Arial", 24);
        drawText(CANVAS_WIDTH - 140, 30, "Right: " + scoreRight, "Arial", 24);
    }


    private void drawMenu() {
        drawImage(menu, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        String[] options = { "练习模式", "双人模式", "退出" };
        changeColor(Color.WHITE);
        for (int i = 0; i < options.length; i++) {
            if (i == menuOption) changeColor(Color.YELLOW);
            else                 changeColor(Color.LIGHT_GRAY);
            drawText( CANVAS_WIDTH/2 - 80,
                    200 + i * 60,
                    options[i],
                    "Arial", 36);
        }

    }

    private void drawHitBox(){
        Rectangle2D box = leftPlayer.getRacketHitBox();
        changeColor(255, 0, 0);
        drawRectangle((int)box.getX(),
                (int)box.getY(),
                (int)box.getWidth(),
                (int)box.getHeight());
        box = rightPlayer.getRacketHitBox();
        drawRectangle((int)box.getX(),
                (int)box.getY(),
                (int)box.getWidth(),
                (int)box.getHeight());
        box = birdie.getHitBox();
        drawRectangle((int)box.getX(),
                (int)box.getY(),
                (int)box.getWidth(),
                (int)box.getHeight());
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        //-------------------------------------------------------
        // In Menu
        //-------------------------------------------------------
        if (gameState == GameState.Menu) {
            // 菜单上下选择
            if (k == KeyEvent.VK_UP)   menuOption = (menuOption + 2) % 3;
            if (k == KeyEvent.VK_DOWN) menuOption = (menuOption + 1) % 3;
            if (k == KeyEvent.VK_ENTER) {
                if (menuOption == 0) {
                    // TODO
                    // 练习模式
                } else if (menuOption == 1) {
                    gameState = GameState.PlayTwo;
                } else {                       // 退出
                    System.exit(0);
                }
            }
            return;
        }

        //-------------------------------------------------------
        // In Game
        //-------------------------------------------------------

        // ---- Left control ----
        if      (k == KeyEvent.VK_D)     leftPlayer.setAction(Player.Action.Forward);
        else if (k == KeyEvent.VK_A)     leftPlayer.setAction(Player.Action.Backward);
        else if (k == KeyEvent.VK_W)     leftPlayer.jump();
        else if (k == KeyEvent.VK_S) {
            if (!birdie.isInPlay() && nextServerLeft) leftPlayer.setAction(Player.Action.Serving);
            else                                       leftPlayer.setAction(Player.Action.Swing);
        }
        // ---- Right control ----
        if      (k == KeyEvent.VK_RIGHT) rightPlayer.setAction(Player.Action.Forward);
        else if (k == KeyEvent.VK_LEFT)  rightPlayer.setAction(Player.Action.Backward);
        else if (k == KeyEvent.VK_UP)    rightPlayer.jump();
        else if (k == KeyEvent.VK_DOWN) {
            if (!birdie.isInPlay() && !nextServerLeft) rightPlayer.setAction(Player.Action.Serving);
            else                                       rightPlayer.setAction(Player.Action.Swing);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_A) leftPlayer.setAction(Player.Action.Standing);
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_LEFT) rightPlayer.setAction(Player.Action.Standing);
    }
}

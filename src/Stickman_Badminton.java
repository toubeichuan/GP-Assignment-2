import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

public class Stickman_Badminton extends GameEngine {
    // --------------------------------------------------------------------
    // Constants
    // --------------------------------------------------------------------
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 500;
    private static final double BIRDIE_BOUNCE_SPEED = 600;

    // --------------------------------------------------------------------
    // Audio
    // --------------------------------------------------------------------
    AudioClip bgMusic;
    AudioClip serveSfx;
    AudioClip smashSfx;
    AudioClip suddenTurnSfx;
    AudioClip clearSfx;
    AudioClip winningSfx;

    // --------------------------------------------------------------------
    // Hit Box Visualization
    // --------------------------------------------------------------------
    private final boolean HitBoxVisualization = false;

    // --------------------------------------------------------------------
    // Game Objects
    // --------------------------------------------------------------------
    private Image menu;
    private Image background;
    private Image inst1;
    private Image inst2;
    private Image scoreBoard;
    private Player leftPlayer, rightPlayer;
    private Birdie birdie;
    private GameState gameState = GameState.Menu;
    private int menuOption = 0;
    private int endOption = 0;
    private int scoreLeft = 0;
    private int scoreRight = 0;
    private double instTimer = 0;
    private boolean nextServerLeft = true;  // left first server
    private boolean wasInPlay;
    private static final int WIN_SCORE = 21;
    private boolean restart = false;
    private boolean gameOver = false;
    private boolean enterFlag = false;
    public static void main(String[] args) {
        createGame(new Stickman_Badminton());
    }

    @Override
    public void init() {
        setupWindow(CANVAS_WIDTH, CANVAS_HEIGHT);
        setWindowSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        // Basic image
        background = loadImage("img/background.png");
        menu = loadImage("img/menu.png");
        inst1 = loadImage("img/inst1.png");
        inst2 = loadImage("img/inst2.png");
        scoreBoard = loadImage("img/scoreboard.png");

        // Player
        leftPlayer = new Player(this, "img/left-player", 200 - 75, 350, +1);
        rightPlayer = new Player(this, "img/right-player", 600 - 75, 350, -1);
        leftPlayer.init();
        rightPlayer.init();

        // Birdie
        birdie = new Birdie(this, "img/ball.png");

        // Audio
        bgMusic = loadAudio("Audio/audio_background.WAV");
        serveSfx = loadAudio("Audio/audio_serve.wav");
        smashSfx = loadAudio("Audio/audio_smash.wav");
        suddenTurnSfx = loadAudio("Audio/audio_sudden-turn.wav");
        clearSfx = loadAudio("Audio/audio_clear.wav");
        winningSfx = loadAudio("Audio/audio_winning.wav");

        startAudioLoop(bgMusic);
    }

    @Override
    public void update(double dt) {
        if (restart){
            restartMatch();
            restartScore();
            restart = false;
        }

        switch (gameState) {
            case Inst:
                instTimer += dt;
                if (instTimer >= 1.5) {
                    if(menuOption == 0) {
                        gameState = GameState.PlayOne;
                    }else if (menuOption == 1) {
                        gameState = GameState.PlayTwo;
                    }
                }
                return;
            case Menu:
                return;
            case PlayOne:
                // ——— Update the left side ———
                leftPlayer.update(dt);

                // ——— Update the robot ai on the right ———
                rightPlayer.update(dt);
                // If the ball is in flight, let the robot track the x of the ball
                if (birdie.isInPlay() && birdie.x > 400) {
                    double targetX = birdie.x - 75 - 10; // 机器人 x 对齐球心
                    double targetY = birdie.y;
                    if (Math.abs(targetX - rightPlayer.x) > 10) {
                        if (birdie.vx > 0 || birdie.x > 600)
                        {
                            if (targetX > rightPlayer.x) {
                                rightPlayer.setAction(Player.Action.Forward);
                            } else {
                                rightPlayer.setAction(Player.Action.Backward);
                            }
                        }
                    } else {
                        rightPlayer.setAction(Player.Action.Standing);
                        // If the ball is within the hitting range, swing the racket
                        if (Math.abs(targetY - rightPlayer.y) < 40){
                            rightPlayer.setAction(Player.Action.Swing);
                        }
                    }
                } else {
                    // If the ball is not flying, it remains standing
                    rightPlayer.setAction(Player.Action.Standing);
                }
                

                // ——— Automatic serve ————
                if (!birdie.isInPlay()) {
                    if (nextServerLeft && leftPlayer.consumeServeFinished()) {
                        serveFrom(leftPlayer);
                        playAudio(suddenTurnSfx);
                    }
                    if (!nextServerLeft) {
                        rightPlayer.setAction(Player.Action.Serving);
                        if (rightPlayer.consumeServeFinished()){
                            serveFrom(rightPlayer);
                            playAudio(suddenTurnSfx);
                        }
                    }
                }

                // 3) Record the status before the collision
                wasInPlay = birdie.isInPlay();
                // System.out.println(wasInPlay);
                // 4) Update badminton physics
                birdie.update(dt);

                // 5) Detect the hit and rebound of the swing
                if (birdie.isInPlay()) {
                    if (leftPlayer.tryHit(birdie)) {
                        // 1) Read the swing Angle（degrees → radians）
                        double angleRad = Math.toRadians(leftPlayer.getRacketAngle());
                        // 2) Add random acceleration (up to 30% more here)
                        double randomFactor = 1 + rand(0.5);
                        double bounceSpeed = BIRDIE_BOUNCE_SPEED * randomFactor;
                        // 3) Give vx and vy by Angle (0° corresponds directly above)
                        birdie.vx = Math.sin(angleRad) * bounceSpeed;
                        birdie.vy = -Math.cos(angleRad / 2) * bounceSpeed;

                        // smashSfx
                        playAudio(smashSfx);
                    } else if (rightPlayer.tryHit(birdie)) {
                        double angleRad = Math.toRadians(rightPlayer.getRacketAngle());
                        double randomFactor = 1 + Math.random() * 0.3;
                        double bounceSpeed = BIRDIE_BOUNCE_SPEED * randomFactor;
                        birdie.vx = -Math.sin(angleRad) * bounceSpeed;
                        birdie.vy = -Math.cos(angleRad / 2) * bounceSpeed;

                        // smashSfx
                        playAudio(smashSfx);
                    }
                }

                // 6) From flying to not flying: The point is awarded upon landing and the serve is switched
                if (wasInPlay && !birdie.isInPlay()) {
                    if (birdie.x < CANVAS_WIDTH / 2) {
                        scoreRight++;
                        nextServerLeft = false;
                    } else {
                        scoreLeft++;
                        nextServerLeft = true;
                    }
                    if (scoreLeft >= WIN_SCORE || scoreRight >= WIN_SCORE) {
                        endMatchOne();;
                        return;
                    }
                    restartMatch();
                }
                break;

            case PlayTwo:
                // 1) Update players (handle movement, swing, serve, jump)
                leftPlayer.update(dt);
                rightPlayer.update(dt);

                // 2) If the ball is not in flight at present and the serve animation is completed, the serve is made
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

                // 3) Record the status before the collision for landing scoring
                wasInPlay = birdie.isInPlay();

                // 4) Update badminton physics (gravity, boundary rebound, landing)
                birdie.update(dt);

                // 5) If still in flight, detect that the swing hits and bounces
                if (birdie.isInPlay()) {
                    if (leftPlayer.tryHit(birdie)) {
                        // 1) Read the swing Angle（degrees → radians）
                        double angleRad = Math.toRadians(leftPlayer.getRacketAngle());
                        // 2) Add random acceleration (up to 30% more here)
                        double randomFactor = 1 + rand(0.5);
                        double bounceSpeed = BIRDIE_BOUNCE_SPEED * randomFactor;
                        // 3) Give vx and vy by Angle (0° corresponds directly above)
                        birdie.vx = Math.sin(angleRad) * bounceSpeed;
                        birdie.vy = -Math.cos(angleRad / 2 ) * bounceSpeed;

                        // smashSfx
                        playAudio(smashSfx);
                    } else if (rightPlayer.tryHit(birdie)) {
                        double angleRad = Math.toRadians(rightPlayer.getRacketAngle());
                        double randomFactor = 1 + Math.random() * 0.3;
                        double bounceSpeed = BIRDIE_BOUNCE_SPEED * randomFactor;
                        birdie.vx = -Math.sin(angleRad) * bounceSpeed;
                        birdie.vy = -Math.cos(angleRad / 2) * bounceSpeed;

                        // smashSfx
                        playAudio(smashSfx);
                    }
                }

                // 6) From flying to not flying: The point is awarded upon landing and the serve is switched
                if (wasInPlay && !birdie.isInPlay()) {
                    if (birdie.x < CANVAS_WIDTH / 2) {
                        scoreRight++;
                        nextServerLeft = false;
                    } else {
                        scoreLeft++;
                        nextServerLeft = true;
                    }

                    if (scoreLeft >= WIN_SCORE || scoreRight >= WIN_SCORE) {
                        endMatchTwo();
                        return;
                    }

                    restartMatch();
                }
                break;
        }
        // System.out.println(gameState);
    }

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

        switch (gameState) {
            case Menu:
                drawMenu();
                break;
            case PlayOne:
                drawPlayOne();
                break;
            case PlayTwo:
                drawPlayTwo();
                break;
            case Inst:
                drawInst();
                break;
            case PlayOneEnd:
                drawEndOne();
                break;
            case PlayTwoEnd:
                drawEndTwo();
                break;
            default:
                break;
        }
    }

    private void drawPlayOne() {
        drawImage(background, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        drawScore( scoreLeft, scoreRight);
        // Draw the player and the badminton
        leftPlayer.draw();
        rightPlayer.draw();
        birdie.draw();

        if (HitBoxVisualization) {
            drawHitBox();
        }
    }

    private void drawPlayTwo() {
        drawImage(background, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        drawScore( scoreLeft, scoreRight);
        // Draw the player and the badminton
        leftPlayer.draw();
        rightPlayer.draw();
        birdie.draw();

        if (HitBoxVisualization) {
            drawHitBox();
        }
    }

    private void drawMenu() {
        drawImage(menu, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        String[] options = {"One-player Mode", "Two-player mode", "Quit"};
        changeColor(Color.WHITE);
        for (int i = 0; i < options.length; i++) {
            if (i == menuOption) changeColor(Color.YELLOW);
            else changeColor(Color.WHITE);
            drawText(CANVAS_WIDTH / 2 + 130,
                    350 + i * 40,
                    options[i],
                    "Arial", 24);
        }

    }

    private void drawInst() {
        if (menuOption == 0) drawImage(inst1, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        if (menuOption == 1) drawImage(inst2, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void drawHitBox() {
        Rectangle2D box = leftPlayer.getRacketHitBox();
        changeColor(255, 0, 0);
        drawRectangle((int) box.getX(),
                (int) box.getY(),
                (int) box.getWidth(),
                (int) box.getHeight());
        box = rightPlayer.getRacketHitBox();
        drawRectangle((int) box.getX(),
                (int) box.getY(),
                (int) box.getWidth(),
                (int) box.getHeight());
        box = birdie.getHitBox();
        drawRectangle((int) box.getX(),
                (int) box.getY(),
                (int) box.getWidth(),
                (int) box.getHeight());
    }

    private void drawScore(int leftScore, int rightScore) {
        changeColor(black);
        drawSolidRectangle( 350 , 50 , 100 , 48 );
        changeColor(red);
        drawText(369, 82,  " "+ leftScore+ " - "+ rightScore, "Arial", 24);
    }

    private void drawEndOne() {
        drawPlayOne();
        String[] options = {"Restart", "Back to menu",  "Quit"};
        for (int i = 0; i < options.length; i++) {
            if (i == endOption) changeColor(Color.YELLOW);
            else changeColor(Color.WHITE);
            drawText(CANVAS_WIDTH / 2 - 100,
                    250 + i * 50,
                    options[i],
                    "Arial", 50);
        }
    }

    private void drawEndTwo() {
        drawPlayTwo();
        String[] options = {"Restart", "Back to menu", "Quit"};
        for (int i = 0; i < options.length; i++) {
            if (i == endOption) changeColor(Color.YELLOW);
            else changeColor(Color.WHITE);
            drawText(CANVAS_WIDTH / 2 - 100,
                    250 + i * 50,
                    options[i],
                    "Arial", 50);
        }
    }

    private void endMatchOne() {
        playAudio(winningSfx);
        restartMatch();
        gameState = GameState.PlayOneEnd;
        endOption = 0;
    }

    private void endMatchTwo() {
        playAudio(winningSfx);
        restartMatch();
        gameState = GameState.PlayTwoEnd;
        endOption = 0;
    }

    private void restartMatch(){
        leftPlayer.resetPosition();
        rightPlayer.resetPosition();

        leftPlayer.setSwingBounds();
        rightPlayer.setSwingBounds();
    }

    private void restartScore(){
        nextServerLeft = true;
        scoreRight = 0;
        scoreLeft = 0;
        leftPlayer.setServeFinished(false);
        rightPlayer.setServeFinished(false);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        switch (gameState) {
            case Menu:
                if (k == KeyEvent.VK_UP) menuOption = (menuOption + 1) % 3;
                if (k == KeyEvent.VK_DOWN) menuOption = (menuOption + 2) % 3;
                if (k == KeyEvent.VK_ENTER && !enterFlag) {
                    if (menuOption == 0) {
                        gameState = GameState.Inst;
                        instTimer   = 0;
                    } else if (menuOption == 1) {
                        gameState = GameState.Inst;
                        instTimer   = 0;
                    } else {                       // Quit
                        System.exit(0);
                    }
                }
                break;
            case PlayOne:
                // ---- Left control ----
                if (k == KeyEvent.VK_D) leftPlayer.setAction(Player.Action.Forward);
                else if (k == KeyEvent.VK_A) leftPlayer.setAction(Player.Action.Backward);
                else if (k == KeyEvent.VK_W) leftPlayer.jump();
                else if (k == KeyEvent.VK_S) {
                    if (!birdie.isInPlay() && nextServerLeft) leftPlayer.setAction(Player.Action.Serving);
                    else leftPlayer.setAction(Player.Action.Swing);
                }
                break;
            case PlayTwo:
                // ---- Left control ----
                if (k == KeyEvent.VK_D) leftPlayer.setAction(Player.Action.Forward);
                else if (k == KeyEvent.VK_A) leftPlayer.setAction(Player.Action.Backward);
                else if (k == KeyEvent.VK_W) leftPlayer.jump();
                else if (k == KeyEvent.VK_S) {
                    if (!birdie.isInPlay() && nextServerLeft) leftPlayer.setAction(Player.Action.Serving);
                    else leftPlayer.setAction(Player.Action.Swing);
                }
                // ---- Right control ----
                if (k == KeyEvent.VK_RIGHT) rightPlayer.setAction(Player.Action.Forward);
                else if (k == KeyEvent.VK_LEFT) rightPlayer.setAction(Player.Action.Backward);
                else if (k == KeyEvent.VK_UP) rightPlayer.jump();
                else if (k == KeyEvent.VK_DOWN) {
                    if (!birdie.isInPlay() && !nextServerLeft) rightPlayer.setAction(Player.Action.Serving);
                    else rightPlayer.setAction(Player.Action.Swing);
                }
                break;
            case Inst:
                break;
            case PlayOneEnd:
                if (k == KeyEvent.VK_UP) endOption = (endOption + 1) % 3;
                if (k == KeyEvent.VK_DOWN) endOption = (endOption + 2) % 3;
                if (k == KeyEvent.VK_ENTER && !enterFlag) {
                    if (endOption == 0) {
                        gameState = GameState.PlayOne;
                        restart = true;
                    } else if (endOption== 1) {
                        gameState = GameState.Menu;
                        restart = true;
                    }else{
                        System.exit(0);
                    }
                }
                break;
            case PlayTwoEnd:
                if (k == KeyEvent.VK_UP) endOption = (endOption + 1) % 3;
                if (k == KeyEvent.VK_DOWN) endOption = (endOption + 2) % 3;
                if (k == KeyEvent.VK_ENTER && !enterFlag) {
                    if (endOption == 0) {
                        gameState = GameState.PlayTwo;
                        restart = true;
                    } else if (endOption== 1) {
                        gameState = GameState.Menu;
                    }else{
                        System.exit(0);
                    }
                }
                break;
            default:
                break;
        }

        if (k == KeyEvent.VK_ENTER) {
            enterFlag = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_A) leftPlayer.setAction(Player.Action.Standing);
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_LEFT) rightPlayer.setAction(Player.Action.Standing);
        if (k == KeyEvent.VK_ENTER) enterFlag = false;
    }

    // --------------------------------------------------------------------
    // Game State & Scoring
    // --------------------------------------------------------------------
    enum GameState {Menu, Inst, PlayOne, PlayTwo, PlayOneEnd, PlayTwoEnd}
}

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class Player {
    public enum Action { Standing, Serving, Forward, Backward, Swing }

    private final GameEngine engine;
    private final String folder;
    public double x, y;           // 当前位置
    private final int serveDir;    // +1：左→右，-1：右→左
    private final double initialX, initialY;

    private double restrictedMinX, restrictedMaxX;
    private double SwingMinX,     SwingMaxX;

    // 水平动画帧
    private Image standing;
    private Image[] serving, forward, backward, swing;

    // 发球动画
    private double serveTimer = 0;
    private final double serveDuration = 0.4;
    private boolean serveFinishedFlag = false;

    // 挥拍动画
    private double swingTimer = 0;
    private final double swingDuration = 0.3;
    private double swingAngle = 0 ;

    // 跳跃状态（与 Action 并行）
    private boolean isJumping = false;
    private double baselineY;             // 地面 y 坐标
    private double vy = 0;                // 垂直速度
    private final double JUMP_VELOCITY = -300;  // 跳得更低一点
    private final double GRAVITY       = 800;

    // 水平移动
    private final double speed = 320;
    private double minX, maxX;


    private Action action = Action.Standing;

    public Player(GameEngine engine, String folder, double x, double y, int serveDir) {
        this.engine   = engine;
        this.folder   = folder;
        this.x        = x;
        this.y        = y;
        this.serveDir = serveDir;

        this.initialX = x;
        this.initialY = y;
    }

    public void init() {
        standing = engine.loadImage(folder + "/standing.png");
        forward  = loadFrames("forward",  8);
        backward = loadFrames("backward", 8);
        swing    = loadFrames("swing",   12);
        serving  = loadFrames("serving", 8);

        // 计算半场和全场限位
        int mid = 800 / 2;                // 画布宽度硬编码 800

        if (serveDir > 0) {
            restrictedMinX = -25;
            restrictedMaxX = mid - 100;
            SwingMinX = -25 ;
            SwingMaxX =  mid - 250;
        } else {
            restrictedMinX = mid - 50;
            restrictedMaxX = 800 - 125;
            SwingMinX = mid - 50 + 150 ;
            SwingMaxX =  800 - 125;
        }
        // 默认先用半场限位
        setSwingBounds();

        baselineY = y;  // 记录“地面”高度
    }

    /** 切换到“发球前”半场限位 */
    public void setRestrictedBounds() {
        this.minX = restrictedMinX;
        this.maxX = restrictedMaxX;
    }

    /** 切换到全场限位 */
    public void setSwingBounds() {
        this.minX = SwingMinX;
        this.maxX = SwingMaxX;
    }

    /** 得分后重置到初始位置 */
    public void resetPosition() {
        this.x = initialX;
        this.y = initialY;
    }

    private Image[] loadFrames(String name, int count) {
        Image[] arr = new Image[count];
        Image src = engine.loadImage(folder + "/" + name + ".png");
        for (int i = 0; i < count; i++) {
            arr[i] = engine.subImage(src, i * 150, 0, 150, 150);
        }
        return arr;
    }

    /** 水平/挥拍/发球 动作切换 */
    public void setAction(Action a) {
        switch (a) {
            case Swing:
                action     = Action.Swing;
                swingTimer = 0;
                break;
            case Serving:
                action        = Action.Serving;
                serveTimer    = 0;
                break;
            case Forward:
                action = Action.Forward;
                break;
            case Backward:
                action = Action.Backward;
                break;
            case Standing:
                action = Action.Standing;
                break;
        }
    }

    /** 外部调用，触发跳跃（只在地面上生效） */
    public void jump() {
        if (!isJumping && y == baselineY) {
            isJumping = true;
            vy        = JUMP_VELOCITY;
        }
    }

    public void update(double dt) {
        // —— 垂直物理（跳跃／下落） ——
        if (isJumping) {
            vy += GRAVITY * dt;
            y  += vy * dt;
            if (y >= baselineY) {
                y          = baselineY;
                vy         = 0;
                isJumping  = false;
            }
        }

        // —— 水平移动 ——
        if (action == Action.Forward)  x += speed * dt;
        if (action == Action.Backward) x -= speed * dt;
        x = Math.max(minX, Math.min(maxX, x));

        // —— 发球动画 ——
        if (action == Action.Serving) {
            serveTimer += dt;
            if (serveTimer >= serveDuration) {
                action            = Action.Standing;
                serveTimer        = 0;
                serveFinishedFlag = true;
            }
        }

        // —— 挥拍动画 ——
        if (action == Action.Swing) {
            swingTimer += dt;
            if (swingTimer >= swingDuration) {
                action      = Action.Standing;
                swingTimer  = 0;
            }
        }
    }

    public boolean consumeServeFinished() {
        if (!serveFinishedFlag) return false;
        serveFinishedFlag = false;
        return true;
    }

    public Rectangle2D getRacketHitBox() {
        // 计算帧左上角：人物中心底部为 (x, y)，向上 FRAME 像素
        int w = 40;
        int h = 40;

        double centralX = 75 + x;
        double centralY = 75 + y;
        double racketAngle = swingAngle + 45;
        double racketLength = 55;
        double left, top;
        if (serveDir == 1){
            left = centralX - Math.cos(Math.toRadians(racketAngle)) * racketLength - w/2;
            top = centralY - Math.sin(Math.toRadians(racketAngle))  * racketLength - h/2;
        }else{
            left = centralX + Math.cos(Math.toRadians(racketAngle)) * racketLength - w/2;
            top = centralY - Math.sin(Math.toRadians(racketAngle))  * racketLength - h/2;
        }
        return new Rectangle2D.Double(left ,top , w, h);
    }


    /** 空中/地面都可以挥拍击球 */
    public boolean tryHit(Birdie b) {
        if (action!=Action.Swing || swingTimer> swingDuration/2) return false;
        return getRacketHitBox().intersects(b.getHitBox());
    }

    public void draw() {
        Image frame = standing;

        switch (action) {
            case Forward:
                frame = forward[(int)((System.currentTimeMillis()/60) % forward.length)];
                break;
            case Backward:
                frame = backward[(int)((System.currentTimeMillis()/60) % backward.length)];
                break;
            case Swing:
                int si = (int)((swingTimer / swingDuration) * swing.length);
                if (si < swing.length) frame = swing[si];
                if (swingTimer <= swingDuration/2) {
                    swingAngle =  swingTimer / (swingDuration / 2) * (180.0 - 45.0) ;
                }else {
                    swingAngle = 0;
                }
                // System.out.println(swingTimer+ " "+ swingAngle);
                break;
            case Serving:
                int pi = (int)((serveTimer / serveDuration) * serving.length);
                if (pi < serving.length) frame = serving[pi];
                break;
            default:
                break;
        }
        engine.drawImage(frame, (int)x, (int)y, 150, 150);

//        Rectangle2D box = getRacketHitBox();
//        engine.changeColor(255, 0, 0);
//        engine.drawRectangle((int)box.getX(),
//                (int)box.getY(),
//                (int)box.getWidth(),
//                (int)box.getHeight());
    }

    public int getServeDir() { return serveDir; }
    public Action getAction() { return action; }
    public double getSwingAngle() { return swingAngle; }
}

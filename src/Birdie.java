import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Birdie {
    private final GameEngine engine;
    private final Image image;
    private final int size = 30;                // 固定 50×50
    private boolean inPlay = false;

    // 物理属性
    public double x, y;
    public double vx, vy;
    private static final double GRAVITY    = 800;
    private static final double INIT_SPEED = 500;  // 初始速度
    private static final double DRAG_H = 0.8;  // 水平阻尼
    private static final double DRAG_V = 0.5; // 垂直阻尼
    private static final double BOUNCE_DAMPING = 0.30;

    private double groundTimer = 0;
    private static final double GROUND_SLIDE_DURATION = 0.5; // 0.5 秒

    public Birdie(GameEngine engine, String imagePath) {
        this.engine = engine;
        this.image  = engine.loadImage(imagePath);
    }

    /** 发球，从 (startX, startY) 中心发出，dir = ±1 */
    public void serve(double startX, double startY, int dir) {
        this.x       = startX;
        this.y       = startY;
        this.vx      = dir * INIT_SPEED;
        this.vy      = -INIT_SPEED * ( 1 + engine.rand(1.0));
        this.inPlay  = true;
    }

    public Rectangle2D getHitBox() {
        // 中心 (x,y)，大小 SIZE×SIZE
        return new Rectangle2D.Double(
                x - size/2.0,
                y - size /2.0,
                size,
                size
        );
    }
    /** 是否还在场上飞行 */
    public boolean isInPlay() {
        return inPlay;
    }

    /** 每帧更新物理 */
    public void update(double dt) {
        if (!inPlay) return;

        // 重力
        x  += vx * dt;
        y  += vy * dt;

        // 落地判定（画布高固定 500）
        double groundY = 470;
        if (y >= groundY) {
            // 第一次触地时，把 y 固定到地面高度，并清零垂直速度
            y  = groundY;
            vy = 0;
            vx *= 0.5;
            // 累加滑行时间
            groundTimer += dt;

            // 如果滑行超过 0.5s，才真正结束飞行
            if (groundTimer >= GROUND_SLIDE_DURATION) {
                inPlay = false;
            }
        } else {
            // 左右反弹（画布宽度固定 800）
            if (x < 0) {
                x  = 0;
                vx = -vx * BOUNCE_DAMPING;
            } else if (x > 800) {
                x  = 800;
                vx = -vx * BOUNCE_DAMPING;
            }

            if (x < 425 && x > 375 && y > 400){
                vx = -vx * 0.01;
            }

            // 阻尼和重力
            vy += GRAVITY * dt;

            vx *= Math.max(0, 1 - DRAG_H * dt);
            vy *= Math.max(0, 1 - DRAG_V * dt);
            groundTimer = 0;
        }
    }

    /** 绘制到屏幕上，并根据速度方向旋转 */
    public void draw() {
        if (!inPlay) return;

        // 保存当前 transform
        engine.saveCurrentTransform();

        // 移动到羽毛球中心
        engine.translate(x, y);

        // 计算旋转角：atan2(vy, vx) + 90°，因为贴图默认朝上
        double angle = Math.toDegrees(Math.atan2(vy, vx)); //+ Math.PI / 2)
        engine.rotate(angle);
        // System.out.println(angle);

        // 绘制贴图（相对于中心点偏移 size/2）
        engine.drawImage(image,
                -size / 2,
                -size / 2,
                size, size);
//        engine.drawRectangle(
//                -size / 2,
//                -size / 2,
//                size, size);
        // 恢复 transform
        engine.restoreLastTransform();

    }


}

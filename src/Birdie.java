import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Birdie {
    private final GameEngine engine;
    private final Image image;
    private final int size = 30;                // 固定 50×50
    private boolean inPlay = false;

    // Physical properties
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

    /** Serve from the (startX, startY) center，dir = ±1 */
    public void serve(double startX, double startY, int dir) {
        this.x       = startX;
        this.y       = startY;
        this.vx      = dir * INIT_SPEED;
        this.vy      = -INIT_SPEED * ( 1 + engine.rand(1.0));
        this.inPlay  = true;
    }

    public Rectangle2D getHitBox() {
        // Center (x,y), size SIZE×SIZE
        return new Rectangle2D.Double(
                x - size/2.0,
                y - size /2.0,
                size,
                size
        );
    }
    /** Is it still flying on the field */
    public boolean isInPlay() {
        return inPlay;
    }

    /** Update the physics per frame */
    public void update(double dt) {
        if (!inPlay) return;

        // Gravity
        x  += vx * dt;
        y  += vy * dt;

        // Landing determination (Canvas height fixed at 500)
        double groundY = 470;
        if (y >= groundY) {
            // When touching the ground for the first time, fix y to the ground height and reset the vertical velocity to zero
            y  = groundY;
            vy = 0;
            vx *= 0.5;
            // Accumulate the sliding time
            groundTimer += dt;

            // If the glide exceeds 0.5 seconds, the flight will truly end
            if (groundTimer >= GROUND_SLIDE_DURATION) {
                inPlay = false;
            }
        } else {
            // Left and right rebound (Canvas width fixed at 800)
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

            // Damping and Gravity
            vy += GRAVITY * dt;

            vx *= Math.max(0, 1 - DRAG_H * dt);
            vy *= Math.max(0, 1 - DRAG_V * dt);
            groundTimer = 0;
        }
    }

    /** Draw it on the screen and rotate it according to the speed direction */
    public void draw() {
        if (!inPlay) return;

        // Save the current transform
        engine.saveCurrentTransform();

        // Move to the badminton center
        engine.translate(x, y);

        // Calculate the rotation Angle: atan2(vy, vx) + 90°, as the texture is projected upwards by default
        double angle = Math.toDegrees(Math.atan2(vy, vx)); //+ Math.PI / 2)
        engine.rotate(angle);
        // System.out.println(angle);

        // Draw the map (offset by size/2 relative to the center point)
        engine.drawImage(image,
                -size / 2,
                -size / 2,
                size, size);
//        engine.drawRectangle(
//                -size / 2,
//                -size / 2,
//                size, size);
        // Restore transform
        engine.restoreLastTransform();

    }


}

package xyz.bi2nb9o3.minecartspeeddisplay.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MinecartTracer {
    private static final MinecartTracer INSTANCE = new MinecartTracer();
    private final List<TrackedMinecart> trackedMinecarts = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat(".00");
    private TrackPoint lookingAtPoint=null;
    private static final int maxTrailPoints = 2000;

    public static MinecartTracer getInstance() {
        return INSTANCE;
    }

    public void startTracking(AbstractMinecartEntity minecart) {
        // Check if already tracking
        for (TrackedMinecart tracked : trackedMinecarts) {
            if (tracked.minecart == minecart) {
                return;
            }
        }

        trackedMinecarts.add(new TrackedMinecart(minecart));
    }

    public void stopTracking() {
        // Stop tracking new trail but keep existing trails
        // This method only stops recording new trail, not clears existing trails
        for (TrackedMinecart tracked : trackedMinecarts) {
            tracked.stopTracking();
        }
    }

    public void clearAllTrails() {
        // Clear all existing trails completely
        trackedMinecarts.clear();
    }

    public void update() {
        if (DisplayManager.getInstance().isDisabled()) {
            return;
        }

        List<TrackedMinecart> toRemove = new ArrayList<>();

        for (TrackedMinecart tracked : trackedMinecarts) {
            if (!tracked.isValid()) {
                toRemove.add(tracked);
                continue;
            }

            tracked.update();
        }

        trackedMinecarts.removeAll(toRemove);
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider.Immediate vertexConsumers, double camX, double camY, double camZ) {
        if (DisplayManager.getInstance().isDisabled()) {
            return;
        }

        for (TrackedMinecart tracked : trackedMinecarts) {
            tracked.render(matrixStack, vertexConsumers, camX, camY, camZ);
        }
    }

    public void renderHud(DrawContext drawContext) {
        if (DisplayManager.getInstance().isDisabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Check if looking at any trail point
        for (TrackedMinecart tracked : trackedMinecarts) {
            ImmutableTriple<TrackPoint,Double,Double> result = tracked.getLookingAtPoint();
            if (result != null) {
                if (result.left!=null && result.middle!=null && result.right!=null){
                    TrackPoint point = (TrackPoint) result.left;
                    lookingAtPoint = point;
                    renderPointInfo(drawContext, point);
//                    drawContext.fill(result.middle.intValue(),result.right.intValue(),result.middle.intValue()+100,result.right.intValue()+100,0xFF00FF00);
                    break;
                }
            }
        }
    }

    private void renderPointInfo(DrawContext drawContext, TrackPoint point) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2;
        int y = screenHeight / 2 + 20;

        drawContext.drawText(textRenderer, "Trail Point:", x - textRenderer.getWidth("Trail Point:") / 2, y, Formatting.WHITE.getColorValue()+(255<<24), false);
        drawContext.drawText(textRenderer, "Speed: " + df.format(point.speed) + " block/s", x - textRenderer.getWidth("Speed: 00.00 block/s") / 2, y + 12, Formatting.GREEN.getColorValue()+(255<<24), false);
        drawContext.drawText(textRenderer, "Time: " + point.tick, x - textRenderer.getWidth("Time: 0000") / 2, y + 24, Formatting.AQUA.getColorValue()+(255<<24), false);
    }

    private static class TrackedMinecart {
        public final AbstractMinecartEntity minecart;
        public final World world;
        private final List<TrackPoint> trail = new ArrayList<>();
        private long lastUpdateTick = 0;
        private double maxSpeed = 0;
        private boolean isTracking = true;

        public TrackedMinecart(AbstractMinecartEntity minecart) {
            this.minecart = minecart;
            this.world = minecart.getEntityWorld();
        }

        public boolean isValid() {
            return minecart.isAlive() && minecart.getEntityWorld() == world;
        }

        public void stopTracking() {
            // Stop tracking new trail but keep existing trail
            isTracking = false;
        }

        public void update() {
            // Only update if still tracking
            if (!isTracking) {
                return;
            }

            long currentTick = world.getTime();
            if (currentTick - lastUpdateTick < 2) { // Update every 2 ticks for performance
                return;
            }

            lastUpdateTick = currentTick;

            Vec3d pos = minecart.getEntityPos();
            Vec3d velocity = minecart.getVelocity();
            double speed = velocity.length() * 20;

            if (speed > maxSpeed) {
                maxSpeed = speed;
            }

            trail.add(new TrackPoint(pos, speed, (int) currentTick));

            // Limit trail length to avoid memory issues
            if (trail.size() > maxTrailPoints) {
                trail.subList(0, trail.size() - maxTrailPoints).clear();
            }
        }

        public void render(MatrixStack matrixStack, VertexConsumerProvider.Immediate vertexConsumers, double camX, double camY, double camZ) {
            // Always render existing trail, even if not tracking anymore
            if (trail.size() < 2) {
                return;
            }

            // Render connected trail
            for (int i = 1; i < trail.size(); i++) {
                TrackPoint prev = trail.get(i - 1);
                TrackPoint curr = trail.get(i);

                // Calculate color based on speed
                int color = getColorForPoint(curr, maxSpeed);

                // Draw line between trail
                drawLine(matrixStack, prev.pos, curr.pos, color, camX, camY, camZ);
            }
        }

        private void drawLine(MatrixStack matrixStack, Vec3d start, Vec3d end, int color, double camX, double camY, double camZ) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Draw line segments using actual world positions
            // This ensures the rendered trail match the world coordinates used for detection
//            int segments = 1;
//            for (int i = 0; i < segments; i++) {
//                double t = (double)i / segments;
//                // Calculate actual world position of the point
//                Vec3d pointPos = new Vec3d(
//                    start.x + (end.x - start.x) * t,
//                    start.y + (end.y - start.y) * t,
//                    start.z + (end.z - start.z) * t
//                );
//                // Draw the point at the actual world position
//                StringDrawer.drawString(matrixStack, pointPos, 0, 0, new String[]{"·"}, new int[]{color});
//            }
            StringDrawer.drawString(matrixStack, end, 0, 0, new String[]{"·"}, new int[]{color});
//            StringDrawer.drawString(matrixStack, pointPos, 0, 0, new String[]{"·"}, new int[]{color});
        }

        private int getColorForPoint(TrackPoint point, double maxSpeed) {
            double speed=point.speed;
            if (MinecartTracer.INSTANCE.lookingAtPoint == point){
                return Formatting.WHITE.getColorValue(); // Looking at
            }
            if (speed < maxSpeed * 0.3) {
                return Formatting.GREEN.getColorValue(); // Slow
            } else if (speed < maxSpeed * 0.7) {
                return Formatting.YELLOW.getColorValue(); // Medium
            } else {
                return Formatting.RED.getColorValue(); // Fast
            }
        }

//        public TrackPoint getLookingAtPoint() {
//            MinecraftClient client = MinecraftClient.getInstance();
//            if (client.player == null) return null;
//
//            double maxDistance = 5.0;
//            TrackPoint closestPoint = null;
//            double closestDistance = maxDistance;
//
//            Vec3d cameraPos = client.player.getCameraPosVec(1.0f);
//            Vec3d lookDirection = client.player.getRotationVec(1.0f);
//
//            for (TrackPoint point : trail) {
//                Vec3d pointPos = point.pos;
//                Vec3d cameraToPoint = pointPos.subtract(cameraPos);
//                double distance = cameraToPoint.length();
//
//                if (distance > maxDistance) {
//                    continue;
//                }
//
//                double dotProduct = cameraToPoint.normalize().dotProduct(lookDirection);
//                if (dotProduct > 0.95) { // Looking almost directly at the point
//                    if (distance < closestDistance) {
//                        closestDistance = distance;
//                        closestPoint = point;
//                    }
//                }
//            }
//
//            return closestPoint;
//        }
        public ImmutableTriple<TrackPoint,Double,Double> getLookingAtPoint() {
            if (trail == null || trail.isEmpty()) return null;
            double screenWidth =  MinecraftClient.getInstance().getWindow().getWidth();
            double screenHeight =  MinecraftClient.getInstance().getWindow().getHeight();
            double maxDistance =5.0;

            double screenCenterX = screenWidth / 2.0;  // 需要传入屏幕尺寸或从其他地方获取
            double screenCenterY = screenHeight / 2.0;

            TrackPoint closest = null;
            double minDistSq = Double.MAX_VALUE;
            double recordCloestX = 0.0;
            double recordCLoestY = 0.0;

            for (TrackPoint tp : trail) {
                Vec3d ndc = MinecraftClient.getInstance().gameRenderer.project(tp.pos);

                // 检查点是否在视锥内（即 NDC 坐标在 [-1,1] 范围内）
                if (ndc.x < -1.0 || ndc.x > 1.0 || ndc.y < -1.0 || ndc.y > 1.0 || ndc.z < -1.0 || ndc.z > 1.0) {
                    continue; // 跳过屏幕外的点
                }

                // 将 NDC 坐标转换为屏幕像素坐标
                double screenX = (ndc.x + 1.0) / 2.0 * screenWidth;
                double screenY = (1.0 - ndc.y) / 2.0 * screenHeight; // 因为 NDC y 向上，屏幕 y 向下

                double dx = screenX - screenCenterX;
                double dy = screenY - screenCenterY;
                double distSq = dx * dx + dy * dy;

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    closest = tp;
                    recordCloestX=screenX;
                    recordCLoestY=screenY;
                }
            }


            if (closest!=null)
                if(closest.pos.distanceTo(MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos())>maxDistance)
                    return null;
            return ImmutableTriple.of(closest,recordCloestX,recordCLoestY);
        }

//        private double distanceToLine(Vec3d start, Vec3d end, Vec3d point) {
//            // Calculate the shortest distance from a point to a line segment
//            Vec3d lineVector = end.subtract(start);
//            Vec3d pointVector = point.subtract(start);
//
//            // Calculate parameter t for the closest point on the line
//            double t = Math.max(0, Math.min(1, pointVector.dotProduct(lineVector) / lineVector.dotProduct(lineVector)));
//
//            // Calculate the closest point on the line
//            Vec3d closestPoint = start.add(lineVector.multiply(t));
//
//            // Return the distance between the point and the closest point on the line
//            return closestPoint.distanceTo(point);
//        }
    }

    private static class TrackPoint {
        public final Vec3d pos;
        public final double speed;
        public final int tick;

        public TrackPoint(Vec3d pos, double speed, int tick) {
            this.pos = pos;
            this.speed = speed;
            this.tick = tick;
        }
    }
}

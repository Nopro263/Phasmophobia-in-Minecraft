package at.nopro.phasmo.game;

import at.nopro.phasmo.Reflection;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

public class DisplayManager {
    private final int SANITY1 = 1;
    private final int SANITY2 = 2;
    private final int ACTIVITY1 = 3;
    private final int ACTIVITY2 = 4;
    private final int MAP1 = 5;
    private final int MAP2 = 6;
    private final int MAP3 = 7;
    private final int MAP4 = 8;
    private final int CAM1 = 9;
    private final int CAM2 = 10;
    private final int CAM3 = 11;
    private final int CAM4 = 12;
    private final int CAM5 = 13;
    private final int CAM6 = 14;
    private final int CAM7 = 15;
    private final int CAM8 = 16;

    private final MapDataPacket[] sanityCache = new MapDataPacket[2];
    private final MapDataPacket[] activityCache = new MapDataPacket[2];
    private final MapDataPacket[][] mapCache;
    private final MapDataPacket[] camCache = new MapDataPacket[8];

    private final GameContext gameContext;
    private int currentMapLevel = 0;

    private Robot camRobot;
    private Rectangle camRectangle;

    public DisplayManager(GameContext gameContext) {
        this.gameContext = gameContext;
        this.mapCache = new MapDataPacket[gameContext.getMapContext().validLevels().size()][4];
    }

    public void init() {
        gameContext.getScheduler().run(this.hashCode() + "VanCam", () -> {
            try {
                drawCam();
            } catch (AWTException e) {
                System.err.println("Error during camera rendering: " + e);
                render4x2(camCache, CAM1, CAM2, CAM3, CAM4, CAM5, CAM6, CAM7, CAM8, (g) -> {
                    g.setPaint(Color.RED);
                    g.fillRect(0, 0, 128 * 4, 128 * 2);
                });
                return TaskSchedule.stop();
            }
            return TaskSchedule.tick(1);
        });

        drawActivity();
        drawMap();
        drawSanity();

        for (int i = 0; i < gameContext.getMapContext().validLevels().size(); i++) {
            renderMapAtLevel(i, gameContext.getMapContext().validLevels().get(i));
        }
    }

    public void drawCam() throws AWTException {
        if (camRobot == null) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
            camRobot = new Robot(gd);
            camRectangle = new Rectangle(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        }
        render4x2(camCache, CAM1, CAM2, CAM3, CAM4, CAM5, CAM6, CAM7, CAM8, (g) -> {
            BufferedImage screenShot = camRobot.createScreenCapture(camRectangle);
            g.drawImage(screenShot, 0, 0, 128 * 4, 128 * 2, null);
        });
    }

    private void render4x2(MapDataPacket[] cache, int map1, int map2, int map3, int map4, int map5, int map6, int map7, int map8, Consumer<Graphics2D> renderer) {
        LargeGraphics2DFramebuffer sanity = new LargeGraphics2DFramebuffer(128 * 4, 128 * 2);
        Graphics2D g = sanity.getRenderer();

        renderer.accept(g);

        cache[0] = sanity.preparePacket(map1, 0, 0);
        cache[1] = sanity.preparePacket(map2, 128, 0);
        cache[2] = sanity.preparePacket(map3, 128 * 2, 0);
        cache[3] = sanity.preparePacket(map4, 128 * 3, 0);
        cache[4] = sanity.preparePacket(map5, 0, 128);
        cache[5] = sanity.preparePacket(map6, 128, 128);
        cache[6] = sanity.preparePacket(map7, 128 * 2, 128);
        cache[7] = sanity.preparePacket(map8, 128 * 3, 128);
        for (Player player : gameContext.getInstance().getPlayers()) {
            player.sendPacket(cache[0]);
            player.sendPacket(cache[1]);
            player.sendPacket(cache[2]);
            player.sendPacket(cache[3]);
            player.sendPacket(cache[4]);
            player.sendPacket(cache[5]);
            player.sendPacket(cache[6]);
            player.sendPacket(cache[7]);
        }
    }

    public void drawActivity() {
        render2x1(activityCache, ACTIVITY1, ACTIVITY2, (g) -> {
            g.setPaint(Color.RED);
            //g.fillRect(0,0,128*2,128);
            gameContext.getActivityManager().drawActivityLine(g, 3, 253, 125, 3);
        });
    }

    public void drawMap() {

    }

    public void drawSanity() {
        render2x1(sanityCache, SANITY1, SANITY2, (g) -> {
            g.setStroke(new BasicStroke(1));

            int padding = 3;
            int font_height = g.getFontMetrics().getHeight();

            String average = "Average";
            int average_width = g.getFontMetrics().stringWidth(average);
            g.drawString(average, ( 128 * 2 - average_width ) / 2, font_height);

            int top_y = font_height * 2;
            int size_y = 128 - top_y;
            int[] x = new int[]{ padding, padding + 128, padding, padding + 128 };
            int[] y = new int[]{ top_y + padding, top_y + padding, top_y + padding + ( size_y / 2 ), top_y + padding + ( size_y / 2 ) };
            int i = 0;
            int sanity_sum = 0;

            for (Player player : gameContext.getInstance().getPlayers()) {
                if (player == gameContext.getCamPlayer()) {
                    continue;
                }
                PlayerManager.PlayerData playerData = gameContext.getPlayerManager().getPlayerData(player);
                sanity_sum += playerData.sanity();

                renderPlayerSanity(padding, g,
                        player.getUsername(),
                        playerData.sanity(),
                        playerData.alive(),
                        playerData.color(),
                        x[i], y[i]);
                i++;
            }

            String average_percent = ( sanity_sum / Math.max(i, 1) ) + "%";
            int average_percent_width = g.getFontMetrics().stringWidth(average_percent);
            g.drawString(average_percent, ( 128 * 2 - average_percent_width ) / 2, font_height * 2);
        });
    }

    private void renderMapAtLevel(int levelId, int level) {
        int x1 = gameContext.getMapContext().lowerEnd().blockX();
        int x2 = gameContext.getMapContext().upperEnd().blockX();
        int z1 = gameContext.getMapContext().lowerEnd().blockZ();
        int z2 = gameContext.getMapContext().upperEnd().blockZ();

        int sx = x2 - x1 + 1;
        int sz = z2 - z1 + 1;
        int size = Math.max(sx, sz);
        double scale = 256d / size;

        render2x2(mapCache[levelId], MAP1, MAP2, MAP3, MAP4, (g) -> {
            g.setPaint(Color.BLACK);
            g.scale(scale, scale);
            switch (gameContext.getMapContext().mapUpIsDirection()) {
                case NORTH -> g.rotate(0);
                case WEST -> g.rotate(Math.toRadians(90), sx / 2d, sx / 2d);
                case SOUTH -> g.rotate(Math.toRadians(180), sx / 2d, sx / 2d);
                case EAST -> g.rotate(Math.toRadians(270), sx / 2d, sx / 2d);
            }

            g.fillRect(0, 0, sx, sz);
            g.setPaint(Color.LIGHT_GRAY);
            for (int i = x1; i <= x2; i++) {
                for (int j = z1; j <= z2; j++) {
                    if (gameContext.getPathCache().isInvalid((short) i, (short) level, (short) j)) {
                        g.fillRect(i - x1, j - z1, 1, 1);
                    }
                }
            }
        });
    }

    private void render2x1(MapDataPacket[] cache, int map1, int map2, Consumer<Graphics2D> renderer) {
        LargeGraphics2DFramebuffer sanity = new LargeGraphics2DFramebuffer(128 * 2, 128);
        Graphics2D g = sanity.getRenderer();

        renderer.accept(g);

        cache[0] = sanity.preparePacket(map1, 0, 0);
        cache[1] = sanity.preparePacket(map2, 128, 0);
        for (Player player : gameContext.getInstance().getPlayers()) {
            player.sendPacket(cache[0]);
            player.sendPacket(cache[1]);
        }
    }

    private void renderPlayerSanity(int padding, Graphics2D g, String name, int sanity, boolean alive, Color color, int x, int y) {
        int font_height = g.getFontMetrics().getHeight();
        int top_y = font_height * 2;
        int size_y = 128 - top_y;

        String percent;
        int percent_width;
        int width = 128 - 2 * padding;
        int height = size_y / 2 - 2 * padding;
        int font_correction = font_height / 4;

        if (!alive) {
            percent = "?";
        } else {
            percent = sanity + "%";
        }
        percent_width = g.getFontMetrics().stringWidth(percent);
        g.setPaint(Color.WHITE);
        g.drawRect(x, y, width, height); // outer outline
        g.drawRect(x + 1, y + height - font_height - 1, width - 1, font_height + 1); // outline for colored bar
        if (alive) {
            g.setPaint(color.darker());
            g.fillRect(x + 1, y + height - font_height, width - 1, font_height); // dark bar
            g.setPaint(color);
            g.fillRect(x + 1, y + height - font_height, (int) ( ( width - 1 ) * ( sanity / 100d ) ), font_height); // light bar
        }
        g.setPaint(Color.WHITE);
        g.drawString(percent, x + width - percent_width, y + height - font_correction); // percentage text
        g.drawString(name, x + 1, y + ( height - font_height ) / 2 + font_correction); // Name
    }

    private void render2x2(MapDataPacket[] cache, int map1, int map2, int map3, int map4, Consumer<Graphics2D> renderer) {
        LargeGraphics2DFramebuffer sanity = new LargeGraphics2DFramebuffer(128 * 2, 128 * 2);
        Graphics2D g = sanity.getRenderer();

        renderer.accept(g);

        cache[0] = sanity.preparePacket(map1, 0, 0);
        cache[1] = sanity.preparePacket(map2, 128, 0);
        cache[2] = sanity.preparePacket(map3, 0, 128);
        cache[3] = sanity.preparePacket(map4, 128, 128);
        for (Player player : gameContext.getInstance().getPlayers()) {
            player.sendPacket(cache[0]);
            player.sendPacket(cache[1]);
            player.sendPacket(cache[2]);
            player.sendPacket(cache[3]);
        }
    }

    @ApiStatus.Internal
    public Entity modifyEntity(Entity entity) {
        if (entity.getEntityMeta() instanceof ItemFrameMeta itemFrameMeta) {
            ItemStack itemStack = itemFrameMeta.getItem();
            if (itemStack.get(DataComponents.CUSTOM_NAME) instanceof TextComponent text) {
                String name = text.content().toUpperCase();
                Integer mapId = Reflection.get(this, name);
                if (mapId != null) {
                    itemFrameMeta.setItem(ItemStack.builder(Material.FILLED_MAP)
                            .set(DataComponents.MAP_ID, mapId)
                            .build());
                }
            }
        }
        return entity;
    }

    public void sendAllCached(Player player) {
        for (MapDataPacket[] m : List.of(sanityCache, activityCache, mapCache[currentMapLevel], camCache)) {
            for (MapDataPacket mp : m) {
                player.sendPacket(mp);
            }
        }
    }

    public void increaseMapLevel() {
        currentMapLevel = Math.min(currentMapLevel + 1, gameContext.getMapContext().validLevels().size() - 1);
    }

    public void decreaseMapLevel() {
        currentMapLevel = Math.max(0, currentMapLevel - 1);
    }
}

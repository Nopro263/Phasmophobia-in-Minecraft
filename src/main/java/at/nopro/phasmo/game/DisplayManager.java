package at.nopro.phasmo.game;

import at.nopro.phasmo.Pair;
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
import java.util.Map;
import java.util.Objects;
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

    private final MapDataPacket[] sanityCache = new MapDataPacket[2];
    private final MapDataPacket[] activityCache = new MapDataPacket[2];
    private final MapDataPacket[] mapCache = new MapDataPacket[4];
    private final MapDataPacket[] camCache = new MapDataPacket[2];

    private final GameContext gameContext;

    private Robot camRobot;
    private Rectangle camRectangle;

    public DisplayManager(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void init() {
        drawActivity();
        try {
            drawCam();
        } catch (AWTException e) {
            throw new RuntimeException(e); // hmmm
        }
        drawMap();
        drawSanity();

        ScopedScheduler.run(this.hashCode()+"VanCam", () -> {
            try {
                drawCam();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            return TaskSchedule.tick(1);
        });
    }


    @ApiStatus.Internal
    public Entity modifyEntity(Entity entity) {
        if(entity.getEntityMeta() instanceof ItemFrameMeta itemFrameMeta) {
            ItemStack itemStack = itemFrameMeta.getItem();
            if(itemStack.get(DataComponents.CUSTOM_NAME) instanceof TextComponent text) {
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
        for(MapDataPacket[] m : List.of(sanityCache, activityCache, mapCache, camCache)) {
            for(MapDataPacket mp : m) {
                player.sendPacket(mp);
            }
        }
    }

    private void render2x1(MapDataPacket[] cache, int map1, int map2, Consumer<Graphics2D> renderer) {
        LargeGraphics2DFramebuffer sanity = new LargeGraphics2DFramebuffer(128 * 2, 128);
        Graphics2D g = sanity.getRenderer();

        renderer.accept(g);

        cache[0] = sanity.preparePacket(map1, 0,0);
        cache[1] = sanity.preparePacket(map2, 128,0);
        for(Player player : gameContext.getInstance().getPlayers()) {
            player.sendPacket(cache[0]);
            player.sendPacket(cache[1]);
        }
    }

    private void render2x2(MapDataPacket[] cache, int map1, int map2, int map3, int map4, Consumer<Graphics2D> renderer) {
        LargeGraphics2DFramebuffer sanity = new LargeGraphics2DFramebuffer(128 * 2, 128 * 2);
        Graphics2D g = sanity.getRenderer();

        renderer.accept(g);

        cache[0] = sanity.preparePacket(map1, 0,0);
        cache[1] = sanity.preparePacket(map2, 128,0);
        cache[2] = sanity.preparePacket(map3, 0,128);
        cache[3] = sanity.preparePacket(map4, 128,128);
        for(Player player : gameContext.getInstance().getPlayers()) {
            player.sendPacket(cache[0]);
            player.sendPacket(cache[1]);
            player.sendPacket(cache[2]);
            player.sendPacket(cache[3]);
        }
    }

    public void drawSanity() {
        render2x1(sanityCache, SANITY1, SANITY2, (g) -> {
            g.setPaint(Color.ORANGE);
            g.fillRect(0,0,128*2,128);
        });
    }

    public void drawActivity() {
        render2x1(activityCache, ACTIVITY1, ACTIVITY2, (g) -> {
            g.setPaint(Color.LIGHT_GRAY);
            g.fillRect(0,0,128*2,128);
        });
    }

    public void drawCam() throws AWTException {
        if(camRobot == null) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
            camRobot = new Robot(gd);
            camRectangle = new Rectangle(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        }
        render2x1(camCache, CAM1, CAM2, (g) -> {
            BufferedImage screenShot = camRobot.createScreenCapture(camRectangle);
            g.drawImage(screenShot,0,0,128*2,128, null);
        });
    }

    public void drawMap() {
        render2x2(mapCache, MAP1, MAP2, MAP3, MAP4, (g) -> {
            g.setPaint(Color.BLUE);
            g.fillRect(0,0,128*2,128*2);
        });
    }
}

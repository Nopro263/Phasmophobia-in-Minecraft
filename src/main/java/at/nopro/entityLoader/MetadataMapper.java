package at.nopro.entityLoader;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.metadata.other.GlowItemFrameMeta;
import net.minestom.server.entity.metadata.other.HangingMeta;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Rotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MetadataMapper {
    public static Tag<BinaryTag> DATA_TAG = Tag.NBT("data");

    public static Map<String, EntityType> MAP = new HashMap<>();
    public static Map<String, BiConsumer<CompoundBinaryTag, Entity>> META_CONSUMER = new HashMap<>();
    private static Class<?>[] SIGNATURE = new Class[] {CompoundBinaryTag.class, EntityMeta.class};

    @Retention(RetentionPolicy.RUNTIME)
    private @interface NBTParser {
    }

    static {
        System.out.println("generating entity map");
        try {
            for(Method m : MetadataMapper.class.getDeclaredMethods()) {
                if(Arrays.equals(m.getParameterTypes(), SIGNATURE) && m.isAnnotationPresent(NBTParser.class)) {
                    META_CONSUMER.put("minecraft:" + m.getName(), (ct, em) -> {
                        try {
                            m.invoke(null, ct, em.getEntityMeta());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

            META_CONSUMER.put("minecraft:marker", (ct, em) -> {
                marker(ct, em);
            });

            Class<?> clazz = Class.forName("net.minestom.server.entity.EntityTypes");
            for(Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                EntityType t = (EntityType) f.get(null);
                MAP.put(t.name(), t);
                META_CONSUMER.putIfAbsent(t.name(), (a,b) -> {
                    System.err.println("Not Implemented: " + t.name());
                    //throw new RuntimeException();
                });
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {}

    public static float[] listToFloatArray(ListBinaryTag listBinaryTag) {
        float[] f = new float[listBinaryTag.size()];
        for (int i = 0; i < listBinaryTag.size(); i++) {
            f[i] = listBinaryTag.getFloat(i);
        }
        return f;
    }

    public static Vec listToVec(ListBinaryTag listBinaryTag, Point origin) {
        String x = listBinaryTag.getString(0);
        String y = listBinaryTag.getString(1);
        String z = listBinaryTag.getString(2);

        double xd,yd,zd;

        if(x.startsWith("~")) {
            xd = origin.x() + Double.parseDouble(x.substring(1));
        } else {
            xd = Double.parseDouble(x);
        }

        if(y.startsWith("~")) {
            yd = origin.y() + Double.parseDouble(y.substring(1));
        } else {
            yd = Double.parseDouble(y);
        }

        if(z.startsWith("~")) {
            zd = origin.z() + Double.parseDouble(z.substring(1));
        } else {
            zd = Double.parseDouble(z);
        }

        return new Vec(
                xd,
                yd,
                zd
        );
    }

    private static void parseEntity(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {

    }

    private static void parseDisplay(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        AbstractDisplayMeta abstractDisplayMeta = (AbstractDisplayMeta) entityMeta;
        abstractDisplayMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.valueOf(compoundBinaryTag.getString("billboard", "fixed").toUpperCase()));
        // Brightness
        // Glow color override
        // height & width, ...

        CompoundBinaryTag transformation = compoundBinaryTag.getCompound("transformation");
        ListBinaryTag right_rotation = transformation.getList("right_rotation");
        ListBinaryTag left_rotation = transformation.getList("left_rotation");
        ListBinaryTag scale = transformation.getList("scale");
        ListBinaryTag translation = transformation.getList("translation");
        abstractDisplayMeta.setRightRotation(listToFloatArray(right_rotation));
        abstractDisplayMeta.setLeftRotation(listToFloatArray(left_rotation));
        abstractDisplayMeta.setScale(new Vec(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2)));
        abstractDisplayMeta.setTranslation(new Pos(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2)));


        entityMeta.setHasNoGravity(true);

        parseEntity(compoundBinaryTag, entityMeta);
    }

    private static void parseHanging(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        HangingMeta hangingMeta = (HangingMeta) entityMeta;
        Direction direction = switch (compoundBinaryTag.getByte("Facing")) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> null;
        };
        if(direction == null) {
            throw new RuntimeException("invalid key");
        }
        hangingMeta.setDirection(direction);

        parseEntity(compoundBinaryTag, entityMeta);
    }

    @NBTParser
    private static void block_display(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        BlockDisplayMeta blockDisplayMeta = (BlockDisplayMeta) entityMeta;
        CompoundBinaryTag blockState = compoundBinaryTag.getCompound("block_state");
        String name = blockState.getString("Name");
        CompoundBinaryTag blockStateProperties = blockState.getCompound("Properties");
        Block block = Block.fromKey(name);
        for(String key : blockStateProperties.keySet()) {
            block = block.withProperty(key, blockStateProperties.getString(key));
        }
        blockDisplayMeta.setBlockState(block);

        parseDisplay(compoundBinaryTag, entityMeta);
    }

    @NBTParser
    private static void item_display(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        ItemDisplayMeta itemDisplayMeta = (ItemDisplayMeta) entityMeta;

        ItemStack itemStack = ItemStack.fromItemNBT(compoundBinaryTag.getCompound("item"));
        itemDisplayMeta.setItemStack(itemStack);

        ItemDisplayMeta.DisplayContext displayContext = ItemDisplayMeta.DisplayContext.valueOf(compoundBinaryTag.getString("item_display").toUpperCase());
        itemDisplayMeta.setDisplayContext(displayContext);

        parseDisplay(compoundBinaryTag, entityMeta);
    }

    @NBTParser
    private static void text_display(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) entityMeta;

        parseDisplay(compoundBinaryTag, entityMeta);
    }

    @NBTParser
    private static void glow_item_frame(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        item_frame(compoundBinaryTag, entityMeta);
    }

    @NBTParser
    private static void item_frame(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        ItemFrameMeta itemFrameMeta = (ItemFrameMeta) entityMeta;

        if(!compoundBinaryTag.getCompound("Item").isEmpty()) {
            ItemStack itemStack = ItemStack.fromItemNBT(compoundBinaryTag.getCompound("Item"));
            itemFrameMeta.setItem(itemStack);
        }

        itemFrameMeta.setRotation(Rotation.values()[compoundBinaryTag.getByte("itemRotation")]);

        parseHanging(compoundBinaryTag, entityMeta);
    }

    @NBTParser
    private static void marker(CompoundBinaryTag compoundBinaryTag, Entity entity) {
        entity.setTag(DATA_TAG, compoundBinaryTag.get("data"));
    }
}

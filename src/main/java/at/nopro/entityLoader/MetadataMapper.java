package at.nopro.entityLoader;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MetadataMapper {
    public static Map<String, EntityType> MAP = new HashMap<>();
    public static Map<String, BiConsumer<CompoundBinaryTag, EntityMeta>> META_CONSUMER = new HashMap<>();
    private static Class<?>[] SIGNATURE = new Class[] {CompoundBinaryTag.class, EntityMeta.class};

    static {
        System.out.println("generating entity map");
        try {
            for(Method m : MetadataMapper.class.getDeclaredMethods()) {
                if(Arrays.equals(m.getParameterTypes(), SIGNATURE) && !m.getName().startsWith("parse")) {
                    META_CONSUMER.put("minecraft:" + m.getName(), (ct, em) -> {
                        try {
                            m.invoke(null, ct, em);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

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

    private static float[] listToFloatArray(ListBinaryTag listBinaryTag) {
        float[] f = new float[listBinaryTag.size()];
        for (int i = 0; i < listBinaryTag.size(); i++) {
            f[i] = listBinaryTag.getFloat(i);
        }
        return f;
    }

    private static void parseDisplay(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta, boolean positionFix) {
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
        if(positionFix) {
            abstractDisplayMeta.setTranslation(new Pos(translation.getFloat(0), translation.getFloat(1)+0.5, translation.getFloat(2)));
        } else {
            abstractDisplayMeta.setTranslation(new Pos(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2)));
        }


        entityMeta.setHasNoGravity(true);
    }


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

        parseDisplay(compoundBinaryTag, entityMeta, true);
    }

    private static void item_display(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        ItemDisplayMeta itemDisplayMeta = (ItemDisplayMeta) entityMeta;

        ItemStack itemStack = ItemStack.fromItemNBT(compoundBinaryTag.getCompound("item"));
        itemDisplayMeta.setItemStack(itemStack);

        ItemDisplayMeta.DisplayContext displayContext = ItemDisplayMeta.DisplayContext.valueOf(compoundBinaryTag.getString("item_display").toUpperCase());
        itemDisplayMeta.setDisplayContext(displayContext);

        parseDisplay(compoundBinaryTag, entityMeta, false);
    }

    private static void text_display(CompoundBinaryTag compoundBinaryTag, EntityMeta entityMeta) {
        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) entityMeta;

        parseDisplay(compoundBinaryTag, entityMeta, false);
    }
}

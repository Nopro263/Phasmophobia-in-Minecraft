package at.nopro.phasmo.lighting;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LightingComputeTest {

    @Test
    void countPrecedingBits() {
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        bitSet.set(3);
        bitSet.set(7);
        bitSet.set(10);

        assertEquals(2, NewLightingCompute.countPrecedingBits(7, bitSet));
    }

    @Test
    void getExistingByteArrayForSection() {
        byte[] b1 = new byte[2048];
        b1[0] = 1;

        byte[] b2 = new byte[2048];
        b2[0] = 2;

        byte[] b3 = new byte[2048];
        b3[0] = 3;

        BitSet bitSet = new BitSet();
        bitSet.set(2);
        bitSet.set(5);
        bitSet.set(7);

        List<byte[]> list = new ArrayList<>(List.of(b1, b2, b3));

        byte[] retrieved = NewLightingCompute.getByteArrayForSection(5, bitSet, list);
        assertEquals(2, retrieved[0]);
    }

    @Test
    void getNewByteArrayForSection() {
        byte[] b1 = new byte[2048];
        b1[0] = 1;

        byte[] b2 = new byte[2048];
        b2[0] = 2;

        byte[] b3 = new byte[2048];
        b3[0] = 3;

        BitSet bitSet = new BitSet();
        bitSet.set(2);
        bitSet.set(5);
        bitSet.set(7);

        List<byte[]> list = new ArrayList<>(List.of(b1, b2, b3));

        byte[] retrieved = NewLightingCompute.getByteArrayForSection(4, bitSet, list);
        assertAll(
                () -> assertEquals(0, retrieved[0]),
                () -> assertEquals(2, list.indexOf(b2)),
                () -> assertEquals(3, list.indexOf(b3)),
                () -> assertEquals(1, list.indexOf(retrieved))
        );
    }
}
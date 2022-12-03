package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.utils.JsonEnum;

public class GIREnumTest {

    public GIREnumTest() {
        // TODO Auto-generated constructor stub
    }

    @Test
    public void testEnum() {
        final Map<String, JsonEnum> map = JsonEnum.PROPERTIES;
        assertTrue(map.containsKey("ZS32"));
        assertTrue(map.containsKey("BUE"));
        final String[] array = map.get("BUE").getAllowedValues().toArray(new String[0]);
        assertArrayEquals(array, new String[] {
                "BUE2_1", "BUE2_2", "BUE2_3", "BUE2_4", "BUE3", "BUE4", "BUE5"
        });
    }

}

package de.interoberlin.merlot_android.model.mapping;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MappingTest {
    private Method methodMappingGetNodeValue;

    @Before
    public void setUp () {
        try {
            methodMappingGetNodeValue = Mapping.class.getDeclaredMethod("getNodeValue", String.class, String.class);
            methodMappingGetNodeValue.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetNodeValue() {
        String json = "{ \"foo\": { \"bar\": \"hello\" } }";
        String node = "foo.bar";

        try {
            Object o = methodMappingGetNodeValue.invoke(new Mapping(), json, node);

            Assert.assertEquals("hello", o.toString());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
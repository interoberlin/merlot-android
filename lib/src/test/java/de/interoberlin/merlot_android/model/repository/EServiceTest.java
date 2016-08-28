package de.interoberlin.merlot_android.model.repository;

import junit.framework.Assert;

import org.junit.Test;

public class EServiceTest {

    @Test
    public void testGetCharacteristics() throws Exception {
        Assert.assertEquals(4, EService.GENERIC_ACCESS.getCharacteristics().size());
        Assert.assertEquals(1, EService.GENERIC_ATTRIBUTE.getCharacteristics().size());
        Assert.assertEquals(3, EService.DEVICE_INFORMATION.getCharacteristics().size());
        Assert.assertEquals(1, EService.BATTERY_LEVEL.getCharacteristics().size());
        Assert.assertEquals(4, EService.CONNECTED_TO_MASTER_MODULE.getCharacteristics().size());
        Assert.assertEquals(7, EService.DIRECT_CONNECTION.getCharacteristics().size());
    }
}
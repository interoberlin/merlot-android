package de.interoberlin.merlot_android.model.parser.data;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.Test;

public class SentientLightLEDTest {
    @Test
    public void testSerialization() {
        SentientLightLED.LED led1 = new SentientLightLED().new LED(1, 255, 255, 255);

        SentientLightLED sentientLightLED = new SentientLightLED();
        sentientLightLED.getLeds().add(led1);

        Assert.assertEquals("{\"leds\":[{\"index\":1,\"values\":[255,255,255]}]}", new Gson().toJson(sentientLightLED));
    }

    @Test
    public void testDeserialization() {
        SentientLightLED sentientLightLED = new Gson().fromJson("{\"leds\":[{\"index\":1,\"values\":[255,255,255]}, {\"index\":2,\"values\":[0,0,0]}]}", SentientLightLED.class);

        Assert.assertEquals(2, sentientLightLED.getLeds().size());
        Assert.assertEquals((Integer) 255, sentientLightLED.getLeds().get(0).getValues().get(0));
    }
}
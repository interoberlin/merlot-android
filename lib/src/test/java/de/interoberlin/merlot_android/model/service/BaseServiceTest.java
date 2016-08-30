package de.interoberlin.merlot_android.model.service;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.Test;

import de.interoberlin.merlot_android.model.parser.DataPackage;

public class BaseServiceTest {
    @Test
    public void testPlainValue () {
        DataPackage data = new Gson().fromJson("", DataPackage.class);
        Assert.assertEquals(null, data);
    }
}
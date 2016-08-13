package de.interoberlin.merlot_android.model.mapping.functions;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Locale;


public class IFunctionDeserializer implements JsonDeserializer<IFunction> {

    public IFunction deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {

        String type = json.getAsJsonObject().get("type").getAsString();
        Class c = EFunctionType.valueOf(type.toUpperCase(Locale.GERMAN)).getC();

        if (c == null)
            throw new RuntimeException("Unknown type: " + type);
        return context.deserialize(json, c);
    }
}

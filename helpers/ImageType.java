package com.bosonshiggs.extendedcanvas.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum ImageType implements OptionList<String> {
	PNG("png"),
    JPEG("jpeg"),
    WEBP("webp");

    private String value;

    ImageType(String value) {
        this.value = value;
    }

    public String toUnderlyingValue() {
        return value;
    }

    private static final Map<String, ImageType> lookup = new HashMap<>();

    static {
        for (ImageType val : ImageType.values()) {
            lookup.put(val.toUnderlyingValue(), val);
        }
    }

    public static ImageType fromUnderlyingValue(String value) {
        return lookup.get(value);
    }
}
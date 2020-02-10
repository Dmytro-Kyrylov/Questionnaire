package com.kyrylov.questionnaire.web.util.helpers;

import java.util.HashMap;
import java.util.Map;

public class DialogHelper {

    //enums.properties
    public enum DialogResult {
        SUCCESS, CANCEL, ERROR
    }

    public static Map<String, Object> getDialogOptions(boolean modal, boolean draggable, boolean resizable,
                                                       int width, int height) {
        Map<String, Object> options = new HashMap<>();
        options.put("modal", modal);
        options.put("width", width);
        options.put("height", height);
        options.put("draggable", draggable);
        options.put("resizable", resizable);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        return options;
    }
}

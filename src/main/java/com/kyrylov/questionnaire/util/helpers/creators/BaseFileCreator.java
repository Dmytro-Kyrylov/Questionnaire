package com.kyrylov.questionnaire.util.helpers.creators;

import com.kyrylov.questionnaire.util.helpers.ResourceHelper;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Locale;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseFileCreator {

    private final Locale locale;

    protected BaseFileCreator(Locale locale) {
        this.locale = locale;
    }

    protected String resource(String resource) {
        return ResourceHelper.getFileCreatingResource(resource, getLocale());
    }

}

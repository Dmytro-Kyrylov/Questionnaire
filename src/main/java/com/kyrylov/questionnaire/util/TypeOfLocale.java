package com.kyrylov.questionnaire.util;

import lombok.Getter;

import java.util.Locale;

/**
 * Types of application locale
 *
 * @author Dmitrii
 */
@Getter
public enum TypeOfLocale {
    EN("en", "english"), RU("ru", "russian");

    private String region;
    private String language;

    TypeOfLocale(String language, String region) {
        this.language = language;
        this.region = region;
    }

    public static TypeOfLocale getByCode(String localeCode) {
        if (localeCode != null) {
            for (TypeOfLocale locale : TypeOfLocale.values()) {
                if (locale.getLanguage().equalsIgnoreCase(localeCode)) {
                    return locale;
                }
            }
        }
        return TypeOfLocale.EN;
    }

    public Locale createLocale() {
        return new Locale(this.getLanguage(), this.getRegion());
    }
}

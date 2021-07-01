package com.qualaroo.internal.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Survey implements Serializable {
    private final int id;
    private final String name;
    private final String canonicalName;
    private final boolean active;
    private final Spec spec;
    private final String type;

    @VisibleForTesting Survey(int id, String name, String canonicalName, boolean active, Spec spec, String type) {
        this.id = id;
        this.name = name;
        this.canonicalName = canonicalName;
        this.active = active;
        this.spec = spec;
        this.type = type;
    }

    @SuppressWarnings("unused") private Survey() {
        this.id = 0;
        this.name = null;
        this.canonicalName = null;
        this.active = false;
        this.spec = null;
        this.type = null;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String canonicalName() {
        return canonicalName;
    }

    public boolean isActive() {
        return active;
    }

    public Spec spec() {
        return spec;
    }

    public String type() {
        return type;
    }

    public Survey copy(Spec spec) {
        return new Survey(id, name, canonicalName, active, spec, type);
    }

    @Override public String toString() {
        return String.format(Locale.ROOT, "%s (%d)", name, id);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Survey survey = (Survey) o;

        return id == survey.id;
    }

    @Override public int hashCode() {
        return id;
    }

    public static final class Spec implements Serializable {
        private final RequireMap requireMap;
        private final OptionMap optionMap;
        private final Map<Language, List<Question>> questionList;
        private final Map<Language, List<Message>> msgScreenList;
        private final Map<Language, List<QScreen>> qscreenList;
        private final Map<Language, Node> startMap;
        private final List<Language> surveyVariations;

        public RequireMap requireMap() {
            return requireMap;
        }

        public Map<Language, List<Question>> questionList() {
            return questionList;
        }

        public Map<Language, List<Message>> msgScreenList() {
            return msgScreenList;
        }

        public Map<Language, List<QScreen>> qscreenList() {
            return qscreenList;
        }

        public Map<Language, Node> startMap() {
            return startMap;
        }

        public List<Language> surveyVariations() {
            return surveyVariations;
        }

        public OptionMap optionMap() {
            return optionMap;
        }

        @VisibleForTesting Spec(RequireMap requireMap, OptionMap optionMap, Map<Language, List<Question>> questionList,
                                Map<Language, List<Message>> msgScreenList, Map<Language, List<QScreen>> qscreenList,
                                Map<Language, Node> startMap, List<Language> surveyVariations) {
            this.requireMap = requireMap;
            this.optionMap = optionMap;
            this.questionList = questionList;
            this.msgScreenList = msgScreenList;
            this.qscreenList = qscreenList;
            this.startMap = startMap;
            this.surveyVariations = surveyVariations;
        }

        @SuppressWarnings("unused") private Spec() {
            this.requireMap = null;
            this.optionMap = null;
            this.questionList = null;
            this.msgScreenList = null;
            this.qscreenList = null;
            this.startMap = null;
            this.surveyVariations = null;
        }

        public Spec copy(Map<Language, List<Question>> questionList, Map<Language, List<Message>> msgScreenList, Map<Language, List<QScreen>> qscreenList) {
            return new Spec(requireMap, optionMap, questionList, msgScreenList, qscreenList, startMap, surveyVariations);
        }
    }

    public static final class RequireMap implements Serializable {
        private final List<String> deviceTypeList;
        private final boolean isPersistent;
        private final boolean isOneShot;
        private final String customMap;
        //whether identity of the user should be known or not
        private final String wantUserStr;
        private final Integer samplePercent;

        public String wantUserStr() {
            return wantUserStr;
        }

        public List<String> deviceTypeList() {
            return deviceTypeList;
        }

        public boolean isPersistent() {
            return isPersistent;
        }

        public boolean isOneShot() {
            return isOneShot;
        }

        public String customMap() {
            return customMap;
        }

        @Nullable public Integer samplePercent() {
            return samplePercent;
        }

        @VisibleForTesting RequireMap(List<String> deviceTypeList, boolean isPersistent, boolean isOneShot,
                                      String customMap, String wantUserStr, Integer samplePercent) {
            this.deviceTypeList = deviceTypeList;
            this.isPersistent = isPersistent;
            this.isOneShot = isOneShot;
            this.customMap = customMap;
            this.wantUserStr = wantUserStr;
            this.samplePercent = samplePercent;
        }

        @SuppressWarnings("unused") private RequireMap() {
            this.deviceTypeList = null;
            this.isPersistent = false;
            this.isOneShot = false;
            this.customMap = null;
            this.wantUserStr = null;
            this.samplePercent = null;
        }
    }

    public static final class OptionMap implements Serializable {
        private final ColorThemeMap colorThemeMap;
        private final boolean mandatory;
        private final boolean showFullScreen;
        private final String logoUrl;
        private final String progressBar;

        public ColorThemeMap colorThemeMap() {
            return colorThemeMap;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public boolean isShowFullScreen() {
            return showFullScreen;
        }

        public String progressBar() {
            return progressBar;
        }

        @Nullable public String logoUrl() {
            return logoUrl;
        }

        @VisibleForTesting OptionMap(ColorThemeMap colorThemeMap, boolean mandatory, boolean showFullScreen,
                                     String logoUrl, String progressBar) {
            this.colorThemeMap = colorThemeMap;
            this.mandatory = mandatory;
            this.showFullScreen = showFullScreen;
            this.logoUrl = logoUrl;
            this.progressBar = progressBar;
        }

        @SuppressWarnings("unused") private OptionMap() {
            this.colorThemeMap = null;
            this.mandatory = false;
            this.showFullScreen = false;
            this.logoUrl = null;
            this.progressBar = null;
        }
    }

}

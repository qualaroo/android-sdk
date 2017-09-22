package com.qualaroo.internal.model;

import android.support.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Survey implements Serializable {
    private int id;
    private String name;
    private String canonicalName;
    private boolean active;
    private Spec spec;

    @VisibleForTesting Survey(int id, String name, String canonicalName, boolean active, Spec spec) {
        this.id = id;
        this.name = name;
        this.canonicalName = canonicalName;
        this.active = active;
        this.spec = spec;
    }

    Survey() {
        //deserializing with gson requires a default constructor
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
        private RequireMap requireMap;
        private OptionMap optionMap;
        private Map<Language, List<Question>> questionList;
        private Map<Language, List<Message>> msgScreenList;
        private Map<Language, Node> startMap;
        private List<Language> surveyVariations;

        public RequireMap requireMap() {
            return requireMap;
        }

        public Map<Language, List<Question>> questionList() {
            return questionList;
        }

        public Map<Language, List<Message>> msgScreenList() {
            return msgScreenList;
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

        @VisibleForTesting Spec(RequireMap requireMap, OptionMap optionMap, Map<Language, List<Question>> questionList, Map<Language, List<Message>> msgScreenList, Map<Language, Node> startMap, List<Language> surveyVariations) {
            this.requireMap = requireMap;
            this.optionMap = optionMap;
            this.questionList = questionList;
            this.msgScreenList = msgScreenList;
            this.startMap = startMap;
            this.surveyVariations = surveyVariations;
        }

        Spec() {
            //deserializing with gson requires a default constructor
        }
    }

    public static final class RequireMap implements Serializable {
        private List<String> deviceTypeList;
        private boolean isPersistent;
        private boolean isOneShot;
        private String customMap;

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

        @VisibleForTesting RequireMap(List<String> deviceTypeList, boolean isPersistent, boolean isOneShot, String customMap) {
            this.deviceTypeList = deviceTypeList;
            this.isPersistent = isPersistent;
            this.isOneShot = isOneShot;
            this.customMap = customMap;
        }

        RequireMap() {
            //deserializing with gson requires a default constructor
        }
    }

    public static final class OptionMap implements Serializable {
        private ColorThemeMap colorThemeMap;
        private boolean mandatory;
        private boolean showFullScreen;

        public ColorThemeMap colorThemeMap() {
            return colorThemeMap;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public boolean isShowFullScreen() {
            return showFullScreen;
        }

        @VisibleForTesting OptionMap(ColorThemeMap colorThemeMap, boolean mandatory, boolean showFullScreen) {
            this.colorThemeMap = colorThemeMap;
            this.mandatory = mandatory;
            this.showFullScreen = showFullScreen;
        }

        OptionMap() {
            //deserializing with gson requires a default constructor
        }
    }

}

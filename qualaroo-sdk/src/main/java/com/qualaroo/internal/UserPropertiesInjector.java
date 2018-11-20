/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UserPropertiesInjector {

    private static final Pattern USER_PROPERTY_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    private final UserInfo userInfo;

    public UserPropertiesInjector(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean canInjectAllProperties(Survey survey) {
        return new PropertiesMatcher(userInfo).matches(survey);
    }

    public Survey injectCustomProperties(Survey survey, @Nullable Language language) {
        Map<String, String> userProperties = userInfo.getUserProperties();

        Map<Language, List<Question>> questionList = new HashMap<>(survey.spec().questionList());
        List<Question> questions = questionList.get(language);
        if (questions != null) {
            questionList.put(language, injectQuestions(questions, userProperties));
        }

        Map<Language, List<Message>> msgScreenList = new HashMap<>(survey.spec().msgScreenList());
        List<Message> messages = msgScreenList.get(language);
        if (messages != null) {
            msgScreenList.put(language, injectMessages(messages, userProperties));
        }

        Map<Language, List<QScreen>> qScreenList = new HashMap<>(survey.spec().qscreenList());
        List<QScreen> qScreens = qScreenList.get(language);
        if (qScreens != null) {
            qScreenList.put(language, injectQscreens(qScreens, userProperties));
        }

        Survey.Spec spec = survey.spec().copy(questionList, msgScreenList, qScreenList);
        return survey.copy(spec);
    }

    private List<QScreen> injectQscreens(List<QScreen> qScreens, Map<String, String> userProperties) {
        return injectDescriptions(qScreens, userProperties, new TextExtractor<QScreen>() {
            @Nullable @Override public String getText(QScreen qScreen) {
                return qScreen.description();
            }
        }, new ContentConverter<QScreen>() {
            @Override public QScreen replace(QScreen item, String... args) {
                return item.copy(args[0]);
            }
        });
    }

    private List<Message> injectMessages(List<Message> messages, Map<String, String> userProperties) {
        return injectDescriptions(messages, userProperties, new TextExtractor<Message>() {
            @Nullable @Override public String getText(Message message) {
                return message.description();
            }
        }, new ContentConverter<Message>() {
            @Override public Message replace(Message item, String... args) {
                return item.copy(args[0]);
            }
        });
    }

    private List<Question> injectQuestions(final List<Question> questions, Map<String, String> userProperties) {
        return injectTitlesAndDescriptions(questions, userProperties, new TextExtractor<Question>() {
            @Nullable @Override public String getText(Question question) {
                return question.title();
            }
        }, new TextExtractor<Question>() {
            @Nullable @Override public String getText(Question question) {
                return question.description();
            }
        }, new ContentConverter<Question>() {
            @Override public Question replace(Question item, String... args) {
                return item.copy(args[0], args[1]);
            }
        });
    }

    private <T> List<T> injectDescriptions(List<T> origin, Map<String, String> userProperties,
                                           TextExtractor<T> descriptionExtractor, ContentConverter<T> converter) {
        List<T> result = new ArrayList<>(origin.size());
        for (T item : origin) {
            String description = fillWithUserProperties(userProperties, descriptionExtractor, item);
            result.add(converter.replace(item, description));
        }
        return result;
    }

    private <T> List<T> injectTitlesAndDescriptions(List<T> origin, Map<String, String> userProperties,
                                                    TextExtractor<T> titleExtractor,
                                                    TextExtractor<T> descriptionExtractor,
                                                    ContentConverter<T> converter) {
        List<T> result = new ArrayList<>(origin.size());
        for (T item : origin) {
            String title = fillWithUserProperties(userProperties, titleExtractor, item);
            String description = fillWithUserProperties(userProperties, descriptionExtractor, item);
            result.add(converter.replace(item, title, description));
        }
        return result;
    }

    private <T> String fillWithUserProperties(Map<String, String> userProperties,
                                              TextExtractor<T> textExtractor, T item) {
        String text = textExtractor.getText(item);
        Matcher matcher = propertyMatcher(text);
        while (matcher.find()) {
            String key = matcher.group(1);
            text = replaceText(text, matcher.group(), userProperties.get(key));
        }
        return text;
    }

    private static Matcher propertyMatcher(@Nullable String text) {
        if (text == null) {
            return USER_PROPERTY_PATTERN.matcher("");
        }
        return USER_PROPERTY_PATTERN.matcher(text);
    }

    private static String replaceText(String origin, String target, @Nullable String replacement) {
        if (replacement == null) {
            return origin.replace(target, "");
        }
        return origin.replace(target, replacement);
    }

    private interface TextExtractor<T> {
        @Nullable String getText(T t);
    }

    private interface ContentConverter<T> {
        T replace(T item, String... args);
    }

    private static class PropertiesMatcher {

        private final UserInfo userInfo;

        PropertiesMatcher(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        boolean matches(Survey survey) {
            Set<String> userPropertyKeys = userInfo.getUserProperties().keySet();
            return matchesQuestions(survey, userPropertyKeys)
                    && matchesMessages(survey, userPropertyKeys)
                    && matchesQscreens(survey, userPropertyKeys);
        }

        private boolean matchesQuestions(Survey survey, Set<String> userPropertyKeys) {
            return matches(survey.spec().questionList(), userPropertyKeys, new TextExtractor<Question>() {
                @Override public String getText(Question question) {
                    return question.title() + " " + question.description();
                }
            });
        }

        private boolean matchesMessages(Survey survey, Set<String> userPropertyKeys) {
            return matches(survey.spec().msgScreenList(), userPropertyKeys, new TextExtractor<Message>() {
                @Override public String getText(Message message) {
                    return message.description();
                }
            });
        }

        private boolean matchesQscreens(Survey survey, Set<String> userPropertyKeys) {
            return matches(survey.spec().qscreenList(), userPropertyKeys, new TextExtractor<QScreen>() {
                @Nullable @Override public String getText(QScreen qScreen) {
                    return qScreen.description();
                }
            });
        }

        private <T> boolean matches(Map<Language, List<T>> itemsMap, Set<String> userPropertyKeys,
                                    TextExtractor<T> textExtractor) {
            for (Map.Entry<Language, List<T>> languageListEntry : itemsMap.entrySet()) {
                List<T> items = languageListEntry.getValue();
                for (T item : items) {
                    String text = textExtractor.getText(item);
                    Matcher matcher = propertyMatcher(text);
                    while (matcher.find()) {
                        String key = matcher.group(1);
                        if (!userPropertyKeys.contains(key)) {
                            reportMissingProperty(key);
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private static void reportMissingProperty(String property) {
            QualarooLogger.debug("%s property is not set", property);
        }
    }
}

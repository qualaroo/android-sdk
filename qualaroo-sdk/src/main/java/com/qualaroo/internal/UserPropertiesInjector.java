package com.qualaroo.internal;

import android.support.annotation.Nullable;

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

public class UserPropertiesInjector extends SurveySpecMatcher {

    private static final Pattern USER_PROPERTY_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    private final UserInfo userInfo;

    public UserPropertiesInjector(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Survey injectCustomProperties(Survey survey, Language language) {
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

    private List<Question> injectQuestions(List<Question> questions, Map<String, String> userProperties) {
        List<Question> result = new ArrayList<>(questions.size());
        for (Question question : questions) {
            String title = question.title();
            Matcher matcher = propertyMatcher(title);
            while (matcher.find()) {
                String key = matcher.group(1);
                title = title.replace(matcher.group(), getUserPropertySafely(userProperties, key));
            }
            String description = question.description();
            matcher = propertyMatcher(description);
            while (matcher.find()) {
                String key = matcher.group(1);
                description = description.replace(matcher.group(), getUserPropertySafely(userProperties, key));
            }
            result.add(question.copy(title, description));
        }
        return result;
    }

    private List<Message> injectMessages(List<Message> messages, Map<String, String> userProperties) {
        List<Message> result = new ArrayList<>(messages.size());
        for (Message message : messages) {
            String description = message.description();
            Matcher matcher = propertyMatcher(description);
            while (matcher.find()) {
                String key = matcher.group(1);
                description = description.replace(matcher.group(), getUserPropertySafely(userProperties, key));
            }
            result.add(message.copy(description));
        }
        return result;
    }

    private List<QScreen> injectQscreens(List<QScreen> qScreens, Map<String, String> userProperties) {
        List<QScreen> result = new ArrayList<>(qScreens.size());
        for (QScreen qScreen: qScreens) {
            String description = qScreen.description();
            Matcher matcher = propertyMatcher(description);
            while (matcher.find()) {
                String key = matcher.group(1);
                description = description.replace(matcher.group(), getUserPropertySafely(userProperties, key));
            }
            result.add(qScreen.copy(description));
        }
        return result;
    }

    private String getUserPropertySafely(Map<String, String> userProperties, String key) {
        String value = userProperties.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override boolean matches(Survey survey) {
        Set<String> userPropertyKeys = userInfo.getUserProperties().keySet();
        return allQuestionsMatch(survey.spec().questionList(), userPropertyKeys)
                && allMessagesMatch(survey.spec().msgScreenList(), userPropertyKeys)
                && allQscreensMatch(survey.spec().qscreenList(), userPropertyKeys);
    }

    private boolean allMessagesMatch(Map<Language, List<Message>> messagesMap, Set<String> userPropertyKeys) {
        for (Map.Entry<Language, List<Message>> languageListEntry : messagesMap.entrySet()) {
            List<Message> messages = languageListEntry.getValue();
            for (Message message : messages) {
                Matcher matcher = propertyMatcher(message.description());
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

    private boolean allQuestionsMatch(Map<Language, List<Question>> questionsMap, Set<String> userPropertyKeys) {
        for (Map.Entry<Language, List<Question>> languageListEntry : questionsMap.entrySet()) {
            List<Question> questions = languageListEntry.getValue();
            for (Question question : questions) {
                Matcher matcher = propertyMatcher(question.title());
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if (!userPropertyKeys.contains(key)) {
                        reportMissingProperty(key);
                        return false;
                    }
                }
                matcher = propertyMatcher(question.description());
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

    private boolean allQscreensMatch(Map<Language, List<QScreen>> qScreensMap, Set<String> userPropertyKeys) {
        for (Map.Entry<Language, List<QScreen>> languageListEntry : qScreensMap.entrySet()) {
            List<QScreen> qScreens = languageListEntry.getValue();
            for (QScreen qScreen: qScreens) {
                Matcher matcher = propertyMatcher(qScreen.description());
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

    private Matcher propertyMatcher(@Nullable String text) {
        if (text == null) {
            return USER_PROPERTY_PATTERN.matcher("");
        }
        return USER_PROPERTY_PATTERN.matcher(text);
    }

    private void reportMissingProperty(String property) {
        QualarooLogger.debug("%s property is not set", property);
    }


}

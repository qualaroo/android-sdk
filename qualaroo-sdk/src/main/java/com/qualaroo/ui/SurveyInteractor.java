package com.qualaroo.ui;

import android.support.annotation.Nullable;

import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Node;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.storage.LocalStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class SurveyInteractor {

    public interface EventsObserver {
        void showQuestion(Question question);
        void showMessage(Message message);
        void closeSurvey();
    }

    private final Survey survey;
    private final LocalStorage localStorage;
    private final ReportManager reportManager;
    private final Language preferredLanguage;
    private final Executor backgroundExecutor;
    private final Executor uiExecutor;

    private final List<Question> questions;
    private final List<Message> messages;

    private EventsObserver eventsObserver = new StubEventsObserver();

    SurveyInteractor(Survey survey, LocalStorage localStorage, ReportManager reportManager, Language preferredLanguage, Executor backgroundExecutor, Executor uiExecutor) {
        this.survey = survey;
        this.localStorage = localStorage;
        this.reportManager = reportManager;
        this.preferredLanguage = preferredLanguage;
        this.backgroundExecutor = backgroundExecutor;
        this.uiExecutor = uiExecutor;
        this.questions = preferredLanguageOrDefault(survey.spec().questionList());
        this.messages = preferredLanguageOrDefault(survey.spec().msgScreenList());
    }

    public void startSurvey() {
        Node startNode = selectStartNode(survey.spec().startMap());
        followNode(startNode);
    }

    public void questionAnsweredWithText(Question question, String answer) {
        reportManager.recordTextAnswer(survey, question, answer);
        Node nextNode = findNextNode(question, Collections.<Answer>emptyList());
        followNode(nextNode);
    }

    public void questionAnswered(Question question, List<Answer> selectedAnswers) {
        reportManager.recordAnswer(survey, question, selectedAnswers);
        Node nextNode = findNextNode(question, selectedAnswers);
        followNode(nextNode);
    }

    private Node findNextNode(Question question, List<Answer> selectedAnswers) {
        Node nextNode = null;
        for (Answer answer : selectedAnswers) {
            if (answer.nextMap() != null) {
                nextNode = answer.nextMap();
                break;
            }
        }
        if (nextNode == null) {
            nextNode = question.nextMap();
        }
        return nextNode;
    }

    private void followNode(@Nullable Node node) {
        if (node == null) {
            eventsObserver.closeSurvey();
        } else if (node.nodeType().equals("message")) {
            eventsObserver.showMessage(findMessageById(node.id(), messages));
        } else if (node.nodeType().equals("question")) {
            eventsObserver.showQuestion(findQuestionById(node.id(), questions));
        }
    }

    public void messageConfirmed(Message message) {
        eventsObserver.closeSurvey();
    }

    private static Question findQuestionById(int id, List<Question> questions) {
        for (Question question : questions) {
            if (question.id() == id) {
                return  question;
            }
        }
        return null;
    }

    private static Message findMessageById(int id, List<Message> messages) {
        for (Message message : messages) {
            if (message.id() == id) {
                return message;
            }
        }
        return null;
    }

    void registerObserver(EventsObserver eventsObserver) {
        this.eventsObserver = UiThreadEventsObserverDelegate.wrap(eventsObserver, uiExecutor);
    }

    void unregisterObserver() {
        this.eventsObserver = new StubEventsObserver();
    }

    void stopSurvey() {
        this.eventsObserver.closeSurvey();
    }

    private static class UiThreadEventsObserverDelegate implements EventsObserver {

        static EventsObserver wrap(EventsObserver eventsObserver, Executor executor) {
            return new UiThreadEventsObserverDelegate(eventsObserver, executor);
        }

        private final EventsObserver eventsObserver;
        private final Executor executor;

        private UiThreadEventsObserverDelegate(EventsObserver eventsObserver, Executor executor) {
            this.eventsObserver = eventsObserver;
            this.executor = executor;
        }

        @Override public void showQuestion(final Question question) {
            executor.execute(new Runnable() {
                @Override public void run() {
                    eventsObserver.showQuestion(question);
                }
            });
        }

        @Override public void showMessage(final Message message) {
            executor.execute(new Runnable() {
                @Override public void run() {
                    eventsObserver.showMessage(message);
                }
            });
        }

        @Override public void closeSurvey() {
            executor.execute(new Runnable() {
                @Override public void run() {
                    eventsObserver.closeSurvey();
                }
            });
        }
    }

    private static class StubEventsObserver implements EventsObserver {
        @Override public void showQuestion(Question question) {}
        @Override public void showMessage(Message message) {}
        @Override public void closeSurvey() {}
    }

    private <T> List<T> preferredLanguageOrDefault(Map<Language, List<T>> map) {
        if (map.isEmpty()) {
            return Collections.emptyList();
        }
        if (map.containsKey(preferredLanguage)) {
            return map.get(preferredLanguage);
        }
        Language defaultLanguage = new Language("en");
        if (map.containsKey(defaultLanguage)) {
            return map.get(defaultLanguage);
        }
        Language firstLanguage = survey.spec().surveyVariations().get(0);
        return map.get(firstLanguage);
    }

    private Node selectStartNode(Map<Language, Node> map) {
        if (map.isEmpty()) {
            return null;
        }
        if (map.containsKey(preferredLanguage)) {
            return map.get(preferredLanguage);
        }
        Language defaultLanguage = new Language("en");
        if (map.containsKey(defaultLanguage)) {
            return map.get(defaultLanguage);
        }
        Language firstLanguage = survey.spec().surveyVariations().get(0);
        return map.get(firstLanguage);
    }

}

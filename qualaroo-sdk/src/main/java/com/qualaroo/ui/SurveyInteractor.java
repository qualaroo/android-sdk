package com.qualaroo.ui;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.SparseArrayCompat;

import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Node;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.storage.LocalStorage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
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
    private final SparseArrayCompat<Question> questions;
    private final SparseArrayCompat<Message> messages;

    private Node currentNode;
    private EventsObserver eventsObserver = new StubEventsObserver();

    SurveyInteractor(Survey survey, LocalStorage localStorage, ReportManager reportManager, Language preferredLanguage, Executor backgroundExecutor, Executor uiExecutor) {
        this.survey = survey;
        this.localStorage = localStorage;
        this.reportManager = reportManager;
        this.preferredLanguage = preferredLanguage;
        this.backgroundExecutor = backgroundExecutor;
        this.uiExecutor = uiExecutor;
        this.questions = prepareQuestions();
        List<Message> messages = preferredLanguageOrDefault(survey.spec().msgScreenList());
        this.messages = convertMessagesToSparseArray(messages);
    }

    public void displaySurvey() {
        if (currentNode == null) {
            reportManager.recordImpression(survey);
            markSurveyAsSeen();
            Node startNode = selectStartNode(survey.spec().startMap());
            followNode(startNode);
        } else {
            followNode(currentNode);
        }
    }

    public void questionAnsweredWithText(Question question, String answer) {
        reportManager.recordTextAnswer(survey, question, answer);
        Node nextNode = findNextNode(question.id(), Collections.<Answer>emptyList());
        followNode(nextNode);
    }

    public void questionAnswered(Question question, List<Answer> selectedAnswers) {
        reportManager.recordAnswer(survey, question, selectedAnswers);
        Node nextNode = findNextNode(question.id(), selectedAnswers);
        followNode(nextNode);
    }

    private Node findNextNode(int questionId, List<Answer> selectedAnswers) {
        Node nextNode = null;
        //TODO: Question and Answer objects provided by a presenter are not trusted and local copies are used instead.
        //This was done to avoid having to pass fully built objects in tests. Could be fixed by either passing simple int ids
        //or by changing the way we acquired objects in tests (directly from Survey model instead of creating new ones via TestModel.kt helper class)
        Question question = questions.get(questionId);
        for (Answer answer : selectedAnswers) {
            int index = question.answerList().indexOf(answer);
            Answer storedAnswer = question.answerList().get(index);
            if (storedAnswer.nextMap() != null) {
                nextNode = storedAnswer.nextMap();
                break;
            }
        }
        if (nextNode == null) {
            nextNode = question.nextMap();
        }
        return nextNode;
    }

    private void followNode(@Nullable Node node) {
        this.currentNode = node;
        if (node == null) {
            markSurveyAsFinished();
            eventsObserver.closeSurvey();
        } else if (node.nodeType().equals("message")) {
            eventsObserver.showMessage(messages.get(node.id()));
        } else if (node.nodeType().equals("question")) {
            eventsObserver.showQuestion(questions.get(node.id()));
        }
    }

    public void messageConfirmed(Message message) {
        eventsObserver.closeSurvey();
    }

    void registerObserver(EventsObserver eventsObserver) {
        this.eventsObserver = UiThreadEventsObserverDelegate.wrap(eventsObserver, uiExecutor);
    }

    void unregisterObserver() {
        this.eventsObserver = new StubEventsObserver();
    }

    void stopSurvey() {
        if (!survey.spec().optionMap().isMandatory()) {
            this.eventsObserver.closeSurvey();
        }
    }

    private void markSurveyAsSeen() {
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                localStorage.markSurveyAsSeen(survey);
            }
        });
    }

    private void markSurveyAsFinished() {
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                localStorage.markSurveyFinished(survey);
            }
        });
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

    private SparseArrayCompat<Question> prepareQuestions() {
        List<Question> originalQuestions = preferredLanguageOrDefault(survey.spec().questionList());
        SparseArrayCompat<Question> result = new SparseArrayCompat<>();
        for (Question originalQuestion : originalQuestions) {
            if (originalQuestion.enableRandom()) {
                final LinkedList<Answer> answerList = new LinkedList<>(originalQuestion.answerList());
                Answer anchoredLastAnswer = null;
                if (originalQuestion.anchorLast()) {
                    anchoredLastAnswer = answerList.removeLast();
                }
                Collections.shuffle(answerList);
                if (anchoredLastAnswer != null) {
                    answerList.addLast(anchoredLastAnswer);
                }
                result.append(originalQuestion.id(), originalQuestion.mutateWith(answerList));
            } else {
                result.append(originalQuestion.id(), originalQuestion);
            }
        }
        return result;
    }

    private SparseArrayCompat<Message> convertMessagesToSparseArray(List<Message> messages) {
        final SparseArrayCompat<Message> result = new SparseArrayCompat<>(messages.size());
        for (Message message : messages) {
            result.append(message.id(), message);
        }
        return result;
    }

    private SparseArrayCompat<Question> prepareQuestions(List<Question> questions) {
        final SparseArrayCompat<Question> result = new SparseArrayCompat<>(questions.size());
        for (Question question : questions) {
            result.append(question.id(), question);
        }
        return result;
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

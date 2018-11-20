/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.LongSparseArray;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.event.SurveyEvent;
import com.qualaroo.internal.event.SurveyEventPublisher;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.MessageType;
import com.qualaroo.internal.model.Node;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.internal.progress.StepsLeftCalculator;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.util.LanguageHelper;
import com.qualaroo.util.Shuffler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SurveyInteractor {

    public interface EventsObserver {
        void showQuestion(Question question);
        void showMessage(Message message);
        void showLeadGen(QScreen qscreen, List<Question> questions);
        void setProgress(float progress);
        void openUri(@NonNull String stringUri);
        void closeSurvey();
    }

    private final Survey survey;
    private final LocalStorage localStorage;
    private final ReportManager reportManager;
    @Nullable private final Language preferredLanguage;
    private final Shuffler shuffler;
    private final SurveyEventPublisher surveyEventPublisher;
    private final Executor backgroundExecutor;
    private final Executor uiExecutor;
    private final LongSparseArray<Question> questions;
    private final LongSparseArray<Message> messages;
    private final LongSparseArray<QScreen> qscreens;
    private final StepsLeftCalculator stepsLeftCalculator;

    private Node currentNode;
    private EventsObserver eventsObserver = new StubEventsObserver();
    private AtomicBoolean isStoppingSurvey = new AtomicBoolean(false);

    private int numOfStepsDisplayed;

    SurveyInteractor(Survey survey, LocalStorage localStorage, ReportManager reportManager, @Nullable Language preferredLanguage, Shuffler shuffler, SurveyEventPublisher surveyEventPublisher, Executor backgroundExecutor, Executor uiExecutor) {

        this.survey = survey;
        this.localStorage = localStorage;
        this.reportManager = reportManager;
        this.preferredLanguage = preferredLanguage;
        this.shuffler = shuffler;
        this.surveyEventPublisher = surveyEventPublisher;
        this.backgroundExecutor = backgroundExecutor;
        this.uiExecutor = uiExecutor;
        this.questions = prepareQuestions();
        this.messages = prepareData(survey.spec().msgScreenList(), new IdExtractor<Message>() {
            @Override public long getId(Message message) {
                return message.id();
            }
        });
        this.qscreens = prepareData(survey.spec().qscreenList(), new IdExtractor<QScreen>() {
            @Override public long getId(QScreen qScreen) {
                return qScreen.id();
            }
        });
        this.stepsLeftCalculator = new StepsLeftCalculator(questions, messages, qscreens);
    }

    public void displaySurvey() {
        if (currentNode == null) {
            reportManager.reportImpression(survey);
            markSurveyAsSeen();
            Node startNode = selectStartNode(survey.spec().startMap());
            followNode(startNode);
        } else {
            followNode(currentNode);
        }
    }

    public void onResponse(UserResponse userResponse) {
        reportManager.reportUserResponse(survey, userResponse);
        Node nextNode = findNextNode(userResponse);
        followNode(nextNode);
    }

    public void onLeadGenResponse(List<UserResponse> userResponse) {
        reportManager.reportUserResponse(survey, userResponse);
        Node nextNode = qscreens.get(currentNode.id()).nextMap();
        followNode(nextNode);
    }

    @Nullable private Node findNextNode(UserResponse userResponse) {
        Question question = questions.get(userResponse.questionId());
        for (UserResponse.Entry entry : userResponse.entries()) {
            for (Answer answer : question.answerList()) {
                if (entry.answerId() != null && answer.id() == entry.answerId()) {
                    if (answer.nextMap() != null) {
                        return answer.nextMap();
                    }
                }
            }
        }
        return question.nextMap();
    }

    private void followNode(@Nullable Node node) {
        if (currentNode != node) {
            this.numOfStepsDisplayed++;
        }
        this.currentNode = node;
        if (currentNode != null) {
            stepsLeftCalculator.setCurrentStep(currentNode.id(), currentNode.nodeType());
            int stepsLeft = stepsLeftCalculator.getStepsLeft();
            float progress = (float) numOfStepsDisplayed / (numOfStepsDisplayed + stepsLeft);
            eventsObserver.setProgress(progress);
            QualarooLogger.debug("Steps left: " + stepsLeftCalculator.getStepsLeft());
        }
        if (node == null) {
            markSurveyAsFinished();
            eventsObserver.closeSurvey();
        } else if (node.nodeType().equals("message")) {
            eventsObserver.showMessage(messages.get(node.id()));
        } else if (node.nodeType().equals("question")) {
            eventsObserver.showQuestion(questions.get(node.id()));
        } else if (node.nodeType().equals("qscreen")) {
            QScreen leadGen = qscreens.get(node.id());
            List<Question> leadGenQuestions = new ArrayList<>(leadGen.questionList().size());
            for (Long questionId : leadGen.questionList()) {
                leadGenQuestions.add(questions.get(questionId));
            }
            eventsObserver.showLeadGen(leadGen, leadGenQuestions);
        }
    }

    @MainThread
    public void messageConfirmed(Message message) {
        if (message.type() == MessageType.CALL_TO_ACTION) {
            eventsObserver.openUri(message.ctaMap().uri());
        }
        closeSurvey();
    }

    @MainThread
    void registerObserver(EventsObserver eventsObserver) {
        this.eventsObserver = UiThreadEventsObserverDelegate.wrap(eventsObserver, uiExecutor);
    }

    @MainThread
    void unregisterObserver() {
        this.eventsObserver = new StubEventsObserver();
    }

    @MainThread
    void requestSurveyToStop() {
        if (!survey.spec().optionMap().isMandatory()) {
            closeSurvey();
        }
    }

    private void closeSurvey() {
        if ("message".equals(currentNode.nodeType())) {
            markSurveyAsFinished();
        } else {
            surveyEventPublisher.publish(SurveyEvent.dismissed(survey.canonicalName()));
        }
        if (isStoppingSurvey.compareAndSet(false, true)) {
            this.eventsObserver.closeSurvey();
        }
    }

    private void markSurveyAsSeen() {
        surveyEventPublisher.publish(SurveyEvent.shown(survey.canonicalName()));
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                localStorage.markSurveyAsSeen(survey);
            }
        });
    }

    private void markSurveyAsFinished() {
        surveyEventPublisher.publish(SurveyEvent.finished(survey.canonicalName()));
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

        @Override public void showLeadGen(final QScreen qScreen, final List<Question> questionList) {
            executor.execute(new Runnable() {
                @Override public void run() {
                    eventsObserver.showLeadGen(qScreen, questionList);
                }
            });
        }

        @Override public void setProgress(final float progress) {
            executor.execute(new Runnable() {
                @Override public void run() {
                    eventsObserver.setProgress(progress);
                }
            });
        }

        @Override public void openUri(@NonNull final String stringUri) {
            executor.execute(new Runnable() {
                @Override public void run() {
                    eventsObserver.openUri(stringUri);
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
        @Override public void showLeadGen(QScreen qscreen, List<Question> questions) {}
        @Override public void setProgress(float progress) {}
        @Override public void openUri(@NonNull String stringUri) {}
        @Override public void closeSurvey() {}
    }

    private LongSparseArray<Question> prepareQuestions() {
        List<Question> originalQuestions = preferredLanguageOrDefault(survey.spec().questionList());
        LongSparseArray<Question> result = new LongSparseArray<>();
        for (Question question : originalQuestions) {
            if (question.enableRandom()) {
                result.append(question.id(), shuffleAnswers(question));
            } else {
                result.append(question.id(), question);
            }
        }
        return result;
    }

    private Question shuffleAnswers(Question question) {
        int legacyLastItemsToAnchor = question.anchorLast() ? 1 : 0;
        int lastItemsToAnchor = question.anchorLastCount() == 0 ? legacyLastItemsToAnchor : question.anchorLastCount();
        final LinkedList<Answer> answerList = new LinkedList<>(question.answerList());
        final LinkedList<Answer> anchoredAnswers = new LinkedList<>();
        for (int i = 0; i < lastItemsToAnchor; i++) {
            anchoredAnswers.addFirst(answerList.removeLast());
        }
        shuffler.shuffle(answerList);
        if (anchoredAnswers.size() > 0) {
            answerList.addAll(anchoredAnswers);
        }
        return question.copy(answerList);
    }

    private <T> LongSparseArray<T> prepareData(Map<Language, List<T>> data, IdExtractor<T> extractor) {
        final List<T> dataList = preferredLanguageOrDefault(data);
        return convertToLongSparseArray(dataList, extractor);
    }

    private <T> LongSparseArray<T> convertToLongSparseArray(List<T> objects, IdExtractor<T> extractor) {
        final LongSparseArray<T> result = new LongSparseArray<>(objects.size());
        for (T object : objects) {
            long id = extractor.getId(object);
            result.append(id, object);
        }
        return result;
    }

    private <T> List<T> preferredLanguageOrDefault(Map<Language, List<T>> map) {
        if (map.isEmpty()) {
            return Collections.emptyList();
        }
        Language targetLanguage = LanguageHelper.getTargetLanguage(survey, preferredLanguage);
        return map.get(targetLanguage);
    }

    private Node selectStartNode(Map<Language, Node> map) {
        if (map.isEmpty()) {
            return null;
        }
        Language targetLanguage = LanguageHelper.getTargetLanguage(survey, preferredLanguage);
        return map.get(targetLanguage);
    }

    private interface IdExtractor<T> {
        long getId(T t);
    }

}

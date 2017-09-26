package com.qualaroo.ui.render;

import android.content.Context;
import android.view.View;

import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.QuestionType;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.ui.OnMessageConfirmedListener;

import java.util.HashMap;
import java.util.Map;

public class Renderer {

    private final Theme theme;
    private final Map<QuestionType, QuestionRenderer> questionRenderers = new HashMap<>();

    public Renderer(Theme theme) {
        this.theme = theme;
        questionRenderers.put(QuestionType.RADIO, new RadioQuestionRenderer(theme));
        questionRenderers.put(QuestionType.CHECKBOX, new CheckboxQuestionRenderer(theme));
        questionRenderers.put(QuestionType.NPS, new NpsQuestionRenderer(theme));
        questionRenderers.put(QuestionType.TEXT, new TextQuestionRenderer(theme));
        questionRenderers.put(QuestionType.UNKNOWN, new UnknownQuestionTypeRenderer(theme));
        questionRenderers.put(QuestionType.DROPDOWN, new DropdownQuestionRenderer(theme));
    }

    public View renderMessage(Context context, Message message, OnMessageConfirmedListener onMessageConfirmedListener) {
        return new MessageRenderer(theme).render(context, message, onMessageConfirmedListener);
    }

    public QuestionView renderQuestion(Context context, Question question, OnAnsweredListener onAnsweredListener) {
        return questionRenderers.get(question.type()).render(context, question, onAnsweredListener);
    }

}

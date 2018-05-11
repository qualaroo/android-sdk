package com.qualaroo.internal.model

object TestModels {

    fun survey(id: Int, name: String = "Test survey", canonicalName: String = "test_survey", active: Boolean = true,
               spec: Survey.Spec = spec(), type: String = "sdk"): Survey {
        return Survey(id, name, canonicalName, active, spec, type)
    }

    fun spec(requireMap: Survey.RequireMap = requireMap(), optionMap: Survey.OptionMap = optionMap(),
             questionList: Map<Language, List<Question>> = mapOf(),
             msgScreenList: Map<Language, List<Message>> = mapOf(), qscreenList: Map<Language, List<QScreen>> = mapOf(),
             startMap: Map<Language, Node> = mapOf(), surveyVariations: List<Language> = emptyList()): Survey.Spec {
        return Survey.Spec(requireMap, optionMap, questionList, msgScreenList, qscreenList, startMap, surveyVariations)
    }

    fun requireMap(deviceTypeList: List<String> = listOf("phone"), isPersistent: Boolean = false,
                   isOneShot: Boolean = false, customMap: String = "", wantUserStr: String? = null,
                   samplePercent: Int? = 100): Survey.RequireMap {
        return Survey.RequireMap(deviceTypeList, isPersistent, isOneShot, customMap, wantUserStr, samplePercent)
    }

    fun optionMap(colorThemeMap: ColorThemeMap = colorThemeMap(), mandatory: Boolean = false,
                  showFullScreen: Boolean = false, logoUrl: String? = null): Survey.OptionMap {
        return Survey.OptionMap(colorThemeMap, mandatory, showFullScreen, logoUrl)
    }

    fun question(id: Long, type: QuestionType = QuestionType.RADIO, title: String = "Title",
                 description: String = "some description", descriptionPlacement: String = "after",
                 answerList: List<Answer> = emptyList(), sendText: String = "send", nextMap: Node? = null,
                 npsMinLabel: String = "", npsMaxLabel: String = "", cname: String = "", disableRandom: Boolean = true,
                 anchorLast: Boolean = false, anchorLastCount: Int = 0, alwaysShowSend: Boolean = true,
                 isRequired: Boolean = false, minAnswersCount: Int = 0, maxAnswersCount: Int = 0): Question {
        return Question(id, type, title, description, descriptionPlacement, answerList, sendText, nextMap, npsMinLabel,
                npsMaxLabel, cname, disableRandom, anchorLast, anchorLastCount, alwaysShowSend, isRequired,
                minAnswersCount, maxAnswersCount)
    }

    fun answer(id: Int, title: String = "Answer", nextMap: Node? = null, explainType: String? = null): Answer {
        return Answer(id, title, nextMap, explainType)
    }

    fun qscreen(id: Long, questionList: List<Long> = emptyList(), description: String = "", sendText: String = "",
                nextMap: Node? = null): QScreen {
        return QScreen(id, questionList, description, sendText, nextMap)
    }

    fun language(value: String) = Language(value)

    fun node(id: Long, nodeType: String) = Node(id, nodeType)

    fun colorThemeMap(dimType: String = "light", backgroundColor: String = "#FFFFFF", textColor: String = "#000000",
                      buttonEnabledColor: String = "#00A000", buttonDisabledColor: String = "#A00000",
                      buttonTextEnabled: String = "#FFFFFF", buttonTextDisabled: String = "#FFFFFF",
                      uiNormal: String = "#81D4FA", uiSelected: String = "#01579B",
                      dimOpacity: Float = 1.0f): ColorThemeMap {
        return ColorThemeMap(backgroundColor, dimType, textColor, buttonEnabledColor, buttonDisabledColor,
                buttonTextEnabled, buttonTextDisabled, uiNormal, uiSelected, dimOpacity)
    }

    fun message(id: Long, type: MessageType = MessageType.REGULAR, description: String = "",
                ctaMap: Message.CtaMap = ctaMap()): Message {
        return Message(id, type, description, ctaMap)
    }

    fun ctaMap(text: String = "Go!", uri: String = "http://qualaroo.com"): Message.CtaMap {
        return Message.CtaMap(text, uri)
    }
}

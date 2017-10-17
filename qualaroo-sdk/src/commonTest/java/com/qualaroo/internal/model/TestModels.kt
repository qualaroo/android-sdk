package com.qualaroo.internal.model

object TestModels {

    fun survey(id: Int, name: String = "Test survey", canonicalName: String = "test_survey", active: Boolean = true, spec: Survey.Spec = spec(), type: String = "sdk"): Survey {
        return Survey(id, name, canonicalName, active, spec, type)
    }

    fun spec(requireMap: Survey.RequireMap = requireMap(), optionMap: Survey.OptionMap = optionMap(), questionList: Map<Language, List<Question>> = mapOf(), msgScreenList: Map<Language, List<Message>> = mapOf(), startMap: Map<Language, Node> = mapOf(), surveyVariations: List<Language> = emptyList()): Survey.Spec {
        return Survey.Spec(requireMap, optionMap, questionList, msgScreenList, startMap, surveyVariations)
    }

    fun requireMap(deviceTypeList: List<String> = emptyList(), isPersistent: Boolean = false, isOneShot: Boolean = false, customMap: String = "", wantUserStr: String? = null): Survey.RequireMap {
        return Survey.RequireMap(deviceTypeList, isPersistent, isOneShot, customMap, wantUserStr)
    }

    fun optionMap(colorThemeMap: ColorThemeMap = colorThemeMap(), mandatory: Boolean = false, showFullScreen: Boolean = false): Survey.OptionMap {
        return Survey.OptionMap(colorThemeMap, mandatory, showFullScreen)
    }

    fun question(id: Int, type: QuestionType = QuestionType.RADIO, title: String = "Title", description: String = "some description", answerList: List<Answer> = emptyList(), sendText: String = "send", nextMap: Node? = null, npsMinLabel: String = "", npsMaxLabel: String = "", disableRandom: Boolean = true, anchorLast: Boolean = false, alwaysShowSend: Boolean = true, isRequired: Boolean = false): Question {
        return Question(id, type, title, description, answerList, sendText, nextMap, npsMinLabel, npsMaxLabel, disableRandom, anchorLast, alwaysShowSend, isRequired)
    }

    fun answer(id: Int, title: String = "Answer", nextMap: Node? = null): Answer {
        return Answer(id, title, nextMap)
    }

    fun language(value: String) = Language(value)

    fun node(id: Int, nodeType: String) = Node(id, nodeType)

    fun colorThemeMap(dimType: String = "light", backgroundColor: String = "#000000", borderColor: String = "#000000", textColor: String = "#000000", buttonTextColor: String = "#000000", buttonEnabledColor: String = "#000000", buttonDisabledColor: String = "#000000"): ColorThemeMap {
        return ColorThemeMap(dimType, backgroundColor, borderColor, textColor, buttonTextColor, buttonEnabledColor, buttonDisabledColor)
    }

    fun message(id: Int, description: String = ""): Message {
        return Message(id, description)
    }

}

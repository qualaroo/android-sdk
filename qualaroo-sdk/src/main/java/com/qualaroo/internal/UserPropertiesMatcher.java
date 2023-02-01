package com.qualaroo.internal;

import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.qualaroo.internal.model.Survey;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Script;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;


@RestrictTo(LIBRARY)
public final class UserPropertiesMatcher extends SurveySpecMatcher {

    private final UserInfo userInfo;
    private final JexlEngine jexlEngine;

    public UserPropertiesMatcher(UserInfo userInfo) {
        this.userInfo = userInfo;
        this.jexlEngine = new JexlBuilder().create();
    }

    @Override boolean matches(Survey survey) {
        String customMap = survey.spec().requireMap().customMap();
        return doesUserPropertiesMatch(customMap);
    }

    @SuppressWarnings("WeakerAccess") @VisibleForTesting boolean doesUserPropertiesMatch(String customMap) {
        if (customMap == null || customMap.trim().length() == 0) {
            return true;
        }
        try {
            JexlExpression expression = jexlEngine.createExpression(customMap);
            MapContext expressionContext = new MapContext();
            Map<String, String> userProperties = userInfo.getUserProperties();
            if (expression instanceof org.apache.commons.jexl3.internal.Script) {
                Set<List<String>> variablesSet = ((Script) expression).getVariables();
                for (List<String> variables : variablesSet) {
                    for (String variable : variables) {
                        expressionContext.set(variable, userProperties.get(variable));
                    }
                }
            }
            Object result = expression.evaluate(expressionContext);
            return parseResult(result);
        } catch (JexlException e) {
            return false;
        }
    }

    private boolean parseResult(Object result) {
        if (result instanceof Boolean) {
            return (boolean) result;
        } else if (result instanceof String) {
            return true;
        } else if (result instanceof Integer) {
            return (Integer) result > 0;
        }
        return result != null;
    }
}



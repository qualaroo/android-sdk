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

    public UserPropertiesMatcher(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override boolean matches(Survey survey) {
        try {
            if (survey == null || survey.spec() == null || survey.spec().requireMap() == null) {
                return false;
            }
            String customMap = survey.spec().requireMap().customMap();
            return doesUserPropertiesMatch(customMap);
        } catch (Exception e) {
            // Return false if there's any issue with survey structure
            return false;
        }
    }

    @SuppressWarnings("WeakerAccess") @VisibleForTesting boolean doesUserPropertiesMatch(String customMap) {
        if (customMap == null || customMap.trim().length() == 0) {
            return true;
        }
        
        // Always use fallback evaluator - never try JEXL
        return evaluateComplexExpression(customMap);
    }

    private boolean evaluateComplexExpression(String expression) {
        try {
            Map<String, String> userProperties = userInfo.getUserProperties();
            
            System.out.println("UserPropertiesMatcher: Evaluating expression: " + expression);
            System.out.println("UserPropertiesMatcher: Available properties: " + userProperties);
            
            // Remove outer parentheses if they wrap the entire expression
            expression = expression.trim();
            while (expression.startsWith("(") && expression.endsWith(")") && 
                   getMatchingParenthesisIndex(expression, 0) == expression.length() - 1) {
                expression = expression.substring(1, expression.length() - 1).trim();
            }
            
            // Handle OR expressions first (lower precedence)
            if (expression.contains("||")) {
                String[] parts = expression.split("\\|\\|");
                boolean result = false;
                for (String part : parts) {
                    if (evaluateComplexExpression(part.trim())) {
                        result = true;
                        break;
                    }
                }
                System.out.println("UserPropertiesMatcher: OR expression - " + expression + " = " + result);
                return result;
            }
            
            // Handle AND expressions
            if (expression.contains("&&")) {
                String[] parts = expression.split("&&");
                boolean result = true;
                for (String part : parts) {
                    if (!evaluateComplexExpression(part.trim())) {
                        result = false;
                        break;
                    }
                }
                System.out.println("UserPropertiesMatcher: AND expression - " + expression + " = " + result);
                return result;
            }
            
            // Handle simple equality checks like "property==value"
            if (expression.contains("==")) {
                String[] parts = expression.split("==");
                if (parts.length == 2) {
                    String property = parts[0].trim();
                    String expectedValue = parts[1].trim().replace("\"", "");
                    String actualValue = userProperties.get(property);
                    boolean result = expectedValue.equals(actualValue);
                    System.out.println("UserPropertiesMatcher: Equality check - " + property + " == " + expectedValue + " = " + result + " (actual: " + actualValue + ")");
                    return result;
                }
            }
            
            // Handle simple comparisons like "property > value"
            if (expression.contains(">")) {
                String[] parts = expression.split(">");
                if (parts.length == 2) {
                    String property = parts[0].trim();
                    String expectedValue = parts[1].trim();
                    String actualValue = userProperties.get(property);
                    if (actualValue != null) {
                        try {
                            int actual = Integer.parseInt(actualValue);
                            int expected = Integer.parseInt(expectedValue);
                            boolean result = actual > expected;
                            System.out.println("UserPropertiesMatcher: Greater than check - " + property + " > " + expectedValue + " = " + result + " (" + actual + " > " + expected + ")");
                            return result;
                        } catch (NumberFormatException e) {
                            // Not numeric comparison
                        }
                    }
                }
            }
            
            // Handle simple comparisons like "property < value"
            if (expression.contains("<")) {
                String[] parts = expression.split("<");
                if (parts.length == 2) {
                    String property = parts[0].trim();
                    String expectedValue = parts[1].trim();
                    String actualValue = userProperties.get(property);
                    if (actualValue != null) {
                        try {
                            int actual = Integer.parseInt(actualValue);
                            int expected = Integer.parseInt(expectedValue);
                            boolean result = actual < expected;
                            System.out.println("UserPropertiesMatcher: Less than check - " + property + " < " + expectedValue + " = " + result + " (" + actual + " < " + expected + ")");
                            return result;
                        } catch (NumberFormatException e) {
                            // Not numeric comparison
                        }
                    }
                }
            }
            
            // Handle simple string literals
            if (expression.startsWith("\"") && expression.endsWith("\"")) {
                System.out.println("UserPropertiesMatcher: String literal - " + expression + " = true");
                return true;
            }
            
            // Handle simple numbers
            try {
                Integer.parseInt(expression.trim());
                System.out.println("UserPropertiesMatcher: Number literal - " + expression + " = true");
                return true;
            } catch (NumberFormatException e) {
                // Not a number
            }
            
            // Handle simple boolean expressions
            if (expression.trim().equals("true")) {
                System.out.println("UserPropertiesMatcher: Boolean true = true");
                return true;
            }
            if (expression.trim().equals("false")) {
                System.out.println("UserPropertiesMatcher: Boolean false = false");
                return false;
            }
            
            System.out.println("UserPropertiesMatcher: Unknown expression - " + expression + " = false");
            return false;
        } catch (Exception e) {
            System.out.println("UserPropertiesMatcher: Error evaluating expression - " + expression + ": " + e.getMessage());
            return false;
        }
    }
    
    private int getMatchingParenthesisIndex(String expression, int startIndex) {
        int count = 0;
        for (int i = startIndex; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1; // No matching parenthesis found
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



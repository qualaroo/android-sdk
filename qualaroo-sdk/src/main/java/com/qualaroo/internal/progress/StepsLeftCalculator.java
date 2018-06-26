package com.qualaroo.internal.progress;

import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StepsLeftCalculator {

    private static final String TYPE_QUESTION = "question";
    private static final String TYPE_MESSAGE = "message";
    private static final String TYPE_QSCREEN = "qscreen";

    private final Map<String, Map<Long, GraphNode>> nodes;
    private final LongSparseArray<Question> questions;
    private final LongSparseArray<Message> messages;
    private final LongSparseArray<QScreen> qScreens;
    private Graph graph;

    public StepsLeftCalculator(LongSparseArray<Question> questions, LongSparseArray<Message> messages, LongSparseArray<QScreen> qScreens) {
        this.questions = questions;
        this.messages = messages;
        this.qScreens = qScreens;
        this.nodes = new HashMap<>();
        this.nodes.put(TYPE_QUESTION, new HashMap<>());
        this.nodes.put(TYPE_MESSAGE, new HashMap<>());
        this.nodes.put(TYPE_QSCREEN, new HashMap<>());

        initialize();
    }

    private void initialize() {
        for (int i = 0; i < messages.size(); i++) {
            node(messages.valueAt(i));
        }
        for (int i = 0; i < questions.size(); i++) {
            node(questions.valueAt(i));
        }
        for (int i = 0; i < qScreens.size(); i++) {
            node(qScreens.valueAt(i));
        }
    }

    public void setCurrentStep(long id, String type) {
        GraphNode node = nodes.get(type).get(id);
        graph = new Graph(node);
    }

    public int getStepsLeft() {
        return graph.longestPathFromRoot();
    }

    private GraphNode node(Message message) {
        GraphNode node = nodes.get(TYPE_MESSAGE).get(message.id());
        if (node != null) {
            return node;
        }
        GraphNode messageNode = new GraphNode(message.id(), TYPE_MESSAGE, Collections.emptyList());
        nodes.get(TYPE_MESSAGE).put(message.id(), messageNode);
        return messageNode;
    }

    private GraphNode node(Question question) {
        GraphNode questionNode = nodes.get(TYPE_QUESTION).get(question.id());
        if (questionNode != null) {
            return questionNode;
        }
        List<GraphNode> childNodes = new ArrayList<>();

        for (Answer answer : question.answerList()) {
            if (answer.nextMap() != null) {
                childNodes.add(node(answer.nextMap().id(), answer.nextMap().nodeType()));
            }
        }
        if (question.nextMap() != null) {
            childNodes.add(node(question.nextMap().id(), question.nextMap().nodeType()));
        }

        questionNode = new GraphNode(question.id(), TYPE_QUESTION, childNodes);
        nodes.get(TYPE_QUESTION).put(question.id(), questionNode);
        return questionNode;
    }

    private GraphNode node(QScreen qScreen) {
        GraphNode qscreenNode = nodes.get(TYPE_QSCREEN).get(qScreen.id());
        if (qscreenNode != null) {
            return qscreenNode;
        }
        List<GraphNode> childNodes = new ArrayList<>();
        if (qScreen.nextMap() != null) {
            childNodes.add(node(qScreen.nextMap().id(), qScreen.nextMap().nodeType()));
        }
        GraphNode graphNode = new GraphNode(qScreen.id(), TYPE_QSCREEN, childNodes);
        nodes.get(TYPE_QSCREEN).put(qScreen.id(), graphNode);
        return graphNode;
    }

    private @Nullable GraphNode node(long id, String type) {
        switch (type) {
            case "message":
                return node(messages.get(id));
            case "question":
                return node(questions.get(id));
            case "qscreen":
                return node(qScreens.get(id));
            default:
                return null;
        }
    }

    private static class Graph {
        private final StepsLeftCalculator.GraphNode root;

        Graph(GraphNode root) {
            this.root = root;
        }

        int longestPathFromRoot() {
            List<GraphNode> nodes = topologicalSort(root);
            Map<GraphNode, Integer> distanceToNode = new HashMap<>();
            for (GraphNode node : nodes) {
                distanceToNode.put(node, 0);
            }

            for (GraphNode node : nodes) {
                for (GraphNode child : node.children) {
                    if (distanceToNode.get(child) <= distanceToNode.get(node) + 1) {
                        distanceToNode.put(child, distanceToNode.get(node) + 1);
                    }
                }
            }
            return Collections.max(distanceToNode.values());
        }

        private List<GraphNode> topologicalSort(GraphNode root) {
            Set<GraphNode> visited = new HashSet<>();
            List<GraphNode> result = new LinkedList<>();
            topologicalSort(root, visited, result);
            Collections.reverse(result);
            return result;
        }

        private void topologicalSort(GraphNode node, Set<GraphNode> visited, List<GraphNode> result) {
            if (visited.contains(node)) {
                return;
            }

            for (GraphNode child : node.children) {
                topologicalSort(child, visited, result);
            }
            visited.add(node);
            result.add(node);
        }
    }

    private static class GraphNode {
        long id;
        List<GraphNode> children;
        String type;

        GraphNode(long id, String type, List<GraphNode> children) {
            this.id = id;
            this.type = type;
            this.children = children;
        }
    }
}

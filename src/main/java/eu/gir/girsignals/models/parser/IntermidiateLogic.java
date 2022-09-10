package eu.gir.girsignals.models.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import eu.gir.girsignals.models.parser.interm.EvaluationLevel;
import eu.gir.girsignals.models.parser.interm.IntermidiateNode;

public class IntermidiateLogic {

    private final Stack<List<IntermidiateNode>> stackNodes = new Stack<>();

    public IntermidiateLogic() {
        this.push();
    }

    public void push() {
        stackNodes.add(new ArrayList<>());
    }

    public IntermidiateNode pop() {
        List<IntermidiateNode> node = stackNodes.pop();
        for (final EvaluationLevel level : EvaluationLevel.values()) {
            if (level.equals(EvaluationLevel.PRELEVEL))
                continue;
            final List<IntermidiateNode> nextNode = new ArrayList<>();
            for (int i = 0; i < node.size(); i++) {
                final IntermidiateNode current = node.get(i);
                if (!current.getLevel().equals(level)) {
                    nextNode.add(current);
                    continue;
                }

                final int nextIndex = i + 1;
                if (nextIndex >= node.size())
                    throw new LogicalParserException("Expected more input at the end!");
                final IntermidiateNode next = node.get(nextIndex);
                if (current.next(next)) {
                    nextNode.add(current.getFinished());
                    i++;
                    continue;
                }

                final int prevIndex = nextNode.size() - 1;
                if (prevIndex < 0)
                    throw new LogicalParserException("Expected more input at the begining!");
                final IntermidiateNode last = nextNode.get(prevIndex);
                if (current.combine(last, next)) {
                    nextNode.set(prevIndex, current.getFinished());
                    i++;
                    continue;
                }
            }
            node = nextNode;
        }
        if (node.size() != 1)
            throw new LogicalParserException("Could not merge all nodes!");
        if (!stackNodes.isEmpty())
            stackNodes.lastElement().add(node.get(0));
        return node.get(0);
    }

    public void add(final IntermidiateNode node) {
        stackNodes.lastElement().add(node);
    }

}

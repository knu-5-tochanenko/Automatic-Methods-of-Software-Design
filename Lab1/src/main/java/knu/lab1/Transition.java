package knu.lab1;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Transition implements Comparable<Transition> {
    private final State source;
    private final Symbol activator;
    private final State target;
    private final Symbol out;

    public String getTransition() {
        return source + " -> " + target + " [label=\"" + activator.getSymbol() + "\"]";
    }

    public String getOutput() {
        return source + " -> " + out.getSymbol() + " [label=\"" + activator.getSymbol() + "\"]";
    }

    @Override
    public int compareTo(Transition o) {
        int sourceCompare = source.compareTo(o.source);
        if (sourceCompare == 0) {
            int activatorCompare = activator.compareTo(o.activator);
            if (activatorCompare == 0) {
                return target.compareTo(o.target);
            }
            return activatorCompare;
        }
        return sourceCompare;
    }
}

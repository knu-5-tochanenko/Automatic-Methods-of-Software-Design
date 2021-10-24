package knu.lab1;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public class Automaton {
    private final Map<String, State> states = new LinkedHashMap<>();
    private final List<Symbol> alphabet = new ArrayList<>();
    private final List<Transition> transitions = new ArrayList<>();
    private final SortedSet<State> initialStates = new TreeSet<>();

    private Automaton() {}

    public void show() {
        transitions.sort(Transition::compareTo);
        System.out.println("---------- STATES ----------");
        states.values().stream().map(State::toString).forEach(str -> System.out.print(str + " "));
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
        System.out.println();
        System.out.println("---------TRANSITION---------");
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        System.out.println("---------OUTPUT---------");
        transitions.stream().map(Transition::getOutput).forEach(System.out::println);
        System.out.println("----------------------------");
        System.out.println();
    }

    public void eliminateEps() {
        System.out.println("1 ======================================================");
        addInitialStates();
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
        System.out.println("2 ======================================================");
        buildEpsilonTransitionsClosure();
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
        System.out.println("3 ======================================================");
        markNewFinalStates();
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
        System.out.println("4 ======================================================");
        addNewTransitions();
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
        System.out.println("5 ======================================================");
        discardEpsTransitions();
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
        System.out.println("6 ======================================================");
        verify();
        transitions.stream().map(Transition::getTransition).forEach(System.out::println);
        states.values().forEach(state -> {
            if (state.getType() == StateType.FINAL) {
                System.out.println();
                System.out.println(state.getName() + " [shape=doublecircle]");
            }
        });
    }

    // Add new initial states
    private void addInitialStates() {
        Queue<State> stq = new LinkedList<>();

        stq.addAll(initialStates);

        while (!stq.isEmpty()) {
            State s = stq.remove();
            List<Transition> outbound = s.getOutbound().get(Symbol.EPS);

            if (outbound != null) {
                for (Transition t: outbound) {
                    State to = t.getTarget();
                    if (!initialStates.contains(to)) {
                        initialStates.add(to);
                        stq.add(to);
                    }
                }
            }
        }
    }

    // Transitive closure for epsilon transitions
    private void buildEpsilonTransitionsClosure() {
        Queue<Transition> epsTransitions = new LinkedList<>(listEpsTransitions());
        while (!epsTransitions.isEmpty()) {
            Transition epsTransition = epsTransitions.remove();
            State sourceState = epsTransition.getSource();
            State targetState = epsTransition.getTarget();

            List<Transition> incomingEpsTransitions = sourceState.getInbound().get(Symbol.EPS);

            if (incomingEpsTransitions != null) {
                boolean wasAdded = false;
                for (Transition incomingEpsTransition : incomingEpsTransitions) {
                    State source = incomingEpsTransition.getSource();
                    Transition newEpsTransition = addTransition(source, Symbol.EPS, targetState, incomingEpsTransition.getOut());
                    if (newEpsTransition != null) {
                        epsTransitions.add(newEpsTransition);
                        wasAdded = true;
                    }
                }
                if (wasAdded) {
                    epsTransitions.add(epsTransition);
                }
            }
        }
    }

    // Mark states preceding epsilon transitions to final states as final
    private void markNewFinalStates() {
        for (Transition t : listEpsTransitions()) {
            if (t.getTarget().getType().equals(StateType.FINAL)) {
                t.getSource().setType(StateType.FINAL);
            }
        }
    }

    // Add transitive edges
    private void addNewTransitions() {
        Queue<Transition> epsTransitions = new LinkedList<>(listEpsTransitions());

        while (!epsTransitions.isEmpty()) {
            Transition epsTransition = epsTransitions.remove();

            State src = epsTransition.getSource();
            State trg = epsTransition.getTarget();
            Symbol out = epsTransition.getOut();

            Map<Symbol, List<Transition>> parentTransitions = src.getInbound();
            for (Symbol activator : parentTransitions.keySet()) {
                if (activator != Symbol.EPS) {
                    List<Transition> transitions = parentTransitions.get(activator);

                    boolean wasAdded = false;
                    for (Transition transition : transitions) {
                        State source = transition.getSource();

                        Transition newTransition = addTransition(source, activator, trg, out);
                        if (newTransition != null) {
                            wasAdded = true;
                        }
                    }
                    if (wasAdded) {
                        epsTransitions.add(epsTransition);
                    }
                }
            }
        }
    }

    // Discard all existing eps transitions
    private void discardEpsTransitions() {
        for (Transition transition : new LinkedList<>(transitions)) {
            if (transition.getActivator().equals(Symbol.EPS)) {
                transitions.remove(transition);
                transition.getSource().removeOutboundTransition(transition);
            }
        }
    }

    // Verify
    private void verify() {
        for (State state : states.values()) {
            Stream.concat(state.getInbound().values().stream(), state.getOutbound().values().stream()).forEach(
                    list -> list.forEach(
                            transition -> {
                                if (transition.getActivator().equals(Symbol.EPS)) {
                                    throw new IllegalStateException("SOMETHING WENT WRONG, EPS TRANSITION LEFTOVER FOUND");
                                }
                            }
                    )
            );
        }
    }

    private List<Transition> listEpsTransitions() {
        return transitions.stream().filter(tran -> tran.getActivator().equals(Symbol.EPS)).collect(Collectors.toList());
    }

    // HELPERS FOR AUTOMATA MODIFICATION
    private Transition addTransition(final State source, final Symbol activator, final State target, final Symbol out) {
        // Check if exists
        for (Transition trans: transitions) {
            if (trans.getSource().equals(source) && trans.getActivator().equals(activator) && trans.getTarget().equals(target)) {
                return null;
            }
        }
        // Create if not
        Transition t = new Transition(source, activator, target, out);
        source.addOutboundTransition(t);
        transitions.add(t);
        return t;
    }

    // FACTORIES
    public static Automaton fromSource() {
        return AutomatonFactory.fromSource1();
    }

    private static class AutomatonFactory{
        private static Automaton fromSource1() {

            Automaton result = new Automaton();

            State q0 = new State("q0", StateType.COMMON);
            State q1 = new State("q1", StateType.COMMON);
            State q2 = new State("q2", StateType.COMMON);
            State q3 = new State("q3", StateType.FINAL);
            State q4 = new State("q4", StateType.COMMON);
            State q5 = new State("q5", StateType.COMMON);

            result.initialStates.add(q0);

            result.states.put(q0.getName(), q0);
            result.states.put(q1.getName(), q1);
            result.states.put(q2.getName(), q2);
            result.states.put(q3.getName(), q3);
            result.states.put(q4.getName(), q4);
            result.states.put(q5.getName(), q5);

            Symbol x = new Symbol("x");
            Symbol y = new Symbol("y");
            Symbol eps = Symbol.EPS;

            result.alphabet.add(x);
            result.alphabet.add(y);

            result.addTransition(q0, x, q1, y);
            result.addTransition(q1, x, q2, y);
            result.addTransition(q2, eps, q3, x);
            result.addTransition(q1, x, q5, y);
            result.addTransition(q5, y, q2, x);
            result.addTransition(q1, y, q4, x);
            result.addTransition(q4, eps, q2, y);

            return result;
        }
        private static Automaton fromSource2() {
            Automaton automaton = new Automaton();

            State q0 = new State("q0", StateType.COMMON);
            State q1 = new State("q1", StateType.COMMON);
            State q2 = new State("q2", StateType.FINAL);

            automaton.initialStates.add(q0);

            automaton.states.put(q0.getName(), q0);
            automaton.states.put(q1.getName(), q1);
            automaton.states.put(q2.getName(), q2);

            Symbol x = new Symbol("x");
            Symbol y = new Symbol("y");
            Symbol eps = Symbol.EPS;

            automaton.alphabet.add(x);
            automaton.alphabet.add(y);

            automaton.addTransition(q0, x, q0, y);
            automaton.addTransition(q0, y, q0, x);
            automaton.addTransition(q0, x, q1, y);
            automaton.addTransition(q0, eps, q2, x);
            automaton.addTransition(q1, eps, q2, y);
            automaton.addTransition(q1, y, q2, x);
            automaton.addTransition(q1, y, q0, x);

            return automaton;
        }
        private static Automaton fromSource3() {
            Automaton automaton = new Automaton();

            State q0 = new State("q0", StateType.COMMON);
            State q1 = new State("q1", StateType.COMMON);
            State q2 = new State("q2", StateType.COMMON);
            State q3 = new State("q3", StateType.COMMON);
            State q4 = new State("q4", StateType.COMMON);
            State q5 = new State("q5", StateType.FINAL);
            State q6 = new State("q6", StateType.FINAL);

            automaton.states.put(q0.getName(), q0);
            automaton.states.put(q1.getName(), q1);
            automaton.states.put(q2.getName(), q2);
            automaton.states.put(q3.getName(), q3);
            automaton.states.put(q4.getName(), q4);
            automaton.states.put(q5.getName(), q5);
            automaton.states.put(q6.getName(), q6);

            automaton.initialStates.add(q0);

            Symbol a = new Symbol("a");
            Symbol b = new Symbol("b");
            Symbol eps = Symbol.EPS;

            automaton.alphabet.add(a);
            automaton.alphabet.add(b);

            automaton.addTransition(q0, eps, q1, b);
            automaton.addTransition(q1, eps, q2, a);
            automaton.addTransition(q2, eps, q5, a);
            automaton.addTransition(q0, eps, q3, b);
            automaton.addTransition(q3, a, q4, b);
            automaton.addTransition(q4, b, q5, a);
            automaton.addTransition(q4, eps, q6, b);

            return automaton;
        }
        private static Automaton fromSource4() {
            Automaton automaton = new Automaton();

            State q0 = new State("q0", StateType.COMMON);
            State q1 = new State("q1", StateType.COMMON);
            State q2 = new State("q2", StateType.COMMON);
            State q3 = new State("q3", StateType.FINAL);

            automaton.states.put(q0.getName(), q0);
            automaton.states.put(q1.getName(), q1);
            automaton.states.put(q2.getName(), q2);
            automaton.states.put(q3.getName(), q3);

            automaton.initialStates.add(q0);

            Symbol x = new Symbol("x");
            Symbol y = new Symbol("y");
            Symbol eps = Symbol.EPS;

            automaton.alphabet.add(x);
            automaton.alphabet.add(y);

            automaton.addTransition(q0, x, q1, y);
            automaton.addTransition(q0, y, q2, x);
            automaton.addTransition(q0, eps, q3, x);

            automaton.addTransition(q1, eps, q2, y);
            automaton.addTransition(q1, y, q3, x);

            automaton.addTransition(q2, x, q0, y);
            automaton.addTransition(q2, x, q3, y);
            automaton.addTransition(q2, eps, q3, x);
            automaton.addTransition(q2, y, q1, x);

            automaton.addTransition(q3, x, q0, y);

            return automaton;
        }
        private static Automaton fromSource5() {
            Automaton automaton = new Automaton();

            State q0 = new State("q0", StateType.COMMON);
            State q1 = new State("q1", StateType.COMMON);
            State q2 = new State("q2", StateType.COMMON);
            State q3 = new State("q3", StateType.COMMON);
            State q4 = new State("q4", StateType.COMMON);
            State q5 = new State("q5", StateType.FINAL);

            automaton.states.put(q0.getName(), q0);
            automaton.states.put(q1.getName(), q1);
            automaton.states.put(q2.getName(), q2);
            automaton.states.put(q3.getName(), q3);
            automaton.states.put(q4.getName(), q4);
            automaton.states.put(q5.getName(), q5);

            automaton.initialStates.add(q0);

            Symbol a = new Symbol("a");
            Symbol c = new Symbol("c");
            Symbol d = new Symbol("d");
            Symbol eps = Symbol.EPS;

            automaton.alphabet.add(a);
            automaton.alphabet.add(c);
            automaton.alphabet.add(d);

            automaton.addTransition(q0, eps, q1, d);
            automaton.addTransition(q0, a, q1, c);

            automaton.addTransition(q1, c, q1, d);
            automaton.addTransition(q1, d, q2, d);
            automaton.addTransition(q1, c, q4, c);

            automaton.addTransition(q2, c, q3, c);

            automaton.addTransition(q3, c, q3, d);
            automaton.addTransition(q3, eps, q5, c);

            automaton.addTransition(q4, d, q3, a);

            return automaton;
        }
    }
}

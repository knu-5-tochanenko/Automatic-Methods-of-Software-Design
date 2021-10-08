package knu.lab1;

class Main {
    public static void main(String[] args) {
        Automaton automaton = Automaton.fromSource();
        automaton.show();
        automaton.eliminateEps();
        automaton.show();
    }
}

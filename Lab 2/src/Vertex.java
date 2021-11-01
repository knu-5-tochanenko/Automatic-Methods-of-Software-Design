import java.util.Objects;

class Vertex {
    private String stateLabel;
    private boolean start;
    private boolean accept;

    public Vertex(String stateLabel, boolean start, boolean accept) {
        this.stateLabel = stateLabel;
        this.start = start;
        this.accept = accept;
    }
    public Vertex(String stateLabel) {
        this.stateLabel = stateLabel;
        this.start = false;
        this.accept = false;
    }

    public Vertex(Vertex v) {
        this.stateLabel = v.getStateLabel();
        this.start = v.isStart();
        this.accept = v.isAccept();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return start == vertex.start && accept == vertex.accept && Objects.equals(stateLabel, vertex.stateLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateLabel, start, accept);
    }

    public String getStateLabel() {
        return stateLabel;
    }

    public void setStateLabel(String stateLabel) {
        this.stateLabel = stateLabel;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

}
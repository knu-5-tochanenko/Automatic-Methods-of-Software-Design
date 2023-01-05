import java.util.ArrayList;

public class Graph {
    int stateCount;
    String[][] matrix;

    public Graph(int stateCount) {
        this.stateCount = stateCount;
        matrix = new String[stateCount][stateCount];
        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j < stateCount; j++) {
                matrix[i][j] = "";
            }
        }
    }

    public void addEdge(int source, int destination, String transaction) {
        //add edge
        matrix[source][destination] = transaction;
    }

    public void removeEdge(int source, int destination){
        matrix[source][destination] = "";
    }

    public void removeQRip(int qRip){
        for (int i = 0; i < stateCount; i++) {
            removeEdge(i,qRip);
        }
        for (int i = 0; i <stateCount ; i++) {
            removeEdge(qRip,i);
        }
    }

    public void print(ArrayList<Vertex> vertices, ArrayList<String> alphabet) {
        //Print alphanet
        System.out.print("\nAlphabet: ");
        for (int i = 0; i < alphabet.size(); i++) {
            System.out.print(alphabet.get(i) + ", ");
        }
        System.out.println();
        //Print States
        System.out.println("=====STATES=====");
        for (int i = 0; i < vertices.size(); i++) {
            if (!vertices.get(i).getStateLabel().equals("")){
                System.out.print(vertices.get(i).getStateLabel());
                if (vertices.get(i).isStart())
                    System.out.print(" (Start)");
                if (vertices.get(i).isAccept())
                    System.out.print(" (Accept)");
                System.out.println();
            }
        }
        System.out.println("================");
        System.out.println("\n=====Graph: (Adjacency Matrix)=====");
        for (int i = 0; i < vertices.size(); i++) {
            System.out.print( "\t" + vertices.get(i).getStateLabel());
        }
        System.out.println();
        for (int i = 0; i < stateCount; i++) {
            if(vertices.size() > i)
                System.out.print(vertices.get(i).getStateLabel() + "\t");
            for (int j = 0; j <stateCount ; j++) {
                System.out.print(matrix[i][j]+ "\t");
            }
            System.out.println();
        }
        System.out.println("\n===================================");
        System.out.println("\n=====Transactions=====");
        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j <stateCount ; j++) {
                if(!matrix[i][j].equals("")){
                    if (i < stateCount -2 && j < stateCount -2)
                        System.out.println(vertices.get(i).getStateLabel() + " --" + matrix[i][j] + "--> " + vertices.get(j).getStateLabel());
                    else if (i == stateCount -2) {
                        if (j == stateCount -1)
                            System.out.println("S --" + matrix[i][j] + "--> A");
                        else
                            System.out.println("S --" + matrix[i][j] + "--> " + vertices.get(j).getStateLabel());
                    }
                    if(j == stateCount -1 && i != stateCount -2){
                        System.out.println(vertices.get(i).getStateLabel() + " --" + matrix[i][j] + "--> A");
                    }
                }
            }
        }
        System.out.println("\n====================");
    }
}
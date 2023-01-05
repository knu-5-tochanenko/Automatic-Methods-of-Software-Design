import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Dfa2Regex {
    public static String epsilon = "E";
    public static String union = "U";
    public static ArrayList<String> alphabet = new ArrayList<>();
    public static boolean comeBack;
    public static Graph graph;
    public static ArrayList<Vertex> vertices = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter dfa file name (e.g. DFA1.txt) :");
        String fileName = sc.nextLine();
        //read from file and assign to lines list
        ArrayList<String> lines = readFromFile(fileName);
        //Determine the state count
        int stateCount = getStateCount(lines) + 2;
        //create square matrix with statecount
        graph = new Graph(stateCount);
        createGraph(lines);
        System.out.println("=====DFA TO "+ (vertices.size()+2) +" state GNFA=====");
        dfa2FirstGnfa(stateCount);
        gnfa2Regex();
        System.out.println("\n\nRegular Expression = " + graph.matrix[stateCount-2][stateCount-1]);
    }

    /*Read from file operation. Takes txt file name as(filename.txt) in same direction with class.
    returns a Arraylist includes file lines.*/
    public static ArrayList<String> readFromFile(String fileName) throws Exception {
        ArrayList<String> lines = new ArrayList<>();
        URL path = Dfa2Regex.class.getResource(fileName);
        File file = new File(path.getFile());
        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        while ((st = br.readLine()) != null) {
            lines.add(st);
        }
        return lines;
    }

    //Convert DFA to GNFA
    public static void dfa2FirstGnfa(int stateCount){
        //Createing start and accept vertices.
        Vertex start = new Vertex("S",true,false);
        Vertex accept = new Vertex("A",false,true);
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).isStart()){
                //Start state change
                vertices.get(i).setStart(false);
                graph.addEdge(stateCount-2,i,epsilon);
            }
            if (vertices.get(i).isAccept()){
                //Accept states change
                vertices.get(i).setAccept(false);
                graph.addEdge(i,stateCount-1,epsilon);

            }
        }
        //Adding created vertices into vertices list
        vertices.add(start);
        vertices.add(accept);
        //set first state to q-rip
        vertices.get(0).setStateLabel("qr");
        graph.print(vertices,alphabet);
    }



    public static void gnfa2Regex(){
        int qRip = 0;
        String comeBackLabel = "", loopLabel = "", transactionLabel ="";

        //Get connections of GNFA's start state
        for (int i = 0; i < graph.stateCount; i++) {
            if (!graph.matrix[qRip][i].equals("") && qRip != i){
                graph.matrix[graph.stateCount-2][i] = graph.matrix[qRip][i];
            }
        }
        graph.removeEdge(graph.stateCount -2,qRip);

        //Constructing equivalent GNFA with one fewer state
        for (int k = 0; k < graph.stateCount -2; k++) {
            //define q-rip index
            System.out.println("\n=============="+ (graph.stateCount - (k+1)) +"-state GNFA=============");
            for (int i = 0; i < vertices.size(); i++) {
                if (vertices.get(i).getStateLabel().equals("qr"))
                    qRip = i;
            }
            //Control the comeback transactions with q-rip (i.e. qr to q2 and q2 to qr)
            for (int i = 0; i < graph.stateCount; i++) {
                if (!graph.matrix[qRip][i].equals("") && qRip != i && !graph.matrix[i][qRip].equals("")) {
                    comeBack = true;
                    comeBackLabel += graph.matrix[i][qRip];
                    //Self loop control
                    if (!graph.matrix[qRip][qRip].equals("")) {
                        comeBackLabel += "(" + graph.matrix[qRip][qRip] + ")*";
                    }
                    comeBackLabel += graph.matrix[qRip][i];
                    if (!graph.matrix[i][i].equals("")) {
                        comeBackLabel += union + graph.matrix[i][i];
                    }
                }
                if (comeBack && i < graph.stateCount - 2) {
                    graph.matrix[i][i] = comeBackLabel;
                }
                comeBack = false;
                comeBackLabel = "";
            }

            //Control the qn-qm transaction over q-rip
            for (int i = 0; i < graph.stateCount; i++) {
                //Control coming transactions to qRip to qn
                if (!graph.matrix[i][qRip].equals("") && i != qRip) {
                    transactionLabel += graph.matrix[i][qRip];
                    //Self loop control
                    if (!graph.matrix[qRip][qRip].equals("")) {
                        transactionLabel += "(" + graph.matrix[qRip][qRip] + ")*";
                    }
                    String temp = transactionLabel;
                    for (int j = 0; j < graph.stateCount; j++) {
                        //Control going transactions from qRip to qm
                        if (!graph.matrix[qRip][j].equals("") && j != i && j != qRip) {
                            transactionLabel += graph.matrix[qRip][j];
                            //control if exist another way of transaction qn-qm
                            if (!graph.matrix[i][j].equals("")) {
                                transactionLabel += union + graph.matrix[i][j];
                            }
                            graph.matrix[i][j] = transactionLabel;
                            transactionLabel = temp;
                        }
                    }
                }
                transactionLabel = "";
            }
            graph.removeQRip(qRip);
            vertices.get(qRip).setStateLabel("");
            vertices.get(qRip + 1).setStateLabel("qr");
            graph.print(vertices,alphabet);
        }
    }

    //According to readings from txt file, returns count of states
    public static int getStateCount(ArrayList<String> graphInfo){
        int stateCount = 0;
        String[] splitArrEquals = new String[10];
        String[] splitArrComma = new String[10];
        splitArrEquals = graphInfo.get(3).split("=");
        splitArrComma = splitArrEquals[1].split(",");
        for (int i = 0; i < splitArrComma.length; i++) {
            if (!splitArrComma[i].equals("")){
                stateCount++;
            }
        }
        return stateCount;
    }

    //Creating the graph with given vertices info in txt file
    public static void createGraph(ArrayList<String> graphInfo){
        String[] splitArrEquals = new String[10];
        String[] splitArrComma = new String[10];
        splitArrEquals = graphInfo.get(3).split("=");
        splitArrComma = splitArrEquals[1].split(",");
        for (int i = 0; i < splitArrComma.length; i++) {
            if (!splitArrComma[i].equals("")){
                vertices.add(new Vertex(splitArrComma[i]));
            }
        }
        //Define start vertex
        splitArrEquals = graphInfo.get(0).split("=");
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).getStateLabel().equals(splitArrEquals[1])) {
                vertices.get(i).setStart(true);
            }
        }

        //Define accept vertices
        splitArrEquals = graphInfo.get(1).split("=");
        splitArrComma = splitArrEquals[1].split(",");

        for (int i = 0; i < splitArrComma.length; i++) {
            for (int j = 0; j < vertices.size(); j++) {
                if (vertices.get(j).getStateLabel().equals(splitArrComma[i])) {
                    vertices.get(j).setAccept(true);
                }
            }
        }

        //Define Alphabet
        alphabet.add(epsilon);
        splitArrEquals = graphInfo.get(2).split("=");
        splitArrComma = splitArrEquals[1].split(",");
        for (int i = 0; i < splitArrComma.length; i++) {
            if (!splitArrComma[i].equals("")){
                alphabet.add(splitArrComma[i]);
            }
        }

        //Create edge relations
        int transactionNum = vertices.size() * (alphabet.size()-1);
        for (int i = 0; i < transactionNum; i++) {
            splitArrComma = graphInfo.get(i+4).split(",");
            splitArrEquals = splitArrComma[1].split("=");
            //get vertex
            for (int j = 0; j < vertices.size(); j++) {
                if (splitArrEquals[1].equals(vertices.get(j).getStateLabel())){
                    //read edge source
                    for (int k = 0; k < vertices.size(); k++) {
                       if (splitArrComma[0].equals(vertices.get(k).getStateLabel())){
                           //create and set edge source
                           graph.addEdge(k,j,splitArrEquals[0]);
                       }
                    }
                }
            }
        }
        graph.print(vertices,alphabet);
    }
}

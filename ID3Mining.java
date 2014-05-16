import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class ID3Mining {
	static int rowCount = 0;
	static DataBase db = null;
	static Target target = null;
	static int level = 0;
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		String filename = null;
		Scanner scan = new Scanner(System.in);
		
		// get input from the user
		System.out.println("What is the name of the file containing your data?: ");
		filename = scan.nextLine();
		ArrayList<AVList> avLists = loadDB(filename);
		
		// get potential target attributes
		ArrayList<String> potentialTargets = new ArrayList<String>();
		for (AVList av : avLists){
			if (av.isBinary()){
				potentialTargets.add(av.getAttribute());
			}
		}
		if (potentialTargets.isEmpty()){
			System.out.println("No potential targets..");
			System.exit(1);
		}
		
		// get the user's selection of target
		System.out.println("Please choose an attribute (by number): ");
		int i = 1;
		for (String potTarget : potentialTargets){
			System.out.println("\t" + i + ". " + potTarget);
			i++;
		}
		System.out.println("Attribute: ");
		int select = Integer.parseInt(scan.nextLine());
		String targetAtt = null;
		try {
			targetAtt = potentialTargets.get(select - 1);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Selection not available.");
			e.printStackTrace();
		}
		System.out.println("Target attribute is: " + targetAtt);
		
		target = new Target(targetAtt);
		String[] targetClasses = getTargetClasses(avLists);
		target.setvalue1(targetClasses[0]);
		target.setvalue2(targetClasses[1]);
		
		
		Node tree = id3(avLists, db);
		
		PrintWriter writer = new PrintWriter("Rules", "UTF-8");
		printTree(tree, writer);
		
		System.out.println("Decision tree mining completed.");
		
		writer.close();
		scan.close();
	}
	
	private static ArrayList<AVList> loadDB(String filename){
		FileInputStream	file;
		BufferedReader dataReader;
		ArrayList<AVList> avLists = null;
		
		try {
			file = new FileInputStream(filename);
			dataReader = new BufferedReader(new InputStreamReader(file));
			
			avLists = new ArrayList<AVList>();
			StringTokenizer lineTok = null;		
			
			String attributeLine = dataReader.readLine();
			lineTok = new StringTokenizer(attributeLine);
			
			// build the database from the input file
			while(lineTok.hasMoreTokens()){
				avLists.add(new AVList(lineTok.nextToken()));
			}
			db = new DataBase(new ArrayList<String>(Arrays.asList(attributeLine.split("\\s+"))));
			
			String valueLine = dataReader.readLine();
			while(valueLine != null){
				lineTok = new StringTokenizer(valueLine);
				rowCount++;
				
				for(int i = 0; lineTok.hasMoreTokens(); i++){
					avLists.get(i).add(lineTok.nextToken());
				}
				db.addRow(valueLine.split("\\s+"));
				
				valueLine = dataReader.readLine();
			}
			
			dataReader.close();
		} catch (IOException e) {
			System.out.println("File not found...");
			e.printStackTrace();
		}
		return avLists;
	}

	private static String[] getTargetClasses(ArrayList<AVList> avLists){
		String[] classes = new String[2];
		Set<String> uniqueValues = null;
		for (AVList list : avLists){
			if (list.getAttribute().equals(target.getAttribute())){
				uniqueValues = list.getUniqueValues();
			}
		}
		int i = 0;
		for (String val : uniqueValues){
			classes[i] = val;
			i++;
		}
		
		return classes;
	}
	
	private static Node id3(ArrayList<AVList> avLists, DataBase examples) {
			// check if all examples are val1
			if (examples.allMatchClass(target.getAttribute(), target.getvalue1())){
				return new Node(target.getvalue1());
			}
			// check if all are val2
			if (examples.allMatchClass(target.getAttribute(), target.getvalue2())){
				return new Node(target.getvalue2());
			}
			// check if there are any attributes left
			if(avLists.size() < 2){
				int val1Count = examples.numRowsWithValue(target.getAttribute(), target.getvalue1());
				int val2Count = examples.numRowsWithValue(target.getAttribute(), target.getvalue2());
				
				if (val1Count > val2Count){
					return new Node(target.getvalue1());
				} else if (val1Count < val2Count){
					return new Node(target.getvalue2());
				} else {
					return null;
				}
			}
			
			AVList chosenAv = null;
			for (AVList list : avLists){
				if (!list.getAttribute().equals(target.getAttribute())){
					chosenAv = list;
				}
			}
			double greatestGain = gain(examples, chosenAv);
			// get gain of all attributes
			for (AVList av : avLists){
				double testGain = gain(examples, av);
				if (!av.getAttribute().equals(target.getAttribute()) && testGain > greatestGain){
					chosenAv = av;
					greatestGain = testGain;
				}
			}
			Node root = new Node(chosenAv.getAttribute());
			chosenAv.setUniqueValues();
			for (String value : chosenAv.getUniqueValues()){
				DataBase newExamples = new DataBase(examples.getAttributes(), examples.getRowsWithValue(chosenAv.getAttribute(), value));
				if (newExamples.getData().size() == 0){
	// this is what the algorthim from the slides says to do:
	//				ArrayList<AVList> targetAVList = new ArrayList<AVList>();
	//				for (AVList list : avLists){
	//					if (list.getAttribute().equals(target.getAttribute())){
	//						targetAVList.add(list);
	//					}
	//				}
	//				root.addNode(id3(targetAVList, examples), value);
					
	// this is what actually gives the same results as the example outputs:
					root.addNode(null, value);
				} else {
					ArrayList<AVList> avListsCopy = new ArrayList<AVList>(avLists);
					int i = 0;
					for (AVList attrib : avListsCopy){
						if (attrib.getAttribute().equals(chosenAv.getAttribute())){
							break;
						}
						i++;
					}
					avListsCopy.remove(i);
					
					root.addNode(id3(avListsCopy, newExamples), value);
				}
			}
			
			return root;
		}

	private static double gain(DataBase examples, AVList A){
		double gain = entropyS(examples);
		A.setUniqueValues();
		Set<String> values = A.getUniqueValues();
		
		for (String value : values){
			if (examples.numRowsWithValue(A.getAttribute(), value) > 0) {
				int valueRows = examples.numRowsWithValue(A.getAttribute(), value);
				double prop = ((double) valueRows)/ ((double) examples.getData().size()); 
				double difference = prop*entropy(examples, A.getAttribute(), value);
				gain -= difference;
			}
		}
		
		return gain;
	}
	
	private static double entropyS(DataBase examples){
		double p1 = examples.proportion(target.getAttribute(), target.getvalue1(), target.getAttribute(), target.getvalue1());
		double p2 = examples.proportion(target.getAttribute(), target.getvalue2(), target.getAttribute(), target.getvalue2());
		return (-1*(p1*log2(p1)))-(p2*log2(p2));
	}

	private static double entropy(DataBase examples, String attribute, String value){
		double posiProp = examples.proportion(attribute, value, target.getAttribute(), target.getvalue1());
		double negProp = examples.proportion(attribute, value, target.getAttribute(), target.getvalue2());
		
		if (negProp == 1.0 || posiProp == 1.0){
			return 0.0;
		}
		
		return -1*(posiProp*log2(posiProp))-(negProp*log2(negProp));
	}
	
	private static double log2(double x){
		return Math.log(x)/Math.log(2.0);
	}

	private static void printTree(Node tree, PrintWriter writer) {
		for (Node subTree : tree.getChildren()){
			writer.println();
			for (int i = 0; i < level; i++){
				writer.print("\t");
			}
			writer.print("If " + tree.getLabel() + " is " + subTree.getValue() + ", then ");
			level++;
			printTree(subTree, writer);
		}
		if (tree.getChildren().isEmpty()){
			writer.print(target.getAttribute() + " is " + tree.getLabel() + ".");
		}
		level--;
	}

}

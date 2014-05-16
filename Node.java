import java.util.ArrayList;


public class Node {
	String label;
	ArrayList<Node> children;
	String value = null;
	
	public Node(String label){
		this.label = label;
		children = new ArrayList<Node>();
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void addNode(Node node, String value){
		if (null != node){
			node.setValue(value);
			children.add(node);
		}
	}
	
	public ArrayList<Node> getChildren(){
		return children;
	}
	
	public String getLabel(){
		return label;
	}
}

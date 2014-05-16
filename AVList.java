import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class AVList {
	private String attribute;
	private ArrayList<String> values;
	private Set<String> uniqueValues;
	private int distinctCount = 0;
	private double gain;
	
	public AVList(String attribute){
		this.attribute = attribute;
		this.values = new ArrayList<String>();
	}
	
	public void add(String value){
		values.add(value);
	}

	@Override
	public String toString() {
		return "AVPair [attribute=" + attribute + ", values=" + values + "]";
	}
	
	public void setUniqueValues(){
		uniqueValues = new HashSet<String>();
		uniqueValues.addAll(values);
		distinctCount = uniqueValues.size();
	}
	
	public Set<String> getUniqueValues(){
		return uniqueValues;
	}
	
	public int getUniqueCount(){
		return distinctCount;
	}
	
	public String getAttribute(){
		return attribute;
	}
	
	public void setGain(double gain){
		this.gain = gain;
	}
	
	public double getGain(){
		return gain;
	}
	
	public boolean isBinary(){
		setUniqueValues();
		return getUniqueCount() == 2;
	}
}

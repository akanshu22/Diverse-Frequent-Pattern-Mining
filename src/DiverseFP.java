import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import spmf.*;

public class DiverseFP {
	private BufferedReader in;
	private HashMap<Integer,Integer> hierarchyMap;
	private ArrayList<ArrayList<Integer>> itemset;
	private double min_support;
	private double min_diversity_threshold;
	private int height_of_concept_hierarchy;
	private String input_filename;
	private String output_filename;
	private String concept_hierarchy_filename;
	private String diversefp_output_filename;

	//Main method to demonstrate the DiverseFP algorithm
	public static void main(String [] arg) throws IOException{
		DiverseFP ob = new DiverseFP();
	    
		ob.min_support = 0.5; 											//Relative Support
		ob.min_diversity_threshold = 0.4;								//User-specified minimum diversity threshold
		//ob.height_of_concept_hierarchy = 3;							//Height of the concept hierarchy
		ob.input_filename = ".//mushroom.dat";							//Dataset file name for Frequent Item Set Mining
		ob.output_filename = ".//frequent_item_set_output.txt";			//Output file name for Frequent Item Set
		ob.concept_hierarchy_filename = ".//hierarchy.txt";				//Concept Hierarchy file name
		ob.diversefp_output_filename=".//diversefp_output.txt";			//Output file name for Diverse Frequent Item Set
		
		ob.extractFrequentItemSets(ob.input_filename, ob.output_filename, ob.min_support);
		ob.hierarchyMap = ob.makeHierarchyTreeUsingHashMaps(ob.concept_hierarchy_filename);
		
		ob.itemset = ob.formatFrequentItemSets(ob.output_filename);
		ob.diverseFP(ob.itemset, ob.height_of_concept_hierarchy, ob.min_diversity_threshold, ob.diversefp_output_filename);		
	}
	
	//diverseFP Function to calculate the DR value of Frequent Item sets and to determine all the Diverse Frequent Item sets
	private void diverseFP(ArrayList<ArrayList<Integer>> itemset, int h, double min_div, String filename) throws IOException{
		double plf[] = new double[h];
		for(int l=h-1;l>=1;l--)
			plf[l]=2*(h-l)/((float)(h-1)*h);
		int count=0;
		System.out.println("\n\n");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		
		for(ArrayList<Integer> items:itemset){
			double dr=0;
			int l = h-1;
			int gfp[] = new int[h+1];
			gfp[h]=items.size();
			ArrayList<Integer> gfp_list,list;
			list = items;
			
			while(true){
				gfp_list = GFPGenerate(list,l);
				gfp[l]=gfp_list.size();
				if(gfp_list.size()==1)
					break;
				int mf = (gfp[l]-1)/(gfp[l+1]-1);
				dr+=plf[l]*mf;
				list=gfp_list;
				l--;
			}
			if(dr > min_div){
				count++;
				String dfp="";
				System.out.print("DiverseFP #"+count+":\t");
				for(int item:items){
					System.out.print(item+" ");
					dfp=dfp.concat(item+" ");
				}
				System.out.println("\tDR Value: "+dr);
				dfp=dfp.concat("\t#DR Value: "+dr+"\n");
				bw.write(dfp);
			}	
		}
		bw.close();
		System.out.println("\n\nYou can view the Diverse Frequent Item Sets in "+filename+" file.");
	}	
	
	//GFPGenerate function to calculate GFP value of the given frequent pattern at l level
	private ArrayList<Integer> GFPGenerate(ArrayList<Integer> items, int l) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for(int item:items){
			int key = hierarchyMap.get(item);
			if(!list.contains(key))
				list.add(key);
		}
		
		return list;
	}

	//Function to extract the Frequent Item Sets using Apriori Algorithm
	private void extractFrequentItemSets(String input_file_name, String output_file_name, double min_support) throws IOException{
		//Applying the Apriori algorithm and finding Frequent Item sets
		AlgoApriori apriori = new AlgoApriori();
		apriori.runAlgorithm(min_support, java.net.URLDecoder.decode(input_file_name,"UTF-8"), output_file_name);
		apriori.printStats();
	}
	
	//Function to fetch the HierarchyTree from hierarchy.txt file and calculate the height of Hierarchy Tree
	public HashMap<Integer,Integer> makeHierarchyTreeUsingHashMaps(String filename) throws IOException{
		in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filename))));
	    ArrayList<String> hierarchy = new ArrayList<String>();
	    String line;
	    while((line = in.readLine()) != null){  
			hierarchy.add(line);
	    }
	    
	    HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
	    int temp=0, height=1;
	    
	    for(int i = hierarchy.size()-1;i>=0;i--)
	    {
	    	line = hierarchy.get(i);
	    	line =line.replace('[',' ');
	    	line =line.replace(']',' ');
	    	line =line.replace(':',' ');
	    	line =line.replace(',',' ');
	    	String[] arr=line.split(" ");
	    	int j=0,t=0;
    		for(String a:arr)
    			if(a.trim().length()!=0){
    				j++;
    				if(j==1){
    					if(a.equals("root"))
    						t=0;
    					else
    						t=Integer.valueOf(a);
    				}
    				else{
    					hm.put(Integer.valueOf(a), t);
    					if(i==hierarchy.size()-1 && j==2)
    						temp=Integer.valueOf(a);
    				}
    			}
	    }
	    in.close();
	    
	    //Logic to calculate the height of Concept Hierarchy Tree
	    while(hm.get(temp)!=0){
	    	height++;
	    	temp=hm.get(temp);
	    }
	    this.height_of_concept_hierarchy = height;
	    
	    return hm;
	}
	
	//Function to fetch the FrequentItemSet from output.txt file
	public ArrayList<ArrayList<Integer>> formatFrequentItemSets(String filename) throws IOException{
		this.in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filename))));
		ArrayList<ArrayList<Integer>> itemsets = new ArrayList<ArrayList<Integer>>();
		String line;
	    while((line = in.readLine()) != null){  
	    	line =line.replaceAll("#SUP: [0-9]*","");
	    	
	    	String[] arr=line.split(" ");
	    	ArrayList<Integer> item = new ArrayList<Integer>();
    		for(String a:arr)
    			if(a.trim().length()!=0){
    				item.add(Integer.valueOf(a));
    			}
    		itemsets.add(item);
	    }
	    in.close();
	    return itemsets;
	}
}
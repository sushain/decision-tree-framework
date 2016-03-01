/**
 * <code>DecisionTree</code> class provides the key implementation of building a decision
 * tree using the TreeNode and DataPoint instances.
 *
 * @author pandit
 * 
 * @version 1.0
 *
 * Revision Log
 *
 * Date       Version        Description
 ******************************************************************************
 * 17th Nov    1.0     	First cut at DecisionTree
 * 
 * 18th Nov	   1.1		Added code for Max-Gain
 ******************************************************************************
 */

import java.util.Iterator;
import java.util.Vector;

public class DecisionTree {

	private int numAttributes;

	private String [] attributeNames;

	private Vector [] domains;
	
	private TreeNode trainingRoot;
	
	private TreeNode testingRoot;
	
	private String namesFile;
	
	private String trainingDataFile;
	
	private String testingDataFile;
	
	private int correctCount;
	
	private int inCorrectCount;
	
	private boolean debug;
	
	private int maxDepth;
	
	
	public DecisionTree (final String namesFile, final String trainingDataFile, final String testingDataFile, final int maxDepth, final boolean debug) {
		
		this.namesFile = namesFile;
		
		this.trainingDataFile = trainingDataFile;
		
		this.testingDataFile = testingDataFile;
		
		this.maxDepth = maxDepth;
		
		this.debug = debug;
	}
	
	public void process () throws Exception {
		
		/**********************************************************************
		 * This call handles all the input processing including splitting the * 
		 * training data, if required, in case when the testing data set is   *
		 * not given. In that case, it splits the data into 2/3rd - 1/3rd,    *
		 * if testingDataFile is set to "SPLIT".  							  *
		 **********************************************************************/		
		
		new InputProcessor (this);
		
		/**********************************************************************/

		// create and display the decision tree induced on the trainingRoot
		createDecisionTree ();
	}
	
	public void createDecisionTree () {

		induce (trainingRoot);

		new DisplayProcessor (this.maxDepth).displayTree (trainingRoot, "", this.numAttributes, this.attributeNames, this.domains, 1);
		
		/*****************************************************
		 *  On to the classification of the testing set now  *	
		 *****************************************************/
		
		Iterator panditIterator = testingRoot.data.iterator ();
			
		System.out.println ("\n*****************************************************");
		
		System.out.println ("\nClassifying Test Data Set");
		
		while (panditIterator.hasNext ()) {
		
			DataPoint spanPoint = (DataPoint) panditIterator.next ();
			
			if (debug) {
				
				System.out.println ("\n*****************************************************");
			
				System.out.println ("Classifying: " + spanPoint.label);
			}
			
			categorize (trainingRoot, "", spanPoint);
		}
		
		System.out.println ("\n*******************************************************");
		
		System.out.println ("\n\t\t   Overall Results");
		
		System.out.println ("\n Correctly Classified = " + correctCount);
		
		System.out.println ("\n Incorrectly Classified = " + inCorrectCount);
		
		System.out.println ("\n Percentage Incorrect = " + (1. * inCorrectCount * 100 / (correctCount + inCorrectCount)));
		
		System.out.println ("\n*******************************************************");
	}
	
	public Vector getSubset (final Vector data, final int attribute, final int value) {

		Vector subset = new Vector ();

		int num = data.size ();

		for (int i = 0; i < num; i++) {

			DataPoint point = (DataPoint) data.elementAt (i);

			if (point.attributes[attribute] == value) {
				
				subset.addElement (point);
			}
		}

		return subset;
	}

	public double calculateEntropy (final Vector data) {

		int numdata = data.size ();

		if (numdata == 0) {
		
			return 0;
		}

		int attribute = numAttributes - 1;

		int numvalues = domains[attribute].size ();

		double sum = 0;

		for (int i = 0; i < numvalues; i++) {

			int count = 0;

			for (int j = 0; j < numdata; j++) {

				DataPoint point = (DataPoint) data.elementAt (j);

				if (point.attributes[attribute] == i) {
				
					count++;
				}
			}

			double probability = ((double) count) / numdata;
			
			//System.out.print (probability);

			if (count > 0) {
				
				sum += -probability * Math.log (probability);
			}
		}
		
		return sum;
	}

	public boolean alreadyUsedToDecompose (final TreeNode node, final int attribute) {

		if (node.children != null) {

			if (node.decompositionAttribute == attribute) {

				return true;
			}
		}

		if (node.parent == null) {
		
			return false;
		}

		return alreadyUsedToDecompose (node.parent, attribute);
	}

	public void categorize (final TreeNode node, final String tab, final DataPoint data) {
	
		int outputattr = numAttributes - 1;

		if (node.children == null) {

			int [] values = DisplayProcessor.getAllValues (node.data, outputattr, domains);

			if (values.length == 1) {

				if (debug) {
					
					System.out.print (" " + domains[outputattr].elementAt (values[0]));
				}
				
				if (domains[outputattr].elementAt (values[0]).equals (domains[outputattr].elementAt (data.attributes[outputattr]))) {
					
					correctCount = correctCount + 1; 
					
					if (debug) {
						
						System.out.print ("\n\n--- Correctly Classified ---");
					}
					
				} else {
					
					inCorrectCount = inCorrectCount + 1;
						
					System.out.print ("\n" + data.label);
					
					System.out.println ("  --- Incorrectly Classified ---");
				}
				
				return;
			}

			if (debug) {
				
				System.out.print (" {");
			}

			for (int i = 0; i < values.length; i++) {
				
				if (debug) {
					
					System.out.print ("\"" + domains[outputattr].elementAt (values[i]) + "\"");

					if (i != values.length - 1) {
						
						System.out.print (" , ");
					}
				}
			}
			
			if (debug) {
				
				System.out.print (" };");
			}

			return;
		}

		int numvalues = node.children.length;

		for (int i = 0; i < numvalues; i++) {

			if (domains[node.decompositionAttribute].elementAt (i).equals (domains[node.decompositionAttribute].elementAt (data.attributes[node.decompositionAttribute]))) {
				
				if (debug) {
					
					System.out.print ("\n" + tab + attributeNames[node.decompositionAttribute] + " = " + domains[node.decompositionAttribute].elementAt (i) + ":");
				}

				categorize (node.children[i], tab + ":   ", data);
				
				break;
			}
		}
	}
	
	public void induce (final TreeNode node) {

		double bestEntropy = 0;

		boolean selected = false;

		int selectedAttribute = 0;

		int numdata = node.data.size ();

		int numinputattributes = numAttributes - 1;

		node.entropy = calculateEntropy (node.data);

		if (node.entropy == 0) {
		
			return;
		}

		for (int i = 0; i < numinputattributes; i++) {

			int numvalues = domains[i].size ();

			if (alreadyUsedToDecompose (node, i)) {
			
				continue;
			}

			double averageentropy = 0;

			for (int j = 0; j < numvalues; j++) {

				Vector subset = getSubset (node.data, i, j);

				if (subset.size () == 0) {
				
					continue;
				}

				double subentropy = calculateEntropy (subset);

				averageentropy += subentropy * 	subset.size ();
			}

			averageentropy = averageentropy / numdata;

			if (selected == false) {

				selected = true;

				bestEntropy = averageentropy;

				selectedAttribute = i;

			} else {

				if (averageentropy < bestEntropy) {

					selected = true;

					bestEntropy = averageentropy;

					selectedAttribute = i;
				}

			}
			/*
			 * if ( alreadyUsedToDecompose(node, i) ) continue;
			 * 
			 * selectedAttribute = i; selected = true; break;
			 */
		}

		if (selected == false) {
			
			return;
		}

		int numvalues = domains[selectedAttribute].size ();

		node.decompositionAttribute = selectedAttribute;

		node.children = new TreeNode [numvalues];

		for (int j = 0; j < numvalues; j++) {

			node.children[j] = new TreeNode ();

			node.children[j].parent = node;

			node.children[j].data = getSubset (node.data,

			selectedAttribute, j);

			node.children[j].decompositionValue = j;
		}

		for (int j = 0; j < numvalues; j++) {

			induce (node.children[j]);
		}

		node.data = null;
	}
		
	public void setNumAttributes (int numAttributes) {
	
		this.numAttributes = numAttributes;
	}

	public void setAttributeNames (final String [] attributeNames) {
	
		this.attributeNames = attributeNames;
	}

	public String getTrainingDataFile () {
		
		return this.trainingDataFile;
	}
	
	public void setTrainingDataFile (final String name) {
		
		this.trainingDataFile = name;
	}

	public String getTestingDataFile () {
		
		return this.testingDataFile;
	}

	public void setTestingDataFile (final String name) {
		
		this.testingDataFile = name;
	}

	public void setDomains (final Vector [] domains) {
	
		this.domains = domains;
	}
	
	public void setTestingRoot (final TreeNode testingRoot) {
		
		this.testingRoot = testingRoot;
	}
	
	public void setTrainingRoot (final TreeNode trainingRoot) {
		
		this.trainingRoot = trainingRoot;
	}
	
	public String getNamesFile () {
		
		return this.namesFile;
	}
}

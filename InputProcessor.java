/**
 * <code>InputProcessor</code> provides the key functionality to process the 
 * input data files and populate the Node structures. 
 *
 * @author pandit
 * 
 * @version 1.0
 *
 * Revision Log
 *
 * Date       Version        Description
 ******************************************************************************
 * 17th Nov    1.0     	First cut at InputProcessor - reading training set
 *  
 * 18th Nov	   1.1		Added functionality to read the names file separately
 * 
 * 25th Nov    1.2      Added the code to discretize the linear / nominal data
 ******************************************************************************
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class InputProcessor {
	
	private String [] attributeNames;
	
	private Vector [] domains;
	
	private TreeNode trainingRoot = new TreeNode ();
	
	private TreeNode testingRoot = new TreeNode ();
	
	private int numAttributes;
	
	private int skipCount = -1;
	
	private double [] minLinearArray;
	
	private double [] maxLinearArray;
	
	public static final int MAX_POSS = +9999;

	public static final int MIN_POSS = -9999;
	
	/*
	 * The number of discrete classes for linear and nominal data 
	 */
	public static final int NUM_OF_CLASSES = 4;
	
	public static final String TINY = "TINY";
		
	public static final String SMALL = "SMALL";
	
	public static final String MEDIUM = "MEDIUM";
	
	public static final String LARGE = "LARGE";
	
	public static final String HUGE = "HUGE";
	
	/*
	 * Flag-field to indicate whether we've at least one linear or nominal attribute. 
	 */
	private boolean caseOfLinearOrNominal = false; 


	public InputProcessor () {
		
	}
	
	public InputProcessor (DecisionTree instance) throws Exception {

		// read the names file
		if (readAttributes (instance.getNamesFile ()) < 0) {
			
			System.out.println ("Problem reading the names file");
			
			throw new IOException ();
		} 
		
		// check whether the names-file had any nominal or linear attributes; 
		// if yes, then create new training / testing files after pre-processing 
		// the train / test data into discreet bins - tiny / small / medium / large / huge  
		if (caseOfLinearOrNominal) {
			
			computeMinMaxInTrainingSet (instance.getTrainingDataFile ());
			
			// populate training data set - moreover, based on whether a test set is provided or not, split training data into 
			// 2/3-1/3 for train-test.
			if (discretize (instance.getTrainingDataFile (), this.trainingRoot, instance.getTestingDataFile ()) < 0) {
				
				System.out.println ("Problem reading the training file");
				
				throw new IOException ();
			}
			
			// if the testing file is given, then load the testing data set from it,
			// otherwise skip, since testingRoot would be already populated in the above 'if'.
			if (instance.getTestingDataFile ().equals ("SPLIT") == false) {
				
				if (discretize (instance.getTestingDataFile (), this.testingRoot, "DONT_SPLIT") < 0) {
					
					System.out.println ("Problem reading the testing file");
					
					throw new IOException ();
				}
			}
		}
	
		else {
			
			// populate training data set - moreover, based on whether a test set is provided or not, split training data into 
			// 2/3-1/3 for train-test.
			if (readDataSets (instance.getTrainingDataFile (), this.trainingRoot, instance.getTestingDataFile ()) < 0) {
				
				System.out.println ("Problem reading the training file");
				
				throw new IOException ();
			} 
			
			// if the testing file is given, then load the testing data set from it,
			// otherwise skip, since testingRoot would be already populated in the above 'if'.
			if (instance.getTestingDataFile ().equals ("SPLIT") == false) {
				
				if (readDataSets (instance.getTestingDataFile (), this.testingRoot, "DONT_SPLIT") < 0) {
					
					System.out.println ("Problem reading the testing file");
					
					throw new IOException ();
				}
			}
		}
		
		
		// set all the values back in the decision tree class.
		instance.setTrainingRoot (this.trainingRoot);
		
		instance.setTestingRoot (this.testingRoot);
		
		instance.setAttributeNames (this.attributeNames);
		
		instance.setDomains (this.domains);
		
		instance.setNumAttributes (this.numAttributes);
	}

	
	public int readAttributes (final String namesFile) throws Exception {
		
		FileInputStream in = null;

		/********************************************
		 * Reading the Names File					*
		 ********************************************/
		try {

			File inputFile = new File (namesFile);

			in = new FileInputStream (inputFile);

		} catch (Exception e) {

			System.err.println ("Unable to open the names file: " + namesFile + "\n" + e);

			return -1;
		}

		BufferedReader bin = new BufferedReader (new InputStreamReader (in));

		String input = bin.readLine ();
		
		if (input == null) {

			System.err.println ("No data found in the names file: " + namesFile + "\n");

			return 0;	
		}
		
		String classNames = input.substring (0, input.indexOf (".")); 
			
		Vector tempSpanVector = new Vector ();
		
		int featureCount = 0;
		
		while ((input = bin.readLine ()) != null) {
			
			if (input.startsWith ("|")) {
			
				continue;
				
			} else if (input.contains ("|")) {
				
				input = input.substring (0, input.indexOf ("|"));
			}
			
			if (input.equals ("")) {
				
				continue;
			}
			
			// Strip the domains of the attributes as we'll be anyways reading those from
			// the actual data sets - needn't store them here
			try {
				
				if (input.substring (input.indexOf (":")).contains ("linear") || input.substring (input.indexOf (":")).contains ("nominal")) {
					
					caseOfLinearOrNominal = true;
				}
				
				input = input.substring (0, input.indexOf (":"));
				
			} catch (IndexOutOfBoundsException excp) {
				
				if (input.contains ("label")) {
					
					skipCount = featureCount;
					
					continue;
					
					// Done - change code for test data to skip label values
					
				} else {
					
					System.out.print ("Bad formatting in names file - missing ':' for some field");
					
					System.exit (1);
				}
			}
			
			// skip the class names - store at the end of the attributeNames array
			if (input.equals (classNames)) {
				
				continue;
			}

			tempSpanVector.addElement (input);
			
			featureCount = featureCount + 1;
		}
		
		tempSpanVector.addElement (classNames);
		
		numAttributes = tempSpanVector.size ();
		
		if (numAttributes <= 1) {
			
			return -1;
		}
		
		domains = new Vector [numAttributes];

		for (int i = 0; i < numAttributes; i++) {
		
			domains[i] = new Vector ();
		}

		attributeNames = new String [numAttributes];

		Iterator spanIterator = tempSpanVector.iterator ();
		
		int index = 0;
		
		while (spanIterator.hasNext ()) {
			
			attributeNames[index] = (String) spanIterator.next ();
			
			index = index + 1;
		}
		
		in.close ();
		
		bin.close ();
		
		return 1;		
	}
	
	
	public int readDataSets (final String fileName, final TreeNode root, final String toSplit) throws Exception {

		FileInputStream in = null;
		
		/************************************************
		 * Reading the Training / Testing data set File	*
		 ************************************************/		
		try {

			File inputFile = new File (fileName);

			in = new FileInputStream (inputFile);

		} catch (Exception e) {

			System.err.println ("Unable to open file: " + fileName + "\n" + e);

			return -1;
		}

		BufferedReader bin = new BufferedReader (new InputStreamReader (in));

		String input;
		
		int index = 1;
		
		while (true) {

			input = bin.readLine ();

			if (input == null) {
				
				break;
				
			} else if (input.startsWith ("|")) {
			
				continue;
				
			} else if (input.contains ("|")) {
				
				input = input.substring (0, input.indexOf ("|"));
			}
			
			if (input.equals ("")) {
				
				continue;
			}

			StringTokenizer tokenizer = new StringTokenizer (input, ",");

			int numtokens = tokenizer.countTokens ();
			
			if (skipCount > -1) {
				
				if (numtokens != numAttributes + 1) {
					
					return -1;
				}
				
			} else if (numtokens != numAttributes) {

				return -1;
			}

			DataPoint point = new DataPoint (numAttributes);

			// if there is no label to skip
			if (skipCount == -1) {
				
				point.label = "Example#" + index;

				for (int i = 0; i < numAttributes; i++) {

					point.attributes[i] = getSymbolValue (i, tokenizer.nextToken ());
				}
				
			} else if (skipCount > -1) {

				int attributeIndex = 0;
				
				for (int panditIndex = 0; panditIndex < numAttributes + 1; panditIndex++) {

					// assign label to the data point and skip it as an attribute field
					if (panditIndex == skipCount) {
						
						point.label = tokenizer.nextToken ();
						
						continue;
					}
					
					point.attributes[attributeIndex] = getSymbolValue (attributeIndex, tokenizer.nextToken ());
					
					attributeIndex = attributeIndex + 1;
				}
			}
			
			
			/************************************************
			 * Required 2/3-1/3 random data split follows 	*
			 ************************************************/		
			if (toSplit.equals ("SPLIT")) {
				
				double randomNumber = 3 * Math.random ();
				
				if (randomNumber > 2) {
					
					testingRoot.data.addElement (point);					
					
				} else {
				
					root.data.addElement (point);
				}
				
			} else {
				
				root.data.addElement (point);
			}
			
			index = index + 1;
		}
		
		in.close ();
		
		bin.close ();
		
		return 1;
	}
	
	
	public int getSymbolValue (final int attribute, final String symbol) {

		int index = domains[attribute].indexOf (symbol);

		if (index < 0) {

			domains[attribute].addElement (symbol);

			return domains[attribute].size () - 1;
		}
		
		return index;
	}

	
	public int discretize (final String fileName, final TreeNode root, final String toSplit) throws Exception {

		FileInputStream in = null;
		
		/*****************************************************************
		 * Reading and discretizing the Training / Testing data set File *
		 *****************************************************************/
		try {

			File inputFile = new File (fileName);

			in = new FileInputStream (inputFile);

		} catch (Exception e) {

			System.err.println ("Unable to open file: " + fileName + "\n" + e);

			return -1;
		}

		BufferedReader bin = new BufferedReader (new InputStreamReader (in));

		String input;
		
		int index = 1;
		
		while (true) {

			input = bin.readLine ();

			if (input == null) {
				
				break;
				
			} else if (input.startsWith ("|")) {
			
				continue;
				
			} else if (input.contains ("|")) {
				
				input = input.substring (0, input.indexOf ("|"));
			}
			
			if (input.equals ("")) {
				
				continue;
			}

			StringTokenizer tokenizer = new StringTokenizer (input, ",");

			int numtokens = tokenizer.countTokens ();
			
			if (skipCount > -1) {
				
				if (numtokens != numAttributes + 1) {
					
					return -1;
				}
				
			} else if (numtokens != numAttributes) {

				return -1;
			}

			DataPoint point = new DataPoint (numAttributes);

			// if there is no label to skip
			if (skipCount == -1) {
				
				point.label = "Example#" + index;
				
				String newString;

				for (int attributeIndex = 0; attributeIndex < numAttributes; attributeIndex++) {
			
					// don't discretize the class attribute
					if (attributeIndex == numAttributes-1) {
					
						point.attributes[attributeIndex] = getSymbolValue (attributeIndex, tokenizer.nextToken ());
						
						continue;
					}
						
					try {
						
						double nextToken = Double.valueOf (tokenizer.nextToken ());
						
						// Based on the computed bin string, form the new string to be written
						// to the new training / testing file
						newString = computeBin (nextToken, attributeIndex);
						
					} catch (NumberFormatException excp) {
						
						// case when a '?' is encountered - assign the 'HUGE' class by default
						newString = HUGE;
					}

					point.attributes[attributeIndex] = getSymbolValue (attributeIndex, newString);
				}
				
			} else if (skipCount > -1) {

				int attributeIndex = 0;
				
				String newString; 
				
				for (int panditIndex = 0; panditIndex < numAttributes + 1; panditIndex++) {

					// assign label to the data point and skip it as an attribute field
					if (panditIndex == skipCount) {
						
						point.label = tokenizer.nextToken ();
						
						continue;
					}
					
					// don't discretize the class attribute
					if (panditIndex == numAttributes) {
					
						point.attributes[attributeIndex] = getSymbolValue (attributeIndex, tokenizer.nextToken ());
						
						attributeIndex = attributeIndex + 1;
						
						continue;
					}
					
					try {
						
						double nextToken = Double.valueOf (tokenizer.nextToken ());
						
						// Based on the computed bin string, form the new string to be written
						// to the new training / testing file
						newString = computeBin (nextToken, index);
						
					} catch (NumberFormatException excp) {
						
						// case when a '?' is encountered - assign the 'HUGE' class by default
						newString = HUGE;
					}

					point.attributes[attributeIndex] = getSymbolValue (attributeIndex, newString);
					
					attributeIndex = attributeIndex + 1;
				}
			}
			
			
			/************************************************
			 * Required 2/3-1/3 random data split follows 	*
			 ************************************************/		
			if (toSplit.equals ("SPLIT")) {
				
				double randomNumber = 3 * Math.random ();
				
				if (randomNumber > 2) {
					
					testingRoot.data.addElement (point);					
					
				} else {
				
					root.data.addElement (point);
				}
				
			} else {
				
				root.data.addElement (point);
			}
			
			index = index + 1;
		}
		
		in.close ();
		
		bin.close ();
		
		return 1;
	}
	
	
	/**
	 * Computes the Bin for the passed token based on the min / max
	 * values of the corresponding nominal / linear attribute 
	 * 
	 * @param token
	 * @param whichAttribute
	 * 
	 * @return a String representing a discrete bin
	 */
	public String computeBin (final double token, final int whichAttribute) {
	
		int bin = -1;
		
		for (int index = 1; index <= NUM_OF_CLASSES; index++) {
			
			double classComparisonValue = 1. * (index * (this.maxLinearArray[whichAttribute] - this.minLinearArray[whichAttribute]) / NUM_OF_CLASSES) + this.minLinearArray[whichAttribute];
			
			if (token <= classComparisonValue) {
				
				bin = index;
				
				break;
			}
		}
		
		// The encountered token extends beyond the defined limits as per the training set;
		// thus, assign it to the highest class (unbounded)
		if (bin == -1) {
			
			bin = NUM_OF_CLASSES + 1;
		}
		
		switch (bin) {
			
			case 1: return TINY;
			
			case 2: return SMALL;
			
			case 3: return MEDIUM;
			
			case 4: return LARGE;
			
			case 5: return HUGE;
			
			default: return HUGE;
		}
	}
	
	
	/**
	 * This function computes the min / max bounds for a each
	 * nominal / linear attribute in the training data set	 *  
	 * 
	 * @param trainingFileName
	 * 
	 * @throws IOException
	 */
	public void computeMinMaxInTrainingSet (final String trainingFileName) throws IOException {
		
		FileInputStream in = null;
		
		/*********************************************************
		 * Computing min / max of the linear and nominal data in *
		 * the training data set. 								 *
		 *********************************************************/		
		try {

			File inputFile = new File (trainingFileName);

			in = new FileInputStream (inputFile);

		} catch (Exception e) {

			System.err.println ("Unable to open file during discretization process: " + trainingFileName + "\n" + e);
			
			System.exit (-1);
		}

		BufferedReader bin = new BufferedReader (new InputStreamReader (in));

		String input = bin.readLine ();
		
		if (input == null) {
		
			System.out.println ("File empty: " + trainingFileName);
			
			System.exit (1);
		}
		
		StringTokenizer tokenizer = new StringTokenizer (input, ",");

		int numTokens = tokenizer.countTokens ();		
		
		double [] minArray = new double [numTokens];
		
		double [] maxArray = new double [numTokens];

		for (int index = 0; index < numTokens; index++) {
			
			minArray[index] = MAX_POSS;
			
			maxArray[index] = MIN_POSS;
		}
			
		while (true) {

			if (input == null) {
				
				break;
				
			} else if (input.startsWith ("|")) {
			
				continue;
				
			} else if (input.contains ("|")) {
				
				input = input.substring (0, input.indexOf ("|"));
			}
			
			if (input.equals ("")) {
				
				continue;
			}

			tokenizer = new StringTokenizer (input, ",");

			numTokens = tokenizer.countTokens ();
			
			for (int index = 0; index < numTokens; index++) {
				
				try {
					
					double nextToken = Double.valueOf (tokenizer.nextToken ());
					
					if (nextToken < minArray[index]) {
						
						minArray[index] = nextToken;
					}
					
					if (nextToken > maxArray[index]) {
						
						maxArray[index] = nextToken;
					} 
					
				} catch (NumberFormatException excp) {
					
					// case when a '?' is encountered - do nothing 
				}
			}
			
			input = bin.readLine ();
		}
		
		in.close ();
		
		bin.close ();
		
		this.minLinearArray = minArray;
		
		this.maxLinearArray = maxArray;
	}
}
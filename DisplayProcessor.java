/**
 * <code>DisplayProcessor</code> class provides the implementation for handling
 * the display functionality.
 *
 * @author pandit
 * 
 * @version 1.0
 *
 * Revision Log
 *
 * Date       Version        Description
 ******************************************************************************
 * 17th Nov    1.0     	First cut at DisplayProcessor
 * 
 * 18th Nov	   1.1		Modified the code for better formatting
 ******************************************************************************
 */

import java.util.Vector;

public class DisplayProcessor {

	private static int MAX_DEPTH;
	
	
	public DisplayProcessor (final int maxDepth) {
		
		this.MAX_DEPTH = maxDepth;
	}
	
	public void displayTree (final TreeNode node, final String tab, final int numAttributes, final String [] attributeNames, final Vector [] domains, final int depth) {

		int outputattr = numAttributes - 1;

		if (node.children == null) {

			int [] values = getAllValues (node.data, outputattr, domains);

			if (values.length == 1) {

				System.out.print (" " + domains[outputattr].elementAt (values[0]));

				return;
			}

			System.out.print (" {");

			for (int i = 0; i < values.length; i++) {

				System.out.print ("\"" + domains[outputattr].elementAt (values[i]) + "\"");

				if (i != values.length - 1) {
					
					System.out.print (" , ");
				}
			}
			
			System.out.print (" };");

			return;
		}

		int numvalues = node.children.length;

		for (int i = 0; i < numvalues; i++) {

			System.out.print ("\n" + tab + attributeNames[node.decompositionAttribute] + " = " + domains[node.decompositionAttribute].elementAt (i) + ":");
				
			if (depth <= this.MAX_DEPTH) {
				
				displayTree (node.children[i], tab + ":   ", numAttributes, attributeNames, domains, depth + 1);
				
			} else {
				
				System.out.print ("...");
			}

			if (i != numvalues - 1) {
				
				System.out.print ("\t");
				
			} else {
				
				System.out.print ("");
			}
		}
	}
	
	public static int [] getAllValues (final Vector data, final int attribute, final Vector [] domains) {

		Vector values = new Vector ();

		int num = data.size ();

		for (int i = 0; i < num; i++) {

			DataPoint point = (DataPoint) data.elementAt (i);

			String symbol =	(String) domains[attribute].elementAt (point.attributes[attribute]);

			int index = values.indexOf (symbol);

			if (index < 0) {

				values.addElement (symbol);
			}
		}

		int [] array = new int [values.size ()];

		for (int i = 0; i < array.length; i++) {

			String symbol = (String) values.elementAt (i);

			array[i] = domains[attribute].indexOf (symbol);
		}

		values = null;

		return array;
	}
}
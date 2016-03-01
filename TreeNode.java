/**
 * <code>TreeNode</code> provides an implementation of a node to be used 
 * in the decision tree.
 *
 * @author pandit
 * 
 * @version 1.0
 *
 * Revision Log
 *
 * Date       Version        Description
 ******************************************************************************
 * 17th Nov    1.0     	First cut at TreeNode
 ******************************************************************************
 */

import java.util.Vector;

class TreeNode {

	public double entropy;

	public Vector data;

	public int decompositionAttribute;

	public int decompositionValue;

	public TreeNode [] children;

	public TreeNode parent;

	
	public TreeNode () {

		data = new Vector ();
	}
};
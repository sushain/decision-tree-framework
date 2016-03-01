/**
 * <code>DataPoint</code> provides an implementation of a data point 
 * consisting of values of the associated attributes
 *
 * @author pandit
 * 
 * @version 1.0
 *
 * Revision Log
 *
 * Date       Version        Description
 ******************************************************************************
 * 17th Nov    1.0     	First cut at DataPoint
 ******************************************************************************
 */

class DataPoint {

	public int [] attributes;
	
	public String label;

	public DataPoint (final int numattributes) {

		attributes = new int [numattributes];
	}
};

/**
 * <code>C5</code> class provides an entry-point for the Decision-Tree framework
 *
 * @author pandit
 *
 * @version 1.0
 *
 * Revision Log
 *
 * Date       Version        Description
 ******************************************************************************
 * 17th Nov    1.0     	First cut at C5
 *
 * 18th Nov    1.1     	Modified code to add extended command-line support
 *
 * 25th Nov	   1.2	 	Ignored the label in audiology dataset
 * 						Incorporated max-depth argument
 * 						Checked outputs of wrongly classified examples
 * 						Discretized linear and nominal values
 ******************************************************************************
 */


public class C5 {

	private static boolean debug = false;

	private static DecisionTree instance;


	public C5 () {

		super ();
	}

	public static void main (final String [] args) throws Exception {

	    if (args.length == 2) {

	    	instance = new DecisionTree (args[0], args[1], "SPLIT", 3, debug);

	    } else if (args.length == 3) {

	    	// Determine what is passed as the 3rd argument - depth or the test-set-filename
	    	// (since both are optional parameters)
	    	try {
	    			instance = new DecisionTree (args[0], args[1], "SPLIT", Integer.valueOf(args[2]), debug);

	    	} catch (NumberFormatException excp) {

	    		instance = new DecisionTree (args[0], args[1], args[2], 3, debug);
	    	}

		} else if (args.length == 4) {

			instance = new DecisionTree (args[0], args[1], args[2], Integer.valueOf (args[3]), debug);

		} else {

			System.out.println ("\nUsage - java C5 <names-File> <training-Set-Filename> [testing-Set-Filename] [max-depth]");

			System.out.println ("\n[Optional] - [testing-Set-Filename] - defaults to 1/3rd of training-file sampled randomly");

			System.out.println ("\n[Optional] - [max-depth] - defaults to 3 | Enter a large value (like 100) to make it redundant");

			System.out.println ("\nNote: In case you're not providing any of the optional parameters, you needn't worry about the order; as in whether [max-depth] can come as the 3rd argument in case [testing-Set-Filename] isn't present. The framework will take care of this :-)");

			System.exit (0);
		}

	    instance.process ();

	    Utils.waitHere ("Press a key to exit");
	}
}

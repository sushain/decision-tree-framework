class Utils {

	// In Microsoft's J++, the console window can close up before you get a
	// chance to read it,
	// so this method can be used to wait until you're ready to proceed.
	public static void waitHere (final String msg) {

		System.out.println ("");
		
		System.out.print (msg);
		
		try {
		
			System.in.read ();
		
		} catch (Exception e) {
			
		} // Ignore any errors while reading.
	}

	// Note: this does NOT add a final "linefeed" to the file. Might not want to
	// get that upon a subsequent "read" of the file.
	static synchronized boolean writeStringToFile (String contents, String fileName) {

		try {
			
			// Want this to be an auto-flush file.
			java.io.File file = new java.io.File (fileName);
			
			java.io.PrintWriter stream = new java.io.PrintWriter (new java.io.FileOutputStream (file), true);

			stream.print (contents);
			
			stream.close ();

			return true;
			
		} catch (Exception ioe) {
			
			System.out.println ("Exception writing to " + fileName + ".  Error msg: " + ioe);

			return false;
		}
	}
}

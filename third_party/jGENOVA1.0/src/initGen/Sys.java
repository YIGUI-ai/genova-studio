package initGen;

import java.io.IOException;

public class Sys {


	private static String warning = null;
	
	public static String delimiters;

	public static int lastRun;
	public static int linesPerPage;
	public static int[] matBase;
	public static int offsetPT;
	public static int offsetSE;
	public static int recsToPrint;


	// =========== sysDefaultValues =======
	// start

	public static void sysDefaaultValues() {
		recsToPrint = 10;
		linesPerPage = 56;
		matBase = new int[64];
		offsetPT = 0;
		offsetSE = 0;
		lastRun = 0;
		// =========== initialize matbase
		matSet();
		// ============
		delimiters = "[ ,\t]+";
//		delimiters = ", \t";

	}
	// =========== sysDefaultValues =======
	// end
	// =========== matSet =======
	// start  

	private static void matSet() {

		int i = 0;
		for (int j = 1; j <= 63; j++) {
			i = i + j - 1;
			matBase[j] = i;
		} // end for
	}// end matSet
	// =========== matSet =======
	// end
	// =========== setErrorMsg =======
	// start

	public static void setErrorMsg(String errorMsg) throws IOException {
		if (errorMsg != null) {
			try {
				IO.mybw.newLine();
				IO.mybw.write(" *** " + errorMsg);
				IO.mybw.newLine();
				IO.mybw.newLine();
				IO.mybw.flush();
				System.exit(0);
			} // end try
			catch (Exception e) {
				System.out.println(" Fatal Error: Unable to write to output file");
			} // end catch
			IO.doPrintLine(" System will exit");
			IO.mybw.flush();
			System.exit(1);
		} // end if
	} // end setErrorMsg
	// =========== setErrorMsg =======
	// end
	// =========== setWarning =======
	// start
	public static void setWarning(String message) throws IOException {
		Sys.warning = message;
		try {
			IO.mybw.newLine();
			IO.mybw.write(" *** " + warning);
			IO.mybw.flush();
		} catch (Exception e) {
			System.out.println(" Fatal Error: Unable to " + "write warning to output file");
		} // end catch
	}
	// =========== setWarning =======
	// end
	// =========== getLinesPerPage =======
	// start

	public static int getLinesPerPage() {
		return linesPerPage;
	}
	// =========== getLinesPerPage =======
	// end

}	// end Class Sys

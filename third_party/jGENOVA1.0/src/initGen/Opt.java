package initGen;

import java.io.IOException;

public class Opt {

	private static final String[] OPTIONS =  {
			"ALGO","ALL ","CORR","DTAB","ECOL",
			"EMS ","NEGA","NONE","OFF ","ON  ",
			"RECO","RESE","DEBU","LINE"};

	/**
	 * Opt initializes the private vector OPTIONS of strings
	 * for the options keywords (up to 4 characters - *which may be found on
	 * jGENOVA control cards including the OPTIONS control card
	 * 
	 * OPTIONS [ 0] --> "ALGO" --> Option: ALGORITHM 
	 * OPTIONS [ 1] --> "ALL"  --> Option: records ALL 
	 * OPTIONS [ 2] --> "CORR" --> Option: CORRELATION 
	 * OPTIONS [ 3] --> "DTAB" --> Option: DTABLE 
	 * OPTIONS [ 4] --> "ECOL" --> Option: ECOLOGY 
	 * OPTIONS [ 5] --> "EMS"  --> Option: EMS 
	 * OPTIONS [ 6] --> "NEGA" --> Option: NEGATIVE 
	 * OPTIONS [ 7] --> "NONE" --> Option: records NONE 
	 * OPTIONS [ 8] --> "OFF"  --> Option: debug OFF  
	 * OPTIONS [9] --> "ON"   --> Option: debug ON 
	 * OPTIONS [10] --> "RECO" --> Option: RECORDS 
	 * OPTIONS [11] --> "RESE" --> Option: RESET 
	 * OPTIONS [12] --> "DEBU" --> Option: DEBU 
	 * OPTIONS [13] --> "LINE" --> option: set Lines per page
	 * not yet implemented
	 * 
	 * 
	 * max line per page default 56
	 *
	 * The getOpt takes a string as input and returns an integer value index
	 * (optINdex) into the OPTIONS vector if a match was found and =1 otherwise
	 * 
	 * If a match was found, the value of JGRN.logOPt[optINdex] will be set to
	 * true
	 */
	// ============= getOpt ==============
	// start

	private static int getOpt(String inString) {

		int optIndex = -1;
		inString = inString + "   ";
		inString = inString.substring(0, 4);

		for (int k = 0; k < OPTIONS.length; k++) {
			// input is upper case before it gets here
			if (inString.startsWith(OPTIONS[k])) {
				optIndex = k;
				break;
			} // end if
		} // end for k
		return optIndex;
	}
	// ============= getOpt ==============
	// end
	// ============= processOptions ==============
	// start

	public static void processOptions(String[] args, int first, int numb) throws IOException {

		// ..................... debug area
		StringBuilder sb = new StringBuilder();
		sb.append(" tokens in processOptions: ");
		for (int n = first; n < first + numb; n++) {
			sb.append(args[n]);
			sb.append(" ");
		}
		// ...................................

		String hold;
		int result;
		int k = 0;
		for (int j = 0; j < numb; j++) {
			k = first + j;
			hold = args[k];
			result = getOpt(hold);
			switch (result) {

			case 0:
				Flags.EMS = false;
				break; // ALGORITHM SET EMS to false
			case 2:
				Flags.corrTable = true;
				break;
			case 3:
				Flags.dstudyTable = true;
				break;
			case 4:
				Flags.ecology = true;
				break;
			case 5:
				Flags.EMS = true;
				break;
			case 6:
				Flags.printNegative = true;
				break;
			case 11:
				Flags.setOptionInit();
				break;
			case 10: // here deal with a RECORDS Option
				j = processRecordsOption(args, j, first, numb);
				break;
			case 12: // here deal with a Debug
				j = processDebugOption(args, j, first, numb);
				break;
			case 13:
				j = processLinesPerPage(args,j,first,numb);
				break;
			default:
				Sys.setErrorMsg(" Invalid option on OPTIONS card = " + hold);
				j = numb;
				break;

			} // end switch result
		} // end for j

	} // end processOptions
	// ============= processOptions ==============
	// end
	// ============= processLinesPerPage =============
	// start
	
	private static int processLinesPerPage(String[] args, int j, int first, int numb) throws IOException {
		int newj = j;
		// set up processLinesPerPage RETURN J
		String hold1;
		int k = j + first;
		if (j + 1 < numb) {

			hold1 = args[k + 1];

		// Here it better be an integer
			try {
				int howMany = Integer.parseInt(hold1);
				Sys.linesPerPage = howMany;
			} // end try
			catch (Exception e) {
				Sys.setErrorMsg(" Invalid parameter after LINES option " + hold1);
				newj = numb;
			} // end catch
				newj++;
		} // end if
		else {
			Sys.setErrorMsg(" No parameter following LINES option ");

		} // end else
		return newj;
	} // end processLinesPerPage

	// ============= processLinesPerPage =============
	// end	
	// ============= processDebugOption ==============
	// start

	private static int processDebugOption(String[] args, int j, int first, int numb) throws IOException {
		int newj = j;
		// set up processRecessDebugOption
		String hold1;
		int extra;
		int k = j + first;
		if (j + 1 < numb) {

			hold1 = args[k + 1];
			extra = getOpt(hold1);
			switch (extra) {
			case 8: // OFF
				Flags.debug = false;
				newj++;
				break;
			case 9: // ON
				Flags.debug = true;
				newj++;
				break;
			default: // Here it better be an integer
				Sys.setErrorMsg(" Invalid parameter flags" + " after DEBUG option " + hold1);
				newj = numb;
				break;
			} // end switch
		} // end if
		else {
			Sys.setErrorMsg(" No parameter following DEBUG option ");

		} // end else

		return newj;
	} // end processRecordsOption
	// ============= processDebugOption ==============
	// end
	// ========== processRecordsOption ==============
	// start

	private static int processRecordsOption(String[] args, int j, int first, int numb) throws IOException {

		int newj = j;
		// set up processRecordsOption RETURN J
		String hold1;
		int extra;
		int k = j + first;
		if (j + 1 < numb) {

			hold1 = args[k + 1];
			extra = getOpt(hold1);
			switch (extra) {
			case 1: // ALL
				Flags.printRecords = true;
				Sys.recsToPrint = 0;
				newj++;
				break;
			case 7: // NONE
				Flags.printRecords = false;
				newj++;
				break;
			default: // Here it better be an integer
				try {
					int howMany = Integer.parseInt(hold1);
					Sys.recsToPrint = howMany;
				} // end try
				catch (Exception e) {
					Sys.setErrorMsg(" Invalid parameter after RECORDS option " + hold1);
					newj = numb;
				} // end catch
				newj++;
				break;
			} // end switch
		} // end if
		else {
			Sys.setErrorMsg(" No parameter following RECORDS option ");

		} // end else
		return newj;
	} // end processRecordsOption
	// ========== processRecordsOption ==============
	// end

}	// end Class Opt
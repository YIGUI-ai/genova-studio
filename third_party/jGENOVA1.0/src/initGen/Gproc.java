package initGen;

import java.io.IOException;

public class Gproc {

	// ============== startProcess ============
	// start

	public static void startProcess(String[] tokens, int howMany) throws IOException {
		if (G.numMain < 2)
			Sys.setErrorMsg("  Less than 2 EFFECT cards before" + " a PROCESS card ");
		if (howMany > 2)
			Sys.setErrorMsg(" too many arguments on a PROCESS card");
		if (!Flags.format)
			Sys.setErrorMsg("  No runtime FORMAT before" + " PROCESS card - processing ceases");
		if (Flags.nonRandom) {
			// here at least one universe value is > 0
			// if all are fixed - abort
			boolean anyRandom = false;
			for (int k = 0; k < G.numMain; k++) {
				if (G.mainUniverse[k] != G.mainLevel[k])
					anyRandom = true;
			} // end for
			if (!anyRandom)
				Sys.setErrorMsg(" All facets are fixed - " + "processing ceases for this study");
		} // end if Flags.nonRandom

		// now check the parameters
		switch (howMany) {

		case 0: // all is well
			break;

		case 1:
			if (U.isDefault(tokens[0])) {
				IO.setDataToCard();
				// set dataFile to CardFile
			} // end if
			else {
				IO.setDataFile(tokens[0]);
			} // end else
			break;

		case 2:
			IO.setDataFile(tokens[0]);
			if (!U.isDefault(tokens[1]))
				Sys.setWarning(" Second argument" + " not DEFAULT " + "- ignored");
			break;
		} // end switch howMany

		if (G.irStar == 0)
			Sys.setErrorMsg(" No starred effect - " + "processing ceases");

		// HERE EVERYTHING IS OK READY TO START CALLING OTHER ROUTINES

	} // end startProcess
	// ============== startProcess ============
	// end

}	// end Class Gproc

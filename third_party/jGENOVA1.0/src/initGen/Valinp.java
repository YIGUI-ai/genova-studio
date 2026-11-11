package initGen;

import java.io.IOException;

public class Valinp {

	/*
	 * Valinp is used to process both VCOMPONENTS AND MEANSQUARES CONTROL CARDS
	 * 
	 * IT RETURNS AN INTEGER FOR THE ROW OF THE id TABLE FOR THE SPECIFIED
	 * EFFECT AND RETRN THE DOUBLE VALUE INPUT ON THE CARD INTO the G.f
	 * vcompOrMsquare
	 *
	 */
	// ================== vcompCard ==============
	// start
	public static void vcompCard(String[] tokens, int howMany) throws IOException {

		int idrow = vcompOrMsq("VCOMP", tokens, howMany);
		G.varMod[idrow] = G.vcompOrMsquare;
		G.xBarb[idrow] = 1.0d;

	}
	// ================== vcompCard ==============
	// end
	// ================== msquareCard ==============
	// start

	public static void msquareCard(String[] tokens, int howMany) throws IOException {

		int idrow = vcompOrMsq("MSQUARE", tokens, howMany);
		G.xMsqr[idrow] = G.vcompOrMsquare;
		G.xBarb[idrow] = 1.0d;

	}
	// ================== msquareCard ==============
	// end
	// ================== vcompOrMsq ==============
	// start

	private static int vcompOrMsq(String type, String[] tokens, int howMany) throws IOException {
		// return the row of the ID table
		// for the current control card being
		// processed
		// if successful the G.vcompOrMsquare
		// will contain the real value read in
		// as the second token on the current card
		// Flags.inMain will be true if no interaction
		// effect has been processed yet
		if (howMany < 2 | howMany > 4) {
			Sys.setErrorMsg("Number of parameters on " + type + " card not 2-4");
		} // end if

		int row = 0;
		if (!Flags.inMain) {
			// here is an interaction effect
			// do something appropriate

			row = interactionEffect(type, tokens, howMany);
			return row;
		}
		if (Flags.inMain) {
			if (howMany == 2) {
				// here switching from main to
				// interaction effect
				doSwitchFromMain(type);
				row = interactionEffect(type, tokens, howMany);
			} else {
				// should be a regular main effect here
				row = doMainEffect(type, tokens, howMany);
			}
		}

		return row;
	}
	// ================== vcompOrMsq ==============
	// end
	// ================== interactionEfect ==============
	// start

	private static int interactionEffect(String type, String[] tokens, int howMany) throws IOException {
		if (howMany > 2)
			Sys.setErrorMsg(" Excesss parameters on a " + type + " interaction effect card");
		int row = findInteraction(type, tokens[0]);
		
		G.vcompOrMsquare = myDouble(type, tokens[1]);
		return row;
	}
	// ================== interactionEfect ==============
	// end
	// ================== findInteraction ==============
	// start
	
	private static int findInteraction(String type, String token) throws IOException {
		int row = 0;
		for (int k = G.numMain + 1; k <= G.numTotal; k++) {
			if (token.matches(G.alphaID[k].trim()))
				return k;
		} // end for k
		if (row == 0)
			Sys.setErrorMsg(" bad interaction effect string " + "found on " + type + " card");
		return row;

	}
	// ================== findInteraction ==============
	// end
	// ================== doSwithFromMain ==============
	// start

	private static void doSwitchFromMain(String type) throws IOException {

		if (G.numMain < 2)
			Sys.setErrorMsg(
					" less than 2 main effect " + "found before first interaction " + "effect on " + type + "card");
		IDset.setID();
		IDset.setDegrees();

		// =============== set up alphaID
		//
		for (int k = 1; k <= G.numTotal; k++) {
			G.alphaID[k] = U.effectOut(k);
		}
		Flags.inMain = false;
	}
	// ================== doSwithFromMain ==============
	// end
	// ================== doMainEffect ==============
	// start

	private static int doMainEffect(String type, String[] tokens, int howMany) throws IOException {
		// return row f ID table if successful

		if (G.numMain == 6)
			Sys.setErrorMsg("more than 6 " + type + " main effect cards");
		G.numMain++;
		String tokenValue = tokens[0];
		E.effectInput(tokenValue);
		G.vcompOrMsquare = myDouble(type, tokens[1]);
		int level = U.toInteger(type, tokens[2]);
		if (level < 2)
			Sys.setErrorMsg(" Sample size less than 2 - Invalid");
		G.mainLevel[G.numMain] = level;
		int temp;

		if (howMany == 4) {
			temp = U.toInteger(type, tokens[3]);
			G.mainUniverse[G.numMain] = temp;
			if (temp > 0) {
				Flags.nonRandom = true;
				if (level > temp)
					Sys.setErrorMsg(" Sample > " + "Population - Invalid");
			}
		}
		return G.numMain;
	}
	// ================== doMainEffect ==============
	// end
	// ================== myDouble ==============
	// start

	private static double myDouble(String type, String token) throws NumberFormatException, IOException {

		double d = 0.0d;
		String mess = " Second parameter on a "
			 + type + " card not a numeric value";
		d = U.toDouble(mess,  token);
		return d;
	}

	// ================== myDouble ==============
	// end

} // end class Valinp

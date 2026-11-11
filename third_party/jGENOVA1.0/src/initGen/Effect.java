package initGen;

import java.io.IOException;

public class Effect {

	// ================== processEffect ==============
	// start

	public static void processEffect(String[] cardTokens, int howMany) throws IOException { // 1
		if (G.numMain == 6)
			Sys.setErrorMsg("more than 6 EFFECT cards");
		G.numMain++;
		// int numMainM1= G.numMain - 1;

		if (howMany < 2 || howMany > 4)
			Sys.setErrorMsg("Number of parameters on " + "EFFECT card not 2-4");

		int currToken = 0;
		String tokenValue = cardTokens[currToken];

		switch (tokenValue) { // 2

		case ("*"):
			if (G.irStar > 0) { // 3
				Sys.setErrorMsg(" Multiple Starred " + "Effects found!");
			} // end if -3
			else { // 3
				G.irStar = G.numMain;
			} // end if else -3
			currToken++;
			break;

		case ("+"):
			Flags.printMean[G.numMain] = true; // $$$$
			Flags.anyMean = true;
			currToken++;
			break;

		default:
			// now there should be a new main effect
			break;
		} // end switch -2

		// after the switch - if not error look for main effect

		// no errors do far - look for a main effect
		tokenValue = cardTokens[currToken];

		E.effectInput(tokenValue);
		currToken++;
		tokenValue = cardTokens[currToken];

		G.mainLevel[G.numMain] = U.toInteger("EFFECT",tokenValue);
		currToken++;

		// no errors do far - look for population for main effect
		// if he parameter is present

		if (currToken < howMany) { // 2
			// look for population)
			tokenValue = cardTokens[currToken];
			G.mainUniverse[G.numMain] = U.toInteger("EFFECT",tokenValue);
			Flags.nonRandom = true;
			currToken++;
			if (currToken < howMany) {
				System.out.println(currToken + " " + howMany);
				Sys.setWarning(" Excess parameters on an EFFECT card - ignored");
			}
		} // end if currtoken -2

		int sample = G.mainLevel[G.numMain];
		if (sample < 2)
			Sys.setErrorMsg(" Excess parameters on an EFFECT card - ignored");

		int universe = G.mainUniverse[G.numMain];
		if ((universe > 0) && (sample > universe))
			Sys.setErrorMsg(" sample size greater " + "the universe GSTUDY terminated");

	} // end processEffect
	// ================== processEffect ==============
	// end

} // end class Effect
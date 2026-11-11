package initGen;

import java.io.IOException;

public class Deffect {
	// ============== Deffect..process ============
	// start
	public static void process(String[] tokens, int howMany) throws IOException {
		if (howMany == 0)
			Sys.setErrorMsg(" No Min effect on DEFFECT card");
		G.numMain++;
		int current = 0;
		if (ifDollar(tokens[0])) {
			if (D.starID > 0)
				Sys.setErrorMsg(" Duplicate object of " +
			"measurement ($) specified for this d Study");
			else {
				D.starID = G.numMain;
				current++;
			}
		} // end of ifDollar
		String str = tokens[current];
		// process main effect string
		E.effectInput(str);
		// checkMain checks that the correct
		// default sample and universe go with
		// the current main effect and insure the
		// the main effect characters was in the
		// preceding G level analysis

		checkMain(G.numMain);
		current++;
		int temp = 0;
		int sampHold = 0;
		
		
		loopSample: for (int tk = current; tk < howMany; tk++) {
			// look for sample sizes and/or universe size
			// temp will contain the number of samples
			// for this DEFFECT card
			str = tokens[tk];
			if (U.ifSlash(str)) {
				findUniverse(tokens, tk, howMany);
				break loopSample;
			}
			temp++;
			if (temp > 30)
				Sys.setErrorMsg(" Too many Sample sizes " + "specified for this d Study main effect");

			sampHold = U.toInteger("Deffect", str);
			if (sampHold < 1)
				Sys.setErrorMsg(" Sample size on "
						+ "DEFFECT card must be > Zero!!");

			D.sampID[G.numMain][temp] = sampHold;
		}
		D.dSample[G.numMain] = Math.max(1, temp);
		D.maxDstudy = Math.max(D.maxDstudy, temp);

	} // end process
	// ============== Seffect..process ============
	// end
	// ============== checkMain ============
	// start

	private static void checkMain(int index) throws IOException {

		int gIndex = 0;
		char current = G.mefChar[index];
		loop20: for (int k = 1; k <= D.numMainG; k++) {
			if (D.mefCharG[k] == current) {
				gIndex = k;
				break loop20;

			}
		} // end for k loop20
		if (gIndex == 0)
			Sys.setErrorMsg(" Main effect character not " 
		+ "found in previous G Study desgin - abort");
		if (gIndex == index)
			return;
		D.dUniverse[index] = G.mainUniverse[gIndex];
		D.sampID[G.numMain][1] = G.mainLevel[gIndex];
		return;
	}

	// ============== checkMain ============
	// end
	// ============== findUniverse ============
	// start
	private static void findUniverse(String[] tokens, int tok, int howMany) throws IOException {
		// here '/' found in Tokens [tok]
		if (tok + 1 <= howMany)
			D.dUniverse[G.numMain] = U.toInteger("DEFFECT",tokens[tok + 1]);

		if (tok + 2 != howMany) {
			Sys.setWarning(" Excess parameters on Effect card " + "- ignored");
			return;
		}
		if (tok + 1 > howMany) {
			G.mainUniverse[G.numMain] = 0;
			Sys.setWarning(" Universe specified for the DEFFECT" 
					+ " card after a '/' zeero ==> infinite assumed");
			return;
		}	
		if (G.mainUniverse[G.numMain] < 0) {
			Sys.setWarning(" No universe specified for the DEFFECT" 
					+ " card after a '/' negative = Invalid");
			return;
		}
	} // end findUniverse
	// ============== findUniverse ============
	// end
	// =========== ifDollar =======
	// start

	private static boolean ifDollar(String str) {

		if (str.length() != 1)
			return false;
		if (str.startsWith("$"))
			return true;
		else
			return false;

	} // end ifDollar
	// =========== ifDollar =======
	// end

}	// end lass Deffect

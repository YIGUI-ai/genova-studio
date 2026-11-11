package initGen;

import java.io.IOException;

public class Dproc {

	// ============== end ============
	// start

	public static void end() throws IOException {

		if (Flags.defaultDstudy) {
			IDset.setID();
		} else {
			dsCheck();
			IDset.setID();
			if (!Dtran.transform())
				return;
		} // end else

		// 70 in ENDDSTUDY
		// NOW START REAL PROCESSING

		if (!Flags.dstudyPossible && D.dstudyCut) {
			Sys.setWarning(" No Grand Mean - DCUT" + " score input is ignored");
			D.dstudyCut = false;
			D.numDcut = 0;
		} // end if
		if (G.indexTotal[D.starID] != 1) {
			Sys.setWarning(" Nested object of Measurement" + " not currently supported in Genova -"
					+ " D Study processing ceases");
			return;
		}
		// EQUALIZE D.sampID BY USING dSample
		if (D.maxDstudy > 1) {
			int jd, isamp;
			loop120: for (int i = 1; i <= G.numMain; i++) {
				jd = D.dSample[i];
				if (jd == D.maxDstudy)
					continue loop120;
				isamp = D.sampID[i][jd];
				for (int j = jd + 1; j <= D.maxDstudy; j++) {
					D.sampID[i][j] = isamp;
				} // end for j
			} // end for i loop120
		} // end if naxDstudy > 1
		D.maxDstudy = Math.max(1, D.maxDstudy);
		loopStudy: for (int k = 1; k <= D.maxDstudy; k++) {
			D.numDstudy = k;
			if (!Dcalc.dProc1())
				continue loopStudy;
			PrD.dPrint1();
			Dcalc.dProc2();
			Dcalc.dPrint2();
			if (Flags.dstudyTable)
				PrG.printDiagonal(3);
		} // end for k loopStudy
		if (D.maxDstudy > 1)
			PrD.dPrintSummary();
		PR.clearTempHead();
		PR.setNumLines(100);
		PR.setPageType(1);

	} // end end
	// ============== end ============
	// end
	// ============== go ============
	// start

	public static void go(String heading) throws IOException {

		PR.setMyTitle(heading);
		PR.setPageType(3);
		PR.clearTempHead();
		D.starID = 0;
		D.numDcut = 0;
		D.maxDstudy = 0;
		D.dstudyCut = false;
		D.dhtemp[1] = " ";

		dsPrep();

		G.numMain = 0;
		G.numTotal = 0;
	} // ebd go
	// ============== go ============
	// end
	// ============== dsCheck ============
	// start

	private static void dsCheck() throws IOException { // 1

		if (Flags.debug)
			Bug.bug40dproc();
		if (G.numMain != D.numMainG)
			Sys.setErrorMsg(" Number of Main effects" + " in D study different from G Study");

		// START CHECKING THE COLUMNS
		int jd = 0;
		for (int j = 1; j <= G.numMain; j++) { // 2

			loop20: for (int j1 = j; j1 <= G.numMain; j1++) { // 3
				if (D.mefCharG[j] == G.mefChar[j1]) { // 4
					jd = j1;
					break loop20;
				} // end if -4
			} // end for j1 -3
			if (jd != j)
				doSwitch(j, jd);
		} // end for j -2
		if (Flags.debug)
			Bug.bug40dproc();

	} // end dsCheck -1
	// ============== dsCheck ============
	// end
	// ============== doSwitch ============
	// start

	private static void doSwitch(int i, int j) {


		if (D.starID == i)
			D.starID = j;
		else if (D.starID == j)
			D.starID = i;
		int ix;
		ix = G.ID[i][i];
		G.ID[i][i] = G.ID[j][j];
		G.ID[j][j] = ix;

		ix = G.ID[i][j];
		G.ID[i][j] = G.ID[j][i];
		G.ID[j][i] = ix;

		loop10: for (int k = 1; k <= G.numMain; k++) {
			if (k == i || k == j)
				continue loop10;
			ix = G.ID[i][k];
			G.ID[i][k] = G.ID[j][k];
			G.ID[j][k] = ix;

			ix = G.ID[k][i];
			G.ID[k][i] = G.ID[k][j];
			G.ID[k][j] = ix;
		}
		ix = D.dSample[i];
		D.dSample[i] = D.dSample[j];
		D.dSample[j] = ix;

		ix = D.dUniverse[i];
		D.dUniverse[i] = D.dUniverse[j];
		D.dUniverse[j] = ix;

		int imax = Math.max(D.dSample[i], D.dSample[j]);
		for (int k = 1; k <= imax; k++) {
			ix = D.sampID[i][k];
			D.sampID[i][k] = D.sampID[j][k];
			D.sampID[j][k] = ix;
		}
		ix = G.indexMain[i];
		G.indexMain[i] = G.indexMain[j];
		G.indexMain[j] = ix;

		ix = G.indexTotal[i];
		G.indexTotal[i] = G.indexTotal[j];
		G.indexTotal[j] = ix;

		char cx = G.mefChar[i];
		G.mefChar[i] = G.mefChar[j];
		G.mefChar[j] = cx;

	};

	// ============== doSwitch ============
	// end
	// ============== getGrand ============
	// start
	public static void getGrand(String token, int howMany) throws IOException {

		if (howMany == 0)
			return;
		if (howMany > 1)
			Sys.setWarning(" Excess parameters on" + " an ENDDSTUDY control card - ignored");

		G.grandMean = U.toDouble(" Invalid value for" + " grandmean on an ENDDSTUDY control" + " card", token);
		Flags.grandMean = true;
		Flags.dstudyPossible = true;
		return;
	} // end getGrand
	// ============== getGrand ============
	// end
	// ============== dsPrep ============
	// start

	private static void dsPrep() throws IOException {

		if (Sys.lastRun == 0)
			Sys.setErrorMsg(" DSTUDY found before a " + " GStudy or equivalent");
		if (Sys.lastRun == 1) {
			// this is the first DSTUDY run after a G
			// level run
			D.seqDstudy = 0;
			if (Flags.grandMean)
				D.gGraandMean = G.grandMean;
			D.numMainG = G.numMain;

			D.numTotalG = G.numTotal;
			D.offsetGT = Sys.offsetPT;
			for (int i = 1; i <= G.numMain; i++) {
				D.mefCharG[i] = G.mefChar[i];
				for (int j = 1; j <= G.numTotal; j++) {
					D.IDG[j][i] = G.ID[j][i];
				} // end for j
			} // end for i
			Sys.lastRun = 2;

			// MOVE VAR.-COVARIANCE MATRIX TO BEGINNING OF XBUF
			if (Sys.offsetSE != 0) {
				int jp = Sys.offsetSE;
				for (int i = 1; i <= Sys.offsetPT; i++) {
					jp++;
					G.xbuf[i] = G.xbuf[jp];
				}
			} // end if
			Sys.offsetSE = 0;

			// INSURE THAT VARCMP AND VARFIN NEGATUVE
			// ELEMENTS ARE ZEROED

			for (int j = 1; j <= G.numTotal; j++) {
				if (G.varFin[j] < 0.0d)
					G.varFin[j] = 0.0d;
				if (G.varMod[j] < 0.0d)
					G.varMod[j] = 0.0d;
				D.gorderedTEF[j] = G.orderedTEF[j];
			}

		} // end if lastRun = 1
		// here is a subsequent Dstudy run - reset defaults
		D.seqDstudy++;
		if (Flags.grandMean)
			G.grandMean = D.gGraandMean;
		for (int j = 1; j <= D.numMainG; j++) {
			for (int i = 1; i <= D.numTotalG; i++) {
				G.ID[i][j] = 0;
			}
			D.dSample[j] = 1;
			D.sampID[j][1] = G.mainLevel[j];
			D.dUniverse[j] = G.mainUniverse[j];
		}

	} // end dsPrep
	// ============== dsPrep ============
	// end
	// ============== dCut ============
	// start

	public static void dCut(String[] tokens, int howMany) throws IOException { // 1

		if (D.dstudyCut)
			Sys.setErrorMsg(" Duplicate DCUT control" + " cards = fatal");
		else {
			D.dstudyCut = true;
			D.numDcut = 0;
		}
		if (howMany == 0) { // 2
			Sys.setWarning(" Empty DCUT card - STANDARD " + "cutting scores assumed");
			standardCut();
			return;
		} // -2

		String str;
		String message = "";
		if (ifStandard(tokens[0])) { // 2
			standardCut();
			if (howMany == 1)
				return;
			if (howMany != 3)
				Sys.setErrorMsg("invalid number of " + "parameters following " + " STANDARD on DCUT card");
			if (U.ifSlash(tokens[1])) { // 3
				// third parameter must be a grandmean
				message = " Grandmean value of DCUT card " + "not a valid number";
				G.grandMean = U.toDouble(message, tokens[2]);
				return;
			} // -3
		} // -2
		// here first parameter is not STANDARD
		// not process a regular DCUT card
		// with or withouta grandmean
		for (int k = 0; k < howMany; k++) { // 2
			str = tokens[0];
			if (U.ifSlash(str)) { // 3
				if (k + 3 == howMany) { // 4
					G.grandMean = U.toDouble(message, tokens[k + 1]);
					Flags.grandMean = true;
					return;
				} // -4

			} // end if *ifSlash -3
			else { // 3
				D.numDcut++;
				if (D.numDcut > 30)
					Sys.setErrorMsg(" Too many values (>30) " + "on a DCUT card - Fatal");
				D.xDcut[D.numDcut] = U.toDouble(" invalid value for a " + "cut score on DCUT card", tokens[k]);
			} // end else-3

		} // end for k -2
		return;

	} // end of dCut
	// ============== dCut ============
	// end
	// ============== standardCut ============
	// start

	private static void standardCut() {

		double xinc = Math.pow(10, -1);
		double xval = 0.0d;

		D.numDcut = 11;
		for (int m = 1; m <= D.numDcut; m++) {
			D.xDcut[m] = xval;
			xval = xval + xinc;
		}
	}

	// ============== standardCut ============
	// end
	// ============== ifStandard ============
	// start
	private static boolean ifStandard(String str) {

		str = str + "   ";
		str = str.substring(0, 4);
		if (str.startsWith("STAN"))
			return true;
		else
			return false;

	} // end ifStandard

	// ============== ifStandard ============
	// end


}	// end Class Dproc

package initGen;

import java.io.IOException;

public class Pass1 {

	// ============= only used in gSetup and gPrep and lExclude
	private static int jIDcol;
	private static int passIDCols;
	private static boolean lPass2;
	private static boolean lPos;
	private static boolean lSum;
	private static int jTemp;
	//
	// LPOS = TRUE => THE LAST COLUMN OF ID PROCESSED WAS > ZERO
	// = FALSE => COLUMN WAS = ZERO
	//
	// LSUM = TRUE => AN ARRAY AREA FOR SUMS WILL BE NEEDED ON
	// PASS ONE
	//

	// ========================== gSetup =================
	// start

	public static boolean gSetup() throws IOException { // 1

		boolean lProblem = false;

		gPrep();
		G.nSum1 = 0;
		G.nPass1 = 0;
		G.nxtOff = G.inRecord;
		lPass2 = true;
		// lPass2 will be true if 2 pass processing is possible

		for (int ir = 1; ir <= G.numTotal; ir++) { // old 100 2
			if (!lExclude(ir)) { // to old 100 3

				// here row 'ir' of the ID table will be
				// processed on the current pass
				G.nPass1++;
				G.iPass1[G.nPass1] = ir;
				// this effective move nPass1 to 0 based as an index

				lPos = true;
				lSum = false;
				jTemp = 0;

				if (jIDcol > G.numMain) { // 4
					G.passID[1][1] = 0; // to old 55
				} // -4
				else { // 4
					do50Loop(ir);

				} // end if else // -4
				// ==================================
				if (G.passNumber == 1)
					lSum = true;
				G.lArray[G.nPass1] = lSum;
				if (lSum)
					G.nSum1++;

				int nTemp = 1;
				int iValue;
				if (passIDCols != 0) { // 4
					for (int j = 1; j <= passIDCols; j++) { // 5
						iValue = G.passID[G.nPass1][j]; // $$$$$$$
						nTemp = nTemp * Math.max(1, iValue);
					} // end for 5
				} // end if // 4

				G.np1Cell[G.nPass1] = nTemp;
				// ============================`
				if (lSum) { // 4
					G.nOffset[G.nPass1] = G.nxtOff;
					G.nxtOff = G.nxtOff + G.inRecord / nTemp;

				} // end if lsum // -4
				else { // 4
					G.nxtOff++;
					G.nOffset[G.nPass1] = G.nxtOff;

				} // end else // -4,
			} // end if lXclude // -3

		} // end for ir // -2 end do 100

		if (G.passNumber == 0) { // 2
			setupNZ();

		} // -2
		if (G.nxtOff > G.bufLimit)
			Sys.setErrorMsg(" "
					+ "* Buffer Limit " + G.bufLimit + "Exceeded Space Needed = " + G.nxtOff);

		// Initialize xbuf
		for (int k = G.inRecord + 1; k <= G.nxtOff; k++) {
			G.xbuf[k] = 0.0;
		} // end for
		if (!lPass2)
			lProblem = true;

		for (int k = 1; k <= G.numMain; k++) {
			G.nIndex[k] = 1;
		} // end for

		if (Flags.debug)
			Bug.bu19pass1gsetupDebug(lPass2, passIDCols);
		if (lProblem) 
		{	IO.mybw.newLine();
			IO.mybw.newLine();
			IO.mybw.write(" Invalid jGENOVA Input Structure - see Input Restrictions in Chapter 1 of the Manual");
			IO.mybw.flush();
			System.exit(0);
		}
		return lProblem;
	} // end gSetup
	// ========================== gSetup =================
	// end
	// ========================== setupNZ =================
	// start

	private static void setupNZ() {
		// set up NZDIM, NZOFF, and NZDEX for first pass
		int nSize = G.dataPoints / G.mainLevel[G.irStar];
		G.nzOffset = G.nxtOff;
		G.nxtOff = G.nxtOff + nSize;
		if (G.irStar != 1) {
			for (int j = 1; j <= G.irStar - 1; j++) {
				G.nzIndex[j] = 1;
				G.nzDim[j] = G.mainLevel[j];
			} // end for
		} // end if irStar
		// here irStqr = 1 ==> != numMain
		if ((G.irStar == 1) | (G.irStar != G.numMain)) {
			for (int j = G.irStar + 1; j <= G.numMain; j++) { // $$$$
				G.nzIndex[j - 1] = 1;
				G.nzDim[j - 1] = G.mainLevel[j];
			} // end for
		} // end if irStar

	} // end setupNZ

	// ========================== setupNZ =================
	// end
	// ========================== do50Loop =================
	// start

	private static void do50Loop(int ir) {

		for (int j = jIDcol; j <= G.numMain; j++) { // old 50 // 1

			if ((G.passNumber != 1) || (j != G.irStar)) { // 2

				jTemp++;
				if (G.ID[ir][j] <= 0) { // 3
					G.passID[G.nPass1][jTemp] = G.mainLevel[j];
					lPos = false;

				} // -3
				else { // old 40// 3
					G.passID[G.nPass1][jTemp] = 0;
					if (!lPos) { // 4
						lPos = true;
						lSum = true;
					} // end if not lPos // -4

				} // end else // -3
			} // end if not G.passNumber //-2
		} // end for j // -1

	} // end do50Loop
	// ========================== do50Loop =================
	// end
	// ========================== lExclude =================
	// start

	private static boolean lExclude(int row) throws IOException {
		boolean exclude = true;

		switch (G.passNumber) {

		case 0:
			if (G.ID[row][G.irStar] == 0)
				break;
			exclude = false;
			if (G.irStar == 1)
				break;
			for (int j = 1; j <= G.irStar - 1; j++) {
				if (G.ID[row][j] <= 0) {
					lPass2 = false;
					Sys.setWarning(" Row " + row + " of the ID" + " table cannot be used for " + "Two-Pass Processing");
				} // end if
			} // end for
			break;
		case 1:
			if (G.ID[row][G.irStar] == 0)
				exclude = false;
			break;

		}// end switch

		return exclude;
	}
	// ========================== lExclude =================
	// end
	// ========================== gPrep =================
	// start

	private static void gPrep() throws IOException {

		switch (G.passNumber) {

		case 0:
			G.numRecs = 1;
			for (int k = 1; k <= G.irStar; k++) {
				G.numRecs = G.numRecs * G.mainLevel[k];
			} // end for

			G.inRecord = G.dataPoints / G.numRecs;

			G.xMean = 0.0;

			passIDCols = G.numMain - G.irStar;
			jIDcol = G.irStar + 1;
			break;

		case 1:
			G.numRecs = 1;
			G.inRecord = G.dataPoints / G.mainLevel[G.irStar];
			passIDCols = G.numMain - 1;
			jIDcol = 1;
			int j1 = -0;
			for (int j = 1; j <= G.numMain; j++) {
				if (G.irStar != j) {
					j1++;
					G.nzDim[j1] = G.mainLevel[j];
				} // end if
			} // end for
			break;

		} // end switch

		if (Flags.debug)
			Bug.bug18pass1gprep(jIDcol, passIDCols);
	}

	// ========================== gPrep =================
	// end

}	// end Class Pass1

package initGen;

import java.io.IOException;

public class IDset {

	// ============= setDegrees ==============
	// start

	public static void setDegrees() {

		int[] numDegrees = new int[64];
		int points = 1;
		int totDegrees = 0;
		int idegree, ilevel;
		for (int k = 1; k <= G.numMain; k++) {
			points = points * G.mainLevel[k];
		} // end for k

		for (int row = 1; row <= G.numTotal; row++) { // 1
			idegree = 1;
			for (int m = 1; m <= G.numMain; m++) { // 2
				ilevel = G.mainLevel[m];
				if (G.ID[row][m] > 1)
					idegree = idegree * ilevel;
				if (G.ID[row][m] == 1)
					idegree = idegree * (ilevel - 1);
			} // end for m // -2
			numDegrees[row] = idegree;
			totDegrees = totDegrees + idegree;
		} // end for row // -1

		G.numDegFreedom = numDegrees;
		G.totalDegFreedom = totDegrees;
		G.dataPoints = points;

	}
	// ============= setDegrees ==============
	// end
	// ============= setID ==============
	// start

	public static void setID() throws IOException {
		int[] nStart = { 0, 2, 3, 4, 5, 6, 7 };
		int[] result = new int[9];

		G.firstRow[1] = 1;
		G.firstRow[2] = G.numMain + 1;
		G.numNested = 1;
		G.numTotal = G.numMain;

		int nWay = G.numMain - 1;

		int nWayP1, nLast, nStartJM;

		while (nWay > 0) { // 1
			// now start big loop

			nWayP1 = nWay + 1;
			nLast = G.numTotal;
			for (int jm = 1; jm <= nWayP1; jm++) { // dp 100// 2
				nStartJM = nStart[jm];

				if (nStartJM <= nLast) { // 3
					for (int jp = nStartJM; jp <= nLast; jp++) { // 4
						result = logMatch(jm, jp);

						if (result[7] > 0) { // 5 HERE ADD NEW ROW // 5
							G.numTotal++;

							for (int k = 1; k <= G.numMain; k++) { // 6
								G.ID[G.numTotal][k] = result[k];
							} // end for k // -6
						} // end of if result[7] // -5
					} // 4 end for jp-4

					nStart[jm] = G.numTotal + 1;

				} // 3 end if nStartJM // -3

			} // end for JM 

			// ===================
			if (nLast < G.numTotal)
				G.numNested++;

			G.firstRow[G.numNested + 1] = G.numTotal + 1;

			nWay--;

		} // end while nWay > 0// -1

		int numt, numm, hold;
		// calculate G.indexMain and G.indexTotal
		for (int t = 1; t <= G.numTotal; t++) {
			numt = 0;
			numm = 0;
			for (int m = 1; m <= G.numMain; m++) {
				hold = G.ID[t][m];
				switch (hold) {
				case 1:
					numm++;
					numt++;
					break;
				case 0:
					break;
				default:
					numt++;
					break;
				} // end switch hold
			} // end for m
			G.indexMain[t] = numm;
			G.indexTotal[t] = numt;
		} // end for t

		// end of setting up G.indexMain and G.indexTotal

		// set up G.orderedMEF and ordered TEF
		int jpos = 0;
		G.orderedMEF[1] = 1;
		for (int m = 1; m <= G.numMain; m++) { // 1
			for (int t = 1; t <= G.numTotal; t++) { // 2
				if (G.indexTotal[t] == m) { // 3
					jpos++;
					G.orderedTEF[jpos] = t;
				} // end if // 3
			} // end for // 2
			G.orderedMEF[m + 1] = jpos + 1;

		} // nd for m // 1


		Sys.offsetPT = Sys.matBase[G.numTotal] + G.numTotal;
		Sys.offsetSE = Sys.offsetPT + Sys.offsetPT;
		int bufNeeded = Sys.offsetPT + Sys.offsetSE;
		if (bufNeeded > G.bufLimit) {
			Sys.setWarning(" xbuf limit of " + G.bufLimit + " is too small");
			Sys.setErrorMsg(" G.bufLimit must be reset in java" + " source to at least " + bufNeeded);
		} // end if

	} // end of SetID
	// ============= setID ==============
	// end
	// ============= logMatch ==============
	// start

	private static int[] logMatch(int iRow, int jRow) {
		/**
		 * logMatch implements the LAMATCH function and returns a 9 element
		 * vector result where result[1-6] will be a new XidArray interaction
		 * effect row if the method is successful
		 * 
		 * result[8] will = number of main effects in the row and result[7] will
		 * = the total number of effect in the row
		 * 
		 * if unsuccessful result[7] will be zero
		 */

		int[] result = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int jnumMain = 0;
		int jnumTotal = 0;
		int indexI, indexJ, maxIJ;

		boolean isDup;

		for (int j = 1; j <= G.numMain; j++) { // 1
			indexI = G.ID[iRow][j];
			indexJ = G.ID[jRow][j];

			if (indexI * indexJ != 0) { // 2
				if (Math.min(indexI, indexJ) == 1)
					return result;
			} // end if ... != 0 // -2

			maxIJ = Math.max(indexI, indexJ);
			result[j] = maxIJ;
			if (maxIJ != 0) { // 2
				jnumTotal++;
				if (maxIJ == 1)
					jnumMain++;
			} // end of if maxIJ != 0 // -2
		} // end of for j // -1

		result[8] = jnumMain;
		result[7] = jnumTotal;

		isDup = checkForDup(result);

		if (isDup) {
			result[7] = 0;
		}
		return result;

	} // end of logMatch
	// ============= logMatch ==============
	// end
	// ============= checkForDup ==============
	// start

	private static boolean checkForDup(int[] thisRow) {

		int jnumTotal = thisRow[7];
		int jnumMain = thisRow[8];
		boolean isDuplicate = false;

		for (int t = 1; t <= G.numTotal; t++) {

			if (G.indexTotal[t] != jnumTotal)
				break;
			if (G.indexMain[t] != jnumMain)
				break;
			for (int m = 1; m <= G.numMain; m++) {
				if (G.ID[t][m] != thisRow[m]) {
					isDuplicate = false;
					break;
				} // end if ID(t,m)
			} // end for m
			isDuplicate = true;
		} // end for t

		return isDuplicate;

	} // end of CheckForDup
	// ============= checkForDup ==============
	// end

	// ------------------------------------
} // end class IDset

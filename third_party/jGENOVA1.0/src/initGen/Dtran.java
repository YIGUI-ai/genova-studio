package initGen;

import java.io.IOException;

public class Dtran {

	// ============== transform ============
	// start
	public static boolean transform() throws IOException {

		boolean valid = true;

		int[] igr = new int[64];
		int[][] itran = new int[48][64];
		int j1, k1, idgm, idm, nc;
		int ic = 0;
		boolean equal = true;

		D.offsetDV = -0;
		if (G.numTotal == D.numTotalG) { // 1
			loop20: for (int j = 1; j <= G.numTotal; j++) { // 2
				for (int k = 1; k <= G.numMain; k++) { // 3
					if (G.ID[j][k] != D.IDG[j][k]) { // 4
						equal = false;
						break loop20;
					} // end if // -4
				} // end for k // -3
			} // end for j loop20 -22

			if (equal) { // 2
				dsEqual();
				return true;
			} // end if // -2

		} // end if numTotal are == // -1

		// DIFFERENT STRUCTURES - INITIALIZE ITRAN AND IGR
		if (G.numTotal > D.numTotalG) {
			noTransPossible();
			return false;
		} // end if
		D.offsetDV = D.offsetGT;
		for (int j = 1; j <= D.numTotalG; j++) {
			for (int k = 1; k <= G.numTotal; k++) {
				itran[k][j] = 0;
			} // end for k
			igr[j] = 0;
		} // end for j

		// CALCULATE ITRAN
		nc = 0;
		for (int j = 1; j <= G.numTotal; j++) {
			ic = 0;
			loop90: for (int k = 1; k <= D.numTotalG; k++) {

				// COMPARE ID(J,*) WITH IDG(K,*)
				loop80: for (int m = 1; m <= G.numMain; m++) {
					idm = G.ID[j][m];
					idgm = D.IDG[k][m];
					if (idm > 1)
						continue loop80;
					switch (idm) {
					case 0:
						if (idgm != 0)
							continue loop90;
						else
							continue loop80;

					case 1:
						if (idgm == 0)
							continue loop90;
						else
							continue loop80;

					} // end switch
				} // end for m loop80

				// HERE IDG(K,*) IS IN ROW J OF ITRAN

				j1 = G.orderedTEF[j];
				k1 = G.orderedTEF[k];
				if (Flags.debug)
					Bug.bug45dstran(j, k, j1, k1);
				itran[j1][k1] = 1;
				ic++;
				igr[k]++;
			} // end for k loop90
			if (ic == 0) {
				noTransPossible();
				return false;
			} // end if
			nc = nc + ic;
		} // end for j
		if (nc != D.numTotalG) {
			noTransPossible();
			return false;
		} // end if
		for (int k = 1; k <= D.numTotalG; k++) {
			if (igr[k] != 1) {
				noTransPossible();
				return false;
			}
		} // end for k

		// HERE THE TRANSFORMATION WAS SUCCESSFUL

		D.offsetDN = D.offsetDV + Sys.offsetPT;

		// START COMPUTING THE NEW VAR. COMPONENTS AND
		// THE VAR-COVAR. MATRIX

		if (Flags.EMS)
			dsMatrix1(G.varFin, itran);
		else
			dsMatrix1(G.varMod, itran);
		dsMatrix2(itran);
		if (Flags.debug)
			Bug.bug46dstran(itran);
		if (Flags.debug)
			Bug.bug47dstran(ic, nc, igr);

		return valid;

	}
	// ============== transform ============
	// end
	// ============== noTransPossible ============
	// start

	private static void noTransPossible() throws IOException {
		Sys.setWarning(" D Study structure cannot be" + " generated from G Study structure - D " + "Study skipped");

	} // END noTransPossible
	// ============== noTransPossible ============
	// end
	// ================ dsEqual ============
	// start

	private static void dsEqual() {
		D.offsetDV = 0;
		D.offsetDN = D.offsetGT;
		for (int j = 1; j <= G.numTotal; j++) {
			if (Flags.EMS)
				D.varDGT[j] = G.varFin[j];
			else
				D.varDGT[j] = G.varMod[j];
		} // end for j

	} // end dsEqual
	// ============== dsEqual ============
	// end
	// ============== dsMatrix1 ============
	// start

	private static void dsMatrix1(double[] varxxx, int[][] itran) throws IOException {

		if (Flags.debug)
			Bug.bug50dsmatrix1(varxxx);

		double x;
		for (int j = 1; j <= G.numTotal; j++) {
			x = 0.0d;
			for (int k = 1; k <= D.numTotalG; k++) {
				if (itran[j][k] != 0)
					x = x + varxxx[k];
			} // end for k
			D.varDGT[j] = x;
		} // end for j

		if (Flags.debug)
			Bug.bug51dsmatrix1(D.varDGT);

	} // end dsMatrix1
	// ============== dsMatrix1 ============
	// end
	// ============== dsMatrix2 ============
	// start

	private static void dsMatrix2(int[][] itran) throws IOException {
		double x;
		int i1, j1, k1, m1, tik;
		for (int ix = 1; ix <= G.numTotal; ix++) {
			i1 = G.orderedTEF[ix];
			for (int j = ix; j <= G.numTotal; j++) {
				j1 = G.orderedTEF[j];
				x = 0.0d;
				loop230: for (int k = 1; k <= D.numTotalG; k++) {
					k1 = G.orderedTEF[k];
					tik = itran[i1][k1];
					if (tik == 0)
						continue loop230;
					for (int m = 1; m <= D.numTotalG; m++) {
						m1 = G.orderedTEF[m];
						x = x + tik * itran[j1][m1] * U.valMat(0, k, m);
					} // end for m
				} // end for k loop230
				U.matPut(D.offsetDV, ix, j, x);
			} // end for j
		} // end for ix

		if (Flags.debug)
			Bug.bug52dsmatrix2();
		
	} // end dsMatrix2

	// ============== dsMatrix2 ============
	// end

}	// end Class Dtran

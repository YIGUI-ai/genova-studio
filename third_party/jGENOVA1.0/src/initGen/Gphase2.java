
package initGen;

import java.io.IOException;

public class Gphase2 {

	// ========================== Gphase2.go ==============
	// start

	public static void go() throws IOException {

		double xbval;
		int j1, j2, jsign;
		int[] bufTemp = new int[20];

		if (!Flags.gMSquare) { // 1
			// to 55 if meansquare run

			for (int ir = 1; ir <= G.numTotal; ir++) { // 2
				xbval = G.xBarb[ir];
				for (int ic = 1; ic <= G.numMain; ic++) { // 3
					if (G.ID[ir][ic] == 0)
						xbval = xbval * G.mainLevel[ic];
				} // end for ic -3
				G.xBarb[ir] = xbval;
			} // end for ir -2

			if (Flags.debug)
				Bug.bug08gphase2after20();

			/* CALCULATE SUMSQ & XMSQR VALUES */

			double ssumv;
			int ntot, nest, main;

			boolean first = true;

			G.totalMsq = 0.0;
			loop50: for (int ir = 1; ir <= G.numTotal; ir++) { // 2
				ssumv = G.xBarb[ir];
				jsign = 1;
				main = G.indexMain[ir];
				ntot = G.indexTotal[ir];
				nest = ntot - main;

				/* START A STEP PASS FOR SUMSQ CALCULATION */

				while (first || (ntot > nest)) { // 3

					first = false;
					jsign = -jsign;
					ntot--;
					if (ntot <= 0) { // 4
						ssumv = ssumv + jsign * G.xMean;
						do45to50(ir, ssumv);
						continue loop50;
					} // -4

					// here ntot > 0
					j1 = G.orderedMEF[ntot];
					j2 = G.orderedMEF[ntot + 1] - 1;
					for (int k = j1; k <= j2; k++) { // 4
						ssumv = ssumv + jsign * ssVal(ir, G.orderedTEF[k]);
						if (Flags.debug)
							Bug.bug09gphase2in40(ir, k, ntot, nest, main, ssumv);

					} // end for k -4
				} // end while // -3
				do45to50(ir, ssumv);

			} // end for ir end loop50// -2

			if (Flags.debug)
				Bug.bug10gphase2after50();

		} // end if not Flags.gMSquare -1
		/* ================ old 55 confirmed ==================== */
		// CALCULATE THE VARIANCE COMPONENTS

		int num, jnumTotal, numt, result;
		double vcomp;
		int irm = 0;
		boolean loop70OK;
		int im = 0;

		loop200: for (int ir = 1; ir <= G.numTotal; ir++) { // 1
			jsign = -1;
			num = 0;
			jnumTotal = G.indexTotal[ir] + 1;
			vcomp = G.xMsqr[ir];
			if (jnumTotal > G.numMain) { // 2
				do150to200(ir, vcomp);
				continue loop200;
			} // -2
			// here jnumTotal <= numTotal
			j1 = G.orderedMEF[jnumTotal];
			j2 = G.orderedMEF[jnumTotal + 1] - 1;
			loop60: for (int ip = j1; ip <= j2; ip++) { // 2
				irm = G.orderedTEF[ip];
				if (!U.lSuper(ir, irm))
					continue loop60;

				/* ROW IRM IS TO BE INCLUDED - STEP 1 */
				num++;
				vcomp = vcomp - G.xMsqr[irm];
				bufTemp[num] = irm;
			} // end for ip end loop // -2
			if (Flags.debug)
				Bug.bug11gphase2after60(ir, num, irm, jsign, vcomp);
			/* ====================== loop 700 =================== */
			loop70OK = true;
			while (loop70OK) { // 2
				if (num == 1) { // 3
					do150to200(ir, vcomp);
					continue loop200;
				} // -3
				jsign = -jsign;
				if (num <= 2) { // 3
					im = nvAdd(bufTemp[1], bufTemp[2]);
					vcomp = vcomp + jsign * G.xMsqr[im];
					if (Flags.debug)
						Bug.bug05gphase2after70(ir, im, jsign, vcomp);
					do150to200(ir, vcomp);
					continue loop200;

				} // end if num <= 2 label 80 -3
				numt = 0;
				result = 0;
				for (int k1 = 1; k1 <= (num - 1); k1++) { // 3
					loop100: for (int k2 = (k1 + 1); k2 <= num; k2++) { // 4
						result = nvAdd(bufTemp[k1], bufTemp[k2]);
						if (numt != 0) { // 5
							for (int k3 = 1; k3 <= numt; k3++) { // 6
								if (result == G.nOffset[k3])
									continue loop100;
							} // end for k3 -6
						} // end if numt != 0 -5
						numt++;
						G.nOffset[numt] = result;
					} // end loop100 -4
				} // end for k1 -3

				/* C ADJUST VCOMP BY THE NEW TERMS */
				if (numt == 0) { // 3
					do150to200(ir, vcomp);
					continue loop200;
				} // -3
				for (int j = 1; j <= numt; j++) { // 3
					im = G.nOffset[j];
					vcomp = vcomp + jsign * G.xMsqr[im];
					bufTemp[j] = im;
				} // -3
				num = numt;

			} // end while looprooK -2

			/* NOW DO THE DIVISION OF VCOMP */
			do150to200(ir, vcomp);

		} // end for ir loop200 -1

		if (Flags.debug)
			Bug.bug06gphase2after200();

		cmpMod();
		;
	} // end go
	// ========================== Gphase2.go ==============
	// end
	// ========================== do45to50 ==============
	// start

	private static void do45to50(int ir, double ssumv) {
		G.sumSQ[ir] = ssumv;
		G.totalMsq = G.totalMsq + ssumv;
		G.xMsqr[ir] = ssumv / (double) G.numDegFreedom[ir];
	} // end do45to50
	// ========================== do45to50 ==============
	// end
	// ========================== cmpMod ==============
	// start/

	private static void cmpMod() throws IOException {

		if (!Flags.nonRandom) {
			for (int j = 1; j <= G.numTotal; j++) {
				G.varMod[j] = G.varComp[j];
			}
			return;
		} // end if nonRandom
		double sum;
		int j1, jrow, ndiv, num;

		loop300: for (int row = 1; row <= G.numTotal; row++) { // 1

			sum = G.varComp[row];
			num = G.indexTotal[row];
			if (num == G.numMain) { // 2
				G.varMod[row] = sum;
				continue loop300;
			} // end if num -2
			j1 = G.orderedMEF[num + 1];

			loop280: for (int j = j1; j <= G.numTotal; j++) { // 2
				jrow = G.orderedTEF[j];
				if (!U.lSuper(row, jrow))
					continue loop280;

				ndiv = 1;
				loop230: for (int k = 1; k <= G.numMain; k++) { // 3
					if (G.ID[row][k] > 0)
						continue loop230;
					if (G.ID[jrow][k] > 0)
						ndiv = ndiv * G.mainUniverse[k];
				} // end for k loop 230 -3
				if (ndiv == 0)
					continue loop280;

				sum = sum + G.varComp[jrow] / (double) ndiv;
				if (Flags.debug)
					Bug.bug07gphase2cmpmod(row, jrow, ndiv, sum);

			} // end loop280 -2
			G.varMod[row] = sum;
		} // end loop300 -1

	} // end cmpMod
	// ========================== cmpMod ==============
	// end
	// ========================== nvAdd ==============
	// start

	private static int nvAdd(int row1, int row2) 
			 throws IOException {
// 1
		int nvadd = 0;
		int jtotal = 0;
		int[] hold = new int[7];

		for (int j = 1; j <= G.numMain; j++) { // 2
			hold[j] = 0;
			if (G.ID[row1][j] + G.ID[row2][j] > 0) { // 3
				jtotal++;
				hold[j] = 1;
			} // -3
		} // end for j // -2
		int j1 = G.orderedMEF[jtotal];
		int j2 = G.orderedMEF[jtotal + 1] - 1;
		loop30: for (int j = j1; j <= j2; j++) { // 2
			nvadd = G.orderedTEF[j];
			loop20: for (int k = 1; k <= G.numMain; k++) { // 3
				if (hold[k] + G.ID[nvadd][k] == 0)
					continue loop20;
				if (hold[k] * G.ID[nvadd][k] == 0)
					continue loop30;
			} // end for k // -3
			return nvadd;
		} // end for j // -2
		Sys.setErrorMsg(" NVADD flaw in logic @#$@#");

		return 0;
	} // end nvAdd-1
	// ========================== nvAdd ==============
	// end
	// ========================== do150to200 ============
	// start

	private static void do150to200(int ir, double vcomp) {

		int ndiv = 1;
		for (int j = 1; j <= G.numMain; j++) {
			if (G.ID[ir][j] == 0)
				ndiv = ndiv * G.mainLevel[j];
		} // end forj
		G.varComp[ir] = vcomp / (double) ndiv;

	}
	// ========================== do150to200 ============
	// end
	// ========================== ssVal ==============
	// start

	private static double ssVal(int rowc, int rowm) {

		double ssval = 0.0;
		if (!U.lSuper(rowm, rowc))
			return ssval;
		loop10: for (int j = 1; j < G.numMain; j++) {
			if (G.ID[rowc][j] <= 1)
				continue loop10;
			if (G.ID[rowm][j] == 0)
				return ssval;
		} // end for j
		ssval = G.xBarb[rowm];
		return ssval;
	}

	// ========================== ssVal ==============
	// end

}	// end Class Gphase2

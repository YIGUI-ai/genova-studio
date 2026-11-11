package initGen;

import java.io.IOException;

public class Dcalc {

	private static double xe;
	private static double xd;
	private static double xep;

	private static boolean l23;
	private static boolean l26;
	private static boolean l31;
	private static boolean l38a;
	private static boolean l38b;

	private static double dv20;
	private static double dv21;
	private static double dv22;
	private static double dv23;
	private static double dv24;
	private static double dv25;
	private static double dv26;
	private static double dv27;
	private static double dv28;
	private static double dv29;
	private static double dv30;
	private static double dv31;
	private static double dv32;
	private static double dv33;
	private static double dv34;
	private static double dv35;
	private static double dv37;
	private static double dv38;
	private static double dv39;
	private static double dv40;

	// ======================= dPrint2 =========
	// start
	public static void dPrint2() throws IOException {

		StringBuilder sa = new StringBuilder();
		int j = 14;
		if (D.dstudyCut || D.numDcut > 0)
			j = j + 3 + (D.numDcut + 1) / 2;
		if (PR.getNumLines() + j > Sys.getLinesPerPage()) {
			PR.setNumLines(100);
		}
		IO.doPrintLine(D.divider);
		IO.mybw.newLine();
		IO.doPrintLine(D.d[2][1],1);
		IO.doPrintLine(D.d[2][2]);
		IO.doPrintLine(D.d[2][3]);
		IO.mybw.newLine();
		sa.append(D.d[2][4]);
		sa.append(Out.dx(12, 5, dv20));
		sa.append(Out.dx(10, 5, dv21));
		sa.append(Out.dx(10, 5, dv22));
		IO.doPrintLine(sa.toString(), 1);
		sa.setLength(0);

		sa.append(D.d[2][5]);
		sa.append(Out.dx(12, 5, dv23));
		sa.append(Out.dx(10, 5, dv24));
		sa.append(Out.dx(10, 5, dv25));
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		sa.append(D.d[2][6]);
		sa.append(Out.dx(12, 5, dv26));
		sa.append(Out.dx(10, 5, dv27));
		sa.append(Out.dx(10, 5, dv28));
		sa.append(D.d2l6p2);
		sa.append(Out.dx(9, 5, dv29));
		sa.append("   (");
		sa.append(Out.dx(8, 5, dv30)).append(")");
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		sa.append(D.d[2][7]);
		sa.append(Out.dx(12, 5, dv31));
		sa.append(Out.dx(10, 5, dv32));
		sa.append(Out.dx(10, 5, dv33));
		sa.append(D.d2l7p2);
		sa.append(Out.dx(9, 5, dv34));
		sa.append("   (");
		sa.append(Out.dx(8, 5, dv35)).append(")");
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		double d1 = Math.sqrt(dv38);
		sa.append(D.d[2][8]);
		sa.append(Out.dx(12, 5, dv38));
		sa.append(Out.dx(10, 5, d1));
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		if (D.dstudyCut && D.numDcut > 0) {
			String lp = " (";
			String rp = ")  ";
			IO.doPrintLine(D.divider);
			sa.append(Out.blank(18));
			sa.append("Dbar =");
			sa.append(Out.dx(10, 6, dv37));
			sa.append(Out.blank(36));
			sa.append("Phi (Lambda =    Dbar) =");
			sa.append(Out.dx(10, 5, dv39) + lp);
			sa.append(Out.dx(9, 5, dv40) + rp);
			IO.mybw.newLine();
			IO.doPrintLine(sa.toString(), 1);

			boolean first = true;
			for (int k = 1; k <= D.numDcut; k++) {
				if (first) {
					sa.setLength(0);
					buildCutOut(k, sa);
					first = false;
					if (k == D.numDcut)
						IO.doPrintLine(sa.toString());
				} else { // here is not the first
					buildCutOut(k, sa);
					first = true;
					IO.doPrintLine(sa.toString());
				}
			} // end for k
		} // end if 
		if (D.maxDstudy > 1) {
			D.dSummary[D.numDstudy][1] = dv20;
			D.dSummary[D.numDstudy][2] = dv23;
			D.dSummary[D.numDstudy][3] = dv26;
			D.dSummary[D.numDstudy][4] = dv31;
			D.dSummary[D.numDstudy][5] = dv38;
			D.dSummary[D.numDstudy][6] = dv29;
			D.dSummary[D.numDstudy][7] = dv34;
		}
		IO.doPrintLine(D.divider);
		IO.doPrintLine(D.d[2][9]);
		PR.setNumLines(100);
		IO.mybw.flush();

	} // end dPrint2
	// ======================= dPrint2 =========
	// end
	// ======================= buildCutOut =========
	// start

	private static void buildCutOut(int k, StringBuilder sa) {
		// TODO Auto-generated method stub
		
		String lp = " (";
		String rp = ")  ";
		String rpe = ") =";
		String b11 = "           ";
		String dophi = b11 + "Phi (Lambda =";
		sa.append(dophi);
		sa.append(Out.dx(8, 4, D.xDcut[k]));
		sa.append(rpe);
		sa.append(Out.dx(10, 5, D.dc43[k]));
		sa.append(lp);
		sa.append(Out.dx(9, 5, D.dc42[k]));
		sa.append(rp);

	} // end builddCutOut

	// ======================= buildCutOut =========
	// end
	// ======================= dProc2 =========
	// start
	public static void dProc2() throws IOException {

		double vx, dcnum;
		int[] ind23 = new int[64];
		int[] ind26 = new int[64];
		int[] ind31 = new int[64];

		int ind, jnd, iorder, jorder;

		int nd23 = 0;
		int nd26 = 0;
		int nd31 = 0;
		dv20 = D.v18[D.starID];
		dv21 = Math.sqrt(dv20);
		dv22 = D.se19[D.starID];
		dv23 = 0.0d;
		dv25 = 0.0d;
		dv26 = 0.0d;
		dv28 = 0.0d;
		dv31 = 0.0d;
		dv33 = 0.0d;
		dv38 = 0.0d;
		double va = (double) D.sampID[D.starID][D.numDstudy];
		double v1 = 1.0d / va;
		double v2 = v1;

		if (D.dUniverse[D.starID] > 0)
			v2 = 1.0d - ((double) D.dUniverse[D.starID]) * v1;
		loop100: for (int in = 1; in <= G.numTotal; in++) {

			dsIDRow(in);
			if (l23) {
				nd23++;
				ind23[nd23] = in;
			}
			if (l26) {
				nd26++;
				ind26[nd26] = in;
			}
			if (l31) {
				nd31++;
				ind31[nd31] = in;
			}
			if (l38a) {
				dv38 = dv38 + v2 * D.v18[in];
				continue loop100;
			}
			if (l38b)
				dv38 = dv38 + v1 * D.v18[in];
		} // end for in loop100
		if (Flags.debug)
			Bug.bug48dproc2(nd23, nd26, nd31, ind23, ind26, ind31, va, v2, dv38);
		for (int i1 = 1; i1 <= nd23; i1++) {
			ind = ind23[i1];
			dv23 = dv23 + D.v18[ind];
			iorder = G.orderedTEF[ind];
			for (int j1 = i1; j1 <= nd23; j1++) {
				jnd = ind23[j1];
				jorder = G.orderedTEF[jnd];
				vx = 1.0d;
				if (i1 != j1)
					vx = 2.0d;
				dv25 = dv25 + vx * U.valMat(D.offsetDN, iorder, jorder);
			} // end for j1
		} // end for ix
		dv24 = Math.sqrt(dv23);

		// NOW CALCULATE VARIANCE OF LOWER CASE DELTA (DV26)

		for (int i1 = 1; i1 <= nd26; i1++) {
			ind = ind26[i1];
			dv26 = dv26 + D.v18[ind];
			iorder = G.orderedTEF[ind];
			for (int j1 = i1; j1 <= nd26; j1++) {
				jnd = ind26[j1];
				jorder = G.orderedTEF[jnd];
				vx = 1.0d;
				if (j1 != i1)
					vx = 2.0d;
				dv28 = dv28 + vx * U.valMat(D.offsetDN, iorder, jorder);
			} // end for j1
		} // end for i1
		dv27 = Math.sqrt(dv26);

		// NOW CALCULATE VARIANCE OF UPPER CASE DELTA

		for (int i1 = 1; i1 <= nd31; i1++) {
			ind = ind31[i1];
			dv31 = dv31 + D.v18[ind];
			iorder = G.orderedTEF[ind];
			for (int j1 = i1; j1 <= nd31; j1++) {
				jnd = ind31[j1];
				jorder = G.orderedTEF[jnd];
				vx = 1.0d;
				if (i1 != j1)
					vx = 2.0d;
				dv33 = dv33 + vx * U.valMat(D.offsetDN, iorder, jorder);
			} // end for j1
		} // end for i1
		if (Flags.debug)
			Bug.bug49dproc2(dv25, dv28, dv33, dv31, dv26, dv35);

		dv25 = Math.sqrt(dv25);
		dv28 = Math.sqrt(dv28);
		dv33 = Math.sqrt(dv33);
		dv32 = Math.sqrt(dv31);

		dv30 = 0.0d;
		if (dv26 > 0.0d)
			dv30 = dv20 / dv26;
		dv29 = dv30 / (1.0d + dv30);

		dv35 = 0.0d;
		if (dv31 > 0.0d)
			dv35 = dv20 / dv31;
		dv34 = dv35 / (1.0d + dv35);

		dv37 = G.grandMean;
		dv38 = (dv31 - dv26) + dv38;
		dv40 = 0.0d;
		if (dv31 > 0.0d)
			dv40 = (dv20 - dv38) / dv31;
		dv39 = dv40 / (1.0d + dv40);
		if (!D.dstudyCut)
			return;

		// NOW SET UP CUTTING SCORE OUTPUTS
		double temp;
		dcnum = dv20 - dv38;
		for (int ix = 1; ix <= D.numDcut; ix++) {
			temp = D.xDcut[ix] - G.grandMean;
			D.dc42[ix] = (dcnum + temp * temp) / dv31;
			D.dc43[ix] = D.dc42[ix] / (1.0d + D.dc42[ix]);
		} // end for i

	} // end dProc2

	// ======================= dProc2  =========
	// end
	// ======================= dsIDRow =========
	// start

	private static void dsIDRow(int row) {
	// calculate the row of ID the dStudy
		// calculations will enter into
		l23 = false;
		if (G.ID[row][D.starID] != 0)
			l23 = true;
		l26 = l23;
		if (row == D.starID)
			l26 = false;
		l31 = true;
		boolean noto20 = true;
		loop10: for (int k = 1; k <= G.numMain; k++) {
			if (G.ID[D.starID][k] != 0)
				continue loop10;
			if (G.ID[row][k] != 0) {
				noto20 = false;
				break loop10;
			}
		} // end for k
		if (noto20)
			l31 = false;
		l38a = false;
		l38b = false;
		if (G.ID[row][D.starID] == 0)
			return;
		// NOW CHECK FOR PRIMARY OR NOT PRIMARY
		if (G.ID[row][D.starID] == 1) {
			l38a = true;
			return;
		}
		// NOT PRIMARY
		l38b = true;
		return;

	} // end dsIDRow

	// ======================= dsIDRow =========
	// end
	// ==================   dP1Init    =========
	// start
	
	private static boolean dP1Init(double[] xsamp,
			int[] object)  throws IOException {
	
		int isamp, i1;
		PrD.dsHead(0);
		loop10: for (int j = 1; j <= G.numMain; j++) {
			isamp = D.sampID[j][D.numDstudy];
			xsamp[j] = (double) isamp;
			object[j] = G.ID[D.starID][j];
			i1 = D.dUniverse[j];
			if (isamp > i1 && i1 != 0) {
				doPrint901(j); // returns false
				return false;
			}
			if (i1 <= G.mainUniverse[j] || G.mainUniverse[j] == 0)
				continue loop10;
			 doPrint901(j); // returns false
			return false;
		} // end for j loop10

		return true;
	}	// end sP1Init
	// ==================   dP1Init           =========
	// end	
	// ==================   setupZEandZEP    =========
	// start

	private static void setupZEandZEP(double [] ze, 
		double[] zep, double[] xsamp, double d1em6) {
		
		int na, ni;
		double xs;
		loop25: for (int i = 1; i <= G.numMain; i++) {
			xs = xsamp[i];
			if (xs < d1em6) {
				ze[i] = 0.0d;
				zep[i] = 0.0d;
				continue loop25;
			}
			na = G.mainUniverse[i];
			ni = D.dUniverse[i];
			ze[i] = 1.0d;
			zep[i] = 1.0d;
			if (na != 0)
				ze[i] = 1.0d - xs / (double) na;
			if (ni != 0)
					zep[i] = 1.0d - xs / (double) ni;
		} // end for i loop25
		
	}// end setupZEandZEP
	// ================== setupZEandZEP =========
	// end
	// ================== dProc1 =========
	// start

	public static boolean dProc1() throws IOException {

		double[] f = new double[7];
		double[] h = new double[7];
		double[] xsamp = new double[7];
		double[] zep = new double[7];
		double[] ze = new double[7];
		double xedp;
		double hf, fm, hm, x, hxh;
		int  i1, j1, na, ni;
		int[] hold = new int[7];
		int[] object = new int[7];

		if (Flags.debug)
			IO.doPrintLine(" Entered DPROC1 " + " D.numDstudy = " + D.numDstudy);
		// SET UP XSAMP & IOBJ & DO CHECKS

		double d1em6 = Math.pow(10, -6);
		
		if ( ! dP1Init(xsamp,object))	
			return false;

		// SET UP ZE AND ZEP
		
		setupZEandZEP(ze, zep, xsamp, d1em6);
		if (Flags.debug)
			Bug.bug41dproc1(xsamp, ze, zep);

		// DO FIRST SET OF CALCULATIONS

		for (int in = 1; in <= G.numTotal; in++) {
			xe = 1.0d;
			xd = 1.0d;
			xep = 1.0d;
			if (in != D.starID)
				doLoop50(in, object, zep, ze, xsamp);

			D.e11[in] = xe;
			D.ep16[in] = xep;
			D.dp12[in] = xd;
			xedp = xe / xd;
			D.v13[in] = xedp * D.varDGT[in];
			D.epdp[in] = xep / xd;
			D.se14[in] = xedp * Math.sqrt(U.valMat(D.offsetDV, in, in));
			if (Flags.debug)
				Bug.bug42dproc1(in, xedp);
		} // end for in

		// NOW START THE BIG LOOP FOR V15 & T

		for (int i = 1; i <= G.numMain; i++) {
			hxh = 1.0d;
			na = G.mainUniverse[i];
			ni = D.dUniverse[i];
			f[i] = (double) ni;
			if (ni == 0)
				hxh = 0.0d;
			if (na != 0)
				hxh = 1.0d - f[i] / (double) na;
			h[i] = hxh;
		} // end for i

		for (int i = 1; i <= G.numTotal; i++) {
			i1 = G.orderedTEF[i];
			x = 0.0d;
			for (int j = 1; j <= G.numMain; j++) {
				hold[j] = G.ID[i1][j];
			} // end for j

			loop180: for (int j = i; j <= G.numTotal; j++) {
				j1 = G.orderedTEF[j];
				if (i == j) {
					U.matPut(D.offsetDN, i, j, 1.0d);
					x = D.varDGT[i1];
					continue loop180;
				}
				hm = 1.0d;
				fm = 1.0d;
				boolean not170 = true;
				int idjk;
				loop160: for (int k = 1; k <= G.numMain; k++) {
					idjk = G.ID[j][k];
					if (hold[k] == 0) {
						if (idjk == 0)
							continue loop160;
						// HERE K IS IN BETA (J1), BUT NOT IN ALPHA
						if (idjk <= 1)
							hm = hm * h[k];
						fm = fm * f[k];
						continue loop160;
					} // end if hold[k] == 0
					if (idjk == 0) {
						not170 = false;
						break loop160;
					}
				} // end for k
				if (not170) {

					if (fm >= d1em6 && hm >= d1em6) {
						hf = hm / fm;
						x = x + hf * D.varDGT[j1];
						U.matPut(D.offsetDN, i, j, hf);
						if (Flags.debug)
							Bug.bug43dproc1(i, j, i1, j1, x, hf, hm, fm, D.varDGT[j1]);
						continue loop180;
					}
				} // end if dokiip160 or 170

				U.matPut(D.offsetDN, i, j, 0.0d);

			} // end for j loop180
			D.v15[i1] = x;
			D.v18[i1] = D.epdp[i1] * x;
		} // end for i (190

		// NOW COMPUTE THE B MATRIX AT LOCATION NOFFDB
		// NOW COMPUTE C MATRIX - STORE IN offsetDN (OVER T)

		calculateBCMatrix();
		return true;
	}
	// ======================= dProc1 =========
	// end
	// ======================= calculateBCMatrix =======
	// start

	private static void calculateBCMatrix() throws IOException {

		// NOW COMPUTE THE B MATRIX AT LOCATION NOFFDB

		int noffdb = D.offsetDN + Sys.offsetPT;
		double x, tik;
		for (int in = 1; in <= G.numTotal; in++) {
			for (int j = in; j <= G.numTotal; j++) {
				x = 0.0d;
				loop230: for (int k = in; k <= G.numTotal; k++) {
					tik = U.valMat(D.offsetDN, in, k);
					if (tik == 0.0d)
						continue loop230;
					for (int m = j; m <= G.numTotal; m++) {
						x = x + tik * U.valMat(D.offsetDN, j, m) * U.valMat(D.offsetDV, k, m);
					} // end for m
				} // end fort k loop230
				U.matPut(noffdb, in, j, x);
			} // end for j
		} // end for in
		if (Flags.debug)
			Bug.bug44dproc1(noffdb);

		// NOW COMPUTE C MATRIX - STORE IN offsetDN (OVER T)

		int i1, j1;
		double c;
		for (int in = 1; in <= G.numTotal; in++) {
			i1 = G.orderedTEF[in];
			for (int j = in; j <= G.numTotal; j++) {
				j1 = G.orderedTEF[j];
				c = U.valMat(noffdb, in, j) * D.epdp[i1] * D.epdp[j1];
				U.matPut(D.offsetDN, in, j, c);
				if (in == j)
					D.se19[i1] = Math.sqrt(c);
			} // end for j
		} // end for in
	} // end calculateBCMatrix
	// ======================= calculateBCMatrix =======
	// end
	// ======================= doLoop50 =======
	// start

	private static void doLoop50(int in, int[] object, double[] zep, double[] ze, double[] xsamp) {
		loop50: for (int j = 1; j <= G.numMain; j++) {
			if (object[j] != 0)
				continue loop50;

			if (G.ID[in][j] == 1) {
				xep = xep * zep[j];
				xe = xe * ze[j];
			} // end if G.ID
			if (G.ID[in][j] >= 1) {
				xd = xd * xsamp[j];
			} // end if G.ID
		} // end for j loop50

	} // end doLoop50
	// ======================= doLoop50 =======
	// end
	// ======================= doPrint901 =======
	// start

	private static void doPrint901(int j) throws IOException {

		IO.doPrintLine(" D Study sample <= D Study " +
		"Universe <= G Study Universe is not " + "True for "
				+ G.mefChar[j] + " D Study skipped");
		return;

	} // end doPrint901
	// ======================= doPrint901 =======
	// end

}	// end Class Dcalc

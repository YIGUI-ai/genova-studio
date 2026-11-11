package initGen;

import java.io.IOException;

public class Bug {

	// this class will hold all the debugging routines for jGENOVA
	

	public static void bug01gphase1debug200(int jtemp) throws IOException {
		// in Gphas1 after 200
		String str;
		str = " CALL TO INDEX FOR Z ARRAY: JTEMP =";
		str = str + Out.intx(8, jtemp) + " NMEFM1 =" + Out.intx(4, G.numMain - 1) + " NZOFF =" + Out.intx(8, G.nzOffset)
				+ " NZDIM NZDEX";

		PR.bugLine(str);
		IO.mybw.flush();

		for (int k = 1; k <= G.numMain - 1; k++) {
			str = Out.intx(6, G.nzDim[k]) + Out.intx(6, G.nzIndex[k]);
			PR.bugLine(str);
		} // end for k
	}

	public static void bug02gphase1debug250() throws IOException {
		String str = "  XBARB VALUES =";
		for (int k = 1; k <= G.numTotal; k++) {
			str = str + Out.dx(8, 3, G.xBarb[k]);
		}
		PR.bugLine(str);
		int first = 1;
		int last;
		while (first < G.nxtOff) {
			last = first + 9;
			str = "  XBUF VALUES =";
			if (last > G.nxtOff)
				last = G.nxtOff;
			for (int k = first; k <= last; k++) {
				str = str + Out.dx(8, 4, G.xbuf[k]);
			}
			PR.bugLine(str);
			first = first + 10;
		}
	}

	public static void bug03gphase1Final() throws IOException {
		String str = " *** AT END OF GPHAS1: XMEAN =" + Out.dx(20, 4, G.xMean) + " XGRAND ="
				+ Out.dx(20, 6, G.grandMean) + " Z ARRAY";
		PR.bugLine(str);
		int first = 1;
		int last = 0;
		int limit = G.nxtOff;
		str = "";
		while (first < limit) {
			last = last + 12;
			if (last > limit)
				last = limit;
			for (int k = first; k < last; k++) {
				str = str + Out.dx(10, 4, G.xbuf[k]);
			}
			PR.bugLine(str);
			first = first + 12;
			str = "";
		} // end while
	}

	public static void bug04gphas1490(int irec) throws IOException {
		String str = "";
		// ====================
		PR.bugLine("  IN GPHAS1: RECORD #" + Out.intx(7, irec) + " HAS VALUES");
		int first = 1;
		int last = 0;
		while (first < G.inRecord) {
			last = last + 12;
			if (last > G.inRecord)
				last = G.inRecord;
			for (int k = first; k < last; k++) {
				str = str + Out.dx(10, 4, G.xbuf[k]);
			}
			PR.bugLine(str);
			str = "";
			first = first + 12;
		}
		// ===========================
		PR.bugLine(" IN GPHAS1: XBUF VALUES FOR OFFSET =" + G.nzOffset);
		str = "";
		;
		first = G.inRecord + 1;
		last = 0;
		while (first < G.nzOffset) {
			last = first + 12;
			if (last > G.nzOffset)
				last = G.nzOffset;
			for (int k = first; k < last; k++) {
				str = str + Out.dx(10, 4, G.xbuf[k]);
			}
			PR.bugLine(str);
			str = "";
			first = first + 12;
		}
		// ===================
		for (int j = 1; j <= G.nPass1; j++) {

			str = " J =" + Out.intx(4, j) + "  IPASS1(J) =" + Out.intx(4, G.iPass1[j]) + "  XBARB(IPASS1(J)) ="
					+ Out.dx(20, 4, G.xBarb[G.iPass1[j]]);

			PR.bugLine(str);
		}
		str = "IN GPHAS1: AFTER RECORD #" + Out.intx(7, irec) + "  XMEAN =" + Out.dx(20, 6, G.xMean);
		PR.bugLine(str);
		IO.mybw.flush();

	}

	public static void bug08gphase2after20() throws IOException { // 1

		String str;
		int first = 1;
		int last;
		while (first <= G.numTotal) { // 2
			last = first + 9;
			str = " IN GPHAS2: XBARB =";
			if (last > G.numTotal)
				last = G.numTotal;
			for (int k = first; k <= last; k++) { // 3
				str = str + Out.dx(12, 6, G.xBarb[k]);
			} // -3
			PR.bugLine(str);
			first = first + 10;
		} // -2

		IO.mybw.flush();
	} // -1

	public static void bug09gphase2in40(int ir, int k, int ntot, int nest, int main, double ssumv) throws IOException {

		String str = " DURING SUMSQ CALC FOR ROW " + Out.intx(3, ir) + " SSUMV =" + Out.dx(14, 6, ssumv) + " NORTEF ="
				+ Out.intx(4, G.orderedTEF[k]) + " NTOT =" + Out.intx(4, ntot) + " NEST =" + Out.intx(4, nest)
				+ " MAIN =" + Out.intx(5, main);
		PR.bugLine(str);
		IO.mybw.flush();
	}

	public static void bug10gphase2after50() throws IOException {

		PR.bugLine(" IN GPHAS2: SUMSQ ,  NDEGF ,  XMSQR =");
		for (int k = 1; k <= G.numTotal; k++) {
			PR.bugLine(Out.dx(20, 6, G.sumSQ[k]) + Out.intx(8, G.numDegFreedom[k]) + Out.dx(20, 6, G.xMsqr[k]));
		}
		PR.bugLine(Out.dx(20, 6, G.totalMsq));
		IO.mybw.flush();
	}

	public static void bug11gphase2after60(int ir, int num, int irm, int jsign, double vcomp) throws IOException {
		String str = " ON STEP 1 FOR VAR. COMP. FOR ROW:" + Out.intx(3, ir) + "  NUM =" + Out.intx(4, num) + "  IRM ="
				+ Out.intx(4, irm) + "  VCOMP =" + Out.dx(20, 6, vcomp) + "  XMSQR(JRM) ="
				+ Out.dx(20, 6, G.xMsqr[irm]);

		PR.bugLine(str);
		IO.mybw.flush();

	} // end debugafter60

	public static void bug05gphase2after70(int ir, int im, int jsign, double vcomp) throws IOException {
		String str = " FOR ROW IR =" + Out.intx(3, ir) + "  FINAL COMPONENT ROW =" + Out.intx(3, im) + "  SIGN ="
				+ Out.intx(3, jsign) + "  VCOMP =" + Out.dx(20, 6, vcomp) + "  XMSQR(IM) ="
				+ Out.dx(20, 6, G.xMsqr[im]);
		PR.bugLine(str);
		IO.mybw.flush();

	} // end of debug after 70

	public static void bug06gphase2after200() throws IOException {

		String str;
		PR.bugLine(" IN GPHAS2 THE VAR. COMPONENTS ARE:");
		str = "";
		for (int k = 1; k <= G.numTotal; k++) {
			str = str + Out.intx(4, k) + Out.dx(20, 8, G.varComp[k]);
			PR.bugLine(str);
			str = "";
		}
		IO.mybw.flush();
	}

	public static void bug07gphase2cmpmod(int row, int jrow, int ndiv, double sum) throws IOException {
		String str = " IN CMPMOD FOR MIXED MODEL:  IR =" + Out.intx(4, row) + "  JROW =" + Out.intx(4, jrow)
				+ "  NDIV =" + Out.intx(8, ndiv) + "  SUM =" + Out.dx(16, 6, sum) + "  VARCMP(JROW) ="
				+ Out.dx(16, 6, G.varComp[jrow]);

		PR.bugLine(str);
		IO.mybw.flush();

	}

	public static void bug12gphase3before150(int j1, int jrow, int m, double x, int ndiv, double hval, int mdiv,
			double vtemp) throws IOException {
		String str = " ** DURING VARIN  RECALC FOR ROW" + Out.intx(4, j1) + " VAROUT(JROW) ="
				+ Out.dx(14, 7, G.varFin[jrow]) + "  JROW =" + Out.intx(3, jrow) + "  VTEMP =" + Out.dx(14, 7, vtemp)
				+ "  M =" + Out.intx(4, m) + " ** X =" + Out.dx(14, 7, x) + "  ND =" + Out.intx(4, ndiv) + "  HVAL ="
				+ Out.dx(14, 7, hval) + "  MDIV =" + Out.intx(8, mdiv);
		PR.bugLine(str);
		IO.mybw.flush();
	}

	public static void bug13gphase3after200(double[] xems) throws IOException {
		PR.bugLine(" AFTER GPHAS3:'");
		PR.bugLine("         EMS      VAROUT");
		String str = "";
		for (int t = 1; t <= G.numTotal; t++) {
			str = Out.intx(3, t) + Out.dx(12, 7, xems[t]) + Out.dx(12, 7, G.varFin[t]);
			PR.bugLine(str);
		}
		PR.bugLine(" *** AT END OF GPHAS3:  EMS COEFFICIENTS:");
		double xhold;
		for (int j1 = 1; j1 <= G.numTotal; j1++) {
			for (int j2 = j1; j2 <= G.numTotal; j2++) {
				xhold = U.valMat(0, j1, j2);
				str = " ROW =" + Out.intx(3, j1) + " COLUMN =" + Out.intx(3, j2) + " VALUE =" + Out.dx(20, 6, xhold);
				PR.bugLine(str);
			} // end for j2
		} // end for j1
		PR.bugLine(" *** AT END OF GPHAS3:  EMS COEFFICIENTS:");

		IO.mybw.flush();

	} // end debugafter200

	public static void bug14gphase3aftermatmpy() throws IOException {

		int j1;
		int j2;
		IO.doPrintLine(" *** IN GPHAS3 AFTER MATINV AND MATMPY:");
		IO.doPrintLine("    J  NORTEF(J)        P" + Out.fill(14, " ") + "PINV" + Out.fill(13, " ") + "VCVC");

		IO.mybw.newLine();
		for (int j = 1; j <= Sys.offsetPT; j++) {
			j1 = Sys.offsetPT + j;
			j2 = Sys.offsetSE + j;
			IO.doPrintLine(Out.intx(5, j) + Out.intx(7, G.orderedTEF[j]) + Out.ex(17, 7, G.xbuf[j])
					+ Out.ex(17, 7, G.xbuf[j1]) + Out.ex(17, 7, G.xbuf[j2]));
		}

	}

	public static void bug15gphase3ftermsvar() throws IOException {

		IO.doPrintLine(" *** AFTER MSVAR:");
		IO.doPrintLine("     J" + Out.fill(12, " ") + "VARMOD");
		for (int j = 1; j <= G.numTotal; j++) {
			IO.doPrintLine(Out.intx(6, j) + Out.dx(20, 7, G.varMod[j]));
		}
		IO.mybw.flush();
	}

	public static void bug16gprintafter70(int nument, double xmult, int[] jent, int kcol, double[] xrem)
			throws IOException {
		StringBuilder sd = new StringBuilder();

		PR.bugLine(" === IN FTPRNT:  NUMENT = " + Out.intx(4, nument) + "  XMULT =" + Out.dx(5, 1, xmult));
		sd.append("  JENT =");
		for (int j = 1; j <= nument; j++) {
			sd.append(Out.intx(3, jent[j]));
		}
		PR.bugLine(sd.toString());
		sd.setLength(0);
		sd.append("  XREM (");
		sd.append(Out.intx(3, kcol));
		sd.append(" TO numTotal) =");
		String start = sd.toString();

		String str;
		int first = kcol;
		int last;
		while (first <= G.numTotal) { // 2
			last = first + 9;
			str = start;
			if (last > G.numTotal)
				last = G.numTotal;
			for (int k = first; k <= last; k++) { // 3
				str = str + Out.dx(10, 1, xrem[k]);
			} // -3
			PR.bugLine(str);
			first = first + 10;
		} // end while
		IO.mybw.flush();
	} // end debugafter90

	public static void bug17gprintafter90(int j1, int j2, double xfn, double xn2, double xfd, double xd2)
			throws IOException {
		String sstr = " DURING FTPRNT: J1 =" + Out.intx(4, j1) + "  J2 =" + Out.intx(4, j2) + "  XFN ="
				+ Out.ex(14, 6, xfn) + "  XN2 =" + Out.ex(14, 6, xn2) + "  XFD =" + Out.ex(14, 6, xfd) + "  XD2 ="
				+ Out.ex(14, 6, xd2);
		PR.bugLine(sstr);
		IO.mybw.flush();
	}

	public static void bug18pass1gprep(int jIDcol, int passIDCols) throws IOException {

		IO.doPrintLine(" after gprep jIDcol, passIDCols " + jIDcol + " " + passIDCols);
		IO.doPrintLine("  numrec inrecord passno " + G.numRecs + " " + G.inRecord + " " + G.passNumber);
		if (G.passNumber == 1) {
			IO.doPrintLine(" in Gprep on  passnumber = 1 mzFIM = ");
		}
	}

	public static void bu19pass1gsetupDebug(boolean lPass2, int passIDCols) throws IOException {
		// debug output after GSETUP is done

		IO.doPrintLine(" After Gsetup  NPASS1 NXTOff " + "LPASS2  inRecord= numREc  " + " " + G.nPass1 + " " + G.nxtOff
				+ " " + lPass2 + " " + G.inRecord + " " + G.numRecs);
		IO.doPrintLine(" ROW   IPASS1  NOFF   NP1CEL LARRAY  passID");

		for (int p = 1; p <= G.nPass1; p++) {

			IO.doPrintLine(Out.intx(10, p) + Out.intx(8, G.iPass1[p]) + " " + G.nOffset[p] + " " + G.np1Cell[p] + " "
					+ G.lArray[p]);
			for (int m = 1; m <= passIDCols; m++) {
				IO.mybw.write(Out.intx(10, G.passID[p][m]));
			}
			IO.doPrintLine(" ");
			IO.mybw.flush();
		}

	}

	public static void bug40dproc() throws IOException {
		String str;
		str = " $$$ UPON ENTRY TO DSCHCK:  numMain = " + G.numMain + "  numMainG =" + D.numMainG + "  numTotal = "
				+ G.numTotal + "  numTotalG = " + D.numTotalG;
		IO.doPrintLine(str);
		str = "  megchar   mefcharG  MainLevel   MainUnivwerse";
		for (int i = 1; i <= 6; i++) {
			str = i + " " + G.mefChar[i] + "   " + D.mefCharG[i] + " " + G.mainLevel[i] + " " + D.sampID[i][1] + " "
					+ D.dUniverse[i] + " " + G.mainUniverse[i];
			IO.doPrintLine(str);
		}
		String st2;
		for (int k = 1; k <= 6; k++) {
			str = " G.ID" + k + "";
			st2 = " D.IDG " + k + " ";
			for (int j = 1; j <= 6; j++) {
				str = str + G.ID[k][j] + " ";
				st2 = st2 + D.IDG[k][j] + " ";
			}
			IO.doPrintLine(str + " " + st2);
		}

	}

	public static void bug41dproc1(double[] xsamp, double[] ze, double[] zep) throws IOException {
		StringBuilder sa = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();
		sa.append(" in DPROC1 : XSAMP = ");
		sb.append(" in DPROC1 : ZE    = ");
		sc.append(" in DPROC1 : ZEP   = ");
		for (int k = 1; k <= G.numMain; k++) {
			sa.append(Out.dx(10, 4, xsamp[k]));
			sb.append(Out.dx(10, 4, ze[k]));
			sc.append(Out.dx(10, 4, zep[k]));
		}
		IO.doPrintLine(sa.toString());
		IO.doPrintLine(sb.toString());
		IO.doPrintLine(sc.toString());
		IO.mybw.flush();

	} // end bug41dproc1

	public static void bug42dproc1(int in, double xedp) throws IOException {
		String str = " $$$ IN DPROC1: I =" + Out.intx(4, in) + "  E11(I) =" + Out.dx(10, 4, D.e11[in]) + "  DP12(I) ="
				+ Out.dx(10, 4, D.dp12[in]) + "  V13(I) =" + Out.dx(10, 4, D.v13[in]);
		IO.doPrintLine(str);
		str = "  XEDP =" + Out.dx(10, 4, xedp) + "  EPDP(I) =" + Out.dx(10, 4, D.epdp[in]) + "  SE14(I) ="
				+ Out.dx(20, 8, D.se14[in]) + "  varDGT(I) =" + Out.dx(20, 8, D.varDGT[in]) + " $$$";
		IO.doPrintLine(str);
		IO.mybw.flush();

	} // end bug42dproc1

	public static void bug43dproc1(int i, int j, int i1, int j1, double x, double hf, double hm, double fm,
			double dgtj1) throws IOException {
		String str = " $$$ DURING V15 CALC IN" + " DPROC1:  I =" + Out.intx(3, i) + "  J =" + Out.intx(3, j) + "  I1 ="
				+ Out.intx(3, i1) + "  J1 =" + Out.intx(3, j1) + "  X =" + Out.dx(20, 8, x) + "  H/F ="
				+ Out.dx(10, 6, hf);
		IO.doPrintLine(str);
		str = "  H =" + Out.dx(10, 6, hm) + "  F =" + Out.dx(10, 6, fm) + "  varDGT(J1)" + Out.dx(20, 8, dgtj1)
				+ " $$$";
		IO.doPrintLine(str);
		IO.mybw.flush();

	}// end bug43dproc1

	public static void bug44dproc1(int noffdb) throws IOException {

		double x, xb;
		IO.doPrintLine(" $$$ DURING DPROC1:");
		String str = "      I     J" + Out.blank(10) + " T(I,J)" + Out.blank(14) + " B(I<J)";
		IO.doPrintLine(str);
		for (int in = 1; in <= G.numTotal; in++) {
			for (int j = in; j <= G.numTotal; j++) {
				x = U.valMat(D.offsetDN, in, j);
				xb = U.valMat(noffdb, in, j);
				str = Out.intx(6, in) + Out.intx(6, j) + Out.dx(20, 8, x) + Out.dx(20, 8, xb);
			} // end for j
		} // end for ion
		IO.mybw.flush();

	} // end bug44dproc1

	public static void bug45dstran(int j, int k, int j1, int k1) throws IOException {
		String str = " $$$ DURING DSTRAN: " + "AFTER 80: J =" + Out.intx(8, j) + " K =" + Out.intx(8, k) + " j1 ="
				+ Out.intx(8, j1) + " k1 =" + Out.intx(8, k1);
		IO.doPrintLine(str);
	} // end bug45dstran

	public static void bug46dstran(int[][] itran) throws IOException {
		System.out.println(" got to bug4 - need " + "work ====dsrtran=========$$$");

	}

	public static void bug47dstran(int ic, int nc, int[] igr) throws IOException {
		System.out.println(" go to bug47 - need " + "work ====dstran===========$$$");

	}

	public static void bug48dproc2(int nd23, int nd26, int nd31, int[] ind23, int[] ind26, int[] ind31, double va,
			double v2, double dv38) throws IOException {
		String dpro = " IN DPROC2: ";
		StringBuilder sa = new StringBuilder();

		sa.append(dpro + "ND23 =" + Out.intx(5, nd23));
		sa.append("  IND23 =");
		IO.doPrintLine(sa.toString());
		bugint(nd23, 10, 8, ind23);

		sa.setLength(0);
		sa.append(dpro + "ND26 =" + Out.intx(5, nd26));
		sa.append("  IND326 =");
		IO.doPrintLine(sa.toString());
		bugint(nd23, 10, 8, ind26);

		sa.setLength(0);
		sa.append(dpro + "ND31 =" + Out.intx(5, nd31));
		sa.append("  IND31 =");
		IO.doPrintLine(sa.toString());
		bugint(nd23, 10, 8, ind31);

		sa.setLength(0);
		sa.append(" IN DPROC2: DV38 =");
		sa.append(Out.ex(20, 8, dv38));
		sa.append("  VA =");
		sa.append(Out.ex(20, 8, va));
		sa.append("  V2 =");
		sa.append(Out.ex(20, 8, v2));
		IO.doPrintLine(sa.toString());
	}

	private static void bugint(int limit, int howMany, int width, int[] array) throws IOException {
		int first = 1;
		int last = first + howMany - 1;
		StringBuilder sa = new StringBuilder();
		while (first < limit) {
			if (last > limit)
				last = limit;
			for (int k = 1; k <= last; k++) {
				sa.append(Out.intx(width, array[k]));
			}
			IO.doPrintLine(sa.toString());
			sa.setLength(0);
			first = first + howMany;
			last = last + howMany;
		} // end while first
	}

	public static void bugdx(int limit, int howMany, int width, int dec, double[] array) throws IOException {

		int first = 1;
		int last = first + howMany - 1;
		StringBuilder sa = new StringBuilder();
		while (first < limit) {
			if (last > limit)
				last = limit;
			for (int k = 1; k <= last; k++) {
				sa.append(Out.dx(width, dec, array[k]));
			}
			IO.doPrintLine(sa.toString());
			sa.setLength(0);
			first = first + howMany;
			last = last + howMany;
		} // end while first
	} // end bugdx

	public static void bug49dproc2(double dv25, double dv28, double dv33, double dv31, double dv26, double dv35)
			throws IOException {
		StringBuilder sa = new StringBuilder();
		sa.append(" IN DPROC2: DV25 =");
		sa.append(Out.ex(20, 8, dv25));
		sa.append(" DV28 =").append(Out.ex(20, 8, dv28));
		sa.append(" DV33 =").append(Out.ex(20, 8, dv33));
		sa.append(" DV31 =").append(Out.ex(20, 8, dv31));
		sa.append(" DV26 =").append(Out.ex(20, 8, dv26));
		sa.append(" DV35 =").append(Out.ex(20, 8, dv35));
		sa.append(" $$$");
		IO.doPrintLine(sa.toString());
		IO.mybw.flush();
	}

	public static void bug50dsmatrix1(double[] var) throws IOException {
		String str = " $$$ ENTERED DSMAT1:" + "  VARXXX =";
		IO.doPrintLine(str);
		Bug.bugdx(D.numTotalG, 6, 20, 8, var);

	} // end bug50dsmatrix1

	public static void bug51dsmatrix1(double[] var) throws IOException {
		String str = " $$$ AFTER DSMAT1:  " + "varDGT =";
		IO.doPrintLine(str);
		Bug.bugdx(G.numTotal, 6, 20, 8, D.varDGT);
	} // end bug51dsmatrix1

	public static void bug52dsmatrix2() throws IOException {

		double x;
		String str = " $$$ AFTER DSMAT2:  " + "NEW VAR-COVAR MATRIX =";
		IO.doPrintLine(str);
		for (int ix = 1; ix <= G.numTotal; ix++) {
			for (int j = ix; j <= G.numTotal; j++) {
				x = U.valMat(D.offsetDV, ix, j);
				str = "' VAR-COVAR(" + Out.intx(2, ix) + "," + Out.intx(2, j) + ") )" + Out.dx(20, 8, x);
			} // endc for j
		} // end fpor ix

	}

}

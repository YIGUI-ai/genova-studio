package initGen;

import java.io.IOException;

public class Gprint {

	// ========================== go =================
	// start

	public static void go() throws IOException {

		print1();
		IO.mybw.flush();
		print2();

		IO.mybw.flush();
		PR.clearTempHead();

	} // end Gprint.go

	// ========================== go =================
	// end
	// ========================== print1 =================
	// start

	private static void print1() throws IOException {

		PR.setTempHead("ANOVA TABLE");
		PR.setNumLines(200);

		StringBuilder sa = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();

		String a9 = "         ";
		String divider = "        " + Out.fill(114, "_");

		sa.append(Out.fill(22, " ")).append("Degrees      ");
		sa.append("Sums of        Sums of");
		sa.append(Out.fill(29, " "));
		sa.append("(QF = Quasi F Ratio)");
		sb.append(Out.fill(25, " ")).append("of      ");
		sb.append("Squares for    Squares for       ");
		sb.append("Mean            F            ");
		sb.append("F-test Degrees of Freedom");
		sc.append(a9).append("Effect       ");
		sc.append("Freedom    Mean Scores   ");
		sc.append("Score Effects     Squares      ");
		sc.append("Statistic        Numerator      ");
		sc.append("Denominator");

		String aMinus = "     ----------";
		int j1, j2, k;

		int[] nrtfrv = new int[64];
		for (int j = 1; j <= G.numTotal; j++) {
			k = G.orderedTEF[j];
			nrtfrv[k] = j;
		} // end for j

		// BIG LOOP FOR EACH FACET

		StringBuilder sdet = new StringBuilder();

			for (int j = 1; j <= G.numNested; j++) {
			j1 = G.firstRow[j];
			j2 = G.firstRow[j + 1] - 1;
			if ((j2 - j1 + 5 + PR.getNumLines()) > PR.getLineLimit()) {
				PR.setNumLines(200);				
				IO.doPrintLine(G.nhead1);
				IO.doPrintLine(G.nhead2);
				IO.doPrintLine(G.nhead3);

				IO.doPrintLine(divider);
				IO.doPrintLine(sa.toString());
				IO.doPrintLine(sb.toString());
				IO.doPrintLine(sc.toString());
				IO.doPrintLine(divider);
				PR.setNumLines(13);				

			} // end if
			/* START LOOP FOR EACH FACET SET */
			for (k = j1; k <= j2; k++) {

				sdet.setLength(0);
				sdet.append(a9).append(G.alphaID[k]);
				sdet.append(Out.intx(9, G.numDegFreedom[k]));

				if (Flags.gMSquare) {
					sdet.append(aMinus + aMinus);
					sdet.append(Out.dx(14, 5, G.xMsqr[k]));
				} // end if Flags.gMSquare
				else {
					sdet.append(Out.dx(15, 5, G.xBarb[k]));
					sdet.append(Out.dx(15, 5, G.sumSQ[k]));
					sdet.append(Out.dx(14, 5, G.xMsqr[k]));
				} // end if else
// 2.0				sdet.append(ftPrint(k));
				sdet.append(ftPrint(nrtfrv[k]));
				IO.doPrintLine(sdet.toString());
				IO.mybw.flush();
				sdet.setLength(0);
			} // end for k
			if (j != G.numNested)
				IO.doPrintLine(divider);
		} // end big for j loop
		IO.doPrintLine(divider);

		sdet.setLength(0);
		if (Flags.gStudy) {
			sdet.append(a9).append("Mean");
			sdet.append(Out.fill(16, " "));
			sdet.append(Out.dx(15, 5, G.xMean));
			IO.doPrintLine(sdet.toString());
			IO.doPrintLine(divider);
			sdet.setLength(0);
			sdet.append(a9).append("Total");
			sdet.append(Out.fill(6, " "));
			sdet.append(Out.intx(9, G.totalDegFreedom));
			sdet.append("               ");
			sdet.append(Out.dx(15, 5, G.totalMsq));
			IO.doPrintLine(sdet.toString());
			IO.doPrintLine(divider);
			IO.doPrintLine(" Note: for Generalizability " + "Analyses, F-Statistics should " + "be ignored");
			IO.mybw.flush();
			PR.clearTempHead();
		} else { // not Gstudy
			sdet.append(a9).append("Total");
			sdet.append("      ");
			sdet.append(Out.intx(9, G.totalDegFreedom));
			IO.doPrintLine(sdet.toString());
			IO.doPrintLine(divider);
			IO.doPrintLine(a9 + " Note: for Generalizability " + "Analyses, F-Statistics should " + "be ignored");
			IO.mybw.flush();
		} // end of else
		PR.setNumLines(200);

	} // end print`
	// ========================== print1 =================
	// end
	// ========================== print2 =================
	// start

	private static void print2() throws IOException {

		int kind;
		int[] nrtfrv = new int[64];
		for (int j = 1; j <= G.numTotal; j++) {
			kind = G.orderedTEF[j];
			nrtfrv[kind] = j;
		} // end for j

		String a5zero = "(0.0)";

		StringBuilder sa = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();

		String s0 = Out.fill(35, " ") + "M o d e l   V a r i a n c e   " + "C o m p o n e n t s";

		String a9 = "         ";
		String divider = "        " + Out.fill(77, "_");
		int diff = 86 - G.nhead3.length();
		String altHead3 = G.nhead3 + Out.fill(diff, " ") + "QFM = Quadratic Form";
		sa.append(Out.fill(22, " ") + "Degrees      ");
		sa.append("- - - - -   - - - - - - - -" + "   - - - - - - - - - -");
		sb.append(Out.fill(25, " ") + "of " + a9);
		sb.append("Using   " + a9);
		sb.append("Using EMS " + a9);
		sb.append("Standard");
		sc.append(a9 + "Effect       ");
		sc.append("Freedom      ");
		sc.append("Algorithm " + a9);
		sc.append("Equations  " + a9);
		sc.append("Error");

		PR.setTempHead("G Study Results");
		PR.setNumLines(200);

		int j1, j2;
		String out4;
		double xval;

		// BIG LOOP FOR EACH FACET

		StringBuilder sdet = new StringBuilder();

		for (int j = 1; j <= G.numNested; j++) {
			j1 = G.firstRow[j];
			j2 = G.firstRow[j + 1] - 1;
			if ((j2 - j1 + 5 + PR.getNumLines()) > PR.getLineLimit()) {
				PR.setNumLines(200);				
				IO.doPrintLine(G.nhead1);
				IO.doPrintLine(G.nhead2);
				IO.doPrintLine(altHead3);
				IO.doPrintLine(divider);
				IO.doPrintLine(s0);
				IO.doPrintLine(sa.toString());
				IO.doPrintLine(sb.toString());
				IO.doPrintLine(sc.toString());
				IO.doPrintLine(divider);
				PR.setNumLines(14);

			} // end if
			// START LOOP FOR EACH FACET SET //
			for (int k = j1; k <= j2; k++) {

				sdet.setLength(0);
				sdet.append(a9).append(G.alphaID[k]);
				sdet.append(Out.intx(9, G.numDegFreedom[k]));

				out4 = qfmCheck(k);

				if (Flags.printNegative || G.varMod[k] > 0.0d) {
					sdet.append("    " + Out.dx(13, 7, G.varMod[k]));
					sdet.append(out4);
				} else {
					sdet.append("       " + a5zero + "     ");
					sdet.append(out4);
				}

				if (Flags.printNegative || G.varFin[k] > 0.0d) {
					sdet.append(Out.dx(15, 7, G.varFin[k]));
					sdet.append(out4);
				} else {
					sdet.append("     " + a5zero + "     ");
					sdet.append(out4);
				}

				if (out4.matches("    ")) {
					xval = U.valMat(Sys.offsetSE, nrtfrv[k], nrtfrv[k]);
					xval = Math.sqrt(xval);
					sdet.append("  " + Out.dx(13, 7, xval));
				}
				IO.doPrintLine(sdet.toString());
				;
			} // end for k
			if (j != G.numNested)
				IO.doPrintLine(divider);

		} // end for j
		IO.doPrintLine(divider);
		IO.doPrintLine(a9 + " Note: the 'Algorithm' " + "and 'EMS' Estimated Variance " + "Components will be");
		IO.doPrintLine(a9 + "      Identical if " + "there are no Negative Estimates");
		IO.mybw.flush();
		PR.clearTempHead();
		PR.setNumLines(200);
	}
	// ========================== print2 =================
	// end
	// ========================== qfmCheck ===============
	// start

	private static String qfmCheck(int k) {
		String qfm = " QFM";
		loop30: for (int ilx = 1; ilx <= G.numMain; ilx++) {
			if (G.ID[k][ilx] == 0)
				continue loop30;
			if (G.mainLevel[ilx] != G.mainUniverse[ilx])
				return "    ";
		} // end for loop30
		return qfm;
	}
	// ========================== qfmCheck ===============
	// end
	// ========================== ftPrint =================
	// start
	private static String ftPrint(int jrow) throws IOException {

		String space = " ";
		StringBuilder sb = new StringBuilder();

		int[] jent = new int[64];
		double[] xrem = new double[64];
		int j1, j2, nument;
		int jcol = 0;
		int kcol = 0;
		boolean nomore;
		double xmult, xval;

		double xdiff = 1.0d * Math.pow(10, -12);
		if (jrow == G.numTotal)
			return space;

		nomore = true;
		loop10: for (int jind = (jrow + 1); jind <= G.numTotal; jind++) {
			if (U.valMat(0, jrow, jind) > 0.0d) {
				jcol = jind;
				nomore = false;
				break loop10;
			} // end f
		} // end for loop10
		if (nomore)
			return space;
		// got to 20

		nument = 1;
		jent[nument] = jcol;
		double dtemp;
		nomore = true;
		loop30: for (int kind = jcol; kind <= G.numTotal; kind++) {
			dtemp = U.valMat(0, jrow, kind) - U.valMat(0, jcol, kind);
			if (Math.abs(dtemp) > xdiff) {
				kcol = kind;
				nomore = false;
				break loop30;
			} // end if

		} // end for loop30

		if (nomore) {
			/* F-TEST CAN BE CALCULATED HERE */

			j1 = G.orderedTEF[jrow];
			j2 = G.orderedTEF[jcol];
			xval = -99999.99999;
			if (G.xMsqr[j2] > xdiff)
				xval = G.xMsqr[j1] / G.xMsqr[j2];
			sb.append("    ").append(Out.dx(10, 5, xval));
			sb.append(Out.fill(9, " "));
			sb.append(Out.intx(7, G.numDegFreedom[j1]));
			sb.append(Out.fill(9, " "));
			sb.append(Out.intx(7, G.numDegFreedom[j2]));
			return sb.toString();

		} // end if nomore loop30

		/*
		 * HAS TO BE A QUASI F RATIO HERE C UNLESS THERE IS SAMPLING FROM A
		 * FINITE UNIVERSE
		 */

		for (int j = 1; j <= G.numMain; j++) {
			if (G.mainLevel[j] < G.mainUniverse[j])
				return space;
		} // end for j

		for (int jind = kcol; jind <= G.numTotal; jind++) {
			xrem[jind] = U.valMat(-0, jrow, jind) - U.valMat(0, jcol, jind);
		} // end for j

		boolean more = true;
		while (more) { // 60
			nument++;
			if (nument > 3)
				return space;

			jent[nument] = kcol;
			xmult = 1.0d;
			if (xrem[kcol] <= 0.0d) {
				xmult = -1.0d;
				jent[nument] = -kcol;
			} // end if xrem
			jcol = kcol;
			if (Flags.debug)
				Bug.bug16gprintafter70(nument, xmult, jent, kcol, xrem);
			more = false;
			loop80: for (int jind = jcol; jind <= G.numTotal; jind++) {
				xrem[jind] = xrem[jind] - xmult * U.valMat(0, jcol, jind);
				if (more || (Math.abs(xrem[jind]) < xdiff))
					continue loop80;
				more = true;
				kcol = jind;
			} // end for j loop80
		} // end while more

		int jr1 = G.orderedTEF[jrow];
		double xfn = 0.0d;
		double xfd = 0.0d;
		double xn2 = 0.0d;
		double xd2 = 0.0d;
		double xm;

		loop100: for (int j = 1; j <= nument; j++) {

			j1 = jent[j];
			j2 = G.orderedTEF[Math.abs(j1)];
			xm = G.xMsqr[j2];
			if (j1 >= 0) {
				/* POSITIVE - PART OF DENOMINATOR */

				xfd = xfd + xm;
				xd2 = xd2 + xm * (xm / (double) G.numDegFreedom[j2]);
				if (Flags.debug)
					Bug.bug17gprintafter90(j1, j2, xfn, xn2, xfd, xd2);
				continue loop100;
			} // end if j1
			/* NEGATIVE - PART OF NUMERATOR */

			xfn = xfn + xm;
			xn2 = xn2 + xm * (xm / (double) G.numDegFreedom[j2]);
			if (Flags.debug)
				Bug.bug17gprintafter90(j1, j2, xfn, xn2, xfd, xd2);

		} // end for j loop100

		int jden, jnum;
		xm = G.xMsqr[jr1];
		if ((xfd - xfn) > 0.0d) {
			/* HERE USE THE STANDARD QUASI F FORMULA */

			xfd = xfd - xfn; // 11....
			xval = xm / xfd;
			if ((xn2 + xd2) <= xdiff)
				return space;
			jden = (int) (xfd * xfd / (xn2 + xd2) + .05);
			jnum = G.numDegFreedom[jr1];
		} // end if xfd - xfn
		else {
			/* HERE USE THE ALTERNATE QUASI F FORMULA */

			xfn = xfn + xm;
			xn2 = xn2 + xm * (xm / (double) G.numDegFreedom[jr1]);
			if ((xfd <= xdiff) || (xn2 <= xdiff) || (xd2 <= xdiff))
				return space;

			xval = xfn / xfd;
			jnum = (int) (xfn * xfn / xn2 + 0.5d);
			jden = (int) (xfd * xfd / xd2 + 0.5d);
		} // end if else
		/* BUILD OUTPUT LINE HERE */

		sb.setLength(0);
		;
		String qf = " QF      ";
		sb.append("    ").append(Out.dx(10, 5, xval));
		sb.append(qf).append(Out.intx(7, jnum));
		sb.append(qf).append(Out.intx(7, jden));
		sb.append(qf);
		return sb.toString();

	}
	// ========================== ftPrint =================
	// end

} // end Class Gprint

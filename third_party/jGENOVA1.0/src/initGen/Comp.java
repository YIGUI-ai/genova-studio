package initGen;

import java.io.IOException;

public class Comp {

	private static StringBuilder sa;
	private static StringBuilder sb;
	private static StringBuilder sc;
	private static StringBuilder sd;
	// ============== Comp.go ============
	// start

	public static void go(String heading) {
		// set title to this token

		PR.setMyTitle(heading);
		G.numMain = 0;
	}
	// ============== Comp.go ============
	// end
	// ============== Comp.end ============
	// start

	public static void end(String[] tokens, int howMany) throws IOException { // 1

		if (Flags.inMain)
			Sys.setErrorMsg(" ENDCOMP card found before " + "interactions effect VCOMP cards");
		if (howMany > 3)
			Sys.setErrorMsg(" too many arguments on" + " an ENDCOMP card");
		if (G.numMain < 2)
			Sys.setErrorMsg("  Less than 2 main effect VCOMP" + " cards before an ENDCOMP card ");

		PR.setNumLines(100);
		Flags.defaultDstudy = false;
		switch (howMany) { // 2
		case 1:
			checkDefault(tokens[0]);
			Sys.setWarning(" Single parameter on " + "ENDCOMP - is not 'DEFAULT'");
			Flags.defaultDstudy = false;
			break;
		case 2:
			checkGrand(tokens, 0);
			break;
		case 3:
			checkDefault(tokens[0]);
			checkGrand(tokens, 1);
			break;
		default:// here there are no parameters
			break;
		} // end switch // -2
		if (Flags.nonRandom)
			checkAllFixed();
		checkForComplete();
		if (Flags.name)
			Name.printNames();

		PrintID.printID();
		Gphase3.go();
		modComp();
		U.matInv();
		U.matMpy();
		printComp();

		PrG.ems();
		PrG.vcov();

		if (Flags.defaultDstudy) {
			D.defaultDstudy();
			Dproc.end();
			Flags.defaultDstudy = false;
		}

		// finish up
		Flags.gComponents = false;
		Flags.dstudyPossible = true;
		PR.setNumLines(100);
		PR.setPageType(1);

	}

	// ============== comp.end ============
	// end
	// ============== modComp ============
	// start
	private static void modComp() {
		for (int j = 1; j <= G.numTotal; j++) {
			G.varComp[j] = G.varMod[j];
		} // end for j
		boolean anyFixed = false;
		loop20: for (int j = 1; j <= G.numMain; j++) {
			if (G.mainUniverse[j] > 0) {
				anyFixed = true;
				continue loop20;
			} // end if
		} // end for j
		if (!anyFixed)
			return;
		// Here at least one fixed facet
		int j1, j2, jf1, irow, jrow, k1, ndiv;
		double sum, term;

		for (int jfac = 1; jfac <= G.numMain; jfac++) {
			jf1 = G.numMain - jfac + 1;
			j1 = G.orderedMEF[jf1];
			j2 = G.orderedMEF[jf1 + 1] - 1;

			loop200: for (int ir = j1; ir <= j2; ir++) {
				jrow = G.orderedTEF[ir];
				sum = 0.0d;
				if (jf1 == G.numMain)
					continue loop200;
				k1 = j1 + 1;
				loop100: for (int i = k1; i <= G.numTotal; i++) {
					irow = G.orderedTEF[i];
					if (!U.lSuper(jrow, irow))
						continue loop100;
					ndiv = 1;
					loop50: for (int k = 1; k < -G.numMain; k++) {
						if (G.ID[jrow][k] > 0)
							continue loop50;
						if (G.ID[irow][k] > 0)
							ndiv = ndiv * G.mainUniverse[k];
					} // end for k loop50
					if (ndiv == 0)
						continue loop100;
					term = G.varMod[irow] / (double) ndiv;
					sum = sum + term;
				} // end for i loop100
				G.varComp[jrow] = G.varMod[jrow] = sum;
			} // end for ir loop200

		} // end for jfac
		return;

	} // end modComp
	// ============== modComp ============
	// end

	// ============== checkForComplete ============
	// start
	private static void checkForComplete() throws IOException {
		double xdiff = 1.0d * Math.pow(10, -6);
		boolean missing = false;
		for (int k = G.numMain + 1; k <= G.numTotal; k++) {
			if (G.xBarb[k] <= xdiff) {
				Sys.setWarning(" VCOMP card missing for " + G.alphaID[k]);
				missing = true;
			} // end if
		} // end for
		if (missing)
			Sys.setErrorMsg("  Processing will cease");
	}

	// ============== checkForComplete ============
	// end
	// ============== checkAllFixed ============
	// start
	private static void checkAllFixed() throws IOException {
		int j1 = 0;
		loop10: for (int k = 1; k <= G.numMain; k++) {
			if (G.mainUniverse[k] == 0)
				continue loop10;
			if (G.mainLevel[k] == G.mainUniverse[k])
				j1++;
		}
		if (j1 == G.numMain)
			Sys.setErrorMsg(" All facets are fixed - " + "processing ceases");

	} // end checkAllFixed
	// ============== checkAllFixed ============
	// end
	// ============== checkGrand ============
	// start

	private static void checkGrand(String[] tokens, int first) 
			throws IOException {
		String message = " GrandMean parameter on an " +
			"ENDCOMP  card not a numeric value";
		if (!U.ifSlash(tokens[first]))
			Sys.setErrorMsg(" first of 2 parameters " + "not = '/'");
		else
			// here should be a grand Mean
			G.grandMean = U.toDouble(message, tokens[first + 1]);

	} // end checkGrand
	// ============== checkGrand ============
	// end
	// ============== checkDefault ============
	// start

	private static void checkDefault(String inString) throws IOException {

		if (U.isDefault(inString))
			G.irStar = 1;
		else
			Sys.setErrorMsg(" Single parameter on" + " an ENDCOMP card not 'DEFAULT'");
	} // end checkDefault
	// ============== checkDefault ============
	// end
	// ================== printComp ==============
	// start

	private static void printComp() throws IOException {

		int[] nrt = new int[64];
		PR.setTempHead("Data Input for GCOMPONENTS " + "run -- and Standard Errors");
		for (int j = 1; j <= G.numTotal; j++) {
			nrt[j] = G.orderedTEF[j];
		} // end for j
		prHead();

		// BIG LOOP FOR EACH FACET
		int j1, j2, k1, un1;
		boolean anyDiff;
		double xval;
		String stvar, stuni, sterr, stlev, stTot;

		StringBuilder sdet = new StringBuilder();

		for (int j = 1; j <= G.numNested; j++) {
			j1 = G.firstRow[j];
			j2 = G.firstRow[j + 1] - 1;
			if ((j2 - j1 + 5 + PR.getNumLines()) > PR.getLineLimit()) {

				IO.doPrintLine(sd.toString());
				IO.doPrintLine(sa.toString());
				IO.doPrintLine(sb.toString());
				IO.doPrintLine(sc.toString());
				IO.doPrintLine(sd.toString());

			} // end if
			/* START LOOP FOR EACH FACET SET */

			for (int k = j1; k <= j2; k++) {

				sdet.setLength(0);
				sdet.append(Out.blank(25));
				sdet.append(G.alphaID[k]).append("   ");
				sdet.append(Out.intx(9, G.numDegFreedom[k]));
				stvar = "   " + Out.dx(13, 7, G.varMod[k]);

				sterr = "               ";
				stlev = "            ";
				anyDiff = false;
				// C CHECK FOR S.E. PRINTING

				loop15: for (int p1 = 1; p1 <= G.numMain; p1++) {
					if (G.ID[k][p1] == 0)
						continue loop15;
					if (G.mainLevel[p1] != G.mainUniverse[p1]) {
						anyDiff = true;
						break loop15;
					} // end if
				} // end for p1
				if (anyDiff) {
					k1 = nrt[k];
					xval = U.valMat(Sys.offsetSE, k1, k1);
					xval = Math.sqrt(xval);
					sterr = "   " + Out.dx(12, 7, xval);
				} // end if anyDiff
				if (j == 1) { // sample size and universe
								// only for main effects
					stlev = Out.intx(12, G.mainLevel[k]);
					un1 = G.mainUniverse[k];
					if (un1 == 0)
						stuni = Out.blank(10) + "****";
					else
						stuni = "      " + Out.intx(8, un1);
					sdet.append(stlev).append(stuni);
				} // end if j == 1
				else
					sdet.append(Out.blank(26));

				sdet.append(stvar).append(sterr);
				IO.doPrintLine(sdet.toString());
				IO.mybw.flush();
				sdet.setLength(0);
			} // end for k loop 30

			if (j != G.numNested)
				IO.doPrintLine(sd.toString());

		} // end for j loop50
		IO.doPrintLine(sd.toString());
		stTot = Out.blank(25) + "Total";
		stTot = stTot + "        " + Out.intx(10, G.totalDegFreedom);
		IO.doPrintLine(stTot);
		IO.doPrintLine(sd.toString());

	} // end printComp

	// ================== printComp ==============
	// end
	// ================== prHead ==============
	// start
	private static void prHead() {
		// set up headings public in class
		// String divider = Out.blank(24) +
		// Out.fill(84, "-");
		sa = new StringBuilder();
		sb = new StringBuilder();
		sc = new StringBuilder();
		sd = new StringBuilder();
		sd.append(Out.blank(24));
		sd.append(Out.fill(84, "_"));

		sa.append(Out.blank(39)).append("Degrees");
		sa.append(Out.blank(20));
		sa.append("M  o  d  e  l");
		sb.append(Out.blank(43)).append("of          ");
		sb.append("Sample       ");
		sb.append("Universe     ");
		sb.append("Variance      Standard");
		sc.append(Out.blank(25)).append("Effect");
		sc.append("          Freedom");
		sc.append("        Size");
		sc.append("          Size");
		sc.append("      Components");
		sc.append("       Error");
	} // end prHead
	// ================== prHead ==============
	// end
	
} // end class Comp

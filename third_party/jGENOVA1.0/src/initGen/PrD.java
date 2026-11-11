package initGen;

import java.io.IOException;

public class PrD {

	// will provide the major output routines
	// for D Study results

	private static String qfm1;
	private static String qfm2;

	// ======================= dPrintSummary =======
	// start

	public static void dPrintSummary() throws IOException {

		PR.clearTempHead();
		StringBuilder sa = new StringBuilder();
		double temp;
		for (int i = 1; i <= D.maxDstudy; i++) {
			D.numDstudy = i;
			if (PR.getNumLines() >= PR.getLineLimit())
				printSummaryHeads();
			sa.append(setD3and3(D.seqDstudy, D.numDstudy));
			sa.append("        ");
			sa.append(setSamp(3));
			temp = D.dSummary[D.numDstudy][1];
			sa.append(" " + Out.dx(12, 5, temp));
			temp = D.dSummary[D.numDstudy][2];
			sa.append(" " + Out.dx(12, 5, temp));
			for (int j = 3; j <= 7; j++) {
				temp = D.dSummary[D.numDstudy][j];
				sa.append(" " + Out.dx(9, 5, temp));
			} // end for j
			IO.doPrintLine(sa.toString());
			sa.setLength(0);
		} // end for i
		IO.mybw.flush();
		PR.setNumLines(100);

	} // end dPrintSummary
	// ======================= dPrintSummary =======
	// end
	// ======================= setSamp =======
	// start

	private static String setSamp(int type) {

		StringBuilder sa = new StringBuilder();
		int value = (6 - G.numMain) * 6;
		String leftOver = Out.blank(value);

		switch (type) {
		case 1:
			for (int i = 1; i <= G.numMain; i++) {
				if (D.starID == i)
					sa.append("    $");
				else
					sa.append("     ");
				sa.append(G.mefChar[i]);
			} // end for
			break;
		case 2:
			for (int i = 1; i <= G.numMain; i++) {
				value = D.dUniverse[i];
				if (value == 0)
					sa.append("  inf.");
				else {
					if (value > 9999)
						sa.append("  ****");
					else
						sa.append(Out.intx(6, value));
				} // end else
			} // end for
			break;
		case 3:
			for (int i = 1; i <= G.numMain; i++) {
				value = D.sampID[i][D.numDstudy];
				if (value == 0)
					sa.append(" inf. ");
				else
					sa.append(Out.intx(6, value));
			} // end for
			break;

		} // end switch type
		if (G.numMain < 6)
			sa.append(leftOver);
		return sa.toString();
	} // end setSamp

	// ======================= setSamp =======
	// end
	// ==================== printSummaryHeads =======
	// start

	private static void printSummaryHeads() throws IOException {

		StringBuilder sa = new StringBuilder();
		String str = "Summary of D Study Results" + " for set of Control Cards no. ";
		sa.append(Out.blank(30));
		sa.append(str);
		sa.append(Out.int3(D.seqDstudy));
		IO.doPrintLine(sa.toString(), 3);
		sa.setLength(0);

		IO.doPrintLine(D.divider);

		sa.append(Out.blank(74));
		sa.append("V a r i a n c e s");
		IO.doPrintLine(sa.toString());
		sa.setLength(0);
		;

		sa.append(Out.blank(28));
		sa.append("Sample sizes");
		sa.append(Out.blank(15));
		sa.append(Out.fill(56, "_"));
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		sa.append(" D Study        ");
		sa.append(Out.fill(37, "_"));
		sa.append(Out.blank(17));
		sa.append("Expected");
		sa.append("     Lower");
		sa.append("     Upper");
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		sa.append("  Design  Index=");
		sa.append(setSamp(1));
		sa.append("     Universe");
		sa.append("     Observed");
		sa.append("      Case");
		sa.append("      Case");
		sa.append(Out.blank(15));
		sa.append("Gen.");
		IO.doPrintLine(sa.toString());
		sa.setLength(0);

		sa.append("   numb   Univ.=");
		sa.append(setSamp(2));
		sa.append("       Score");
		sa.append("        Score");
		sa.append("      Delta");
		sa.append("     Delta");
		sa.append("     Mean");
		sa.append("      Coef.");
		sa.append("      Phi");
		IO.doPrintLine(sa.toString());
		IO.doPrintLine(D.divider);
		sa.setLength(0);
		;

	} // end printSummaryHeads
	// ==================== printSummaryHeads =======
	// end
	// ======================= dPrint1 =======
	// start

	public static void dPrint1() throws IOException {

		loop50: for (int m = 1; m <= G.numTotal; m++) {
			dPrintLine(m);
			if (PR.getNumLines() < Sys.getLinesPerPage())
				continue loop50;
			if ((m + 1) == Sys.getLinesPerPage())
				continue loop50;
			PR.setNumLines(100);
			PrD.dsHead(1);
		} // end for m
		IO.doPrintLine(D.divider);
		IO.doPrintLine(D.d[1][9]);
		
		IO.mybw.flush();

	} // end dPrint1
	// ======================= dPrint1 =======
	// end
	// ======================= dsHead =======
	// start

	public static void dsHead(int type) throws IOException {
		// type= 0 sill be a full page
		// type != 0 will be a continuation
		// if D.dhtemp[1] has length < 5 ==>
		// must recalculate the top 4
		// headlines for the primary D Study
		// report using the correct sample
		// and population sizes
		PR.setNumLines(100);
		if (type != 0) {
			printContinued();
			printDetail();
			return;
		} else {
			setTempHead();
			printTopFour();
			printDetail();
			return;
		}
	} // end PrD.dsHead
	// ======================= dsHead =======
	// end
	// ======================= setQFM =======
	// start

	private static void setQFM(int kw) {
		qfm1 = "QFM";
		qfm2 = qfm1;
		int mg = 1;
		int md = 1;
		loop120: for (int i = 1; i <= G.numMain; i++) {
			if (G.ID[kw][i] == 0)
				continue loop120;
			if (G.mainUniverse[i] == 0)
				mg = 0;
			else {
				if (G.mainLevel[i] < G.mainUniverse[i])
					mg = 0;
			} // end else
			if (D.dUniverse[i] == 0)
				md = 0;
			else {
				if (D.sampID[i][D.numDstudy] < D.dUniverse[i])
					md = 0;
			} // end else
		} // end for i
		if (mg == 0)
			
		qfm1 = "   ";
		if (md == 0)
			qfm2 = "   ";

	} // end setQFM
	// ======================= setQFM =======
	// end
	// ======================= dPrintLine =======
	// start

	private static void dPrintLine(int kw) throws IOException {

		setQFM(kw);

		StringBuilder sa = new StringBuilder();
		sa.append(" " + G.alphaID[kw]);
		// 23
		sa.append("  " + Out.dx(9, 5, D.varDGT[kw]));
		sa.append(qfm1);
		// 34
		sa.append(Out.dx(8, 4, D.e11[kw]));

		String correct = "   " + Out.intx(6, (int) D.dp12[kw]);
		String minus = "     -------";
		// 73
		sa.append(correct);
		if (D.e11[kw] > Math.pow(10, -6)) {
			// 55
			sa.append("   " + Out.dx(9, 5, D.v13[kw]));
			// 69
			sa.append("     " + Out.dx(9, 5, D.se14[kw]));
		} else {
			sa.append(minus);
			sa.append("  " + minus);
		} // end if else e11
		// 82
		sa.append("    " + Out.dx(9, 5, D.v15[kw]));
		sa.append(qfm2);
		// 93
		sa.append(Out.dx(8, 4, D.ep16[kw]));
		// 102
		sa.append(correct);

		if (D.ep16[kw] > Math.pow(10, -6)) {
			// 114
			sa.append("  " + Out.dx(10, 5, D.v18[kw]));
			// 127
			sa.append("    " + Out.dx(10, 5, D.se19[kw]));
		} else {
			sa.append(minus);
			sa.append("  " + minus);
		} // end if else ep15
		IO.doPrintLine(sa.toString());

	} // end dPrintLine
	// ======================= dPrintLine =======
	// end
	// ======================= printTopFour =======
	// start

	private static void printTopFour() throws IOException {

		if (D.dhtemp[1].length() > 5) {

			for (int k = 1; k <= 4; k++) {
				IO.doPrintLine(D.dhtemp[k]);
			}
			return;
		} // end if D.dhtemp length
		// here have to build current headings
		StringBuilder sa = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();
		StringBuilder sd = new StringBuilder();
		String temp;
		loopNoStar: for (int k = 1; k <= G.numMain; k++) {
			if (D.starID == k)
				continue loopNoStar;
			sa.append(centerIn(G.alphaID[k].trim(), 12));
			sb.append(centerIn(setUniv(G.mainUniverse[k]), 12));
			sc.append(centerIn(setUniv(D.dUniverse[k]), 12));
			temp = Out.intx(12, D.sampID[k][D.numDstudy]);
			sd.append(centerIn(temp.trim(), 12));
		}
		// now do object of measurement
		StringBuilder sdet = new StringBuilder();
		sdet.append(D.dh[1][1]);
		temp = G.alphaID[D.starID].trim();
		sdet.append(centerIn(temp, 11));
		sdet.append(D.dh[1][2]);
		sdet.append(sa.toString());
		IO.doPrintLine(sdet.toString());

		sdet.setLength(0);
		sdet.append(D.dh[2][1]);
		temp = setUniv(G.mainUniverse[D.starID]);
		sdet.append(centerIn(temp, 11));
		sdet.append(D.dh[2][2]);
		sdet.append(sb.toString());
		IO.doPrintLine(sdet.toString());

		sdet.setLength(0);
		sdet.append(D.dh[3][1]);
		temp = setUniv(D.dUniverse[D.starID]);
		sdet.append(centerIn(temp, 11));
		sdet.append(D.dh[3][2]);
		sdet.append(sc.toString());
		IO.doPrintLine(sdet.toString());

		sdet.setLength(0);
		sdet.append(D.dh[4][1]);
		temp = Out.intx(11, D.sampID[D.starID][D.numDstudy]).trim();
		sdet.append(centerIn(temp, 11));
		sdet.append(D.dh[4][2]);
		sdet.append(sd.toString());
		IO.doPrintLine(sdet.toString());

	} // end printTopFour
	// ======================= printTopFour =======
	// end
	// ======================= setUniv =======
	// start

	private static String setUniv(int value) {
		if (value == 0)

			return "Infinite";
		else
			return Out.intx(10, value).trim();

	} // end setUniv

	// ======================= setUniv =======
	// end
	// ======================= setTempHead =======
	// start

	private static void setTempHead() throws IOException {
		if (Flags.defaultDstudy)
			PR.setTempHead("Default D-Study");
		else {
			String str = "D study Design Number " + setD3and3(D.seqDstudy, D.numDstudy);
			PR.setTempHead(str);
		}
	} // end setTempHead

	// ======================= setTempHead =======
	// end
	// ======================= printContinued =======
	// start

	private static void printContinued() throws IOException {

		String str = centerIn("'**  (continued)  **'",130);
		
		IO.doPrintLine(str);
		IO.doPrintLine(" ");

	} // end printContinued
	// ======================= printContinued =======
	// end
	// ======================= printDetail =======
	// start

	private static void printDetail() throws IOException {

		IO.doPrintLine(D.divider);
		for (int k = 1; k <= 8; k++) {
			IO.doPrintLine(D.d[1][k]);
		}
		IO.doPrintLine(D.divider);

	} // end printDetail
	// ======================= printDetail =======
	// end
	// ============== centerIn ============
	// start

	private static String centerIn(String sValue, int width) {
		String result = sValue.trim();
		int diff = (width - result.length()) / 2;
		result = Out.blank(diff) + result;
		result = result + Out.blank(width - result.length());
		return result;
	}
	// ============== centerIn ============
	// end
	// =========== setD3and3 =======
	// start

	private static String setD3and3(int left, int right) {

		String result = Out.int3(left) + "-" + Out.int3(right);
		return result;
	}
	// =========== setD3and3 =======
	// end


} // end class PrD

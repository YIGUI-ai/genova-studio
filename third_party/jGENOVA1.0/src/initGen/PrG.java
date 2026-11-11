package initGen;

import java.io.IOException;

public class PrG {

	// ========================== PrG.ems =================
	// start

	public static void ems() throws IOException {

		PR.setTempHead("Expected Mean Square Equations");
		PR.setNumLines(200);
		String a23 = Out.fill(20, " ") + "+  ";
		String str;
		String plus5 = "  +  ";

		int j1, col;
		double fval;
		boolean start;
		StringBuilder sb = new StringBuilder();

		for (int row = 1; row <= G.numTotal; row++) {
			j1 = G.orderedTEF[row];
			fval = U.valMat(0, row, row);
			sb.setLength(0);
			sb.append(emsStart(j1));
			start = true;
			col = G.numTotal + 1;
			loop10: for (int j2 = row; j2 <= G.numTotal; j2++) {
				col = col - 1;
				fval = U.valMat(0, row, col);
				if (fval <= 0.0d)
					continue loop10;
				str = emsOut(G.orderedTEF[col], fval);
				if (str.length() + sb.length() > 126) {
					// set up sprint and go here

					if (PR.getNumLines() + 2 > PR.getLineLimit())
						doemsPage();
					IO.doPrintLine(sb.toString(), 2);
					sb.setLength(0);
					sb.append(a23).append(str);
				} // end if str.length() ...
				else if (start) {
					sb.append(str);
					start = false;
				} else
					sb.append(plus5).append(str);
			} // end for j2 loop10

			if (sb.length() > 20) {
				// here there is something left to print

				if (PR.getNumLines() + 2 > PR.getLineLimit())
					doemsPage();
				IO.doPrintLine(sb.toString(), 2);

			} // end if

		} // end for row
		PR.clearTempHead();
		PR.setNumLines(200);
		IO.mybw.flush();

	} // end PrG.ems
	// ========================== PrG.ems =================
	// end
	// ========================== doemsPage =================
	// Start

	private static void doemsPage() throws IOException {
		PR.setNumLines(200);
		IO.doPrintLine(G.nhead1);
		IO.doPrintLine(G.nhead2);
		IO.doPrintLine(G.nhead3);
	}

	// ========================== doemsPage =================
	// end
	// ========================== emsOut =================
	// start
	private static String emsOut(int row, double fMult) {
		StringBuilder sb = new StringBuilder();
		sb.append(Out.dx(12, 2, fMult).trim());
		sb.append("*VC(");
		sb.append(G.alphaID[row].trim()).append(")");
		return sb.toString();
		
	}

	// ========================== emsOut =================
	// end
	// ========================== emsStart =================
	// start
	private static String emsStart(int row) {
		String str = " EMS(" + G.alphaID[row].trim() + ")";
		int len = str.length();
		str = str + Out.fill(18 - len, " ") + "=  ";
		return str;
	}

	// ========================== emsStart =================
	// end
	// ========================== PrG.vcov =================
	// start
	public static void vcov() throws IOException {
		printDiagonal(1);
		if (Flags.corrTable)
			printDiagonal(2);
		return;

	} // end PrG.vcov
	// ========================== PrG.vcov =================
	// end
	// ====================== PrG.printDiagonal ===========
	// start

	public static void printDiagonal(int type) throws IOException { // 1

		String str = "";
		if (type == 3) {
			str = " Variance - Covariance" + " Matrix for Estimate Variance " + "Components (fir Mean Scores) "
					+ "in Universe of Generalization";
			str = PR.center(str);
		} // end type == 3
		int[] ivec = new int[64];
		double[] xhold = new double[9];

		setDiagonalHead(type);
		ivec = getIvec(type);
		;
		int newTotal = G.newTotal;

		// done with preliminaries

		int nwFirst = 0;
		int nwLast = 0;

		String[] colHeads = new String[9];
		int numSets;
		numSets = (newTotal + 7) / 8;
		for (int k = 1; k <= numSets; k++) { // 2
			nwFirst = nwLast + 1;
			nwLast = nwFirst + 7;
			if (nwLast > newTotal)
				nwLast = newTotal;
			colHeads[k] = doColHead(nwFirst, nwLast, ivec);
		} // here all the column heading sets are set up // -2

		int[][] page = new int[3][3];
		int pagelimit = PR.getLineLimit() - 11;
		// allows for sub heading
		int jcol, jrow, jindex;

		StringBuilder detail = new StringBuilder();

		for (int set = 1; set <= numSets; set++) { // 3 set
			nwFirst = set * 8 - 7;
			nwLast = nwFirst + 7;
			if (nwLast > newTotal)
				nwLast = newTotal;
			page = setPages(set, pagelimit, newTotal);
			// for ready to do pages in the set
			for (int ipage = 1; ipage <= page[0][0]; ipage++) { // 4 page
				PR.setNumLines(200);
				if (type == 3)
					IO.doPrintLine(str);
				IO.doPrintLine(colHeads[set], 2);
				IO.mybw.newLine();
				for (int row = page[ipage][1]; row <= page[ipage][2]; row++) { // 5
																				// row
					jrow = ivec[row];
					jindex = 0;
					detail.setLength(0);
					detail.append(" " + G.alphaID[jrow] + "");

					// now fill up and print each row
					loop110: for (int col = nwFirst; col <= nwLast; col++) { // 6
																				// col
						if (col > row)
							break loop110;
						jindex++;
						jcol = ivec[col];
						xhold[jindex] = diagonalValue(jrow, jcol, type);
					} // end for col loop110
					for (int k = 1; k <= jindex; k++) { // 6
						detail.append(Out.dx(14, 7, xhold[k]));
					} // end for k // -6
					IO.doPrintLine(detail.toString());
				} // end for row // -5 row
			} // end for set // -4 page

		} // end for set // =3 set
		PR.clearTempHead();
		PR.setNumLines(100);
	} // end PrG.printDiagonal

	// ====================== PrG.printDiafonal ===========
	// end

	// ========================== setPages =================
	// start
	private static int[][] setPages(int set, int pagelimit, int newTotal) {

		// page [..][1] = first row
		// page [..][2] = last row
		// page [2][..] = page 1 set
		// page [2][..] = page 2 set
		// [age[0][0] = num [ages per set
		int[][] page = new int[3][3];
		int nwFirst, nwLast;
		nwFirst = set * 8 - 7;
		nwLast = nwFirst + 7;
		if (nwLast > newTotal)
			nwLast = newTotal;

		if ((newTotal - nwFirst - 1) > pagelimit) {
			page[0][0] = 2;
			page[1][1] = nwFirst;
			page[1][2] = nwFirst + pagelimit - 1;
			page[2][1] = nwFirst + pagelimit;
			page[2][2] = newTotal;
		} else {
			page[0][0] = 1;
			page[1][1] = nwFirst;
			page[1][2] = newTotal;
		}
		return page;
	}

	// ========================== setPages =================
	// end
	// ========================== doColHead =================
	// start
	private static String doColHead(int first, int last, int[] ivec) {

		StringBuilder colHead = new StringBuilder();
		int j2;
		colHead.append(Out.fill(18, " "));
		for (int j = first; j <= last; j++) {
			j2 = ivec[j];
			colHead.append(G.alphaID[G.orderedTEF[j2]]);
			colHead.append("   ");
		}
		return colHead.toString();
	}
	// ========================== doColHead =================
	// end
	// =========== getIvec =======
	// start

	private static int[] getIvec(int type) {

		int[] ivec = new int[64];
		int newTotal = 0;
		int j1;
		if (type == 3) {
			loop5:

			for (int j = 1; j <= G.numTotal; j++) {
				j1 = G.orderedTEF[j];
				loop2: for (int j2 = 1; j2 <= G.numMain; j2++) {
					if (G.ID[j1][j2] != 1)
						continue loop2;
					if (D.sampID[j2][D.numDstudy] == D.dUniverse[j2])
						continue loop5;
				} // end for loop2
				// No Primary Effects are Fixed

				newTotal++;
				ivec[newTotal] = j;
			} // end for loop5
			G.newTotal = newTotal;
			return ivec;
		} // end type == 3
		else {
			boolean allFixed;
			loop30: for (int j = 1; j <= G.numTotal; j++) {
				allFixed = true;
				j1 = G.orderedTEF[j];
				loop10: for (int j2 = 1; j2 <= G.numMain; j2++) {
					if (G.ID[j1][j2] == 0)
						continue loop10;
					if (G.mainLevel[j2] != G.mainUniverse[j2]) {
						allFixed = false;
						break loop10;
					} // end if
				} // end for loop10
				if (allFixed)
					continue loop30;
				newTotal++;
				ivec[newTotal] = j;
			} // end for loop30
			G.newTotal = newTotal;
			return ivec;

		} // end type = 1 or 2
	} // end getIvec
	// =========== getIvec =======
	// end
	// =========== diagonalValue =======
	// start

	private static double diagonalValue(int jrow, int jcol, int type) throws IOException {
		// will return a value for a diagonal
		// matrix printout for:
		// type = 1 G study variance covariance
		// for Estimated Variance
		// components
		// type = 2 G Study Correlation Matrix for
		// Estimated Variance Components
		// type = 3 D Study Variance = Covariance
		// matrix for Estimate Variance
		// Components (fir Mean Scores)
		// in Universe of Generalization
		Double result = 1.0d;
		double xdiv;
		double xdiff = 1.0d * Math.pow(10, -8);

		switch (type) {
		case 1:
			result = U.valMat(Sys.offsetSE, jrow, jcol);
			break;
		case 2:
			if (jrow != jcol) {
				// when == results will be 1.0d
				result = U.valMat(Sys.offsetSE, jrow, jcol);
				xdiv = U.valMat(Sys.offsetSE, jrow, jrow) * U.valMat(Sys.offsetSE, jcol, jcol);
				xdiv = Math.sqrt(xdiv);
				if (xdiv <= xdiff) {
					return -999999.99999999;
				} // end if
				result = result / xdiv;
			} // end else
			break;
		case 3:
			result = U.valMat(D.offsetDN, jrow, jcol);
			break;
			
		} // end switch type
		return result;

	} // end diagonalValue
	// =========== diagonalValue =======
	// end
	// =========== setDiagonalHead =======
	// start

	private static void setDiagonalHead(int type) throws IOException {

		switch (type) {
		case 1:
			PR.setTempHead("Variance - Covariance" + " Matrix for Estimated Variance" + " Components (V)");
			return;
		case 2:
			PR.setTempHead("Correlation Matrix for " + "Estimated Variance Components");
			return;
		case 3:
			return;

		} // end switch type

	} // end setDiagonalHead
	// =========== setDiagonalHead =======
	// end

}	// end Class PrG

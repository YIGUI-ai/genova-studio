package initGen;

import java.io.IOException;

public class PrintID {

	// =================== idHeadings =======
	// start

	private static void idHeadings() throws IOException {

		PR.addLine(1);

		StringBuilder sa = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();

		String a9 = "         ";

		sa.append(a9).append("(** = Infinite)");
		sb.append(a9).append("Sample Size");
		sc.append(a9).append("Universe Size");

		sa.append(String.format("%5s", G.mefChar[1]));
		sb.append(String.format("%9d", G.mainLevel[1]));
		if (G.mainUniverse[1] == 0) {
			sc.append(String.format("%7s", " ****"));
		} // end if
		else {
			sc.append(String.format("%7d", G.mainUniverse[1]));
		} // end else

		for (int i = 2; i <= G.numMain; i++) {
			sa.append(String.format("%10s", G.mefChar[i]));
			sb.append(String.format("%10d", G.mainLevel[i]));
			if (G.mainUniverse[i] == 0) {
				sc.append(String.format("%10s", " ****"));
			} // end if
			else {
				sc.append(String.format("%10d", G.mainUniverse[i]));
			} // end else

		} // end for i
		/* Store away the three headlines for many output tables */
		G.nhead1 = sa.toString();
		G.nhead2 = sb.toString();
		G.nhead3 = sc.toString();

		sa.append(String.format("%33s", "Total   Degrees"));
		sb.append(String.format("%30s", "Primary  Number     of"));
		sc.append(String.format("%33s", "Indices  Indices  Freedom"));
		IO.doPrintLine(" ");

		IO.doPrintLine(sa.toString());
		IO.doPrintLine(sb.toString());
		IO.doPrintLine(sc.toString());
		
		PR.setNumLines(9);

	} // end of idHeadings

	// =================== idHeadings =======
	// end
	// =================== getTop =======
	// start

	private static String getTop() {

		StringBuilder top = new StringBuilder();
		String a8 = "        ";

		String x12 = "__________";
		top.append(a8).append(" ____");

		for (int i = 1; i <= G.numMain + 1; i++) {
			top.append(x12);
		}
		return top.toString();

	} // end of getTop*
	// ================== getTop =======
	// end
	// ================== getBottom =======
	// start

	private static String getBottom() {

		StringBuilder bot = new StringBuilder();
		String a8 = "        ";

		String x12 = "_________|";
		bot.append(a8).append("|_____");

		for (int i = 1; i <= G.numMain + 1; i++) {
			bot.append(x12);
		}
		return bot.toString();

	} // end getBottom
	// ================== getBottom =======
	// end
	// ================== getSpacer =======
	// start

	private static String getSpacer() {

		String a8 = "        ";

		StringBuilder spacer = new StringBuilder();
		spacer.append(a8).append("|     ");
		for (int i = 1; i <= G.numMain + 1; i++) {
			spacer.append(a8).append(" |");
		} // end for i
		return spacer.toString();

	}// end of gwetSpacer
	// ================== getSpacer =======
	// end
	// ================== checkLines =======
	// start

	private static boolean checkLines(int lineEst) throws IOException {
		if (lineEst > PR.getLineLimit()) {
			PR.setNumLines(200);
			idHeadings();
			return true;
		} // end if
		return false;
	} // end of checkLines
	// ================== checkLines =======
	// end
	// ================== idPrintDetail =======
	// start

	private static void idPrintDetail(int jp) throws IOException {
		String bar6 = "    |";

		StringBuilder sbLine = new StringBuilder();
		int[] idHold = new int[7];
		idHold = getRowID(jp);
//		sbLine.append("        ").append("|    ");
		sbLine.append("        ").append("|  ");
		sbLine.append(G.alphaID[jp]);
		sbLine.append(" |");
		for (int r = 1; r <= G.numMain; r++) { // 4
			sbLine.append(String.format("%5d%5s", idHold[r], bar6));
		} // end for r
		sbLine.append(String.format("%7d%9d%11d", G.indexMain[jp], G.indexTotal[jp], G.numDegFreedom[jp]));
		IO.doPrintLine(sbLine.toString());

		
	} // end idPrintDetail
	// ================== idPrintDetail =======
	// end
	// ================== printID =======
	// start

	public static void printID() throws IOException { // 1

		PR.setNumLines(200);
		for (int k = 1; k <= G.numTotal; k++) {
			G.alphaID[k] = U.effectOut(k);
		}

		PR.setTempHead("Expanded Main and Interaction Effect Table");

		// =============== dsone local storage set up ========

		int lastMain = 0;
		int[] jHold = { 0, 0, 0, 0, 0, 0 };
		int jtemp = 0;

		String top = getTop();
		String spacer = getSpacer();
		String bottom = getBottom();

		idHeadings();
		IO.doPrintLine(top);
		IO.doPrintLine(spacer);
		for (int i = 1; i <= G.numTotal; i++) {

			// adjust the index
			jtemp = G.indexMain[i];
			jHold[jtemp - 1]++;
		} // end for i
		for (int jp = 1; jp <= G.numTotal; jp++) {
			if (lastMain != G.indexMain[jp]) {
				lastMain = G.indexMain[jp];
				if (lastMain != 1) {
					IO.doPrintLine(bottom);
					jtemp = PR.getNumLines() + jHold[lastMain-1]+5;
					if (checkLines(jtemp)) {
						IO.doPrintLine(top);
						IO.doPrintLine(spacer);
					} 
					else
						IO.doPrintLine(spacer);
					// end if checkLines
				}  // end if lastMain
			
			} //// end of if lastMain
PrintID.
			idPrintDetail(jp);

		} // end of for jp
		IO.doPrintLine(bottom);
		IO.mybw.flush();
		PR.clearTempHead();
		PR.setNumLines(200);

	} // end of printID
	// ================== printID =======
	// end
	// =========== getRowID =======
	// start

	private static int[] getRowID(int row) {
		int[] rowOut = new int[7];
		for (int j = 1; j <= 6; j++) {
			rowOut[j] = G.ID[row][j];
		}
		return rowOut;
	}
	// =========== getRowID =======
	// end

}	// end Class PrintID

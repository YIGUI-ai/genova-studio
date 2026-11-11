package initGen;

import java.io.IOException;

public class Gphase1 {

	private static int ir;

	private static int kIndex;
	private static int kLevel;
	private static int kDim;
	private static boolean firstElement;

	/*
	 * the following routines are only used in GPJas1 SUMIND
	 * 
	 * JINDEX
	 * 
	 */
	// ========================== Gphase1.go ==============
	// start

	public static void go() throws IOException {

		int first, last, jtemp;
		double xval;

		IO.mybw.flush();
		if (G.passNumber == 0) { // 1
			if (Flags.printRecords)
				printRec(-1);
			first = G.irStar + 1;
			last = G.numMain;
		} // end if G/passNumber = 0 // -1
		else { // 1
			if (G.nPass1 == 0)
				return;
			first = 1;
			last = G.numMain - 1;
		} // end else G.passNumber = 1 // -1

		// ..================ D0 500
		for (int irec = 1; irec <= G.numRecs; irec++) { // 1

			if (G.passNumber == 0) { // 2
				// read in physical record only on first pass (=0)

				Read.record();
				printRec(irec);

			} // end if G.passNumber = 0 // -2
				// ..=================== read in data in G.passNumber = 0

			// now start big loop for each value in the
			// current record
			// loop300:
			for (int j1 = 1; j1 <= G.inRecord; j1++) { // 2

				xval = G.xbuf[j1];

				// now start a loop for each effect being
				// processed on this pass
				// ==================== 200

				do200Loop(first, last, xval);

				if (G.passNumber == 0) { // 3
					G.xMean = G.xMean + xval;
					jtemp = jIndex(G.nzDim, G.nzIndex, G.numMain - 1, G.nzOffset);

					if (Flags.debug)
						Bug.bug01gphase1debug200(jtemp);

					G.xbuf[jtemp] = G.xbuf[jtemp] + xval;
				} // end if G.passNumber = 0 // -3
					// 250

				if (G.passNumber == 0)
					updateIndex(G.mainLevel);
				else
					updateIndex(G.nzDim);

				if (Flags.debug)
					Bug.bug02gphase1debug250();

			} // end of j1 to inRecord 300 loop // --2

			/* CLEAN UP SUM AREAS FOR THIS RECORD */

			if (G.nSum1 != 0)
				do400Loop();

			/* END OF RECORD LOOP */

			if (Flags.debug)
				Bug.bug04gphas1490(irec);

		} // end irec numREcs loop500 // -1

		PR.clearTempHead();
		if (G.passNumber != 1)
			finishGphase1();
		return;
	} // end gphase11.go
	// ========================== Gphase1.go ==============
	// end
	// ==================== finishGphase1.go ==============
	// start
	
	private static void finishGphase1()  throws IOException {

		// NOW SET UP Z ARRAY BY DIVIDING BY MEFLEV(IRSTAR) &
		// MOVE IT TO FRONT OF XBUF

		int ndiv, j1, j2,jpos;
		
		ndiv = G.mainLevel[G.irStar];
		j1 = G.nzOffset + 1;
		j2 = G.nzOffset + (G.dataPoints / ndiv);
		jpos = 0;
		
		double xdiv = (double) ndiv;
		
		for (int j = j1; j <= j2; j++) {
			jpos++;
			G.xbuf[jpos] = G.xbuf[j] / xdiv;
		} // end for j
		G.nxtOff = jpos;

		// INISH CALCULATING XMEAN AND XGRAND

		G.grandMean = G.xMean / ((double) G.dataPoints);
		G.xMean = G.grandMean * G.xMean;
		if (Flags.anyMean)
			prMean(0, 0);
		if (Flags.debug)
			Bug.bug03gphase1Final();
		
	}	// end finishGphase1
	// ==================== finishGphase1.go ==============
	// end
		// ========================== do200Loop ==============
		// start

	private static void do200Loop(int first, int last, double xval) {
		// ==================== 200 lop start

		int nj;
		int subscr;
		double zhold;
		loop200: for (int j = 1; j <= G.nPass1; j++) { // 1

			nj = G.nOffset[j];
			if (!G.lArray[j]) { // 2
				G.xbuf[nj] = G.xbuf[nj] + xval;
				if (noAdd(j, first, last))
					continue loop200;

				ir = G.iPass1[j];
				zhold = G.xbuf[nj] / G.np1Cell[j];
				G.xBarb[ir] = G.xBarb[ir] + zhold * zhold;
				G.xbuf[nj] = 0.0;
				continue loop200;
			} // end if not kArray -2

			/*
			 * ADD TO A SUM AREA HERE wow = KEY TO TWO-PASS PROCESSING
			 */

			if (G.passNumber == 0)
				subscr = sumIndex(j, first, last, G.mainLevel);
			else
				subscr = sumIndex(j, first, last, G.nzDim);

			G.xbuf[subscr] = G.xbuf[subscr] + xval;
		} // end for j end 200 // -1

	} // end do200Loop
		// ========================== do200Loop ==============
		// end
		// ========================== do400Loop ==============
		// start

	private static void do400Loop() throws IOException {

		int jtemp, ndiv, ntemp;
		double xdiv,  xtemp, zhold;

		loop400: for (int j1 = 1; j1 <= G.nPass1; j1++) { // 1
			if (!G.lArray[j1])
				continue loop400;

			// found a Sum Area

			ir = G.iPass1[j1];
			ndiv = G.np1Cell[j1];
			ntemp = G.inRecord / ndiv;

			/* CHECK FOR MEAN PRINTING ON SECOND PASS */

			if ((G.passNumber == 1) && Flags.anyMean)
				prMean(j1, ntemp);

			xdiv = (double) ndiv;
			jtemp = G.nOffset[j1];
			xtemp = 0.0;

			for (int j = 1; j <= ntemp; j++) { // do 350 2
				jtemp++;
				zhold = G.xbuf[jtemp] / xdiv;
				xtemp = xtemp + zhold * zhold;
				G.xbuf[jtemp] = 0.0;
			} // end for j 350// -2

			G.xBarb[ir] = G.xBarb[ir] + xtemp;

		} // end for j1 loop 400 // -1

	} // end do 400Loop
		// ========================== do400Loop ==============
		// end
		// ========================== prMean ==============
		// start

	private static void prMean(int row, int ntemp) throws IOException {
		String tempHold, str, hold;
		int npline;
		int jmod = 4;
		String aLine = " ";
		String starLine = " " + Out.fill(128, "_");

		if (row == 0) {
			PR.setTempHead("CELL MEAN SCORES");
			PR.setNumLines(200);
			IO.doPrintLine(starLine);
			PR.lineSkip(1);
			str = Out.fill(45, " ") + "*** GRAND MEAN = " + Out.dx(20, 7, G.grandMean) + " ***";
			IO.doPrintLine(str);
// 2.0			PR.lineSkip(1);
			return;
		}

		// regular processing here

		StringBuilder sb = new StringBuilder();
		sb.append("(");
		int ir = G.iPass1[row];

		for (int j = 1; j <= G.numMain; j++) {
			if (G.ID[ir][j] > 0) {
				sb.append(G.mefChar[j] + ",");
			}
		} // end for

		str = sb.toString();
		hold = str.substring(0, str.length() - 1) + ")"; // f to 1

		// the string is built

		npIndex(row, 1);
		double xdiv = kDim;
		int kper = G.nzDim[kLevel];
		/*
		 * Set up printing Mean scores
		 */
		npline = (kper + 3) / jmod;
// 2.0		PR.addLine(npline + 4);
		npline = npline + PR.getNumLines() + 6;
		
		if (npline>= PR.getLineLimit()) {
			PR.setNumLines(200);
		};
		IO.doPrintLine(starLine);
		PR.lineSkip(1);
		str = "   MEAN SCORES FOR EFFECT:  " + G.alphaID[ir] + "    SUBSCRIPT NOTATION:  " + hold;
/* 2,0		IO.doPrintLine(str, 2);*/
		IO.doPrintLine(str, 1);
		PR.lineSkip(1);
		/* START BIG LOOP FOR EACH XBUF VALUE */

		double absxm;
		for (int j = 1; j <= ntemp; j++) { // do 210

			absxm = Math.abs(G.xbuf[kIndex] / xdiv);

			if (aLine.length() > 115) {
				IO.doPrintLine(aLine);
				aLine = " ";
			}
// 2.0		tempHold = assemble(G.nzIndex, kLevel, 12, 18, 5, absxm);
			tempHold = assemble(G.nzIndex, kLevel, 12, 18, 6, absxm);

			if (firstElement && aLine.length() > 10) {
				IO.doPrintLine(aLine);
				aLine = " " + tempHold;
			} else {
				aLine = aLine + tempHold;
			}

			if (j != ntemp)
				npIndex(row, j + 1);
		} // end for j
		if (aLine.length() > 1)
			IO.doPrintLine(aLine);

	}
	// ========================== prMean ==============
	// end
	// ========================== assemble ==============
	// start

	private static String assemble(int[] index, int level, int howlong, int wide, int dec, double xval) {
		String str = subscr(index, level, 11) + " = " + Out.dx(wide, dec, xval);
		return str;
	}
	// ========================== assemble ==============
	// end
	// ========================== subscr ==============
	// start

	private static String subscr(int[] index, int level, int howlong) {

		StringBuilder sb = new StringBuilder();
		String str;
		sb.append(Out.fill(howlong, " ") + "(");
		for (int s = 1; s <= level; s++) {
			sb.append(index[s] + ","); // $ -1 to 0
		}

		str = sb.toString();

		str = str.substring(0, str.length() - 1) + ")";
		str = str.substring(str.length() - howlong);
		if (str.substring(str.length() - 3).equals(",1)"))
			firstElement = true;
		else
			firstElement = false;
		return str;

	}
	// ========================== subscr ==============
	// end
	// ========================== npIndex ==============
	// start

	private static void npIndex(int row, int kpos) {

		/*
		 * npIndex will set the following values to be used by the method
		 * = pfMean
		 * 
		 * kIndex = THE POSITION IN XBUF WHERE THE CURRENT SUM WILL BE FOUND =
		 * NOFF(IROW) + KPOS.
		 * 
		 * kLevel = THE NUMBER OF NON-ZERO ELEMENTS OF THE IROW'TH ROW OF NPID
		 * == THE NUMBER OF DIMENSIONS FOR THIS SUM AREA.
		 * 
		 * kDim = THE DIVISOR FOR EACH SUM AREA TO CONVERT THE VALUE TO A MEAN
		 * SCORE.
		 * 
		 */
		if (kpos <= 1) { // 1
			int ival;
			kLevel = 0;
			kDim = G.dataPoints / G.mainLevel[G.irStar];
			int iir = G.iPass1[row];
			loop10: for (int j = 1; j <= G.numMain; j++) { // 2
				if (j == G.irStar)
					continue loop10;
				if (G.ID[iir][j] == 0)
					continue loop10;
				kLevel++;
				ival = G.mainLevel[j];
				G.nzIndex[kLevel] = 1;
				G.nzDim[kLevel] = ival;
				kDim = kDim / ival;

			} // end for j ;pp[10 // -2

			kIndex = G.nOffset[row] + 1;
			return;
		} // end kpos <= 1 // -1

		// here not an initialization pass

		kIndex++;
		int j = kLevel;
		boolean first = true;
		while (first || j > 0) { // 1
			first = false;
			if (G.nzIndex[j] != G.nzDim[j]) { // 2
				G.nzIndex[j]++;
				return;
			} // end if nzIndex... // -2

			G.nzIndex[j] = 1;
			j--;

		} // end while j > 0 // -1

		return;

	}
	// ========================== npIndex ==============
	// end
	// ========================== noAdd ==============
	// start

	private static boolean noAdd(int row, int first, int last) {
		if (first > last)
			return false;

		int j1 = 0;
		loop10: for (int j = first; j <= last; j++) {
			j1++;
			if (G.passID[row][j1] == 0)
				continue loop10;
			if (G.passID[row][j1] != G.nIndex[j])
				return true;
		} // end for k loop10
		return false;

	} // end noAdd
		// ========================== noAdd ==============
		// end
		// ========================== sumIndex ==============
		// start

	private static int sumIndex(int row, int first, int last, int[] myLevel) {

		int[] nd = { 0, 0, 0, 0, 0, 0, 0 };
		int[] ns = { 0, 0, 0, 0, 0, 0, 0 };
		int nSub = 0;
		int j1 = 0;
		loop10: for (int k = first; k <= last; k++) {
			j1++;
			if (G.passID[row][j1] != 0)
				continue loop10;
			nSub++;
			nd[nSub] = myLevel[k];
			ns[nSub] = G.nIndex[k];
		} // end for k loop10

		return jIndex(nd, ns, nSub, G.nOffset[row]);
	}
	// ========================== sumIndex ==============
	// end
	// ========================== updateIndex ==============
	// start

	private static void updateIndex(int[] levels) {

		int j = G.numMain - G.passNumber;

		while (j > 0) {
			if (G.nIndex[j] != levels[j]) {
				G.nIndex[j]++;
				break;
			} // end if
			G.nIndex[j] = 1;
			j--;
		} // end while j

		if (G.passNumber == 1)
			return;
		if (G.irStar != 1) {
			for (int k = 1; k <= (G.irStar - 1); k++) {
				G.nzIndex[k] = G.nIndex[k];
			} // end for
		} // end if G.irStar

		if (G.irStar == G.numMain)
			return;

		for (int m = (G.irStar + 1); m <= G.numMain; m++) {
			G.nzIndex[m - 1] = G.nIndex[m];
		}

	} // end updateIndex
		// ========================== updateIndex ==============
		// end
		// ========================== jIndex ==============
		// start

	private static int jIndex(int[] levels, int[] values, int numdim, int offset) {
		/* levels values will be correct */

		int[] ivl = new int[7];

		int nlevelt = levels[numdim];
		int numd1 = numdim - 1;
		int j2;

		if (numd1 != 0) {
			for (int k = 1; k <= numd1; k++) {
				j2 = numdim - k;
				ivl[j2] = values[k] - 1;
			} // end for k
		}
		int location;
		// 1
		location = offset + values[numdim];
		if (numdim == 1)
			return location;

		// 2
		location = location + ivl[1] * nlevelt;
		if (numdim == 2)
			return location;

		// 3
		nlevelt = nlevelt * levels[numdim - 1];
		location = location + ivl[2] * nlevelt;
		if (numdim == 3)
			return location;

		// 4
		nlevelt = nlevelt * levels[numdim - 2];
		location = location + ivl[3] * nlevelt;
		if (numdim == 4)
			return location;

		// 5
		nlevelt = nlevelt * levels[numdim - 3];
		location = location + ivl[4] * nlevelt;
		if (numdim == 5)
			return location;

		// 6
		location = location + ivl[5] * nlevelt * levels[numdim - 4];
		return location;
	}
	// ========================== jIndex ==============
	// end
	// ========================== printRec ==============
	// start

	private static void printRec(int row) throws IOException {

		int howmany;
		if (row == -1) {
			PR.setTempHead("INPUT RECORD LISTING WITH RECORD MEANS");
			return;
		}
		// here row is not = -1

		if ( ! Flags.printRecords)
			return;
		/* recsToPrint = 0 means print all records */
		if (Sys.recsToPrint > 0) { // 1
			if (row > Sys.recsToPrint) { // 2
				if ((G.numRecs - row) >= Sys.recsToPrint)
					return;
			} // end if row ..//-2
		} // -1

		// record to print here

		howmany = (G.inRecord + 1 + 7)/ 8;
		PR.testPage(howmany + 1);
		int first = 1;
		int last = 8;
		int count;
		double temp = 0.0;
		StringBuilder sb = new StringBuilder();

		IO.mybw.newLine();
		PR.addLine(1);
		sb.append(" RECORD #" + Out.intx(7, row));
		while (first <= G.inRecord) {
			count = 0;
			last = first + 7;
			if (last > G.inRecord)
				last = G.inRecord;
			for (int k = first; k <= last; k++) {
				sb.append(Out.dx(12, 5, G.xbuf[k]));
				count++;
				temp = temp + G.xbuf[k];
			} // end for
			if (last == G.inRecord) {
				// here all record values have been queued up
				temp = temp / (double) G.inRecord;
				if (count != 8)
					sb.append(Out.dx(12, 5, temp));
				else {
					// here the record mean will be on its own line
					IO.doPrintLine(sb.toString());
					sb.setLength(0);
					sb.append(Out.fill(16, " "));
					sb.append(Out.dx(12, 5, temp));
					IO.doPrintLine(sb.toString());
				} // end else
			} // end if last ..,.
			IO.doPrintLine(sb.toString());
			first = first + 8;
			sb.setLength(0);
			sb.append(Out.fill(16, " "));
		} // end while

	} // end printRec
		// ========================== printRec ==============
		// end

}	// end Class Gphase1
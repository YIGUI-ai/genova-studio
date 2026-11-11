package initGen;

import java.io.IOException;

public class Gphase3 {

	static double[] xems = new double[64];
	static double vtemp;
	static double valems;

	// ========================== Gphase3.go ==============
	// start

	public static void go() throws IOException {

		double[] hold = new double[7];

		for (int jind = 1; jind <= G.numMain; jind++) {
			hold[jind] = 1.0d;
			if (G.mainUniverse[jind] > 0)
				hold[jind] = 1.0d - ((double) G.mainLevel[jind] / (double) G.mainUniverse[jind]);
		} // end for jind

		/* ===== temporary variables used in Gphase3.go ===== */
		int jtemp, j1, irow, numtot;
		int jrow = 0;

		int nd = 0;

		double xnd;
		double hval = 0;
		int mdiv = 0;
		double x = 0.0;

		// ===== temporary variables used in Gphase3.go =====

		jtemp = Sys.matBase[G.numTotal] + G.numTotal;
		for (int jind = 1; jind <= jtemp; jind++) { // 1
			G.xbuf[jind] = 0.0d;
		} // end for loop // -1

		j1 = G.numTotal + 1;
		loop200: for (int jind = 1; jind <= G.numTotal; jind++) { // 1
			j1--;
			irow = G.orderedTEF[j1];
			vtemp = 0.0d;
			numtot = G.indexTotal[irow] + 1;
			nd = 1;
			for (int k = 1; k <= G.numMain; k++) { // 2
				if (G.ID[irow][k] == 0)
					nd = nd * G.mainLevel[k];
			} // end for k -2
			xnd = (double) nd;
			valems = G.varMod[irow] * xnd;
			U.matPut(0, j1, j1, xnd);

			if (numtot > G.numMain) { // 2
				do180To200(irow, xnd);
				continue loop200;
			} // -2

			loop150: for (int m = G.orderedMEF[numtot]; m <= G.numTotal; m++) { // 2
				jrow = G.orderedTEF[m];
				if (!U.lSuper(irow, jrow))
					continue loop150;
				mdiv = 1;
				hval = 1.0d;

				loop50: for (int k = 1; k <= G.numMain; k++) { // 3
					if (G.ID[irow][k] > 0)
						continue loop50;
					if (G.ID[jrow][k] == 0)
						mdiv = mdiv * G.mainLevel[k];
					if (G.ID[jrow][k] == 1)
						hval = hval * hold[k];
				} // end for k loop50 // -3
				if (hval <= 0.0d)
					continue loop150;

				/* GOT A TERM TO ADD HERE */
				x = hval * (double) mdiv;

				U.matPut(0, j1, m, x);
				if (Flags.gComponents)
					valems = valems + x * G.varMod[jrow];
				if (G.varFin[jrow] > 0)
					vtemp = vtemp + x * G.varFin[jrow];
				if (Flags.debug)
					Bug.bug12gphase3before150(j1, jrow, m, x, nd, hval, mdiv, vtemp);

			} // end for m loop150 // -2
			do180To200(irow, xnd);

		} // end for jind iio200 // -1
		if (Flags.debug)
			Bug.bug13gphase3after200(xems);
		if (Flags.gComponents)
			return;
		U.matInv();

		U.matMpy();

		if (Flags.debug)
			Bug.bug14gphase3aftermatmpy();

		msVar();
		if (Flags.debug)
			Bug.bug15gphase3ftermsvar();

	} // end Gohase3.go
	// ========================== Gphase3.go ==============
	// end
	// ========================== do180To200 ==============
	// start

	private static void do180To200(int irow, double xnd) {
		if (Flags.gComponents)
			// if components run run
			G.xMsqr[irow] = valems;

		G.varFin[irow] = (G.xMsqr[irow] - vtemp) / xnd;
		xems[irow] = xnd * G.varFin[irow] + vtemp;
		if (G.varFin[irow] <= 0.0)
			xems[irow] = vtemp;
	}

	// ========================== do180To200 ==============
	// end
	// ========================== msVar ==============
	// start
	private static void msVar() throws IOException {

		int ktotal;
		int irow;
		double xdummy;

		ktotal = G.numTotal + 1;

		for (int j = 1; j <= G.numTotal; j++) {
			ktotal--;
			xdummy = 0.0d;
			for (int i = ktotal; i <= G.numTotal; i++) {
				irow = G.orderedTEF[i];
				xdummy = xdummy + U.valMat(Sys.offsetPT, i, ktotal) * G.xMsqr[irow];
			} // end for i
			irow = G.orderedTEF[ktotal];
			G.varMod[irow] = xdummy;
		} // end for j
	}
	// ========================== msVar ==============
	// end

}	// end Class Gphase3

package initGen;

public class G {

	public static String[] alphaID; //

	public static int[] firstRow; //

	public static int[][] ID; //
	public static int[] indexMain; //
	public static int[] indexTotal; //
	public static int[] iPass1; //

	public static boolean[] lArray; //

	public static int[] mainLevel; // Meflev
	public static int[] mainUniverse; //
	public static char[] mefChar; //

	public static int[] nIndex; //
	public static int[] nOffset; //
	public static int[] np1Cell; //
	public static int[] numDegFreedom; //
	public static int[] nzDim; //
	public static int[] nzIndex; //

	public static int[] orderedTEF; // NORTEF
	public static int[] orderedMEF;// INDTEF

	public static int[][] passID; //

	public static double[] varFin; //
	public static double[] varComp; //
	public static double[] varMod; //

	public static double[] sumSQ; //

	public static double[] xBarb; //
	public static double[] xbuf; //
	public static double[] xMsqr; //

	// non-array variables
	public static int bufLimit = 96000;

	public static int dataPoints; //

	// parameters from Format Card
	public static int fSkip; //
	public static int fWidth; //

	public static double grandMean; //

	public static int inRecord; //
	public static int irStar; //

	public static String nhead1; //
	public static String nhead2; //
	public static String nhead3; //

	// from Pass1
	public static int nPass1; //
	public static int nSum1; //

	public static int numMain; //
	public static int numNested; //
	public static int numTotal; //

	public static int numRecs; //
	public static int nxtOff; //
	public static int nzOffset; //

	public static int passNumber; //

	public static int totalDegFreedom; //
	public static double totalMsq; //

	public static double vcompOrMsquare; //

	public static double xMean; //

	public static int newTotal;

	// ============= initExecute ==============
	// start

	public static void initExecute() {

		numNested = 1;
		numRecs = 0;
		inRecord = 0;
		xMean = 0.0;
		totalMsq = 0.0;
	}
	// ============= initExecute ==============
	// end
	// ============= gPopulate ==============
	// start

	private static void gPopulate() {
		if (Flags.populateG)
			return;
		Flags.populateG = true;

		alphaID = new String[64];
		firstRow = new int[8];
		ID = new int[65][7];
		indexMain = new int[64];
		indexTotal = new int[64];
		iPass1 = new int[33];

		lArray = new boolean[33];
		mainUniverse = new int[7];
		mefChar = new char[7];
		mainLevel = new int[7];

		nIndex = new int[7];
		nOffset = new int[33];
		np1Cell = new int[33];
		numDegFreedom = new int[64];
		nzDim = new int[7];
		nzIndex = new int[7];

		orderedMEF = new int[8];
		orderedTEF = new int[64];
		passID = new int[34][7];

		varComp = new double[64];
		varFin = new double[64];
		varMod = new double[64];
		sumSQ = new double[64];

		xBarb = new double[64];
		xbuf = new double[G.bufLimit];
		xMsqr = new double[64];

	}
	// ============= gPopulate ==============
	// end
	// ============= initcommon ==============
	// start

	private static void initcommon() {
		// initializes ID,mainUniverse, and numMean

		gPopulate();

		grandMean = 0.0;
		nPass1 = 0;
		nSum1 = 0;
		numMain = 0;
		numTotal = 0;
		nxtOff = 0;
		nzOffset = 0;
		totalDegFreedom = 0;

		for (int k = 1; k < 7; k++) {
			mainUniverse[k] = 0;
			mainLevel[k] = 0;
			mefChar[k] = ' ';

			for (int j = 1; j < 64; j++) {
				ID[j][k] = 0;
				if (j < 64) {
					indexMain[j] = 0;
					indexTotal[j] = 0;
					orderedTEF[j] = 0;
					numDegFreedom[j] = 0;
				} // end if

			} // end for j

		} // end for k
		for (int k = 0; k < 64; k++) {
			xBarb[k] = 0.0d;
			sumSQ[k] = 0.0d;
			xMsqr[k] = 0.0d;
			varComp[k] = 0.0d;
			varMod[k] = 0.0d;
			varFin[k] = 0.0d;
		} // end for k
		for (int m = 1; m < 8; m++) {
			orderedMEF[m] = 0;
			firstRow[m] = 0;
		}

	} // end initCommon

	// ============= initcommon ==============
	// end
	// ============= gstudyInit ==============
	// start

	public static void gstudyInit() {

		initcommon();

		Flags.gStudy = true;
		Flags.gComponents = false;
		Flags.gMSquare = false;
		Flags.defaultDstudy = false;

		Flags.anyMean = false;
		Flags.dstudyPossible = false;
		Flags.format = false;
		Flags.grandMean = true;
		Flags.name = false;
		Flags.nonRandom = false;
		Flags.printMsquare = false;
		for (int k = 0; k < 6; k++) {
			Flags.printMean[k] = false;
		}

		G.irStar = 0;
		G.numMain = 0;

		Sys.lastRun = 1;
		PR.clearSubHead();
		PR.clearTempHead();
		;
	}
	// ============= gstudyInit ==============
	// end
	// ============= gMsqInit ==============
	// start

	public static void gMsqInit() {

		initcommon();

		Flags.gStudy = false;
		Flags.gComponents = false;
		Flags.gMSquare = true;
		Flags.defaultDstudy = false;

		Flags.dstudyPossible = false;
		Flags.grandMean = false;
		Flags.inMain = true;
		Flags.nonRandom = false;

		numMain = 0;
		for (int k = 1; k < 64; k++) {
			xBarb[k] = -1.0d;
		}
		for (int k = 0; k < 7; k++) {
			mainUniverse[k] = 0;
		} // end for k

		Sys.lastRun = 1;
		PR.clearSubHead();
		PR.clearTempHead();
		;

	}
	// ============= gMsqInit ==============
	// end

	// ============= gCompInit ==============
	// start

	public static void gCompInit() {

		initcommon();

		Flags.gStudy = false;
		Flags.gComponents = true;
		Flags.gMSquare = false;
		Flags.defaultDstudy = false;

		Flags.dstudyPossible = false;
		Flags.grandMean = false;
		Flags.inMain = true;
		Flags.nonRandom = false;

		numMain = 0;
		for (int k = 1; k < 64; k++) {
			xBarb[k] = -1.0d;
		}

		Sys.lastRun = 1;
		PR.clearSubHead();
		PR.clearTempHead();
		;

	}
	// ============= gCompInit ==============
	// end
	// ============= charIndex ==============
	// start

	public static int charIndex(char achar) {
		int index = -1;
		for (int k = 1; k <= numMain; k++) {
			if (achar == mefChar[k]) {
				return k;
			} // end if
		} // end for
		return index;

	} // end charIndex

	// ============= charIndex ==============
	// end

}	// end Class G

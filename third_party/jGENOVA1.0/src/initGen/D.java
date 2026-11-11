package initGen;

import java.io.IOException;

public class D {

	// the D class will hold variables and report headings
	// for various D Study methods and reports
	// and willset up several report heading strings for reuse

	public static String divider;
	public static String d2l6p2;
	public static String d2l7p2;
	public static String[][] d;

	public static String[][] dh;
	public static String dhtemp[];

	public static int[] dSample;
	public static int[] dUniverse;
	public static int[] gorderedTEF;
	public static int[][] IDG;
	public static char[] mefCharG;
	public static int[][] sampID;

	public static double[] varDGT;
	public static double[] xDcut;
	public static double gGraandMean;

	public static int maxDstudy;

	public static int numDcut;
	public static int numDstudy;
	public static int numMainG;
	public static int numTotalG;

	public static int offsetDV;
	public static int offsetGT;
	public static int offsetDN;

	public static int seqDstudy;
	public static int starID;

	public static boolean dstudyCut;

	public static double[] e11;
	public static double[] dp12;
	public static double[] v13;
	public static double[] se14;
	public static double[] v15;
	public static double[] ep16;
	public static double[] v18;
	public static double[] se19;
	public static double[] epdp;
	public static double[] dc42;
	public static double[] dc43;

	public static double[][] dSummary;

	// ============== dPopulate ============
	// start

	private static void dPopulate() {

		if (Flags.populateD)
			return;
		Flags.populateD = true;

		d = new String[3][10];
		dh = new String[5][3];
		dhtemp = new String[5];

		dSample = new int[7];
		dUniverse = new int[7];
		gorderedTEF = new int[64];
		IDG = new int[64][7];
		mefCharG = new char[7];
		varDGT = new double[64];

		e11 = new double[64];
		dp12 = new double[64];
		v13 = new double[64];
		se14 = new double[64];
		v15 = new double[64];
		ep16 = new double[64];
		v18 = new double[64];
		se19 = new double[64];
		epdp = new double[64];
		dc42 = new double[64];
		dc43 = new double[64];

		sampID = new int[7][33];
		xDcut = new double[31];
		dSummary = new double[31][8];
	}
	// ============== dPopulate ============
	// end
	// ============== D.dInit ============
	// start

	public static void dInit() {

		dPopulate();

		setDstudyHeads();

		for (int k = 1; k <= 6; k++) {
			dSample[k] = 0;
			dUniverse[k] = 0;
			mefCharG[k] = ' ';
			;
		}

		for (int k = 1; k <= 63; k++) {
			gorderedTEF[k] = 0;
			varDGT[k] = 0.0d;
			for (int j = 1; j <= 6; j++) {
				IDG[k][j] = 0;
				if (k < 33)
					sampID[j][k] = 0;
			}
		}

		numDcut = 0;
		numDstudy = 0;
		numMainG = 0;
		numTotalG = 0;

		offsetDV = 0;
		offsetGT = 0;
		offsetDN = 0;

		seqDstudy = 0;
		starID = 0;
		numDcut = 0;
		maxDstudy = 0;
		dstudyCut = false;
//		PR.setPageType(3);
		;

	}
	// ============== D.dInit ============
	// end
	// ============== D.defaultDstudy ============
	// start

	public static void defaultDstudy() throws IOException {

		dInit();
		starID = Math.max(1, G.irStar);
		maxDstudy = 1;
		numDstudy = 0;
		for (int k = 1; k <= G.numMain; k++) {
			dSample[k] = 1;
			dUniverse[k] = G.mainUniverse[k];
			sampID[k][1] = G.mainLevel[k];
		}
		double x;

		for (int j = 1; j <= G.numTotal; j++) {
			if (Flags.EMS)
				x = G.varFin[j];
			else
				x = G.varMod[j];
			if (x < 0.0d)
				x = 0.0d;
			D.varDGT[j] = x;
		} // end for j

		int jp = Sys.offsetSE;
		for (int k = 1; k <= Sys.offsetPT; k++) {
			jp++;
			G.xbuf[k] = G.xbuf[jp];
		}
		Sys.offsetSE = 0;
		offsetDV = 0;
		offsetDN = Sys.offsetPT;
		seqDstudy = 0;

		PR.setTempHead("Default D-Study");
		Flags.dStudyCut = false;
		numDcut = 0;

	}
	// ============== D.defaultDstudy ============
	// end
	// ============== setDstudyHeads ============
	// start

	private static void setDstudyHeads() {

		StringBuilder sb = new StringBuilder();
		StringBuilder sc = new StringBuilder();

		sb.append(Out.blank(26));
		sb.append("Variance Components in Terms of");
		sb.append(Out.blank(28));
		sb.append("Variance Components in Terms of");
		d[1][1] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(16));
		sb.append("G Study Universe (of ");
		sb.append("Admissible Observations) Sizes");
		sb.append(Out.blank(13));
		sb.append(" Study Universe (of ");
		sb.append("Generalization) Sizes");
		d[1][2] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(13));
		sb.append(Out.fill(58, "_") + " ");
		sb.append(Out.fill(59, "_"));
		d[1][3] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(48));
		sb.append("Variance Components");
		sb.append(Out.blank(40));
		sb.append("Variance Components");
		d[1][4] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(15));
		sc.append("Variance     Finite   D Study");
		sc.append("      for Mean Scores");
		sb.append(sc);
		sb.append("         ");
		sb.append(sc);
		d[1][5] = sb.toString();

		sb.setLength(0);
		sc.setLength(0);
		sb.append(Out.blank(14));
		sc.append("Components   Universe Sampling ");
		sc.append(Out.fill(26, "_"));
		sb.append(sc).append("  ");
		sb.append(sc).append("_");
		d[1][6] = sb.toString();

		sb.setLength(0);
		sc.setLength(0);
		sb.append(Out.blank(14));
/* 2.0		sc.append("for Single     Cor-     Pre-"); 
 */
		sc.append("for Single     Cor-     Fre-");
		sc.append(Out.blank(19)).append("Standard");
		sb.append(sc).append("    ").append(sc);
		d[1][7] = sb.toString();

		sb.setLength(0);
		sc.setLength(0);
		sb.append(" Effect      ");
		sc.append("Observations  rections quencies");
		sc.append("   Estimates      Errors    ");
		sb.append(sc).append(sc);
		d[1][8] = sb.toString();

		sb.setLength(0);
		sc.setLength(0);
		sb.append(Out.blank(12));
		sb.append("QFM = Quadratic Form");
		d[1][9] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(52));
		sb.append("Standard");
		d[2][1] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(42));
		sb.append("Standard  Error of");
		d[2][2] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(32));
		sb.append("Variance Deviation  Variance");
		d[2][3] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(14));
		sb.append("Universe Score");
		d[2][4] = sb.toString();

		d[2][5] = "     Expected Observed Score";

		sb.setLength(0);
		sb.append(Out.blank(12));
		sb.append("Lower Case Delta");
		d[2][6] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(12));
		sb.append("Upper Case Delta");
		d[2][7] = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(24));
		sb.append("Mean");
		d[2][8] = sb.toString();

		sb.setLength(0);
		sb.append("        Note:  Signal/Noise ");
		sb.append("Ratios are in parentheses");
		d[2][9] = sb.toString();

		sb.setLength(0);
		sb.setLength(0);
		sb.append(Out.blank(10));
		sb.append("Generalizability Coefficient = ");
		d2l6p2 = sb.toString();

		sb.setLength(0);
		sb.append(Out.blank(35));
		sb.append("Phi = ");
		d2l7p2 = sb.toString();

		dh[1][1] = "       Object of Measurement : ";
		dh[1][2] = "                  Facets : ";
		dh[2][1] = "     G Study Population Size : ";
		dh[2][2] = "  G Study Universe Sizes : ";
		dh[3][1] = "     D Study Population Size : ";
		dh[3][2] = "  D Study Universe Sizes : ";
		dh[4][1] = "         D Study Sample Size : ";
		dh[4][2] = "    D Study Sample Sizes : ";
		dhtemp[1] = "";
		// dhtemp will be good for a particular D study run

		divider = " " + Out.fill(129, "_");
	}
	// ============== D.setDstudyHeads ============
	// end

}	// end Class D

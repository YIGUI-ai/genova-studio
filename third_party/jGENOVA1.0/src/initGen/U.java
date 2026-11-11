package initGen;

import java.io.IOException;

/**		
 * Class u		
 * <P<		
 * The u class contains several utility		
 * Methods that are utilized by multiple methods		
 * </P>		
 * @Author joecrick		
 *		
 */
/**		
 * U.matMpy		
 * <P></p> 		
 * @Author joecrick		
 * *  Matmpy will multiply pinv * d2 * pinvt to produce 		
 *     A symmetric variance-covariance matrix where 		
 *     Se(i) = sqrt(vcvc(i,i)) for all i. 		
 * 		
 *  Pinv = the inverse of p == triangular matrix of 		
 *     Ems coefficients. 		
 * 		
 *  D2 = 2 * ms * ms / (df + 2) where ms = mean 		
 *     Square and df = degrees of freedom. 		
 * 		
 *  Pinvt = transpose of pinv. 		
 * 		
 * @Return   variance-covariance matrix		
 * 		
 *     The variance-covariance matrix will be 		
 *     Stored in G.xbuf ( a double[] vector in class g)		
 *     Starting at location Sys.offsetSE + 1. 		
 * Calling methods: 		
 *  	Gphase3.Go	
 * 		Endcomp.Go
 * 		
 * Methods invoked:		
 * 		U.Matput
 * 		U.Valmat
 *		
 */
/**		
 * 		
 *  U.Matinv		
 * 		
 * @Author joecrick		
 *		
 *  matInv will invert the triangular matrix of ems    		
 *     Coefficients stored at the first part of xbuf   		
 *     And will store the inverted matrix starting 		
 *     At location Dys.offsetPT + 1  		
 *  		
 *  		
 *  @Returns inverted triangular matrix in		
 *     G.Xbug (a double[] vector in class g)		
 *    Starting at Sys.offsetPT + 1		
 *  		
 * Calling methods: 		
 *  	Gphase3.Go	
 * 		Endcomp.Go
 * 		
 * Methods invoked:		
 * 		U.matPut
 * 		U.valMat
 * </P>		
 -		
 */
/**
 * U.lSuper
 * 
 * @Author j joecrick
 * 
 *         Calling methods: Cphase2.Cmpmod Endcomp.Modcmp Gphase2.Ssval
 *         Gphase2.Go Cphase3.Go
 * 
 *         Methods invoked: None
 * 
 */

public class U {
	// =========== effectOut =======
	// start

	public static String effectOut(int row) { // 1

		int ntotal = G.indexTotal[row];

		int sofar = 0;
		StringBuilder sb = new StringBuilder();

		for (int main = 1; main <= G.numMain; main++) { // 2

			for (int col = 1; col <= G.numMain; col++) { // 3

				if (G.ID[row][col] == main) { // 4
					sb.append(G.mefChar[col]);
					sofar++;
				} // end if // -4
			} // end icol // -3
			if (sofar < ntotal)
				sb.append(":");
		} // -2
		sb.append("          ");
		String alpha = sb.toString();
		return alpha.substring(0, 11);

	}
	// =========== effectOut =======
	// end
	// =========== ifSlash =======
	// start

	public static boolean ifSlash(String str) {
		if (str.length() != 1)
			return false;
		if (str.startsWith("/"))
			return true;
		else
			return false;

	} // end ifSlash
	// =========== ifSlash =======
	// end
	// =========== isDefault =======
	// start

	public static boolean isDefault(String inString) {

		inString = inString + "   ";
		inString = inString.substring(0, 4);
		if (inString.startsWith("DEFA")) {
			Flags.defaultDstudy = true;
			return true;
		} else
			return false;
	} // end isDefault
	// =========== isDefault =======
	// end
	// =========== lSuper =======
	// start

	public static boolean lSuper(int irow, int jrow) {

		loop10: for (int k = 1; k <= G.numMain; k++) {
			if (G.ID[irow][k] == 0)
				continue loop10;
			if (G.ID[jrow][k] == 0)
				return false;
		} // end for loop10
		return true;
	} // end lSuper
	// =========== lSuper =======
	// end
	// =========== matInv =======
	// start

	public static void matInv() throws IOException {

		double x;
		for (int ind = 1; ind <= G.numTotal; ind++) {
			x = 1.0d / U.valMat(0, ind, ind);
			U.matPut(Sys.offsetPT, ind, ind, x);
		} // end for ind
		for (int j = 1; j <= (G.numTotal - 1); j++) {
			for (int i = (j + 1); i <= G.numTotal; i++) {
				x = 0.0d;
				for (int k = j; k <= (i - 1); k++) {
					x = x + U.valMat(0, k, i) * U.valMat(Sys.offsetPT, k, j);
				} // end for k
				x = -x * U.valMat(Sys.offsetPT, i, i);
				U.matPut(Sys.offsetPT, i, j, x);
			} // end for i
		} // end for j

		return;
	} // end matInv
	// =========== matInv =======
	// end
	// =========== matMpy =======
	// start

	public static void matMpy() throws IOException {
		int jtemp;
		double dtwo = 2.0;
		double[] xd2 = new double[64];
		double xdummy;
		for (int i = 1; i <= G.numTotal; i++) {
			jtemp = G.orderedTEF[i];
			xd2[i] = dtwo * G.xMsqr[jtemp] * G.xMsqr[jtemp] / (double) (G.numDegFreedom[jtemp] + 2);
		} // end for i
		for (int j = 1; j <= G.numTotal; j++) {
			for (int ind = j; ind <= G.numTotal; ind++) {
				xdummy = 0.0d;
				for (int k = ind; k <= G.numTotal; k++) {
					xdummy = xdummy + U.valMat(Sys.offsetPT, j, k) * xd2[k] * U.valMat(Sys.offsetPT, k, ind);
				} // end for k
				U.matPut(Sys.offsetSE, ind, j, xdummy);
			} // end for i
		} // end for j
	} // end matMpy
	// =========== matMpy =======
	// end
	// =========== matPut =======
	// start

	public static void matPut(int offset, int irow, int jrow, double value) throws IOException {
		int jloc;
		if (irow >= jrow)
			jloc = Sys.matBase[irow] + jrow;
		else
			jloc = Sys.matBase[jrow] + irow;
		jloc = jloc + offset;
		G.xbuf[jloc] = value;

		if (Flags.debug) {
			String str = " *** IN MATPUT: JOFF =" + Out.intx(3, offset) + " IROW =" + Out.intx(3, irow) + " IROW ="
					+ Out.intx(3, jrow) + " XVAL =" + Out.dx(20, 6, value) + " XBUF INDEX =" + Out.intx(4, jloc);
			PR.bugLine(str);
			IO.mybw.flush();
		}

	} // end matPut
	// =========== matPut =======
	// end
	// =========== saveSubTitle    =======
	// start

	public static void saveSubTitle(String subHead) throws IOException {
		// set title to this token
		if (subHead.length() > 100) {
			subHead = subHead.substring(0,100);
		}
		PR.setSubHead(subHead);
	} // end saveSubTitle
	// =========== saveSubTitle    =======
	// end
	// =========== setOutputFile   =======
	// start

	public static void setOutputtFile(String filename) throws IOException {
		// set output file to this token
		IO.setPrintFile(filename);
		PR.setNumLines(200);
	}
	// =========== setOutputFile   =======
	// end
	// =========== toDouble        =======
	// start
	public static double toDouble(String message, String token) throws NumberFormatException, IOException {

		double d = 0.0d;
		try {
			// the String to double conversion happens here
			d = Double.parseDouble(token);
		} // end try
		catch (NumberFormatException nfe) {
			Sys.setErrorMsg(message);
		} // end catch
		return d;
	}
	// =========== toDouble        =======
	// end
	// =========== toInteger       =======
	// start
	public static int toInteger(String type, String token) throws NumberFormatException, IOException {

		int result = 0;
		try {
			// the String to integer conversion happens here
			result = Integer.parseInt(token);
		} // end try
		catch (NumberFormatException nfe) {
			Sys.setErrorMsg(" Sample or Universe parameter " + type + " card not a integer value");
		} // end catch
		return result;
	}
	// =========== toInteger       =======
	// end
	// =========== valMat          =======
	// start

	public static double valMat(int offset, int row, int col) throws IOException {
		double value;
		int jloc;
		if (row < col)
			jloc = offset + Sys.matBase[col] + row;
		else
			jloc = offset + Sys.matBase[row] + col;
		value = G.xbuf[jloc];

		if (Flags.debug) {
			String str = " *** IN VALMAT:  OFF =" +

					Out.intx(3, offset) + " JROW =" + Out.intx(3, row) + ",JCOL =" + Out.intx(3, col) + " JLOC ="
					+ Out.intx(3, jloc) + " VALMAT =" + Out.dx(12, 6, value);
			PR.bugLine(str);
			IO.mybw.flush();
		} // END IF DEBUG

		return value;
	} // end valMat
	// =========== valMat =======
	// end

} // end Class U

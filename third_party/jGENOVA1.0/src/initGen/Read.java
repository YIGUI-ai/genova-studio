package initGen;

import java.io.IOException;

public class Read {

	private static String[] tokens;
	private static double[] dout;
	private static String line;
	
	// ============= record =======
	// start

	public static void record() throws IOException {

		tokens = new String[G.inRecord + 1];
		dout = new double[G.inRecord + 1];

		if (G.fWidth == 0)
			readFreeRec();
		else
			readFixedRec();
		// now convert to Double ad store in xbuf
		tokensToBuffer();
		for (int i = 1; i <= G.inRecord; i++) {
			G.xbuf[i] = dout[i];
		}

	} // end Read.record
	// ============= record =======
	// end
	// ============= getaLine =======
	// start

	private static void getaLine() throws IOException {
		line = IO.getData();
		if (IO.getDataEOF()) {
			Sys.setErrorMsg("  EOF encountered reading data");
		}
		if (G.fSkip < line.length())// $$ error on eof
			line = line.substring(G.fSkip);
	}
	// ============= getaLine =======
	// end
	// ============= tokensToBuffer =======
	// start

	private static void tokensToBuffer() throws IOException {
		// this will transfer strings from tokens and
		// convert to Double and store in xbuf
		String message = 
				" Invalid non-numeric value "
				+ "found when reading input data";
			
		for (int k = 1; k <= G.inRecord; k++) {
			dout[k] = U.toDouble(message, tokens[k]);
		}
	}
	// ============= tokensToBuffer =======
	// end
	// ============= readFreeRec =======
	// start

	private static void readFreeRec() throws IOException {

		int soFar = 0;
		getaLine();
		int howMany;
		String[] parts = new String[G.inRecord + 1];
		loopWhile: while (soFar < G.inRecord) {
			if (soFar > 0)
				// multiple physical records
				// needed for one logical record
				getaLine();
			
			parts = line.split(Sys.delimiters, 0);

			howMany = parts.length;
			for (int k = 0; k < howMany; k++) {
				if (parts[k].length() > 0) {
					soFar++;
					if (soFar <= G.inRecord) {
						tokens[soFar] = parts[k];
					} else
						break loopWhile;
				} // end if
			} // end for
		} // end while loopWhile

	}
	// ============= readFreeRec =======
	// end
	// ============= readFixedRec =======
	// start

	private static void readFixedRec() throws IOException {

		int soFar = 0;
		getaLine();
		int first, last, thisRec;
		loopWhile: while (soFar < G.inRecord) {
			if (soFar > 0)
				// multiple physical records
				// needed for one logical record
				getaLine();
			first = 0;
			last = G.fWidth;
			thisRec = line.length() / G.fWidth;
			for (int k = 1; k <= thisRec; k++) {
				// put fWidth characters into a vector
				// of Strings

				soFar++;
				if (soFar <= G.inRecord) {

					tokens[soFar] = line.substring(first, last);
					first = first + G.fWidth;
					last = last + G.fWidth;
				} // end if
				else
					break loopWhile;
			} // end for
		} // end loopWhile

	} // end readFixedRec
	// ============= readFixedRec =======
	// end
	
}	// end Class Read
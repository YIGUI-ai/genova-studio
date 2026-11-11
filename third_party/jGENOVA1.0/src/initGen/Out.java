package initGen;

public class Out {

	// Out will contain a variety of value to string routines
	// to facilitate building output line of text

	

	// ============= int3 ==============
	// start

	public static String int3(int value) {
		String pattern = "%0" + 3 + "d";
		String str = String.format(pattern, value);
		return str;
	}
	// ============= int3 ==============
	// end
	// ============= intx ==============
	// start

	public static String intx(int wide, int value) {
		String pattern = "%" + wide + "d";
		String str = String.format(pattern, value);
		return fixstr(str, wide);
	}
	// ============= intx ==============
	// end
	// ============= fill ==============
	// start

	public static String fill(int width, String symbol) {
		StringBuilder sb = new StringBuilder();
		for (int k = 1; k <= width; k++) {
			sb.append(symbol);
		}
		return sb.toString();
	}
	// ============= fill ==============
	// end
	// ============= blank ==============
	// start

	public static String blank(int width) {
		StringBuilder sb = new StringBuilder();
		for (int k = 1; k <= width; k++) {
			sb.append(" ");
		}
		return sb.toString();
	}
	// ============= blank ==============
	// end
	// ============= dx ==============
	// start

	public static String dx(int w, int d, double value) {
		String pattern = "% " + w + "." + d + "f";
		String str = String.format(pattern, value);
		return fixstr(str, w);
	}
	// ============= dx ==============
	// end
	// ============= fixstr ==============
	// start

	private static String fixstr(String st, int wide) {

		int len = st.length();
		if (len == wide)
			return st;
		if (len > wide + 1)
			return fill(wide, "*");
		if (len == wide + 1) {
			if (st.substring(0, 1).matches(" "))
				return st.substring(1);
			else
				return fill(wide, "*");
		}
		return st;
	}
	// ============= fixstr ==============
	// end
	// ============= ex ==============
	// start

	public static String ex(int w, int d, double value) {
		String pattern = "% " + w + "." + d + "e";
		String str = String.format(pattern, value);
		return str;
	}
	// ============= ex ==============
	// end

}	// end Class Out

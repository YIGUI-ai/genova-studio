package initGen;

import java.io.IOException;

public class Name {

	private static String[] nameHold;

	// ============= initName ==============
	// start

	private static void initName() {
	
		Flags.name = true;
		nameHold = new String[6];

		for (int k = 0; k < 6; k++) {
			nameHold[k] = null;
		} // end for
	} // end Name.initBane
	// ============= initName ==============
	// end

	// ============= processName ==============
	// start

	public static void processName(String effect, String description) throws IOException {
		// tokens [0] = main effect string
		// tokens [1] = main effect description
		//
		// if (Glevel.getNumMain() == 6)
		// Sys.setErrorMsg(" Name card found before Effect cards - exit");

		if (!Flags.name)
			initName();
		if (effect.length() != 1)
			Sys.setErrorMsg(" first parameter must be a single letter");
		else { // else 1
			char achar = effect.charAt(0);
			int index = G.charIndex(achar);
			if (index == -1)
				Sys.setErrorMsg(" first parameter on a name card not a main effect");
			else { // else 2
				if (nameHold[index] != null)
					Sys.setWarning(" duplicate name card found for " + achar + " ignored");
				else
					nameHold[index] = description;
			} // end else 2
		} // end else 1

	} // end processName
	// ============= processName ==============
	// end
	// ============= doHeadings ==============
	// start

	private static void doHeadings() throws IOException {
		PR.clearTempHead();
		PR.lineSkip(5);
		String fill32 = Out.blank(32);
		String str = "Listed below are the " + "User-supplied Names that";
		IO.doPrintLine(fill32 + str);
		str = "the respective Main Effect" + " characters in this";
		IO.doPrintLine(fill32 + str);
		str = "Study represent.";
		IO.doPrintLine(fill32 + str);
		skipOnly(4);
		IO.doPrintLine("          Main");
		IO.doPrintLine("          Effect");
		IO.doPrintLine("          Character" + "  User-supplied Name");
		skipOnly(1);

	} // end of doHeadings
	// ============= doHeadings ==============
	// end
	// ============== skipOnly ============
	// start

	private static void skipOnly(int lines) throws IOException {
		for (int k = 0; k < lines; k++) {
			IO.mybw.newLine();
		} // end for
	}
	// ============== skipOnly ============
	// end
	// ============= doTop ==============
	// start

	private static void doTop() throws IOException {

		IO.doPrintLine(Out.blank(10) + Out.fill(89, "_"));

	} // end of getTop
	// ============= doTop ==============
	// end
	// ============= doBottom ==============
	// start

	private static void doBottom() throws IOException {

		String str = Out.blank(9) + "|_______|" + Out.fill(81, "_") + "|";
		IO.doPrintLine(str);

	} // end getBottom
	// ============= doBottom ==============
	// end
	// ============= doSpacer ==============
	// start

	private static void doSpacer() throws IOException {

		String str = Out.blank(9) + "|       |" + Out.blank(81) + "|";
		IO.doPrintLine(str);

	} // end of doSpacer
	// ============= doSpacer ==============
	// end
	// ============= printNames ==============
	// start

	public static void printNames()
			throws IOException {
		
		StringBuilder sb = new StringBuilder();

		doHeadings();
		doTop();
		for (int k = 1; k <= G.numMain; k++) {
			if (nameHold[k] != null) {
				doSpacer();
				sb.setLength(0);
				sb.append(Out.blank(9));
				sb.append("|   ");
				sb.append(G.mefChar[k]);
				sb.append("   |   ");
				sb.append(fixDesc(nameHold[k]));
				sb.append("   |");
				IO.doPrintLine(sb.toString());
				doSpacer();
				doBottom();
			} // end if
		} // end for k
	// clear Flags.name
		Flags.name = false;

	} // end printNames
	// ============= printNames ==============
	// end
	// ============= fixDesc ==============
	// start

	private static String fixDesc(String name) {

		int len = 75 - name.length();
		if (len < 0)
			return name.substring(0, 75);
		else
			return name + Out.blank(len);
	} // end fixDesc

	// ============= fixDesc ==============
	// end

} // end Class Name

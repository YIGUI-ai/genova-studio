package initGen;

import java.io.IOException;

public class E {

	// The E class will contain routines
	// relative to input of a main effect string
	// used for:
	// EFFECT cards
	// VCOMPONENTS cards
	// MEANSQUARES cards
	// DEFFECT cards

	// ================== effectInput ==============
	// start

	public static void effectInput(String instring) throws IOException {

		int len = instring.length();
		char charFirst = instring.charAt(0);
		G.mefChar[G.numMain] = charFirst;
		G.ID[G.numMain][G.numMain] = 1;

		checkErrors(instring);

		duplicate(charFirst);
		// now the string is valid
		G.mefChar[G.numMain] = charFirst;
		// Glevel.mefChar[numMainM1] = charFirst;

		int numColon = 1;
		int index;
		char achar;
		for (int k = 1; k < len; k++) {
			achar = instring.charAt(k);
			if (achar == ':')
				numColon++;
			else { // else 1
				index = G.charIndex(achar);

				if (index == -1) {
					Sys.setWarning(" logic error in effectInput");
					Sys.setErrorMsg(" character = " + achar);
				} // end if

				G.ID[G.numMain][index] = numColon;

			} // end else 1
		} // end for k

	} // end EffectInput
	// ================== effectInput ==============
	// end
	// ================== checkErrors ==============
	// start

	private static void checkErrors(String instring) throws IOException {
		int len = instring.length();
		char charfirst = instring.charAt(0);
		char charlast = instring.charAt(len - 1);

		if (!(Character.isLetter(charfirst) && Character.isLetter(charlast)))
			Sys.setErrorMsg(" First or last character " + "not a letter");
		// error 5 ==> first or last character was not a letter OK

		else {
			if (len > 1) {
				if (instring.charAt(1) != ':')
					Sys.setErrorMsg("Second character of " + "main effect not a ':'");
				// error 6 ==> second character not a colon
			} // end if len > 1
		} // end else

		if (len > 3) {
			char charmid;
			for (int j = 2; j < len - 1; j++) {
				charmid = instring.charAt(j);
				if (!Character.isLetter(charmid) && charmid != ':')
					Sys.setErrorMsg(" Invalid character " + "in main effect string");
				// error 1 ==> invalid character in effect string OK
			} // end for j
		} // end if len > 3

		checkColons(instring);
		dupChars(instring);
		effectExists(instring);

	} // end CheckErrors
	// ================== checkErrors ==============
	// end
	// ================== duplicate ==============
	// start

	private static void duplicate(char mchar) throws IOException {
		// will return true if mchar is already a main effect

		if (G.numMain > 1) {
			for (int k = 1; k <= G.numMain - 1; k++) {
				if (mchar == G.mefChar[k])
					Sys.setErrorMsg(" Duplicate Main effect " + mchar + " encountered");
			} // end for
		} // end id numMain > 1
	} // end duplicate
	// ================== duplicate ==============
	// end
	// ================== checkColons ==============
	// start

	private static void checkColons(String inValue) throws IOException {

		int lastLoc = -1;
		char aChar;
		for (int k = 1; k < inValue.length() - 1; k++) {
			aChar = inValue.charAt(k);
			if (aChar == ':') {
				if (lastLoc + 1 == k)
					Sys.setErrorMsg(" Adjacent colons " + "invalid in main effect string");
				// error 7 ==> duplicate colons
				else
					lastLoc = k;
			} // end if aChar
		} // end for

	} // end checkColons
	// ================== checkColons ==============
	// end
	// ================== effectExists ==============
	// start

	private static void effectExists(String instring) throws IOException {
		// ensure that all characters after the first
		// are already read in
		char achar;
		int len = instring.length();
		if (len > 2) {
			for (int k = 2; k < len; k++) {
				achar = instring.charAt(k);
				if (achar != ':') {
					if (!inmefChar(achar))
						Sys.setErrorMsg(" Effect " + achar + " not alrady defined");
					// error 3 ==> main effect character not found before OK

				} // end if achar not colon OK
			} // end for k

		} // end if len > 2

	} // end effectExists
	// ================== effectExists ==============
	// end
	// ================== inmefChar ==============
	// start

	private static boolean inmefChar(char achar) {
		boolean exists = false;

		for (int k = 1; k <= G.numMain; k++) {
			if (achar == G.mefChar[k]) {
				exists = true;
				break;
			} // end if
		} // end for

		return exists;
	} // end inmefChar
	// ================== inmefChar ==============
	// end
	// ================== dupChars ==============
	// start

	private static void dupChars(String instring) throws IOException {
		// now check for duplicate char on main effect
		char charHold;
		int len = instring.length();

		for (int k = 1; k < len - 1; k++) {
			charHold = instring.charAt(k);
			if (charHold != ':') {
				for (int j = k + 2; j < len; j++) {
					if (charHold == instring.charAt(j))
						Sys.setErrorMsg(" Duplicate characters " + "on main effect string");
					// error 4 ==> duplicate character in main effect OK

				} // end for j
			} // end if charHold not = colon

		} // end for k

	} // // end dupChars
	// ================== dupChars ==============
	// end

} // end class E

package initGen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

public class PR {

	private static int numLines; // get set
	private static int pageNum; // get set
	//

	private static String pageTop = new String(); // get set
	private static String subHead = new String(); // get set
	private static String myTitle = new String(); // get set
	private static String tempHead = new String(); // get set
	private static String pageHeading = new String();
	//
	// ============== Init ============
	// start

	public static void Init() {
		// initialize PR variables for processing
		numLines = 200;
		pageNum = 0;
		setPageType(1);
		subHead = "";
		pageTop = setPageTop();

	}
	// ============== Init ============
	// end
	// ============== center ============
	// start

	public static String center(String sValue) {
		return center(sValue, 130);
	}
	// ============== center ============
	// end
	// ============== center ============
	// start

	public static String center(String sValue, int width) {
		String result = sValue.trim();
		int diff = (width - result.length()) / 2;
		result = Out.blank(diff) + result;
		return result;
	}
	// ============== center ============
	// end
	// ============== getLineLimit ============
	// start

	public static int getLineLimit() {
		return Sys.getLinesPerPage();
	}

	// ============== getLineLimit ============
	// end
	// ============== setPageType ============
	// start
	//
	public static void setPageType(int pageType) {
		switch (pageType) {
		case 1:
			pageHeading = "CONTROL CARD INPUT LISTING";
			break;
		case 2:
			pageHeading = "G Study " + myTitle;
			break;
		case 3:
			pageHeading = "D Study " + myTitle;
			break;
		}
		pageHeading = center(pageHeading);
	}
	// ============== setPageType ============
	// end
	// ============== setPageTop ============
	// start

	private static String setPageTop() {

		LocalDateTime timePoint = LocalDateTime.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy");

		String dateOut = timePoint.format(formatter);

		subHead = "";

		StringBuilder sa = new StringBuilder();

		sa.append("   jGENOVA 1.0");
		sa.append(" ").append(dateOut);
		sa.append(String.format("%95s", "Page"));
		return sa.toString();

	}
	// ============== setPageTop ============
	// end
	// ============== addLine ============
	// start

	public static void addLine(int value) throws IOException {

		numLines = numLines + value;
		if (numLines > Sys.getLinesPerPage())  {
			sysPage();
		}
	}
	// ============== addLine ============
	// end
	// ============== testPage ============
	// start

	public static void testPage(int value) throws IOException {

		if (numLines + value > Sys.getLinesPerPage())  {
			sysPage();
		}
	}
	// ============== testPage ============
	// end	
	// ============== sysPage ============
	// start

	private static void sysPage() throws IOException {

		StringBuilder hold = new StringBuilder();
		numLines = 1;
		pageNum++;
		IO.mybw.newLine();
		if (Flags.ecology) {
			 hold.append(" ");
			 lineSkip(4);
		}
		else
			hold.append((char) 12);
		// end if else
		
		hold.append(pageTop).append(String.format("%8d%n", pageNum));

		IO.mybw.write(hold.toString());
		IO.doPrintLine(pageHeading,1);
		if (subHead.length() > 0)
			IO.doPrintLine(subHead, 2);
		else {
			if (tempHead.length() > 0)
				IO.doPrintLine(tempHead, 2);
			else 
				IO.doPrintLine(" ");
		}

	}
	// ============== sysPage ============
	// end
	// ============== printTitlePage ============
	// start

	public static void printTitlePage() throws IOException {
		String[] mytitle = { 
	"               GGGGGGGGGG   EEEEEEEEEEEE  NN        NN    OOOOOOOO    VV        VV   AAAAAAAAAA",
	"              GGGGGGGGGGGG  EEEEEEEEEEEE  NNN       NN   OOOOOOOOOO   VV        VV  AAAAAAAAAAAA",
	"              GG         G  EE            NNNN      NN  OO        OO  VV        VV  AA        AA",
	"          jj  GG            EE            NN NN     NN  OO        OO   VV      VV   AA        AA",
	"          jj  GG            EE            NN  NN    NN  OO        OO   VV      VV   AA        AA",
	"          jj  GG            EE            NN   NN   NN  OO        OO   VV      VV   AA        AA",
	"          jj  GG            EE            NN    NN  NN  OO        OO    VV    VV    AA        AA",
	"          jj  GG            EEEEEEEE      NN     NN NN  OO        OO    VV    VV    AA        AA",
	"          jj  GG      GGGG  EEEEEEEE      NN      NNNN  OO        OO    VV    VV    AAAAAAAAAAAA",
	"          jj  GG      GGGG  EE            NN       NNN  OO        OO     VV  VV     AAAAAAAAAAAA",
	"          jj  GG        GG  EE            NN        NN  OO        OO     VV  VV     AA        AA",
	"          jj  GG        GG  EE            NN        NN  OO        OO     VV  VV     AA        AA",
	"jj        jj  GG        GG  EE            NN        NN  OO        OO      V  V      AA        AA",
	" jj      jj   GG        GG  EE            NN        NN  OO        OO      VVVV      AA        AA",
	"  jj    jj    GGGGGGGGGGGG  EEEEEEEEEEEE  NN        NN   OOOOOOOOOO       VVVV      AA        AA",
	"   jjjjjj      GGGGGGGGGG   EEEEEEEEEEEE  NN        NN    OOOOOOOO         VV       AA        AA" };

		String s25 = "                         ";
		String s16 = "                ";
		String st = "";
		for (int i = 0; i < 16; i++) {
			IO.mybw.newLine();
			st = s16 + mytitle[i];
			IO.mybw.write(st);
		}
		String x = "A GENeralized Analysis Of VAriance System";
		String a = s25 + s25 + x;
		IO.mybw.newLine();
		IO.mybw.newLine();
		IO.mybw.write(center(x));
		IO.mybw.newLine();
		a = center("___                  _  __          ");
		IO.mybw.write(a);
		a = null;
		s25 = null;
		// clear myTitle
		a = center("jGENOVA IS A JAVA 8 program for Analysis of Variance");
		lineSkip(a, 2);

		a = center("and Generalizability Analysis with Balanced Designs");
		lineSkip(a);
		a = center("Authors");
		lineSkip(a, 2);
		a = center("Joe E. Crick, Ed.D.");
		lineSkip(a, 2);
		a = center("Chief Technology and Information Officer, Retired");
		lineSkip(a);
		a = center("National Board of Medical Examiners");
		lineSkip(a);
		a = center("Robert L. Brennan, Ed.D.");
		lineSkip(a, 2);
		a = center("Lindquist Chair, Emeritus");
		lineSkip(a);
		a = center("University of Iowa");
		lineSkip(a);
		a = center("Version 1.0");
		lineSkip(a, 2);
		a = center("January, 2021");
		lineSkip(a);
		a = center("jGenova has been checked for accuracy of output, however the authors");
		lineSkip(a);
		a = center("can make no assurances that the program is totally without error.");
		lineSkip(a);
		a = center("The FORTRAN version of GENOVA was developed in part under contract No. N00123-78-C-1206");
		lineSkip(a, 2);
		a = center("with the Navy Personnel Research and Development Center (NPRDC);");
		lineSkip(a);
		a = center("Robert L. Brennan Principal Investigator.  jGenova does not necessarily");
		lineSkip(a);
		a = center(" reflect NPRDC positions or policy, and no official endorsement should be inferred.");
		lineSkip(a);

		// convert to string builder and clear out strings when done
		PR.setNumLines(200);
		IO.mybw.flush();
	}
	// ============== printTitlePage ============
	// end
	// ============== lineSkip ============
	// start

	private static void lineSkip(String str1, int lines) throws IOException {
		for (int k = 0; k < lines; k++) {
			IO.mybw.newLine();
		} // end for
		IO.mybw.write(str1);
	}
	// ============== lineSkip ============
	// end
	// ============== bugLine ============
	// start

	public static void bugLine(String str1) throws IOException {
		IO.mybw.newLine();
		IO.mybw.write(str1);
	}
	// ============== bugLine ============
	// end
	// ============== lineSkip ============
	// start

	private static void lineSkip(String str1) throws IOException {

		IO.mybw.newLine();
		IO.mybw.write(str1);
	}
	// ============== lineSkip ============
	// end
	// ============== lineSkip ============
	// start

	public static void lineSkip(int lines) throws IOException {

		PR.addLine(lines);
		for (int k = 0; k < lines; k++) {
			IO.mybw.newLine();
		} // end for
	}
	// ============== lineSkip ============
	// end
	// ============== setMyTitle ============
	// start

	public static void setMyTitle(String myTitle) {
		if (myTitle.length() > 100) {
			myTitle = myTitle.substring(0,100);
		}
		PR.myTitle = myTitle;
	}
	// ============== setMyTitle ============
	// end
	// ============== getNumLines ============
	// start

	public static int getNumLines() {
		return numLines;
	}
	// ============== getNumLines ============
	// end
	// ============== clearSubHead ============
	// start

	public static void clearSubHead() {
		PR.subHead = "";
	}
	// ============== clearSubHead ============
	// end
	// ============== setSubHead ============
	// start

	public static void setSubHead(String value) throws IOException {
		PR.subHead = center(value);
	}
	// ============== setSubHead ============
	// end
	// ============== setTempHead ============
	// start

	public static void setTempHead(String value) throws IOException {
		PR.setNumLines(200);
		PR.tempHead = center(value);
	}
	// ============== setTempHead ============
	// end
	// ============== clearTempHead ============
	// start

	public static void clearTempHead() {
		PR.tempHead = "";
	}
	// ============== clearTempHead ============
	// end
	// ============== setNumLines ============
	// start

	public static void setNumLines(int numLines) {
		PR.numLines = numLines;
	}
	// ============== setNumLines ============
	// end

}	// end Class PR
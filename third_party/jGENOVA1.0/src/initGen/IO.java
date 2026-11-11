package initGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.File;

public class IO {
	/**
	 * Myio has methods to get and set input, output, data temp files plus
	 * routine to print a line to the output file and retrieve EOF plus get a
	 * line from the input file
	 *
	 
	 */

	private static BufferedReader cardFile = null;
	private static BufferedReader dataFile = null;
	private static BufferedWriter printFile = null;
	private static boolean inEOF;
	private static boolean dataEOF;


	public static BufferedReader mybr; // standard input file
	public static BufferedWriter mybw; // standard output file
	public static BufferedReader mybdr; // current data file reader

	// ============= inputFileNames ==============
	// start

	public static void inputFileNames(String[] args, 
			String[] defaultArgs) 
			throws IOException {

		// Scanner scan = new Scanner(System.in);
		int howMany = args.length;
		System.out.println(howMany + "howmany");
		String [] callArgs = new String [3];
		
		//	if howMany > 0 then file names are
		//	being input from the command line
		//	and not from the console input stream
		//
		callArgs[0] = defaultArgs[0];
		callArgs[1] = defaultArgs[1];
		callArgs[2] = defaultArgs[2];
		if (howMany > 0) {
			System.out.println(  howMany + args[0]);
			callArgs[0] = args[0];
		}
		if (howMany > 1)	{
			System.out.println(  howMany + args[1]);
			callArgs[1] = args[1];
		}
		if (howMany > 2)	{
			System.out.println(  howMany + args[2]);
			callArgs[2] = args[2];
		}
			
		//	if 2 or more arguments then both
		//	input and output files specified
	
	
		processFromCommand(callArgs);
//		doPrintLine(" number of arguments = " + howMany);
		return;
		
		}	 

	
	// ============= inputFileNames ==============
	// end
	// ============= processFromCommand ==========
	// start

	private static void processFromCommand(String[] args)
			throws IOException {
		
	;
		setPrintFile(args[1]);
		setConsoleCardFile(args[0]);
		
	}
	// ============= processFromCommand ==========
	// end
	// ============= doPrintLine    ==============
	// start

	public static void doPrintLine(String st1, int lines) throws IOException {
		PR.addLine(lines);
		if (lines < 2)
			doPrintLine(st1);
		else {
			for (int k = 1; k < lines; k++) {
				mybw.newLine();
			} // end for
			doPrintLine(st1);
		} // end else

	} // end doPrintLine
	// ============= doPrintLine ==============
	// end
	// ============= doPrintLine ==============
	// start

	public static void doPrintLine(String st1) throws IOException {
		try {
			PR.addLine(1);
			mybw.newLine();
			mybw.write(st1);
		} catch (IOException e) {
			
			System.out.println("File not Found - try again");
		} // end catch
	}
	// ============= doPrintLine ==============
	// end
	// ============= getData ==============
	// start

	public static String getData()

	{
		String dataImage = null;
		dataEOF = false;
		try {
			dataImage = IO.mybdr.readLine();
		} catch (IOException ex) {
			dataEOF = true;
		} finally {
			if (dataImage == null) {
				dataEOF = true;
			}
		}
		return dataImage;
	}

	// ============= getData ==============
	// end
	// ============= getCard ==============
	// start

	public static String getCard()

	{
		String cardImage = null;
		inEOF = false;
		try {
			cardImage = IO.mybr.readLine();
		} catch (IOException ex) {
			inEOF = true;
		} finally {
			if (cardImage == null) {
				inEOF = true;
			}
		}
// 2.0 restrict control card image to 120 characters
		if (cardImage.length() > 120) {
			cardImage = cardImage.substring(0,120);  
		}
		return cardImage;
	}
	// ============= getCard ==============
	// end
	// ============= getEOF ==============
	// start

	public static boolean getEOF() {
		return inEOF;
	}
	// ============= getEOF ==============
	// end
	// ============= getDataEOF ==============
	// start

	public static boolean getDataEOF() {
		return dataEOF;
	}
	// ============= getDataEOF ==============
	// end
	// ============= setConsoleCardFile ======
	// start
	
	private static void setConsoleCardFile (String fileName) 
			throws IOException {
		
//		boolean fileNotfound = true;
		
		FileInputStream fileIS = null;
		InputStreamReader fileIR = null;
		BufferedReader bufIn = null;

		try {
			fileIS = new FileInputStream(fileName);
			} // end try
			catch (FileNotFoundException e) {
				// `
				Sys.setErrorMsg("Control cards not found" + fileName);
				
			} // end catch

		fileIR = new InputStreamReader(fileIS);
		bufIn = new BufferedReader(fileIR);

		cardFile = bufIn;
		mybr = cardFile;

		// by default the data file source is the control card file
		// unless updated by a control card to the contrary
		
		setDataToCard();
	}
	// ============= setConsoleCardFile ======
	// end
	// ============= setCardFile ==============
	// start

	// ============= setDataToCard ==============
	// start

	public static void setDataToCard() {
		// set dataFile to cardFile
		dataFile = cardFile;
		mybdr = mybr;
	}
	// ============= setDataToCard ==============
	// end
	// ============= setDataFile   ==============
	// start
	
	public static void setDataFile(String fileName) {

		// boolean fileNotfound = true;

		FileInputStream fileIS = null;
		InputStreamReader fileIR = null;
		BufferedReader bufIn = null;

		// fileNotfound = false;

		try {
			fileIS = new FileInputStream(fileName);
		} // end try
		catch (FileNotFoundException e) {
			//
			try {
				Sys.setErrorMsg(" Data File " + fileName + " not Found!!");
			} catch (IOException g) {
				System.out.println(" problems writing to error" + " message in IO setDataFile");
			}

		} // end catch

		fileIR = new InputStreamReader(fileIS);
		bufIn = new BufferedReader(fileIR);
		dataFile = bufIn;
		mybdr = dataFile;

		// by default the data file source is the control card file
		// unless updated by a control card to the contrary
	}
	// ============= setDataFile    ==============
	// end
	// ============= setPrintFile   ==============
	// start

	public static void setPrintFile(String fileText) throws IOException {

		boolean fileNotOK = true;
		File fin = null;
		fileNotOK = false;
		fin = new File(fileText);
		if (!fin.exists()) {
			try {
				
				fin.createNewFile();
			} catch (Exception e) {
				//  Unable to create output file
				fileNotOK = true;
			} // END CATCH
			finally {
				if (fileNotOK) {
					Sys.setErrorMsg(" unable to locate or create this file");
				} // end if
			} // end finally
		} // end not exists

		BufferedWriter buffOut = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter(fin, false);
		} catch (FileNotFoundException e) {
			
			Sys.setErrorMsg(" Error creating print file");
		}

		buffOut = new BufferedWriter(fw);

		printFile = buffOut;
		printFile.newLine();
		printFile.newLine();
		printFile.flush();
		mybw = printFile;
	}
	// ============= setPrintFile ==============
	// end

}	// end Class IO
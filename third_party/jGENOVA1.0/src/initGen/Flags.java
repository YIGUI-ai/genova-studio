package initGen;

public class Flags {

	// internal developer use only

	public static boolean debug;

	public static boolean printNegative;
	public static boolean EMS;
	public static boolean corrTable;
	public static boolean ecology;
	public static boolean dstudyTable;
	public static boolean printRecords;
	public static boolean dStudyCut;

	// GSTUDY Related flags
	public static boolean printMsquare;
	public static boolean nonRandom;
	public static boolean name;
	public static boolean grandMean;
	public static boolean dstudyPossible;

	public static boolean anyMean;
	public static boolean[] printMean = new boolean[7];

	public static boolean format;

	// GPROC related flags
	public static boolean defaultDstudy;

	public static boolean gStudy;
	public static boolean gComponents;
	public static boolean gMSquare;

	// control first and subsequent setups
	// and initializations in class G and D
	public static boolean populateG;
	public static boolean populateD;

	// used in GCOMP and GMSQ
	public static boolean inMain;

	/**
	 * Boolean Variables defined in and referenced from class Flags
	 * 
	 * =========== Flags set by OPTION card Parameters
	 * 
	 * debug followed by ON or OFF determines whether debug extended output is
	 * produced 

	 * 
	 * printNegative EMS corrTable ecology dstudyTable printRecords
	 * 
	 * ==================== GSTUDY Related Flags
	 * 
	 * printMsquare nonRandom name grandMean dstudyPossible anyMean intMean =
	 * new boolean[6]
	 * 
	 * ==================== GPROC Related Flags
	 * 
	 * defaultDstudy
	 * 
	 * @return
	 */
	// ============= sysDefaultFlags ==============
	// start

	public static void sysDefaultFlags() {

		printMean = new boolean[7];
		printRecords = true;
		debug = false;

		printRecords = true;
		// process run
		gStudy = false;
		gComponents = false;
		gMSquare = false;
		populateG = false;
		populateD = false;
	}
	// ============= sysDefaultFlags ==============
	// end
	// ============= setOptionInit ==============
	// start

	public static void setOptionInit() {
		printNegative = false;
		EMS = true;
		corrTable = false;
		ecology = false;
		dstudyTable = false;
		debug = false;
	}
	// ============= setOptionInit ==============
	// end

}	// end Class Flags

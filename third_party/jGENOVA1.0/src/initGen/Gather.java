package initGen;

import java.io.IOException;
import java.util.ArrayList;

public class Gather {

	private static int numCards;
	private static int processNum;
	private static ArrayList<String> tokenHold;
	private static ArrayList<String> cardImage;
	private static ArrayList<Integer> cardValue;
	private static ArrayList<Integer> firstToken;
	private static int[] numTokens = new int[100];

	// used internally

	private static int totalTokens;
	private static int keyLength;
	private static int howMany;
	private static boolean setDone;
	private static boolean cardOK;

	// ============= processSet ==============
	// start

	public static void processSet() throws IOException {
		// TODO Auto-generated method stub
		//
		/***
		 * processSet This method gathers a set of control cards for a
		 * process 1 = GSTUDY or STUDY 2 = DSTUDY 3 = GMEANSQUARES 4 =
		 * GCOMPONENTS 5 = FINISH
		 *
		 * sets/updates numCards
		 *
		 * public methods in Gather:
		 *
		 * public static void processSet () public static int getNumCards()
		 * public static int getProcessNum() public static int[] geFirstToken()
		 * public static int[] getCardValue() public static int[] getNmTokens()
		 * public static String [] getCardImage() public static String []
		 * getTokenHold()
		 *
		 * static (not public) methods in Gather:
		 *
		 * static boolean checkEmpty(String controlCard) static boolean
		 * first) static boolean checkValid(int cardNumb) static boolean
		 * checkZero(int numb, int type, int first) static boolean
		 * validInProcess (int numb, int process, int type, int first) static
		 * String scrunchName(String [] tokens) static String
		 * scrunchTitle(String card,int keyLength) static String[]
		 * shrinkTokens(String [] tokens, int loop) static void
		 * processRegular(int numb, String[] tokens)
		 *
		 */

		cardImage = new ArrayList<String>();
		tokenHold = new ArrayList<String>();
		cardValue = new ArrayList<Integer>();
		firstToken = new ArrayList<Integer>();

		int lastCard = 0;
		int firstCard = 0;
		int cardType = 0;
		int cardNumb = 0;
		int lastCardNumb = -1;
		int lastNonContinue= 0; // the last numCards
							// value that was not a
							// continuation

		String controlCard = null;
		String[] tokens = null;
		String keyword;

		setDone = false;
		cardOK = false;
		numCards = 0;
		processNum = 0;
		totalTokens = 0;
		tokenHold = new ArrayList<String>();

		//
		// Start while not setDone
		//
		while (!setDone) { // Start While not setDone
			
	
			controlCard = IO.getCard(); // 
			cardOK = true;
			
			// check for end of file
			
			if (IO.getEOF()) {
				numCards++;
				controlCard = " EOF encountered => Fatal!";
				cardImage.add(controlCard);
				processNum = 6;
				break;
 			}
			
			// check for empty card

			if (checkEmpty(controlCard)) {
				controlCard = " Empty control card ignored!";
				numCards++;
				cardImage.add(controlCard);
				cardValue.add(1);
				numTokens[numCards - 1] = 0;
				firstToken.add(totalTokens);
				continue;
			}

			numCards++;
			controlCard = controlCard.toUpperCase();
			cardImage.add(controlCard);
			controlCard = controlCard.trim();

			tokens = controlCard.split(Sys.delimiters, 0);
			//
			// Tokens will contain 0 length elements when
			// multiple banks occur
			//
			howMany = tokens.length;
			keyword = tokens[0];

			cardNumb = Crd.getCard(keyword);
		
			if (cardNumb > 0) {
				lastCardNumb = cardNumb;
				lastNonContinue = numCards;
			}
			cardValue.add(cardNumb);
			cardType = Crd.getType(cardNumb);
			lastCard = Crd.getLast(cardNumb);
			firstCard = Crd.getFirst(cardNumb);
			keyLength = keyword.length();
			//
			// check for valid control card number
			//
			if (checkValid(cardNumb, controlCard)) {
				break;
			}

			//
			// check for control card being valid in current process
			//
			if (!validInProcess(cardNumb, processNum, cardType, firstCard)) {
				break;
			} // end if ! validInProcess
			//
			// Ready to finish process for q good control card

			if (cardOK) { // start of cardOK == true
		
				switch (cardNumb) {

				case 0:
					if (howMany < 1) {
						break;
					}
					if (!  Crd.okToContinue(lastCardNumb)) {
						break;
					} // end of not Crd.okToContinue
					else {

						// continuation has parameters
						
						// firstToken.add(totalTokens);
						for (int k = 1; k < howMany; k++) {
							if (tokens[k].length() > 0) {
								tokenHold.add(tokens[k]);
								numTokens[lastNonContinue - 1]++;
								totalTokens++;
							} // end if
	
								
						} // end for k
						firstToken.add(totalTokens - 1);
						numTokens[numCards - 1] = 0;
					} // end else

					break;

				case 1:
					// do comment card

					numTokens[numCards - 1] = 0;
					firstToken.add(totalTokens);
					break;

				default: // start of else cardNumb > 1

					if (Crd.getTitle(cardNumb)) {
						if (howMany < 2)  {
							flushBuffer();
							Sys.setErrorMsg(" description expected on this " + "control card = stop processing");
						}
						else
							howMany = 2;
						tokens[1] = scrunchTitle(controlCard, keyLength);
					} // end if Crd.getTitle

					processRegular(cardNumb, tokens);
					break;

				} // end of switch
				//
				// now check for lastCard
				//
				if (cardType > 0) {
					if (lastCard == processNum) {
						setDone = true;
					} // end if lastCard
				} // end f cardType

			} // end if cardOK = true;

		} // end do while

	}

	// ============= processSet ==============
	// end
	// ============= processRegular ==============
	// start

	private static void processRegular(int numb, String[] tokens) throws IOException {
		firstToken.add(totalTokens);
		if (numb == 15) {
			tokens = shrinkTokens(tokens, howMany);

		}
		if (numb == 15 & howMany > 3) {
			// here is a NAME card that must have description packed
			tokens[2] = scrunchName(tokens);

			howMany = 3;
		}
		int actual = 0;
		String hold = null;
		for (int i = 1; i < howMany; i++) {
			hold = tokens[i];
			if (hold.length() > 0) {
				actual++;
				tokenHold.add(hold);
			} // end if length > 0

		} // end for i
		numTokens[numCards - 1] = actual;
		totalTokens = totalTokens + actual;
	} // end of processRegular

	// ============= processRegular ==============
	// end
	// ============= shrinkTokens ==============
	// start
	private static String[] shrinkTokens(String[] tokens, int loop) {
		int actual = 0;
		String hold;
		for (int k = 0; k < loop; k++) {

			hold = tokens[k];
			if (hold.length() > 0) {
				tokens[actual] = tokens[k];
				actual++;
			} // end for k
			howMany = actual;
		} // end for
		return tokens;
	}
	// ============= shrinkTokens ==============
	// end
	// ============= scrunchName ==============
	// start

	private static String scrunchName(String[] tokens) {

		StringBuilder sb = new StringBuilder(tokens[2]);
		for (int k = 3; k < howMany; k++) {
			sb.append(" ");
			sb.append(tokens[k]);
		} // end for k
		return sb.toString();
	}
	// ============= scrunchName ==============
	// end
	// ============= scrunchTitle ==============
	// start

	private static String scrunchTitle(String card, int keyLength) {
		String titleOut = card;

		// strip of keyword and trim the remains
		titleOut = titleOut.substring(keyLength);
		titleOut = titleOut.trim();
		return titleOut;
	}
	// ============= scrunchTitle ==============
	// end
	// ============= checkEmpty ==============
	// start

	private static boolean checkEmpty(String controlCard) throws IOException {

		boolean ifEmpty = false;
		if (controlCard.length() < 1) {
			ifEmpty = true;

		} // end if controlCard.length() < 1
		return ifEmpty;
	}
	// ============= checkEmpty ==============
	// end
	// ============= checkValid ==============
	// start

	private static boolean checkValid(int cardNumb, String cardImage) throws IOException {
		boolean ifValid = false;
		if (cardNumb < 0) {
			ifValid = true;
			IO.mybw.newLine();
			IO.mybw.write("  " + cardImage);
			flushBuffer();
			Sys.setErrorMsg(" Invalid control card found!");
			setDone = true;
			cardOK = false;

		} // end if cardNumb < 0
		return ifValid;
	}
	// ============= checkValid ==============
	// end
	// ============= validInProcess ==============
	// start

	private static boolean validInProcess(int numb, int process, int type, int first) throws IOException {

		boolean isOK;
		if (process == 0)
			isOK = checkZero(numb, type, first);
		else
			isOK = checkNonZero(process, type, first);
		return isOK;
	}
	// ============= validInProcess ==============
	// end
	// ============= checkZero ==============
	// start

	private static boolean checkZero(int numb, int type, int first) throws IOException {
		// here the processNum will be zero
		boolean isOK = true;
		if (type > 0) {
			if (first == type) {
				processNum = type;
			} // end of if first == type
			else {
				flushBuffer();
				Sys.setErrorMsg(" Control card invalid in this run");

				isOK = false;
			} // end else
		} // end type > 0

		// here type == 0
		if (numb == 15) { // NAME card must be in process > 0
			flushBuffer();
			Sys.setErrorMsg(" NAME card must be within a process run");
			isOK = false;
		} // end if numb = 15 == NAME card

		if (!isOK) {
			setDone = true;
			cardOK = false;
		}
		return isOK;
	} // end checkZero
	// ============= checkZero ==============
	// end
	// ============= checkNonZero ==============
	// start

	private static boolean checkNonZero(int process, int type, int first) throws IOException {
		// here the processNum will be zero
		boolean isOK = true;
		if (type > 0) { // if type < 0
			if (process != type) { // type not = ProcessNum
				flushBuffer();
				Sys.setErrorMsg(" Control card invalid in this run");

				isOK = false;
			} // end if process != ...
			else {
				if (first > 0) {
					flushBuffer();
					Sys.setErrorMsg("Control card invalid in this run");

					isOK = false;
				} // end eif first
			} // end else
		} // end type > 0
		if (!isOK) {
			setDone = true;
			cardOK = false;
		}
		return isOK;
	} // end checkNonZero

	// ============= checkNonZero ==============
	// end
	// ============= getProcessNum ==============
	// start

	public static int getProcessNum() {
		return processNum;
	}
	// ============= getProcessNum ==============
	// end
	// ============= getNumCards ==============
	// start

	public static int getNumCards() {
		return Gather.numCards;
	}
	// ============= getNumCards ==============
	// end
	// ============= getTokenHold ==============
	// start

	public static String[] getTokenHold() {
		String[] myTokens = tokenHold.stream().toArray(String[]::new);
		return myTokens;
	}
	// ============= getTokenHold ==============
	// end
	// ============= getCardImage ==============
	// start

	public static String[] getCardImage() {
		String[] myCards = cardImage.stream().toArray(String[]::new);
		return myCards;
	}
	// ============= getCardImage ==============
	// end
	// ============= getCardValue ==============
	// start

	public static int[] getCardValue() {
		int[] myValue = cardValue.stream().mapToInt(Integer::intValue).toArray();
		return myValue;
	}
	// ============= getCardValue ==============
	// end
	// ============= getNumTokens ==============
	// start

	public static int[] getNumTokens() {
		return numTokens;
	}
	// ============= getNumTokens ==============
	// end
	// ============= getFirstToken ==============
	// start

	public static int[] getFirstToken() {
		int[] myFirst = firstToken.stream().mapToInt(Integer::intValue).toArray();
		return myFirst;
	}
	// ============= getFirstToken ==============
	// end
	// ============= flushBuffer   ==============
	// start
	
	private static void flushBuffer()  throws IOException {
		
	//	Print out the unprocessed control cards in the
	// 		buffer when an error occurs in Gather
	
		for (int k = 0; k < numCards; k++) {
			IO.doPrintLine(" " + cardImage.get(k));
		}
		
		
	}
	// ============= flushBuffer   ==============
	// end

} // End of class Gather
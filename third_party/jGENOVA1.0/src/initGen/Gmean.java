package initGen;

import java.io.IOException;

public class Gmean {

	private static int[] firstToken;
	private static String[] myCards;
	private static String[] tokens;
	private static int[] numTokens;
	private static int[] cardValue;

	private static String[] cardTokens;
	private static int howMany;

	// ============== Gmean.process ============
	// start

	public static void process() throws IOException {

		// processes the control returned by Gather.processSet()

		tokens = Gather.getTokenHold();
		myCards = Gather.getCardImage();
		firstToken = Gather.getFirstToken();
		numTokens = Gather.getNumTokens();
		cardValue = Gather.getCardValue();
		int numCards = Gather.getNumCards();

		int currentCard = 0;
		cardTokens = new String[50];

		// ============= initializations related to GMSQ

		PR.setNumLines(200);
		Sys.setErrorMsg(null);
		G.gMsqInit();

		// set heading to control card listing

		for (int thisCard = 0; thisCard < numCards; thisCard++) {
			howMany = numTokens[thisCard];

			if (howMany > 0) {
				int hold = 0;
				int first = firstToken[thisCard];
				// move this cards' tokens into cardTokens
				for (int k = first; k < howMany + first; k++) {
					cardTokens[hold] = tokens[k];
					hold++;
				} // end for

			} // end if

			currentCard = cardValue[thisCard];
			if (currentCard == 19)
				currentCard = 13;
			IO.doPrintLine("  " + myCards[thisCard]);
			IO.mybw.flush();

			if (howMany < Crd.getMintokens(currentCard)) {

				Sys.setErrorMsg(" Not enough parameters for this control card");
			}
			switch (currentCard) {

			case 1: // just print out comment- done above
				break;
			case 8: // process an ENDMEAN
				PR.setPageType(2);	
				Mean.end(cardTokens, howMany);
				break;
			case 12: // process GMSQ card
				G.gMsqInit();
				processGMean(cardTokens[0]);
				break;
			case 14: // MEANSQUARES card here
				Valinp.msquareCard(cardTokens, howMany);
				break;
			case 15: // process Name Card
				Name.processName(cardTokens[0], cardTokens[1]);
				break;
			case 16: // process options card OK
				Opt.processOptions(cardTokens, 0, howMany);
				break;
			case 17: // process OUTPUT card
				U.setOutputtFile(cardTokens[0]);
				break;
			case 20: // process Sub heading card
				U.saveSubTitle(cardTokens[0]);
				break;

			}// end Switch - check for error here

		} // end for

	} // end process

	// ============== Gmean.process ============
	// end	
	// ============== processGMean ============
	// start

	private static void processGMean(String heading) {
		// set title to this token
		PR.setMyTitle(heading);
	} // end ProcessGMean
	// ============== processGMean ============
	// end

}	// end Class Gmean

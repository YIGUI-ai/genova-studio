package initGen;

import java.io.IOException;

public class Dstudy {

	private static int[] firstToken;
	private static String[] myCards;
	private static String[] tokens;
	private static int[] numTokens;
	private static int[] cardValue;

	private static String[] cardTokens;
	private static int howMany;
	
	// =================== Dstudy.process =======
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

		// ============= initializations related to GSTUDY

		PR.setNumLines(200);
		if (Sys.lastRun == 1)
			D.dInit();

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
				IO.mybw.flush();
				Sys.setErrorMsg(" Not enough parameters for this control card");
			}
			switch (currentCard) {

			case 1: // just print out comment- done above
				break;
			case 2: // DCUT card
				Dproc.dCut(cardTokens, howMany);
				IO.mybw.newLine();
				break;
			case 3: // DEFFECT card
				Deffect.process(cardTokens, howMany);
				IO.mybw.newLine();
				break;
			case 4: // DSTUDY card
				Dproc.go(cardTokens[0]);
				break;

			case 7: // process ENDDSTUDY
				if (howMany > 0)
					Dproc.getGrand(cardTokens[0], howMany);
				if (Flags.name)
					Name.printNames();
				Dproc.end();
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

			} // end Switch - check for error here
		} // end for

	} // end process

	// =================== Dstudy.process =======
	// end
	
}	// end Class Dstudy

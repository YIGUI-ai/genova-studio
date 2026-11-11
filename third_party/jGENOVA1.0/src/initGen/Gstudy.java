package initGen;

import java.io.IOException;
	
public class Gstudy {

	private static int[] firstToken;
	private static String[] myCards;
	private static String[] tokens;
	private static int[] numTokens;
	private static int[] cardValue;

	private static String[] cardTokens;
	private static int howMany;

	// ============== Gstudy.process ============
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
		G.gstudyInit();

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
			if (currentCard == 19) // if STUDY
				currentCard = 13;  // then GSTUDY
			IO.doPrintLine("  " + myCards[thisCard]);
			IO.mybw.flush();

			if (howMany < Crd.getMintokens(currentCard)) {

				Sys.setErrorMsg(" Not enough parameters for this control card");
			}
			switch (currentCard) {

			case 1: // just print out comment- done above
				break;
			case 5: // process an EFFECT card OK
				Effect.processEffect(cardTokens, howMany);
				break;
			case 10: // process Format Card
				setFormat(cardTokens, howMany);
				Flags.format = true;
				break;
			case 13: // process Gstudy or Study card OK
				G.gstudyInit();
				processGstudy(cardTokens[0]);
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
			case 18: // PROCESS card Here

				Gproc.startProcess(cardTokens, howMany);
				doGstudy();
				break;

			} // end Switch - check for error here
		} // end for

	} // end process

	// ============== Gstudy.process ============
	// end
	// ============== setFormat ============
	// start
	private static void setFormat(String[] cardTokens, int howMany) throws IOException {
		if (howMany != 2)
			Sys.setErrorMsg(" Wrong number of parameters " + "on a Format not = 2 ");
		try {
			G.fSkip = Integer.parseInt(cardTokens[0]);
			G.fWidth = Integer.parseInt(cardTokens[1]);

		} catch (NumberFormatException e) {
			Sys.setErrorMsg(" parameters on Format card not " + "an integer = exit");
		} // end catch
	} // end setFormat
	// ============== setFormat ============
	// end
	// ============== processGstudy ============
	// start

	private static void processGstudy(String heading) {
		// set title to this token
		PR.setMyTitle(heading);
		G.numMain = 0;
		//	reset  default data source to input stream
		IO.setDataToCard();
	} // end ProcessGstudy
	// ============== processGstudy ============
	// end
	// =================== doGstudy =======
	// start

	private static void doGstudy() throws IOException {
		// this is the start of GPROC
		PR.setPageType(2);
		PR.setNumLines(200);
		
		
		G.initExecute();


		if (Flags.name)
			Name.printNames();
		IDset.setID();

		IDset.setDegrees();
		PrintID.printID();
		G.passNumber = 0;
		IO.mybw.flush();
		Pass1.gSetup();
		Gphase1.go();

		G.passNumber = 1;

		Pass1.gSetup();

		Gphase1.go();
		Gphase2.go();
		Gphase3.go();

		Gprint.go();

		PrG.ems();
		PrG.vcov();

		if (Flags.defaultDstudy) {
			D.defaultDstudy();
			Dproc.end();
			Flags.defaultDstudy = false;
		}

		Flags.gStudy = false;
		Flags.dstudyPossible = true;
		IO.mybw.flush();
		PR.setNumLines(100);
		PR.setPageType(1);

	} // end doGstudy
	// =================== doGstudy =======
	// end

}	// end Class Gstudy

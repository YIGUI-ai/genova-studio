package initGen;

import java.io.BufferedReader;
import java.io.IOException;

public class GENOVA {
	
	private static int processNum = 0;
	public static BufferedReader CardFile = null;
	
	public static void main(String[] args) 
		throws IOException {
	
	// This is the new GENOVA Mainline
		
	//  Method to initialize a whole lot of stuff

		// ==================  Initialization components

		Sys.sysDefaaultValues();	
		Flags.sysDefaultFlags();
		Flags.setOptionInit();
		PR.Init();

		String [] defaultArgs = new String [3];
	
		defaultArgs[0] = "c:\\g\\jGenInp.txt";
		defaultArgs[1] = "c:\\g\\jGenOut.txt";
		
		IO.inputFileNames(args, defaultArgs);
		PR.printTitlePage();
		IO.mybw.flush();
		
		IO.mybw.newLine();
			
		boolean okSoFar = true;
		while ( okSoFar) {
			
			Gather.processSet();
			processNum = Gather.getProcessNum();
			PR.setPageType(1);
			PR.setNumLines(200);
			

			switch (processNum) {
				
			case 1: Gstudy.process();
		//			PR.addLine(2);
					break;
			case 2:  Dstudy.process();
					break;
			case 3:	 // processMeanSquare; 
					Gmean.process();
					break;
			case 4: // process GComponents 
					Gcomp.process(); 
					break;
			case 5: // print finish flush all and quit;
					IO.doPrintLine(" FINISH");
					okSoFar= false;
					break;
			case 6:	// fatal error flush control cards
					flushControlCards();
					break;
			default: 
					Sys.setErrorMsg("Control cards do not"
							+ " form a valid set - abort");
					okSoFar = false;
					break;	
				}  // end switch
	
		} // end while okSoFar

		IO.doPrintLine(" jGENOVA has exited!!");
		System.out.println(" jGENOVA has exited!!");
		IO.mybw.flush();
		System.exit(0);				
	}

	private static void flushControlCards()
		throws IOException {
		String[] myCards = Gather.getCardImage();

		for (int j = 0; j< Gather.getNumCards(); j++) {
			IO.doPrintLine(myCards[j]);
			IO.mybw.flush();
		}  // end for j
		Sys.setErrorMsg(" GENOVA will  exit!!");
		
		IO.mybw.flush();

	}	// end flushControlCards	
			
}	// end Mainline GENOVA
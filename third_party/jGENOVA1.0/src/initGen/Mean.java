package initGen;

import java.io.IOException;

public class Mean {
	
	public static StringBuilder sa;
	public static StringBuilder sb;
	public static StringBuilder sc;
	public static StringBuilder sd;
///	==============  Mean.end          ============
//	start

	public static void end(String [] tokens, int howMany)
			throws IOException {	// 1

		if (Flags.inMain)
			Sys.setErrorMsg(" ENDMEAN card found before "
					+ "interactions effect MEANSQ cards");

		if (howMany > 3)
			Sys.setErrorMsg(" too many arguments on"
					+ " an ENDMEAN card");
		if (G.numMain < 2)
			Sys.setErrorMsg("  Less than 2 main effect VCOMP"
					+ " cards before an ENDMEAN crd ");
		
		PR.setNumLines(100);
		Flags.defaultDstudy = false;
		switch(howMany)  {	//	2
			case 1 : 
				checkDefault(tokens[0]);
				Sys.setWarning(" NO GrandMean fond after slash "
						+ "on ENDMEAN - Default D Study inhibited");
				Flags.defaultDstudy = false;
				break; 
			case 2:
				checkGrand(tokens,0);
				break;
			case 3:
				checkDefault(tokens[0]);
				checkGrand(tokens,1);
				break;
			default:// here there are no parameters	
				break;
		} // end switch	//	-2
		if (Flags.nonRandom)
			checkAllFixed();
		checkForComplete();
		if (Flags.name)
			Name.printNames();
		
		PrintID.printID();
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
		// finish up 
		Flags.printMsquare = false;
		Flags.dstudyPossible = true;
		PR.setPageType(1);
		
	}
//	==============  Mean.end          ============
//	end//	end

//	==============  checkForComplete ============
//	start	
	private static void checkForComplete()
			throws IOException {	
		double xdiff = 1.0d * Math.pow(10, -6);
		boolean missing = false;
		for (int k = G.numMain + 1; k<= G.numTotal; k++){
			if (G.xBarb[k] <= xdiff)	{
				Sys.setWarning(" MEANSQUARE card "
					+ "missing for " + G.alphaID[k]);
				missing = true;
			}	// end if
		}	// end for
		if (missing)
			Sys.setErrorMsg("  Processing will cease");
	}
	
//	==============  checkForComplete ============
//	end
//	==============  checkAllFixed     ============
//	start	
	private static void checkAllFixed()
			throws IOException {
		int j1 = 0;
		loop10:
		for (int k = 1; k <= G.numMain; k++)	{
			if (G.mainUniverse[k] == 0)
				continue loop10;
			if (G.mainLevel[k] == G.mainUniverse[k]) 
				j1++;
		}
		if (j1 == G.numMain)
			Sys.setErrorMsg(" All facets are fixed - "
				+ "processing ceases");
		
	}	// end checkAllFixed	
//	==============  checkAllFixed     ============
//	end	
//	==============  checkGrand        ============
//	start			

	private static void checkGrand(String[] tokens,
			int first) 
					throws IOException {
		if ( ! U.ifSlash(tokens[first]))
			Sys.setErrorMsg(" first of 2 parameters "
					+ "not = '/'");
		else
			// here should be a grand Mean
			G.grandMean = toDouble(tokens[first + 1]);
		
	}	// end checkGrand
//	==============  checkGrand          ============
//	end			
//	==============  checkDefault        ============
//	start			
	
	private static void checkDefault(String inString) 
			throws IOException {	

		if (U.isDefault(inString))
			G.irStar = 1;
		else 
			Sys.setErrorMsg(" Single parameter on"
					+ " an ENDMEAN card not 'DEFAULT'");
		
	}	// end checkDefault
//	==============  checkDefault        ============
//	end	
//	================== toDouble          ==============
//	start
	private static double toDouble(String token) 
			throws NumberFormatException, 	
			 IOException {
		
	   double d = 0.0d;
	    try
	    {
	      // the String to double conversion happens here
	    	d = Double.parseDouble(token);
	    }	// end try
	    catch (NumberFormatException nfe)
	    {
	  		Sys.setErrorMsg(" GrandMean parameter on an "
	  				+ "ENCCoMP  card not a numeric value");    
	    }	// end catch
	
	    return d;
	}
//	================== toDouble          ==============
//	end	

}	//	end class Mean

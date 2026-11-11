package initGen;

import java.util.Arrays;

public class Crd {
	

	/**
	 * The Crd class contains the following
	 * constants:
	 * 
	 * CARD - a string vector containing the first 
	 * 4-characters of a jGENOVA control card
	 * 
	 * CARDTYPE - an integer vector containing the type of control
	 * card corresponding value ion CARD
	 * 
	 * The Crd class contains the following
	 * methods:
	 * 
	 * int getCard (String str) will return the index of 
	 * the string matched in CARDS or -1 if not found
	 * 
	 * int getType (int j) will return the  value of 
	 * TYPE[j] if j is in range else -1
	 * 
	 */
	private static final int[] TITLE = {4, 11, 12,
			 	13, 19, 20 };
 
	private static final int[] CONTINUE = {2, 3,
			 5, 7, 14, 16, 21};
		
	private static final  String[] CARDVAL = 
	   {"-   ",
		"COMM", "DCUT", "DEFF", "DSTU", "EFFE",
		"ENDC", "ENDD", "ENDM", "FINI", "FORM",
		"GCOM", "GMEA", "GSTU", "MEAN", "NAME",
		"OPTI", "OUTP", "PROC", "STUD", "SUBH", 
		"VCOM"};
			 
	private static final int [] CARDTYPE = 
	   {0,
		0, 2, 2, 2, 1,
		4, 2, 3, 5, 1,
		4, 3, 1, 3, 0, 
		0, 0, 1, 1, 0, 
		4};
		 
	private static final int [] CARDFIRST = 
	   {0,
		0, 0, 0, 2, 0,
		0,  0, 0, 5, 0,
		4, 3, 1, 0, 0, 
		0, 0, 0, 1, 0,
		0};
		 
	private static final int [] MINTOKENS = 
	   {0,
		0, 0, 0, 1, 2,
		0, 0, 0, 0, 1,
		1, 1, 1, 1, 2,
		1, 1, 0, 1, 1,
		2};
			 
	private static final int [] CARDLAST = 
	   {0,
		0, 0, 0, 0, 0,
		4, 2, 3, 5, 0,
		0, 0, 0, 0, 0,
		0, 0, 1, 0, 0,
		0};
	
//	==============  Crd.getCard         ============
//	start

	public static int getCard ( String inString) {	
			
		int keyNumb = -1;
		boolean b1 = false;
		String myString = inString.trim() + "   ";
		myString = myString.toUpperCase();
		myString = myString.substring(0,4);
				
		for (int k = 0; k < CARDVAL.length;k++){
			String compare = myString.toUpperCase();
			b1 =  compare.startsWith(CARDVAL[k]);

			if (b1) {
				keyNumb = k;
				break;
			}	// end if
		}	// end for k
		return keyNumb;
	}
//	==============  Crd.getCard         ============
//	end
//	==============  Crd.getFirst         ============
//	start
			
	public static int getFirst(int numb) {
		if (numb >=0 & numb < CARDVAL.length) {
					 return CARDFIRST[numb];
		}
		else 
			return -1;
	}
//	==============  Crd.getFirst         ============
//	end
//	==============  Crd.getLast         ============
//	start

	public static int getLast(int numb) {		
		if (numb >=0 & numb < CARDVAL.length) {
			return CARDLAST[numb];
		}
		else 
			return -1; 
	}		
//	==============  Crd.getLast         ============
//	end
//	==============  Crd.getNumTokens     ============
//	start
			 
	public static int getMintokens(int index) {
		return MINTOKENS[index];	
	}
//	==============  Crd.getNumTokens     ============
//	end
//	==============  Crd.getTitle         ============
//	start
	 
	public static boolean getTitle(int numb) {
		return Arrays.stream(TITLE).anyMatch
				(i -> i == numb);				
	}
//	==============  Crd.getTitle         ============
//	end
//	==============  Crd.getType         ============
//	start

	public static int getType (int numb) {	
		if (numb >=0 & numb < CARDVAL.length) {
			return CARDTYPE[numb];
		}
		else
			return -1;	
	}
//	==============  Crd.getType         ============
//	end
//	==============  Crd.okToContinue     ============
//	start
		
	public static boolean okToContinue(int numb) {
		 return Arrays.stream(CONTINUE).anyMatch
				(i -> i == numb);				
	}	
//	==============  Crd.okToContinue     ============
//	end
			
}	// end Class Crd

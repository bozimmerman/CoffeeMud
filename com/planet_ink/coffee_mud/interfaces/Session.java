package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import java.io.IOException;
public interface Session
{
	public void rawPrintln(String msg);
	public void rawPrint(String msg);
	
	public void stdPrint(String msg);
	public void stdPrint(Environmental Source,
					  Environmental Target,
					  String msg);
	public void stdPrint(String msg,
						 int Length,
						 String msgEnd);
	public void stdPrintln(String msg);
	public void stdPrintln(Environmental Source,
						   Environmental Target,
						   String msg);
	
	public void print(String msg);
	public void print(Environmental Source,
					  Environmental Target,
					  String msg);
	public void print(String msg,
					  int Length,
					  String msgEnd);
	public void println(String msg);
	public void println(Environmental Source,
						Environmental Target,
						String msg);
	
	
	public void unfilteredPrintln(String msg);
	public void unfilteredPrint(String msg);
	
	public void colorOnlyPrintln(String msg);
	public void colorOnlyPrint(String msg);
	
	public String prompt(String Message, String Default)
		throws IOException;
	public String prompt(String Message)
		throws IOException;
	public boolean confirm(String Message, String Default)
	throws IOException;
	public String choose(String Message, String Choices, String Default)
	throws IOException;
	
	public void cmdExit(MOB mob, Vector commands)
		throws Exception;
	public void logoff();
	public boolean killFlag();
	public void setKillFlag(boolean truefalse);
	
	public String blockingIn()
		throws IOException;
	public String readlineContinue()
		throws IOException;
	public String filter(Environmental source,
						 Environmental target,
						 String msg,
						 boolean wrapOnly);


	public void enque(int tickDown, Vector commands);
	public Vector deque();
	public Vector previousCMD();
	
	public MOB mob();
	public void setMob(MOB newmob);
	
	public void setTermID(int tid);
	public int getTermID();
	
	public String getAddress();
}

package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class EnglishParser
{
	private EnglishParser(){};
	
	// these should be checked after pmap prelim check.
	private String[] articles={"a","an","the","some","of","pair"};
	private String[] universalStarters={
		"go ",
		"go and ",
		"i want you to ",
		"i command you to ",
		"i order you to ",
		"you are commanded to ",
		"please ",
		"to ",
	    "you are ordered to "};
	
	//codes:
	//%m mob name (anyone)
	//%i item name (anything)
	//%c casters name
	//%s social name
	//%k skill command word
	// * match anything
	private String[][] pmap={
		// below is killing
		{"kill %m","mobfind %m;kill %m"},
		{"find and kill %m","mobfind %m;kill %m"},
		{"murder %m","mobfind %m;kill %m"},
		{"find and murder %m","mobfind %m;kill %m"},
		{"find and destroy %m","mobfind %m;kill %m"},
		{"destroy %i","itemfind %i;recall"},
		{"find and destroy %i","mobfind %i;recall"},
		{"destroy %m","mobfind %m;kill %m"},
		{"assassinate %m","mobfind %m; kill %m"},
		{"find and assassinate %m","mobfind %m; kill %m"},
		// below is socials
		{"find and %s %m","mobfind %m;%s %m"},
		{"%s %m","mobfind %m;%s %m"},
		// below is item fetching
		{"bring %i","itemfind %i;mobfind %c;give %i %c"},
		{"bring %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"bring %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"bring me %i","itemfind %i;mobfind %c;give %i %c"},
		{"find %i","itemfind %i;mobfind %c;give %i %c"},
		{"find %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"find %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"find me %i","itemfind %i;mobfind %c;give %i %c"},
		{"fetch %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"fetch %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"fetch me %i","itemfind %i;mobfind %c;give %i %c"},
		{"get %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"get %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"get me %i","itemfind %i;mobfind %c;give %i %c"},
		{"get %i","itemfind %i;mobfind %c;give %i %c"},
		{"deliver %i to %m","itemfind %i;mobfind %m;give %i %m"},
		// below are eats, drinks
		{"eat %i ","itemfind %i;eat %i"},
		{"consume %i ","itemfind %i;eat %i"},
		{"stuff yourself with %i ","itemfind %i;eat %i"},
		{"drink %i ","itemfind %i;drink %i"},
		// below are skill usages
		// below are gos, and find someone (and report back where), take me to, show me
		// below are buys and sells
		// follow someone around (but not FOLLOW)
		// simple commands: hold, lock, unlock, read, channel
		// more simpletons: say sit sleep stand wear x, wield x, hold x, 
		// below are sit x sleep x mount x enter x 
		// below are learns, practices, teaches, etc..
		// below are tells, say tos, report tos, 
		// below are silly questions
		{"where *","say You want me to answer where? I don't know where!"},
		{"who *","say You want me to answer who? I don't know who!"},
		{"when *","say You want me to answer when? I don't know when!"},
		{"what *","say You want me to answer what? I don't know what!"},
		{"why *","say You want me to answer why? I don't know why!"},
		{"*?","say You want me to answer a question? I don't know where!"},
	};
	
	public final static int STEP_EVAL=0;
	
	public class geasStep
	{
		public Vector que=new Vector();
		public int step=STEP_EVAL;
		public Vector bothered=new Vector();
	}
	
	private Vector findPMapPossibilies(Vector req)
	{
		
	}
	
	private String cleanWord(String s)
	{
		String chars=".,;!?'";
		for(int x=0;x<chars.length();x++)
			for(int i=0;i<chars.length();i++)
			{
				while(s.startsWith(chars.charAt(i)))
					s=s.substring(1).trim();
				while(s.endsWith(chars.charAt(i)))
					s=s.substring(0,s.length()-1).trim();
			}
		return s;
	}
	
	public geasStep processRequest(MOB you, MOB me, String req)
	{
		Vector REQ=Util.parse(req.toLowerCase().trim());
		for(int v=0;v<REQ.size();v++)
			REQ.setElementAt(cleanWord((String)REQ.elementAt(v)),v);
		Vector itemNames=new Vector();
		Vector mobNames=new Vector();
		
	}
}

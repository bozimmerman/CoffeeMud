package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class EnglishParser
{
	private EnglishParser(){};
	
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
		{"murder %m","mobfind %m;kill %m"},
		{"destroy %m","mobfind %m;kill %m"},
		{"find and kill %m","mobfind %m;kill %m"},
		{"go and kill %m","mobfind %m;kill %m"},
		{"go kill %m","mobfind %m;kill %m"},
		{"find and murder %m","mobfind %m;kill %m"},
		{"go and murder %m","mobfind %m;kill %m"},
		{"go murder %m","mobfind %m;kill %m"},
		{"find and destroy %m","mobfind %m;kill %m"},
		{"assassinate %m","mobfind %m; kill %m"},
		{"find and assassinate %m","mobfind %m; kill %m"},
		{"destroy %i","itemfind %i;recall"},
		{"find and destroy %i","mobfind %i;recall"},
		// below is socials
		{"find and %s %m","mobfind %m;%s %m"},
		{"go and %s %m","mobfind %m;%s %m"},
		{"%s %m","mobfind %m;%s %m"},
		// below is item fetching
		{"bring %i","itemfind %i;mobfind %c;give %i %c"},
		{"find %i","itemfind %i;mobfind %c;give %i %c"},
		{"bring %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"find %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"bring %i to %m","itemfind %i;mobfind %m;give %i %m"},
		{"find %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"get %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"get %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"fetch %i for %m","itemfind %i;mobfind %m;give %i %m"},
		{"fetch %m %i","itemfind %i;mobfind %m;give %i %m"},
		{"bring me %i","itemfind %i;mobfind %c;give %i %c"},
		{"find me %i","itemfind %i;mobfind %c;give %i %c"},
		{"fetch me %i","itemfind %i;mobfind %c;give %i %c"},
		{"get me %i","itemfind %i;mobfind %c;give %i %c"},
		{"get %i","itemfind %i;mobfind %c;give %i %c"},
		{"go and get %i","itemfind %i;mobfind %c;give %i %c"},
		{"go and bring me %i","itemfind %i;mobfind %c;give %i %c"},
		{"go and find me %i","itemfind %i;mobfind %c;give %i %c"},
		{"go and fetch me %i","itemfind %i;mobfind %c;give %i %c"},
		{"go get %i","itemfind %i;mobfind %c;give %i %c"},
		{"go bring me %i","itemfind %i;mobfind %c;give %i %c"},
		{"go find me %i","itemfind %i;mobfind %c;give %i %c"},
		{"deliver %i %m","itemfind %i;mobfind %m;give %i %m"},
		// below are eats, drinks
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
}

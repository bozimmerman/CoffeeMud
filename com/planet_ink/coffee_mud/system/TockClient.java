package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.Tickable;
import com.planet_ink.coffee_mud.interfaces.Environmental;
import com.planet_ink.coffee_mud.utils.Util;

public class TockClient
{
	public Tickable clientObject;
	public int tickID=0;
	public int reTickDown=0;
	public int tickDown=0;
	public boolean suspended=false;
	public long lastStart=0;
	public long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;
	
	public TockClient(Tickable newClientObject,
					  int newTickDown,
					  int newTickID)
	{
		reTickDown=newTickDown;
		tickDown=newTickDown;
		clientObject=newClientObject;
		tickID=newTickID;
	}
	
	public String tickCodeWord()
	{
		if(clientObject==null) return "";
		Tickable obj=clientObject;
		long code=obj.getTickStatus();
		if(obj instanceof Environmental)
		{
			if(Util.bset(code,Tickable.STATUS_BEHAVIOR))
			{
				long b=(code-Tickable.STATUS_BEHAVIOR);
				String codeWord="Behavior #"+b;
				if((b>=0)&&(b<((Environmental)obj).numBehaviors()))
					codeWord+=" ("+(((Environmental)obj).fetchBehavior((int)b)).name();
				return codeWord;
			}
			else
			if(Util.bset(code,Tickable.STATUS_AFFECT))
			{
				long b=(code-Tickable.STATUS_AFFECT);
				String codeWord="Affect #"+b;
				if((b>=0)&&(b<((Environmental)obj).numAffects()))
					codeWord+=" ("+(((Environmental)obj).fetchAffect((int)b)).name();
				return codeWord;
			}
		}
		String codeWord=null;
		if(Util.bset(code,Tickable.STATUS_BEHAVIOR))
		   codeWord="Behavior?!";
		else
		if(Util.bset(code,Tickable.STATUS_AFFECT))
		   codeWord="Affect?!";
		else
		switch((int)code)
		{
		case (int)Tickable.STATUS_ALIVE:
			codeWord="Alive"; break;
		case (int)Tickable.STATUS_CLASS:
			codeWord="Class"; break;
		case (int)Tickable.STATUS_DEAD:
			codeWord="Dead"; break;
		case (int)Tickable.STATUS_END:
			codeWord="End"; break;
		case (int)Tickable.STATUS_FIGHT:
			codeWord="Fighting"; break;
		case (int)Tickable.STATUS_NOT:
			codeWord="!"; break;
		case (int)Tickable.STATUS_OTHER:
			codeWord="Other"; break;
		case (int)Tickable.STATUS_RACE:
			codeWord="Race"; break;
		case (int)Tickable.STATUS_START:
			codeWord="Start"; break;
		case (int)Tickable.STATUS_WEATHER:
			codeWord="Weather"; break;
		default:
			codeWord="?"; break;
		}
		return codeWord;
	}
	
}

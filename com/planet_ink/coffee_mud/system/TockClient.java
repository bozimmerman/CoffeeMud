package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.Environmental;
import java.util.Calendar;

public class TockClient
{
	public Environmental clientObject;
	public int tickID=0;
	public int reTickDown=0;
	public int tickDown=0;
	public boolean suspended=false;
	public Calendar lastStart=null;
	public Calendar lastStop=null;
	public long milliTotal=0;
	public long tickTotal=0;
	
	public TockClient(Environmental newClientObject,
					  int newTickDown,
					  int newTickID)
	{
		reTickDown=newTickDown;
		tickDown=newTickDown;
		clientObject=newClientObject;
		tickID=newTickID;
	}
}

package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_SingleMindedness extends Song
{
	public String ID() { return "Song_SingleMindedness"; }
	public String name(){ return "Single Mindedness";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_SingleMindedness();	}
	protected CMMsg themsg=null;

	public void executeMsg(Environmental ticking, CMMsg msg)
	{
		super.executeMsg(ticking,msg);
		if((themsg==null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL)))
			themsg=msg;
	}

	public boolean okMessage(Environmental ticking, CMMsg msg)
	{
		if((themsg!=null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&(themsg.sourceMinor()!=msg.sourceMinor()))
		{
			msg.source().tell(msg.source(),null,null,"The only thing you have a mind to do is '"+themsg.sourceMessage()+"'.");
			return false;
		}
		return super.okMessage(ticking,msg);
	}
}

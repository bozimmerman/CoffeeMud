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
	protected Affect themsg=null;
	
	public void affect(Environmental ticking, Affect msg)
	{
		super.affect(ticking,msg);
		if((themsg==null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(!Util.bset(msg.sourceCode(),Affect.MASK_GENERAL)))
			themsg=msg;
	}
	
	public boolean okAffect(Environmental ticking, Affect msg)
	{
		if((themsg!=null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(!Util.bset(msg.sourceCode(),Affect.MASK_GENERAL))
		&&(themsg.sourceMinor()!=msg.sourceMinor()))
		{
			msg.source().tell(msg.source(),null,null,"The only thing you have a mind to do is '"+themsg.sourceMessage()+"'.");
			return false;
		}
		return super.okAffect(ticking,msg);
	}
}

package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Comprehension extends Song
{
	public String ID() { return "Song_Comprehension"; }
	public String name(){ return "Comprehension";}
	public String displayText(){ return "(Song of Comprehension)";}
	public int quality(){ return OK_OTHERS;}
	public Environmental newInstance(){	return new Song_Comprehension();}
	
	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end).trim();
		return null;
	}
	protected String subStitute(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affected instanceof MOB)
		&&(!affect.amISource((MOB)affected))
		&&((affect.sourceMinor()==Affect.TYP_SPEAK)
		   ||(affect.sourceMinor()==Affect.TYP_TELL)
		   ||(Util.bset(affect.sourceCode(),Affect.MASK_CHANNEL)))
		&&(affect.tool() !=null)
		&&(affect.sourceMessage()!=null)
		&&(affect.tool() instanceof Ability)
		&&(((Ability)affect.tool()).classificationCode()==Ability.LANGUAGE)
		&&(((MOB)affected).fetchAffect(affect.tool().ID())==null))
		{
			String msg=this.getMsgFromAffect(affect.sourceMessage());
			if(msg!=null)
			{
				if(Util.bset(affect.sourceCode(),Affect.MASK_CHANNEL))
					affect.addTrailerMsg(new FullMsg(affect.source(),null,null,Affect.NO_EFFECT,Affect.NO_EFFECT,affect.othersCode(),this.subStitute(affect.othersMessage(),msg)+" (translated from "+ID()+")"));
				else
				if(affect.amITarget(null)&&(affect.targetMessage()!=null))
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affected,null,Affect.NO_EFFECT,affect.targetCode(),Affect.NO_EFFECT,this.subStitute(affect.targetMessage(),msg)+" (translated from "+((Ability)affect.tool()).ID()+")"));
				else
				if(!affect.amITarget(null)&&(affect.othersMessage()!=null))
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affected,null,Affect.NO_EFFECT,affect.othersCode(),Affect.NO_EFFECT,this.subStitute(affect.othersMessage(),msg)+" (translated from "+((Ability)affect.tool()).ID()+")"));
			}
		}
	}

}

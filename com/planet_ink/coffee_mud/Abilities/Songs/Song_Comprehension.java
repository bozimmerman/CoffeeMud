package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Song_Comprehension extends Song
{
	public String ID() { return "Song_Comprehension"; }
	public String name(){ return "Comprehension";}
	public int quality(){ return OK_OTHERS;}

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
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Ability)
		&&(((Ability)msg.tool()).classificationCode()==Ability.LANGUAGE)
		&&(((MOB)affected).fetchEffect(msg.tool().ID())==null))
		{
			String str=this.getMsgFromAffect(msg.sourceMessage());
			if(str!=null)
			{
				if(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
					msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),this.subStitute(msg.othersMessage(),str)+" (translated from "+ID()+")"));
				else
				if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
					msg.addTrailerMsg(new FullMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.targetCode(),CMMsg.NO_EFFECT,this.subStitute(msg.targetMessage(),str)+" (translated from "+((Ability)msg.tool()).ID()+")"));
				else
				if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
				{
					String otherMes=msg.othersMessage();
					if(msg.target()!=null)
						otherMes=CoffeeFilter.fullOutFilter(((MOB)affected).session(),(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
					msg.addTrailerMsg(new FullMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.othersCode(),CMMsg.NO_EFFECT,this.subStitute(otherMes,str)+" (translated from "+ID()+")"));
				}
			}
		}
	}

}

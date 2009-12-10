package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
    protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
		   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
		&&(msg.tool() !=null)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
		&&(((MOB)affected).fetchEffect(msg.tool().ID())==null))
		{
			String str=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(str!=null)
			{
				if(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,msg.othersCode(),CMStrings.substituteSayInMessage(msg.othersMessage(),str)+" (translated from "+ID()+")"));
				else
				if(msg.amITarget(affected)&&(msg.targetMessage()!=null))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.targetCode(),CMMsg.NO_EFFECT,CMStrings.substituteSayInMessage(msg.targetMessage(),str)+" (translated from "+((Ability)msg.tool()).ID()+")"));
				else
				if((msg.othersMessage()!=null)&&(msg.othersMessage().indexOf("'")>0))
				{
					String otherMes=msg.othersMessage();
					if(msg.target()!=null)
						otherMes=CMLib.coffeeFilter().fullOutFilter(((MOB)affected).session(),(MOB)affected,msg.source(),msg.target(),msg.tool(),otherMes,false);
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,null,CMMsg.NO_EFFECT,msg.othersCode(),CMMsg.NO_EFFECT,CMStrings.substituteSayInMessage(otherMes,str)+" (translated from "+ID()+")"));
				}
			}
		}
	}

}

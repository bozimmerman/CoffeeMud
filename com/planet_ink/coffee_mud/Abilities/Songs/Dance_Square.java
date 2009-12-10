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
@SuppressWarnings("unchecked")
public class Dance_Square extends Dance
{
	public String ID() { return "Dance_Square"; }
	public String name(){ return "Square";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected boolean skipStandardDanceInvoke(){return true;}
	protected String danceOf(){return name()+" Dance";}
    protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isMonster())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amISource(invoker())
		&&(affected!=invoker())
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
            String cmd=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(cmd!=null)
			{
				MOB M=(MOB)affected;
                if(CMLib.flags().canBeHeardBy(invoker(),M)
                &&CMLib.flags().canBeSeenBy(invoker(),M)
                &&(M.location()==invoker().location())
                &&(M.location().show(invoker(),affected,CMMsg.MSG_ORDER,null)))
                {
                    Object O=CMLib.english().findCommand(M,CMParms.parse(cmd));
                    if((O!=null)&&((!(O instanceof Command))||(((Command)O).canBeOrdered())))
						M.enqueCommand(CMParms.parse(cmd),Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
                }
			}
		}
		super.executeMsg(myHost,msg);

	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().aliveAwakeMobile(mob,false)))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		undance(mob,null,true);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"^SThe "+danceOf()+" begins!^?":"^S<S-NAME> begin(s) to dance the "+danceOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+danceOf()+" over again.^?";

			HashSet friends=mob.getGroupMembers(new HashSet());
			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					invoker=mob;
					Dance newOne=(Dance)this.copyOf();
					newOne.invokerManaCost=-1;
	
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB follower=R.fetchInhabitant(i);
						Room R2=follower.location();
	
						// malicious dances must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
						if((!friends.contains(follower))&&(follower!=mob))
							affectType=affectType|CMMsg.MASK_MALICIOUS;
						if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
	
						if((CMLib.flags().canBeSeenBy(invoker,follower)
							&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							CMMsg msg3=msg2;
							if((!friends.contains(follower))&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								R2.send(follower,msg2);
								if(msg2.value()<=0)
								{
									R2.send(follower,msg3);
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
									{
										if(follower!=mob)
											follower.addEffect((Ability)newOne.copyOf());
										else
											follower.addEffect(newOne);
									}
								}
							}
						}
					}
					mob.location().recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}

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
public class Song_Thanks extends Song
{
	public String ID() { return "Song_Thanks"; }
	public String name(){ return "Thanks";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected boolean skipStandardSongInvoke(){return true;}
	protected boolean maliciousButNotAggressiveFlag(){return true;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat()&&(mob.isMonster()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		if(invoker==null) return true;
		if(mob.location()!=invoker.location()) return true;
		//if(!mob.isMonster()) return true;
		if((CMLib.dice().rollPercentage()<6)
		   &&(CMLib.dice().rollPercentage()>mob.charStats().getSave(CharStats.STAT_SAVE_MIND))
		   &&(CMLib.dice().rollPercentage()>mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC))
		   &&(CMLib.flags().canMove(mob))
		   &&(CMLib.flags().canBeSeenBy(invoker,mob))
		   &&(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)>(1.0+super.getXLEVELLevel(invoker()))))
		{
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1:
				CMLib.commands().postSay(mob,invoker,"Thank you "+invoker.name()+"!",false,false);
				break;
			case 2:
				CMLib.commands().postSay(mob,invoker,"Thanks for being you, "+invoker.name()+"!",false,false);
				break;
			case 3:
				CMLib.commands().postSay(mob,invoker,"Thanks "+invoker.name()+"!",false,false);
				break;
			case 4:
				CMLib.commands().postSay(mob,invoker,"You are great, "+invoker.name()+"!  Thanks!",false,false);
				break;
			case 5:
				CMLib.commands().postSay(mob,invoker,"I appreciate you, "+invoker.name()+"!",false,false);
				break;
			case 6:
				CMLib.commands().postSay(mob,invoker,"Keep it up, "+invoker.name()+"! Thanks!",false,false);
				break;
			case 7:
				CMLib.commands().postSay(mob,invoker,"Thanks a lot, "+invoker.name()+"!",false,false);
				break;
			case 8:
				CMLib.commands().postSay(mob,invoker,"Thank you dearly, "+invoker.name()+"!",false,false);
				break;
			case 9:
				CMLib.commands().postSay(mob,invoker,"Thank you always, "+invoker.name()+"!",false,false);
				break;
			case 10:
				CMLib.commands().postSay(mob,invoker,"You're the best, "+invoker.name()+"! Thanks!",false,false);
				break;
			}
			Coins C=CMLib.beanCounter().makeBestCurrency(mob,CMath.mul(1.0,super.getXLEVELLevel(invoker())));
			if(C!=null)
			{
				CMLib.beanCounter().subtractMoney(mob,CMath.mul(1.0,super.getXLEVELLevel(invoker())));
				mob.addInventory(C);
				mob.doCommand(CMParms.parse("GIVE \""+C.name()+"\" \""+invoker.name()+"\""),Command.METAFLAG_FORCED);
				if(!C.amDestroyed()) C.putCoinsBack();
			}
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		unsing(mob,mob,true);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"^SThe "+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to sing the "+songOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+songOf()+" over again.^?";

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),msgStr);
				if(mob.location().okMessage(mob,msg))
				{
					HashSet h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null) continue;
					Song newOne=(Song)this.copyOf();
	
					for(Iterator f=h.iterator();f.hasNext();)
					{
						MOB follower=(MOB)f.next();
	
						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
						if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
	
						if((CMLib.flags().canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							CMMsg msg3=msg2;
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								follower.location().send(follower,msg2);
								if(msg2.value()<=0)
								{
									follower.location().send(follower,msg3);
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
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}

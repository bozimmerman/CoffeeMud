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
public class Song_Friendship extends Song
{
	public String ID() { return "Song_Friendship"; }
	public String name(){ return "Friendship";}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected boolean skipStandardSongInvoke(){return true;}
	protected boolean maliciousButNotAggressiveFlag(){return true;}
	public long flags(){return Ability.FLAG_CHARMING;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(affected==invoker))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.amISource(mob.amFollowing())))
			unInvoke();
		else
		if((msg.amISource(mob))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.amITarget(mob.amFollowing())))
		{
			mob.tell("You like "+mob.amFollowing().charStats().himher()+" too much.");
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(!mob.isMonster())
		&&(msg.target() instanceof Room)
		&&((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.sourceMinor()==CMMsg.TYP_RECALL))
		&&(mob.amFollowing()!=null)
		&&(((Room)msg.target()).isInhabitant(mob.amFollowing())))
		{
			mob.tell("You don't want to leave your friend.");
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(mob.amFollowing()!=null)
		&&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW))
		{
			mob.tell("You like "+mob.amFollowing().name()+" too much.");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(mob==invoker) return true;
		if(mob.amFollowing()!=invoker)
		{
			unInvoke();
			return false;
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			super.unInvoke();
			if(mob!=invoker)
			{
                if((canBeUninvoked()&&(!mob.amDead())))
                {
                    mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> free-will returns.");
    				mob.setFollowing(null);
    				CMLib.commands().postStand(mob,true);
    				if(mob.isMonster())
    				{
                        if((CMLib.dice().rollPercentage()>50)
                        ||((mob.getStartRoom()!=null)
                            &&(mob.getStartRoom().getArea()!=mob.location().getArea())
                            &&(CMLib.flags().isAggressiveTo(mob,null)||(invoker==null)||(!mob.location().isInhabitant(invoker)))))
    							CMLib.tracking().wanderAway(mob,true,true);
    					else
    					if((invoker!=null)&&(invoker!=mob))
    						mob.setVictim(invoker);
    				}
                }
			}
		}
		else
			super.unInvoke();
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
					Song newOne=(Song)this.copyOf();
					HashSet h=sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null) continue;
	
					for(Iterator f=h.iterator();f.hasNext();)
					{
						MOB follower=(MOB)f.next();
						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
						if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
							affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
						if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
	
						if((CMLib.flags().canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							CMMsg msg3=msg2;
							if((mindAttack())&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
							int levelDiff=follower.envStats().level()-(mob.envStats().level()+(getXLEVELLevel(mob)*2));
							if(levelDiff<0) levelDiff=0;
	
							if((levelDiff>(3+((mob.envStats().level()+(getXLEVELLevel(mob)*2))/10)))&&(mindAttack()))
								mob.tell(mob,follower,null,"<T-NAME> looks too powerful.");
							else
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								follower.location().send(mob,msg2);
								if(msg2.value()<=0)
								{
									follower.location().send(mob,msg3);
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
									{
										if((follower.amFollowing()!=mob)&&(follower!=mob))
										{
											CMLib.commands().postFollow(follower,mob,false);
											if(follower.amFollowing()==mob)
											{
												if(follower!=mob)
													follower.addEffect((Ability)newOne.copyOf());
												else
													follower.addEffect(newOne);
												CMLib.combat().makePeaceInGroup(mob);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}

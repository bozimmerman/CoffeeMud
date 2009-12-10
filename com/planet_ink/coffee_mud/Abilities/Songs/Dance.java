package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Dance extends StdAbility
{
	public String ID() { return "Dance"; }
	public String name(){ return "a Dance";}
	public String displayText(){ return "("+danceOf()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"DANCE","DA"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SONG|Ability.DOMAIN_DANCING;}
	public int usageType(){return USAGE_MOVEMENT;}
    public int maxRange(){return adjustedMaxInvokerRange(2);}
	protected int invokerManaCost=-1;
    protected int steadyDown=-1;
    protected Vector commonRoomSet=null;
    protected Room originRoom=null;

	protected boolean skipStandardDanceInvoke(){return false;}
	protected boolean mindAttack(){return abstractQuality()==Ability.QUALITY_MALICIOUS;}
	protected boolean skipStandardDanceTick(){return false;}
	protected boolean maliciousButNotAggressiveFlag(){return false;}
	protected String danceOf(){return name();}

    protected boolean HAS_QUANTITATIVE_ASPECT(){return true;}
    
	public int adjustedLevel(MOB mob, int asLevel)
	{
        int level=super.adjustedLevel(mob,asLevel);
        int charisma=(invoker().charStats().getStat(CharStats.STAT_CHARISMA)-10);
        if(charisma>0)
            return level+(charisma/3);
        return level;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;

		MOB mob=(MOB)affected;
		if((affected==invoker())&&(invoker()!=null)&&(invoker().location()!=originRoom))
		{
			Vector V=getInvokerScopeRoomSet(null);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			originRoom=invoker().location();
		}
		else
		if((abstractQuality()==Ability.QUALITY_MALICIOUS)
		&&(!maliciousButNotAggressiveFlag())
		&&(!mob.amDead())
		&&(mob.isMonster())
		&&(!mob.isInCombat())
		&&(CMLib.flags().aliveAwakeMobile(mob,true))
        &&(mob.amFollowing()==null)
        &&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0))
        &&(!CMLib.flags().isATrackingMonster(mob)))
		{
			if((mob.location()!=originRoom)
			&&(CMLib.flags().isMobile(mob)))
			{
				int dir=this.getCorrectDirToOriginRoom(mob.location(),commonRoomSet.indexOf(mob.location()));
				if(dir>=0)
					CMLib.tracking().move(mob,dir,false,false);
			}
			else
			if((mob.location().isInhabitant(invoker()))
			&&(CMLib.flags().canBeSeenBy(invoker(),mob)))
				CMLib.combat().postAttack(mob,invoker(),mob.fetchWieldedItem());
		}
		
		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||(commonRoomSet==null)
		||(!commonRoomSet.contains(mob.location())))
			return possiblyUndance(mob,null,false);
		
		if(skipStandardDanceTick())
			return true;

		if((invoker==null)
		||(CMLib.flags().isSitting(invoker()))
		||(!CMLib.flags().aliveAwakeMobile(mob,true))
		||(!CMLib.flags().aliveAwakeMobile(invoker(),true))
		||(!CMLib.flags().canBeSeenBy(invoker,mob)))
			return possiblyUndance(mob,null,false);
		if(invokerManaCost<0) invokerManaCost=usageCost(invoker(),false)[1];
		if(!mob.curState().adjMovement(-(invokerManaCost/15),mob.maxState()))
		{
			mob.tell("The dancing exhausts you.");
			undance(mob,null,false);
			return false;
		}
		return true;
	}

	protected void undance(MOB mob, MOB invoker, boolean notMe)
	{
		if(mob==null) return;
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)
			&&(A instanceof Dance)
			&&((!notMe)||(!A.ID().equals(ID())))
			&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
            {
                if((!(A instanceof Dance))||(((Dance)A).steadyDown<=0))
    				A.unInvoke();
            }
		}
	}

    protected Vector getInvokerScopeRoomSet(MOB backupMob)
    {
    	if((invoker()==null)
    	||(invoker().location()==null))
        {
    		if((backupMob!=null)&&(backupMob.location()!=null))
	    		 return CMParms.makeVector(backupMob.location());
			return new Vector();
        }
    	int depth=super.getXMAXRANGELevel(invoker());
    	if(depth==0) return CMParms.makeVector(invoker().location());
    	Vector rooms=new Vector();
        // needs to be area-only, because of the aggro-tracking rule
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.OPENONLY)
				.add(TrackingLibrary.TrackingFlag.AREAONLY)
				.add(TrackingLibrary.TrackingFlag.NOAIR);
    	CMLib.tracking().getRadiantRooms(invoker().location(), rooms, flags, null, depth, null);
    	if(!rooms.contains(invoker().location()))
    		rooms.addElement(invoker().location());
    	return rooms;
    }
    
	protected boolean possiblyUndance(MOB mob, MOB invoker, boolean notMe)
	{
        if(steadyDown<0) steadyDown=((invoker()!=null)&&(invoker()!=mob))?super.getXTIMELevel(invoker()):0;
        if(steadyDown==0)
        {
            undance(mob,invoker,notMe);
            return false;
        }
        mob.tell("The "+danceOf()+" lingers in your head ("+steadyDown+").");
        steadyDown--;
        return true;
	}

	protected int getCorrectDirToOriginRoom(Room R, int v)
	{
		if(v<0) return -1;
		int dir=-1;
		Room R2=null;
		Exit E2=null;
		int lowest=v;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R2=R.getRoomInDir(d);
			E2=R.getExitInDir(d);
			if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
			{
				int dx=commonRoomSet.indexOf(R2);
				if((dx>=0)&&(dx<lowest))
				{
					lowest=dx;
					dir=d;
				}
			}
		}
		return dir;
	}
	
	protected String getCorrectMsgString(Room R, String str, int v)
	{
		String msgStr=null;
		if(R==originRoom)
			msgStr=str;
		else
		{
			int dir=this.getCorrectDirToOriginRoom(R,v);
			if(dir>=0)
				msgStr="^SYou see the "+danceOf()+" being performed "+Directions.getInDirectionName(dir)+"!^?";
			else
				msgStr="^SYou see the "+danceOf()+" being performed nearby!^?";
		}
		return msgStr;
	}
	
	public HashSet sendMsgAndGetTargets(MOB mob, Room R, CMMsg msg, Environmental givenTarget, boolean auto)
	{
		if(originRoom==R)
			R.send(mob,msg);
		else
			R.sendOthers(mob,msg);
		if(R!=originRoom)
			mob.setLocation(R);
		HashSet h=properTargets(mob,givenTarget,auto);
		if(R!=originRoom)
		{
			R.delInhabitant(mob);
			mob.setLocation(originRoom);
		}
		if(h==null) return null;
		if(R==originRoom)
		{
			if(!h.contains(mob)) 
				h.add(mob);
		}
		else
			h.remove(mob);
		return h;
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((invoker()!=null)
		&&(!unInvoked)
		&&(affected==invoker())
		&&(msg.amISource(invoker()))
		&&(msg.target() instanceof Armor)
		&&(msg.targetMinor()==CMMsg.TYP_WEAR))
		{
			if(msg.source().location()!=null)
				msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,"<S-NAME> stop(s) dancing.");
			unInvoke();
		}
		super.executeMsg(host,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            for(int e=0;e<mob.numAllEffects();e++)
                if(mob.fetchEffect(e) instanceof Dance)
                    return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) the "+name()+" due to <S-HIS-HER> armor!");
			return false;
		}

		if(skipStandardDanceInvoke())
			return true;

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

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					HashSet h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null) continue;
					invoker=mob;
					Dance newOne=(Dance)this.copyOf();
					newOne.invoker=mob;
					newOne.invokerManaCost=-1;
	
					for(Iterator f=h.iterator();f.hasNext();)
					{
						MOB follower=(MOB)f.next();
						Room R2=follower.location();
	
						// malicious dances must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
						if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
							affectType=affectType|CMMsg.MASK_MALICIOUS;
						if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
	
						if((R2!=null)&&(CMLib.flags().canBeSeenBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							CMMsg msg3=msg2;
							if((mindAttack())&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								R2.send(follower,msg2);
								if(msg2.value()<=0)
								{
									R2.send(follower,msg3);
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
									{
										undance(follower,null,false);
										newOne.setSavable(false);
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
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> make(s) a false step.");

		return success;
	}
}

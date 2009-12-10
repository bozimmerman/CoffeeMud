package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Sermon extends Prayer
{
	public String ID() { return "Prayer_Sermon"; }
	public String name(){return "Sermon";}
	public String displayText(){return "(Sermon)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_CHARMING|Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

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
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(mob.amFollowing()!=null)
				CMLib.commands().postFollow(mob,null,false);
			CMLib.commands().postStand(mob,true);
			if((mob.isMonster())&&(!CMLib.flags().isMobile(mob)))
				CMLib.tracking().wanderAway(mob,true,true);
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
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
			mob.tell("You admire "+mob.amFollowing().charStats().himher()+" too much.");
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(!mob.isMonster())
		&&(msg.target() instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(mob.amFollowing()!=null)
		&&(((Room)msg.target()).isInhabitant(mob.amFollowing())))
		{
			mob.tell("You are too enthralled to leave.");
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(mob.amFollowing()!=null)
		&&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW))
		{
			mob.tell("You believe in "+mob.amFollowing().name()+" too much.");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		Hashtable h=new Hashtable();
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&(CMLib.flags().canBeSeenBy(M,mob))&&(M!=mob)
			&&(M.charStats().getStat(CharStats.STAT_INTELLIGENCE)>4))
				h.put(M,M);
		}
		if(h.size()==0)
		{
			mob.tell("There doesn't appear to be anyone here worth sermonizing to.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,-(h.size()*3),auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> begin(s) sermonizing on the wonders of "+hisHerDiety(mob)+".^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				if((CMLib.flags().canBeHeardBy(mob,target))&&(mob.mayIFight(target)))
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						success=maliciousAffect(mob,target,asLevel,0,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0));
						if((success)&&(msg.value()<=0))
						{
							if(target.getVictim()==mob)
								target.makePeace();
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> begin(s) nodding and shouting praises to "+hisHerDiety(mob)+".");
							CMLib.commands().postFollow(target,mob,true);
						}
					}
				}
				else
					beneficialWordsFizzle(mob,target,"<T-NAME> seem(s) unmoved by the <S-YOUPOSS> sermon.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> forget(s) how <S-YOUPOSS> sermon to "+hisHerDiety(mob)+" goes.");


		// return whether it worked
		return success;
	}
}

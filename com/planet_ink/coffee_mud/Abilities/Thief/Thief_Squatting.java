package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Squatting extends ThiefSkill
{
	public String ID() { return "Thief_Squatting"; }
	public String name(){ return "Squatting";}
	public String displayText(){return "(Squatting)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"SQUAT","SQUATTING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	protected boolean failed=false;
	protected Room room=null;
	private LandTitle title=null;
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((msg.source()==mob)
			&&(msg.target()==mob.location())
			&&(msg.targetMinor()==CMMsg.TYP_LEAVE))
			{
				failed=true;
				unInvoke();
			}
			else
			if((CMLib.flags().isStanding(mob))||(mob.location()!=room))
			{
				failed=true;
				unInvoke();
			}
		}
		super.executeMsg(host,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null))
		{
			if((failed)||(!CMLib.flags().isSitting(mob))||(room==null)||(title==null)||(mob.location()!=room))
				mob.tell("You are no longer squatting.");
			else
			if(title.landOwner().length()>0)
			{
				mob.tell("Your squat has succeeded.  This property no longer belongs to "+title.landOwner()+".");
				title.setLandOwner("");
				title.updateTitle();
				title.updateLot(null);
			}
			else
			if(title.landOwner().length()>0)
			{
				mob.tell("Your squat has succeeded.  This property now belongs to you.");
				title.setLandOwner(mob.Name());
				title.updateTitle();
				title.updateLot(CMParms.makeVector(mob.name()));
			}
		}
		failed=false;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already squatting.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(CMLib.law().doesHavePriviledgesHere(mob,mob.location()))
		{
			mob.tell("This is your place already!");
			return false;
		}
		LandTitle T=CMLib.law().getLandTitle(mob.location());
		boolean confirmed=false;
		for(int r=0;r<mob.location().numEffects();r++)
			if(mob.location().fetchEffect(r)==T)
				confirmed=true;
		if(T==null)
		{
			mob.tell("This property is not available for sale, and cannot be squatted upon.");
			return false;
		}
		MOB warnMOB=null;
		if(T.landOwner().length()>0)
		{
			Clan C=CMLib.clans().getClan(T.landOwner());
			if(C==null)
			{
				MOB M=CMLib.players().getLoadPlayer(T.landOwner());
				if(M!=null)
					warnMOB=M;
			}
			else
			{
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session S=CMLib.sessions().elementAt(s);
					if((S.mob()!=null)
					&&(S.mob()!=mob)
					&&(S.mob().getClanID().equals(C.clanID())))
						warnMOB=S.mob();
				}
			}
			if((warnMOB==null)||(!CMLib.flags().isInTheGame(warnMOB,true)))
			{
				mob.tell("The owners must be in the game for you to begin squatting.");
				return false;
			}
		}
		if(!confirmed)
		{
			mob.tell("You cannot squat on an area for sale.");
			return false;
		}
		if(!CMLib.flags().isSitting(mob))
		{
			mob.tell("You must be sitting!");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":"<S-NAME> start(s) squatting.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> can't seem to get comfortable here.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			failed=false;
			room=mob.location();
			title=T;
			beneficialAffect(mob,target,asLevel,(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)));
			if(warnMOB!=null)
				warnMOB.tell("You've heard a rumor that someone is squatting on "+T.landOwner()+"'s property.");
		}
		return success;
	}
}

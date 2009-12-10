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
public class Prayer_Revival extends Prayer
{
	public String ID() { return "Prayer_Revival"; }
	public String name(){ return "Revival";}
	public String displayText(){return "(Revival)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your part in the revival is over.");
		super.unInvoke();

	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null))
		{
			MOB mob=(MOB)affected;
			Room R=mob.location();
			int levels=0;
			Vector inhabs=new Vector();
			Vector clerics=new Vector();
            int bonus=(2*super.getXLEVELLevel(invoker()));
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(M!=null)
				{
					if(mob.getWorshipCharID().equals(M.getWorshipCharID()))
					{
						if(M.fetchEffect(ID())!=null)
						{
							levels+=(M.envStats().level()+bonus);
							clerics.addElement(M);
						}
					}
					else
					if(CMLib.dice().rollPercentage()<10)
						inhabs.addElement(M);
				}
			}
			Deity D=CMLib.map().getDeity(mob.getWorshipCharID());
			if((D!=null)&&(CMLib.dice().rollPercentage()<50))
			switch(CMLib.dice().roll(1,13,0))
			{
			case 1:	CMLib.commands().postSay(mob,null,D.name()+" is great! Shout "+CMStrings.capitalizeAndLower(D.charStats().hisher())+" praises!",false,false); break;
			case 2:	CMLib.commands().postSay(mob,null,"Can I hear an AMEN?!",false,false); break;
			case 3:	CMLib.commands().postSay(mob,null,"Praise "+D.name()+"!",false,false); break;
			case 4:	CMLib.commands().postSay(mob,null,"Halleluyah! "+D.name()+" is great!",false,false); break;
			case 5:	CMLib.commands().postSay(mob,null,"Let's hear it for "+D.name()+"!",false,false); break;
			case 6:	CMLib.commands().postSay(mob,null,"Exalt the name of "+D.name()+"!",false,false); break;
			case 7:	if(clerics.size()>1)
					{
						MOB M=(MOB)clerics.elementAt(CMLib.dice().roll(1,clerics.size(),-1));
						if(M!=mob)
							CMLib.commands().postSay(mob,null,"Preach it "+M.name()+"!",false,false);
						else
							CMLib.commands().postSay(mob,null,"I LOVE "+D.name()+"!",false,false);
					}
					else
						CMLib.commands().postSay(mob,null,"I LOVE "+D.name()+"!",false,false);
					break;
			case 8:	CMLib.commands().postSay(mob,null,"Holy is the name of "+D.name()+"!",false,false); break;
			case 9:	CMLib.commands().postSay(mob,null,"Do you BELIEVE?!? I BELIEVE!!!",false,false); break;
			case 10: CMLib.commands().postSay(mob,null,"Halleluyah!",false,false); break;
			case 11: mob.enqueCommand(CMParms.parse("EMOTE do(es) a spirit-filled dance!"),Command.METAFLAG_FORCED,0); break;
			case 12: mob.enqueCommand(CMParms.parse("EMOTE wave(s) <S-HIS-HER> hands in the air!"),Command.METAFLAG_FORCED,0);  break;
			case 13: mob.enqueCommand(CMParms.parse("EMOTE catch(es) the spirit of "+D.name()+"!"),Command.METAFLAG_FORCED,0); break;
			}
			if((clerics.size()>2)&&(inhabs.size()>0))
			{
				levels=levels/clerics.size();
				levels=levels+((clerics.size()-3)*5);
				MOB M=(MOB)inhabs.elementAt(CMLib.dice().roll(1,inhabs.size(),-1));
				if((M!=null)&&(levels>=(M.envStats().level()+bonus)))
				{
					MOB vic1=mob.getVictim();
					MOB vic2=M.getVictim();
					if(M.getWorshipCharID().length()>0)
					{
						Ability A=CMClass.getAbility("Prayer_Faithless");
						if(A!=null) A.invoke(mob,M,true,0);
					}
					if(M.getWorshipCharID().length()==0)
					{
						Ability A=CMClass.getAbility("Prayer_UndeniableFaith");
						if(A!=null)
							if(A.invoke(mob,M,true,0))
							{
								if(M.getWorshipCharID().equals(mob.getWorshipCharID()))
								{
									for(int c=0;c<clerics.size();c++)
									{
										MOB M2=(MOB)clerics.elementAt(c);
										if(M2!=mob)
											CMLib.leveler().postExperience(M2,M,null,25,false);
									}
								}
							}
					}
					mob.setVictim(vic1);
					M.setVictim(vic2);
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if((target.getWorshipCharID().length()==0)
		||(CMLib.map().getDeity(target.getWorshipCharID())==null))
		{
			target.tell("You must worship a god to use this prayer.");
			return false;
		}
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already participating in a revival.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> start(s) a revival!":"^S<S-NAME> "+prayWord(mob)+" for successful revival, and then start(s) MOVING!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,10);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for a successful revival, but fail(s).");

		return success;
	}
}

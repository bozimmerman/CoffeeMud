package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_Whiplash extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Whiplash";
	}

	private final static String localizedName = CMLib.lang().L("Whiplash");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"WHIPLASH"});

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected String gone="";
	protected MOB target=null;

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(adjustedLevel((MOB)affected,0)/5)*(super.getXLEVELLevel((MOB)affected)));
			affectableStats.setDamage(affectableStats.damage()+(adjustedLevel((MOB)affected,0)/4)+(super.getXLEVELLevel((MOB)affected)));
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(target==null))
			return super.okMessage(myHost,msg);
		final MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.amITarget(target))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==mob.fetchWieldedItem())
		&&(msg.value()>0))
		{
			if((gone!=null)&&(gone.length()>0))
			{
				LimbDamage scarredA=(LimbDamage)target.fetchEffect("Scarring");
				if(scarredA==null)
				{
					scarredA=(LimbDamage)CMClass.getAbility("Scarring");
					if(scarredA!=null)
						target.addNonUninvokableEffect(scarredA);
				}
				mob.location().show(mob, msg.target(), CMMsg.MSG_OK_VISUAL, L("<S-YOUPOSS> attack scar(s) <T-NAME>"));
				scarredA.damageLimb(gone);
			}
			unInvoke();
		}
		return super.okMessage(myHost,msg);
	}

	protected boolean prereqs(final MOB mob, final boolean quiet)
	{
		final Item w=mob.fetchWieldedItem();
		if((w==null)
		||(!(w instanceof Weapon))
		||(((Weapon)w).weaponClassification()!=Weapon.CLASS_FLAILED)
		||((((Weapon)w).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		{
			if(!quiet)
				mob.tell(L("You need a leather flailed weapon to perform a whiplash!"));
			return false;
		}
		final Weapon wp=(Weapon)w;
		if((wp.weaponDamageType()!=Weapon.TYPE_SLASHING)&&(wp.weaponDamageType()!=Weapon.TYPE_PIERCING))
		{
			if(!quiet)
				mob.tell(L("You cannot whiplash someone with @x1!",wp.name()));
			return false;
		}
		return true;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!prereqs(mob,true))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!prereqs(mob,false))
			return false;

		gone="";
		target=null;

		if(commands.size()>0)
		{
			final String s=CMParms.combine(commands,0);
			if(mob.location().fetchInhabitant(s)!=null)
				target=mob.location().fetchInhabitant(s);
			if((target!=null)&&(!CMLib.flags().canBeSeenBy(target,mob)))
			{
				mob.tell(L("You can't see '@x1' here.",s));
				return false;
			}
			if(target!=null)
				commands.remove(0);
		}
		if(target==null)
			target=mob.getVictim();
		if((target==null)||(target==mob))
		{
			mob.tell(L("Whiplash whom?"));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("@x1 already has a whiplash attempt.",target.name(mob),target.charStats().hisher()));
			return false;
		}

		LimbDamage ampuA=(LimbDamage)target.fetchEffect("Amputation");
		final List<String> remainingLimbList;
		if(ampuA!=null)
			remainingLimbList=ampuA.unaffectedLimbSet();
		else
		{
			ampuA=(LimbDamage)CMClass.getAbility("Amputation");
			ampuA.setAffectedOne(target);
			remainingLimbList=ampuA.unaffectedLimbSet();
			ampuA.destroy();
		}

		final LimbDamage scarredA=(LimbDamage)target.fetchEffect("Scarring");
		if(scarredA!=null)
			remainingLimbList.removeAll(scarredA.affectedLimbNameSet());

		/*
		if(remainingLimbList.size()==0)
		{
			if(!auto)
				mob.tell(L("There is nothing left on @x1 to whiplash!",target.name(mob)));
			return false;
		}
		*/
		if(mob.isMonster()||(remainingLimbList.size()>0))
			gone=remainingLimbList.get(CMLib.dice().roll(1,remainingLimbList.size(),-1));
		/*
		else
		if(commands.size()<=0)
		{
			mob.tell(L("You must specify a body part to cut into."));
			final StringBuffer str=new StringBuffer(L("Parts include: "));
			for(int i=0;i<remainingLimbList.size();i++)
				str.append((remainingLimbList.get(i))+", ");
			mob.tell(str.toString().substring(0,str.length()-2)+".");
			return false;
		}
		else
		{
			final String off=CMParms.combine(commands,0);
			if((off.equalsIgnoreCase("head"))
			&&(target.charStats().getBodyPart(Race.BODY_HEAD)>=0))
			{
				gone=Race.BODYPARTSTR[Race.BODY_HEAD].toLowerCase();
				hpReq=3;
			}
			else
			for(int i=0;i<remainingLimbList.size();i++)
			{
				if(remainingLimbList.get(i).toUpperCase().startsWith(off.toUpperCase()))
				{
					gone=remainingLimbList.get(i);
					break;
				}
			}
			if(gone.length()==0)
			{
				mob.tell(L("'@x1' is not a valid body part.",off));
				final StringBuffer str=new StringBuffer(L("Parts include: "));
				for(int i=0;i<remainingLimbList.size();i++)
					str.append((remainingLimbList.get(i))+", ");
				mob.tell(str.toString().substring(0,str.length()-2)+".");
				return false;
			}
		}
		*/

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		//TODO: check if they are wearing anything, and if so, ungone the gone.
		for(int i=0;i<Race.BODY_PARTS;i++)
		{
			if(gone.equalsIgnoreCase(Race.BODYPARTSTR[i]))
			{
				// now we have the code?
				final long wearLoc = Race.BODY_WEARVECTOR[i];
				if(wearLoc > 0)
				{
					final List<Item> alreadyWearing=target.fetchWornItems(wearLoc, (short)-2048,(short)0);
					if((alreadyWearing!=null)&&(alreadyWearing.size()>0))
						gone="";
					else
					{
						if ((target.charStats().getWearableRestrictionsBitmap() & wearLoc) > 0)
							gone="";
					}
				}
				break;
			}
		}

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,L("^F^<FIGHT^><S-NAME> aim(s) a whiplash at <T-NAME>!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				final Thief_Whiplash A=(Thief_Whiplash)beneficialAffect(mob,mob,asLevel,2);
				A.target=target;
				A.gone=gone;
				mob.recoverPhyStats();
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> wind(s) up to whiplash <T-NAME>, but fail(s)."));

		// return whether it worked
		return success;
	}
}

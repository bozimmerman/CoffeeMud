package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2018 Bo Zimmerman

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
public class Spell_ImprovedPolymorph extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ImprovedPolymorph";
	}

	private final static String localizedName = CMLib.lang().L("Improved Polymorph");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Improved Polymorph)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	protected Race		newRace	= null;
	protected boolean	noxp	= false;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(noxp)
		&&((msg.target()==affected)   &&(affected instanceof MOB)))
		{
			msg.setValue(0);
		}
		return super.okMessage(myHost,msg);
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(' ')>0)
				affectableStats.setName(L("a @x1 called @x2",newRace.name(),affected.name()));
			else
				affectableStats.setName(L("@x1 the @x2",affected.name(),newRace.name()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0)
				affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if((affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
			&&(newRace.getAgingChart()[oldCat]<Short.MAX_VALUE))
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> morph(s) back to <S-HIS-HER> normal form."));
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((target instanceof MOB)&&(target.fetchEffect(ID())==null))
			{
				if((mob.getVictim()==target)||(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)))
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell(L("You need to specify what to turn your target into!"));
			return false;
		}
		final String race=commands.get(commands.size()-1);
		commands.remove(commands.size()-1);
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if((target==mob)&&(!auto))
		{
			mob.tell(L("You cannot hold enough energy to cast this on yourself."));
			return false;
		}
		Race R=CMClass.getRace(race);
		if((R==null)&&(!auto))
		{
			if(mob.isMonster())
			{
				R=CMClass.randomRace();
				for(int i=0;i<10;i++)
				{
					if((R!=null)
					&&(CMProps.isTheme(R.availabilityCode()))
					&&(R!=mob.charStats().getMyRace()))
						break;
					R=CMClass.randomRace();
				}
			}
			else
			{
				mob.tell(L("You can't turn @x1 into a '@x2'!",target.name(mob),race));
				return false;
			}
		}
		else
		if(R==null)
		{
			R=CMClass.randomRace();
			for(int i=0;i<10;i++)
			{
				if((R!=null)
				&&(CMProps.isTheme(R.availabilityCode()))
				&&(R!=mob.charStats().getMyRace()))
					break;
				R=CMClass.randomRace();
			}
		}

		if(target.baseCharStats().getMyRace() != target.charStats().getMyRace())
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already polymorphed."));
			return false;
		}

		if((R!=null)&&(!CMath.bset(R.availabilityCode(),Area.THEME_FANTASY)))
		{
			mob.tell(L("You can't turn @x1 into a '@x2'!",target.name(mob),R.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int targetStatTotal=0;
		final MOB fakeMOB=CMClass.getFactoryMOB();
		for(final int s: CharStats.CODES.BASECODES())
		{
			targetStatTotal+=target.baseCharStats().getStat(s);
			fakeMOB.baseCharStats().setStat(s,target.baseCharStats().getStat(s));
		}
		fakeMOB.baseCharStats().setMyRace(R);
		fakeMOB.recoverCharStats();
		fakeMOB.recoverPhyStats();
		fakeMOB.recoverMaxState();
		fakeMOB.recoverCharStats();
		fakeMOB.recoverPhyStats();
		fakeMOB.recoverMaxState();
		int fakeStatTotal=0;
		for(final int s: CharStats.CODES.BASECODES())
			fakeStatTotal+=fakeMOB.charStats().getStat(s);

		int statDiff=targetStatTotal-fakeStatTotal;
		boolean noxp=false;
		if(CMLib.flags().canMove(fakeMOB)!=CMLib.flags().canMove(target))
		{
			statDiff+=75;
			noxp=true;
		}
		if(CMLib.flags().canBreatheHere(fakeMOB,target.location())!=CMLib.flags().canBreatheHere(target,target.location()))
		{
			statDiff+=40;
			noxp = true;
		}
		if(CMLib.flags().canSee(fakeMOB)!=CMLib.flags().canSee(target))
			statDiff+=25;
		if(CMLib.flags().canHear(fakeMOB)!=CMLib.flags().canHear(target))
			statDiff+=10;
		if(CMLib.flags().canSpeak(fakeMOB)!=CMLib.flags().canSpeak(target))
			statDiff+=25;
		if(CMLib.flags().canSmell(fakeMOB)!=CMLib.flags().canSmell(target))
			statDiff+=5;
		fakeMOB.destroy();

		if(statDiff<0)
			statDiff=statDiff*-1;
		final int levelDiff=((mob.phyStats().level()+(2*getXLEVELLevel(mob)))-target.phyStats().level());
		boolean success=proficiencyCheck(mob,levelDiff-statDiff,auto);
		if(success&&(!auto)&&(!mob.mayIFight(target))&&(mob!=target)&&(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)))
		{
			mob.tell(L("@x1 is a player, so you must be group members, or your playerkill flags must be on for this to work.",target.name(mob)));
			success=false;
		}

		if((success)&&((auto)||((levelDiff-statDiff)>-100)))
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> form(s) an improved spell around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=R;
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) a @x1!",newRace.name()));
					Spell_ImprovedPolymorph morph = (Spell_ImprovedPolymorph) beneficialAffect(mob,target,asLevel,0); 
					if(morph != null)
					{
						success=true;
						morph.noxp = noxp;
					}
					else
						success=false;
					target.recoverCharStats();
					CMLib.utensils().confirmWearability(target);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> form(s) an improved spell around <T-NAMESELF>, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}

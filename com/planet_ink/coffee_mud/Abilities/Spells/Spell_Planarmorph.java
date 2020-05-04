package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
public class Spell_Planarmorph extends Spell
{
	@Override
	public String ID()
	{
		return "Spell_Planarmorph";
	}

	private final static String localizedName = CMLib.lang().L("Planarmorph");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planarmorph)");

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
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	protected Race					newRace		= null;
	protected final List<Ability>	fakeEffects	= new Vector<Ability>();
	protected int[]					lastSet		= null;
	protected int[]					newSet		= null;
	protected int					addAtmo		= -1;

	protected String getPlanarTarget()
	{
		return "";
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		final Race R=getMixRace();
		if(R!=null)
		{
			if(affected.name().indexOf(' ')>0)
				affectableStats.setName(L("@x1 called @x2",CMLib.english().startWithAorAn(R.name()),affected.name()));
			else
				affectableStats.setName(L("@x1 the @x2",affected.name(),R.name()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			R.setHeightWeight(affectableStats,'M');
			if(oldAdd>0)
				affectableStats.setWeight(affectableStats.weight()+oldAdd);
			for(final Ability A : fakeEffects)
				A.affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		for(final Ability A : fakeEffects)
			A.executeMsg(myHost, msg);
		return;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		for(final Ability A : fakeEffects)
		{
			if(!A.okMessage(myHost, msg))
				return false;
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		for(final Ability A : fakeEffects)
		{
			if(!A.tick(ticking, tickID))
				return false;
		}
		if(ticking instanceof Physical)
		{
			if(!text().equalsIgnoreCase(CMLib.flags().getPlaneOfExistence((Physical)ticking)))
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final Race R=getMixRace();
		if(R!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(R);
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,R.getAgingChart()[oldCat]);
			for(final Ability A : fakeEffects)
				A.affectCharStats(affected, affectableStats);
			final int[] breatheables=affectableStats.getBreathables();
			if(breatheables.length>0)
			{
				if((lastSet!=breatheables)||(newSet==null))
				{
					if(CMParms.contains(affectableStats.getBreathables(), addAtmo))
						newSet=affectableStats.getBreathables();
					else
					{
						newSet=Arrays.copyOf(affectableStats.getBreathables(),affectableStats.getBreathables().length+1);
						newSet[newSet.length-1]=addAtmo;
						Arrays.sort(newSet);
					}
					lastSet=breatheables;
				}
				affectableStats.setBreathables(newSet);
			}
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected,affectableStats);
		final Race R=getMixRace();
		if(R!=null)
		{
			for(final Ability A : fakeEffects)
				A.affectCharState(affected, affectableStats);
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> morph(s) back into <S-HIM-HERSELF> again."));
	}

	protected Race getMixRace()
	{
		if((newRace==null)
		&&(affected instanceof MOB)
		&&(miscText.length()>0))
		{
			final MOB target=(MOB)affected;
			final PlanarAbility plane =(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
			newRace=target.charStats().getMyRace();
			this.fakeEffects.clear();
			if(plane != null)
			{
				plane.setMiscText(text());
				final Map<String,String> planarVars = plane.getPlaneVars();
				if((planarVars != null)&&(planarVars.size()>0))
				{
					final String mixRace=planarVars.get(PlanarVar.MIXRACE.name());
					if((mixRace != null)
					&& (mixRace.length()>0)
					&& (CMClass.getRace(mixRace) != null))
					{
						newRace=CMLib.utensils().getMixedRace(mixRace, target.charStats().getMyRace().ID(), false);
						if(newRace != null)
						{
							if(target.location()!=null)
								target.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) a @x1!",newRace.name()));
							target.recoverCharStats();
							target.recoverPhyStats();
							CMLib.utensils().confirmWearability(target);
							final String adjStat = planarVars.get(PlanarVar.ADJSTAT.toString());
							if(adjStat != null)
							{
								final Ability A=CMClass.getAbility("Prop_StatAdjuster");
								A.setAffectedOne(target);
								A.setMiscText(adjStat);
								A.setInvoker(target);
								fakeEffects.add(A);
							}
							final String resistWeak = planarVars.get(PlanarVar.MOBRESIST.toString());
							if(resistWeak != null)
							{
								final Ability A=CMClass.getAbility("Prop_Resistance");
								A.setAffectedOne(target);
								A.setMiscText(resistWeak);
								A.setInvoker(target);
								fakeEffects.add(A);
							}
							final String atmosphere = planarVars.get(PlanarVar.ATMOSPHERE.toString());
							if((atmosphere!=null)&&(atmosphere.length()>0))
							{
								final int atmo=RawMaterial.CODES.FIND_IgnoreCase(atmosphere);
								this.addAtmo = atmo;
							}
							int eliteLevel=0;
							if(planarVars.containsKey(PlanarVar.ELITE.toString()))
								eliteLevel=CMath.s_int(planarVars.get(PlanarVar.ELITE.toString()));
							final String adjSize = planarVars.get(PlanarVar.ADJSIZE.toString());
							if(adjSize != null)
							{
								final double heightAdj = CMParms.getParmDouble(adjSize, "HEIGHT", Double.MIN_VALUE);
								final double weightAdj = CMParms.getParmDouble(adjSize, "WEIGHT", Double.MIN_VALUE);
								if((weightAdj > Double.MIN_VALUE)||(heightAdj > Double.MIN_VALUE))
								{
									final Ability A=CMClass.getAbility("Prop_Adjuster");
									A.setAffectedOne(target);
									String adjStr = "";
									if(weightAdj > Double.MIN_VALUE)
										adjStr="weightadj="+(int)Math.round(CMath.mul(target.baseWeight(),weightAdj))+" ";
									if(heightAdj > Double.MIN_VALUE)
									{
										if(eliteLevel > 0)
											adjStr += "height+"+(100+(heightAdj*100));
										else
											adjStr += "height+"+(int)Math.round(CMath.mul(target.basePhyStats().height(),heightAdj));
									}
									A.setMiscText(adjStr);
									A.setInvoker(target);
									fakeEffects.add(A);
								}
							}
						}
					}
				}
			}
		}
		return newRace;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		newRace = null;
		getMixRace(); // do work, if able
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		final String planeName;
		if(this.getPlanarTarget().length()>0)
			planeName = this.getPlanarTarget();
		else
		{
			planeName = CMLib.flags().getPlaneOfExistence(target.location());
			if(planeName == null)
			{
				mob.tell(L("This magic does not do anything on the Prime Material plane!"));
				return false;
			}
		}
		final PlanarAbility plane =(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		if(plane == null)
			return false;
		plane.setMiscText(planeName);
		final Map<String,String> planarVars = plane.getPlaneVars();
		final String mixRace=planarVars.get(PlanarVar.MIXRACE.name());
		if((mixRace == null) || (mixRace.length()==0) || (CMClass.getRace(mixRace) == null))
		{
			mob.tell(L("This magic would not do anything on this plane of existence!"));
			return false;
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already polymorphed."));
			return false;
		}

		if(target.baseCharStats().getMyRace() != target.charStats().getMyRace())
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already polymorphed."));
			return false;
		}

		final Race R=CMLib.utensils().getMixedRace(mixRace, target.charStats().getMyRace().ID(), false);
		if(R==null)
		{
			mob.tell(L("This magic does not seem to work for you here!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int mobStatTotal=0;
		for(final int s: CharStats.CODES.BASECODES())
			mobStatTotal+=mob.baseCharStats().getStat(s);

		final MOB fakeMOB=CMClass.getFactoryMOB();
		for(final int s: CharStats.CODES.BASECODES())
			fakeMOB.baseCharStats().setStat(s,mob.baseCharStats().getStat(s));
		fakeMOB.baseCharStats().setMyRace(R);
		fakeMOB.recoverCharStats();
		fakeMOB.recoverPhyStats();
		fakeMOB.recoverMaxState();
		int fakeStatTotal=0;
		for(final int s: CharStats.CODES.BASECODES())
			fakeStatTotal+=fakeMOB.charStats().getStat(s);

		fakeMOB.destroy();
		final int statDiff=mobStatTotal-fakeStatTotal;
		boolean success=proficiencyCheck(mob,(statDiff*5),auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> incant(s) to <T-NAMESELF> about @x1.^?",CMLib.english().makePlural(R.name())));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					//mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) a @x1!",CMLib.english().startWithAorAn(R.name())));
					final Ability cA = beneficialAffect(mob,target,asLevel,0);
					if(cA != null)
					{
						success = true;
						cA.setMiscText(planeName);
					}
					target.recoverCharStats();
					CMLib.utensils().confirmWearability(target);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> incant(s) to <T-NAMESELF>, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}

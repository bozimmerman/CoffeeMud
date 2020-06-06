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
public class Spell_TrueName extends Spell
{
	@Override
	public String ID()
	{
		return "Spell_TrueName";
	}

	private final static String localizedName = CMLib.lang().L("True Name");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	protected volatile MOB monster=null;
	protected volatile String trueName=null;
	protected volatile Long monsterAppearanceTime=null;

	protected final static Map<String,String> trueNames = new TreeMap<String,String>();

	final Runnable periodicCheckToGoHome = new Runnable()
	{
		@Override
		public void run()
		{
			final MOB mob=monster;
			final Long tm=monsterAppearanceTime;
			if((mob!=null)
			&&(tm!=null)
			&&(!mob.amDead())
			&&(CMLib.flags().isInTheGame(mob, true)))
			{
				if(mob.fetchEffect("Spell_Geas")!=null)
				{
					monsterAppearanceTime=new Long(tm.longValue()+4000);
					CMLib.threads().scheduleRunnable(periodicCheckToGoHome, 4000);
				}
				else
				if(System.currentTimeMillis()>tm.longValue())
				{
					CMLib.tracking().wanderAway(mob, false, false);
					mob.destroy();
					monster=null;
					monsterAppearanceTime=null;
				}
				else
					CMLib.threads().scheduleRunnable(periodicCheckToGoHome, 4000);
			}
		}
	};

	protected String getTrueName()
	{
		if(trueName!=null)
			return trueName;
		if(text().length()>0)
		{
			final int x=text().indexOf(';');
			if(x<0)
				return null;
			trueName=text().substring(0,x);
		}
		return trueName;
	}

	protected MOB getMonster()
	{
		if(monster!=null)
			return monster;
		if(text().length()>0)
		{
			final int x=text().indexOf(';');
			if(x<0)
				return monster;
			trueName=text().substring(0,x);
			monster=CMLib.coffeeMaker().getMobFromXML(text().substring(x+1));
			monsterAppearanceTime=Long.valueOf(System.currentTimeMillis()+(5*60*1000));
		}
		return monster;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMessage()!=null)
		&&(getTrueName()!=null))
		{
			if(getTrueName().equalsIgnoreCase(CMStrings.getSayFromMessage(msg.sourceMessage())))
			{
				final Spell_TrueName mt=this;
				msg.addTrailerRunnable(new Runnable()
				{
					private final Spell_TrueName me=mt;
					private final MOB caster = msg.source();
					@Override
					public void run()
					{
						final Room R=caster.location();
						if((R!=null)
						&&(R.isInhabitant(caster)))
						{
							final MOB monster;
							if(me.monster == null)
							{
								monster=me.getMonster();
								final Ability charm=CMClass.getAbility("Spell_AweOther");
								charm.setMiscText(caster.Name());
								monster.addNonUninvokableEffect(charm);
								monster.text();
								monster.bringToLife(R,true);
								CMLib.threads().scheduleRunnable(periodicCheckToGoHome, 4000);
							}
							else
							{
								if(CMLib.flags().isInTheGame(me.monster, true))
								{
									monster=me.monster;
									if(!R.isInhabitant(monster))
									{
										final Ability A=me.monster.fetchEffect("Spell_Geas");
										if(A!=null)
										{
											A.unInvoke();
											monster.delEffect(A);
											R.bringMobHere(monster, false);
											R.show(monster, null, CMMsg.MSG_OK_VISUAL, "<S-NAME> appears!");
										}
									}
								}
								else
									return;
							}
							switch(CMLib.dice().roll(1, 7, -1))
							{
							case 0:
								CMLib.commands().postSay(monster, caster, "What is thy bidding, my master.", false,false);
								break;
							case 1:
								CMLib.commands().postSay(monster, caster, "What? More work?", false,false);
								break;
							case 2:
								CMLib.commands().postSay(monster, caster, "Yes?", false,false);
								break;
							case 3:
								CMLib.commands().postSay(monster, caster, "Something to do?", false,false);
								break;
							case 4:
								CMLib.commands().postSay(monster, caster, "What?!", false,false);
								break;
							case 5:
								CMLib.commands().postSay(monster, caster, "Yes, milord?", false,false);
								break;
							case 6:
								CMLib.commands().postSay(monster, caster, "What is it?", false,false);
								break;
							}
						}
					}
				});
			}
			else
			if((monster!=null)
			&&(msg.target()==monster)
			&&(!CMStrings.getSayFromMessage(msg.sourceMessage()).equalsIgnoreCase(getTrueName())))
			{
				Ability A=monster.fetchEffect("Spell_Geas");
				if(A==null)
				{
					final String geas=CMStrings.getSayFromMessage(msg.sourceMessage());
					A=CMClass.getAbility("Spell_Geas");
					final Vector<String> cmds=new XVector<String>(monster.Name(), geas);
					A.invoke(msg.source(), cmds, monster, true, 0);
				}
			}
		}
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		this.monster=null;
		this.trueName=null;
		super.setMiscText(newMiscText);
		getTrueName();
	}


	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return false;
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked()&&(!mob.amDead())))
		{
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!CMLib.flags().canSpeak(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if(CMLib.flags().getPlaneOfExistence(((MOB)target).getStartRoom())==null)
					return Ability.QUALITY_INDIFFERENT;
				if(!((MOB)target).isMonster())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;

		if(!CMLib.flags().canSpeak(mob))
		{
			mob.tell(L("You can't speak!"));
			return false;
		}

		if((!auto)&&(!CMLib.flags().canBeHeardSpeakingBy(mob,target)))
		{
			mob.tell(L("@x1 can't hear your words.",target.charStats().HeShe()));
			return false;
		}

		if(CMLib.flags().getPlaneOfExistence(target.getStartRoom())==null)
		{
			mob.tell(L("@x1 would not be affected by their true name.",target.charStats().HeShe()));
			return false;
		}

		if((target.fetchEffect("Spell_TrueName")!=null)
		||(target.fetchEffect("Spell_Geas")!=null))
		{
			mob.tell(L("@x1 is already under a powerful geas.",target.charStats().HeShe()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,-25-((target.charStats().getStat(CharStats.STAT_WISDOM)*2)+(levelDiff*15)),auto);
		if(success)
		{
			final String trueName;
			if(trueNames.containsKey(target.Name()))
				trueName=trueNames.get(target.Name());
			else
			{
				trueName = CMLib.login().generateRandomName(2, 6);
				while(trueNames.size()>100)
					trueNames.remove(trueNames.keySet().iterator().next());
				trueNames.put(target.Name(), trueName);
			}
			final String str=auto?"":L("^S<S-NAME> speak(s) <T-YOUPOSS> True Name: '"+trueName+"'!.^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_VERBAL_SPELL|CMMsg.MASK_MALICIOUS,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Spell_TrueName A=(Spell_TrueName)beneficialAffect(mob, mob, asLevel, (int)CMProps.getTicksPerDay());
					if(A!=null)
					{
						final StringBuffer xml=CMLib.coffeeMaker().getMobXML(target);
						A.setMiscText(trueName+";"+xml.toString());
						if(target.isInCombat())
							target.makePeace(true);
						CMLib.combat().makePeaceInGroup(mob);
						switch(CMLib.dice().roll(1, 7, -1))
						{
						case 0:
							CMLib.commands().postSay(target, mob, "Argh!  Speak no more!!!", false,false);
							break;
						case 1:
							CMLib.commands().postSay(target, mob, "How dare you!", false,false);
							break;
						case 2:
							CMLib.commands().postSay(target, mob, "Yes?", false,false);
							break;
						case 3:
							CMLib.commands().postSay(target, mob, "Something to do?", false,false);
							break;
						case 4:
							CMLib.commands().postSay(target, mob, "What?!", false,false);
							break;
						case 5:
							CMLib.commands().postSay(target, mob, "Yes, milord?", false,false);
							break;
						case 6:
							CMLib.commands().postSay(target, mob, "What is it?", false,false);
							break;
						}
						mob.location().show(target, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME> flee(s)..."));
						target.killMeDead(false);
					}
					else
						success=false;
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to divine <T-YOUPOSS> name, but fails."));

		// return whether it worked
		return success;
	}
}

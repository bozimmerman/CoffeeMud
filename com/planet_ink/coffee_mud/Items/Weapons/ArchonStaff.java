package com.planet_ink.coffee_mud.Items.Weapons;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class ArchonStaff extends Staff implements Wand, MiscMagic, ArchonOnly
{
	@Override
	public String ID()
	{
		return "ArchonStaff";
	}

	private static Wand theWand=(Wand)CMClass.getMiscMagic("StdWand");
	protected final static String[] MAGIC_WORDS={"LEVEL","RESTORE","REFRESH","BLAST","BURN","GAIN","REWIND"};
	protected AtomicBoolean noRecurse = new AtomicBoolean(false);

	public ArchonStaff()
	{
		super();

		setName("a wooden staff");
		setDisplayText("a wooden staff lies in the corner of the room.");
		setDescription("It`s long and wooden, just like a staff ought to be.");
		secretIdentity="The Archon`s Staff of Power!";
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(30);
		basePhyStats.setWeight(4);
		basePhyStats().setAttackAdjustment(10);
		basePhyStats().setDamage(12);
		baseGoldValue=10000;
		recoverPhyStats();
		wornLogicalAnd=true;
		material=RawMaterial.RESOURCE_OAK;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		weaponDamageType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		if(theWand==null)
			theWand=(Wand)CMClass.getMiscMagic("StdWand");
		secretWord="REFRESH, RESTORE, REWIND, BLAST, LEVEL X UP, LEVEL X DOWN, BURN, GAIN X UP";
	}

	@Override
	public int getMaxCharges()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void setMaxCharges(final int newMaxUses)
	{
	}

	@Override
	public void setSpell(final Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="REFRESH, RESTORE, REWIND, BLAST, LEVEL X UP, LEVEL X DOWN, BURN, GAIN X UP";
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		secretWord="REFRESH, RESTORE, REWIND, BLAST, LEVEL X UP, LEVEL X DOWN, BURN, GAIN X UP";
	}

	public boolean safetyCheck(final MOB mob, final String message)
	{
		if((!mob.isMonster())
		&&(message.length()>0)
		&&(mob.session().getPreviousCMD()!=null)
		&&(CMParms.combine(mob.session().getPreviousCMD(),0).toUpperCase().indexOf(message.toUpperCase())<0)
		&&((!mob.isPlayer())||(mob.isMonster())||(mob.playerStats().getAlias(mob.session().getPreviousCMD().get(0))==null)))
		{
			mob.tell(L("The wand fizzles in an irritating way."));
			return false;
		}
		return true;
	}

	@Override
	public boolean checkWave(final MOB mob, final String message)
	{
		if(message==null)
			return false;
		final List<String> parms=CMParms.cleanParameterList(message.toUpperCase());
		for (final String element : MAGIC_WORDS)
		{
			if(parms.contains(element))
			{
				return (mob.isMine(this)) && (amBeingWornProperly());
			}
		}
		return super.checkWave(mob, message);
	}

	@Override
	public void waveIfAble(final MOB mob, final Physical afftarget, String message)
	{
		if((mob.isMine(this))
		&&(amBeingWornProperly()))
		{
			if((mob.location()!=null)&&(afftarget!=null)&&(afftarget instanceof MOB))
			{
				final MOB target=(MOB)afftarget;
				if(message.toUpperCase().indexOf("LEVEL ALL UP")>0)
				{
					if(!safetyCheck(mob,message))
						return;
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
					int destLevel=CMProps.get(target.session()).getInt(CMProps.Int.LASTPLAYERLEVEL);
					if(destLevel==0)
						destLevel=30;
					if(destLevel<=target.basePhyStats().level())
						destLevel=100;
					if((target.charStats().getCurrentClass().leveless())
					||(target.charStats().isLevelCapped(target.charStats().getCurrentClass()))
					||(target.charStats().getMyRace().leveless())
					||(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
						mob.tell(L("The wand will not work on such as @x1.",target.name(mob)));
					else
					{
						int tries = 100 * destLevel;
						while((target.phyStats().level()<destLevel)&&(--tries>0))
						{
							if((target.getExpNeededLevel()==Integer.MAX_VALUE)
							||(target.charStats().getCurrentClass().expless())
							||(target.charStats().getMyRace().expless())
							||(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0))
								CMLib.leveler().level(target);
							else
								CMLib.leveler().postExperience(target,"MISC:"+ID(),null,null,target.getExpNeededLevel()+1, false);
						}
					}
				}
				else
				if(message.toUpperCase().startsWith("LEVEL ")&&message.toUpperCase().endsWith(" UP"))
				{
					if(!safetyCheck(mob,message))
						return;
					message=message.substring(6).trim();
					message=message.substring(0,message.length()-2).trim();
					int num=1;
					if(CMath.isInteger(message))
						num=CMath.s_int(message);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
					if((target.charStats().getCurrentClass().leveless())
					||(target.charStats().isLevelCapped(target.charStats().getCurrentClass()))
					||(target.charStats().getMyRace().leveless())
					||(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
						mob.tell(L("The wand will not work on such as @x1.",target.name(mob)));
					else
					{
						for(int i=0;i<num;i++)
						{
							final int nextLevel = target.phyStats().level()+1;
							int tries = 100;
							while((target.phyStats().level()<nextLevel)&&(--tries>0))
							{
								if((target.getExpNeededLevel()==Integer.MAX_VALUE)
								||(target.charStats().getCurrentClass().expless())
								||(target.charStats().getMyRace().expless())
								||(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0))
									CMLib.leveler().level(target);
								else
									CMLib.leveler().postExperience(target,"MISC:"+ID(),null,null,target.getExpNeededLevel()+1, false);
							}
						}
					}
					return;
				}
				else
				if(message.toUpperCase().startsWith("GAIN ")&&message.toUpperCase().endsWith(" UP"))
				{
					if(!safetyCheck(mob,message))
						return;
					message=message.substring(5).trim();
					message=message.substring(0,message.length()-2).trim();
					if((message.length()>0)&&(!message.equalsIgnoreCase("ALL")))
					{
						Ability A=CMClass.getAbility(message);
						if(A==null)
							A=CMClass.findAbility(message);
						if(A==null)
							mob.tell(L("There is no such skill as @x1.",message.toLowerCase()));
						else
						if((target.fetchAbility(A.ID())!=null)&&(target.fetchAbility(A.ID()).proficiency()>=100))
							mob.tell(L("@x1 is already proficient in @x2.",target.Name(),message.toLowerCase()));
						else
						{
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
							if(target.fetchAbility(ID())!=null)
								target.fetchAbility(ID()).setProficiency(100);
							else
							{
								A.setProficiency(100);
								target.addAbility(A);
								A.setSavable(true);
								A.autoInvocation(target, false);
							}
						}
					}
					else
					{
						final CharClass C=target.charStats().getCurrentClass();
						boolean didSomething = false;
						for(final Enumeration<AbilityMapping> amap = CMLib.ableMapper().getClassAbles(C.ID(), true);amap.hasMoreElements();)
						{
							final AbilityMapping map = amap.nextElement();
							if(target.fetchAbility(map.abilityID()) != null)
							{
								final Ability A=target.fetchAbility(map.abilityID());
								if(A.proficiency()<100)
								{
									A.setProficiency(100);
									didSomething = true;
								}
							}
							else
							{
								final Ability A=CMClass.getAbility(map.abilityID());
								A.setSavable(true);
								A.setProficiency(100);
								A.setMiscText(map.defaultParm());
								target.addAbility(A);
								A.autoInvocation(target, false);
								didSomething = true;
							}
						}
						if(didSomething)
							mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));

					}
					return;
				}
				else
				if(message.toUpperCase().startsWith("LEVEL ")&&message.toUpperCase().endsWith(" DOWN"))
				{
					if(!safetyCheck(mob,message))
						return;
					message=message.substring(6).trim();
					message=message.substring(0,message.length()-4).trim();
					int num=1;
					if(CMath.isInteger(message))
						num=CMath.s_int(message);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
					if((target.charStats().getCurrentClass().leveless())
					||(target.charStats().isLevelCapped(target.charStats().getCurrentClass()))
					||(target.charStats().getMyRace().leveless())
					||(CMSecurity.isDisabled(CMSecurity.DisFlag.UNLEVEL))
					||(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
						mob.tell(L("The wand will not work on such as @x1.",target.name(mob)));
					else
					for(int i=0;i<num;i++)
					{
						if((target.getExpNeededLevel()==Integer.MAX_VALUE)
						||(target.charStats().getCurrentClass().expless())
						||(target.charStats().getMyRace().expless())
						||(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0))
							CMLib.leveler().unLevel(target, true);
						else
						{
							final int oldLevel = target.basePhyStats().level();
							for(int x=0;(x<10) && (oldLevel == target.basePhyStats().level());x++)
							{
								final int xpLevelBelow=CMLib.leveler().getLevelExperience(mob, oldLevel-2);
								int levelDown=(target.getExperience()-xpLevelBelow)+1;
								if(levelDown > target.getExperience())
									levelDown = target.getExperience();
								CMLib.leveler().postExperience(target,"MISC:"+ID(),null,null,-levelDown, false);
							}
						}
					}
					return;
				}
				else
				if(message.toUpperCase().indexOf("RESTORE")>=0)
				{
					if(!safetyCheck(mob,message))
						return;
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
					final List<Ability> diseaseV=CMLib.flags().domainAffects(target,Ability.ACODE_DISEASE);
					if (diseaseV.size() > 0)
					{
						final Ability A = CMClass.getAbility("Prayer_CureDisease");
						if (A != null)
							A.invoke(mob, target, true, 0);
					}
					final List<Ability> poisonV = CMLib.flags().domainAffects(target, Ability.ACODE_POISON);
					if (poisonV.size() > 0)
					{
						final Ability A = CMClass.getAbility("Prayer_RemovePoison");
						if (A != null)
							A.invoke(mob, target, true, 0);
					}
					final Ability bleed = target.fetchEffect("Bleeding");
					if (bleed != null)
					{
						bleed.unInvoke();
						target.delEffect(bleed);
					}
					final Ability injury = target.fetchEffect("Injury");
					if (injury != null)
					{
						injury.unInvoke();
						target.delEffect(injury);
					}
					final Ability ampu = target.fetchEffect("Amputation");
					if (ampu != null)
					{
						ampu.unInvoke();
						target.delEffect(ampu);
					}
					final Ability brok = target.fetchEffect("BrokenLimbs");
					if (brok != null)
					{
						brok.unInvoke();
						target.delEffect(brok);
					}
					final Ability scarA = target.fetchEffect("Scarring");
					if (scarA != null)
					{
						scarA.unInvoke();
						target.delEffect(scarA);
					}

					target.recoverMaxState();
					target.resetToMaxState();
					target.tell(L("You feel refreshed!"));
					return;
				}
				else
				if(message.equals("REWIND"))
				{
					if(!safetyCheck(mob,message))
						return;
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
					boolean didsomething=false;
					for(final Enumeration<Ability> a=target.abilities();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)
						&&(CMath.s_long(A.getStat("NEXTCAST"))>0))
						{
							A.setStat("NEXTCAST", "0");
							didsomething=true;
						}
					}
					if(didsomething)
						target.tell(L("You feel refreshed!"));
					else
						mob.tell(L("Nothing happened."));
					return;
				}
				else
				if(message.toUpperCase().indexOf("REFRESH")>=0)
				{
					if(!safetyCheck(mob,message))
						return;
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly at <T-NAME>.",this.name()));
					final Ability bleed = target.fetchEffect("Bleeding");
					if (bleed != null)
					{
						bleed.unInvoke();
						target.delEffect(bleed);
					}
					target.recoverMaxState();
					target.resetToMaxState();
					target.tell(L("You feel refreshed!"));
					return;
				}
				else
				if(message.toUpperCase().equals("BLAST"))
				{
					if(!safetyCheck(mob,message))
						return;
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 zaps <T-NAME> with unworldly energy.",this.name()));
					target.curState().setHitPoints(1);
					target.curState().setMana(1);
					target.curState().setMovement(1);
					return;
				}
				else
				if(message.toUpperCase().indexOf("BURN")>=0)
				{
					if(!safetyCheck(mob,message))
						return;
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("@x1 wielded by <S-NAME> shoots forth magical green flames at <T-NAME>.",this.name()));
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= 3;
					CMLib.combat().postDamage(mob,target,null,(++flameDamage),CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,(this.name()+" <DAMAGE> <T-NAME>!")+CMLib.protocol().msp("fireball.wav",30));
					return;
				}
			}
		}
		if(this.getSpell()!=null)
			StdWand.waveIfAble(mob,afftarget,message,this);
	}

	@Override
	public void affectCharState(final MOB mob, final CharState affectableState)
	{
		super.affectCharState(mob,affectableState);
		if(amBeingWornProperly())
		{
			affectableState.setHunger(99999999);
			affectableState.setThirst(99999999);
			mob.curState().setHunger(9999999);
			mob.curState().setThirst(9999999);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		final MOB mob=msg.source();
		if(mob.location()==null)
			return true;

		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_HOLD:
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_GET:
		case CMMsg.TYP_PUSH:
		case CMMsg.TYP_PULL:
			if(!CMSecurity.isASysOp(msg.source()))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 flashes and falls out of <S-HIS-HER> hands!",name()));
				return false;
			}
			break;
		}
		if((msg.targetMinor()==CMMsg.TYP_SPEAK)&&(msg.sourceMessage()!=null))
		{
			String said=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(said!=null)
			{
				said=said.trim().toUpperCase();
				final int x=said.indexOf(' ');
				if(x>0)
					said=said.substring(0,x);
				if(CMParms.indexOf(MAGIC_WORDS, said)>=0)
					super.secretWord=said;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.source().location()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead())
		&&(!noRecurse.get()))
		{
			noRecurse.set(true);
			try
			{
				final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
				if(msg.source().location().okMessage(msg.source(),msg2))
				{
					msg.source().location().send(msg.source(), msg2);
					if(msg2.value()<=0)
					{
						int flameDamage = (int) Math.round( Math.random() * 6 );
						flameDamage *= basePhyStats().level();
						if(!((MOB)msg.target()).amDead())
							CMLib.combat().postDamage(msg.source(),(MOB)msg.target(),null,flameDamage,CMMsg.TYP_FIRE,
									Weapon.TYPE_BURNING,L("@x1 shoots a flame which <DAMAGE> <T-NAME>!",name()));
					}
				}
			}
			finally
			{
				noRecurse.set(false);
			}
		}
	}
}

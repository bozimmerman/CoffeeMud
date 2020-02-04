package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/**
 *
 * Title: False Realities Flavored CoffeeMUD
 * Description: The False Realities Version of CoffeeMUD
 * Copyright: Copyright (c) 2003 Jeremy Vyska
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Company: http://www.falserealities.com
 *
 *
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */
public class Prop_ClanEquipment extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ClanEquipment";
	}

	@Override
	public String name()
	{
		return "Clan Equipment";
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_CASTER;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected boolean	activated		= false;
	protected String	type			= "";
	protected int		typeOfEffect	= 0;
	protected int		weaponType		= 0;
	protected int		powerLevel		= 0;
	protected long		lastChecked		= 0;
	protected boolean	notAgain		= false;
	protected String	clanName		= "";
	protected String	clanType		= "";
	String				lastMessage		= null;
	protected CharStats	eqAdjCharStats	= null;
	protected String	secretWord		= CMProps.getAnyListFileValue(CMProps.ListFile.MAGIC_WORDS);

	@Override
	public int triggerMask()
	{
		if(affected instanceof Weapon)
			return TriggeredAffect.TRIGGER_HITTING_WITH;
		if(affected instanceof Armor)
			return TriggeredAffect.TRIGGER_BEING_HIT;
		return TriggeredAffect.TRIGGER_USE;
	}

	@Override
	public String accountForYourself()
	{
		// My slightly complicated way of showing the clan effect when ID'd
		final StringBuffer id=new StringBuffer(L("@x1 @x2 Bonus: ",clanType,clanName));
		if((affected instanceof Weapon)&&(!(affected instanceof Wand))&&(typeOfEffect<1000))
		{
			id.append("Does "+(1*powerLevel)+"-"+(6*powerLevel)+" additional "+type.toLowerCase()+" damage.");
		}
		else
		if((affected instanceof Armor)&&(typeOfEffect<1000)&&(!(affected instanceof Shield)))
		{
			id.append("Raises "+type.toLowerCase()+" resistance by "+(powerLevel*5)+".");
		}
		if((affected instanceof Armor)&&(typeOfEffect<1000)&&(affected instanceof Shield))
		{
			id.append("Causes "+(1*powerLevel)+"-"+(3*powerLevel)+" "+type.toLowerCase()+" damage to melee attackers.");
		}
		if((affected instanceof Wand)&&(typeOfEffect<1000))
		{
			id.append("Does "+(1*powerLevel)+"-"+(6*powerLevel)+" "+type.toLowerCase()+" damage when the user says `"+secretWord+"` to the target.");
		}
		return id.toString();
	}

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		final Vector<String> V=CMParms.parse(text);
		if(V.size()<4)
		{
			return;
		}
		type=V.elementAt(0);
		powerLevel=Integer.valueOf(V.elementAt(1)).intValue();
		clanName=V.elementAt(2);
		clanType=V.elementAt(3);
		secretWord=getWandWord(text); // try to randomize the spell word a
										// little
		// Armor
		this.eqAdjCharStats=(CharStats)CMClass.getCommon("DefaultCharStats");
		initAdjustments(eqAdjCharStats);
		if(type.equalsIgnoreCase("PARALYSIS"))
		{
			typeOfEffect=CMMsg.TYP_PARALYZE;
			weaponType=Weapon.TYPE_STRIKING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_PARALYSIS,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("FIRE"))
		{
			typeOfEffect=CMMsg.TYP_FIRE;
			weaponType=Weapon.TYPE_BURNING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_FIRE,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("COLD"))
		{
			typeOfEffect=CMMsg.TYP_COLD;
			weaponType=Weapon.TYPE_FROSTING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_COLD,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("WATER"))
		{
			typeOfEffect=CMMsg.TYP_WATER;
			weaponType=Weapon.TYPE_SLASHING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_WATER,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("GAS"))
		{
			typeOfEffect=CMMsg.TYP_GAS;
			weaponType=Weapon.TYPE_GASSING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_GAS,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("MIND"))
		{
			typeOfEffect=CMMsg.TYP_MIND;
			weaponType=Weapon.TYPE_STRIKING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_MIND,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("ACID"))
		{
			typeOfEffect=CMMsg.TYP_ACID;
			weaponType=Weapon.TYPE_MELTING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_ACID,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("ELECTRIC"))
		{
			typeOfEffect=CMMsg.TYP_ELECTRIC;
			weaponType=Weapon.TYPE_BURNING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_ELECTRIC,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("POISON"))
		{
			typeOfEffect=CMMsg.TYP_POISON;
			weaponType=Weapon.TYPE_STRIKING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_POISON,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("DISEASE"))
		{
			typeOfEffect=CMMsg.TYP_DISEASE;
			weaponType=Weapon.TYPE_STRIKING;
			this.eqAdjCharStats.setStat(CharStats.STAT_SAVE_DISEASE,powerLevel*5);
		}
		else
		if(type.equalsIgnoreCase("HEALTH"))
		{
			typeOfEffect=1001;
			weaponType=Weapon.TYPE_BURSTING;
		}
		else
		if(type.equalsIgnoreCase("MAGIC"))
		{
			typeOfEffect=1002;
			weaponType=Weapon.TYPE_BURSTING;
		}
		else
		if(type.equalsIgnoreCase("SPEED"))
		{
			typeOfEffect=1003;
		}
	}

	public boolean useAsWand(final MOB mob, final int level)
	{
		int manaRequired=50;
		// For simplicity, there's no charges BUT use costs a flat 10% mana
		manaRequired=(int)CMath.div(mob.maxState().getMana(),10);
		manaRequired-=(5*level);
		if(manaRequired<5)
			manaRequired=5;

		if(manaRequired>mob.curState().getMana())
		{
			mob.tell(L("You don't have enough mana."));
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}

	public static String getWandWord(final String from)
	{
		int hash=from.hashCode();
		if(hash<0)
		{
			hash=hash*-1;
		}
		return CMProps.getListFileChoiceFromIndexedListByHash(CMProps.ListFile.MAGIC_WORDS,hash);
	}

	public boolean checkWave(final MOB mob, final String message, final Wand me)
	{
		if((mob.isMine(me))
		&&(me.amBeingWornProperly()))
		{
			final int x=message.toUpperCase().indexOf(secretWord.toUpperCase());
			return (x>=0);
		}
		return false;
	}

	/*
	 * ********************** Staff/Wand Clan Eq **********************
	 */
	public void waveIfAble(final MOB mob, final Environmental afftarget, String message, final Wand me)
	{
		if((mob.isMine(me))&&(afftarget!=null)&&(afftarget instanceof MOB)&&(!me.amWearingAt(Wearable.IN_INVENTORY)))
		{
			MOB target=null;
			if((mob.location()!=null))
			{
				target=(MOB)afftarget;
			}
			final int x=message.toUpperCase().indexOf(secretWord.toUpperCase());
			if(x>=0)
			{
				message=message.substring(x+secretWord.length());
				final int y=message.indexOf('\'');
				if(y>=0)
				{
					message=message.substring(0,y);
				}
				message=message.trim();
				final Ability wandUse=mob.fetchAbility("Skill_WandUse");
				if((wandUse==null)||(!wandUse.proficiencyCheck(mob,0,false)))
				{
					mob.tell(L("@x1 glows faintly for a moment, then fades.",me.name()));
				}
				else
				{
					wandUse.setInvoker(mob);
					if(useAsWand(mob,wandUse.abilityCode()))
					{
						mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly.",me.name()));
						int flameDamage=CMLib.dice().roll(1,6*powerLevel,1*powerLevel);
						if(flameDamage > mob.phyStats().level())
							flameDamage = mob.phyStats().level();
						CMLib.combat().postDamage(mob,target,null,flameDamage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|typeOfEffect,weaponType,
								L("^F^<FIGHT^>The magic of @x1 <DAMAGE> <T-NAME>!^</FIGHT^>^?",clanName));
						wandUse.helpProficiency(mob, 0);
						return;
					}
				}
			}
		}
	}

	public static void initAdjustments(final CharStats adjCharStats)
	{
		// ensure we get no NULL errors
		for(final int i : CharStats.CODES.SAVING_THROWS())
			adjCharStats.setStat(i,0);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectedStats)
	{
		if(eqAdjCharStats==null)
		{
			setMiscText(text());
			/*
			 * ************************* Armor-based Resistances
			 * *************************
			 */
		}
		if((affected!=null)&&(affected instanceof Armor)&&(!(affected instanceof Shield))&&(activated)
				&&(!((Armor)affected).amWearingAt(Wearable.IN_INVENTORY)))
		{
			for(final int i : CharStats.CODES.SAVING_THROWS())
				affectedStats.setStat(i,affectedStats.getStat(i)+eqAdjCharStats.getStat(i));
		}
		super.affectCharStats(affectedMOB,affectedStats);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((System.currentTimeMillis()-lastChecked)>TimeManager.MILI_HOUR)&&(affected!=null))
		{
			if((clanName!=null)
			&&(clanName.length()>0)
			&&(CMLib.clans().getClanAnyHost(clanName)==null))
				affected.delEffect(this);
			lastChecked=System.currentTimeMillis();
		}
		MOB mob=null;
		MOB source=null;
		if((affected!=null)&&(affected instanceof Item))
		{
			if((((Item)affected).owner()!=null)&&((Item)affected).owner() instanceof MOB)
			{
				mob=(MOB)((Item)affected).owner();
			}
		}
		// if held by the wrong clan, it is inactive.
		if((mob!=null)&&(mob.getClanRole(clanName)!=null))
			activated=true;
		else
			activated=false;
		if(!activated)
			return;
		source=msg.source();
		/*
		 * ********************** Weapon addtl. Damage
		 * **********************
		 */
		if((msg.source().location()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.value()>0)
		&&(msg.tool()==affected)
		&&(!notAgain)
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Weapon)
		&&(!(msg.tool() instanceof Wand))
		&&(typeOfEffect<1000)
		&&(mob!=null)
		&&(!((MOB)msg.target()).amDead()))
		{
			double flameDamage=CMLib.dice().roll(1,6*powerLevel,1*powerLevel);
			if(flameDamage > mob.phyStats().level())
				flameDamage = mob.phyStats().level();
			final String str=L("^F^<FIGHT^>The magic of @x1 <DAMAGE> <T-NAME>!^</FIGHT^>^?",clanName);
			CMLib.combat().postDamage(msg.source(),(MOB)msg.target(),null,(int)Math.round(flameDamage),
					CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|typeOfEffect,weaponType,str);
		}
		/*
		 * ********************** Shield Thorns Damage **********************
		 */
		if((mob!=null)&&(msg.amITarget(mob))&&(affected instanceof Shield)&&(!((Shield)affected).amWearingAt(Wearable.IN_INVENTORY))
		&&(typeOfEffect<1000)&&(!msg.amISource(mob)))
		{
			if((CMLib.dice().rollPercentage()>32+msg.source().charStats().getStat(CharStats.STAT_DEXTERITY))
			&&(msg.source().rangeToTarget()==0)
			&&((lastMessage==null)||(lastMessage.indexOf("The magic around")<0))
			&&((msg.targetMajor(CMMsg.MASK_HANDS))||(msg.targetMajor(CMMsg.MASK_MOVE))))
			{
				final CMMsg msg2=CMClass.getMsg(mob,source,this,CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL,null);
				if((source!=null)&&(source.location().okMessage(source,msg2)))
				{
					source.location().send(source,msg2);
					if(msg2.value()<=0)
					{
						int damage=CMLib.dice().roll(1,3*powerLevel,1*powerLevel);
						if(damage > mob.phyStats().level()/2)
							damage = mob.phyStats().level()/2;
						CMLib.combat().postDamage(mob,source,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|typeOfEffect,weaponType,
								L("^F^<FIGHT^>The magic of @x1 around <S-NAME> <DAMAGE> <T-NAME>!^</FIGHT^>^?",clanName));
						if((!source.isInCombat())&&(source.isMonster())&&(source!=mob)&&(source.location()==mob.location())&&(source.location().isInhabitant(mob))&&(CMLib.flags().canBeSeenBy(mob,source)))
							CMLib.combat().postAttack(source,mob,source.fetchWieldedItem());
					}
				}
			}
		}
		/*
		 * ************************* Staff/Wand Message Watch
		 * *************************
		 */
		if(affected instanceof Wand)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WAND_USE:
				if(msg.amITarget(this)&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
					waveIfAble(mob,msg.tool(),msg.targetMessage(),(Wand)affected);
				break;
			case CMMsg.TYP_SPEAK:
				if(msg.sourceMinor()==CMMsg.TYP_SPEAK)
				{
					boolean alreadyWanding=false;
					final List<CMMsg> trailers =msg.trailerMsgs();
					if(trailers!=null)
					{
						for(final CMMsg msg2 : trailers)
						{
							if(msg2.targetMinor()==CMMsg.TYP_WAND_USE)
								alreadyWanding=true;
						}
					}
					final String said=CMStrings.getSayFromMessage(msg.sourceMessage());
					if((!alreadyWanding)
					&&(said != null)
					&&(checkWave(mob,said,(Wand)affected)))
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,said,CMMsg.NO_EFFECT,null));
				}
				break;
			default:
				break;
			}
		}
	}
}

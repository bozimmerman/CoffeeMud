package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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

public class StdWand extends StdItem implements Wand
{
	@Override
	public String ID()
	{
		return "StdWand";
	}

	protected String secretWord=CMProps.getAnyListFileValue(CMProps.ListFile.MAGIC_WORDS);

	public StdWand()
	{
		super();

		setName("a crooked stick");
		basePhyStats.setWeight(1);
		setDisplayText("a small crooked stick is here.");
		setDescription("Looks like an broken piece of a tree.");
		secretIdentity="";
		baseGoldValue=200;
		material=RawMaterial.RESOURCE_OAK;
		setUsesRemaining(0);
		recoverPhyStats();
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a wand");
	}

	@Override
	public int getCharges()
	{
		return usesRemaining();
	}

	@Override
	public void setCharges(final int newCharges)
	{
		this.setUsesRemaining(newCharges);
	}

	@Override
	public int getMaxCharges()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void setMaxCharges(final int num)
	{
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(((phyStats().disposition()&PhyStats.IS_BONUS)==0)
		&&(usesRemaining()>0)
		&&(getSpell()!=null))
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_BONUS);
	}

	public static boolean useTheWand(final Ability A, final MOB mob, final int level)
	{
		int manaRequired=5;
		final int q=CMLib.ableMapper().qualifyingLevel(mob,A);
		if(q>0)
		{
			if(q<CMLib.ableMapper().qualifyingClassLevel(mob,A))
				manaRequired=0;
			else
				manaRequired=5;
		}
		else
			manaRequired=25;
		manaRequired-=(2*level);
		if(manaRequired<5)
			manaRequired=5;
		if(manaRequired>mob.curState().getMana())
		{
			mob.tell(CMLib.lang().L("You don't have enough mana."));
			return false;
		}
		mob.curState().adjMana(-manaRequired,mob.maxState());
		return true;
	}

	@Override
	public int value()
	{
		if((usesRemaining()<=0)
		&&((getSpell()!=null)||(numEffects()==0)))
			return 0;
		return super.value();
	}

	public static String getWandWord(final String from)
	{
		int hash=from.hashCode();
		if(hash<0)
			hash=hash*-1;
		return CMProps.getListFileChoiceFromIndexedListByHash(CMProps.ListFile.MAGIC_WORDS,hash);
	}

	@Override
	public void setSpell(final Ability theSpell)
	{
		miscText="";
		if(theSpell!=null)
			miscText=theSpell.ID();
		secretWord=StdWand.getWandWord(miscText);
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		secretWord=StdWand.getWandWord(newText);
	}

	@Override
	public Ability getSpell()
	{
		if(text().length()==0)
			return null;
		return CMClass.getAbility(text());
	}

	@Override
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		final Ability A=getSpell();
		final String uses;
		if(this.getCharges() < 999999)
		{
			if(this.getMaxCharges() < 999999)
				uses=""+getCharges()+"/"+getMaxCharges();
			else
				uses = ""+getCharges();
		}
		else
			uses="unlimited";
		final String plus = ((phyStats().ability()>0)&&(phyStats.ability()<10))?("+"+phyStats.ability()):"";
		if(A!=null)
		{
			switch(A.classificationCode()&Ability.ALL_ACODES)
			{
			case Ability.ACODE_PRAYER:
				id="'A relic of "+A.name()+plus+" Charges: "+uses+"\n\r"+id;
				break;
			case Ability.ACODE_CHANT:
				id="'A shard of "+A.name()+plus+" Charges: "+uses+"\n\r"+id;
				break;
			case Ability.ACODE_SPELL:
			default:
				id="'A wand of "+A.name()+plus+" Charges: "+uses+"\n\r"+id;
				break;
			}
		}
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	@Override
	public boolean checkWave(final MOB mob, final String message)
	{
		return StdWand.checkWave(mob, message, this);
	}

	@Override
	public void waveIfAble(final MOB mob, final Physical afftarget, final String message)
	{
		StdWand.waveIfAble(mob, afftarget, message, this);
	}

	public static boolean checkWave(final MOB mob, final String message, final Wand me)
	{
		return (mob.isMine(me))
				&& (message!=null)
				&& (me.amBeingWornProperly())
				&& (message.toUpperCase().indexOf(me.magicWord().toUpperCase()) >= 0);
	}

	public static void waveIfAble(final MOB mob, final Physical afftarget, String message, final Wand me)
	{
		if((mob.isMine(me)) &&(message!=null) &&(!me.amWearingAt(Wearable.IN_INVENTORY)))
		{
			Physical target=null;
			if(mob.location()!=null)
				target=afftarget;
			final int x=message.toUpperCase().indexOf(me.magicWord().toUpperCase());
			if(x>=0)
			{
				message=message.substring(x+me.magicWord().length());
				final int y=message.indexOf('\'');
				if(y>=0)
					message=message.substring(0,y);
				message=message.trim();
				Ability spellA=me.getSpell();
				Ability wandUse = null;
				final boolean pickFirstOne = me.getEnchantType() < 0;
				for(final Enumeration<Ability> e=mob.abilities();e.hasMoreElements();)
				{
					final Ability A=e.nextElement();
					if((A instanceof Wand.WandUsage)
					&&((((WandUsage)A).getEnchantType() < 0)
						||(pickFirstOne)
						||(((WandUsage)A).getEnchantType()==me.getEnchantType())))
					{
						wandUse = A;
						if((pickFirstOne)
						&&(spellA != null)
						&&(((WandUsage)A).getEnchantType()>0)
						&&(((WandUsage)A).getEnchantType()<WandUsage.WAND_OPTIONS.length))
						{
							if(WandUsage.WAND_OPTIONS[((WandUsage)A).getEnchantType()][0].equals(Ability.ACODE.DESCS_.get(spellA.classificationCode()&Ability.ALL_ACODES)))
								break;
						}
					}
				}
				if((wandUse==null)||(!wandUse.proficiencyCheck(null,0,false)))
					mob.tell(CMLib.lang().L("@x1 glows faintly for a moment, then fades.",me.name()));
				else
				{
					if(spellA==null)
						mob.tell(CMLib.lang().L("Something seems wrong with @x1.",me.name()));
					else
					if(me.usesRemaining()<=0)
						mob.tell(CMLib.lang().L("@x1 seems spent.",me.name()));
					else
					{
						wandUse.setInvoker(mob);
						spellA=(Ability)spellA.newInstance();
						if(useTheWand(spellA,mob,wandUse.abilityCode()))
						{
							final Vector<String> V=new Vector<String>();
							if(target!=null)
								V.addElement(target.name());
							V.addAll(CMParms.parse(message));
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("@x1 glows brightly.",me.name()));
							me.setUsesRemaining(me.usesRemaining()-1);
							int level=me.phyStats().level()
									+ Math.min(me.phyStats().ability(),10)
									+ CMLib.expertises().getExpertiseLevelCached(mob, wandUse.ID(), ExpertiseLibrary.XType.LEVEL);
							final int lowest=CMLib.ableMapper().lowestQualifyingLevel(spellA.ID());
							if(level<lowest)
								level=lowest;
							spellA.invoke(mob, V, target, true, level);
							wandUse.helpProficiency(mob, 0);
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();

		switch(msg.targetMinor())
		{
		case CMMsg.TYP_WAND_USE:
			if(msg.amITarget(this)
			&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
				waveIfAble(mob,(Physical)msg.tool(),msg.targetMessage());
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(!amWearingAt(Wearable.IN_INVENTORY)))
			{
				boolean alreadyWanding=false;
				final List<CMMsg> trailers=msg.trailerMsgs();
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
				&&(said!=null)
				&&(checkWave(mob,said)))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,said,CMMsg.NO_EFFECT,null));
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public String magicWord()
	{
		return secretWord;
	}

	@Override
	public int getEnchantType()
	{
		return -1;
	}

	@Override
	public void setEnchantType(final int enchType)
	{
	}

	protected static String[] CODES={"CLASS","LEVEL","ABILITY","TEXT"};

	@Override
	public String getStat(final String code)
	{
		switch(getInternalCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + basePhyStats().level();
		case 2:
			return "" + basePhyStats().ability();
		case 3:
			return text();
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getInternalCodeNum(code))
		{
		case 0:
			return;
		case 1:
			basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break;
		case 2:
			basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setMiscText(val);
			break;
		}
	}

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdWand))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}

package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Prayer_StorePrayer extends Prayer implements AbilityContainer, Dischargeable
{

	@Override
	public String ID()
	{
		return "Prayer_StorePrayer";
	}

	@Override
	public String Name()
	{
		return "Store Prayer";
	}

	@Override
	public String name()
	{
		if((affected!=null)&&(CMLib.flags().isInTheGame(affected,true)))
		{
			return "Store Prayer: "+prayerName;
		}
		return Name();
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	protected int overrideMana()
	{
		return overridemana;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected String prayerName		= "";
	protected int	 overridemana	= -1;

	protected String getPrayerName()
	{
		if(prayerName.length()==0)
		{
			prayerName="unknown";
			final Ability A=fetchAbility(0);
			if(A!=null)
				prayerName=A.name();
		}
		return prayerName;
	}

	protected String getPrayerID()
	{
		final int x=text().indexOf('/');
		if(x>0)
			return text().substring(0,x);
		return "";
	}

	@Override
	public int getCharges()
	{
		String argText=text();
		int x=argText.indexOf('/');
		if(x>0)
		{
			argText=argText.substring(x+1);
			x=argText.indexOf('/');
			if(x>0)
				return CMath.s_int(argText.substring(0,x));
			else
				return CMath.s_int(argText);
		}
		return 0;
	}

	@Override
	public void setCharges(final int newCharges)
	{
		final Physical affected=this.affected;
		final int oldCharges=getCharges();
		final String abilityID=getPrayerID();
		setMiscText(abilityID+"/"+newCharges);
		if((oldCharges > 0) && (newCharges<=0) && (affected != null))
			affected.delEffect(this);
	}

	@Override
	public int getMaxCharges() { return Integer.MAX_VALUE; }

	@Override
	public void setMaxCharges(final int num) {  }

	protected String getDeity()
	{
		final String deityName=CMLib.law().getClericInfused(affected);
		if(deityName==null)
			return "";
		return deityName;
	}

	public String getSpeakableName(String name)
	{
		final String deityName=getDeity();
		if(deityName.length()>0)
			name=deityName;
		name=CMStrings.removeColors(name.toUpperCase());
		if(name.startsWith("A "))
			name=name.substring(2).trim();
		if(name.startsWith("AN "))
			name=name.substring(3).trim();
		if(name.startsWith("THE "))
			name=name.substring(4).trim();
		if(name.startsWith("SOME "))
			name=name.substring(5).trim();
		return name;
	}

	public void waveIfAble(final MOB mob, final Physical afftarget, String message, final Item me)
	{
		if((mob.isMine(me))
		&&(me.amBeingWornProperly())
		&&(message!=null))
		{
			Physical target=null;
			if((mob.location()!=null))
				target=afftarget;
			final String name=getSpeakableName(me.name());
			final int x=message.toUpperCase().indexOf(name);
			if(x>=0)
			{
				message=message.substring(x+name.length());
				final int y=message.indexOf('\'');
				if(y>=0)
					message=message.substring(0,y);
				message=message.trim();
				final int charges=getCharges();
				Ability A=fetchAbility(0);
				final String deity=getDeity();
				if(A==null)
					mob.tell(L("Something seems wrong with @x1.",me.name()));
				else
				if(charges<=0)
				{
					mob.tell(L("@x1 seems spent.",me.name()));
					me.delEffect(this);
				}
				else
				if((deity.length()>0)&&(!mob.charStats().getWorshipCharID().equalsIgnoreCase(deity)))
				{
					mob.tell(L("@x1 seems to reject your lack in fatih in @x2.",me.name(),deity));
				}
				else
				{
					setCharges(charges-1);
					A=(Ability)A.newInstance();
					final Vector<String> V=new Vector<String>();
					if(target!=null)
						V.addElement(target.name());
					V.addElement(message);
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 glows brightly.",me.name()));
					A.invoke(mob, V, target, true, me.phyStats().level());
					if(getCharges()<=0)
						mob.tell(L("@x1 seems spent.",me.name()));
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
			if((msg.amITarget(affected))
			&&(affected instanceof Item)
			&&((msg.tool()==null)||(msg.tool() instanceof Physical)))
				waveIfAble(mob,(Physical)msg.tool(),msg.targetMessage(),(Item)affected);
			break;
		case CMMsg.TYP_SPEAK:
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.sourceMessage()!=null)
			&&(affected != null))
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
				if(!alreadyWanding)
				{
					final String name=getSpeakableName(affected.name());
					final int x=msg.sourceMessage().toUpperCase().indexOf(name);
					if(x>=0)
					{
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),affected,msg.target(),CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,CMStrings.getSayFromMessage(msg.sourceMessage()),CMMsg.NO_EFFECT,null));
					}
				}
			}
			break;
		default:
			break;
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void addAbility(final Ability to)
	{
		if(to != null)
			setMiscText(to.ID()+"/"+getCharges());
	}

	@Override
	public void delAbility(final Ability to)
	{
		final Ability hasA=fetchAbility(0);
		if((hasA!=null)&&(to!=null)&&(hasA.ID().equals(to.ID())))
		{
			final Physical affected=this.affected;
			if(affected != null)
			{
				unInvoke();
				affected.delEffect(this);
			}
		}
	}

	@Override
	public int numAbilities()
	{
		if(fetchAbility(0)!=null)
			return 1;
		return 0;
	}

	@Override
	public Ability fetchAbility(final int index)
	{
		if(index != 0)
			return null;
		final String name=getPrayerID();
		if((name==null)||(name.length()==0))
			return null;
		final Ability A=CMClass.getAbility(name);
		if(A==null)
			return null;
		return A;
	}

	@Override
	public Ability fetchAbility(final String ID)
	{
		final Ability A=fetchAbility(0);
		if(A==null)
			return null;
		if(A.ID().equalsIgnoreCase(ID))
			return A;
		return null;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		final Ability A=fetchAbility(0);
		if(A==null)
			return null;
		return A;
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		final Ability A=fetchAbility(0);
		if(A==null)
			return new XVector<Ability>().elements();
		return new XVector<Ability>(A).elements();
	}

	@Override
	public void delAllAbilities()
	{
		delAbility(fetchAbility(0));
	}

	@Override
	public int numAllAbilities()
	{
		if(fetchAbility(0)!=null)
			return 1;
		return 0;
	}

	@Override
	public Enumeration<Ability> allAbilities()
	{
		return abilities();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Store which prayer onto what?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(commands.size()-1))));
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell(L("You can't bless '@x1'.",target.name(mob)));
			return false;
		}

		final Item item=(Item)target;
		if((item.rawProperLocationBitmap() != Item.WORN_HELD)
		||(item instanceof Food)
		||(item instanceof Drink)
		||(item instanceof MusicalInstrument)
		||(item.isReadable()))
		{
			mob.tell(L("That item looks unsuitable for holding divine magic."));
			return false;
		}

		if(!Prayer.checkInfusionMismatch(mob, target))
		{
			mob.tell(L("You can store no prayer in that repulsive item."));
			return false;
		}

		commands.remove(commands.size()-1);

		final String spellName=CMParms.combine(commands,0).trim();
		Prayer wandThis=null;
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))
			&&(!A.ID().equals(this.ID())))
				wandThis=(Prayer)A;
		}
		if(wandThis==null)
		{
			mob.tell(L("You don't know how to bless anything with '@x1'.",spellName));
			return false;
		}
		if((CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID())>24)
		||(((StdAbility)wandThis).usageCost(null,true)[0]>45)
		||(CMath.bset(wandThis.flags(), Ability.FLAG_CLANMAGIC)))
		{
			mob.tell(L("That prayer is too powerful to store."));
			return false;
		}
		Ability A=item.fetchEffect(ID());
		if((A!=null)&&(A.text().length()>0)&&(!A.text().startsWith(wandThis.ID()+"/")))
		{
			mob.tell(L("'@x1' already has a different prayer stored in it.",item.name()));
			return false;
		}
		else
		if(A==null)
		{
			A=(Ability)copyOf();
			A.setMiscText(wandThis.ID()+"/0");
		}
		int charges=0;
		final int x=A.text().indexOf('/');
		if(x>=0)
			charges=CMath.s_int(A.text().substring(x+1));
		overridemana=-1;
		int mana=usageCost(mob,true)[0]+wandThis.usageCost(mob,true)[0];
		if(mana>mob.maxState().getMana())
			mana=mob.maxState().getMana();
		overridemana=mana;

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		overridemana=-1;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			setMiscText(wandThis.ID()); // for informational purposes
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					L("^S<S-NAME> @x1 while moving <S-HIS-HER> hands around <T-NAMESELF>.^?", prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.fetchEffect(ID())==null)
				{
					A.setInvoker(mob);
					target.addNonUninvokableEffect(A);
				}
				A.setMiscText(wandThis.ID()+"/"+(charges+1));
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) divinely."));
				Prayer.infusePhysicalByAlignment(mob, target);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("^S<S-NAME> @x1 while moving <S-HIS-HER> hands around <T-NAMESELF>, but nothing happens.^?", prayWord(mob)));

		// return whether it worked
		return success;
	}
}

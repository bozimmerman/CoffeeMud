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
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_Superstition extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Superstition";
	}

	private final static String	localizedName	= CMLib.lang().L("Superstition");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_COMBATLORE;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SUPERSTITION" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected String	sayMsg			= "";
	protected String	itemname		= "";
	protected String	language		= "";
	protected boolean	activated		= false;
	protected TimeClock	activatedUntil	= null;	

	protected void setupCheck()
	{
		if((text()!=null)
		&&(text().length() > 0)
		&&(text().indexOf('<') >= 0)
		&&(affected instanceof MOB))
		{
			final List<XMLLibrary.XMLTag> set = CMLib.xml().parseAllXML(text());
			final String say = CMLib.xml().getValFromPieces(set, "SAY");
			final String wear = CMLib.xml().getValFromPieces(set, "WEAR");
			final String lang = CMLib.xml().getValFromPieces(set, "LANG");
			if((say!=null) && (wear!=null) && (say.length()>0) && (wear.length()>0))
			{
				this.sayMsg = say;
				this.itemname = wear;
				this.language=lang;
			}
		}
	}
	
	@Override
	public String displayText()
	{
		if(affected != null)
		{
			if(activated)
			{
				return L("(Superstitiously lucky)");
			}
			else
			if((sayMsg!=null)&&(sayMsg.length()>0))
			{
				return L("(Superstitiously unlucky)");
			}
		}
		return "";
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((!activated)
		&&(affected!=null)
		&&(msg.source() == affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(sayMsg!=null)
		&&(sayMsg.length()>0)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			if((((language==null)||(language.length()==0))
				&&(msg.tool()==null))
			||((language!=null)
				&&(language.length()>0)
				&&(msg.tool() instanceof Language)
				&&(language.equalsIgnoreCase(msg.tool().ID()))))
			{
				String say=CMStrings.getSayFromMessage(msg.sourceMessage());
				if((say!=null)&&(say.equalsIgnoreCase(sayMsg)))
				{
					final Room R=msg.source().location();
					if((R!=null) && (R.getArea()!=null))
					{
						final List<Item> items=msg.source().findItems(itemname);
						boolean found=false;
						for(Item I : items)
						{
							if(CMLib.english().cleanArticles(I.Name()).equalsIgnoreCase(itemname)
							&&(!I.amWearingAt(Item.IN_INVENTORY)))
							{
								found=true;
								break;
							}
						}
						if(found)
						{
							msg.source().tell(L("You feel lucky again!"));
							synchronized(this)
							{
								activated=true;
								helpProficiency(msg.source(), 0);
								activatedUntil = (TimeClock)R.getArea().getTimeObj().copyOf();
								activatedUntil.tickTock(activatedUntil.getHoursInDay());
							}
							msg.source().recoverCharStats();
							msg.source().recoverMaxState();
							msg.source().recoverPhyStats();
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(ticking instanceof MOB)
		{
			final MOB M=(MOB)ticking;
			if(activated)
			{
				synchronized(this)
				{
					final Room R=M.location();
					if((activatedUntil!=null)
					&&(R.getArea()!=null)
					&&(R.getArea().getTimeObj().compareTo(activatedUntil)>0))
					{
						activated=false;
						activatedUntil=null;
					}
				}
				if(!activated)
				{
					M.recoverCharStats();
					M.recoverMaxState();
					M.recoverPhyStats();
				}
			}
			else
			if(!M.isPlayer())
			{
				activated=true; // mobs always get it...
				M.recoverCharStats();
				M.recoverMaxState();
				M.recoverPhyStats();
			}
		}
		return true;
	}
	
	@Override
	public void setAffectedOne(Physical P)
	{
		super.setAffectedOne(P);
		setupCheck();
	}
	
	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		setupCheck();
	}
	
	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		if((sayMsg!=null)&&(sayMsg.length()>0)&&(this.affected == affectedMob))
		{
			if(activated)
				affectableStats.adjustAbilityAdjustment("prof+*",affectableStats.getAbilityAdjustment("prof+*")+10);
			else
				affectableStats.adjustAbilityAdjustment("prof+*",affectableStats.getAbilityAdjustment("prof+*")-10);
		}
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((sayMsg!=null)&&(sayMsg.length()>0)
		&&(affected instanceof MOB))
		{
			final double pct = CMath.div(proficiency(), 100.0);
			final int attackBonus = (int)Math.round(CMath.mul(pct,5+(adjustedLevel((MOB)affected,0)/2)));
			final int damageBonus = (int)Math.round(CMath.mul(pct,1+(adjustedLevel((MOB)affected,0)/10)));
			if(activated)
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + attackBonus);
				affectableStats.setDamage(affectableStats.damage() + damageBonus);
			}
			else
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() - attackBonus);
				affectableStats.setDamage(affectableStats.damage() - damageBonus);
			}
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((text().length()>0)&&(text().indexOf('<')>=0))
		{
			final List<XMLLibrary.XMLTag> set = CMLib.xml().parseAllXML(text());
			final String say = CMLib.xml().getValFromPieces(set, "SAY");
			final String wear = CMLib.xml().getValFromPieces(set, "WEAR");
			final String language = CMLib.xml().getValFromPieces(set, "LANG");
			if((say==null)||(wear==null)||(language==null))
			{
				mob.tell(L("BROKE! Try again."));
				this.setMiscText("");
			}
			else
			{
				if(language.length()>0)
					mob.tell(L("Your superstition is to put on your lucky @x1 and say '@x2' in @x3.",wear,say,language));
				else
					mob.tell(L("Your superstition is to put on your lucky @x1 and say '@x2'.",wear,say));
			}
			return false;
		}
		else
		if(!auto)
		{
			String newSay = CMParms.combine(commands);
			if((newSay == null) || (newSay.trim().length()==0))
			{
				mob.tell(L("No superstition has been set yet.  To set one, give a thing to say that`s at least 5 words and at least 30 characters long."));
				return false;
			}
			if((newSay.trim().length()<30)||(commands.size()<5))
			{
				mob.tell(L("No superstition has been set yet, and your thing to say is too short.  Give one that`s at least 30 characters long consisting of at least 5 words."));
				return false;
			}
			List<Item> choices = new ArrayList<Item>();
			for (final Enumeration<Item> i = mob.items(); i.hasMoreElements();)
			{
				final Item I = i.nextElement();
				if ((!I.amWearingAt(Item.IN_INVENTORY))
				&&(!I.amWearingAt(Item.WORN_HELD))
				&&(!I.amWearingAt(Item.WORN_WIELD)))
					choices.add(I);
			}
			if(choices.size()<3)
			{
				mob.tell(L("No superstition has been set yet, and your thing to say is fine.  However, you need to be wearing at least 3 things to set your superstition."));
				return false;
			}
			Item I=choices.get(CMLib.dice().roll(1, choices.size(), -1));
			String wearName=CMLib.english().cleanArticles(I.Name());
			Language langA = CMLib.utensils().getLanguageSpoken(mob);
			String langID = ((langA==null)||(langA.ID().equals("Common"))) ? "" : langA.ID();
			StringBuilder newXml=new StringBuilder("<SAY>").append(CMLib.xml().parseOutAngleBrackets(newSay)).append("</SAY>")
											.append("<WEAR>").append(CMLib.xml().parseOutAngleBrackets(wearName)).append("</WEAR>")
											.append("<LANG>").append(langID).append("</LANG>");
			mob.tell(L("Your new superstition is to put on your lucky @x1 and say '@x2'.",wearName,newSay));
			this.setMiscText(newXml.toString());
			Ability A=mob.fetchEffect(ID());
			if(A!=null)
			{
				A.setMiscText(newXml.toString());
			}
			mob.recoverCharStats();
			mob.recoverMaxState();
			mob.recoverPhyStats();
		}
		return true;
	}
}

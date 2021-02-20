package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.Align;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2021 Bo Zimmerman

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
public class Prayer extends StdAbility
{
	@Override
	public String ID()
	{
		return "Prayer";
	}

	private final static String	localizedName	= CMLib.lang().L("a Prayer");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PRAY", "PR" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER;
	}

	/* These should be pre-localized, because of the damn deity names */
	protected String prayWord(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return L("pray(s) to @x1",mob.charStats().deityName());
		return L("pray(s)");
	}

	/* These should be pre-localized, because of the damn deity names */
	protected String prayForWord(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return L("pray(s) for @x1",mob.charStats().deityName());
		return L("pray(s)");
	}

	/* These should be pre-localized, because of the damn deity names */
	protected String inTheNameOf(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return L(" in the name of @x1",mob.charStats().deityName());
		return "";
	}

	/* These should be pre-localized, because of the damn deity names */
	protected String againstTheGods(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return L(" against @x1",mob.charStats().deityName());
		return L(" against the gods");
	}

	protected String hisHerDiety(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return mob.charStats().deityName();
		return L("<S-HIS-HER> god");
	}

	protected String ofDiety(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return L(" of @x1",mob.charStats().deityName());
		return "";
	}

	protected String prayingWord(final MOB mob)
	{
		if(mob.charStats().deityName().length()>0)
			return L("praying to @x1",mob.charStats().deityName());
		return L("praying");
	}

	protected static List<Ability> getRelicPrayers(final Physical P)
	{
		final List<Ability> prayV=new Vector<Ability>();
		if(P instanceof Wand)
		{
			final Ability A=((Wand)P).getSpell();
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
				prayV.add(A);
		}
		else
		if((P instanceof Item)
		&&(!(P instanceof Weapon))
		&&(!(P instanceof Armor))
		&&(!(P instanceof Scroll)))
		{
			for(final Enumeration<Ability> a= ((Item)P).effects(); a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof AbilityContainer)
				&&(A instanceof Dischargeable))
				{
					final AbilityContainer aCont = (AbilityContainer)A;
					for(final Enumeration<Ability> suba = aCont.abilities();suba.hasMoreElements();)
					{
						final Ability subA=suba.nextElement();
						if(subA instanceof Prayer)
							prayV.add(subA);
					}
				}
			}
		}
		return prayV;
	}

	protected static boolean checkInfusionMismatch(final MOB mob, final Physical target)
	{
		final String deityName=CMLib.law().getClericInfused(target);
		if((deityName!=null)
		&&(deityName.length()>0)
		&&(mob.baseCharStats().getWorshipCharID().length()>0)
		&&(!deityName.equalsIgnoreCase(mob.baseCharStats().getWorshipCharID())))
			return false;
		return true;
	}

	protected static boolean checkRequiredInfusion(final MOB mob, final Physical target)
	{
		if(mob.baseCharStats().getWorshipCharID().length()==0)
			return false;
		final String deityName=CMLib.law().getClericInfused(target);
		if((deityName==null)||(deityName.length()==0))
			return false;
		if(!deityName.equalsIgnoreCase(mob.baseCharStats().getWorshipCharID()))
			return false;
		return true;
	}

	protected static void infusePhysicalByAlignment(final MOB mob, final Physical target)
	{
		final String deityName=CMLib.law().getClericInfused(target);
		if(CMLib.factions().isAlignmentLoaded(Align.GOOD))
		{
			final String infuserID;
			if(CMLib.flags().isGood(mob))
				infuserID="Prayer_InfuseHoliness";
			else
			if(CMLib.flags().isEvil(mob))
				infuserID="Prayer_InfuseUnholiness";
			else
			if(CMLib.flags().isNeutral(mob))
				infuserID="Prayer_InfuseBalance";
			else
				infuserID=null;
			if((deityName==null)
			&&(infuserID!=null)
			&&(mob.baseCharStats().getWorshipCharID().length()>0))
			{
				final Ability dA=CMClass.getAbility(infuserID);
				dA.setMiscText(mob.baseCharStats().getWorshipCharID());
				target.addNonUninvokableEffect(dA);
			}
		}
		if(CMLib.factions().isAlignmentLoaded(Align.LAWFUL))
		{
			final String infuserID;
			if(CMLib.flags().isGood(mob))
				infuserID="Prayer_InfuseDiscipline";
			else
			if(CMLib.flags().isEvil(mob))
				infuserID="Prayer_InfuseImpunity";
			else
			if(CMLib.flags().isNeutral(mob))
				infuserID="Prayer_InfuseModeration";
			else
				infuserID=null;
			if((deityName==null)
			&&(infuserID!=null)
			&&(mob.baseCharStats().getWorshipCharID().length()>0))
			{
				final Ability dA=CMClass.getAbility(infuserID);
				dA.setMiscText(mob.baseCharStats().getWorshipCharID());
				target.addNonUninvokableEffect(dA);
			}
		}
	}

	protected static void infuseMobAlignment(final MOB mob, final Physical target)
	{
		final Ability zappA=target.fetchEffect("Prop_WearZapper");
		if(zappA==null)
		{
			final Ability A=CMClass.getAbility("Prop_WearZapper");
			if(CMLib.flags().isGood(mob))
			{
				A.setMiscText("+FACTION -EVIL -NEUTRAL");
				target.addNonUninvokableEffect(A);
			}
			else
			if(CMLib.flags().isEvil(mob))
			{
				A.setMiscText("+FACTION -GOOD -NEUTRAL");
				target.addNonUninvokableEffect(A);
			}
			else
			if(CMLib.flags().isNeutral(mob))
			{
				A.setMiscText("+FACTION -GOOD -EVIL");
				target.addNonUninvokableEffect(A);
			}
		}
		else
		{
			if(CMLib.flags().isGood(mob) && ((zappA.text().indexOf("-NEUTRAL")<0)||(zappA.text().indexOf("-EVIL")<0)))
				zappA.setMiscText(zappA.text()+" +FACTION -EVIL -NEUTRAL");
			else
			if(CMLib.flags().isEvil(mob) && ((zappA.text().indexOf("-GOOD")<0)||(zappA.text().indexOf("-NEUTRAL")<0)))
				zappA.setMiscText(zappA.text()+" +FACTION -GOOD -NEUTRAL");
			else
			if(CMLib.flags().isNeutral(mob) && ((zappA.text().indexOf("-GOOD")<0)||(zappA.text().indexOf("-EVIL")<0)))
				zappA.setMiscText(zappA.text()+" +FACTION -GOOD -EVIL");
		}
	}

	protected static void clearInfusions(final Physical P)
	{
		if(P==null)
			return;
		Deity.DeityWorshipper A=CMLib.law().getClericInfusion(P);
		while((A!=null)&&(A instanceof Ability)) // yes, I know, but this feels right
		{
			((Ability)A).unInvoke();
			P.delEffect(((Ability)A));
			A=CMLib.law().getClericInfusion(P);
		}
	}

	protected static Dischargeable getDischargeableRelic(final Physical P)
	{
		if(P instanceof Wand)
			return (Wand)P;
		else
		if((P instanceof Item)
		&&(!(P instanceof Weapon))
		&&(!(P instanceof Armor))
		&&(!(P instanceof Scroll)))
		{
			for(final Enumeration<Ability> a= ((Item)P).effects(); a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A instanceof AbilityContainer)
				&&(A instanceof Dischargeable))
				{
					return (Dischargeable)A;
				}
			}
		}
		return null;
	}

	protected static void setRelicCharges(final Physical P, final int num)
	{
		if(Prayer.isARelic(P))
		{
			final Dischargeable D=getDischargeableRelic(P);
			if(D != null)
				D.setCharges(num);
		}
	}

	protected static void clearRelicMagic(final Physical P)
	{
		if(Prayer.isARelic(P))
		{
			final Dischargeable D=getDischargeableRelic(P);
			if(D != null)
			{
				if(D instanceof Ability)
					P.delEffect((Ability)D);
				else
				if(D instanceof Wand)
					((Wand)D).setSpell(null);
			}
		}
	}

	protected static int getRelicCharges(final Physical P)
	{
		if(Prayer.isARelic(P))
		{
			final Dischargeable D=getDischargeableRelic(P);
			if(D != null)
				return D.getCharges();
		}
		return -1;
	}

	protected static boolean isARelic(final Physical P)
	{
		return getRelicPrayers(P).size()>0;
	}

	protected static boolean prayerAlignmentCheck(final StdAbility A, final MOB mob, final boolean auto)
	{
		if((!auto)
		&&(!mob.isMonster())
		&&(!A.disregardsArmorCheck(mob))
		&&(mob.isMine(A))
		&&(!A.appropriateToMyFactions(mob))
		&&(A.flags()!=0))
		{
			int hq=500;
			if(CMath.bset(A.flags(),Ability.FLAG_HOLY))
			{
				if(!CMath.bset(A.flags(),Ability.FLAG_UNHOLY))
					hq=1000;
			}
			else
			if(CMath.bset(A.flags(),Ability.FLAG_UNHOLY))
				hq=0;

			int basis=0;
			if(hq==0)
				basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().getAlignmentID()),Faction.Align.EVIL);
			else
			if(hq==1000)
				basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().getAlignmentID()),Faction.Align.GOOD);
			else
			{
				basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().getAlignmentID()),Faction.Align.NEUTRAL);
				basis-=10;
			}

			if(CMLib.dice().rollPercentage()>basis)
				return true;

			if(hq==0)
				mob.tell(A.L("The evil nature of @x1 disrupts your prayer.",A.name()));
			else
			if(hq==1000)
				mob.tell(A.L("The goodness of @x1 disrupts your prayer.",A.name()));
			else
			if(CMLib.flags().isGood(mob))
				mob.tell(A.L("The anti-good nature of @x1 disrupts your thought.",A.name()));
			else
			if(CMLib.flags().isEvil(mob))
				mob.tell(A.L("The anti-evil nature of @x1 disrupts your thought.",A.name()));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical target, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,target,auto,asLevel))
			return false;
		if(!prayerAlignmentCheck(this,mob,auto))
			return false;
		return true;
	}

}

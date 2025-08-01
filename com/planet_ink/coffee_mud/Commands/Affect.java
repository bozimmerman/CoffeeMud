package com.planet_ink.coffee_mud.Commands;
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
import java.util.concurrent.TimeUnit;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class Affect extends StdCommand
{
	private final String[] access=I(new String[]{"AFFECTS","AFFECT","AFF","AF"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

//	private final static Class<?>[][] internalParameters=new Class<?>[][]{{Physical.class}};

	private static enum SortField
	{
		NAME,
		TYPE,
		DOMAIN,
		REMAIN
	}

	public String getMOBState(final MOB mob)
	{
		final StringBuffer msg=new StringBuffer("");
		if((mob.playerStats()!=null)
		&&(mob.soulMate()==null)
		&&(mob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
		{
			if(CMSecurity.isASysOp(mob))
				mob.playerStats().setHygiene(0);
			else
			{
				final int x=(int)(mob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
				if(x<=1)
					msg.append(L("^!You could use a bath.^?\n\r"));
				else
				if(x<=3)
					msg.append(L("^!You could really use a bath.^?\n\r"));
				else
				if(x<=7)
					msg.append(L("^!You need to bathe, soon.^?\n\r"));
				else
				if(x<15)
					msg.append(L("^!You desperately need to bathe.^?\n\r"));
				else msg.append(L("^!Your stench is horrendous! Bathe dammit!^?\n\r"));
			}
		}

		if(CMLib.flags().isBound(mob))
			msg.append(L("^!You are bound.^?\n\r"));

		// dont do falling -- the flag doubles for drowning/treading water anyway.
		//if(CMLib.flags().isFalling(mob))
		//    msg.append(L("^!You are falling!!!^?\n\r"));
		//else
		if(CMLib.flags().isSleeping(mob))
			msg.append(L("^!You are sleeping.^?\n\r"));
		else
		if(CMLib.flags().isSitting(mob))
			msg.append(L("^!You are resting.^?\n\r"));
		else
		if(CMLib.flags().isSwimmingInWater(mob))
			msg.append(L("^!You are swimming.^?\n\r"));
		else
		if(CMLib.flags().isClimbing(mob))
			msg.append(L("^!You are climbing.^?\n\r"));
		else
		if(CMLib.flags().isFlying(mob))
			msg.append(L("^!You are flying.^?\n\r"));
		else
			msg.append(L("^!You are standing.^?\n\r"));

		if(mob.riding()!=null)
			msg.append(L("^!You are @x1 @x2.^?\n\r",mob.riding().stateString(mob),mob.riding().name()));

		if(mob.isAttributeSet(MOB.Attrib.PLAYERKILL))
			msg.append(L("^!Your playerkill flag is on.^?\n\r"));

		if(CMLib.flags().isInvisible(mob))
			msg.append(L("^!You are invisible.^?\n\r"));
		if(CMLib.flags().isHidden(mob))
			msg.append(L("^!You are hidden.^?\n\r"));// ("+CMLib.flags().getHideScore(mob)+").^?\n\r");
		if(CMLib.flags().isSneaking(mob))
			msg.append(L("^!You are sneaking.^?\n\r"));
		if(mob.isAttributeSet(MOB.Attrib.QUIET))
			msg.append(L("^!You are in QUIET mode.^?\n\r"));

		if(mob.curState().getFatigue()>CharState.FATIGUED_MILLIS)
			msg.append(L("^!You are fatigued.^?\n\r"));
		if(mob.curState().getHunger()<1)
			msg.append(L("^!You are hungry.^?\n\r"));
		if(mob.curState().getThirst()<1)
			msg.append(L("^!You are thirsty.^?\n\r"));
		return msg.toString();
	}

	public String getAffects(final Session S, final Physical P, final boolean xtra, final boolean autosAlso, final SortField field, final int mask)
	{
		final StringBuffer msg=new StringBuffer("");
		final int NUM_COLS=2;
		final int COL_LEN=CMLib.lister().fixColWidth(36.0,S);
		int colnum=NUM_COLS;
		final MOB mob=(S!=null)?S.mob():null;
		Enumeration<Ability> a = P.effects();
		if(field != null)
		{
			final XVector<Ability> effects = new XVector<Ability>(a);
			effects.sort(new Comparator<Ability>() {

				@Override
				public int compare(final Ability o1, final Ability o2)
				{
					switch(field)
					{
					case TYPE:
					{
						final String domainName1=CMLib.flags().getAbilityType(o1);
						final String domainName2=CMLib.flags().getAbilityType(o2);
						if(domainName1.equals(domainName2))
							return o1.displayText().compareTo(o2.displayText());
						return domainName1.compareTo(domainName2);
					}
					case DOMAIN:
					{
						final String domainName1=CMLib.flags().getAbilityDomain(o1);
						final String domainName2=CMLib.flags().getAbilityDomain(o2);
						if(domainName1.equals(domainName2))
							return o1.displayText().compareTo(o2.displayText());
						return domainName1.compareTo(domainName2);
					}
					case NAME:
						return o1.displayText().compareTo(o2.displayText());
					case REMAIN:
					{
						final boolean comply1 = ((o1.canBeUninvoked())&&(!o1.isNowAnAutoEffect()));
						final boolean comply2 = ((o2.canBeUninvoked())&&(!o2.isNowAnAutoEffect()));
						if(!comply1 && (!comply2))
							return o1.displayText().compareTo(o2.displayText());
						if(!comply1)
							return -1;
						if(!comply2)
							return 1;
						long tr1=o1.expirationDate();
						if(o1.invoker()!=null)
							tr1=tr1-(System.currentTimeMillis()-o1.invoker().lastTickedDateTime());
						long tr2=o2.expirationDate();
						if(o2.invoker()!=null)
							tr2=tr2-(System.currentTimeMillis()-o2.invoker().lastTickedDateTime());
						return Long.valueOf(tr2).compareTo(Long.valueOf(tr1));
					}
					}
					return o1.displayText().compareTo(o2.displayText());
				}

			});
			a=effects.elements();
		}
		for(;a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A==null)
				continue;
			if(mask >= 0)
			{
				if(mask < Ability.ALL_ACODES)
				{
					if((A.classificationCode()&Ability.ALL_ACODES)!=mask)
						continue;
				}
				else
				{
					if((A.classificationCode()&Ability.ALL_DOMAINS)!=mask)
						continue;
				}
			}
			String disp=A.displayText();
			if(autosAlso && disp.length()==0)
			{
				if(A.canBeUninvoked())
					disp=A.ID()+":"+A.getStat("TICKDOWN");
				else
					disp=A.ID()+"+"+A.proficiency();
			}
			if(disp.length()>0)
			{
				if(disp.startsWith("(")
				&&disp.endsWith(")")
				&&(A.canBeUninvoked())
				&&(!A.isNowAnAutoEffect()))
				{
					long tr=A.expirationDate();
					if(A.invoker()!=null)
						tr=tr-(System.currentTimeMillis()-A.invoker().lastTickedDateTime());
					if(tr<Ability.TICKS_ALMOST_FOREVER)
						disp+=" ^.^N"+CMLib.time().date2EllapsedTime(tr, TimeUnit.SECONDS, true);
				}
				if(xtra)
					disp+=", BY="+((A.invoker()==null)?"N/A":A.invoker().Name());
				String[] disps={disp};
				if(CMStrings.lengthMinusColors(disp)>(COL_LEN*NUM_COLS))
				{
					String s=CMLib.coffeeFilter().fullOutFilter(S,mob,null,null,null,disp,true);
					s=CMStrings.replaceAll(s,"\r","");
					final List<String> V=CMParms.parseAny(s,'\n',true);
					disps=new String[V.size()];
					for(int d=0;d<V.size();d++)
						disps[d]=V.get(d);
					colnum=NUM_COLS;
				}
				for (final String disp2 : disps)
				{
					disp=disp2;
					if(((++colnum)>=NUM_COLS)||(CMStrings.lengthMinusColors(disp)>COL_LEN))
					{
						msg.append("\n\r");
						colnum=0;
					}
					msg.append("^S"+CMStrings.padRightPreserve("^<HELPNAME NAME='"+CMStrings.removeColors(A.name())+"'^>"+disp+"^.^N^</HELPNAME^>",COL_LEN));
					if(CMStrings.lengthMinusColors(disp)>COL_LEN)
						colnum=99;
				}
			}
		}
		msg.append("^N\n\r");
		return msg.toString();
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{

		SortField sort = null;
		if(commands.size()>1)
		{
			final String end = commands.get(commands.size()-1).toUpperCase().trim();
			final SortField SF = (SortField)CMath.s_valueOf(SortField.class,end);
			if(SF != null)
			{
				commands.remove(commands.size()-1);
				sort = SF;
			}
		}

		int mask = -1;
		if(commands.size()>1)
		{
			final String end = commands.get(commands.size()-1).toUpperCase().trim().replace(' ','_');
			int typ = CMLib.flags().getAbilityType_(end);
			if(typ >= 0)
			{
				commands.remove(commands.size()-1);
				mask = typ;
			}
			else
			{
				typ = CMLib.flags().getAbilityDomain(end);
				if(typ >= 0)
				{
					commands.remove(commands.size()-1);
					mask = (typ +1) << 5 ;
				}
			}
		}

		if((sort == null)&&(mask>=0)&&(commands.size()>1))
		{
			final String end = commands.get(commands.size()-1).toUpperCase().trim();
			final SortField SF = (SortField)CMath.s_valueOf(SortField.class,end);
			if(SF != null)
			{
				commands.remove(commands.size()-1);
				sort = SF;
			}
		}

		final Session S=mob.session();
		if(S!=null)
		{
			if(CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDMOBS))
			{
				final String name=CMParms.combine(commands,1);
				if(name.length()>0)
				{
					Physical P=null;
					if((name.equalsIgnoreCase("here")||(name.equalsIgnoreCase("room"))))
						P=CMLib.map().roomLocation(mob);
					else
					if((name.equalsIgnoreCase("area")||(name.equalsIgnoreCase("zone"))))
						P=CMLib.map().areaLocation(mob);
					else
						P=mob.location().fetchFromMOBRoomFavorsItems(mob,null,name,Wearable.FILTER_ANY);
					if(P==null)
						S.colorOnlyPrint(L("You don't see @x1 here.\n\r^N",name));
					else
					{
						if(S==mob.session())
							S.colorOnlyPrint(L(" \n\r^!@x1 is affected by: ^?",P.name()));
						final String msg=getAffects(S,P,true,mob.isAttributeSet(MOB.Attrib.SYSOPMSGS),sort,mask);
						if(msg.length()<5)
							S.colorOnlyPrintln(L("Nothing!\n\r^N"));
						else
							S.colorOnlyPrintln(msg);
					}
					return false;
				}

			}
			if(S==mob.session())
				S.colorOnlyPrintln("\n\r"+getMOBState(mob)+"\n\r");
			if(S==mob.session())
				S.colorOnlyPrint(L("^!You are affected by: ^?"));
			final String msg=getAffects(S,mob,mob.isAttributeSet(MOB.Attrib.SYSOPMSGS),mob.isAttributeSet(MOB.Attrib.SYSOPMSGS),sort,mask);
			if(msg.length()<5)
				S.colorOnlyPrintln(L("Nothing!\n\r^N"));
			else
				S.colorOnlyPrintln(msg);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		//if(!super.checkArguments(internalParameters, args)) return Boolean.FALSE.toString();

		Physical target=mob;
		Session S=(mob!=null)?mob.session():null;
		for(final Object o : args)
		{
			if(o instanceof Physical)
			{
				if(o instanceof MOB)
					S=((MOB)o).session();
				target=(Physical)o;
			}
		}
		return getAffects(S,target,false,false,null,-1);
	}
}

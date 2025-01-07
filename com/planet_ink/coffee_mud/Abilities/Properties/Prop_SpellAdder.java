package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
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
public class Prop_SpellAdder extends Property implements AbilityContainer, TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_SpellAdder";
	}

	@Override
	public String name()
	{
		return "Casting spells on oneself";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS | Ability.CAN_ROOMS | Ability.CAN_AREAS | Ability.CAN_MOBS;
	}

	protected Physical		lastMOB			= null;
	protected MOB			invokerMOB		= null;
	protected boolean		uninvocable		= true;
	protected short			level			= -1;
	protected short			maxTicks		= -1;
	protected boolean		onClosed		= false;
	protected short			chanceToHappen	= -1;

	protected PairList<Ability, Integer>	spellV		= null;
	protected MaskingLibrary.CompiledZMask	compiledMask= null;
	protected volatile boolean				processing  = false;

	protected List<Ability> unrevocableSpells = null;

	@Override
	public long flags()
	{
		return Ability.FLAG_CASTER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	@Override
	protected void finalize()
	{
		spellV=null;
		compiledMask=null;
		chanceToHappen=-1;
		unrevocableSpells=null;
		if((invokerMOB!=null)&&(invokerMOB.Name().equals("invoker")))
			invokerMOB.destroy();
	}

	public String getMaskString(final String newText)
	{
		final int maskindex=newText.toUpperCase().indexOf("MASK=");
		if(maskindex>0)
			return newText.substring(maskindex+5).trim();
		return "";
	}

	public String getParmString(final String newText)
	{
		final int maskindex=newText.toUpperCase().indexOf("MASK=");
		if(maskindex>0)
			return newText.substring(0,maskindex).trim();
		return newText;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		spellV=null;
		compiledMask=null;
		lastMOB=null;
		chanceToHappen=-1;
		onClosed=false;
		maxTicks=-1;
		final String maskString=getMaskString(newText);
		if(maskString.length()>0)
			compiledMask=CMLib.masking().getPreCompiledMask(maskString);
	}

	protected boolean setOtherField(final String var)
	{
		return false;
	}

	protected final PairList<Ability, Integer> getMySpellsV()
	{
		if(spellV!=null)
			return spellV;
		spellV=new PairVector<Ability, Integer>();
		final String names=getParmString(text());
		final List<String> set=CMParms.parseSemicolons(names,true);
		String thisOne=null;
		Integer ticks = Integer.valueOf(-1);
		for(int s=0;s<set.size();s++)
		{
			thisOne=set.get(s);
			if(thisOne.equalsIgnoreCase("NOUNINVOKE"))
			{
				this.uninvocable=false;
				continue;
			}
			if(thisOne.equalsIgnoreCase("ONCLOSED"))
			{
				this.onClosed=true;
				continue;
			}
			if(thisOne.toUpperCase().startsWith("LEVEL"))
			{
				level=(short)CMParms.getParmInt(thisOne,"LEVEL",-1);
				if(level>=0)
					continue;
			}
			if(thisOne.toUpperCase().startsWith("MAXTICKS"))
			{
				maxTicks=(short)CMParms.getParmInt(thisOne,"MAXTICKS",-1);
				if(maxTicks!=-1)
					continue;
			}
			if(thisOne.toUpperCase().startsWith("TICKS"))
			{
				ticks = Integer.valueOf(CMParms.getParmInt(thisOne,"TICKS",-1));
				continue;
			}
			if(setOtherField(thisOne))
				continue;
			//TODO: RESTORE THE COMMENTED OVER THE BELOW:
			/**
			final int pctDex=thisOne.indexOf("% ");
			if((pctDex>0) && (pctDex<5) && (thisOne.substring(pctDex+1).trim().length()>0))
				thisOne=thisOne.substring(pctDex+1).trim();
			final List<Ability> aList=CMLib.coffeeMaker().getCodedEffects(thisOne, null);
			if(aList.size()>0)
			{
				final Ability A=aList.get(0);
				if((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON)
					spellV.add(A, ticks);
			}
			 */
			final int pctDex=thisOne.indexOf("% ");
			if((pctDex>0) && (thisOne.substring(pctDex+1).trim().length()>0))
				thisOne=thisOne.substring(pctDex+1).trim();
			String parm="";
			if((thisOne!=null)&&(thisOne.endsWith(")")))
			{
				final int x=thisOne.indexOf('(');
				if(x>0)
				{
					parm=thisOne.substring(x+1,thisOne.length()-1);
					thisOne=thisOne.substring(0,x).trim();
				}
			}

			Ability A=CMClass.getAbility(thisOne);
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
			{
				A=(Ability)A.copyOf();
				A.setMiscText(parm);
				spellV.add(A, ticks);
			}
		}
		return spellV;
	}

	public boolean didHappen()
	{
		if(chanceToHappen<0)
		{
			final String parmString=getParmString(text());
			int x=parmString.indexOf('%');
			if(x<0)
			{
				chanceToHappen=100;
				return true;
			}
			int mul=1;
			int tot=0;
			while((--x)>=0)
			{
				if(Character.isDigit(parmString.charAt(x)))
					tot+=CMath.s_int(""+parmString.charAt(x))*mul;
				else
					x=-1;
				mul=mul*10;
			}
			chanceToHappen=(short)tot;
		}
		if(CMLib.dice().rollPercentage()<=chanceToHappen)
			return true;
		return false;
	}

	public Map<String, String> makeMySpellsH(final Iterator<Ability> v)
	{
		final Hashtable<String, String> spellH=new Hashtable<String, String>();
		for(;v.hasNext();)
		{
			final Ability A=v.next();
			spellH.put(A.ID(),A.ID());
		}
		return spellH;
	}

	public MOB getBestInvokerMOB(final Environmental target)
	{
		if(target instanceof MOB)
			return (MOB)target;
		if((target instanceof Item)&&(((Item)target).owner()!=null)&&(((Item)target).owner() instanceof MOB))
			return (MOB)((Item)target).owner();
		return null;
	}

	public MOB getInvokerMOB(final Environmental source, final Environmental target)
	{
		MOB mob=getBestInvokerMOB(affected);
		if(mob==null)
			mob=getBestInvokerMOB(source);
		if(mob==null)
			mob=getBestInvokerMOB(target);
		if(mob==null)
			mob=invokerMOB;
		if(mob==null)
		{
			Room R=CMLib.map().roomLocation(target);
			if(R==null)
				R=CMLib.map().roomLocation(target);
			if(R==null)
				R=CMLib.map().getRandomRoom();
			mob=CMLib.map().getFactoryMOB(R);
			mob.setName(L("invoker"));
			mob.basePhyStats().setLevel(affected.phyStats().level());
			mob.phyStats().setLevel(affected.phyStats().level());
		}
		invokerMOB=mob;
		return invokerMOB;
	}

	public List<Triad<Ability, List<String>, Integer>> convertToV2(final PairList<Ability, Integer> spellsV, final Physical target)
	{
		final List<Triad<Ability, List<String>, Integer>> VTOO=new ArrayList<Triad<Ability, List<String>, Integer>>();
		for(int v=0;v<spellsV.size();v++)
		{
			Ability A=spellsV.getFirst(v);
			final Integer ticksA=spellsV.getSecond(v);
			final Ability EA=(target!=null)?target.fetchEffect(A.ID()):null;
			if((EA==null)&&(didHappen()))
			{
				final String t=A.text();
				A=(Ability)A.copyOf();
				List<String> V2=new ArrayList<String>();
				if(t.length()>0)
				{
					final int x=t.indexOf('/');
					if(x<0)
					{
						V2=CMParms.parse(t);
						A.setMiscText("");
					}
					else
					{
						V2=CMParms.parse(t.substring(0,x));
						A.setMiscText(t.substring(x+1));
					}
				}
				VTOO.add(new Triad<Ability, List<String>, Integer>(A, V2, ticksA));
			}
		}
		return VTOO;
	}

	public boolean addMeIfNeccessary(final PhysicalAgent source, final Physical target, final boolean makeLongLasting, int asLevel, final short maxTicks)
	{
		final PairList<Ability, Integer> V=getMySpellsV();
		if((target==null)
		||(V.size()==0)
		||((compiledMask!=null)
			&&(!CMLib.masking().maskCheck(compiledMask,target,true))))
				return false;
		if((affected instanceof Container)
		&&(((Container)affected).isOpen())
		&&onClosed)
			return false;
		final List<Triad<Ability, List<String>, Integer>> VTOO=convertToV2(V,target);
		if(VTOO.size()==0)
			return false;
		final MOB qualMOB=getInvokerMOB(source,target);
		for(int v=0;v<VTOO.size();v++)
		{
			final Triad<Ability, List<String>, Integer> triad = VTOO.get(v);
			final Ability A=triad.first;
			final List<String> V2=triad.second;
			final int ticksA=triad.third.intValue();
			if(level >= 0)
				asLevel = level;
			else
			if(asLevel <=0)
				asLevel = (affected!=null)?affected.phyStats().level():0;
			A.invoke(qualMOB,V2,target,true,asLevel);
			final Ability EA=target.fetchEffect(A.ID());
			lastMOB=target;
			// this needs to go here because otherwise it makes non-item-invoked spells long lasting,
			// which means they dont go away when item is removed.
			if(EA!=null)
			{
				if((maxTicks>0)
				&&(maxTicks<Short.MAX_VALUE)
				&&(CMath.s_int(EA.getStat("TICKDOWN"))>maxTicks))
					EA.setStat("TICKDOWN", Short.toString(maxTicks));
				else
				if((ticksA>0)
				&&(ticksA<Short.MAX_VALUE)
				&&(CMath.s_int(EA.getStat("TICKDOWN"))>ticksA))
					EA.setStat("TICKDOWN", Integer.toString(ticksA));
				else
				if(makeLongLasting)
				{
					EA.makeLongLasting();
					if(!uninvocable)
					{
						EA.makeNonUninvokable();
						if(unrevocableSpells == null)
							unrevocableSpells = new Vector<Ability>();
						unrevocableSpells.add(EA);
					}
				}
			}
		}
		return true;
	}

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Casts ", " on the first one who enters.");
	}

	public void removeMyAffectsFromLastMOB()
	{
		removeMyAffectsFrom(lastMOB);
		lastMOB=null;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if(P == null)
		{
			removeMyAffectsFromLastMOB();
			finalize();
		}
	}

	public void removeMyAffectsFrom(final Physical P)
	{
		if(P==null)
			return;

		int x=0;
		final Vector<Ability> eff=new Vector<Ability>();
		Ability thisAffect=null;
		for(x=0;x<P.numEffects();x++) // personal
		{
			thisAffect=P.fetchEffect(x);
			if(thisAffect!=null)
				eff.addElement(thisAffect);
		}
		if(eff.size()>0)
		{
			final Map<String,String> h=makeMySpellsH(getMySpellsV().firstIterator());
			if(unrevocableSpells != null)
			{
				for(int v=unrevocableSpells.size()-1;v>=0;v--)
				{
					thisAffect = unrevocableSpells.get(v);
					if(h.containsKey(thisAffect.ID()))
						P.delEffect(thisAffect);
				}
			}
			else
			for(x=0;x<eff.size();x++)
			{
				thisAffect=eff.elementAt(x);
				final String ID=h.get(thisAffect.ID());
				if((ID!=null)
				&&(thisAffect.invoker()==getInvokerMOB(P,P)))
				{
					thisAffect.unInvoke();
					if((!uninvocable)&&(!thisAffect.canBeUninvoked()))
						P.delEffect(thisAffect);
				}
			}
			unrevocableSpells = null;
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((affected instanceof Room)||(affected instanceof Area))
		{
			if((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_RECALL))
				removeMyAffectsFrom(msg.source());
			if(msg.targetMinor()==CMMsg.TYP_ENTER)
				addMeIfNeccessary(msg.source(),msg.source(),true,0,maxTicks);
		}
		super.executeMsg(host,msg);
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		if(processing)
			return;
		if((affected instanceof MOB)
		   ||(affected instanceof Item))
		{
			try
			{
				processing=true;
				if((lastMOB!=null)
				&&(host!=lastMOB))
					removeMyAffectsFrom(lastMOB);

				if((lastMOB==null)&&(host instanceof PhysicalAgent))
					addMeIfNeccessary((PhysicalAgent)host,host,true,0,maxTicks);
			}
			finally
			{
				processing=false;
			}
		}
	}

	public String spellAccountingsWithMask(final String pre, final String post)
	{
		final PairList<Ability, Integer> spellList = getMySpellsV();
		String id="";
		for(int v=0;v<spellList.size();v++)
		{
			final Ability A=spellList.get(v).first;
			if(spellList.size()==1)
				id+=A.name();
			else
			if(v==(spellList.size()-1))
				id+="and "+A.name();
			else
				id+=A.name()+", ";
		}
		if(spellList.size()>0)
			id=pre+id+post;
		final String maskString=getMaskString(text());
		if(maskString.length()>0)
			id+="  Restrictions: "+CMLib.masking().maskDesc(maskString);
		return id;
	}

	@Override
	public void addAbility(final Ability to)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void delAbility(final Ability to)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public int numAbilities()
	{
		return getMySpellsV().size();
	}

	@Override
	public Ability fetchAbility(final int index)
	{
		final PairList<Ability, Integer> spellsV = getMySpellsV();
		if (spellsV.size() == 0)
			return null;
		if ((index < 0) || (index >= spellsV.size()))
			return null;
		try
		{
			return spellsV.get(index).first;
		}
		catch (final Exception e)
		{
			return null;
		}
	}

	@Override
	public Ability fetchAbility(final String ID)
	{
		for (final Enumeration<Ability> a = abilities(); a.hasMoreElements();)
		{
			final Ability A = a.nextElement();
			if (A == null)
				continue;
			if (A.ID().equalsIgnoreCase(ID))
				return A;
		}
		return null;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		final PairList<Ability, Integer> spellsV = getMySpellsV();
		if (spellsV.size() == 0)
			return null;
		return spellsV.get(CMLib.dice().roll(1, spellsV.size(), -1)).first;
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		return new FilteredEnumeration<Ability>(new IteratorEnumeration<Ability>(getMySpellsV().firstIterator()),new Filterer<Ability>()
		{
			@Override
			public boolean passesFilter(final Ability obj)
			{
				return didHappen();
			}
		});
	}

	@Override
	public void delAllAbilities()
	{
		setMiscText("");
	}

	@Override
	public int numAllAbilities()
	{
		return numAbilities();
	}

	@Override
	public Enumeration<Ability> allAbilities()
	{
		return new IteratorEnumeration<Ability>(getMySpellsV().firstIterator());
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String s=CMParms.combine(commands,0);
		if(s.length()>0)
			setMiscText(s);
		if(givenTarget!=null)
			addMeIfNeccessary(mob,givenTarget,false,asLevel,maxTicks);
		return true;
	}
}

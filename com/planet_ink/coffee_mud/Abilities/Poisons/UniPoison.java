package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
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
import java.util.regex.Pattern;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class UniPoison extends GenPoison implements SpellHolder
{
	public UniPoison()
	{
		super();
		super.ID	= "UniPoison";
	}

	@Override
	protected Object[] makeEmpty()
	{
		final Object[] O=super.makeEmpty();
		O[V_NAME]="Uni-Poison";
		O[V_TRIG]=new String[]{"UPOISON"};
		return O;
	}

	@Override
	protected void cloneFix(final Ability E)
	{
		super.cloneFix(E);
		if(E instanceof UniPoison)
			((UniPoison)E).vars=Arrays.copyOf(vars, NUM_VS);
	}

	protected Object[] vars=this.makeEmpty();
	protected volatile UniPoison parent = null;
	protected final List<UniPoison> siblings = new SVector<UniPoison>();

	@Override
	protected Object V(final String ID, final int varNum)
	{
		return vars[varNum];
	}

	@Override
	protected void SV(final String ID,final int varNum, final Object O)
	{
		vars[varNum] = O;
	}

	@Override
	protected void addEffect(final MOB invokerMOB, final Physical affected)
	{
		final UniPoison P = (UniPoison)affected.fetchEffect("UniPoison");
		if(P == null)
			super.addEffect(invokerMOB, affected);
		else
		if(!P.sameAs(this))
		{
			for(final UniPoison sibP : P.siblings)
				if(sibP.sameAs(this))
				{
					if(tickDown > sibP.tickDown)
						sibP.tickDown = tickDown;
					return;
				}
			setAffectedOne(affected);
			parent = P;
			P.siblings.add(this);
		}
	}

	@Override
	protected boolean alreadyAffected(final Physical affected)
	{
		final UniPoison P = (UniPoison)affected.fetchEffect("UniPoison");
		if(P == null)
			return false;
		if(P.sameAs(this))
			return true;
		for(final UniPoison sibP : P.siblings)
			if(sibP.sameAs(this))
				return true;
		return false;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(siblings.size()>0)
			for(final UniPoison P : siblings)
				P.affectCharStats(affected, affectableStats);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(siblings.size()>0)
			for(final UniPoison P : siblings)
				P.affectPhyStats(affected, affectableStats);
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		super.affectCharState(affectedMob, affectableMaxState);
		if(siblings.size()>0)
			for(final UniPoison P : siblings)
				P.affectCharState(affectedMob, affectableMaxState);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(siblings.size()>0)
		{
			for(final Iterator<UniPoison> p = siblings.iterator(); p.hasNext(); )
			{
				final UniPoison P = p.next();
				if((!P.tick(ticking, tickID))||(P.amDestroyed())||(P.unInvoked))
					p.remove();
			}
		}
		if(!super.tick(ticking, tickID))
		{
			if(siblings.size()>0)
			{
				final UniPoison newMeP = siblings.remove(0);
				if(newMeP != null)
				{
					newMeP.parent = null;
					this.unInvoked = newMeP.unInvoked;
					this.tickDown = newMeP.tickDown;
					this.amDestroyed = newMeP.amDestroyed;
					this.canBeUninvoked = newMeP.canBeUninvoked;
					newMeP.cloneFix(this);
					return !this.unInvoked;
				}

			}
			return false;
		}
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText != null)
		{
			super.miscText = newMiscText;
			final Map<String, String> map = CMParms.parseStrictEQParms(newMiscText);
			for(final String code : map.keySet())
				if(code.equals("RANK"))
					super.rank = CMath.s_double(map.get(newMiscText));
				else
					setStat(code, map.get(code));
		}
	}

	@Override
	public String getHealthConditionDesc()
	{
		if(siblings.size()==0)
			return super.getHealthConditionDesc();
		final List<String> names = new ArrayList<String>();
		names.add(name());
		for(final UniPoison P : siblings)
			if(P.name().toUpperCase().endsWith("S"))
				names.add(name());
			else
				names.add(L("@x1 poisoning",name()));
		return L("Suffering from @x1.", CMLib.english().toEnglishStringList(names.toArray(new String[names.size()]), true));
	}

	@Override
	protected void getAddicted(final MOB mob, final MOB targetMOB)
	{
		Ability A=targetMOB.fetchEffect("Addictions");
		if(A==null)
		{
			A=CMClass.getAbility("Addictions");
			if(A!=null)
				A.invoke(mob, new XVector<String>("effect:name:"+Name()), null, true, 0);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			break;
		case 1:
			setMiscText(val);
			break;
		default:
			super.setStat(code, val);
			break;
		}
	}

	@Override
	public String getStat(final String code)
	{
		if (code.equalsIgnoreCase("javaclass"))
			return "UniPoison";
		return super.getStat(code);
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!super.sameAs(E))
			return false;
		for(final String code : getStatCodes())
			if(!getStat(code).equals(E.getStat(code)))
				return false;
		return true;
	}

	@Override
	public List<Ability> getSpells()
	{
		final List<Ability> spells = new ArrayList<Ability>();
		spells.add(this);
		for(final Iterator<UniPoison> p = siblings.iterator(); p.hasNext(); )
			spells.addAll(p.next().getSpells());
		spells.addAll(super.getSpells());
		return spells;
	}

	@Override
	public String getSpellList()
	{
		return "";
	}

	@Override
	public void setSpellList(final String list)
	{
	}
}

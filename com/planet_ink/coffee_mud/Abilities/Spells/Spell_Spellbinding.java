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
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Spell_Spellbinding extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Spellbinding";
	}

	private final static String	localizedName	= CMLib.lang().L("Spellbinding");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int overrideMana()
	{
		return 0;
	}

	@Override
	public String displayText()
	{
		if (spellbindings.size() == 0)
			return "";
		final StringBuffer bindings = new StringBuffer("");
		for (int i = 0; i < spellbindings.size(); i++)
			bindings.append(" " + (spellbindings.get(i).first));
		return "(Bindings: " + bindings.toString() + ")";
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
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return spellbindings.size() > 0;
	}

	protected PairList<String,PairList<String,Integer>> spellbindings=new PairVector<String,PairList<String,Integer>>();
	protected final static int COST_STATIC=50;

	@Override
	public void affectCharState(final MOB affected, final CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		int total=0;
		for(int i=0;i<spellbindings.size();i++)
		{
			final PairList<String,Integer> V=spellbindings.get(i).second;
			for(int x=0;x<V.size();x++)
				total+=V.get(x).second.intValue();
		}
		total=(total+COST_STATIC)*spellbindings.size();
		if(affectableState.getMana()>=total)
			affectableState.setMana(affectableState.getMana()-total);
		else
			affectableState.setMana(0);
	}

	@Override
	public String text()
	{
		if(spellbindings.size()==0)
			return super.text();
		try
		{
			final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
			new ObjectOutputStream(bytes).writeObject(spellbindings);
			return CMParms.toSemicolonListString(bytes.toByteArray())+";";
		}
		catch(final Exception e)
		{
			Log.errOut("Spell_Spellbinding","Spell bindings are corrupt for "+((affected!=null)?affected.Name():"someone")+".");
		}
		return super.text();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setMiscText(final String text)
	{
		if(text.length()==0)
			spellbindings=new PairVector<String,PairList<String,Integer>>();
		else
		{
			try
			{
				final ByteArrayInputStream bytes=new ByteArrayInputStream(CMParms.parseSemicolonByteList(text));
				spellbindings=(PairList<String,PairList<String,Integer>>)new ObjectInputStream(bytes).readObject();
			}
			catch(final Exception e)
			{
				Log.errOut("Spell_Spellbinding",e);
			}
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			final String s=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(s!=null)
			{
				for(int v=0;v<spellbindings.size();v++)
				{
					if(spellbindings.get(v).first.equalsIgnoreCase(s))
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
							msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.TYP_WAND_USE,L("The magic of '@x1' swells within you!",s),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					}
				}
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_WAND_USE)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			final String s=CMStrings.getSayFromMessage(msg.sourceMessage());
			if(s!=null)
			{
				for(int v=spellbindings.size()-1;v>=0;v--)
				{
					if(spellbindings.get(v).first.equalsIgnoreCase(s))
					{
						final PairList<String,Integer> V2=spellbindings.get(v).second;
						for(int v2=0;v2<V2.size();v2++)
						{
							final Ability A=msg.source().fetchAbility(V2.get(v2).first);
							final int curMana=msg.source().curState().getMana();
							msg.source().curState().setMana(1000);
							if(msg.target()!=null)
								A.invoke(msg.source(),CMParms.parse(msg.target().Name()),null,false,0);
							else
								A.invoke(msg.source(),new Vector<String>(),null,false,0);
							msg.source().curState().setMana(curMana);
						}
						if(canBeUninvoked())
							spellbindings.remove(v);
					}
				}
			}
		}
		if((spellbindings.size()==0)&&(canBeUninvoked()))
			unInvoke();
		super.executeMsg(host,msg);
	}

	protected boolean checkBindableAbility(final MOB mob, final Ability A, final String wd)
	{
		if(A==null)
		{
			mob.tell(L("You can't bind '@x1'.",wd));
			return false;
		}
		if((A.classificationCode()&ALL_ACODES)!=ACODE_SPELL)
		{
			mob.tell(L("You can't bind '@x1' -- it's not a spell!",A.ID()));
			return false;
		}
		if(A.ID().equals(ID()))
		{
			mob.tell(L("You can't bind '@x1'.",A.ID()));
			return false;
		}
		if(A.usageCost(mob,true)[Ability.USAGEINDEX_MANA]>50)
		{
			mob.tell(L("You can't bind '@x1' -- it requires too much mana.",A.ID()));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final MOB target = mob;
		Spell_Spellbinding priorBinding=(Spell_Spellbinding)target.fetchEffect(ID());
		if(commands.size()<2)
		{
			mob.tell(L("You must specify your trigger word, followed by a list of spells, separated by spaces."));
			return false;
		}
		final String key=commands.get(0);
		commands.remove(0);
		final String combined=CMParms.combine(commands,0);
		final PairList<String,Integer> V=new PairVector<String,Integer>();
		Ability A=mob.fetchAbility(combined);
		if(A==null)
		{
			for(final Enumeration<Ability> a = mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A1 = a.nextElement();
				if((A1!=null)
				&&(A1.name().equalsIgnoreCase(combined)))
				{
					A=A1;
					break;
				}
			}
		}
		if(A!=null)
		{
			if(!checkBindableAbility(mob,A,combined))
				return false;
			V.add(A.ID(),Integer.valueOf(A.usageCost(mob,true)[Ability.USAGEINDEX_MANA]));
		}
		else
		{
			for(int v=0;v<commands.size();v++)
			{
				A=mob.fetchAbility(commands.get(v));
				if(A==null)
				{
					for(final Enumeration<Ability> a = mob.allAbilities();a.hasMoreElements();)
					{
						final Ability A1 = a.nextElement();
						if((A1!=null)
						&&(A1.name().equalsIgnoreCase(commands.get(v))))
						{
							A=A1;
							break;
						}
					}
				}
				if(!checkBindableAbility(mob,A,commands.get(v)))
					return false;
				if(A!=null)
					V.add(A.ID(),Integer.valueOf(A.usageCost(mob,true)[Ability.USAGEINDEX_MANA]));
			}
		}

		int totalcost=0;
		for(int v=0;v<V.size();v++)
			totalcost+=V.get(v).second.intValue();
		totalcost=(totalcost+COST_STATIC)*V.size();
		final int curMana=mob.curState().getMana();
		if(curMana<totalcost)
		{
			mob.tell(L("You need @x1 mana to bind those spells.",""+totalcost));
			return false;
		}
		PairList<String,Integer> thePriorKey=null;
		if(priorBinding!=null)
		{
			for(int x=0;x<priorBinding.spellbindings.size();x++)
			{
				if(priorBinding.spellbindings.get(x).first.equalsIgnoreCase(key))
				{
					thePriorKey = priorBinding.spellbindings.get(x).second;
				}
			}
		}

		for(int v=0;v<V.size();v++)
		{
			for(int v2=0;v2<V.size();v2++)
			{
				if((v!=v2)&&(V.get(v).first.equals(V.get(v2).first)))
				{
					mob.tell(L("The same spell can not be bound to the same trigger more than once."));
					return false;
				}
			}
		}
		if(thePriorKey!=null)
		{
			for(int v=0;v<V.size();v++)
			{
				if(thePriorKey.containsFirst(V.get(v).first))
				{
					mob.tell(L("The same spell can not be bound to the same trigger more than once."));
					return false;
				}
			}
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(mob.curState().getMana()>(curMana-totalcost))
			mob.curState().setMana(curMana-totalcost);

		if(success)
		{
			final CMMsg msg = CMClass.getMsg(mob, null, this, verbalCastCode(mob,target,auto),
					L(auto?"":"^S<S-NAME> shout(s) the magic of spellbinding!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(priorBinding==null)
				{
					beneficialAffect(mob,target,asLevel,0);
					priorBinding=(Spell_Spellbinding)target.fetchEffect(ID());
					if(priorBinding==null)
						return false;
					priorBinding.makeLongLasting();
				}
				if(thePriorKey==null)
					priorBinding.spellbindings.add(key,V);
				else
				for(int v=0;v<V.size();v++)
					thePriorKey.add(V.get(v).first, V.get(v).second);
				target.recoverMaxState();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> whisper(s) about spellbinding, and the magic fizzles."));

		// return whether it worked
		return success;
	}
}

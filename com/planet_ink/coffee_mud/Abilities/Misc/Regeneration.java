package com.planet_ink.coffee_mud.Abilities.Misc;
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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Regeneration extends StdAbility implements HealthCondition
{
	private static final int	maxTickDown	= 3;
	protected int				regenTick	= maxTickDown;

	@Override
	public String ID()
	{
		return "Regeneration";
	}

	private final static String	localizedName	= CMLib.lang().L("Stat Regeneration");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Stat Regeneration)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "REGENERATE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY|Ability.DOMAIN_RACIALABILITY;
	}

	protected int	permanentDamage	= 0;

	@Override
	public String getHealthConditionDesc()
	{
		return "Possesses regenerative cells.";
	}

	protected static enum RecType
	{
		BURST,
		HEALTH,
		HITS,
		MANA,
		MOVE
	}

	@SuppressWarnings("unchecked")
	protected Triad<RecType,Integer,int[]>[] tickChanges = new Triad[0];
	protected Set<String> weapExceptions = Collections.synchronizedSet(new HashSet<String>());
	protected int weapMinLevel			= -1;

	public boolean recoverTick(final MOB M)
	{
		if((M==null)||(tickChanges.length==0))
			return false;
		boolean changed = false;
		for(final Triad<RecType,Integer,int[]> typ : tickChanges)
		{
			final int[] td;
			synchronized(typ.third)
			{
				td = typ.third;
			}
			if(--td[0] > 0)
				continue;
			td[0] = td[1];
			if((td.length==3) && (td[2] == 1))
			{
				final int val = typ.second.intValue();
				switch(typ.first)
				{
				case BURST:
				case HEALTH:
					if(M.curState().getHitPoints()<M.maxState().getHitPoints())
						changed = !M.curState().adjHitPoints(val, M.maxState()) || changed;
					changed = !M.curState().adjMana(val, M.maxState()) || changed;
					changed = !M.curState().adjMovement(val, M.maxState()) || changed;
					break;
				case HITS:
					if(M.curState().getHitPoints()<M.maxState().getHitPoints())
						changed = !M.curState().adjHitPoints(val, M.maxState()) || changed;
					break;
				case MANA:
					changed = !M.curState().adjMana(val, M.maxState()) || changed;
					break;
				case MOVE:
					changed = !M.curState().adjMovement(val, M.maxState()) || changed;
					break;
				default:
					break;
				}
			}
			else
			{
				switch(typ.first)
				{
				case BURST:
					for(int i2=0;i2<typ.second.intValue();i2++)
					{
						M.tick(M,Tickable.TICKID_MOB);
						changed=true;
					}
					break;
				case HEALTH:
					for(int i2=0;i2<typ.second.intValue();i2++)
					{
						if(M.curState().getHitPoints()<M.maxState().getHitPoints())
							changed = CMLib.combat().recoverTick(M) || changed;
					}
					break;
				case HITS:
				{
					if(M.curState().getHitPoints()<M.maxState().getHitPoints())
					{
						final int oldMana=M.curState().getMana();
						final int oldMove=M.curState().getMovement();
						for(int i2=0;i2<RecType.HITS.ordinal();i2++)
							changed = CMLib.combat().recoverTick(M);
						M.curState().setMana(oldMana);
						M.curState().setMovement(oldMove);
					}
					break;
				}
				case MANA:
				{
					if(M.curState().getMana() < M.maxState().getMana())
					{
						final int oldHP=M.curState().getHitPoints();
						final int oldMove=M.curState().getMovement();
						for(int i2=0;i2<RecType.MANA.ordinal();i2++)
							CMLib.combat().recoverTick(M);
						M.curState().setHitPoints(oldHP);
						M.curState().setMovement(oldMove);
					}
					break;
				}
				case MOVE:
				{
					if(M.curState().getMovement() < M.maxState().getMovement())
					{
						final int oldMana=M.curState().getMana();
						final int oldHP=M.curState().getHitPoints();
						for(int i2=0;i2<RecType.MOVE.ordinal();i2++)
							CMLib.combat().recoverTick(M);
						M.curState().setMana(oldMana);
						M.curState().setHitPoints(oldHP);
					}
					break;
				}
				default:
					break;
				}
			}
		}
		return changed;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((--regenTick)>0)
			return true;
		regenTick=maxTickDown;
		final MOB mob=(MOB)affected;
		if(mob==null)
			return true;
		if(mob.location()==null)
			return true;
		if(mob.amDead())
			return true;

		boolean doneAnything=false;
		if(tickChanges.length==0)
		{
			if(mob.curState().getHitPoints()<mob.maxState().getHitPoints())
				doneAnything=doneAnything||mob.curState().adjHitPoints((int)Math.round(CMath.div(mob.phyStats().level(),2.0)),mob.maxState());
			doneAnything=doneAnything||mob.curState().adjMana(mob.phyStats().level()*2,mob.maxState());
			doneAnything=doneAnything||mob.curState().adjMovement(mob.phyStats().level()*3,mob.maxState());
		}
		else
			doneAnything = this.recoverTick(mob);
		if(doneAnything)
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> regenerate(s)."));
		return true;
	}

	@Override
	public void setMiscText(final String parameters)
	{
		super.setMiscText(parameters);
		this.weapExceptions.clear();
		weapMinLevel=-1;
		if(parameters == null)
			return;
		for(int i=0;i<parameters.length();i++)
		{
			if(parameters.charAt(i)=='+')
			{
				final int x = parameters.indexOf(' ',i+1);
				final String word = ((x>i)?parameters.substring(i+1,x):parameters.substring(x+1)).toUpperCase().trim();
				if(word.startsWith("LEVEL"))
				{
					weapMinLevel = CMath.s_int(word.substring(5).trim());
					i = i + word.length();
				}
				else
				if(word.equals("MAGIC")
				||(CMParms.indexOf(Weapon.TYPE_DESCS, word)>=0)
				||(RawMaterial.CODES.FIND_IgnoreCase(word)>=0))
				{
					this.weapExceptions.add(word);
					i = i + word.length();
				}
			}
		}
		final List<Triad<RecType,Integer,int[]>> lst = new ArrayList<Triad<RecType,Integer,int[]>>();
		for(final RecType r : RecType.values())
		{
			String val = CMParms.getParmStr(parameters, r.name(), "").trim();
			if(val.length()>0)
			{
				boolean abs = false;
				if(((val.charAt(0)=='+') || (val.charAt(0)=='-'))
				&&(val.length()>1))
				{
					val=val.substring(1);
					abs=true;
				}
				if(Character.isDigit(val.charAt(0)))
				{
					int valn;
					int ticks = 1;
					final int x=val.indexOf('/');
					if(x>0)
					{
						valn = CMath.s_int(val.substring(0,x).trim());
						ticks = CMath.s_int(val.substring(x+1).trim());
					}
					else
						valn  = CMath.s_int(val);
					if(valn != 0)
					{
						if(abs)
							lst.add(new Triad<RecType,Integer,int[]>(r,Integer.valueOf(valn),new int[] {ticks, ticks, 0}));
						else
							lst.add(new Triad<RecType,Integer,int[]>(r,Integer.valueOf(valn),new int[] {ticks, ticks}));
					}
				}
				else
					Log.errOut("Unknown val '"+val+"' on FasterRecovery");
			}
		}
		@SuppressWarnings("unchecked")
		final Triad<RecType,Integer,int[]>[] ch = new Triad[lst.size()];
		tickChanges = lst.toArray(ch);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			if(msg.amISource(M)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			{
				permanentDamage=0;
				M.recoverMaxState();
			}
			else
			if((msg.amITarget(M))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&((weapExceptions.size()>0)||(weapMinLevel>=0)))
			{
				boolean hurts=false;
				if(msg.tool() instanceof Weapon)
				{
					final Weapon W=(Weapon)msg.tool();
					hurts = this.weapExceptions.contains(Weapon.TYPE_DESCS[W.weaponDamageType()]);
					if(CMLib.flags().isABonusItems(W))
						hurts = hurts || this.weapExceptions.contains("MAGIC");
					hurts = hurts || (W.phyStats().level()>=weapMinLevel);
					hurts = hurts || this.weapExceptions.contains(RawMaterial.CODES.NAME(W.material()));
				}
				else
				if(msg.tool() instanceof Ability)
				{
					final int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES;
					switch(classType)
					{
					case Ability.ACODE_SPELL:
					case Ability.ACODE_PRAYER:
					case Ability.ACODE_CHANT:
					case Ability.ACODE_SONG:
						hurts = hurts || this.weapExceptions.contains("MAGIC");
						break;
					default:
						break;
					}
				}
				if(hurts)
				{
					permanentDamage+=msg.value();
					M.recoverMaxState();
				}
			}

		}
		return true;
	}

	@Override
	public void affectCharState(final MOB mob, final CharState state)
	{
		super.affectCharState(mob,state);
		state.setHitPoints(state.getHitPoints()-permanentDamage);
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("You feel less regenerative."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?"":L("<S-NAME> lay(s) regenerative magic upon <T-NAMESELF>.");
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,str);
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				success=beneficialAffect(mob,target,asLevel,0)!=null;
			}
		}
		return success;
	}
}

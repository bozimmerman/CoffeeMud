package com.planet_ink.coffee_mud.Abilities.Traps;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class StdTrap extends StdAbility implements Trap
{
	@Override
	public String ID()
	{
		return "StdTrap";
	}

	private final static String	localizedName	= CMLib.lang().L("standard trap");

	@Override
	public String name()
	{
		return localizedName;
	}

	public static final String[]	TRIGGER	= { "SPRING" };

	@Override
	public String[] triggerStrings()
	{
		return TRIGGER;
	}

	protected boolean sprung=false;
	protected int reset=60; // 5 minute reset is standard
	protected int ableCode=0;
	protected boolean disabled=false;

	public StdTrap()
	{
		super();
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected int trapLevel()
	{
		return -1;
	}

	@Override
	public void setAbilityCode(int code)
	{
		ableCode = code;
	}

	@Override
	public int abilityCode()
	{
		return ableCode;
	}

	@Override
	public boolean isABomb()
	{
		return false;
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	protected List<String> newMessaging=new ArrayList<String>(0);
	private String invokerName=null;

	public PairVector<MOB,Integer> safeDirs=null;

	public int baseRejuvTime(int level)
	{
		if(level>=30)
			level=29;
		int ticks=(int)Math.round((30.0-(CMath.mul(level,.75)))*30.0);
		if(ticks<1)
			ticks=1;
		return ticks;
	}

	public int baseDestructTime(int level)
	{
		return level*30;
	}

	public boolean getTravelThroughFlag()
	{
		return false;
	}

	@Override
	public boolean disabled()
	{
		return (sprung&&disabled)
			   ||(affected==null)
			   ||(affected.fetchEffect(ID())==null);
	}

	public boolean doesSaveVsTraps(MOB target)
	{
		int save=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS);
		if(invoker()!=null)
		{
			save += target.phyStats().level();
			save -= invoker().phyStats().level();
		}
		return (CMLib.dice().rollPercentage()<=save);
	}

	public boolean isLocalExempt(MOB target)
	{
		if(target==null)
			return false;
		final Room R=target.location();
		if((!canBeUninvoked())
		&&(!isABomb())
		&&(R!=null))
		{
			if((CMLib.law().getLandTitle(R)!=null)
			&&(CMLib.law().doesHavePriviledgesHere(target,R)))
				return true;

			if((target.isMonster())
			&&(target.getStartRoom()!=null)
			&&(target.getStartRoom().getArea()==R.getArea()))
				return true;
		}
		return false;
	}

	@Override
	public void disable()
	{
		disabled=true;
		sprung=true;
		if(!canBeUninvoked())
		{
			tickDown=getReset();
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
		}
		else
			unInvoke();
	}

	@Override
	public void setReset(int Reset)
	{
		reset = Reset;
	}

	@Override
	public int getReset()
	{
		return reset;
	}

	@Override
	public MOB invoker()
	{
		if(invoker==null)
		{
			if((invokerName!=null)&&(!invokerName.equalsIgnoreCase("null")))
				invoker=CMLib.players().getLoadPlayer(invokerName);
			if(invoker==null)
			{
				invoker=CMClass.getMOB("StdMOB");
				invoker.setLocation(CMClass.getLocale("StdRoom"));
				invoker.basePhyStats().setLevel(affected.phyStats().level());
				invoker.phyStats().setLevel(affected.phyStats().level());
			}
		}
		else
			invokerName=invoker.Name();
		return super.invoker();
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_TRAP;
	}

	@Override
	public void setMiscText(String text)
	{
		text=text.trim();
		if(text.startsWith("`"))
		{
			final int x=text.indexOf("` ",1);
			if(x>=0)
			{
				invokerName=text.substring(1,x);
				text=text.substring(x+2).trim();
			}
		}
		while(text.startsWith("\""))
		{
			final int x=text.indexOf("\"",1);
			if(x>=0)
			{
				newMessaging.add(text.substring(1,x));
				text=text.substring(x+1).trim();
			}
			else
				break;
		}
		if(text.startsWith(":"))
		{
			final int x=text.indexOf(':');
			final int y=text.indexOf(':',x+1);
			if((x>=0)&&(y>x)&&(CMath.isInteger(text.substring(x+1,y).trim())))
			{
				setAbilityCode(CMath.s_int(text.substring(x+1,y).trim()));
				text=text.substring(y+1).trim();
			}
		}
		super.setMiscText(text);
	}

	@Override
	public String text()
	{
		return "`"+invokerName+"` :"+abilityCode()+":"+super.text();
	}

	public synchronized PairVector<MOB,Integer> getSafeDirs()
	{
		if(safeDirs == null)
			safeDirs=new PairVector<MOB,Integer>();
		return safeDirs;
	}

	@Override
	public CMObject copyOf()
	{
		final StdTrap obj=(StdTrap)super.copyOf();
		obj.safeDirs=null;
		return obj;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!disabled())&&(affected instanceof Item))
		{
			if((msg.tool()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_GIVE)
			&&(msg.targetMessage()!=null)
			&&(msg.target() instanceof MOB)
			&&(!msg.source().getGroupMembers(new HashSet<MOB>()).contains(msg.target())))
			{
				msg.source().tell((MOB)msg.target(),msg.tool(),null,L("<S-NAME> can't accept <T-NAME>."));
				return false;
			}
		}
		if((!sprung)
		&& CMath.bset(canAffectCode(),Ability.CAN_ROOMS)
		&& getTravelThroughFlag()
		&& msg.amITarget(affected)
		&& (affected instanceof Room)
		&& (msg.tool() instanceof Exit))
		{
			final Room room=(Room)affected;
			if ((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_FLEE))
			{
				final int movingInDir=CMLib.map().getExitDir(room, (Exit)msg.tool());
				if((movingInDir!=Directions.DOWN)&&(movingInDir!=Directions.UP))
				{
					final PairVector<MOB,Integer> safeDirs=getSafeDirs();
					synchronized(safeDirs)
					{
						for(final Iterator<Pair<MOB,Integer>> i=safeDirs.iterator();i.hasNext();)
						{
							final Pair<MOB,Integer> p=i.next();
							if(p.first == msg.source())
							{
								i.remove();
								if(movingInDir==p.second.intValue())
									return true;
								spring(msg.source());
								return !sprung();
							}
						}
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void activateBomb()
	{
		if(isABomb())
		{
			tickDown=getReset();
			sprung=false;
			disabled=false;
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!sprung)
		if(CMath.bset(canAffectCode(),Ability.CAN_EXITS))
		{
			if(msg.amITarget(affected))
			{
				if((affected instanceof Exit)
				&&(((Exit)affected).hasADoor())
				&&(((Exit)affected).hasALock())
				&&(((Exit)affected).isLocked()))
				{
					if(msg.targetMinor()==CMMsg.TYP_UNLOCK)
						spring(msg.source());
				}
				else
				if((affected instanceof Container)
				&&(((Container)affected).hasADoor())
				&&(((Container)affected).hasALock())
				&&(((Container)affected).isLocked()))
				{
					if(msg.targetMinor()==CMMsg.TYP_UNLOCK)
						spring(msg.source());
				}
				else
				if(msg.targetMinor()==CMMsg.TYP_OPEN)
					spring(msg.source());
			}
		}
		else
		if(CMath.bset(canAffectCode(),Ability.CAN_ITEMS))
		{
			if(isABomb())
			{
				if(msg.amITarget(affected))
				{
					if((msg.targetMinor()==CMMsg.TYP_HOLD)
					&&(msg.source().isMine(affected)))
					{
						msg.source().tell(msg.source(),affected,null,L("You activate <T-NAME>."));
						activateBomb();
					}
				}
			}
			else
			if(msg.amITarget(affected))
			{
				if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
				&&(!msg.source().isMine(affected)))
					spring(msg.source());
			}
		}
		else
		if(CMath.bset(canAffectCode(), Ability.CAN_ROOMS)
		&& msg.amITarget(affected)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER))
		{
			if(getTravelThroughFlag())
			{
				if ((affected instanceof Room)
				&& (msg.tool() instanceof Exit))
				{
					final Room room=(Room)affected;
					final int movingInDir=CMLib.map().getExitDir(room, (Exit)msg.tool());
					if((movingInDir!=Directions.DOWN)&&(movingInDir!=Directions.UP))
					{
						final PairVector<MOB,Integer> safeDirs=getSafeDirs();
						synchronized(safeDirs)
						{
							final int dex=safeDirs.indexOf(msg.source());
							if(dex>=0)
								safeDirs.remove(dex);
							while(safeDirs.size()>room.numInhabitants()+1)
								safeDirs.remove(0);
							safeDirs.add(new Pair<MOB,Integer>(msg.source(),Integer.valueOf(movingInDir)));
						}
					}
				}
			}
			else
			if(!msg.source().isMine(affected))
				spring(msg.source());
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean maySetTrap(MOB mob, int asLevel)
	{
		if(mob==null)
			return false;
		if(trapLevel()<0)
			return false;
		if(asLevel<0)
			return true;
		if(asLevel>=trapLevel())
			return true;
		return false;
	}

	@Override
	public boolean canReSetTrap(MOB mob)
	{
		final Physical P=affected;
		if(P==null)
		{
			if(mob!=null)
				mob.tell(L("This trap is not presently set."));
			return false;
		}
		if(mob!=null)
		{
			if((!maySetTrap(mob,mob.phyStats().level()))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
			{
				mob.tell(L("You are not high enough level (@x1) to set that trap.",""+trapLevel()));
				return false;
			}
		}
		final Trap T=(Trap)P.fetchEffect(ID());
		if(T!=this)
		{
			if(mob!=null)
				mob.tell(L("This trap is not presently set on @x1.",P.name()));
			return false;
		}
		
		if(T.invoker() != mob)
		{
			if(mob!=null)
				mob.tell(L("The trap was not set by you."));
			return false;
		}
		
		return true;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		if(mob!=null)
		{
			if((!maySetTrap(mob,mob.phyStats().level()))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
			{
				mob.tell(L("You are not high enough level (@x1) to set that trap.",""+trapLevel()));
				return false;
			}
		}
		if(P.fetchEffect(ID())!=null)
		{
			if(mob!=null)
				mob.tell(L("This trap is already set on @x1.",P.name()));
			return false;
		}
		if(!canAffect(P))
		{
			if(mob!=null)
				mob.tell(L("You can't set '@x1' on @x2.",name(),P.name()));
			return false;
		}
		if((canAffectCode()&Ability.CAN_EXITS)==Ability.CAN_EXITS)
		{
			if((P instanceof Item)&&(!(P instanceof Container)))
			{
				if(mob!=null)
					mob.tell(L("@x1 has no lid, so '@x2' cannot be set on it.",P.name(),name()));
				return false;
			}
			if(((P instanceof Exit)&&(!(((Exit)P).hasADoor()))))
			{
				if(mob!=null)
					mob.tell(L("@x1 has no door, so '@x2' cannot be set on it.",P.name(),name()));
				return false;
			}
			if(((P instanceof Container)&&(!(((Container)P).hasADoor()))))
			{
				if(mob!=null)
					mob.tell(L("@x1 has no lid, so '@x2' cannot be set on it.",P.name(),name()));
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		return new Vector<Item>(1);
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null)
			return null;
		final int rejuv=baseRejuvTime(qualifyingClassLevel+trapBonus);
		final Trap T=(Trap)copyOf();
		T.setReset(rejuv);
		T.setInvoker(mob);
		T.setSavable(false);
		T.setAbilityCode(trapBonus);
		P.addEffect(T);
		if(perm)
		{
			T.setSavable(true);
			T.makeNonUninvokable();
		}
		else
		if(!isABomb())
			CMLib.threads().startTickDown(T,Tickable.TICKID_TRAP_DESTRUCTION,baseDestructTime(qualifyingClassLevel+trapBonus));
		return T;
	}

	@Override
	public void setInvoker(MOB mob)
	{
		if(mob!=null)
			invokerName=mob.Name();
		super.setInvoker(mob);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;

		if(tickID==Tickable.TICKID_TRAP_DESTRUCTION)
		{
			if(canBeUninvoked())
				disable();
			return false;
		}
		else
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			if((--tickDown)<=0)
			{
				if((isABomb())
				&&(affected instanceof Item)
				&&(((Item)affected).owner()!=null))
				{
					final Item I=(Item)affected;
					if(I.owner() instanceof MOB)
						spring((MOB)I.owner());
					else
					if(I.owner() instanceof Room)
					{
						final Room R=(Room)I.owner();
						for(int i=R.numInhabitants()-1;i>=0;i--)
						{
							final MOB M=R.fetchInhabitant(i);
							if(M!=null)
								spring(M);
						}
					}
					disable();
					unInvoke();
					I.destroy();
					return false;
				}
				sprung=false;
				disabled=false;
				return false;
			}
		}
		return true;
	}

	@Override
	public void resetTrap(MOB mob)
	{
		if(sprung())
		{
			sprung=false;
			disabled=false;
			if(!isABomb())
				CMLib.threads().deleteTick(this, Tickable.TICKID_TRAP_RESET);
		}
	}
	
	@Override
	public boolean sprung()
	{
		return sprung && (!disabled());
	}

	@Override
	public void spring(MOB target)
	{
		sprung=true;
		disabled=false;
		tickDown=getReset();
		if(!isABomb())
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
	}

	protected Item findFirstResource(Room room, String other)
	{
		return CMLib.materials().findFirstResource(room, other);
	}

	protected Item findFirstResource(Room room, int resource)
	{
		return CMLib.materials().findFirstResource(room, resource);
	}

	protected Item findMostOfMaterial(Room room, String other)
	{
		return CMLib.materials().findMostOfMaterial(room, other);
	}

	protected Item findMostOfMaterial(Room room, int material)
	{
		return CMLib.materials().findMostOfMaterial(room, material);
	}

	protected void destroyResources(Room room, int resource, int number)
	{
		CMLib.materials().destroyResourcesValue(room,number,resource,-1,null);
	}

	protected int findNumberOfResource(Room room, int resource)
	{
		return CMLib.materials().findNumberOfResource(room, resource);
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=super.getTarget(mob, commands, givenTarget);
		if(target == null)
			return false;
		if(!super.proficiencyCheck(mob, 0, auto))
			return true;
		if(!super.invoke(mob, commands, target, auto, asLevel))
			return false;
		StdTrap T=(StdTrap)copyOf();
		T.setInvoker(mob);
		T.setAffectedOne(mob);
		T.spring(target);
		return true;
	}
}

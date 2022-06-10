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

/*
   Copyright 2004-2022 Bo Zimmerman

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
public class Push extends Go
{
	public Push()
	{
	}

	private final String[]	access	= I(new String[] { "PUSH" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public Quad<Physical,String,Integer,Environmental> getArgs(final MOB mob, final List<String> commands, final boolean quiet)
	{
		final Vector<String> workCmds=new XVector<String>(commands);
		Physical pushThis=null;
		String dir="";
		int dirCode=-1;
		Environmental E=null;
		if(workCmds.size()>1)
		{
			dirCode=CMLib.directions().getGoodDirectionCode(workCmds.get(workCmds.size()-1));
			if(dirCode>=0)
			{
				final Room nextR=mob.location().getRoomInDir(dirCode);
				final Exit nextE=mob.location().getExitInDir(dirCode);
				if((nextR==null)
				||(nextE==null)
				||(!nextE.isOpen()))
				{
					if(CMLib.flags().isFloatingFreely(mob))
						E=mob.location();
					else
						E=null;
				}
				else
					E=nextR;
				dir=" "+CMLib.directions().getDirectionName(dirCode, CMLib.flags().getInDirType(mob));
				workCmds.remove(workCmds.size()-1);
			}
		}
		if(dir.length()==0)
		{
			dirCode=CMLib.directions().getGoodDirectionCode(workCmds.get(workCmds.size()-1));
			if(dirCode>=0)
				pushThis=mob.location().getExitInDir(dirCode);
		}
		final String itemName=CMParms.combine(workCmds,1);
		if(pushThis==null)
			pushThis=mob.location().fetchFromRoomFavorItems(null,itemName);
		if(pushThis==null)
			pushThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemName,Wearable.FILTER_ANY);

		if((pushThis==null)||(!CMLib.flags().canBeSeenBy(pushThis,mob)))
		{
			if(!quiet)
				CMLib.commands().postCommandFail(mob,commands,L("You don't see '@x1' here.",itemName));
			return null;
		}
		if((E==null)
		&&(dirCode>=0))
		{
			if((pushThis instanceof SiegableItem)
			&&(((SiegableItem)pushThis).isInCombat()))
				E=mob.location();
			else
			{
				if(!quiet)
					CMLib.commands().postCommandFail(mob,commands,L("You can't push anything that way."));
				return null;
			}
		}
		if(super.isOccupiedWithOtherWork(mob))
		{
			if(!quiet)
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are too busy to push anything right now."));
			return null;
		}
		if(mob.isInCombat() && (!(pushThis instanceof MOB)))
		{
			if(!quiet)
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are too busy fighting right now."));
			return null;
		}
		return new Quad<Physical,String,Integer,Environmental>(pushThis, dir, Integer.valueOf(dirCode), E);
	}

	@Override
	public boolean preExecute(final MOB mob, final List<String> commands, final int metaFlags, final int secondsElapsed, final double actionsRemaining)
	throws java.io.IOException
	{
		final Quad<Physical,String,Integer,Environmental> args=getArgs(mob,commands,false);
		if(args == null)
			return false;

		final boolean stop = secondsElapsed == -1;
		if(stop)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> stop(s) pushing @x1.",args.first.name(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.clearCommandQueue();
			}
			return false;
		}
		final int malmask=(args.first instanceof MOB)?CMMsg.MASK_MALICIOUS:0;
		if(secondsElapsed==0)
		{
			final String msgStr = "<S-NAME> start(s) pushing <T-NAME>"+args.second+".";
			final CMMsg msg=CMClass.getMsg(mob,args.first,args.fourth,CMMsg.MSG_PUSH|malmask,msgStr);
			msg.setValue(args.third.intValue());
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		else
		if((secondsElapsed % 8)==0)
		{
			final String msgStr = "<S-NAME> continue(s) pushing <T-NAME>"+args.second+".";
			final CMMsg msg=CMClass.getMsg(mob,args.first,args.fourth,CMMsg.MSG_PUSH|malmask,msgStr);
			msg.setValue(args.third.intValue());
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		return true;
	}

	protected Pair<Integer,Integer> getClearHelpers(final MOB mob, final Physical targetObj)
	{
		final Room R=(mob==null)?null:mob.location();
		if(R==null)
			return null;
		int totalHelpers = 0;
		int maxCarry = 0;
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(M!=null)
			{
				final Pair<Object, List<String>> qm = M.getTopCommand();
				if((qm != null)
				&&(qm.first == this))
				{
					final Quad<Physical,String,Integer,Environmental> args=getArgs(M, qm.second, true);
					if(args != null)
					{
						if(args.first == targetObj)
						{
							totalHelpers++;
							maxCarry += M.maxCarry();
							M.clearCommandQueue();
						}
					}
				}
			}
		}
		return new Pair<Integer,Integer>(Integer.valueOf(totalHelpers), Integer.valueOf(maxCarry));
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Quad<Physical,String,Integer,Environmental> args=getArgs(mob,commands,false);
		if(args == null)
			return false;

		final Physical pushThis=args.first;
		final String dir=args.second;
		int dirCode=args.third.intValue();
		final Environmental E=args.fourth;

		final int malmask=(pushThis instanceof MOB)?CMMsg.MASK_MALICIOUS:0;
		final String msgStr = "<S-NAME> push(es) <T-NAME>"+dir+".";
		final CMMsg msg=CMClass.getMsg(mob,pushThis,E,CMMsg.MSG_PUSH|malmask,msgStr);
		msg.setValue(dirCode);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if((dir.length()>0)
			&&(msg.tool() instanceof Room)
			&&(msg.tool()!=mob.location()))
			{
				final Room R=(Room)msg.tool();
				if(R.okMessage(mob,msg))
				{
					dirCode=CMLib.tracking().findRoomDir(mob,R);
					if(dirCode>=0)
					{
						if(msg.othersMessage().equals(msgStr))
							msg.setOthersMessage("<S-NAME> push(es) <T-NAME> into here.");
						final Pair<Integer,Integer> helpers = getClearHelpers(mob, pushThis);
						final int movesRequired = pushThis.phyStats().movesReqToPush() - ((mob.maxCarry()+helpers.second.intValue()) / 3);
						if(movesRequired > 0)
							mob.curState().adjMovement(-movesRequired, mob.maxState());
						int expense = Math.round(CMath.sqrt(pushThis.phyStats().weight()/(1+helpers.first.intValue())));
						if(expense < CMProps.getIntVar(CMProps.Int.RUNCOST))
							expense = CMProps.getIntVar(CMProps.Int.RUNCOST);
						for(int i=0;i<expense;i++)
							CMLib.combat().expendEnergy(mob,true);
						if(mob.curState().getMovement()<=0)
						{
							mob.location().show(mob, pushThis, CMMsg.MSG_OK_ACTION, L("<S-NAME> fail(s) to push <T-NAME> due to <T-HIS-HER> weight."));
							return false;
						}
						R.sendOthers(mob,msg);
						if(pushThis instanceof Item)
							R.moveItemTo((Item)pushThis,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
						else
						if(pushThis instanceof MOB)
							CMLib.tracking().walk((MOB)pushThis,dirCode,((MOB)pushThis).isInCombat(),false,true,true);
					}
				}
			}

		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		final double cost=CMProps.getCommandCombatActionCost(ID());
		if(mob == null)
			return cost;
		return cost + mob.actions() + mob.phyStats().speed();
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		final double cost = CMProps.getCommandActionCost(ID());
		if(mob == null)
			return cost;
		return cost + mob.actions() + mob.phyStats().speed();
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean canBeCancelled()
	{
		return true;
	}

}

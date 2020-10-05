package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/*
   Copyright 2004-2020 Bo Zimmerman

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
public class Prayer_Adoption extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Adoption";
	}

	private final static String	localizedName	= CMLib.lang().L("Adoption");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY | Ability.FLAG_UNHOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(commands.size()<2)
		{
			mob.tell(L("Who is adopting whom?"));
			return false;
		}
		final List<String> origCommands = new XVector<String>(commands);
		String confirmChildSessID = null;
		String confirmParentSessID = null;
		for(int i=commands.size()-1;i>=0;i--)
		{
			if(commands.get(i).startsWith("CHILD_CONFIRM_"))
			{
				confirmChildSessID = commands.get(i).substring(14);
				commands.remove(i);
			}
			else
			if(commands.get(i).startsWith("PARENT_CONFIRM_"))
			{
				confirmParentSessID = commands.get(i).substring(15);
				commands.remove(i);
			}
		}
		final String name2=commands.get(commands.size()-1);
		final String name1=CMParms.combine(commands,0,commands.size()-1);
		final MOB parent=R.fetchInhabitant(name1);
		if((parent==null)||(!CMLib.flags().canBeSeenBy(mob,parent)))
		{
			mob.tell(L("You don't see @x1 here!",name1));
			return false;
		}
		final MOB child=R.fetchInhabitant(name2);

		if((child==null)||(!CMLib.flags().canBeSeenBy(mob,child)))
		{
			mob.tell(L("You don't see @x1 here!",name2));
			return false;
		}
		if(child == parent)
		{
			mob.tell(L("@x1 cannot be adopted by @x2!",child.Name(),parent.Name()));
		}
		if((child.isMonster())
		||(child.playerStats()==null)
		||(child.session()==null))
		{
			mob.tell(L("@x1 must be a player to be adopted.",child.name()));
			return false;
		}
		if((parent.isMonster())
		||(parent.playerStats()==null)
		||(parent.session()==null))
		{
			mob.tell(L("@x1 must be a player to adopt someone.",parent.name()));
			return false;
		}

		if((child.session().isWaitingForInput())
		||(parent.session().isWaitingForInput()))
		{
			mob.tell(L("You'll need to try later."));
			return false;
		}

		final Tattoo tattChk=child.findTattoo("PARENT:*");
		if(tattChk!=null)
		{
			mob.tell(L("@x1 already has parents.",child.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final Prayer_Adoption preMe=this;
			final MOB readyParent=parent;
			final MOB readyChild=child;
			final InputCallback[] parentConfirm=new InputCallback[1];
			final InputCallback[] childConfirm=new InputCallback[1];
			childConfirm[0] = new InputCallback(InputCallback.Type.CONFIRM,"N",0)
			{
				final Prayer_Adoption meA=preMe;
				final MOB parent=readyParent;
				final MOB child=readyChild;
				final MOB M=mob;
				final List<String> cmds=origCommands;
				final Physical givenTargetM=givenTarget;
				final boolean givenAuto=auto;
				final int givenLevel=asLevel;

				@Override
				public void showPrompt()
				{
					final Session S=child.session();
					S.promptPrint(L("@x1 wants to adopt you.  Is this OK (y/N)?",parent.name()));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					final Session S=child.session();
					if(this.input.equals("Y"))
					{
						cmds.add("CHILD_CONFIRM_"+S);
						String confirmParentSessID = null;
						for(int i=cmds.size()-1;i>=0;i--)
						{
							if(cmds.get(i).startsWith("PARENT_CONFIRM_"))
							{
								confirmParentSessID = cmds.get(i).substring(15);
								cmds.remove(i);
							}
						}
						if((parent!=M)
						&&(parent.session()!=null)
						&&(!parent.session().isStopped())
						&&(confirmParentSessID==null)
						&&(!parent.session().isWaitingForInput()))
							parent.session().prompt(parentConfirm[0]);
						else
						{
							CMLib.threads().executeRunnable(new Runnable() {

								@Override
								public void run()
								{
									meA.invoke(M, cmds, givenTargetM, givenAuto, givenLevel);
								}
							});
						}
					}
					S.setPromptFlag(true);
				}
			};
			parentConfirm[0] = new InputCallback(InputCallback.Type.CONFIRM,"N",0)
			{
				final Prayer_Adoption meA=preMe;
				final MOB parent=readyParent;
				final MOB child=readyChild;
				final MOB M=mob;
				final List<String> cmds=origCommands;
				final Physical givenTargetM=givenTarget;
				final boolean givenAuto=auto;
				final int givenLevel=asLevel;

				@Override
				public void showPrompt()
				{
					final Session S=parent.session();
					S.promptPrint(L("@x1 wants you to adopt @x2.  Is this OK (y/N)?",child.name(),child.charStats().himher()));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					final Session S=parent.session();
					if(this.input.equals("Y"))
					{
						cmds.add("PARENT_CONFIRM_"+S);
						String confirmChildSessID = null;
						for(int i=cmds.size()-1;i>=0;i--)
						{
							if(cmds.get(i).startsWith("CHILD_CONFIRM_"))
								confirmChildSessID = cmds.get(i).substring(14);
						}
						if((child!=M)
						&&(child.session()!=null)
						&&(!child.session().isStopped())
						&&(confirmChildSessID==null)
						&&(!child.session().isWaitingForInput()))
							child.session().prompt(childConfirm[0]);
						else
						{
							CMLib.threads().executeRunnable(new Runnable() {

								@Override
								public void run()
								{
									meA.invoke(M, cmds, givenTargetM, givenAuto, givenLevel);
								}
							});
						}
					}
					S.setPromptFlag(true);
				}
			};
			if((child!=mob)
			&&(child.session()!=null)
			&&(!child.session().isStopped())
			&&(confirmChildSessID==null)
			&&(!child.session().isWaitingForInput()))
			{
				child.session().prompt(childConfirm[0]);
				mob.tell(L("\n\rYour prayer seeks consent before invoking..."));
				return false;
			}
			else
			if((parent!=mob)
			&&(parent.session()!=null)
			&&(!parent.session().isStopped())
			&&(confirmParentSessID==null)
			&&(!parent.session().isWaitingForInput()))
			{
				mob.tell(L("\n\rYour prayer seeks consent before invoking..."));
				parent.session().prompt(parentConfirm[0]);
				return false;
			}
		}
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> @x1 to bless the adoption of @x3 by @x2.^?",prayForWord(mob),parent.name(),child.name()));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				String sondat;
				switch(child.charStats().getStat(CharStats.STAT_GENDER))
				{
				case 'M':
					sondat="son";
					break;
				case 'F':
					sondat="daughter";
					break;
				default:
					sondat="child";
					break;
				}
				String descAddOn = "@x1 is the adopted "+sondat+" of @x2";
				child.addTattoo("PARENT:"+parent.Name());
				if(parent.isMarriedToLiege() && (parent.getLiegeID().length()>0))
				{
					child.addTattoo("PARENT:"+parent.getLiegeID());
					descAddOn +=" and @x3";
				}
				descAddOn += ".";
				child.setDescription(child.description()+"  "+L(descAddOn,child.Name(),parent.Name(),parent.getLiegeID()));
			 }
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> can not bless this adoption."));

		return success;
	}
}

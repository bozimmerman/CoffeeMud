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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
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
public class Thief_PetSteal extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_PetSteal";
	}

	private final static String	localizedName	= CMLib.lang().L("Pet Steal");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return affected != invoker;
	}

	protected volatile Item stolen = null;
	protected boolean addLimbs = false;

	@Override
	public void affectCharStats(MOB affectedOne, CharStats affectableStats)
	{
		super.affectCharStats(affectedOne, affectableStats);
		if((affected != invoker)&&(addLimbs))
		{
			affectableStats.alterBodypart(Race.BODY_ARM,1);
			affectableStats.alterBodypart(Race.BODY_HAND,1);
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(affected != invoker)
		&&(text().length()>0))
		{
			final MOB boss=invoker;
			final MOB M=(MOB)affected;
			final Room R=M.location();
			if((R != null)&&(M.amFollowing()!=null)&&(boss!=null))
			{
				final Item stolen=this.stolen;
				if((M.amFollowing().location()==R)
				&&(stolen!=null))
				{
					if(M.isMine(stolen))
					{
						this.addLimbs=true;
						M.recoverCharStats();
						try
						{
							if(CMLib.commands().postGive(M, M.amFollowing(), stolen, false))
							{
								this.stolen=null;
								unInvoke();
								return false;
							}
							else
							if(CMLib.commands().postDrop(M, stolen, false, false, false))
							{
								this.stolen=null;
								unInvoke();
								return false;
							}
						}
						finally
						{
							this.addLimbs=false;
							M.recoverCharStats();
						}
					}
					else
						this.stolen=null;
				}
				else
				{
					Item I=R.findItem(text());
					if((I != null)
					&&(CMLib.flags().canBeSeenBy(I, M)
					&&((I.ultimateContainer(null)==null))||(CMLib.flags().canBeSeenBy(I.ultimateContainer(null), M))))
					{
						this.addLimbs=true;
						M.recoverCharStats();
						try
						{
							CMLib.commands().postGet(M, I.container(), I, false);
							if(M.isMine(I))
								this.stolen = I;
						}
						finally
						{
							this.addLimbs=false;
							M.recoverCharStats();
						}
					}
					else
					if(R.numInhabitants()>1)
					{
						final Set<MOB> groupMembers = M.getGroupMembers(new HashSet<MOB>());
						for(final Enumeration<MOB> r=R.inhabitants();r.hasMoreElements();)
						{
							final MOB M2=r.nextElement();
							if((M2!=null)
							&&(!groupMembers.contains(M2))
							&&(M.mayIFight(M2))
							&&(boss.mayIFight(M2))
							&&(M.phyStats().level()>=(M2.phyStats().level()-3))
							&&(CMLib.flags().canBeSeenBy(M2, M)))
							{
								I=M2.findItem(null, text());
								if((I!=null)
								&&(!(I instanceof Coins))
								&&(CMLib.flags().canBeSeenBy(I, M)))
								{
									this.addLimbs=true;
									M.recoverCharStats();
									try
									{
										Ability A=CMClass.getAbility("Thief_Steal");
										if(A != null)
										{
											A.setProficiency(proficiency());
											final List<String> cmds=new XVector<String>(text(),M2.Name());
											A.invoke(M, cmds, M2, false, boss.phyStats().level());
											if(M.isMine(I))
												this.stolen = I;
										}
									}
									finally
									{
										this.addLimbs=false;
										M.recoverCharStats();
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amISource(invoker)
		&&(invoker == affected)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amFollowing()==msg.source())
		&&(((MOB)msg.target()).fetchEffect("Prop_Familiar")!=null)
		&&((msg.sourceMajor()&CMMsg.MASK_MAGIC)==0))
		{
			final MOB mob=msg.source();
			final List<String> commands = CMParms.parseSpaces(CMStrings.getSayFromMessage(msg.sourceMessage()),true);
			if(commands.size()==0)
				return;
			if(!commands.get(0).equalsIgnoreCase("STEAL"))
				return;
			commands.remove(0);
			final Room R=mob.location();
			if(R==null)
				return;
			MOB target=(MOB)msg.target();
			if(commands.size()<1)
			{
				R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) at <T-NAME> expectantly."));
				return;
			}
			
			final Ability oldSteal = target.fetchEffect(ID());
			if(oldSteal != null)
			{
				
				target.delEffect(oldSteal);
			}
			
			final String stealWhat = CMParms.combine(commands);
			if(stealWhat.equalsIgnoreCase("NOTHING")||stealWhat.equalsIgnoreCase("STOP"))
			{
				R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> nod(s)."));
				return;
			}
			
			if(!super.proficiencyCheck(mob, 0, false))
			{
				R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> refuse(s)."));
				return;
			}
			
			Thief_PetSteal affect = (Thief_PetSteal)beneficialAffect(mob,target,0,0);
			if(affect != null)
			{
				R.show(target,mob,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> nod(s)."));
				affect.setMiscText(stealWhat);
				affect.makeLongLasting();
			}
		}
	}
}

package com.planet_ink.coffee_mud.Abilities.Archon;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Archon_Multiwatch extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Multiwatch";
	}

	private final static String localizedName = CMLib.lang().L("Multiwatch");

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
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings = I(new String[] { "MULTIWATCH" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	public static Hashtable<MOB, int[]>			DATA				= new Hashtable<MOB, int[]>();
	public static Hashtable<String, List<MOB>>	IPS					= new Hashtable<String, List<MOB>>();

	public static final int DATA_GOODSPEECH=0;
	public static final int DATA_ANYSPEECH=1;
	public static final int DATA_DIRSPEECH=2;
	public static final int DATA_GOODSOCIAL=3;
	public static final int DATA_ANYSOCIAL=4;
	public static final int DATA_DIRSOCIAL=5;
	public static final int DATA_TYPEDCOMMAND=6;
	public static final int DATA_SYNCHROFOUND=7;
	public static final int DATA_ORDER=8;

	public static final int DATA_TOTAL=10;

	public String lastCommand=null;

	public boolean nonIPnonMonsterWithMe(MOB me)
	{
		if((me.location()!=null)&&(me.session()!=null))
		{
			final Room R=me.location();
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M==null)||(M==me))
					continue;

				if((M.session()!=null)&&(M.session().getAddress().equals(me.session().getAddress())))
					return true;
			}
		}
		return false;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			if(!DATA.containsKey(msg.source()))
				DATA.put(msg.source(),new int[DATA_TOTAL]);
			final int[] data=DATA.get(msg.source());

			if(data==null)
				return;
			if(msg.tool() instanceof Social)
			{
				if(nonIPnonMonsterWithMe(msg.source()))
					data[DATA_GOODSOCIAL]++;
				if((msg.target() instanceof MOB)
				&&(!((MOB)msg.target()).isMonster()))
					data[DATA_DIRSOCIAL]++;
				data[DATA_ANYSOCIAL]++;
			}
			else
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_SPEAK:
				if((msg.othersMessage()!=null)
				&&(msg.sourceMessage()!=null)
				&&(msg.othersMinor()==msg.sourceMinor())
				&&(msg.source().location()!=null)
				&&(msg.source().session()!=null))
				{
					if(msg.sourceMessage().indexOf("order(s)")>0)
					{
						if((msg.target() instanceof MOB)
						&&(((MOB)msg.target()).session()!=null)
						&&(((MOB)msg.target()).session().getAddress().equals(msg.source().session().getAddress())))
							data[DATA_ORDER]++;
					}
					else
					{
						if(nonIPnonMonsterWithMe(msg.source()))
							data[DATA_GOODSPEECH]++;
						if((msg.target() instanceof MOB)
						&&(!((MOB)msg.target()).isMonster()))
							data[DATA_DIRSPEECH]++;
						data[DATA_ANYSPEECH]++;
					}
				}
				break;
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if(!DATA.containsKey(mob))
				DATA.put(mob,new int[DATA_TOTAL]);
			final int[] data=DATA.get(mob);
			if((mob.session()!=null)&&(mob.session().getPreviousCMD()!=null))
			{
				if((lastCommand!=null)
				&&(!CMParms.combine(mob.session().getPreviousCMD(),0).equals(lastCommand)))
				{
					data[DATA_TYPEDCOMMAND]++;
					List<MOB> V=null;
					if(mob.session().getAddress()!=null)
						V=IPS.get(mob.session().getAddress());

					if(V!=null)
					for(int v=0;v<V.size();v++)
					{
						final MOB M=V.get(v);
						if(M==mob)
							continue;
						if(M.session()==null)
							continue;
						if(!CMLib.flags().isInTheGame(M,true))
							continue;
						final String hisLastCmd=CMParms.combine(mob.session().getPreviousCMD(),0);
						final Archon_Multiwatch A=(Archon_Multiwatch)M.fetchEffect(ID());
						if(A!=null)
						{
							if((A.lastCommand!=null)&&(!A.lastCommand.equals(hisLastCmd)))
								data[DATA_SYNCHROFOUND]++;
							break;
						}
					}
				}
				lastCommand=CMParms.combine(mob.session().getPreviousCMD(),0);
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(CMParms.combine(commands,0).equalsIgnoreCase("auto"))
		{
			DATA.clear();
			IPS.clear();
			final Hashtable<String,List<MOB>> ipes=new Hashtable<String,List<MOB>>();
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if((S.getAddress().length()>0)
				&&(S.mob()!=null))
				{
					List<MOB> V=ipes.get(S.getAddress());
					if(V==null)
					{
						V=new Vector<MOB>();
						ipes.put(S.getAddress(),V);
					}
					if(!V.contains(S.mob()))
						V.add(S.mob());
				}
			}
			final StringBuffer rpt=new StringBuffer("");
			for(final Enumeration<String> e=ipes.keys();e.hasMoreElements();)
			{
				final String addr=e.nextElement();
				final List<MOB> names=ipes.get(addr);
				if(names.size()>1)
				{
					IPS.put(addr,names);
					rpt.append("Watch #"+(IPS.size())+" added: ");
					for(int n=0;n<names.size();n++)
					{
						final MOB MN=names.get(n);
						if(MN.fetchEffect(ID())==null)
						{
							final Ability A=(Ability)copyOf();
							MN.addNonUninvokableEffect(A);
							A.setSavable(false);
						}
						rpt.append(MN.Name()+" ");
					}
					rpt.append("\n\r");
				}
			}
			if(rpt.length()==0)
				rpt.append("No users with duplicate IDs found.  Try MULTIWATCH ADD name1 name2 ... ");
			mob.tell(rpt.toString());
			return true;
		}
		else
		if(CMParms.combine(commands,0).equalsIgnoreCase("stop"))
		{
			boolean foundLegacy=false;
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if((S!=null)&&(S.mob()!=null)&&(S.mob().fetchEffect(ID())!=null))
				{
					foundLegacy=true;
					break;
				}
			}
			if((DATA.size()==0)&&(IPS.size()==0)&&(!foundLegacy))
			{
				mob.tell(L("Multiwatch is already off."));
				return false;
			}
			for(final Enumeration<List<MOB>> e=IPS.elements();e.hasMoreElements();)
			{
				final List<MOB> V=e.nextElement();
				for(int v=0;v<V.size();v++)
				{
					final MOB M=V.get(v);
					final Ability A=M.fetchEffect(ID());
					if(A!=null)
						M.delEffect(A);
				}
			}
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if((S!=null)&&(S.mob()!=null))
				{
					final MOB M=S.mob();
					final Ability A=M.fetchEffect(ID());
					if(A!=null)
						M.delEffect(A);
				}
			}
			mob.tell(L("Multiplay watcher is now turned off."));
			DATA.clear();
			IPS.clear();
			return true;
		}
		else
		if((commands.size()>1)&&(commands.get(0)).equalsIgnoreCase("add"))
		{
			final Vector<MOB> V=new Vector<MOB>();
			for(int i=1;i<commands.size();i++)
			{
				final String name=commands.get(i);
				final MOB M=CMLib.players().getPlayer(name);
				if((M.session()!=null)&&(CMLib.flags().isInTheGame(M,true)))
					V.addElement(M);
				else
					mob.tell(L("'@x1' is not online.",name));
			}
			if(V.size()>1)
			{
				for(int n=0;n<V.size();n++)
				{
					final MOB MN=V.elementAt(n);
					if(MN.fetchEffect(ID())==null)
					{
						final Ability A=(Ability)copyOf();
						MN.addNonUninvokableEffect(A);
						A.setSavable(false);
					}
				}
				IPS.put("MANUAL"+(IPS.size()+1),V);
				mob.tell(L("Manual Watch #@x1 added.",""+IPS.size()));
			}
			return true;
		}
		else
		if((commands.size()==0)&&(DATA.size()>0)&&(IPS.size()>0))
		{
			final StringBuffer report=new StringBuffer("");
			for(final Enumeration<String> e=IPS.keys();e.hasMoreElements();)
			{
				final String key=e.nextElement();
				int sync=0;
				final List<MOB> V=IPS.get(key);
				for(int v=0;v<V.size();v++)
				{
					final MOB M=V.get(v);
					final int data[]=DATA.get(M);
					if(data!=null)
						sync+=data[DATA_SYNCHROFOUND];
				}
				report.append("^x"+key+"^?^., Syncs: "+sync+"\n\r");
				report.append(CMStrings.padRight(L("Name"),25)
							 +CMStrings.padRight(L("Speech"),15)
							 +CMStrings.padRight(L("Socials"),15)
							 +CMStrings.padRight(L("CMD"),10)
							 +CMStrings.padRight(L("ORDERS"),10)
							 +"\n\r");
				for(int v=0;v<V.size();v++)
				{
					final MOB M=V.get(v);
					int data[]=DATA.get(M);
					if(data==null)
						data=new int[DATA_TOTAL];
					report.append(CMStrings.padRight(M.Name(),25));
					report.append(CMStrings.padRight(data[DATA_GOODSPEECH]
												+"/"+data[DATA_DIRSPEECH]
												+"/"+data[DATA_ANYSPEECH],15));
					report.append(CMStrings.padRight(data[DATA_GOODSOCIAL]
												+"/"+data[DATA_DIRSOCIAL]
												+"/"+data[DATA_ANYSOCIAL],15));
					report.append(CMStrings.padRight(data[DATA_TYPEDCOMMAND]+"",10));
					report.append(CMStrings.padRight(data[DATA_ORDER]+"",10));
					report.append("\n\r");
				}
				report.append("\n\r");
			}

			mob.tell(report.toString());
			return true;
		}
		else
		{
			mob.tell(L("Try MULTIWATCH AUTO, MULTIWATCH STOP, or MULTIWATCH ADD name1 name2.."));
			return false;
		}

	}

}

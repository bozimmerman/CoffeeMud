package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String ID() { return "Archon_Multiwatch"; }
	public String name(){ return "Multiwatch";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"MULTIWATCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public static Hashtable DATA=new Hashtable();
	public static Hashtable IPS=new Hashtable();

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
			Room R=me.location();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if((M==null)||(M==me)) continue;

				if((M.session()!=null)&&(M.session().getAddress().equals(me.session().getAddress())))
					return true;
			}
		}
		return false;
	}


	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected instanceof MOB)&&(msg.amISource((MOB)affected)))
		{
			Room R=msg.source().location();
			if(!DATA.containsKey(msg.source()))
				DATA.put(msg.source(),new int[DATA_TOTAL]);
			int[] data=(int[])DATA.get(msg.source());

			if(data==null) return;
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


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
		&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(!DATA.containsKey(mob))	DATA.put(mob,new int[DATA_TOTAL]);
			int[] data=(int[])DATA.get(mob);
			if((mob.session()!=null)&&(mob.session().previousCMD()!=null))
			{
				if((lastCommand!=null)
				&&(!Util.combine(mob.session().previousCMD(),0).equals(lastCommand)))
				{
					data[DATA_TYPEDCOMMAND]++;
					Vector V=null;
					if(mob.session().getAddress()!=null)
						V=(Vector)IPS.get(mob.session().getAddress());

					if(V!=null)
					for(int v=0;v<V.size();v++)
					{
						MOB M=(MOB)V.elementAt(v);
						if(M==mob) continue;
						if(M.session()==null) continue;
						if(!Sense.isInTheGame(M)) continue;
						String hisLastCmd=Util.combine(mob.session().previousCMD(),0);
						Archon_Multiwatch A=(Archon_Multiwatch)M.fetchEffect(ID());
						if(A!=null)
						{
							if((A.lastCommand!=null)&&(!A.lastCommand.equals(hisLastCmd)))
								data[DATA_SYNCHROFOUND]++;
							break;
						}
					}
				}
				lastCommand=Util.combine(mob.session().previousCMD(),0);
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(Util.combine(commands,0).equalsIgnoreCase("auto"))
		{
			DATA.clear();
			IPS.clear();
			Hashtable ipes=new Hashtable();
			for(int s=0;s<Sessions.size();s++)
			{
				Session S=Sessions.elementAt(s);
				if((S.getAddress().length()>0)
				&&(S.mob()!=null))
				{
					Vector V=(Vector)ipes.get(S.getAddress());
					if(V==null){
						V=new Vector();
						ipes.put(S.getAddress(),V);
					}
					if(!V.contains(S.mob())) V.addElement(S.mob());
				}
			}
			StringBuffer rpt=new StringBuffer("");
			for(Enumeration e=ipes.keys();e.hasMoreElements();)
			{
				String addr=(String)e.nextElement();
				Vector names=(Vector)ipes.get(addr);
				if(names.size()>1)
				{
					IPS.put(addr,names);
					rpt.append("Watch #"+(IPS.size())+" added: ");
					for(int n=0;n<names.size();n++)
					{
						MOB MN=(MOB)names.elementAt(n);
						if(MN.fetchEffect(ID())==null)
						{
							Ability A=(Ability)copyOf();
							A.setBorrowed(MN,true);
							MN.addNonUninvokableEffect(A);
						}
						rpt.append(MN.Name()+" ");
					}
					rpt.append("\n\r");
				}
			}
			if(rpt.length()==0) rpt.append("No users with duplicate IDs found.  Try MULTIWATCH ADD name1 name2 ... ");
			mob.tell(rpt.toString());
			return true;
		}
		else
		if(Util.combine(commands,0).equalsIgnoreCase("stop"))
		{
			if((DATA.size()==0)&&(IPS.size()==0))
			{
				mob.tell("Multiwatch is already off.");
				return false;
			}
			for(Enumeration e=IPS.elements();e.hasMoreElements();)
			{
				Vector V=(Vector)e.nextElement();
				for(int v=0;v<V.size();v++)
				{
					MOB M=(MOB)V.elementAt(v);
					Ability A=M.fetchEffect(ID());
					if(A!=null) M.delEffect(A);
				}
			}
			mob.tell("Multiplay watcher is now turned off.");
			DATA.clear();
			IPS.clear();
			return true;
		}
		else
		if((commands.size()>1)&&((String)commands.firstElement()).equalsIgnoreCase("add"))
		{
			Vector V=new Vector();
			for(int i=1;i<commands.size();i++)
			{
				String name=(String)commands.elementAt(i);
				MOB M=CMMap.getPlayer(name);
				if((M.session()!=null)&&(Sense.isInTheGame(M)))
					V.addElement(M);
				else
					mob.tell("'"+name+"' is not online.");
			}
			if(V.size()>1)
			{
				for(int n=0;n<V.size();n++)
				{
					MOB MN=(MOB)V.elementAt(n);
					if(MN.fetchEffect(ID())==null)
					{
						Ability A=(Ability)copyOf();
						A.setBorrowed(MN,true);
						MN.addNonUninvokableEffect(A);
					}
				}
				IPS.put("MANUAL"+(IPS.size()+1),V);
				mob.tell("Manual Watch #"+IPS.size()+" added.");
			}
			return true;
		}
		else
		if((commands.size()==0)&&(DATA.size()>0)&&(IPS.size()>0))
		{
			StringBuffer report=new StringBuffer("");
			for(Enumeration e=IPS.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				int sync=0;
				Vector V=(Vector)IPS.get(key);
				for(int v=0;v<V.size();v++)
				{
					MOB M=(MOB)V.elementAt(v);
					int data[]=(int[])DATA.get(M);
					if(data!=null) sync+=data[DATA_SYNCHROFOUND];
				}
				report.append("^x"+key+"^?^., Syncs: "+sync+"\n\r");
				report.append(Util.padRight("Name",25)
							  +Util.padRight("Speech",15)
							  +Util.padRight("Socials",15)
							  +Util.padRight("CMD",10)
							  +Util.padRight("ORDERS",10)
							  +"\n\r");
				for(int v=0;v<V.size();v++)
				{
					MOB M=(MOB)V.elementAt(v);
					int data[]=(int[])DATA.get(M);
					if(data==null) data=new int[DATA_TOTAL];
					report.append(Util.padRight(M.Name(),25));
					report.append(Util.padRight(data[DATA_GOODSPEECH]
												+"/"+data[DATA_DIRSPEECH]
												+"/"+data[DATA_ANYSPEECH],15));
					report.append(Util.padRight(data[DATA_GOODSOCIAL]
												+"/"+data[DATA_DIRSOCIAL]
												+"/"+data[DATA_ANYSOCIAL],15));
					report.append(Util.padRight(data[DATA_TYPEDCOMMAND]+"",10));
					report.append(Util.padRight(data[DATA_ORDER]+"",10));
					report.append("\n\r");
				}
				report.append("\n\r");
			}

			mob.tell(report.toString());
			return true;
		}
		else
		{
			mob.tell("Try MULTIWATCH AUTO, MULTIWATCH STOP, or MULTIWATCH ADD name1 name2..");
			return false;
		}

	}

}
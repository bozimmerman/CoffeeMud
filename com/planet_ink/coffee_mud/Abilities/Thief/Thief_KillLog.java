package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Thief_KillLog extends ThiefSkill
{
	public String ID() { return "Thief_KillLog"; }
	public String name(){ return "Kill Log";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private static final String[] triggerStrings = {"KILLLOG"};
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public String[] triggerStrings(){return triggerStrings;}
	Hashtable theList=new Hashtable();
	public MOB mark=null;

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}

	public String text()
	{
		StringBuffer str=new StringBuffer("<MOBS>");
		for(Enumeration e=theList.elements();e.hasMoreElements();)
		{
			String[] one=(String[])e.nextElement();
			str.append("<MOB>");
			str.append(XMLManager.convertXMLtoTag("NAME",one[0]));
			str.append(XMLManager.convertXMLtoTag("LEVEL",one[1]));
			str.append(XMLManager.convertXMLtoTag("TOTAL",one[2]));
			str.append(XMLManager.convertXMLtoTag("KILLS",one[3]));
			str.append("</MOB>");
		}
		str.append("</MOBS>");
		return str.toString();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((mark!=null)
		&&msg.amISource(mark)
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			String[] set=(String[])theList.get(mark.Name());
			if(set==null)
			{
				set=new String[4];
				set[0]=mark.Name();
				set[2]="1";
				set[3]="0";
				theList.put(mark.Name(),set);
			}
			set[1]=""+mark.envStats().level();
			set[3]=new Integer(Util.s_int(set[3])+1).toString();
			mark=null;
			if((affected!=null)&&(affected instanceof MOB))
			{
				Ability A=((MOB)affected).fetchAbility(ID());
				if(A!=null)	A.setMiscText(text());
			}
		}
		super.executeMsg(myHost,msg);
	}

	public void setMiscText(String str)
	{
		theList.clear();
		if((str.trim().length()>0)&&(str.trim().startsWith("<MOBS>")))
		{
			Vector buf=XMLManager.parseAllXML(str);
			Vector V=XMLManager.getRealContentsFromPieces(buf,"MOBS");
			if(V!=null)
			for(int i=0;i<V.size();i++)
			{
				XMLManager.XMLpiece ablk=(XMLManager.XMLpiece)V.elementAt(i);
				if(ablk.tag.equalsIgnoreCase("MOB"))
				{
					String[] one=new String[4];
					one[0]=XMLManager.getValFromPieces(ablk.contents,"NAME");
					one[1]=XMLManager.getValFromPieces(ablk.contents,"LEVEL");
					one[2]=XMLManager.getValFromPieces(ablk.contents,"TOTAL");
					one[3]=XMLManager.getValFromPieces(ablk.contents,"KILLS");
					theList.put(one[0],one);
				}
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		MOB mob=(MOB)affected;
		MOB m=getMark(mob);
		if(m!=mark)
		{
			mark=m;
			if(mark!=null)
			{
				String[] set=(String[])theList.get(mark.Name());
				if(set==null)
				{
					set=new String[4];
					set[0]=mark.Name();
					set[2]="0";
					set[3]="0";
					theList.put(mark.Name(),set);
				}
				set[1]=""+mark.envStats().level();
				set[2]=new Integer(Util.s_int(set[2])+1).toString();
				if((affected!=null)&&(affected instanceof MOB))
				{
					Ability A=((MOB)affected).fetchAbility(ID());
					if(A!=null)	A.setMiscText(text());
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(profficiencyCheck(mob,0,auto))
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("Name",20)+Util.padRight("Level",6)+"Kill Pct.\n\r");
			Vector order=new Vector();
			int lowLevel=Integer.MIN_VALUE;
			String[] addOne=null;
			while(theList.size()>order.size())
			{
				addOne=null;
				lowLevel=Integer.MIN_VALUE;
				for(Enumeration e=theList.elements();e.hasMoreElements();)
				{
					String[] one=(String[])e.nextElement();
					if((Util.s_int(one[1])>=lowLevel)
					&&(!order.contains(one)))
					{
						lowLevel=Util.s_int(one[1]);
						addOne=one;
					}
				}
				if(addOne==null)
					break;
				else
					order.addElement(addOne);
			}
			for(int i=0;i<order.size();i++)
			{
				String[] one=(String[])order.elementAt(i);
				int pct=0;
				int total=Util.s_int(one[2]);
				int kills=Util.s_int(one[3]);
				if(total>0)
					pct=(int)Math.round((Util.div(kills,total)*100.0));
				str.append(Util.padRight(one[0],20)+Util.padRight(one[1],6)+pct+"%\n\r");
			}
			if(mob.session()!=null)
				mob.session().rawPrintln(str.toString());
			return true;
		}
		else
		{
			mob.tell("You failed to recall your log.");
			return false;
		}

	}
}

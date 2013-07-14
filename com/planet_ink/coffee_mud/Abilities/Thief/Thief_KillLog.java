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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Thief_KillLog extends ThiefSkill
{
	public String ID() { return "Thief_KillLog"; }
	public String name(){ return "Kill Log";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	private static final String[] triggerStrings = {"KILLLOG"};
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_COMBATLORE;}
	protected Map<String,String[]> theList=new Hashtable<String,String[]>();
	protected Thief_Mark lastMarker=null;
	public MOB mark=null;

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=null;
		if((lastMarker != null)&&(lastMarker.affecting()==mob)&&(!lastMarker.amDestroyed())&&(lastMarker.mark!=null))
			A=lastMarker;
		if(A==null)
		{
			A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
			lastMarker=A;
		}
		if(A!=null)
			return A.mark;
		return null;
	}

	public String text()
	{
		StringBuffer str=new StringBuffer("<MOBS>");
		for(String[] one : theList.values())
		{
			str.append("<MOB>");
			str.append(CMLib.xml().convertXMLtoTag("NAME",one[0]));
			str.append(CMLib.xml().convertXMLtoTag("LEVEL",one[1]));
			str.append(CMLib.xml().convertXMLtoTag("TOTAL",one[2]));
			str.append(CMLib.xml().convertXMLtoTag("KILLS",one[3]));
			str.append("</MOB>");
		}
		str.append("</MOBS>");
		return str.toString();
	}
	
	public CMObject copyOf()
	{
		Thief_KillLog obj=(Thief_KillLog)super.copyOf();
		obj.theList=new Hashtable<String,String[]>();
		obj.theList.putAll(theList);
		return obj;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((mark!=null)
		&&msg.amISource(mark)
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			String[] set=theList.get(mark.Name());
			if(set==null)
			{
				set=new String[4];
				set[0]=mark.Name();
				set[2]="1";
				set[3]="0";
				theList.put(mark.Name(),set);
				MOB mob=(MOB)affected;
				mob.tell("Ah, a new one for your kill log.");
				CMLib.leveler().postExperience(mob,null,null,mark.phyStats().level(),false);
			}
			set[1]=""+mark.phyStats().level();
			set[3]=Integer.toString(CMath.s_int(set[3])+1);
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
			List<XMLLibrary.XMLpiece> buf=CMLib.xml().parseAllXML(str);
			List<XMLLibrary.XMLpiece> V=CMLib.xml().getContentsFromPieces(buf,"MOBS");
			if(V!=null)
			for(int i=0;i<V.size();i++)
			{
				XMLLibrary.XMLpiece ablk=V.get(i);
				if(ablk.tag.equalsIgnoreCase("MOB"))
				{
					String[] one=new String[4];
					one[0]=CMLib.xml().getValFromPieces(ablk.contents,"NAME");
					one[1]=CMLib.xml().getValFromPieces(ablk.contents,"LEVEL");
					one[2]=CMLib.xml().getValFromPieces(ablk.contents,"TOTAL");
					one[3]=CMLib.xml().getValFromPieces(ablk.contents,"KILLS");
					theList.put(one[0],one);
				}
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);
		MOB mob=(MOB)affected;
		MOB m=getMark(mob);
		if(m!=mark)
		{
			mark=m;
			if(mark!=null)
			{
				String[] set=theList.get(mark.Name());
				if(set==null)
				{
					set=new String[4];
					set[0]=mark.Name();
					set[2]="0";
					set[3]="0";
					theList.put(mark.Name(),set);
				}
				set[1]=""+mark.phyStats().level();
				set[2]=Integer.toString(CMath.s_int(set[2])+1);
				if((affected!=null)&&(affected instanceof MOB))
				{
					Ability A=((MOB)affected).fetchAbility(ID());
					if(A!=null)	A.setMiscText(text());
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
		{
			StringBuffer str=new StringBuffer("");
			int[] cols={
					ListingLibrary.ColFixer.fixColWidth(20,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(6,mob.session())
				};
			str.append(CMStrings.padRight("Name",cols[0])+CMStrings.padRight("Level",cols[1])+"Kill Pct.\n\r");
			Vector<String[]> order=new Vector<String[]>();
			int lowLevel=Integer.MIN_VALUE;
			String[] addOne=null;
			while(theList.size()>order.size())
			{
				addOne=null;
				lowLevel=Integer.MIN_VALUE;
				for(String[] one : theList.values())
				{
					if((CMath.s_int(one[1])>=lowLevel)
					&&(!order.contains(one)))
					{
						lowLevel=CMath.s_int(one[1]);
						addOne=one;
					}
				}
				if(addOne==null)
					break;
				order.addElement(addOne);
			}
			for(int i=0;i<order.size();i++)
			{
				String[] one=order.elementAt(i);
				int pct=0;
				int total=CMath.s_int(one[2]);
				int kills=CMath.s_int(one[3]);
				if(total>0)
					pct=(int)Math.round((CMath.div(kills,total)*100.0));
				str.append(CMStrings.padRight(one[0],cols[0])+CMStrings.padRight(one[1],cols[1])+pct+"%\n\r");
			}
			if(mob.session()!=null)
				mob.session().rawPrintln(str.toString());
			return true;
		}
		mob.tell("You failed to recall your log.");
		return false;
	}
}

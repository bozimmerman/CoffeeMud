package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_TrophyCount extends ThiefSkill
{
	public String ID() { return "Thief_TrophyCount"; }
	public String name(){ return "Trophy Count";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Thief_TrophyCount();}
	private static final String[] triggerStrings = {"TROPHYCOUNT"};
	public String[] triggerStrings(){return triggerStrings;}
	Hashtable theList=new Hashtable();

	public String text()
	{
		StringBuffer str=new StringBuffer("<MOBS>");
		for(Enumeration e=theList.elements();e.hasMoreElements();)
		{
			String[] one=(String[])e.nextElement();
			str.append("<MOB>");
			str.append(XMLManager.convertXMLtoTag("RACE",one[0]));
			str.append(XMLManager.convertXMLtoTag("KILLS",one[1]));
			str.append("</MOB>");
		}
		str.append("</MOBS>");
		return str.toString();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(msg.tool()!=null)
		&&(msg.tool()==affected))
		{
			Race R=msg.source().charStats().getMyRace();
			if(!R.ID().equalsIgnoreCase("StdRace"))
			{
				String[] set=(String[])theList.get(R.name());
				if(set==null)
				{
					set=new String[4];
					set[0]=R.name();
					set[1]="0";
					theList.put(R.name(),set);
				}
				set[1]=new Integer(Util.s_int(set[1])+1).toString();
				if((affected!=null)&&(affected instanceof MOB))
				{
					Ability A=((MOB)affected).fetchAbility(ID());
					if(A!=null)	A.setMiscText(text());
				}
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
					one[0]=XMLManager.getValFromPieces(ablk.contents,"RACE");
					one[1]=XMLManager.getValFromPieces(ablk.contents,"KILLS");
					theList.put(one[0],one);
				}
			}
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(profficiencyCheck(0,auto))
		{
			StringBuffer str=new StringBuffer("");
			str.append(Util.padRight("Name",20)+"Kills\n\r");
			for(Enumeration e=theList.elements();e.hasMoreElements();)
			{
				String[] one=(String[])e.nextElement();
				int kills=Util.s_int(one[1]);
				str.append(Util.padRight(one[0],20)+kills+"\n\r");
			}
			if(mob.session()!=null)
				mob.session().rawPrintln(str.toString());
			return true;
		}
		else
		{
			mob.tell("You failed to recall your count.");
			return false;
		}

	}
}

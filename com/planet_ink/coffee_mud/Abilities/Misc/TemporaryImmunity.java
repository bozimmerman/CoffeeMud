package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class TemporaryImmunity extends StdAbility
{
	public String ID() { return "TemporaryImmunity"; }
	public String name(){ return "Temporary Immunity";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public Environmental newInstance(){	return new TemporaryImmunity();}
	public int classificationCode(){return Ability.SKILL;}
	public boolean canBeUninvoked(){return true;}
	public boolean isAutoInvoked(){return true;}
	public final static long IMMUNITY_TIME=MudHost.TIME_MILIS_PER_MUDHOUR*30;
	private int tickDown=10;
	private DVector set=new DVector(2);
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==MudHost.TICK_MOB)
		&&((--tickDown)==0))
		{
			tickDown=10;
			makeLongLasting();
			for(int s=set.size()-1;s>=0;s--)
			{
				Long L=(Long)set.elementAt(s,2);
				if((System.currentTimeMillis()-L.longValue())>IMMUNITY_TIME)
					set.removeElementAt(s);
			}
			
			if(set.size()==0){ unInvoke(); return false;}
		}
		return super.tick(ticking,tickID);
	}
	
	public String text()
	{
		if(set.size()==0) return "";
		StringBuffer str=new StringBuffer("");
		for(int s=0;s<set.size();s++)
			str.append(((String)set.elementAt(s,1))+"/"+((Long)set.elementAt(s,2)).longValue()+";");
		return str.toString();
	}

	public void setMiscText(String str)
	{
		if(str.startsWith("+"))
		{
			str=str.substring(1);
			if(set.indexOf(str)>=0)
				set.setElementAt(set.indexOf(str),2,new Long(System.currentTimeMillis()));
			else
				set.addElement(str,new Long(System.currentTimeMillis()));
		}
		else
		{
			set.clear();
			Vector V=Util.parseSemicolons(str,true);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				int x=s.indexOf("/");
				if(x>0)
					set.addElement(s.substring(0,x),new Long(Util.s_long(s.substring(x+1))));
			}
		}
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(!mob.amDead())
		&&(msg.tool() instanceof Ability)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(set.contains(msg.tool().ID())))
		{
			mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+msg.tool().name()+".");
			return false;
		}
		return true;
	}
}
package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BaseWho extends StdCommand
{
	public static StringBuffer showWhoLong(MOB who)
	{

		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().raceName(),7)+" ");
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		msg.append(Util.padRight(who.charStats().displayClassName(),7)+" ");
		msg.append(Util.padRight(levelStr,5));
		msg.append("] "+Util.padRight(who.name(),13)+" ");
		msg.append(Util.padRightPreserve("hp("+Util.padRightPreserve(""+who.curState().getHitPoints(),3)+"/"+Util.padRightPreserve(""+who.maxState().getHitPoints(),3)+")",12));
		msg.append(Util.padRightPreserve("mn("+Util.padRightPreserve(""+who.curState().getMana(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMana(),3)+")",12));
		msg.append(Util.padRightPreserve("mv("+Util.padRightPreserve(""+who.curState().getMovement(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMovement(),3)+")",12));
		msg.append("\n\r");
		return msg;
	}
	public static StringBuffer showWhoShort(MOB who)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().raceName(),12)+" ");
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		msg.append(Util.padRight(who.charStats().displayClassName(),12)+" ");
		msg.append(Util.padRight(levelStr,7));
		String name=who.name();
		if((who.session()!=null)&&(who.session().afkFlag()))
		{
			long t=(who.session().getIdleMillis()/1000);
			String s=t+"s";
			if(t>600)
			{
				t=t/60;
				s=t+"m";
				if(t>120)
				{
					t=t/60;
					s=t+"h";
					if(t>48)
					{
						t=t/24;
						s=t+"d";
					}
				}
			}
			name=name+(" (idle: "+s+")");
		}
		msg.append("] "+Util.padRight(name,35));
		msg.append("\n\r");
		return msg;
	}
}

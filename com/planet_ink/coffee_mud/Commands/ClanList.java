package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.CreateEdit;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class ClanList implements Command
{
  protected Vector accessWords=new Vector();

  public ClanList()
  {
    accessWords.addElement(new String("clanlist"));
  }

	public boolean execute(MOB mob,Vector commands)
	{
    StringBuffer head=new StringBuffer("");
    head.append("[");
    head.append(Util.padRight("Clan Name",24)+" | ");
    head.append(Util.padRight("Clan Members",12)+" | ");
    head.append(Util.padRight("Alignment",16));
    head.append("] \n\r");
    StringBuffer msg=new StringBuffer("");
    for(int c=0;c<Clans.size();c++)
    {
      Clan thisClan=Clans.elementAt(c);
      msg.append(" ");
      msg.append(Util.padRight(thisClan.ID(),24)+"   ");
      msg.append(Util.padRight(new Integer(thisClan.getSize()).toString(),12)+"   ");
      msg.append(Util.padRight(CommonStrings.alignmentStr(thisClan.getAlign()),16));
      msg.append("\n\r");
    }
    mob.tell(head.toString()+msg.toString());
    return false;
	}

  public Vector getAccessWords() {return accessWords;}

}
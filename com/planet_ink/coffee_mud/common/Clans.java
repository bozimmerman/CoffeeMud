package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import com.planet_ink.coffee_mud.Clans.*;
import com.planet_ink.coffee_mud.system.*;
import com.planet_ink.coffee_mud.utils.*;
/**
  * Global utility Vector for holding and creating Clans
  * @author=Jeremy Vyska
  */
public class Clans
{
	private static Vector all=new Vector();

  public static Clan getClan(String name)
  {
    for(int i=0;i<all.size();i++)
    {
      Clan C=(Clan)all.elementAt(i);
      if(C.ID().equalsIgnoreCase(name))
      {
        return C;
      }
    }
    return null;
  }

  public static Clan getClanType(int type)
  {
    switch(type)
    {
      case Clan.TYPE_CLAN:
        return new StdClan();
      default:
        return new StdClan();
    }
  }

  public static String getRoleName(int role, boolean titleCase, boolean plural)
  {
    StringBuffer roleName=new StringBuffer();
    Character c;
    switch(role)
    {
      case Clan.POS_APPLICANT:
        roleName.append("applicant");
        break;
      case Clan.POS_STAFF:
        roleName.append("staff");
        break;
      case Clan.POS_MEMBER:
        roleName.append("member");
        break;
      case Clan.POS_TREASURER:
        roleName.append("treasurer");
        break;
      case Clan.POS_LEADER:
        roleName.append("leader");
        break;
      case Clan.POS_BOSS:
        roleName.append("boss");
        break;
      default:
        roleName.append("member");
        break;
    }
    if(titleCase)
    {
      String titled=Util.capitalize(roleName.toString());
      roleName.setLength(0);
      roleName.append(titled);
    }
    if(plural)
    {
      if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character((new String("f")).charAt(0))))
      {
        // do nothing
      }
      else
      if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character((new String("s")).charAt(0))))
      {
        roleName.append("es");
      }
      else
      {
        roleName.append("s");
      }
    }
    return roleName.toString();
  }

  public static void updateClan(Clan C)
  {
    ClanLoader.DBUpdate(C);
  }

  public static Clan elementAt(int x)
	{
		return (Clan)all.elementAt(x);
	}
	public static int size()
	{
		return all.size();
	}
	public static void addElement(Clan C)
	{
		all.addElement(C);
	}
	public static void removeElementAt(int x)
	{
    removeElement(elementAt(x));
	}
	public static void removeElement(Clan C)
	{
		all.removeElement(C);
	}
}
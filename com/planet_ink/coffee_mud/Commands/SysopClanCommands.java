package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.CreateEdit;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.MOBloader;
import com.planet_ink.coffee_mud.MOBS.StdMOB;

public class SysopClanCommands extends Hashtable implements Command 
{
  protected Vector accessWords=new Vector();
  public static final int REMOVEMEMBER=0;
  public static final int ADDMEMBER=1;
  public static final int SETROLE=2;
  public static final int SETPREMISE=3;
  public static final int CREATE=4;
  public static final int DESTROY=5;
  public static final int SETHOME=6;

  public SysopClanCommands()
  {
    accessWords.addElement(new String("aclan"));
    put("REMOVEMEMBER",new Integer(REMOVEMEMBER));
    put("ADDMEMBER",new Integer(ADDMEMBER));
    put("SETROLE",new Integer(SETROLE));
    put("SETPREMISE",new Integer(SETPREMISE));
    put("CREATE",new Integer(CREATE));
      put("DESTROY",new Integer(DESTROY));
      put("SETHOME",new Integer(SETHOME));
  }

	public boolean execute(MOB mob,Vector commands)
	{
    if(mob.isASysOp(mob.location()))
    {
      if(commands.size()<2)
      {
        mob.tell(getAClanList());
        return false;
      }
      if(commands.size()<3)
      {
        mob.tell("You must also specify a Clan.  Ex:  aclan [command] [Clan name]");
        return false;
      }
      Clan theClan=Clans.getClanType(Clan.TYPE_CLAN);
      boolean found=false;
      for(int j=0;j<Clans.size();j++)
      {
        Clan C=Clans.elementAt(j);
        if(CoffeeUtensils.containsString(C.ID(), (String)commands.elementAt(2)))
        {
          theClan=C;
          found=true;
        }
      }
      String commandWord=(String)commands.elementAt(1);
      commandWord=(String)commandWord.toUpperCase();
      Integer commandCodeObj=(Integer)get(commandWord);
      if(commandCodeObj==null)
      {
        mob.tell("You did not specify a valid aclan function.  Try REMOVEMEMBER, ADDMEMBER, SETROLE, SETPREMISE, CREATE OR DESTROY.");
        return false;
      }
      int commandCode=commandCodeObj.intValue();
      String commandString=new String("");
      if(commands.size()>3) commandString=Util.combine(commands,3);
      if((!found)&&(commandCode!=CREATE))
      {
        mob.tell("No clan was found by the name of '"+commands.elementAt(2)+"'.\n\r");
        return false;
      }
      else
      if(commandCode==CREATE)
      {
        commandString=Util.combine(commands,2);
      }
      switch(commandCode)
      {
        case REMOVEMEMBER:
          removeMemberSCC(mob, theClan, commandString);
          break;
        case ADDMEMBER:
          addMemberSCC(mob, theClan, commandString);
          break;
        case SETROLE:
          if(commands.size()<5)
          {
            mob.tell("You have to specify Clan Name, Clan Member Name, and Role.  Ex: aclan [command] [Clan name] [Clan member] [Role]");
            break;
          }
          setPosSCC(mob, theClan, (String)commands.elementAt(3), (String)commands.elementAt(4));
          break;
        case SETPREMISE:
          setPremiseSCC(mob, theClan, commandString);
          break;
        case CREATE:
          createSCC(mob, theClan, commandString);
          break;
        case DESTROY:
          destroySCC(mob, theClan);
          break;
        case SETHOME:
          setHomeSCC(mob, theClan);
          break;
       }
    }
    else
    {
      mob.tell("You are not powerful enough.");
    }
    return false;
	}

  public Vector getAccessWords() {return accessWords;}
  
  public String getAClanList()
  {
    return new String("");
  }
//CoffeeUtensils.containsString(strToSearch, strToSearchFor)
  public void removeMemberSCC(MOB mob, Clan theClan, String commandString)
  {
    MOB M=null;
    for(int s=0;s<Sessions.size();s++)
		{
			Session S=(Session)Sessions.elementAt(s);
      if(S.mob().ID().equalsIgnoreCase(commandString))
      {
        M=S.mob();
      }
    }
		if(M==null)
		{
      M=new StdMOB();
      M.setName(commandString);
      if(MOBloader.DBReadUserOnly(M))
      {
        MOBloader.DBUpdateClan(commandString, "", 0);
        mob.tell(M.ID()+" has been removed from Clan '"+theClan.ID()+"'.");
        return;
      }
      else
      {
        mob.tell(commandString+" was not found.  Could not remove from Clan.");
        return;
      }
    }
    else
    {
      MOBloader.DBUpdateClan(commandString, "", 0);
      mob.tell(M.ID()+" has been removed from Clan '"+theClan.ID()+"'.");
      M.tell(mob.ID()+" has removed you from Clan '"+theClan.ID()+"'.");
      return;
    }
  }
  
  public void addMemberSCC(MOB mob, Clan theClan, String commandString)
  {
    MOB M=null;
    for(int s=0;s<Sessions.size();s++)
		{
			Session S=(Session)Sessions.elementAt(s);
      if(S.mob().ID().equalsIgnoreCase(commandString))
      {
        M=S.mob();
      }
    }
		if(M==null)
		{
      M=new StdMOB();
      M.setName(commandString);
      if(MOBloader.DBReadUserOnly(M))
      {
        MOBloader.DBUpdateClan(commandString, theClan.ID(), Clan.POS_MEMBER);
        mob.tell(M.ID()+" has been add to Clan '"+theClan.ID()+"'.");
        return;
      }
      else
      {
        mob.tell(commandString+" was not found.  Could not add to Clan.");
        return;
      }
    }
    else
    {
      MOBloader.DBUpdateClan(commandString, theClan.ID(), Clan.POS_MEMBER);
      mob.tell(M.ID()+" has been added to Clan '"+theClan.ID()+"'.");
      M.tell(mob.ID()+" has made you a member of Clan '"+theClan.ID()+"'.");
      return;
    }
  }

  public void setPosSCC(MOB mob, Clan theClan, String clanMember, String position)
  {
    int newPos=0;
    if(position.equalsIgnoreCase("BOSS"))
    {
      newPos=Clan.POS_BOSS;
    }
    else
    if(position.equalsIgnoreCase("LEADER"))
    {
      newPos=Clan.POS_LEADER;
    }
    else
    if(position.equalsIgnoreCase("TREASURER"))
    {
      newPos=Clan.POS_TREASURER;
    }
    else
    if(position.equalsIgnoreCase("MEMBER"))
    {
      newPos=Clan.POS_MEMBER;
    }
    else
    if(position.equalsIgnoreCase("APPLICANT"))
    {
      newPos=Clan.POS_APPLICANT;
    }
    else
    {
      mob.tell("Unknown role '"+position+"'");
      return;
    }
    MOB M=null;
    for(int s=0;s<Sessions.size();s++)
		{
			Session S=(Session)Sessions.elementAt(s);
      if(S.mob().ID().equalsIgnoreCase(clanMember))
      {
        M=S.mob();
      }
    }
		if(M==null)
		{
      M=new StdMOB();
      M.setName(clanMember);
      if(MOBloader.DBReadUserOnly(M))
      {
        MOBloader.DBUpdateClan(clanMember, theClan.ID(), newPos);
        mob.tell(M.ID()+" has been made a '"+position+"' of Clan '"+theClan.ID()+"'.");
        return;
      }
      else
      {
        mob.tell(clanMember+" was not found.  Could not change Clan position.");
        return;
      }
    }
    else
    {
      MOBloader.DBUpdateClan(clanMember, theClan.ID(), newPos);
      mob.tell(M.ID()+" has been made a '"+position+"' of Clan '"+theClan.ID()+"'.");
      M.tell(mob.ID()+" has made you a '"+position+"' of Clan '"+theClan.ID()+"'.");
      return;
    }
    
  }

  public void setPremiseSCC(MOB mob, Clan theClan, String commandString)
  {
    theClan.setPremise(commandString);
    theClan.update();
    mob.tell("Clan '"+theClan.ID()+"' premise updated.");
  }

  public void createSCC(MOB mob, Clan theClan, String commandString)
  {
    theClan.setName(commandString);
    Clans.addElement(theClan);
    mob.tell("Clan '"+commandString+"' created.");
  }

  public void destroySCC(MOB mob, Clan theClan)
  {
    
  }

  public void setHomeSCC(MOB mob, Clan theClan)
  {
    Room R=mob.location();
    theClan.setRecall(R.ID());
    theClan.update();
  }
}
package com.planet_ink.coffee_mud.web.espresso.Commands;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.exceptions.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://thefactory.homedns.org</p>
 * @author not attributable
 * @version 1.0.0.0
 */

public class StdEspressoCommand implements EspressoCommand {
  public String Name = "Generic";

  public String ID() { return name(); }
  public String name() { return Name; }
  public MOB loggedIn=null;

  public StdEspressoCommand() {
  }

  public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

  public Object run(Vector paramV, EspressoServer server)
  {
    loggedIn=server.getMOB(auth(paramV));
    return "";
  }

  public boolean isAuthenticated(Vector param, EspressoServer server)
  {
    return server.authenticated((String)param.elementAt(0));
  }

  public String auth(Vector param)
  {
    if(param.size()==0)
      return "";
    if(param.elementAt(0)==null)
      return "";
    return (String)param.elementAt(0);
  }

  public String getHelp(String helpTopic)
  {
    return getHelp(helpTopic,loggedIn);
  }

  public String getHelp(String helpTopic, MOB forMOB)
  {
    StringBuffer s = MUDHelp.getHelpText(helpTopic,forMOB);
    if (s != null)
      return helpHelp(s).toString();
    return "";
  }

  protected StringBuffer helpHelp(StringBuffer s)
  {
          if(s!=null)
          {
                  s=new StringBuffer(s.toString());
                  int x=s.toString().indexOf("\n\r");
                  while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\n\r");}
                  x=s.toString().indexOf("\r\n");
                  while(x>=0){	s.replace(x,x+2,"<BR>"); x=s.toString().indexOf("\r\n");}
                  int count=0;
                  x=0;
                  int lastSpace=0;
                  while((x>=0)&&(x<s.length()))
                  {
                          count++;
                          if(s.charAt(x)==' ')
                                  lastSpace=x;
                          if((s.charAt(x)=='<')
                             &&(x<s.length()-4)
                             &&(s.substring(x,x+4).equalsIgnoreCase("<BR>")))
                          {
                                  count=0;
                                  x=x+4;
                                  lastSpace=x+4;
                          }
                          if(count>=70)
                          {
                                  s.replace(lastSpace,lastSpace+1,"<BR>");
                                  lastSpace=lastSpace+4;
                                  x=lastSpace+4;
                                  count=0;
                          }
                          else
                                  x++;
                  }
                  return s;
          }
          else
                  return new StringBuffer("");
  }

  public Object safelyGet(Vector param, int element)
  {
    if((param.size()-1)<element)
      return null;
    else
      return param.elementAt(element);
  }

  public String safelyGetStr(Vector param, int element)
  {
    if(safelyGet(param,element)==null)
      return "";
    return (String)safelyGet(param,element);
  }

  public Boolean safelyGetBool(Vector param, int element)
  {
    if (safelyGet(param, element) == null)
      return new Boolean(false);
    return (Boolean) safelyGet(param, element);
  }
}

package com.planet_ink.coffee_mud.web.espresso;

import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.web.espresso.Commands.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://thefactory.homedns.org</p>
 * @author not attributable
 * @version 1.0.0.0
 */

public class EspressoServer
    extends Thread {
  public INI page = null;
  public static final float HOST_VERSION_MAJOR = (float) 1.0;
  public static final float HOST_VERSION_MINOR = (float) 0.0;
  private static EspressoServer server;
  private int Port;
  public ServerSocket servsock = null;
  private static Hashtable commands = new Hashtable();

  public String getPartialName() { return "ESPSVR"; }

  public EspressoServer(MudHost a_mud, int a_port) {
    super("EspressoServer");
    Port = a_port;
    server = this;
  }

  public void run() {
    Socket sock = null;
    try {
      servsock = new ServerSocket(Port, 6, null);

      Log.sysOut("ESPSVR", "Started on port: " + Port);
      while(true)
      {
              sock=servsock.accept();
              EspressoRequest W=new EspressoRequest(sock,this);
              W.equals(W); // this prevents an initialized but never used error
              // nb - EspressoRequest is a Thread, but it .start()s in the constructor
              //  if succeeds - no need to .start() it here
              sock = null;
      }
    }
    catch (Throwable t) {}
    try {
      if (servsock != null) {
        servsock.close();
      }
      if (sock != null) {
        sock.close();
      }
    }
    catch (IOException e) {
    }

    Log.sysOut(getName(), "Thread stopped!");

  }

  public void shutdown(Session S) {
    Log.sysOut(getName(), "Shutting down.");
    if (S != null) {
      S.println(getName() + " shutting down.");
    }
    this.interrupt();
  }

  public void shutdown() {
    shutdown(null);
  }

  // interrupt does NOT interrupt the ServerSocket.accept() call...
  //  override it so it does
  public void interrupt() {
    if (servsock != null) {
      try {
        servsock.close();
        //jef: we MUST set it to null
        // (so run() can tell it was interrupted & didn't have an error)
        servsock = null;
      }
      catch (IOException e) {
      }
    }
    super.interrupt();
  }

  public int getPort() {
    return Port;
  }

  public String getPortStr() {
    return Integer.toString(Port);
  }

  public static Object runCommand(String command, Vector params)
  {
    EspressoCommand ec=(EspressoCommand)commands.get(command.toUpperCase());
    if(ec==null)
    {
      Log.errOut("ESPSRV", "Unknown command: "+command+" "+params);
      return null;
    }
    return ec.run(params, server);
  }

  public static boolean loadEspressoCommands()
  {
          String prefix="com"+File.separatorChar+"planet_ink"+File.separatorChar+"coffee_mud"+File.separatorChar;
          Vector commandsV=CMClass.loadVectorListToObj(prefix+"web"+File.separatorChar+"espresso"+File.separatorChar+"Commands"+File.separatorChar, "%DEFAULT%","com.planet_ink.coffee_mud.interfaces.EspressoCommand");
          Log.sysOut("ESPSVR","Esp. Cmds loaded  : "+commandsV.size());
          if(commandsV.size()==0) return false;
          commands=new Hashtable();
          for(int w=0;w<commandsV.size();w++)
          {
                  EspressoCommand W=(EspressoCommand)commandsV.elementAt(w);
                  commands.put(W.ID().toUpperCase(),W);
          }
          return true;
  }

  public static void unloadWebMacros()
  {
          commands=new Hashtable();
  }

  public static boolean authenticated(String auth)
  {
    if(auth==null) return false;
    if(auth.length()<1) return false;
    Authenticate authCmd = new Authenticate();
    Vector v=new Vector();
    v.addElement(auth);
    return ((Boolean)authCmd.run(v, server)).booleanValue();
  }

  public static MOB getMOB(String auth)
  {
    return Authenticate.getMOB(Authenticate.getLogin(auth));
  }
}
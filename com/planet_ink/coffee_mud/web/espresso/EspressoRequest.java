package com.planet_ink.coffee_mud.web.espresso;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.exceptions.*;

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

public class EspressoRequest extends Thread {
  private Socket sock;
  private static int instanceCnt = 0;
  private String command;
  private Vector param;
  public EspressoRequest(Socket a_sock, EspressoServer a_server) {
    // thread name contains both an instance counter and the client's IP address
    //  (too long)
    //		super( "HTTPrq-"+ instanceCnt++ +"-" + a_sock.getInetAddress().toString() );
    // thread name contains just the instance counter (faster)
    //  and short enough to use in log
    super( "HTTPrq-"+a_server.getPartialName()+ instanceCnt++ );
    sock = a_sock;
    if(sock!=null)
      this.start();
  }

  public void run()
  {
    // basically, we're looking to handle three steps
    // incoming request
    readRequest();
    // process request and return the results
    respondRequest(processRequest());
  }

  public void readRequest()
  {
      try {
        InputStream is = sock.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        command = (String) ois.readObject();
        param = (Vector) ois.readObject();
      }
      catch (ClassNotFoundException ex) {
      }
      catch (IOException ex) {
      }
  }

  public Object processRequest()
  {
    if((command!=null) && (param!=null))
      return EspressoServer.runCommand(command,param);
    return null;
  }

  public void respondRequest(Object response)
  {
    try {
      OutputStream os = sock.getOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(response);
      oos.flush();
      oos.close();
    }
    catch (IOException ex) {
    }
  }
}
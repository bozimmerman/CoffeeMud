package com.planet_ink.coffee_mud.web.espresso.Commands;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;

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

public class Authenticate
    extends StdEspressoCommand {
  public String Name = "Authenticate";
  public String ID() { return name(); }
  public String name() { return Name; }

  public Authenticate() {
  }

  public Object run(Vector param, EspressoServer server) {
    // param vector for authenticated:
    // 0: AUTH Encrypted
    // 1: LOGIN
    // 2: PASSWORD
    if(auth(param).length()>1)
    {
      // They've already logged in, verify that the auth matches at least something
      Vector authV=new Vector();
      authV.addElement(null);
      authV.addElement(getLogin(auth(param)));
      authV.addElement(getPassword(auth(param)));
      return new Boolean(authenticated(authV));
    }
    else {
      if(authenticated(param))
      {
        Log.errOut("ESPSRV", "Espresso Login: " + param.elementAt(1) + " from "+server.servsock.getInetAddress().getHostAddress());
        return Encrypt( (String) param.elementAt(1)) + "-" +
            Encrypt( (String) param.elementAt(2));
      }
      else
      {
        Log.sysOut("ESPSRV", "Failed Login: " + param.elementAt(1) + " from "+server.servsock.getInetAddress().getHostAddress());
      }
      return null;
    }
  }

  public static MOB getMOB(String last) {
    if (!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED)) {
      return null;
    }

    MOB M = CMMap.getPlayer(last);
    if (M == null) {
      for (Enumeration p = CMMap.players(); p.hasMoreElements(); ) {
        MOB mob2 = (MOB) p.nextElement();
        if (mob2.Name().equalsIgnoreCase(last)) {
          M = mob2;
          break;
        }
      }
    }
    MOB TM = CMClass.getMOB("StdMOB");
    if ( (M == null) && (CMClass.DBEngine().DBUserSearch(TM, last))) {
      M = CMClass.getMOB("StdMOB");
      M.setName(TM.Name());
      CMClass.DBEngine().DBReadMOB(M);
      CMClass.DBEngine().DBReadFollowers(M, false);
      if (M.playerStats() != null) {
        M.playerStats().setUpdated(M.playerStats().lastDateTime());
      }
      M.recoverEnvStats();
      M.recoverCharStats();
    }
    return M;
  }

  private static final String ABCs =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
  private static final String FILTER = "peniswrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad";

  private static boolean bannedName(String login) {
    Vector banned = Resources.getFileLineVector(Resources.getFileResource(
        "banned.ini", false));
    if ( (banned != null) && (banned.size() > 0)) {
      for (int b = 0; b < banned.size(); b++) {
        String str = (String) banned.elementAt(b);
        if (str.length() > 0) {
          if (str.equals("*") || ( (str.indexOf("*") < 0)) && (str.equals(login))) {
            return true;
          }
          else
          if (str.startsWith("*") && str.endsWith("*") &&
              (login.indexOf(str.substring(1, str.length() - 1)) >= 0)) {
            return true;
          }
          else
          if (str.startsWith("*") && (login.endsWith(str.substring(1)))) {
            return true;
          }
          else
          if (str.endsWith("*") &&
              (login.startsWith(str.substring(0, str.length() - 1)))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean authenticated(Vector param) {
    String login = (String) param.elementAt(1);
    String password = (String) param.elementAt(2);
    MOB mob = getMOB(login);
    if (mob == null) {
      return false;
    }
    boolean subOp = false;
    for (Enumeration a = CMMap.areas(); a.hasMoreElements(); ) {
      Area A = (Area) a.nextElement();
      if (A.amISubOp(mob.Name())) {
        subOp = true;
        break;
      }
    }
    return (mob.playerStats() != null)
        && (mob.playerStats().password().equalsIgnoreCase(password))
        && (mob.Name().trim().length() > 0)
        && (!bannedName(mob.Name()));
  }

  private static char ABCeq(char C) {
    for (int A = 0; A < ABCs.length(); A++) {
      if (C == ABCs.charAt(A)) {
        return ABCs.charAt(A);
      }
    }
    return (char) 0;
  }

  private static int ABCindex(char C) {
    for (int A = 0; A < ABCs.length(); A++) {
      if (C == ABCs.charAt(A)) {
        return A;
      }
    }
    return 0;
  }

  private static String Encrypt(String ENCRYPTME) {
    StringBuffer INTOME = new StringBuffer("");
    INTOME.setLength(ENCRYPTME.length());
    for (int S = 0; S < ENCRYPTME.length(); S++) {
      INTOME.setCharAt(S, ABCeq(ENCRYPTME.charAt(S)));
      if (INTOME.charAt(S) == (char) 0) {
        INTOME.setCharAt(S, ENCRYPTME.charAt(S));
      }
      else {
        for (int F = S; F < FILTER.length(); F += ENCRYPTME.length()) {
          int X = ABCindex(INTOME.charAt(S));
          X = X + ABCindex(FILTER.charAt(F));
          if (X >= ABCs.length()) {
            X = X - ABCs.length();
          }
          INTOME.setCharAt(S, ABCs.charAt(X));
        }
      }
    }
    return INTOME.toString();
  }

  private static String Decrypt(String DECRYPTME) {
    StringBuffer INTOME = new StringBuffer("");
    INTOME.setLength(DECRYPTME.length());
    for (int S = 0; S < DECRYPTME.length(); S++) {
      INTOME.setCharAt(S, ABCeq(DECRYPTME.charAt(S)));
      if (INTOME.charAt(S) == (char) 0) {
        INTOME.setCharAt(S, DECRYPTME.charAt(S));
      }
      else {
        for (int F = S; F < FILTER.length(); F += DECRYPTME.length()) {
          int X = ABCindex(INTOME.charAt(S));
          X = X - ABCindex(FILTER.charAt(F));
          if (X < 0) {
            X = X + ABCs.length();
          }
          INTOME.setCharAt(S, ABCs.charAt(X));
        }
      }
    }
    return INTOME.toString();
  }

  public static String getLogin(String auth) {
    String login;
    if (auth == null) {
      return "";
    }
    if (auth.indexOf("-") >= 0) {
      auth = auth.substring(0, auth.indexOf("-"));
    }
    login = Decrypt(auth);
    return login;
  }

  public static String getPassword(String auth) {
    String password;
    if (auth == null) {
      return "";
    }
    if (auth.indexOf("-") >= 0) {
      auth = auth.substring(auth.indexOf("-") + 1);
    }
    password = Decrypt(auth);
    return password;
  }

}
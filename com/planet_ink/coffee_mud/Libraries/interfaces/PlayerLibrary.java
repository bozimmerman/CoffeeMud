package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public interface PlayerLibrary extends CMLibrary, Runnable
{
    public int numPlayers();
    public void addPlayer(MOB newOne);
    public void delPlayer(MOB oneToDel);
    public MOB getPlayer(String calledThis);
    public MOB getLoadPlayer(String last);
    public PlayerAccount getLoadAccount(String calledThis);
    public PlayerAccount getAccount(String calledThis);
    public boolean accountExists(String name);
    public Enumeration<ThinPlayer> players();
    public Enumeration<PlayerAccount> accounts(String sort, Hashtable cache);
    public void obliteratePlayer(MOB deadMOB, boolean quiet);
    public void obliterateAccountOnly(PlayerAccount deadAccount);
    public boolean playerExists(String name);
    public void forceTick();
    public int savePlayers();
    public Enumeration thinPlayers(String sort, Hashtable cache);
	public int getCharThinSortCode(String codeName, boolean loose);
	public String getThinSortValue(ThinPlayer player, int code); 
    
	public static final String[] CHAR_THIN_SORT_CODES={ "NAME","CLASS","RACE","LEVEL","AGE","LAST","EMAIL","IP"};
	public static final String[] CHAR_THIN_SORT_CODES2={ "CHARACTER","CHARCLASS","RACE","LVL","HOURS","DATE","EMAILADDRESS","LASTIP"};
	
	public static final String[] ACCOUNT_THIN_SORT_CODES={ "NAME","LAST","EMAIL","IP","NUMPLAYERS"};
	
    public static class ThinPlayer
    {
    	public String name="";
    	public String charClass="";
    	public String race="";
    	public int level=0;
    	public int age=0;
    	public long last=0;
    	public String email="";
    	public String ip="";
    }
    
    public static class ThinnerPlayer
    {
    	public String name="";
    	public String password="";
    	public long expiration=0;
    	public String accountName="";
    	public String email="";
    	public MOB loadedMOB=null;
    }
}

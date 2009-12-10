package com.planet_ink.coffee_mud.Items.interfaces;
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
public interface Container extends Item
{
	public boolean isLocked();
	public boolean hasALock();
	public boolean isOpen();
	public boolean hasALid();
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked);
	public Vector getContents();
	public int capacity();
	public void setCapacity(int newValue);
	public boolean canContain(Environmental E);
	public long containTypes();
	public void setContainTypes(long containTypes);
	public void emptyPlease();
	
    /**
     * For containers with lids and locks, this returns the unique string
     * representing the key code required to unlock/lock the lid.
     * @see com.planet_ink.coffee_mud.Items.interfaces.Container#setKeyName(String)
     * @return the key code
     */
    public String keyName();
    
    /**
     * For containers with lids and locks, this sets the unique string
     * representing the key code required to unlock/lock the lid.
     * @see com.planet_ink.coffee_mud.Items.interfaces.Container#keyName()
     * @param keyName the new key code
     */
    public void setKeyName(String keyName);
    
	public static final int CONTAIN_ANYTHING=0;
	public static final int CONTAIN_LIQUID=1;
	public static final int CONTAIN_COINS=2;
	public static final int CONTAIN_SWORDS=4;
	public static final int CONTAIN_DAGGERS=8;
	public static final int CONTAIN_OTHERWEAPONS=16;
	public static final int CONTAIN_ONEHANDWEAPONS=32;
	public static final int CONTAIN_BODIES=64;
	public static final int CONTAIN_READABLES=128;
	public static final int CONTAIN_SCROLLS=256;
	public static final int CONTAIN_CAGED=512;
	public static final int CONTAIN_KEYS=1024;
	public static final int CONTAIN_DRINKABLES=2048;
	public static final int CONTAIN_CLOTHES=4096;
	public static final int CONTAIN_SMOKEABLES=8192;
	public static final int CONTAIN_SSCOMPONENTS=16384;
	public static final int CONTAIN_FOOTWEAR=32768;
	public static final String[] CONTAIN_DESCS={"ANYTHING",
												"LIQUID",
												"COINS",
												"SWORDS",
												"DAGGERS",
												"OTHER WEAPONS",
												"ONE-HANDED WEAPONS",
												"BODIES",
												"READABLES",
												"SCROLLS",
												"CAGED ANIMALS",
												"KEYS",
												"DRINKABLES",
												"CLOTHES",
												"SMOKEABLES",
												"SS COMPONENTS",
												"FOOTWEAR"};
}

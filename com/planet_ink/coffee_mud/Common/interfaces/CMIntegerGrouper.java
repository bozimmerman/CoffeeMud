package com.planet_ink.coffee_mud.Common.interfaces;
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
   Copyright 2000-2006 Bo Zimmerman

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
public interface CMIntegerGrouper extends CMObject
{
    public static final int NEXT_FLAG=(Integer.MAX_VALUE/2)+1;
    public static final int NEXT_BITS=NEXT_FLAG-1;
    public static final long NEXT_FLAGL=(Long.MAX_VALUE/2)+1;
    public static final long NEXT_BITSL=NEXT_FLAGL-1;
    
    public String text();
    public CMIntegerGrouper parseText(String txt);
    public boolean contains(long x);
    public int roomCount();
    public void growarrayx(int here);
    public void growarrayy(int here);
    public void shrinkarrayx(int here);
    public void shrinkarrayy(int here);
    public void consolodatex();
    public void consolodatey();
    public CMIntegerGrouper add(long x);
    public void addy(long x);
    public void addx(int x);
}

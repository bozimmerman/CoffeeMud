package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class GenKey extends GenItem implements Key
{
	public String ID(){	return "GenKey";}
	public GenKey()
	{
		super();
		setName("a generic key thing");
		setDisplayText("a generic key thing sits here.");
		setDescription("");
		setMaterial(EnvResource.RESOURCE_IRON);
	}


	public boolean isGeneric(){return true;}

	public void setKey(String keyName){readableText=keyName;}
	public String getKey(){return readableText;}
}

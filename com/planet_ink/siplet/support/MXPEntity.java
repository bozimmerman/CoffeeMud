package com.planet_ink.siplet.support;

/*
   Copyright 2008-2018 Bo Zimmerman

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
public class MXPEntity implements Cloneable
{
	private String	name		= "";
	private String	definition	= "";

	public MXPEntity(String theName, String theDefinition)
	{
		super();
		name = theName;
		definition = theDefinition;
	}

	public String getName()
	{
		return name;
	}

	public String getDefinition()
	{
		return definition;
	}

	public void setDefinition(String newDefinition)
	{
		definition = newDefinition;
	}
}

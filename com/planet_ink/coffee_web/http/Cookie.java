package com.planet_ink.coffee_web.http;

/*
   Copyright 2025-2026 Bo Zimmerman

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
public class Cookie
{
	public String	name;
	public String	value;
	public String	domain		= "";
	public String	path		= "/";
	public long		maxAge		= -1;
	public boolean	secure		= false;
	public boolean	httpOnly	= false;

	public Cookie(final String name, final String value)
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder(name + "=" + value);
		if (domain.length() > 0)
			str.append("; domain=" + domain);
		if (path.length() > 0)
			str.append("; path=" + path);
		if (maxAge >= 0)
			str.append("; max-age=" + maxAge);
		if (secure)
			str.append("; secure");
		if (httpOnly)
			str.append("; httpOnly");
		return str.toString();
	}
}

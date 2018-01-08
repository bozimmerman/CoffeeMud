package com.planet_ink.coffee_mud.core.exceptions;
import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public abstract class CoffeeMudException extends Exception
{
	private static final long serialVersionUID = 8932995125810826091L;

	public CoffeeMudException(String s)
	{
		super(s,new Exception());
	}
	
	public CoffeeMudException(String s, Exception e)
	{
		super(s,e);
	}
	
	public CoffeeMudException(Exception e)
	{
		super(e);
	}
}


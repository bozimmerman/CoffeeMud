
package com.planet_ink.coffee_mud.exceptions;

public class HTTPRedirectException extends HTTPServerException
{
	public HTTPRedirectException(String url)
	{
		super(url);
	}
}


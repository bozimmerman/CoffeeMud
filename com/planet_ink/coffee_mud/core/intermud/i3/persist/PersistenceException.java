package com.planet_ink.coffee_mud.core.intermud.i3.persist;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/**
 * com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException
 * Copyright (c) 1996 George Reese
 * This source code may not be modified, copied,
 * redistributed, or used in any fashion without the
 * express written consent of George Reese.
 *
 * A PersistenceException occurs whenever some error
 * condition interrupts a persistence operation.
 * This class maintains a chain of exceptions so
 * that an easy trace can be done to find out what
 * caused the problem.
 */

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An exception that gets thrown on persistence operations.
 * If it was triggered by a specific exception, that
 * exception will be added to the exception chain so
 * any class catching this exception can get detailed
 * information on what went wrong.
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class PersistenceException extends Exception
{
	public static final long serialVersionUID=0;
	private final Exception prior;

	public PersistenceException()
	{
		this("No reason given.");
	}

	/**
	 * Constructs a new PersistenceException with the
	 * specified exception explanation.
	 * @param reason the reason for the exception
	 */
	public PersistenceException(String reason)
	{
		this(reason, null);
	}

	/**
	 * Constructs a new PersistenceException that occured
	 * because another exception was encountered during a
	 * persistence operation such as a save or restore.
	 * @param e the exception causing this exception to be created
	 */
	public PersistenceException(Exception e)
	{
		this("A persistence exception occurred: " + e.getMessage(), e);
	}

	/**
	 * Constructs a new PersistenceException based on a previous
	 * exception during a persistence operation with the specified
	 * exception explanation.
	 * @param reason the explanation for the exception
	 * @param e the exception causing this exception to be created
	 */
	public PersistenceException(String reason, Exception e)
	{
		super(reason);
		prior = e;
	}

	/**
	 * @return the exception which caused this one to be created
	 */
	public Exception getPriorException()
	{
		return prior;
	}

	/**
	 * @return the full chain of exceptions leading to this one
	 */
	public Enumeration getExceptionChain()
	{
		return new PersistenceExceptionEnumeration(this);
	}
}

@SuppressWarnings("rawtypes")
final class PersistenceExceptionEnumeration implements Enumeration {
  private Exception exception;

  public PersistenceExceptionEnumeration()
  {
	this(null);
  }

  public PersistenceExceptionEnumeration(Exception e)
  {
	exception = e;
  }

  @Override
public boolean hasMoreElements()
  {

	if( exception == null )
		return false;
	return true;
  }

  @Override
public Object nextElement()
  {
	Exception e;

	if( exception == null )
		throw new NoSuchElementException();
	e = exception;
	if( e instanceof PersistenceException )
		exception = ((PersistenceException)e).getPriorException();
	else exception = null;
	return e;
  }
}


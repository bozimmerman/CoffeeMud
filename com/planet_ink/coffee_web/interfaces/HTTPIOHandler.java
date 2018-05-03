package com.planet_ink.coffee_web.interfaces;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.server.WebServer;

/*
   Copyright 2012-2018 Bo Zimmerman

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

/**
 * For off-thread async reading, this interface does well.  The runnable portion
 * ensures that nothing is read unless its given its own thread time (reading occurs
 * in the run() method iow).  The rest helps external entites manage or read its
 * internal state.
 * 
 * For now, the only IO handlers are readers, though in the future this same interface
 * would be great for writers.
 * @author Bo Zimmerman
 *
 */
public interface HTTPIOHandler extends Runnable
{
	public static final String 		 EOLN 			= "\r\n";			// standard EOLN for http protocol
	public static final String 		 SERVER_HEADER 	= HTTPHeader.Common.SERVER.makeLine("CoffeeWebServer/"+WebServer.VERSION);
	public static final String 		 CONN_HEADER  	= HTTPHeader.Common.CONNECTION.makeLine("Keep-Alive");
	public static final String 		 RANGE_HEADER  	= HTTPHeader.Common.ACCEPT_RANGES.makeLine("bytes");
	public static final DateFormat 	 DATE_FORMAT	= new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");
	public static final byte[]		 CONT_RESPONSE  = ("HTTP/1.1 "+HTTPStatus.S100_CONTINUE.getStatusCode()+" CONTINUE" + EOLN + EOLN).getBytes(); 

	/**
	 * Returns the name of this handler.
	 * @return the name of this handler
	 */
	public String getName();
	
	/**
	 * Force the io handler to close itself off to any future activity
	 * If the runnable is running, wait until its done before returning
	 */
	public void closeAndWait();
	
	/**
	 * Returns whether this handler considers itself done.  If true is
	 * returned, the close() method should be called next, and then this
	 * object never touched again by its manager.
	 * @return true if this handler is done
	 */
	public boolean isCloseable();
	
	/**
	 * Returns true if this handler is currently, actively, processing 
	 * in another thread.  Can be used to prevent two separate threads from
	 * blocking on the same io channel
	 * @return true if the handler is active atm.
	 */
	public boolean isRunning();
	
	/**
	 * Notifies the I/O handler that it has data to process from somewhere
	 * other than its internal read buffers.
	 * @return true if the scheduling was successful
	 */
	public boolean scheduleProcessing();

	/**
	 * Reads bytes from the given buffer into the internal channel channel.
	 * @param buffer source buffer for the data write
	 * @return number of bytes written
	 * @throws IOException
	 */
	@Deprecated
	public int writeBlockingBytesToChannel(final DataBuffers buffer) throws IOException;
	
	/**
	 * Queues the given buffer for eventual writing to the channel
	 * @param buffer source buffer for the data write
	 * @throws IOException
	 */
	public void writeBytesToChannel(final DataBuffers buffer) throws IOException;
}

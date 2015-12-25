package com.planet_ink.coffee_web.interfaces;

import java.nio.ByteBuffer;

import com.planet_ink.coffee_web.http.HTTPException;

/**
 * Handles the protocol at a high level by receiving  
 * @author Bo Zimmerman
 *
 */
public interface ProtocolHandler
{
	/**
	 * Handles high level state for request reading.
	 * It is handed a ByteBuffer recently written to, which it then flips
	 * and reads out of (in the case of a request state) or simply checks
	 * for errors in the case of a body read state.  
	 * 
	 * The buffer may be modified if an actionable request piece is portioned
	 * off of it.  It will also modify the internal state of parsing as needed.
	 * It will help populate the HTTPRequest object as needed, but will not
	 * generate the output.  It may, however, mark the request as complete
	 * if the state warrants.
	 * 
	 * The method should exit with the buffer in the same writeable state it
	 * was handed.
	 * 
	 * @param request the request currently being parsed
	 * @param buffer the bytebuffer to process data from
	 * @throws HTTPException any parse or protocol errors
	 */
	public DataBuffers processBuffer(HTTPRequest request, ByteBuffer buffer) throws HTTPException;

}

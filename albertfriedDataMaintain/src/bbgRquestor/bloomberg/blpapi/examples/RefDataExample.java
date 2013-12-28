/*
 * Copyright 2012. Bloomberg Finance L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:  The above
 * copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package bbgRquestor.bloomberg.blpapi.examples;

import java.util.ArrayList;
import java.util.List;

import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRquestor.bloomberg.beans.SecurityTimeUnit;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.InvalidRequestException;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;

public class RefDataExample
{
	private static final Name	SECURITY_DATA		= new Name( "securityData" );
	private static final Name	SECURITY			= new Name( "security" );
	private static final Name	FIELD_DATA			= new Name( "fieldData" );
	private static final Name	RESPONSE_ERROR		= new Name( "responseError" );
	private static final Name	SECURITY_ERROR		= new Name( "securityError" );
	private static final Name	FIELD_EXCEPTIONS	= new Name( "fieldExceptions" );
	private static final Name	FIELD_ID			= new Name( "fieldId" );
	private static final Name	ERROR_INFO			= new Name( "errorInfo" );
	private static final Name	CATEGORY			= new Name( "category" );
	private static final Name	MESSAGE				= new Name( "message" );

	public List<SecurityTimeUnit> run(Session session, List<String> names, List<String> fields) throws Exception {
		try {
			sendRefDataRequest( session, names, fields );
		} catch (InvalidRequestException e) {
			e.printStackTrace();
		}

		// wait for events from session.
		return eventLoop( session );
	}

	private List<SecurityTimeUnit> eventLoop(Session session) throws Exception {
		List<SecurityTimeUnit> tmp = new ArrayList<>();
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				System.out.println( "Processing Partial Response" );
				tmp.addAll( processResponseEvent( event ) );
			}
			else if (event.eventType() == Event.EventType.RESPONSE) {
				System.out.println( "Processing Response" );
				tmp.addAll( processResponseEvent( event ) );
				done = true;
			} else {
				MessageIterator msgIter = event.messageIterator();
				while (msgIter.hasNext()) {
					Message msg = msgIter.next();
					System.out.println( msg.asElement() );
					if (event.eventType() == Event.EventType.SESSION_STATUS) {
						if (msg.messageType().equals( "SessionTerminated" ) ||
								msg.messageType().equals( "SessionStartupFailure" )) {
							done = true;
						}
					}
				}
			}
		}
		return tmp;
	}

	// return true if processing is completed, false otherwise
	private List<SecurityTimeUnit> processResponseEvent(Event event) throws Exception
	{
		MessageIterator msgIter = event.messageIterator();
		List<SecurityTimeUnit> res = new ArrayList<>();
		while (msgIter.hasNext()) {
			Message msg = msgIter.next();
			if (msg.hasElement( RESPONSE_ERROR )) { throw new Exception( "REQUEST FAILED: " + msg.getElement( RESPONSE_ERROR ) ); }

			Element securities = msg.getElement( SECURITY_DATA );
			int numSecurities = securities.numValues();
			System.out.println( "Processing " + numSecurities + " securities:" );
			for (int i = 0; i < numSecurities; ++i) {
				Element security = securities.getValueAsElement( i );
				String ticker = security.getElementAsString( SECURITY );
				if (security.hasElement( "securityError" )) { throw new Exception( "SECURITY FAILED: " + security.getElement( SECURITY_ERROR ) ); }

				SecurityTimeUnit tmp = new SecurityTimeUnit( ticker );

				if (security.hasElement( FIELD_DATA )) {
					Element fields = security.getElement( FIELD_DATA );
					if (fields.hasElement( Fields.open )) tmp.setOpen( fields.getElementAsFloat64( Fields.open ) );
					if (fields.hasElement( Fields.close )) tmp.setClose( fields.getElementAsFloat64( Fields.close ) );
					if (fields.hasElement( Fields.high )) tmp.setHigh( fields.getElementAsFloat64( Fields.high ) );
					if (fields.hasElement( Fields.low )) tmp.setLow( fields.getElementAsFloat64( Fields.low ) );
					if (fields.hasElement( Fields.last )) tmp.setLast( fields.getElementAsFloat64( Fields.last ) );
					if (fields.hasElement( Fields.volume )) tmp.setVolume( fields.getElementAsFloat64( Fields.volume ) );
					if (fields.hasElement( Fields.pe )) tmp.setPE( fields.getElementAsFloat64( Fields.pe ) );
					if (fields.hasElement( Fields.sharesOutstanding )) tmp
							.setSharesOutstanding( fields.getElementAsInt64( Fields.sharesOutstanding ) );
					if (fields.hasElement( Fields.vwap )) tmp.setVwap( fields.getElementAsFloat64( Fields.vwap ) );
					if (fields.hasElement( Fields.currentQuarterEEPS )) tmp.setCurrentQuarterlyEEPS( fields
							.getElementAsFloat64( Fields.currentQuarterEEPS ) );
					if (fields.hasElement( Fields.callIV )) tmp.setCallIV( fields.getElementAsFloat64( Fields.callIV ) );
					if (fields.hasElement( Fields.putIV )) tmp.setPutIV( fields.getElementAsFloat64( Fields.putIV ) );

				}
				Element fieldExceptions = security.getElement( FIELD_EXCEPTIONS );
				if (fieldExceptions.numValues() > 0) {
					for (int k = 0; k < fieldExceptions.numValues(); ++k) {
						Element fieldException =
								fieldExceptions.getValueAsElement( k );
						printErrorInfo( fieldException.getElementAsString( FIELD_ID ) +
								"\t\t", fieldException.getElement( ERROR_INFO ) );
					}
				}
				res.add( tmp );
			}
		}
		return res;
	}

	private void sendRefDataRequest(Session session, List<String> names, List<String> fields) throws Exception
	{
		Service refDataService = session.getService( "//blp/refdata" );
		Request request = refDataService.createRequest( "ReferenceDataRequest" );

		// Add securities to request
		Element securities = request.getElement( "securities" );

		for (String security : names) {
			securities.appendValue( security );
		}

		// Add fields to request
		Element fieldss = request.getElement( "fields" );
		for (String field : fields) {
			fieldss.appendValue( field );
		}

		System.out.println( "Sending Request: " + request );
		session.sendRequest( request, null );
	}

	private void printErrorInfo(String leadingStr, Element errorInfo)
			throws Exception
	{
		throw new Exception( leadingStr + errorInfo.getElementAsString( CATEGORY ) +
				" (" + errorInfo.getElementAsString( MESSAGE ) + ")" );
	}
}

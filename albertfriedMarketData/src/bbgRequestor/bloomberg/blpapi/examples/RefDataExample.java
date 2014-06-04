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
package bbgRequestor.bloomberg.blpapi.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import middleware.bbg.beans.DataRequest.RequestType;
import utils.ParseDate;
import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRequestor.bloomberg.beans.DividendTimeUnit;
import bbgRequestor.bloomberg.beans.SecurityTimeUnit;
import bbgRequestor.bloomberg.beans.TimeUnit;

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
	private Logger				_logger;
	private static final Name	SECURITY_DATA		= new Name("securityData");
	private static final Name	SECURITY			= new Name("security");
	private static final Name	FIELD_DATA			= new Name("fieldData");
	private static final Name	RESPONSE_ERROR		= new Name("responseError");
	private static final Name	SECURITY_ERROR		= new Name("securityError");
	private static final Name	FIELD_EXCEPTIONS	= new Name("fieldExceptions");
	private static final Name	FIELD_ID			= new Name("fieldId");
	private static final Name	ERROR_INFO			= new Name("errorInfo");
	private static final Name	CATEGORY			= new Name("category");
	private static final Name	MESSAGE				= new Name("message");
	private RequestType			_type;

	public ArrayList<? extends TimeUnit> run(Session session, RequestType type, Set<String> names, Set<String> fields, Logger logger) throws Exception {
		_logger = logger;
		_type = type;
		try {
			sendRefDataRequest(session, names, fields);
		} catch (InvalidRequestException e) {
			e.printStackTrace();
		}

		// wait for events from session.
		return eventLoop(session);
	}

	private ArrayList<? extends TimeUnit> eventLoop(Session session) throws Exception {
		ArrayList<? extends TimeUnit> list = new ArrayList<>();
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				_logger.info("EventLoop: Processing Partial Response");
				processResponseEvent(event, list);
			}
			else if (event.eventType() == Event.EventType.RESPONSE) {
				_logger.info("EventLoop: Processing Response");
				processResponseEvent(event, list);
				done = true;
			} else {
				MessageIterator msgIter = event.messageIterator();
				while (msgIter.hasNext()) {
					Message msg = msgIter.next();
					_logger.info("EventLoop: " + msg.asElement().toString());
					if (event.eventType() == Event.EventType.SESSION_STATUS) {
						if (msg.messageType().equals("SessionTerminated") ||
								msg.messageType().equals("SessionStartupFailure")) {
							done = true;
						}
					}
				}
			}
		}
		return list;
	}

	// return true if processing is completed, false otherwise
	@SuppressWarnings("unchecked")
	private void processResponseEvent(Event event, List<? extends TimeUnit> list) throws Exception {
		MessageIterator msgIter = event.messageIterator();

		while (msgIter.hasNext()) {
			Message msg = msgIter.next();
			_logger.info("processResponseEvent:msg: " + msg.toString());
			if (msg.hasElement(RESPONSE_ERROR)) { throw new Exception("REQUEST FAILED: " + msg.getElement(RESPONSE_ERROR)); }

			Element securities = msg.getElement(SECURITY_DATA);
			int numSecurities = securities.numValues();
			_logger.info("processResponseEvent: Processing " + numSecurities + " securities:");
			for (int i = 0; i < numSecurities; ++i) {
				Element security = securities.getValueAsElement(i);
				String ticker = security.getElementAsString(SECURITY);
				_logger.info("processResponseEvent:security: " + security);
				if (_type.equals(RequestType.Div)) {
					readDiv(security, (List<DividendTimeUnit>) list, ticker);
				} else if (_type.equals(RequestType.Sec)) {
					readSec(security, (List<SecurityTimeUnit>) list, ticker);
				}
				else throw new Exception("Type of reference should be sec or div");
			}
		}
	}

	private void readSec(Element security, List<SecurityTimeUnit> list, String ticker) throws Exception {
		if (security.hasElement("securityError")) {
			System.err.println(security.getElementAsString(SECURITY_ERROR));
			throw new Exception("SECURITY FAILED: " + security.getElement(SECURITY_ERROR));
		}
		if (security.hasElement(FIELD_DATA)) {
			Element fields = security.getElement(FIELD_DATA);
			SecurityTimeUnit tmp = new SecurityTimeUnit(ticker);

			try {
				_logger.info("readSec: fields: " + fields);
			} catch (Exception e) {
				_logger.info("readSec: Can't print fields:" + e);
			}
			if (fields.hasElement(Fields.open)) tmp.setOpen(fields.getElementAsFloat64(Fields.open));
			if (fields.hasElement(Fields.close)) tmp.setClose(fields.getElementAsFloat64(Fields.close));
			if (fields.hasElement(Fields.high)) tmp.setHigh(fields.getElementAsFloat64(Fields.high));
			if (fields.hasElement(Fields.low)) tmp.setLow(fields.getElementAsFloat64(Fields.low));
			if (fields.hasElement(Fields.last)) tmp.setLast(fields.getElementAsFloat64(Fields.last));
			if (fields.hasElement(Fields.volume)) tmp.setVolume(fields.getElementAsFloat64(Fields.volume));
			if (fields.hasElement(Fields.pe)) tmp.setPE(fields.getElementAsFloat64(Fields.pe));
			if (fields.hasElement(Fields.sharesOutstanding)) tmp
					.setSharesOutstanding(fields.getElementAsInt64(Fields.sharesOutstanding));
			if (fields.hasElement(Fields.vwap)) tmp.setVwap(fields.getElementAsFloat64(Fields.vwap));
			if (fields.hasElement(Fields.currentQuarterEEPS)) tmp.setCurrentQuarterlyEEPS(fields
					.getElementAsFloat64(Fields.currentQuarterEEPS));
			if (fields.hasElement(Fields.callIV)) tmp.setCallIV(fields.getElementAsFloat64(Fields.callIV));
			if (fields.hasElement(Fields.putIV)) tmp.setPutIV(fields.getElementAsFloat64(Fields.putIV));
			list.add(tmp);
		}
		Element fieldExceptions = security.getElement(FIELD_EXCEPTIONS);
		if (fieldExceptions.numValues() > 0) {
			for (int k = 0; k < fieldExceptions.numValues(); ++k) {
				Element fieldException =
						fieldExceptions.getValueAsElement(k);
				printErrorInfo(fieldException.getElementAsString(FIELD_ID) +
						"\t\t", fieldException.getElement(ERROR_INFO));
			}
		}
	}

	private void readDiv(Element security, List<DividendTimeUnit> list, String ticker) throws Exception {
		DividendTimeUnit tmp = new DividendTimeUnit(ticker);

		if (security.hasElement("securityError")) {
			/* //if security.getElementAsString( SECURITY_ERROR ).contains( "BAD_FLD" )
			 * String err = security.getElementAsString( SECURITY_ERROR );
			 * boolean flag = false;
			 * if (err.contains( Fields.exDate )) {tmp.setExDate( "" ); flag = true;}
			 * if (err.contains( Fields.divAmount )) {tmp.setAmount( 0 ); flag = true;}
			 * if (err.contains( Fields.nextExDate )) {tmp.setNextExDate( "" ); flag = true;}
			 * if (err.contains( Fields.nextDivAmount )) {tmp.setNextAmount( 0 ); flag = true;}
			 * if (flag) {list.add( tmp ); return;} */
			// throw new Exception( "SECURITY FAILED: " + security.getElement( SECURITY_ERROR ) );
			System.err.println(security.getElementAsString(SECURITY_ERROR));
			tmp.setExtra("securityError");
		}
		if (security.hasElement(FIELD_DATA)) {
			Element fields = security.getElement(FIELD_DATA);
			try {
				_logger.info("readSec: fields: " + fields);
			} catch (Exception e) {
				_logger.info("readSec: Can't print fields:" + e);
			}
			if (fields.hasElement(Fields.exDate)) tmp.setExDate(ParseDate.standardFromCal(fields.getElementAsDate(Fields.exDate).calendar()));
			if (fields.hasElement(Fields.recordDate)) tmp
					.setRecordDate(ParseDate.standardFromCal(fields.getElementAsDate(Fields.recordDate).calendar()));
			if (fields.hasElement(Fields.payableDate)) tmp.setPayableDate(ParseDate.standardFromCal(fields.getElementAsDate(Fields.payableDate)
					.calendar()));
			if (fields.hasElement(Fields.divAmount)) tmp.setAmount(fields.getElementAsFloat64(Fields.divAmount));
			if (fields.hasElement(Fields.divFreq)) tmp.setFrequency(fields.getElementAsString(Fields.divFreq));
			if (fields.hasElement(Fields.divType)) tmp.setType(fields.getElementAsString(Fields.divType));
			if (fields.hasElement(Fields.nextExDate)) tmp.setNextExDate(ParseDate.standardFromCal(fields.getElementAsDate(Fields.nextExDate).calendar()));
			if (fields.hasElement(Fields.nextDivAmount)) tmp.setNextAmount(fields.getElementAsFloat64(Fields.nextDivAmount));
			if (fields.hasElement(Fields.close)) tmp.setExtra(fields.getElementAsString(Fields.close));
		}
		Element fieldExceptions = security.getElement(FIELD_EXCEPTIONS);
		if (fieldExceptions.numValues() > 0) {
			for (int k = 0; k < fieldExceptions.numValues(); ++k) {
				Element fieldException =
						fieldExceptions.getValueAsElement(k);
				// printErrorInfo( fieldException.getElementAsString( FIELD_ID ) +
				// "\t\t", fieldException.getElement( ERROR_INFO ) );
				tmp.set(fieldException.getElementAsString(FIELD_ID), null);
			}
		}

		list.add(tmp);
	}

	private void sendRefDataRequest(Session session, Set<String> names, Set<String> fields) throws Exception {
		Service refDataService = session.getService("//blp/refdata");
		Request request = refDataService.createRequest("ReferenceDataRequest");

		// Add securities to request
		Element securities = request.getElement("securities");

		for (String security : names) {
			securities.appendValue(security);
		}

		// Add fields to request
		Element fieldss = request.getElement("fields");
		for (String field : fields) {
			fieldss.appendValue(field);
		}

		_logger.info("sendRequest: Sending Request: " + request.toString());
		session.sendRequest(request, null);
	}

	private void printErrorInfo(String leadingStr, Element errorInfo) throws Exception {
		throw new Exception(leadingStr + errorInfo.getElementAsString(CATEGORY) +
				" (" + errorInfo.getElementAsString(MESSAGE) + ")");
	}
}

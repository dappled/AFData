package importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import middleware.BbgDataClient;
import middleware.BbgDataServer;
import bloomberg.BbgNames;
import bloomberg.BbgNames.FieldValue;
import bloomberg.BbgNames.Period;
import bloomberg.BbgNames.PeriodAdj;
import bloomberg.beans.HisSecTS;
import bloomberg.beans.TimeSeries.TSType;

import com.bloomberglp.blpapi.Name;

/**
 * @author Zhenghong Dong
 */
public class ImportManager {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// request parameters
		HashMap<Name, Object> properties = new HashMap<>();
		properties.put( BbgNames.START, "20110101" );
		properties.put( BbgNames.END, "20130101" );
		properties.put( BbgNames.PERIOD, Period.monthly );
		properties.put( BbgNames.PERIOD_ADJ, PeriodAdj.actual );
		properties.put( BbgNames.RETEID, Boolean.TRUE );
		properties.put( BbgNames.MAX_POINTS, BbgNames.maxDataPoints );

		List<String> names = Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" );
		List<String> fields = Arrays.asList( FieldValue.last, FieldValue.open );
	
		// initialize server and client
		final BbgDataServer server = new BbgDataServer( "Data" );
		final BbgDataClient client = new BbgDataClient();

		long startTime = System.currentTimeMillis();

		// client listen to this queue
		String queueName = "Historical";
		client.listenTo( queueName );

		// publish quest on security
		HashMap<String, HisSecTS> res = (HashMap<String, HisSecTS>) server.publishHisQuest( queueName, TSType.HisSec, names, fields, properties );

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println( totalTime );

}
}

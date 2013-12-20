package bbgRequetor.importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import middleware.bbg.BbgDataClient;
import middleware.bbg.BbgDataServer;
import bbgRequestor.bloomberg.BbgNames;
import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRquestor.bloomberg.beans.HisSecTS;
import bbgRquestor.bloomberg.beans.TimeSeries.TSType;

/**
 * @author Zhenghong Dong
 */
public class ImportManager {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		// initialize server and client
		final BbgDataServer server = new BbgDataServer(null);
		final BbgDataClient client = new BbgDataClient();

		// client listen to this queue
		String queueName = "Historical";
		client.listenTo( queueName );		
		
		// request parameters
		HashMap<String, Object> properties = new HashMap<>();
		properties.put( BbgNames.Properties.START, "20110101" );
		properties.put( BbgNames.Properties.END, "20130101" );
		properties.put( BbgNames.Properties.PERIOD, BbgNames.Properties.Period.monthly );
		properties.put( BbgNames.Properties.PERIOD_ADJ, BbgNames.Properties.PeriodAdj.actual );
		properties.put( BbgNames.Properties.RETEID, Boolean.TRUE );
		properties.put( BbgNames.Properties.MAX_POINTS, BbgNames.Properties.maxDataPoints );

		List<String> names = Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" );
		List<String> fields = Arrays.asList( Fields.last, Fields.open );
	
		// publish quest on security
		HashMap<String, HisSecTS> res = (HashMap<String, HisSecTS>) server.publishHisQuest( queueName, TSType.HisSec, names, fields, properties );
		for (String s : res.keySet()) {
			System.out.println(s);
		}

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println( totalTime );
		
		client.close();
		server.close();

}
}

package microhedge;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import utils.CollectionUtils;
import utils.DateUtils;
import utils.GeneralImporterExporter;

/**
 * @author Zhenghong Dong
 */
public class YieldCurve extends GeneralImporterExporter {
	private final static String			_dbServer	= "PROP-RN\\SQLEXPRESS";
	private PolynomialSplineFunction	_cubic;

	public YieldCurve() {
		super( _dbServer, null );
		InitYieldCurve();
	}

	private void InitYieldCurve() {
		List<Double> tenor = new ArrayList<>();
		List<Double> yield = new ArrayList<>();

		final String query = "select [tradedate], [symbol], [tenor], [close] from securitiesMaster.dbo.activeyieldcurve_current where symbol like 'USSO%' or symbol in ('USSWAP7','USSWAP10','USSWAP12','USSWAP15','USSWAP20','USSWAP25','USSWAP30','USSWAP40','USSWAP50')";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				String symbol = rs.getString( 2 ).trim();
				if (rs.getString( 1 ) == null) {
					System.err.println( "YieldCurve:InitYieldCurve:Missing price information for symbol: " + symbol );
				} else {
					tenor.add( getDaysFromTenor( rs.getString( 3 ).trim() ) );
					yield.add( rs.getDouble( 4 ) );
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// sort tenor and according yield
		TreeMap<Double, Double> sortMap = new TreeMap<>();
		for (int i = 0; i < tenor.size(); i++) {
			sortMap.put( tenor.get( i ), yield.get( i ) );
		}
		double[] x = CollectionUtils.doubleFromDouble( sortMap.keySet().toArray( new Double[ tenor.size() ] ));
		double[] y = CollectionUtils.doubleFromDouble( sortMap.values().toArray( new Double[ tenor.size() ] ));

		_cubic = new SplineInterpolator().interpolate( x, y );
	}

	private Double getDaysFromTenor(String tenor) {
		int num = Integer.parseInt( tenor.substring( 0, tenor.length() - 1 ) );
		String typestr = tenor.substring( tenor.length() - 1 );
		Double days = 0d;

		switch (typestr) {
			case "D":
				days = 1d;
				break;
			case "M":
				days = 30d;
				break;
			case "Y":
				days = 365d;
				break;
			case "W":
				days = 7d;
				break;
			default:
				System.err.println( "YieldCurve:ParseTenor:Incorrect format of tenor " + tenor );
		}

		return days * num;
	}

	public double yield(int year, int month) throws Exception {
		if (year < 0 || month <= 0 || month > 12) { throw new Exception(
				"YeildCurve: Incorrect input, year should be larger than 0 and month should be in (0, 12]" ); }
		Days d = Days.daysBetween( LocalDate.now(), DateUtils.GetExpDayFromDate( new LocalDate( year, month, 1 ) ) );

		return _cubic.value( d.getDays() );

	}

	public static void main(final String[] args) throws Exception {
		YieldCurve yc = new YieldCurve();
		System.out.println( yc.yield( 2015, 06 ) );
		yc.close();
		System.out.println( "End" );
		
	}
}

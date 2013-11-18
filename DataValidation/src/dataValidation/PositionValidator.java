package dataValidation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import dataWrapper.RecordAbstract;

/**
 * @author Zhenghong Dong
 */
public class PositionValidator extends ValidatorBase {
	public PositionValidator(final String dbServer, final String catalog) {
		super(dbServer, catalog);
		//String name="cmscim";
		Connection conn = null;

		String url = "jdbc:sqlserver://localhost\\SQLEXPRESS;databasename=yatin";
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String userName = "";
		String password = "";
		Statement stmt;
		try
		{

			Class.forName(driver);//.newInstance();
			conn = DriverManager.getConnection(url,userName,password);
			String query = "truncate table cim";
			stmt = conn.createStatement();
			int flag = stmt.executeUpdate(query);
			System.out.println("flag = "+flag);
			conn.close();
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected List<List<RecordAbstract>> readLocalFile(final String localFile, final String tradeDate) {
		return null;}

	@Override
	protected List<List<RecordAbstract>> readBrokerFile(final String brokerFile, final String tradeDate) {
		return null;
	}

}

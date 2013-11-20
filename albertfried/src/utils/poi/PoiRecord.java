package utils.poi;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A record who can write itself into a workbook at certain row.
 * @author Zhenghong Dong
 */
public interface PoiRecord {
	/**
	 * Write the record into the workbook at certain row starting at certain index
	 * @param wb the workbook to write into
	 * @param row the row to write into
	 * @param index start from this index
	 */
	public void writeNext(Workbook wb, Row row, int index);
	
	/**
	 * Returning how many fields are there in this record (so it will occupy this many of columns) 
	 * @return
	 */
	/* public int size(); */
}

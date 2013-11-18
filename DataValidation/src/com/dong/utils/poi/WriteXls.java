package com.dong.utils.poi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Generic class of methods to write things into xls file
 * @author Zhenghong Dong
 */
public class WriteXls {
	/**
	 * Write a list of list of records into xls file
	 * The final format will similar to (different list might have different number of records)
	 *  1st line:      List A						List B
	 *  2nd line:      A's 1st record				B's 1st record
	 *  3rd line:      A's 2nd record 				B's 2nd record
	 *  nth line:      ...							...
	 *  A's last line: A's last record				...
	 *  B's last line:     							B's last record
	 * @param fileName the output file name
	 * @param workbook the output sheet name
	 * @param list list of list to write
	 * @throws Exception
	 */
	public static void append(final String fileName, final String workbook, final List<List<? extends PoiRecord>> list, final List<Integer> sizeList)
			throws Exception {
		try {
			Workbook wb;
			Sheet sheet;
			try {
				wb = WorkbookFactory.create( new File( fileName ) );
			} catch (FileNotFoundException e) {
				wb = new HSSFWorkbook();
			}
			if ((sheet = wb.getSheet( workbook )) == null) {
				sheet = wb.createSheet( workbook );
			}
			final int start = (sheet.getLastRowNum() == 0 ? 0 : (sheet.getLastRowNum() + 1));
			Row row = null;
			final int maxCol = WriteXls.getTotalColumns( list );

			for (int i = start; i < start + maxCol; i++) {
				row = sheet.createRow( i );
				int j = 0;
				for (int p = 0; p < list.size(); p++) {
					List<? extends PoiRecord> recordList = list.get( p );
					if (recordList.size() > i - start) {
						recordList.get( i - start ).writeNext( wb, row, j );
					}
					j += sizeList.get( p ) + 2;
				}
			}

			// format stuff
			for (int i = 0; i < 15; i++) {
				sheet.autoSizeColumn( i );
			}

			// Write the output to a file
			final FileOutputStream fileOut = new FileOutputStream( fileName );
			wb.write( fileOut );
			fileOut.close();

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** Given a list of list of records, find the maximum size of the list */
	private static int getTotalColumns(final List<List<? extends PoiRecord>> list) {
		int ret = 0;
		// wish can use java 8 now...
		for (final List<? extends PoiRecord> poiRecordList : list) {
			if (poiRecordList.size() > ret) {
				ret = poiRecordList.size();
			}
		}
		return ret;
	}
}

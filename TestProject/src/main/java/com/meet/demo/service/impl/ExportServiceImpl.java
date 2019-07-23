package com.meet.demo.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meet.demo.model.Export;
import com.meet.demo.service.AmazonS3ClientService;
import com.meet.demo.service.ExportService;

/**
 * @author Meet 15-July-2019
 */
@Service
public class ExportServiceImpl implements ExportService {

	XSSFWorkbook workbook;
	XSSFSheet spreadsheet;

	@Autowired
	AmazonS3ClientService i;

	public ExportServiceImpl() {
		// Create blank workbook
		workbook = new XSSFWorkbook();

		// Create a blank sheet
		spreadsheet = workbook.createSheet(" Transaction Info ");
	}

	/**
	 * This method writes database column names into a excel file.
	 * 
	 * @param data is a list of objects of type export.
	 */
	public void writeHeader(List<Export> data) {
		try {
			Font font = workbook.createFont();
			font.setFontName("Times New Roman");
			font.setBold(true);
			font.setFontHeightInPoints((short) 11);
			CellStyle style = workbook.createCellStyle();
			style.setFont(font);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Row row = spreadsheet.createRow(0);
			List<String> fieldNames = getFieldNamesForClass(data.get(0).getClass());

			int cellid = 0;
			Cell cell;
			for (String s : fieldNames) {
				cell = row.createCell(cellid++);
				cell.setCellValue(s);
				cell.setCellStyle(style);
			}

		} catch (Exception e) {
			// LOGGER.error(null, e);
		}

	}

	/**
	 * This method writes data into excel file
	 * 
	 * @param data is a list of objects of type export.
	 * 
	 */
	public String writeExcel(List<Export> data) {

		try {
			int rowCount = 1;
			List<String> fieldNames = getFieldNamesForClass(data.get(0).getClass());
			Class<? extends Object> classz = data.get(0).getClass();
			for (Export t : data) {
				int cellid = 0;
				Row row = spreadsheet.createRow(rowCount++);
				for (String fieldName : fieldNames) {
					Cell cell = row.createCell(cellid++);
					Method method = null;
					try {
						method = classz.getMethod("get" + capitalize(fieldName));
					} catch (NoSuchMethodException nme) {
						method = classz.getMethod("get" + fieldName);
					}

					Object value = method.invoke(t, (Object[]) null);
					if (Objects.nonNull(value)) {
						if (value instanceof String) {
							cell.setCellValue((String) value);
						} else if (value instanceof Long) {
							cell.setCellValue((Long) value);
						} else if (value instanceof Integer) {
							cell.setCellValue((Integer) value);
						} else if (value instanceof Double) {
							cell.setCellValue((Double) value);
						} else if (value instanceof BigDecimal) {
							cell.setCellValue(((BigDecimal) value).doubleValue());
						} else if (value instanceof Date) {

						}

					} else {
						cell.setCellValue("");
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return uploadFile();
	}

	private String uploadFile() {
		return (i.uploadFileToS3Bucket(workbook, true));

	}

	// retrieve field names from a POJO class
	private List<String> getFieldNamesForClass(Class<?> clazz) throws Exception {
		List<String> fieldNames = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	// capitalize the first letter of the field name for retrieving value of the
	// field later
	private static String capitalize(String s) {
		if (s.length() == 0) {
			return s;
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

}

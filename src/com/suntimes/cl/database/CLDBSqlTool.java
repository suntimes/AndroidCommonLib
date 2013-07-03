package com.suntimes.cl.database;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.suntimes.cl.util.CLClassUtil;

public class CLDBSqlTool {
	
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String getCreatSql(Class<?> cla){
		CLDBTableMapping tableMapping = cla.getAnnotation(CLDBTableMapping.class);
		
		
		StringBuffer sql = new StringBuffer(" create table ");
		sql.append("'" + tableMapping.tableName() + "'");
		sql.append(" ( ");
		Field[] fields = CLClassUtil.getFields(cla);
		List<CLDBColumnMapping> primaryColumnMappingList = new ArrayList<CLDBColumnMapping>();
		
		for(int i=0;i<fields.length;i++){
			Field field = fields[i];
			CLDBColumnMapping columnMapping = (CLDBColumnMapping)CLClassUtil.getAnnotation(field, cla, CLDBColumnMapping.class);
			if(columnMapping!=null){
				if(i>0){
					sql.append(" , ");
				}
				sql.append(" '" + columnMapping.columnName() + "' " + columnMapping.columnType() + " ");
				if(columnMapping.isPrimary()){
					primaryColumnMappingList.add(columnMapping);
				}
			}
		}
		if(primaryColumnMappingList.size()>0){
			sql.append(" , PRIMARY KEY (");
			for(int i=0;i<primaryColumnMappingList.size();i++){
				CLDBColumnMapping primaryColumnMapping = primaryColumnMappingList.get(i);
				if(i>0){
					sql.append(" , ");
				}
				sql.append("`" + primaryColumnMapping.columnName() + "`");
				
			}
			sql.append(" ) ");
		}
		sql.append(" ); ");
		
		
		System.out.println(sql.toString());
		return sql.toString();
	}
	
	public static String getSelectSql(Class<?> cla){
		String tableName = getTableName(cla);
		String sql = " select i.* from " + tableName + " i; ";
		return sql;
	}
	
	public static String getDeleteSql(Class<?> cla){
		String tableName = getTableName(cla);
		String sql = " delete from " + tableName + " ";
		return sql;
	}
	
	public static Date changeStringToCursorDate(String strValue){
		Date value = null;
		try {
			if(strValue!=null){
				value = DATE_FORMAT.parse(strValue);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return value;
	}	
	
	public static void cursorToBean(Cursor cursor, Object obj){
		Class<?> cla = obj.getClass();
		Field[] fields = CLClassUtil.getFields(cla);
		for(Field field : fields){
			Class<?> fieldType = field.getType();
			CLDBColumnMapping columnMapping = (CLDBColumnMapping)CLClassUtil.getAnnotation(field, cla, CLDBColumnMapping.class);
			
			if(columnMapping!=null){
				String fieldName = field.getName();
				String columnName = columnMapping.columnName();
				int columnIndex = cursor.getColumnIndex(columnName);
				//Log.i("sqltool", "columnName >> " + columnName + "  columnIndex >>   " + columnIndex + "   type >>>  " + field.getType());
				boolean isSqlPojo = (obj instanceof CLDBSqlPojo);
				CLDBSqlPojo sqlPojo = null;
				if(isSqlPojo){
					sqlPojo = (CLDBSqlPojo)obj;
				}
				if(columnIndex>=0){
					try{
						if(fieldType.isAssignableFrom(String.class)){
							String value = cursor.getString(cursor.getColumnIndex(columnName));
							if(isSqlPojo){
								sqlPojo.set(fieldName, value);
							}else{
								CLClassUtil.setValue(obj, field, value);
							}
							
						}else if(fieldType.isAssignableFrom(Integer.class) ||  fieldType.isAssignableFrom(int.class)){
							Integer value = cursor.getInt(columnIndex);
							if(isSqlPojo){
								sqlPojo.set(fieldName, value);
							}else{
								CLClassUtil.setValue(obj, field, value);
							}
							
						}else if(fieldType.isAssignableFrom(Float.class) ||  fieldType.isAssignableFrom(float.class)){
							Float value = cursor.getFloat(cursor.getColumnIndex(columnName));
							
							if(isSqlPojo){
								sqlPojo.set(fieldName, value);
							}else{
								CLClassUtil.setValue(obj, field, value);
							}
						}else if(fieldType.isAssignableFrom(Long.class) ||  fieldType.isAssignableFrom(long.class)){
							Long value = cursor.getLong(cursor.getColumnIndex(columnName));
							
							if(isSqlPojo){
								sqlPojo.set(fieldName, value);
							}else{
								CLClassUtil.setValue(obj, field, value);
							}
						}else if(fieldType.isAssignableFrom(Date.class)){
							String strValue = cursor.getString(cursor.getColumnIndex(columnName));
							Date value = null;
							try {
								if(strValue!=null){
									String dateFormat = columnMapping.dateFormat();
									SimpleDateFormat ssdf = null;
									if(dateFormat!=null && !"".equals(dateFormat)){
										ssdf = new SimpleDateFormat(dateFormat);
									}else{
										ssdf = DATE_FORMAT;
									}
									value = ssdf.parse(strValue);
									if(isSqlPojo){
										sqlPojo.set(fieldName, value);
									}else{
										CLClassUtil.setValue(obj, field, value);
									}
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}else if(field.getType().isEnum()){
							String strValue = cursor.getString(cursor.getColumnIndex(columnName));
							Method method = field.getType().getDeclaredMethod("getByString", new Class[]{String.class});
							if(method!=null){
								Object enumValue = method.invoke(null, new String[]{strValue});
								
								if(isSqlPojo){
									sqlPojo.set(fieldName, enumValue);
								}else{
									CLClassUtil.setValue(obj, field, enumValue);
								}
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void beanToContentValues(Object obj, ContentValues cv){
		Class<?> cla = obj.getClass();
		Field[] fields = CLClassUtil.getFields(cla);
		
		for(Field field : fields){
			CLDBColumnMapping columnMapping = (CLDBColumnMapping)CLClassUtil.getAnnotation(field, cla, CLDBColumnMapping.class);
			if(columnMapping!=null){
				String columnName = columnMapping.columnName();
				Object value = CLClassUtil.getValue(obj, field);
				String strValue = null;
				if(value!=null){
					if(value instanceof Date){
						strValue = DATE_FORMAT.format((Date)value);
					}else{
						strValue = value.toString();
					}
				}
				cv.put(columnName, strValue);
			}
		}
	}
	
	/*
	 * LK
	 * */
	public static void putBeanToContentValue(String name,Object value,ContentValues cv){
		if(value!=null){
			if(value instanceof Date){
				String strValue = DATE_FORMAT.format((Date)value);
				cv.put(name, strValue);
			}else if(value instanceof String){
				//strValue = value.toString();
				cv.put(name, (String)value);
			}else if(value instanceof Integer){
				cv.put(name, (Integer)value);
			}else if(value instanceof Boolean){
				cv.put(name, (Boolean)value);
			}else if(value instanceof Byte){
				cv.put(name, (Byte)value);
			}else if(value instanceof byte[]){
				cv.put(name, (byte[])value);
			}else if(value instanceof Double){
				cv.put(name, (Double)value);
			}else if(value instanceof Float){
				cv.put(name, (Float)value);
			}else if(value instanceof Long){
				cv.put(name, (Long)value);
			}else if(value instanceof Short){
				cv.put(name, (Short)value);
			}else{
				cv.put(name, value.toString());
			}
		}
		//cv.put(name, strValue);
	}
	
	public static String getTableName(Class<?> cla){
		String tableName = null;
		CLDBTableMapping tableMapping = cla.getAnnotation(CLDBTableMapping.class);
		if(tableMapping!=null){
			tableName = tableMapping.tableName();
		}
		return tableName;
	}
	
}

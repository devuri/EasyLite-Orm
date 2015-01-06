package com.easylite;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easylite.annotation.Id;
import com.easylite.exception.EasyLiteSqlException;

public final class DaoImpl<K,E> implements Dao<K, E>{
	private final SQLiteDatabase db;
	private final Class<E> type;
	
	public DaoImpl (EasyLiteOpenHelper openHelper,Class<E> type){
		this.db = openHelper.getWritableDatabase();
		this.type = type;
	}
	
	public long create(E entity) throws EasyLiteSqlException {
		if (entity == null)
			throw new NullPointerException("Null Entity Supplied");
		
		String name = Table.getEntityName(entity.getClass());
		
		ContentValues values = new ContentValues();
		Field[] fields = entity.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				putContentValue(values, field, entity);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return this.db.insert(name, null, values);
	}
	
	public int delete(E entity) throws EasyLiteSqlException {
		if (entity == null)
			throw new NullPointerException("null Entity Supplied");
		String tableName = Table.getEntityName(type);
		Field[] fields = type.getFields();
		for (Field field : fields){
			if(field.getAnnotation(Id.class) != null){
				try {
					String primaryKeyType = Table.getPrimaryKeyName(type);
					@SuppressWarnings("unchecked")
					K key = (K) field.get(entity);
					return db.delete(tableName, primaryKeyType + "=?", new String[]{parsePrimaryKey(primaryKeyType, key)});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	public int update(E entity) throws EasyLiteSqlException{
		return db.update(type.getSimpleName(), null, null, null);
	}

	public E findById(K key) throws EasyLiteSqlException {
		String primaryKeyName = Table.getPrimaryKeyName(type);
		String primaryKeyType = Table.getPrimaryKeyTypeName(type);
		String table = Table.getEntityName(type);
		
		String sql = String.format("SELECT * FROM %s WHERE %s=?", table,primaryKeyName);
		String args = parsePrimaryKey(primaryKeyType, key);
		
		Cursor cursor = db.rawQuery(sql, new String[]{args});
		cursor.moveToFirst();
		try {
			E entity = type.newInstance();
			Field[] fields = type.getFields();
			for (Field field : fields)
				this.setEntityFields(cursor, field, entity);
			
			return entity;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<E> findAll() throws EasyLiteSqlException {
		String table = Table.getEntityName(type);
		List<E> results = new ArrayList<E> ();
		Cursor cursor = db.query(table, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			try {
				E entity = type.newInstance();
				Field[] fields = type.getFields();
				for (Field field : fields)
					setEntityFields(cursor, field, entity);
				results.add(entity);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return results;
	}
	
	public boolean isExist(E entity) throws EasyLiteSqlException {
		return false;
	}

	public SQLiteDatabase getSqLiteDatabase() {
		return db;
	}

	/**
	 * Parse primary key to String
	 * @author Mario Dennis
	 * @param primaryKeyType
	 * @param key
	 * @return primary key String
	 */
	private  String parsePrimaryKey (String primaryKeyType,K key){
		if (primaryKeyType.equals("int") || primaryKeyType.equals(Integer.class.getName()))
			return Integer.toString((Integer) key);
		else if (primaryKeyType.equals("double") || primaryKeyType.equals(Double.class.getName()))
			return Double.toString((Double) key);
		else if (primaryKeyType.equals("float") || primaryKeyType.equals(Float.class.getName()))
			return Float.toString((Float) key);
		else
			return key.toString();
	}
	
	/**
	 * Sets entity fields reflectively.
	 * @author Mario Dennis
	 * @param cursor
	 * @param field
	 * @param entity
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void setEntityFields (Cursor cursor,Field field,E entity) throws IllegalArgumentException, IllegalAccessException{
		String type = field.getType().getName();
		if (type.equals("int") || type.equals(Integer.class.getName()))
			field.setInt(entity, cursor.getInt(cursor.getColumnIndex(field.getName())));
		
		else if(type.equals(String.class.getName()))
			field.set(entity, cursor.getString(cursor.getColumnIndex(field.getName())));
		
		else if (type.equals("double") || type.equals(Double.class.getName()))
			field.setDouble(entity, cursor.getDouble(cursor.getColumnIndex(field.getName())));
		
		else if (type.equals("boolean") || type.equals(Boolean.class.getName()))
			field.setBoolean(entity, (cursor.getInt(cursor.getColumnIndex(field.getName())) == 1) ? true: false);
		
		else if (type.equals(Date.class.getName())){
			long d = cursor.getLong(cursor.getColumnIndex(field.getName()));
			Date date = new Date(d);
			field.set(entity, date);
		}
		else if (type.equals("float") || type.equals(Float.class.getName()))
			field.setFloat(entity, cursor.getFloat(cursor.getColumnIndex(field.getName())));
	}
	
	/**
	 * Add field value to contentValue
	 * @author Mario Dennis
	 * @param values
	 * @param field
	 * @param entity
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void putContentValue (ContentValues values,Field field,E entity) throws IllegalArgumentException, IllegalAccessException{
		String name = field.getName();
		String type = field.getType().getName();
		if (type.equals("int") || type.equals(Integer.class.getName()))
			values.put(name, field.getInt(entity));
		
		else if (type.equals(String.class.getName()))
			values.put(name, (String) field.get(entity));
		
		else if (type.equals("double") || type.equals(Double.class.getName()))
			values.put(name, field.getDouble(entity));
		
		else if (type.equals("boolean") || type.equals(Boolean.class.getName()))
			values.put(name, field.getBoolean(entity));
		
		else if (type.equals("char") || type.equals(Character.class.getName()))
			values.put(name,Character.toString(field.getChar(entity)));
		
		else if (type.equals("float") || type.equals(Float.class.getName()))
			values.put(name,field.getFloat(entity));
		
		else if (type.equals(Date.class.getName()))
			values.put(name,(String) field.get(entity));
	}

}
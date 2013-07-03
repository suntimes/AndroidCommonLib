package com.suntimes.cl.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Zheng
 * @version 1.0
 * @CreateDate 2013-4-24
 */
public class CLClassUtil {

	/**
	 * get getter or setter method name by arrtibute name
	 * 
	 * @param attributeName
	 * @param type 0:setter 1:getter
	 * @return
	 */
	public static String getMethodName(String attributeName, int type) {
		StringBuffer methodName = new StringBuffer("");
		int index_ = attributeName.indexOf("_");
//		View

		if (type == 0) {
			methodName.append("set");
		} else {
			methodName.append("get");
		}
		attributeName = attributeName.replaceAll(" ", "");
		attributeName = attributeName.replaceAll("\\(", "");
		attributeName = attributeName.replaceAll("\\)", "");
		attributeName = attributeName.replaceAll("\\.", "");
		methodName.append((attributeName.charAt(0) + "").toUpperCase());
		if (index_ > 0 && index_ < attributeName.length() - 1) {
			methodName.append(attributeName.substring(1, index_));
			methodName.append((attributeName.charAt(index_ + 1) + "").toUpperCase());
			if (index_ + 2 < attributeName.length()) {
				methodName.append(attributeName.substring(index_ + 2));
			}
		} else {
			methodName.append(attributeName.substring(1));
		}

		return methodName.toString();
	}

	public static String getSetterMethodName(String attributeName) {
		return CLClassUtil.getMethodName(attributeName, 0);
	}

	public static String getGetterMethodName(String attributeName) {
		return CLClassUtil.getMethodName(attributeName, 1);
	}

	public static Method getGetterMethod(Class<?> cs, String attributeName) {
		String getterName = getGetterMethodName(attributeName);
		Method getter = null;
		Class<?> c = cs;
		while (getter == null && c != null) {
			try {
				getter = c.getDeclaredMethod(getterName, new Class[0]);
			} catch (Exception e) {}
			c = c.getSuperclass();
		}
		return getter;
	}

	public static void setValue(Object obj, Field field, Object value) {

		if (obj != null && field != null && value != null) {

			String stterName = getSetterMethodName(field.getName());
			Method method = null;
			Class<?> c = obj.getClass();

			while (method == null && c != null) {
				try {
					method = c.getDeclaredMethod(stterName, new Class[] { field.getType() });
				} catch (Exception e) {}
				c = c.getSuperclass();
			}
			if (method == null) {
				Class<?> fieldClass = field.getType();
				if (fieldClass.isAssignableFrom(Boolean.class) || fieldClass.isAssignableFrom(boolean.class)) {
					if (stterName.startsWith("setIs")) {
						stterName = stterName.substring(5);
						stterName = "set" + stterName;
					}

					c = obj.getClass();
					while (method == null && c != null) {
						try {
							method = c.getDeclaredMethod(stterName, new Class[] { field.getType() });
						} catch (Exception e) {}
						c = c.getSuperclass();
					}
				}
			}

			if (method != null) {
				try {
					method.invoke(obj, new Object[] { value });
				} catch (IllegalArgumentException e) {
					// e.printStackTrace();
				} catch (IllegalAccessException e) {
					// e.printStackTrace();
				} catch (InvocationTargetException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public static Object getValue(Object obj, Field field) {
		Object value = null;
		Class<?> c = obj.getClass();
		Method method = getGetterMethod(c, field.getName());

		if (method != null) {
			try {
				value = method.invoke(obj, new Object[] {});
			} catch (IllegalArgumentException e) {
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				// e.printStackTrace();
			} catch (InvocationTargetException e) {
				// e.printStackTrace();
			}
		}
		return value;
	}

	public static Field[] getFields(Class<?> cs) {
		List<Field> fieldList = new ArrayList<Field>();
		Class<?> c = cs;
		while (c != null) {
			Field[] fields = c.getDeclaredFields();
			for (Field field : fields) {
				fieldList.add(field);
			}
			c = c.getSuperclass();

		}
		return fieldList.toArray(new Field[fieldList.size()]);
	}

	public static Annotation getAnnotation(Field field, Class<?> objectClass,
			Class<? extends Annotation> annotationClass) {
		Annotation annotation = null;
		annotation = field.getAnnotation(annotationClass);
		if (annotation == null) {
			Method getterMehtod = getGetterMethod(objectClass, field.getName());
			if (getterMehtod != null) {
				annotation = getterMehtod.getAnnotation(annotationClass);
			}
		}

		return annotation;
	}
}

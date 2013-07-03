package com.suntimes.cl.xml;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CLXmlMapping {
	public static int TYPE_OBJECT = 1;
	public static int TYPE_COLLECTION = 2;
	public String tagPath();
	public int type() default TYPE_OBJECT;
	public String genericType() default "";
	public String attributeName() default "";
	public String trueValue() default "true";
	public String dateFormat() default "yyyy/MM/dd HH:mm:ss";
}

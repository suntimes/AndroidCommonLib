package com.suntimes.cl.database;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CLDBColumnMapping {
	public String columnName();
	public String columnType();
	public boolean isPrimary() default false;
	public String trueValue() default "";
	public String dateFormat() default "";
}

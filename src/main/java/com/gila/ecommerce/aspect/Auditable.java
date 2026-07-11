package com.gila.ecommerce.aspect;

import com.gila.ecommerce.util.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to trigger automatic asynchronous audit logging using Aspect-Oriented Programming (AOP).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Define the typesafe audit action categorization for this method.
     * @return audit action categorization
     */
    AuditAction action();
}

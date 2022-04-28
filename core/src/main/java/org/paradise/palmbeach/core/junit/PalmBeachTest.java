package org.paradise.palmbeach.core.junit;

import org.paradise.palmbeach.utils.junit.ParadiseTest;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ParadiseTest
public @interface PalmBeachTest {
}

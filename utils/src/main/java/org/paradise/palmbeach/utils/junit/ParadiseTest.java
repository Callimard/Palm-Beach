package org.paradise.palmbeach.utils.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(ParadiseTestExtension.class)
@ExtendWith(MockitoExtension.class)
public @interface ParadiseTest {
}

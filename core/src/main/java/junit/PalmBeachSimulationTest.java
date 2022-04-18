package junit;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@PalmBeachTest
@ExtendWith(PalmBeachSimulationTestExtension.class)
public @interface PalmBeachSimulationTest {
}

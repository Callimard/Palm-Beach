package junit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(PalmBeachTestExtension.class)
@ExtendWith(MockitoExtension.class)
public @interface PalmBeachTest {
}

package common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Tools {

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> extractClass(String className) throws ClassNotFoundException {
        return (Class<? extends T>) Class.forName(className);
    }

}

package org.paradise.palmbeach.utils.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionTools {

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> extractClass(@NonNull String className) throws ClassNotFoundException {
        return (Class<? extends T>) Class.forName(className);
    }

}

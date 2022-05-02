package org.paradise.palmbeach.basic.messaging;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Message<T> {

    @Getter
    private final T content;
}

package simulation.configuration;

import com.typesafe.config.Config;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import simulation.configuration.exception.GenerationFailedException;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PalmBeachConfiguration<T> {

    @Getter
    @NonNull
    private final Config baseConfig;

    public abstract T generate() throws GenerationFailedException;

}

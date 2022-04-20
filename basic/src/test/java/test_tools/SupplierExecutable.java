package test_tools;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import scheduler.executor.Executable;

@RequiredArgsConstructor
public class SupplierExecutable implements Executable {
    @Getter
    @NonNull
    private final Executable executable;

    @Getter
    private boolean executed = false;

    @Override
    public void execute() throws Exception {
        executable.execute();
        executed = true;
    }
}

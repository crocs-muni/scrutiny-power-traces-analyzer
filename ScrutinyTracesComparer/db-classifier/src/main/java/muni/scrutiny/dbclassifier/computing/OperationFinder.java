package muni.scrutiny.dbclassifier.computing;

import muni.scrutiny.dbclassifier.computing.models.OperationFinderResult;
import muni.scrutiny.traces.models.Trace;

import java.util.List;

public interface OperationFinder {
    OperationFinderResult findOperations(Trace unknownTrace, Trace operationTrace, int takeNth);
}

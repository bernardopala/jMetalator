package org.uma.jmetal.algorithm.util;

import org.uma.jmetal.solution.Solution;

import java.util.List;

public interface CurrentSolutionSetReceiver <S> {
    void ReceiveCurrentSolutionSet(List<S> solutionSet);
}

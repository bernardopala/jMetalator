package jmetalhelpers;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ2;
import org.uma.jmetal.util.ProblemUtils;

public class ProblemHelper {
    public static <S> Problem<S> loadProblem(String problemName, int objectiveCount){
        if (problemName.equalsIgnoreCase("org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1")){
            return (Problem<S>) new DTLZ1(objectiveCount + 4, objectiveCount);
        }
        else if (problemName.equalsIgnoreCase("org.uma.jmetal.problem.multiobjective.dtlz.DTLZ2")){
            return (Problem<S>) new DTLZ2(objectiveCount + 9, objectiveCount);
        }
        return ProblemUtils.loadProblem(problemName);
    }
}

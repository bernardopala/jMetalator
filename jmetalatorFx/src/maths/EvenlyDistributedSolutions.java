package maths;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.distance.Distance;
import org.uma.jmetal.util.distance.impl.EuclideanDistanceBetweenSolutionAndASolutionListInObjectiveSpace;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.referencePoint.impl.IdealPoint;

import java.util.ArrayList;
import java.util.List;

public class EvenlyDistributedSolutions {

    public static <S extends Solution<?>> List<S> get(List<S> solutionList, int newSolutionListSize) {
        List<S> resultSolutionList = new ArrayList<>(newSolutionListSize) ;
        if (solutionList == null) {
            throw new JMetalException("The solution list is null") ;
        }

        if (solutionList.size() > 0) {
            int numberOfObjectives = solutionList.get(0).getNumberOfObjectives() ;
            if (numberOfObjectives == 2) {
                twoObjectivesCase(solutionList, resultSolutionList, newSolutionListSize) ;
            } else {
                moreThanTwoObjectivesCase(solutionList, resultSolutionList, newSolutionListSize) ;
            }
        }
        return resultSolutionList  ;
    }

    private static <S extends Solution<?>> void twoObjectivesCase(
            List<S> solutionList,
            List<S> resultSolutionList,
            int newSolutionListSize) {
        double[][] lambda = new double[newSolutionListSize][2] ;

        // Compute the weight vectors
        for (int i = 0; i < newSolutionListSize; i++) {
            double a = 1.0 * i / (newSolutionListSize - 1);
            lambda[i][0] = a;
            lambda[i][1] = 1 - a;
        }

        IdealPoint idealPoint = new IdealPoint(2) ;
        solutionList.stream().forEach(solution -> idealPoint.update(solution));

        // Select the best solution for each weight vector
        for (int i = 0; i < newSolutionListSize; i++) {
            S currentBest = solutionList.get(0);
            double value = scalarizingFitnessFunction(currentBest, lambda[i], idealPoint);
            for (int j = 1; j < solutionList.size(); j++) {
                double aux = scalarizingFitnessFunction(solutionList.get(j),lambda[i], idealPoint); // we are looking for the best for the weight i
                if (aux < value) { // solution in position j is better!
                    value = aux;
                    currentBest = solutionList.get(j);
                }
            }
            @SuppressWarnings("unchecked")
            S copy = (S) currentBest.copy() ;
            resultSolutionList.add(copy);
        }
    }

    private static <S extends Solution<?>> void moreThanTwoObjectivesCase(
            List<S> solutionList,
            List<S> resultSolutionList,
            int newSolutionListSize) {

        Distance<S, List<S>> distance =
                new EuclideanDistanceBetweenSolutionAndASolutionListInObjectiveSpace() ;

        int randomIndex = JMetalRandom.getInstance().nextInt(0, solutionList.size() - 1) ;

        // create a list containing all the solutions but the selected one (only references to them)
        List<S> candidate = new ArrayList<>();
        resultSolutionList.add(solutionList.get(randomIndex)) ;

        for (int i = 0; i< solutionList.size(); i++) {
            if (i != randomIndex)
                candidate.add(solutionList.get(i));
        }

        while (resultSolutionList.size() < newSolutionListSize && candidate.size() > 0) {
            int index = 0;
            S selected = candidate.get(0); // it should be a next! (n <= population size!)
            double aux = distance.getDistance(selected, solutionList);
            int i = 1;
            while (i < candidate.size()) {
                S nextCandidate = candidate.get(i);
                double  distanceValue = distance.getDistance(nextCandidate, solutionList);
                if (aux < distanceValue) {
                    index = i;
                    aux = distanceValue ;
                }
                i++;
            }

            // add the selected to res and remove from candidate list
            S removedSolution = candidate.remove(index) ;
            @SuppressWarnings("unchecked")
            S copy = (S) removedSolution.copy() ;
            resultSolutionList.add(copy);
        }
    }

    private static <S extends Solution<?>> double scalarizingFitnessFunction(
            S currentBest,
            double[] lambda,
            IdealPoint idealPoint) {

        double maxFun = -1.0e+30;

        for (int n = 0; n < idealPoint.getNumberOfObjectives(); n++) {
            double diff = Math.abs(currentBest.getObjective(n) - idealPoint.getObjective(n));

            double functionValue;
            if (lambda[n] == 0) {
                functionValue = 0.0001 * diff;
            } else {
                functionValue = diff * lambda[n];
            }
            if (functionValue > maxFun) {
                maxFun = functionValue;
            }
        }

        return maxFun;
    }
}

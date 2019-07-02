package jmetalhelpers.algorithms;

import jmetalhelpers.ProblemHelper;
import org.uma.jmetal.algorithm.multiobjective.epsilonboxmoea.EpsilonBoxMOEA;
import org.uma.jmetal.algorithm.multiobjective.epsilonboxmoea.EpsilonBoxMOEABuilder;
import org.uma.jmetal.algorithm.multiobjective.nmoea.nMOEA;
import org.uma.jmetal.algorithm.multiobjective.nmoea.nMOEABuilder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.Problem;

import java.util.HashMap;
import java.util.Map;

public class EpsilonBoxMOEAManager {

    String problemName;
    double crossoverProbability;
    double crossoverDistributionIndex;
    double mutationProbability;
    double mutationDistributionIndex;
    int maxEvaluations;
    int populationSize;
    int objectiveCount;
    Double epsilonFactor;

    public EpsilonBoxMOEAManager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = EpsilonBoxMOEAManager.getDefaultParams();

        setParameters(params);
    }

    public EpsilonBoxMOEAManager(){

        setParameters(EpsilonBoxMOEAManager.getDefaultParams());
    }

    public void setParameters(Map<String, String> params){
        problemName = createProblemUrl(params.get("problemName"));
        crossoverProbability = Double.valueOf(params.get("crossoverProbability"));
        crossoverDistributionIndex = 20.0D; // Double.valueOf(params.get("crossoverDistributionIndex"));
        mutationProbability = Double.valueOf(params.get("mutationProbability"));
        mutationDistributionIndex = 20.0D; // Double.valueOf(params.get("mutationDistributionIndex"));
        maxEvaluations = Integer.valueOf(params.get("maxEvaluations"));
        populationSize = Integer.valueOf(params.get("populationSize"));
        objectiveCount = Integer.valueOf(params.get("objectiveCount"));
        epsilonFactor = Double.valueOf(params.get("epsilonFactor"));
    }

    public EpsilonBoxMOEA Create() {
        Problem problem = ProblemHelper.loadProblem(problemName, objectiveCount);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        return (new EpsilonBoxMOEABuilder(problem, crossover, mutation)).setMaxEvaluations(maxEvaluations).setPopulationSize(populationSize).setEpsilon(epsilonFactor).build();
    }

    private String createProblemUrl(String problemName){
        return "org.uma.jmetal.problem.multiobjective." + problemName;
    }

    public static Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put("problemName", "ConstrEx");
        params.put("crossoverProbability", "0.7D");
        //params.put("crossoverDistributionIndex", "20.0D");
        params.put("mutationProbability", "0.5D");
        //params.put("mutationDistributionIndex", "20.0D");
        params.put("maxEvaluations", "1000");
        params.put("populationSize", "100");
        params.put("objectiveCount", "2");
        params.put("epsilonFactor", "0.05");

        return params;
    }
}

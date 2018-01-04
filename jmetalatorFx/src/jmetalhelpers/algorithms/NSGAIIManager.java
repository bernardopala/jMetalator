package jmetalhelpers.algorithms;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.Map;

public class NSGAIIManager {

    String problemName;
    double crossoverProbability;
    double crossoverDistributionIndex;
    double mutationProbability;
    double mutationDistributionIndex;
    int maxEvaluations;
    int populationSize;

    public NSGAIIManager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = NSGAIIManager.getDefaultParams();

        setParameters(params);
    }

    public NSGAIIManager(){

        setParameters(NSGAIIManager.getDefaultParams());
    }

    public void setParameters(Map<String, String> params){
        problemName = params.get("problemName");
        crossoverProbability = Double.valueOf(params.get("crossoverProbability"));
        crossoverDistributionIndex = Double.valueOf(params.get("crossoverDistributionIndex"));
        mutationProbability = Double.valueOf(params.get("mutationProbability"));
        mutationDistributionIndex = Double.valueOf(params.get("mutationDistributionIndex"));
        maxEvaluations = Integer.valueOf(params.get("maxEvaluations"));
        populationSize = Integer.valueOf(params.get("populationSize"));
    }

    public NSGAII Create() {
        Problem problem = ProblemUtils.loadProblem(problemName);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        BinaryTournamentSelection selection = new BinaryTournamentSelection(new RankingAndCrowdingDistanceComparator());
        return (new NSGAIIBuilder(problem, crossover, mutation)).setSelectionOperator(selection).setMaxEvaluations(maxEvaluations).setPopulationSize(populationSize).build();
    }

    public static Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put("problemName", "org.uma.jmetal.problem.multiobjective.ConstrEx");
        params.put("crossoverProbability", "0.7D");
        params.put("crossoverDistributionIndex", "20.0D");
        params.put("mutationProbability", "0.5D");
        params.put("mutationDistributionIndex", "20.0D");
        params.put("maxEvaluations", "100000");
        params.put("populationSize", "100");

        return params;
    }
}

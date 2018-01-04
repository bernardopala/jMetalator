package jmetalhelpers.algorithms;

import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.Map;

public class SPEA2Manager {

    String problemName;
    double crossoverProbability;
    double crossoverDistributionIndex;
    double mutationProbability;
    double mutationDistributionIndex;
    int maxEvaluations;
    int populationSize;
    int archiveSize;

    public SPEA2Manager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = SPEA2Manager.getDefaultParams();

        setParameters(params);
    }

    public SPEA2Manager(){
        setParameters(SPEA2Manager.getDefaultParams());
    }

    public void setParameters(Map<String, String> params){
        problemName = params.get("problemName");
        crossoverProbability = Double.valueOf(params.get("crossoverProbability"));
        crossoverDistributionIndex = Double.valueOf(params.get("crossoverDistributionIndex"));
        mutationProbability = Double.valueOf(params.get("mutationProbability"));
        mutationDistributionIndex = Double.valueOf(params.get("mutationDistributionIndex"));
        maxEvaluations = Integer.valueOf(params.get("maxEvaluations"));
        populationSize = Integer.valueOf(params.get("populationSize"));
        archiveSize = Integer.valueOf(params.get("archiveSize"));
    }

    public SPEA2 Create() {
        Problem problem = ProblemUtils.loadProblem(problemName);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        BinaryTournamentSelection selection = new BinaryTournamentSelection(new RankingAndCrowdingDistanceComparator());
        return (new SPEA2Builder(problem, crossover, mutation)).setSelectionOperator(selection).setMaxIterations(maxEvaluations).setPopulationSize(populationSize).setArchiveSize(archiveSize).build();
    }

    public static Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put("problemName", "org.uma.jmetal.problem.multiobjective.ConstrEx");
        params.put("crossoverProbability", "0.7D");
        params.put("crossoverDistributionIndex", "20.0D");
        params.put("mutationProbability", "0.5D");
        params.put("mutationDistributionIndex", "20.0D");
        params.put("maxEvaluations", "10000");
        params.put("populationSize", "100");
        params.put("archiveSize", "100");

        return params;
    }
}

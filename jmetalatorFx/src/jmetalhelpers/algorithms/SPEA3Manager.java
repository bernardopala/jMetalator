package jmetalhelpers.algorithms;

import jmetalhelpers.ProblemHelper;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.algorithm.multiobjective.spea3.SPEA3;
import org.uma.jmetal.algorithm.multiobjective.spea3.SPEA3Builder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.Map;

public class SPEA3Manager {

    private String problemName;
    private double crossoverProbability;
    private double crossoverDistributionIndex;
    private double mutationProbability;
    private double mutationDistributionIndex;
    private int maxEvaluations;
    private int populationSize;
    private int archiveSize;
    int objectiveCount;

    public SPEA3Manager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = SPEA2Manager.getDefaultParams();

        setParameters(params);
    }

    public SPEA3Manager(){
        setParameters(SPEA2Manager.getDefaultParams());
    }

    public void setParameters(Map<String, String> params){
        problemName = createProblemUrl(params.get("problemName"));
        crossoverProbability = Double.valueOf(params.get("crossoverProbability"));
        crossoverDistributionIndex = 20.0D; // Double.valueOf(params.get("crossoverDistributionIndex"));
        mutationProbability = Double.valueOf(params.get("mutationProbability"));
        mutationDistributionIndex = 20.0D; // Double.valueOf(params.get("mutationDistributionIndex"));
        maxEvaluations = Integer.valueOf(params.get("maxEvaluations"));
        populationSize = Integer.valueOf(params.get("populationSize"));
        archiveSize = Integer.valueOf(params.get("archiveSize"));
        objectiveCount = Integer.valueOf(params.get("objectiveCount"));
    }

    public SPEA3 Create() {
        Problem problem = ProblemHelper.loadProblem(problemName, objectiveCount);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        BinaryTournamentSelection selection = new BinaryTournamentSelection(new RankingAndCrowdingDistanceComparator());
        return (new SPEA3Builder(problem, crossover, mutation)).setSelectionOperator(selection).setMaxIterations(maxEvaluations).setPopulationSize(populationSize).setArchiveSize(archiveSize).build();
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
        params.put("maxEvaluations", "10000");
        params.put("populationSize", "100");
        params.put("archiveSize", "100");
        params.put("objectiveCount", "2");


        return params;
    }
}

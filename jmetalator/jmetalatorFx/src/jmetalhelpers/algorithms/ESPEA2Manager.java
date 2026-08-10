package jmetalhelpers.algorithms;

import jmetalhelpers.ProblemHelper;
import org.uma.jmetal.algorithm.multiobjective.espea2.ESPEA2;
import org.uma.jmetal.algorithm.multiobjective.espea2.ESPEA2Builder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.Map;

public class ESPEA2Manager {

    private String problemName;
    private double crossoverProbability;
    private double crossoverDistributionIndex;
    private double mutationProbability;
    private double mutationDistributionIndex;
    private int maxEvaluations;
    private int populationSize;
    private int archiveSize;
    private Double epsilonFactor;
    int objectiveCount;

    public ESPEA2Manager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = ESPEA2Manager.getDefaultParams();

        setParameters(params);
    }

    public ESPEA2Manager(){
        setParameters(ESPEA2Manager.getDefaultParams());
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
        epsilonFactor = Double.valueOf(params.get("epsilonFactor"));
        objectiveCount = Integer.valueOf(params.get("objectiveCount"));
    }

    public ESPEA2 Create() {
        Problem problem = ProblemHelper.loadProblem(problemName, objectiveCount);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        BinaryTournamentSelection selection = new BinaryTournamentSelection(new RankingAndCrowdingDistanceComparator());
        return (new ESPEA2Builder(problem, crossover, mutation)).setSelectionOperator(selection).setMaxIterations(maxEvaluations)
                .setPopulationSize(populationSize).setArchiveSize(archiveSize).setEpsilonFactor(epsilonFactor).build();
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
        params.put("archiveSize", "100");
        params.put("epsilonFactor", "0.1D");
        params.put("objectiveCount", "2");


        return params;
    }
}

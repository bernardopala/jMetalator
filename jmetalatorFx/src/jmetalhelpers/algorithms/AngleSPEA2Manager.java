package jmetalhelpers.algorithms;

import org.uma.jmetal.algorithm.multiobjective.anglespea2.AngleSPEA2;
import org.uma.jmetal.algorithm.multiobjective.anglespea2.AngleSPEA2Builder;
import org.uma.jmetal.algorithm.multiobjective.aspea2.ASPEA2;
import org.uma.jmetal.algorithm.multiobjective.aspea2.ASPEA2Builder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.util.HashMap;
import java.util.Map;

public class AngleSPEA2Manager {

    private String problemName;
    private double crossoverProbability;
    private double crossoverDistributionIndex;
    private double mutationProbability;
    private double mutationDistributionIndex;
    private int maxEvaluations;
    private int populationSize;
    private int archiveSize;
    private Double angleFactor;

    public AngleSPEA2Manager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = AngleSPEA2Manager.getDefaultParams();

        setParameters(params);
    }

    public AngleSPEA2Manager(){
        setParameters(AngleSPEA2Manager.getDefaultParams());
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
        angleFactor = Double.valueOf(params.get("angleFactor"));
    }

    public AngleSPEA2 Create() {
        Problem problem = ProblemUtils.loadProblem(problemName);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        BinaryTournamentSelection selection = new BinaryTournamentSelection(new RankingAndCrowdingDistanceComparator());
        return (new AngleSPEA2Builder(problem, crossover, mutation)).setSelectionOperator(selection).setMaxIterations(maxEvaluations)
                .setPopulationSize(populationSize).setArchiveSize(archiveSize).setAngleFactor(angleFactor).build();
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
        params.put("angleFactor", "0.1D");

        return params;
    }
}

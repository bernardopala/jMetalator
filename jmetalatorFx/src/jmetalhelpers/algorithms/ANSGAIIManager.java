package jmetalhelpers.algorithms;

import org.uma.jmetal.algorithm.multiobjective.ansgaii.ANSGAII;
import org.uma.jmetal.algorithm.multiobjective.ansgaii.ANSGAIIBuilder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.RankingComparator;
import org.uma.jmetal.util.comparator.RelaxationType;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.HashMap;
import java.util.Map;

public class ANSGAIIManager {

    String problemName;
    double crossoverProbability;
    double crossoverDistributionIndex;
    double mutationProbability;
    double mutationDistributionIndex;
    int maxEvaluations;
    int populationSize;
    Double alphaFactor;

    public ANSGAIIManager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = ANSGAIIManager.getDefaultParams();

        setParameters(params);
    }

    public ANSGAIIManager(){

        setParameters(ANSGAIIManager.getDefaultParams());
    }

    public void setParameters(Map<String, String> params){
        problemName = createProblemUrl(params.get("problemName"));
        crossoverProbability = Double.valueOf(params.get("crossoverProbability"));
        crossoverDistributionIndex = 20.0D; // Double.valueOf(params.get("crossoverDistributionIndex"));
        mutationProbability = Double.valueOf(params.get("mutationProbability"));
        mutationDistributionIndex = 20.0D; // Double.valueOf(params.get("mutationDistributionIndex"));
        maxEvaluations = Integer.valueOf(params.get("maxEvaluations"));
        populationSize = Integer.valueOf(params.get("populationSize"));
        alphaFactor = Double.valueOf(params.get("alphaFactor"));
    }

    public ANSGAII Create() {
        Problem problem = ProblemUtils.loadProblem(problemName);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        RankingComparator rankComp = new RankingComparator<>();
        DominanceRanking domRank = new DominanceRanking<>();
        domRank.setDominanceComparator(new DominanceComparator<Solution<?>>(RelaxationType.ALPHA, this.alphaFactor));
        rankComp.setRanking(domRank);
        RankingAndCrowdingDistanceComparator rankAndCrowd = new RankingAndCrowdingDistanceComparator<>();
        rankAndCrowd.setRankingComparator(rankComp);
        BinaryTournamentSelection selection = new BinaryTournamentSelection(rankAndCrowd);

        return (new ANSGAIIBuilder(problem, crossover, mutation)).setSelectionOperator(selection).setMaxEvaluations(maxEvaluations).setAlphaFactor(alphaFactor).setPopulationSize(populationSize).build();
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
        params.put("maxEvaluations", "100000");
        params.put("populationSize", "100");
        params.put("alphaFactor", "0.1");

        return params;
    }
}

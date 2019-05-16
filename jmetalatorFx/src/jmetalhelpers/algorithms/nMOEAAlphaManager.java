package jmetalhelpers.algorithms;

import org.uma.jmetal.algorithm.multiobjective.nmoea.nMOEA;
import org.uma.jmetal.algorithm.multiobjective.nmoea.nMOEABuilder;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.RelaxationType;

import java.util.HashMap;
import java.util.Map;

public class nMOEAAlphaManager {

    String problemName;
    double crossoverProbability;
    double crossoverDistributionIndex;
    double mutationProbability;
    double mutationDistributionIndex;
    int maxEvaluations;
    int populationSize;
    double relaxationFactor;

    public nMOEAAlphaManager(Map<String, String> params) {
        if (params == null || params.isEmpty())
            params = this.getDefaultParams();

        setParameters(params);
    }

    public nMOEAAlphaManager(){

        setParameters(this.getDefaultParams());
    }

    public void setParameters(Map<String, String> params){
        problemName = createProblemUrl(params.get("problemName"));
        crossoverProbability = Double.valueOf(params.get("crossoverProbability"));
        crossoverDistributionIndex = 20.0D; // Double.valueOf(params.get("crossoverDistributionIndex"));
        mutationProbability = Double.valueOf(params.get("mutationProbability"));
        mutationDistributionIndex = 20.0D; // Double.valueOf(params.get("mutationDistributionIndex"));
        maxEvaluations = Integer.valueOf(params.get("maxEvaluations"));
        populationSize = Integer.valueOf(params.get("populationSize"));
        relaxationFactor = Double.valueOf(params.get("alphaFactor"));
    }

    public nMOEA Create() {
        Problem problem = ProblemUtils.loadProblem(problemName);
        SBXCrossover crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        PolynomialMutation mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        DominanceComparator comparator =  new DominanceComparator(RelaxationType.ALPHA, this.relaxationFactor);
        SelectionOperator selOp = new BinaryTournamentSelection(comparator);

        return (new nMOEABuilder(problem, crossover, mutation)).setMaxEvaluations(maxEvaluations).setPopulationSize(populationSize).
                setDominanceComprator(comparator).setSelectionOperator(selOp).build();
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
        params.put("alphaFactor", "0.1");

        return params;
    }
}

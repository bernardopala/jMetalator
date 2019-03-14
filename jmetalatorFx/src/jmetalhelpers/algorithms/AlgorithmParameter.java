package jmetalhelpers.algorithms;


import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmParameter {
    private String Name;
    private double Value;

    public AlgorithmParameter(String name, double value) {
        Name = name;
        Value = value;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public static Map<String, String> getParameterSettings(String parameterName){
        if (parameterName == "crossoverProbability")
            return getCrossoverProbabilitySettings();
        else if (parameterName == "mutationProbability")
                return getMutationProbabilitySettings();
        else if  (parameterName == "populationSize")
            return getPopulationSizeSettings();
        else if  (parameterName == "archiveSize")
            return getArchiveSizeSettings();
        else if  (parameterName == "alphaFactor")
            return getAlphaFactorSettings();
        else //if  (parameterName == "maxEvaluationsSize")
            return getMaxEvaluationsSizeSettings();
    }

    private static Map<String, String> getCrossoverProbabilitySettings(){
        Map<String, String> settings = new HashMap<>();
        settings.put("fullName", "Crossover probability");
        settings.put("dataType", "Double");
        settings.put("min", "0.0");
        settings.put("max", "1.0");
        settings.put("step", "0.1");
        return settings;
    }

    private static Map<String, String> getMutationProbabilitySettings(){
        Map<String, String> settings = new HashMap<>();
        settings.put("fullName", "Mutation probability");
        settings.put("dataType", "Double");
        settings.put("min", "0.0");
        settings.put("max", "1.0");
        settings.put("step", "0.1");
        return settings;
    }

    private static Map<String, String> getAlphaFactorSettings(){
        Map<String, String> settings = new HashMap<>();
        settings.put("fullName", "Alpha factor");
        settings.put("dataType", "Double");
        settings.put("min", "0.0");
        settings.put("max", "1.0");
        settings.put("step", "0.1");
        return settings;
    }

    private static Map<String, String> getPopulationSizeSettings(){
        Map<String, String> settings = new HashMap<>();
        settings.put("fullName", "Population size");
        settings.put("dataType", "Integer");
        settings.put("min", "1");
        settings.put("max", "1000");
        settings.put("step", "1");
        return settings;
    }

    private static Map<String, String> getArchiveSizeSettings(){
        Map<String, String> settings = new HashMap<>();
        settings.put("fullName", "Archive size");
        settings.put("dataType", "Integer");
        settings.put("min", "1");
        settings.put("max", "1000");
        settings.put("step", "1");
        return settings;
    }

    private static Map<String, String> getMaxEvaluationsSizeSettings(){
        Map<String, String> settings = new HashMap<>();
        settings.put("fullName", "Max. evaluations");
        settings.put("dataType", "Integer");
        settings.put("min", "1");
        settings.put("max", "1000000");
        settings.put("step", "1");
        return settings;
    }
}

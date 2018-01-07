package jmetalhelpers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jmetalhelpers.algorithms.AlgorithmParameter;
import jmetalhelpers.algorithms.NSGAIIManager;
import jmetalhelpers.algorithms.SPEA2Manager;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExperimentParams{
        String algorithmName;
        String problemName;
        String referenceParetoFront;

        //List<AlgorithmParameter> algorithmParameterList;

        public String getAlgorithmName() {
                return algorithmName;
        }

        public void setAlgorithmName(String algorithmName) {
                this.algorithmName = algorithmName;

        }

        public String getProblemName() {
                return problemName;
        }

        public void setProblemName(String problemName) {
                this.problemName = problemName;
                setReferenceParetoFront(problemName);
        }

        public List<AlgorithmParameter> getAlgorithmParameterList() {
                Map<String, String> defaultParams = getAlgorithmDefaultParameters(this.algorithmName);
                return createParameterList(defaultParams);
        }
//
//        private void setAlgorithmParameterList(List<AlgorithmParameter> algorithmParameterList) {
//                this.algorithmParameterList = algorithmParameterList;
//        }

        private List<AlgorithmParameter> createParameterList(Map<String, String> parameters){
                List<AlgorithmParameter> list = new ArrayList<>();
                parameters.forEach((String key, String value) -> {
                        if (key != "problemName")
                                list.add(new AlgorithmParameter(key, Double.valueOf(value)));
                });

                return list;
        }

        private Map<String,String> getAlgorithmDefaultParameters(String algorithmName) {
                Map<String, String> params = NSGAIIManager.getDefaultParams();
                if (this.algorithmName == "SPEA2") {
                        params = SPEA2Manager.getDefaultParams();
                }

                return params;
        }


        public boolean IsReady()
        {
                if (this.algorithmName != null && !this.algorithmName.isEmpty()
                        && this.problemName != null && !this.problemName.isEmpty())
                {
                        return true;
                }

                return false;
        }

        public String getReferenceParetoFront() {
                return referenceParetoFront;
        }

        private void setReferenceParetoFront(String problemName) {
                this.referenceParetoFront = "/pareto_fronts/" + problemName + ".pf";
        }

        public AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> getJMetalAlgorithm() {

                Map<String, String> params = NSGAIIManager.getDefaultParams();
                params.replace("problemName", this.problemName);
                AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> ea = new NSGAIIManager(params).Create();

                if (this.algorithmName == "SPEA2") {
                        params = SPEA2Manager.getDefaultParams();
                        params.replace("problemName", this.problemName);
                        ea = new SPEA2Manager(params).Create();
                }

                return ea;
        }
}
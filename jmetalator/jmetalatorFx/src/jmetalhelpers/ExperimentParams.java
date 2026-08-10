package jmetalhelpers;

import jmetalhelpers.algorithms.*;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentParams{
        private String algorithmName;
        private String problemName;
        private int objectiveCount;
        private String referenceParetoFront;

        private List<AlgorithmParameter> algorithmParameterList;

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
            if (problemName.contains("DTLZ")) {
                this.problemName = "dtlz." + problemName.substring(0, problemName.indexOf("."));
                String s1 =  problemName.substring(problemName.indexOf(".") + 1, problemName.length());
                String s2 = s1.replace("D", "");
                this.objectiveCount = Integer.parseInt(s2);
            }
            else if (problemName.contains("GLT"))
                this.problemName = "glt." + problemName;
            else if (problemName.contains("LZ09"))
                this.problemName = "lz09." + problemName.replace("_","");
            else if (problemName.contains("UF"))
                this.problemName = "UF." + problemName;
            else if (problemName.contains("WFG"))
                this.problemName = "wfg." + problemName.replace(".3D", "").replace(".2D","");
            else if (problemName.contains("ZDT"))
                this.problemName = "zdt." + problemName;
            else
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
                Map<String, String> params = new HashMap<>();

                if (this.algorithmName == "NSGAII") {
                    params = NSGAIIManager.getDefaultParams();
                }
                else if (this.algorithmName == "SPEA2") {
                    params = SPEA2Manager.getDefaultParams();
                }

                return params;
        }


        public boolean IsReady()
        {
            return this.algorithmName != null && !this.algorithmName.isEmpty()
                    && this.problemName != null && !this.problemName.isEmpty();
        }

        public String getReferenceParetoFront() {
                return referenceParetoFront;
        }

        private void setReferenceParetoFront(String problemName) {
            this.referenceParetoFront = "/pareto_fronts/" + problemName + ".pf";
        }

        public AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> getJMetalAlgorithm() {
            Map<String, String> params = NSGAIIManager.getDefaultParams();

            if (algorithmParameterList != null)
                params = updateParameterList(params, algorithmParameterList);

            params.replace("problemName", this.problemName);
            params.replace("objectiveCount", String.valueOf(this.objectiveCount));
            AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> ea = null;
            
            if (this.algorithmName == "NSGAII") {
                params = NSGAIIManager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                params.replace("objectiveCount", String.valueOf(this.objectiveCount));
                ea = new NSGAIIManager(params).Create();
            }
            if (this.algorithmName == "SPEA2") {
                params = SPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                params.replace("objectiveCount", String.valueOf(this.objectiveCount));
                ea = new SPEA2Manager(params).Create();
            }

            return ea;
        }

        private Map<String, String> updateParameterList(Map<String, String> defaultParams, List<AlgorithmParameter> newParams)
        {
            Map<String, String> returnParams = defaultParams;

            newParams.forEach((AlgorithmParameter ap) -> {
                if (returnParams.containsKey(ap.getName())) {
                    Map<String,String> settings = AlgorithmParameter.getParameterSettings(ap.getName());
                    String dataType = settings.get("dataType");
                    if (dataType == "Double")
                        returnParams.replace(ap.getName(), String.valueOf(Double.valueOf(ap.getValue())));
                    else if (dataType == "Integer")
                        returnParams.replace(ap.getName(), String.valueOf(Double.valueOf(ap.getValue()).intValue()));
                    else
                        returnParams.replace(ap.getName(), String.valueOf(ap.getValue()));
                }
            });

            return returnParams;
        }

    public void setAlgorithmParameterList(List<AlgorithmParameter> algorithmParameterList) {
        this.algorithmParameterList = algorithmParameterList;
    }
}
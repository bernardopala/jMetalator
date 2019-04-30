package jmetalhelpers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
            if (problemName.contains("DTLZ"))
                this.problemName = "dtlz." + problemName.replace(".3D", "").replace(".2D","");
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
                if (this.algorithmName == "SPEA2") {
                    params = SPEA2Manager.getDefaultParams();
                }
                if (this.algorithmName == "DB1SPEA2") {
                    params = DBSPEA2Manager.getDefaultParams();
                }
                if (this.algorithmName == "DB2SPEA2") {
                    params = DBSPEA2Manager.getDefaultParams();
                }
                if (this.algorithmName == "ASPEA2") {
                    params = ASPEA2Manager.getDefaultParams();
                }
                if (this.algorithmName == "AngleSPEA2") {
                    params = AngleSPEA2Manager.getDefaultParams();
                }
                if (this.algorithmName == "ESPEA2") {
                    params = ESPEA2Manager.getDefaultParams();
                }
                else if (this.algorithmName == "SPEA3") {
                    params = SPEA3Manager.getDefaultParams();
                }
                else if (this.algorithmName == "ANSGAII") {
                    params = ANSGAIIManager.getDefaultParams();
                }
                else if (this.algorithmName == "ENSGAII") {
                    params = ENSGAIIManager.getDefaultParams();
                }
                else if (this.algorithmName == "AngleNSGAII") {
                    params = AngleNSGAIIManager.getDefaultParams();
                }
                else if (this.algorithmName == "CDASNSGAII") {
                    params = CDASNSGAIIManager.getDefaultParams();
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
            AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> ea = null;
            
            if (this.algorithmName == "NSGAII") {
                params = NSGAIIManager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new NSGAIIManager(params).Create();
            }
            if (this.algorithmName == "SPEA2") {
                params = SPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new SPEA2Manager(params).Create();
            }
            else if (this.algorithmName == "DB1SPEA2") {
                params = DBSPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new DBSPEA2Manager(params).Create(1);
            }
            else if (this.algorithmName == "DB2SPEA2") {
                params = DBSPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new DBSPEA2Manager(params).Create(2);
            }
            else if (this.algorithmName == "ASPEA2") {
                params = ASPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new ASPEA2Manager(params).Create();
            }
            else if (this.algorithmName == "AngleSPEA2") {
                params = AngleSPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new AngleSPEA2Manager(params).Create();
            }
            else if (this.algorithmName == "ESPEA2") {
                params = ESPEA2Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new ESPEA2Manager(params).Create();
            }
            else if (this.algorithmName == "SPEA3") {
                params = SPEA3Manager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new SPEA3Manager(params).Create();
            }
            else if (this.algorithmName == "ANSGAII") {
                params = ANSGAIIManager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new ANSGAIIManager(params).Create();
            }
            else if (this.algorithmName == "ENSGAII") {
                params = ENSGAIIManager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new ENSGAIIManager(params).Create();
            }
            else if (this.algorithmName == "AngleNSGAII") {
                params = AngleNSGAIIManager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new AngleNSGAIIManager(params).Create();
            }
            else if (this.algorithmName == "CDASNSGAII") {
                params = CDASNSGAIIManager.getDefaultParams();
                if (algorithmParameterList != null)
                    params = updateParameterList(params, algorithmParameterList);

                params.replace("problemName", this.problemName);
                ea = new CDASNSGAIIManager(params).Create();
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
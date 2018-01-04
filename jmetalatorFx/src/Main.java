import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import jmetalhelpers.algorithms.NSGAIIManager;
import jmetalhelpers.algorithms.SPEA2Manager;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.util.CurrentSolutionSetReceiver;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Main implements CurrentSolutionSetReceiver<DoubleSolution>, Initializable {

    public ScatterChart<Double, Double> objectiveSpaceChart;
    public Label qiResultsLabel;

    public ComboBox algorithmsComboBox;
    public ComboBox problemsComboBox;

    Algorithm<List<DoubleSolution>> algorithm;
    AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> eaAlgorithm;
    Property<ObservableList<XYChart.Series<Double, Double>>> sourceData = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<XYChart.Series<Double, Double>>()));
    private final StringProperty qiResults = new SimpleStringProperty();
    String result = "";
    //public ObservableList<XYChart.Data<Double, Double>> solutionSetData = new ObservableList<>();

    String problemName = "org.uma.jmetal.problem.multiobjective.ConstrEx";
    String referenceParetoFront = "/pareto_fronts/ConstrEx.pf";
    int receiveSolutionSetCount = 0;
    int printQIsCount = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        objectiveSpaceChart.dataProperty().bind(sourceData);
        qiResultsLabel.textProperty().bind(qiResults);
//        qiResultsLabel.setMinHeight(Region.USE_PREF_SIZE);
        FillComboBoxAlgorithms();
        FillComboBoxProblems();
    }

    @Override
    public void ReceiveCurrentSolutionSet(final List<DoubleSolution> solutionSet) {
        receiveSolutionSetCount++;

        ObservableList<XYChart.Series<Double, Double>> seriesList = FXCollections.observableArrayList();
        XYChart.Series series = new XYChart.Series();
        series.setName("Pareto front approximation");
        for (int i = 0; i < solutionSet.size(); i++) {
            series.getData().add(new XYChart.Data(solutionSet.get(i).getObjective(0), solutionSet.get(i).getObjective(1)));
        }
        seriesList.add(series);

        result = "Ni ma wynik�w...";

        try {
            result = printQualityIndicators(solutionSet, referenceParetoFront);
            printQIsCount++;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sourceData.setValue(seriesList);
                qiResults.setValue(result);
            }
        });
    }

    public String printQualityIndicators(List<DoubleSolution> population, String paretoFrontFile) throws FileNotFoundException {
        ArrayFront referenceFront = new ArrayFront(paretoFrontFile);
        FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
        Front normalizedFront = frontNormalizer.normalize(new ArrayFront(population));
        List normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
        String outputString = "\n";
        outputString = outputString + "Hypervolume (N) : " + (new PISAHypervolume(normalizedReferenceFront)).evaluate(normalizedPopulation) + "\n";
        outputString = outputString + "Hypervolume     : " + (new PISAHypervolume(referenceFront)).evaluate(population) + "\n";
        outputString = outputString + "Epsilon (N)     : " + (new Epsilon(normalizedReferenceFront)).evaluate(normalizedPopulation) + "\n";
        outputString = outputString + "Epsilon         : " + (new Epsilon(referenceFront)).evaluate(population) + "\n";
        outputString = outputString + "GD (N)          : " + (new GenerationalDistance(normalizedReferenceFront)).evaluate(normalizedPopulation) + "\n";
        outputString = outputString + "GD              : " + (new GenerationalDistance(referenceFront)).evaluate(population) + "\n";
        outputString = outputString + "IGD (N)         : " + (new InvertedGenerationalDistance(normalizedReferenceFront)).evaluate(normalizedPopulation) + "\n";
        outputString = outputString + "IGD             : " + (new InvertedGenerationalDistance(referenceFront)).evaluate(population) + "\n";
        outputString = outputString + "IGD+ (N)        : " + (new InvertedGenerationalDistancePlus(normalizedReferenceFront)).evaluate(normalizedPopulation) + "\n";
        outputString = outputString + "IGD+            : " + (new InvertedGenerationalDistancePlus(referenceFront)).evaluate(population) + "\n";
        outputString = outputString + "Spread (N)      : " + (new Spread(normalizedReferenceFront)).evaluate(normalizedPopulation) + "\n";
        outputString = outputString + "Spread          : " + (new Spread(referenceFront)).evaluate(population) + "\n";
        outputString = outputString + "Error ratio     : " + (new ErrorRatio(referenceFront)).evaluate(population) + "\n";

        return outputString;
        //JMetalLogger.logger.info(outputString);
    }

    public void startButtonClicked(ActionEvent actionEvent) throws ClassNotFoundException {

        String selectedAlgorithmName = (String)algorithmsComboBox.getValue();
        String selectedProblemName = (String)problemsComboBox.getValue();
        if (selectedAlgorithmName == null || selectedProblemName == null) {
            new Alert(Alert.AlertType.ERROR, "Select algorithm and problem!").showAndWait();
            return;
        }

        problemName = "org.uma.jmetal.problem.multiobjective." + selectedProblemName;
        referenceParetoFront = "/pareto_fronts/" + selectedProblemName + ".pf";

        Map<String, String> params = NSGAIIManager.getDefaultParams();
        params.replace("problemName", problemName);
        eaAlgorithm = new NSGAIIManager(params).Create();

        if (selectedAlgorithmName == "SPEA2") {
            params = SPEA2Manager.getDefaultParams();
            params.replace("problemName", problemName);
            eaAlgorithm = new SPEA2Manager(params).Create();
        }

        eaAlgorithm.subscribeCurrentSolutionSetReceiver(this);
        algorithm = eaAlgorithm;

        Thread mainLoop = new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                algorithm.run();
                return null;
            }
        });
        mainLoop.start();
    }

    public void FillComboBoxAlgorithms()
    {
        ObservableList<String> algoritmhs = FXCollections.observableArrayList();
        algoritmhs.addAll("NSGAII", "SPEA2");

        algorithmsComboBox.setItems(algoritmhs);
    }

    public void FillComboBoxProblems()
    {
        ObservableList<String> problems = FXCollections.observableArrayList();
        problems.addAll("Binh2", "ConstrEx", "Fonseca", "Golinski", "Kursawe", "Osyczka2", "Schaffer", "Srinivas", "Tanaka");

        problemsComboBox.setItems(problems);
    }
}

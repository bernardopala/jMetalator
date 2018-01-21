import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Pair;
import jmetalhelpers.ExperimentParams;
import jmetalhelpers.QualityIndicator;
import jmetalhelpers.SolutionDto;
import jmetalhelpers.algorithms.AlgorithmParameter;
import jmetalhelpers.algorithms.NSGAIIManager;
import jmetalhelpers.algorithms.SPEA2Manager;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.util.CurrentSolutionSetReceiver;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import view.PairKeyFactory;
import view.PairValueCell;
import view.PairValueFactory;

import javax.swing.text.TableView;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static jdk.nashorn.internal.objects.NativeFunction.bind;

public class Main implements CurrentSolutionSetReceiver<DoubleSolution>, Initializable {

    //region View controls

    @FXML
    public ScatterChart<Double, Double> objectiveSpaceChart;

    @FXML
    public Label qiResultsLabel;

    @FXML
    public Label receivedSSLabel;

    @FXML
    public Label gdErrorValueLabel;

    @FXML
    public Label spreadErrorValueLabel;

    @FXML
    public ComboBox algorithmsComboBox;

    @FXML
    public ComboBox problemsComboBox;

    @FXML
    public javafx.scene.control.TableView<SolutionDto> solutionsTableView;

    @FXML
    public TableColumn<SolutionDto, String> solutionsV1TableColumn;

    @FXML
    public TableColumn<SolutionDto, String> solutionsV2TableColumn;

    @FXML
    public LineChart<Integer, Double> gdChart;

    @FXML
    public LineChart<Integer, Double> spreadChart;

    @FXML javafx.scene.control.TableView<AlgorithmParameter> algorithmParametersTableView;

    @FXML
    public TableColumn<AlgorithmParameter, String> algorithmParametersNameTableColumn;

    @FXML
    public TableColumn<AlgorithmParameter, Object> algorithmParametersValueTableColumn;

    @FXML
    public Button startButton;

    @FXML
    public Button stopButton;

    //endregion

    //region Private variables

    ExperimentParams ep;
    ObservableList<AlgorithmParameter> algorithmParams = FXCollections.observableArrayList();

    Algorithm<List<DoubleSolution>> algorithm;
    AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> eaAlgorithm;
    Property<ObservableList<XYChart.Series<Double, Double>>> osData = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<XYChart.Series<Double, Double>>()));

    XYChart.Series pfSeries = new XYChart.Series();

    Property<ObservableList<XYChart.Series<Integer, Double>>> gdData = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<XYChart.Series<Integer, Double>>()));
    Property<ObservableList<XYChart.Series<Integer, Double>>> spreadData = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<XYChart.Series<Integer, Double>>()));
    private final List<QualityIndicator> gdArray = new ArrayList<>();
    private final List<QualityIndicator> spreadArray = new ArrayList<>();

    private final StringProperty qiResults = new SimpleStringProperty();
    private final SimpleIntegerProperty receivedSSProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty printedQIProperty = new SimpleIntegerProperty();
    private final SimpleDoubleProperty gdErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty spreadErrorProperty = new SimpleDoubleProperty();


    String result = "";

    int receiveSolutionSetCount = 0;
    int printQIsCount = 0;

    Thread mainLoop = new Thread();

    //endregion

    @Override
    public void initialize(URL url, ResourceBundle rb){
        ep = new ExperimentParams();

        objectiveSpaceChart.dataProperty().bind(osData);
        gdChart.dataProperty().bind(gdData);
        spreadChart.dataProperty().bind(spreadData);

        qiResultsLabel.textProperty().bind(qiResults);
        receivedSSLabel.textProperty().bind(receivedSSProperty.asString());
        gdErrorValueLabel.textProperty().bind(gdErrorProperty.asString());
        spreadErrorValueLabel.textProperty().bind(spreadErrorProperty.asString());
        solutionsV1TableColumn.setCellValueFactory(r -> r.getValue().v1Property());
        solutionsV2TableColumn.setCellValueFactory(r -> r.getValue().v2Property());

//        algorithmParametersNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
//        algorithmParametersValueTableColumn.setCellValueFactory(new PropertyValueFactory<>("Value"));
        algorithmParametersNameTableColumn.setCellValueFactory(new PairKeyFactory());
        algorithmParametersValueTableColumn.setCellValueFactory(new PairValueFactory());
//        algorithmParametersValueTableColumn.setCellFactory(col -> new SpinnerCell<AlgorithmParameter, Double>());
        algorithmParametersValueTableColumn.setCellFactory(new Callback<TableColumn<AlgorithmParameter, Object>, TableCell<AlgorithmParameter, Object>>() {
            @Override
            public TableCell<AlgorithmParameter, Object> call(TableColumn<AlgorithmParameter, Object> column) {
                return new PairValueCell();
            }
        });

//        algorithmParametersValueTableColumn.setOnEditCommit(
//                new EventHandler<TableColumn.CellEditEvent<AlgorithmParameter, Double>>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent<AlgorithmParameter, Double> t) {
//                        ((AlgorithmParameter) t.getTableView().getItems().get(
//                                t.getTablePosition().getRow())
//                        ).setValue(Double.valueOf(t.getNewValue()));
//                    }
//                }
//        );

        FillComboBoxAlgorithms();
        FillComboBoxProblems();
        enableControlsOnStop();
    }

    @Override
    public void ReceiveCurrentSolutionSet(final List<DoubleSolution> solutionSett) {

        List<DoubleSolution> solutionSetResult =  algorithm.getResult();
        if (solutionSetResult.size() == 0)
            solutionSetResult = solutionSett;
//        if (Thread.currentThread().isInterrupted()){
//            algorithm = null;
//            return;
//        }
        ObservableList<XYChart.Series<Double, Double>> osList = FXCollections.observableArrayList();
        XYChart.Series osSeries = new XYChart.Series();
        receiveSolutionSetCount++;
        if (osList.contains(osSeries))
            osList.remove(osSeries);

        osSeries = new XYChart.Series();
        osSeries.setName("Pareto front approximation");

        ObservableList<SolutionDto> solutionList = FXCollections.observableArrayList();

        for (int i = 0; i < solutionSetResult.size(); i++) {
            osSeries.getData().add(new XYChart.Data(solutionSetResult.get(i).getObjective(0), solutionSetResult.get(i).getObjective(1)));

            SolutionDto dto = new SolutionDto();
            dto.setV1(String.valueOf(solutionSetResult.get(i).getObjective(0)));
            dto.setV2(String.valueOf(solutionSetResult.get(i).getObjective(1)));
            solutionList.add(dto);
        }

        osList.add(osSeries);
        solutionsTableView.setItems(solutionList);

        ObservableList<XYChart.Series<Integer, Double>> gdList = FXCollections.observableArrayList();
        XYChart.Series gdSeries = new XYChart.Series();
        gdSeries.setName("Generational distance");

        ObservableList<XYChart.Series<Integer, Double>> spreadList = FXCollections.observableArrayList();
        XYChart.Series spreadSeries = new XYChart.Series();
        spreadSeries.setName("Spread");

        try {
            ArrayFront referenceFront = new ArrayFront(ep.getReferenceParetoFront());
            FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
            Front normalizedFront = frontNormalizer.normalize(new ArrayFront(solutionSetResult));
            List normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

            QualityIndicator gd = new QualityIndicator(receiveSolutionSetCount, (new GenerationalDistance<DoubleSolution>(referenceFront)).evaluate(solutionSetResult));
            QualityIndicator spread = new QualityIndicator(receiveSolutionSetCount, (new Spread<DoubleSolution>(referenceFront)).evaluate(solutionSetResult));
            gdArray.add(gd);
            spreadArray.add(spread);

            for (int i = 0; i < gdArray.size(); i++) {
                gdSeries.getData().add(new XYChart.Data<>(gdArray.get(i).getId(), gdArray.get(i).getValue()));
                spreadSeries.getData().add(new XYChart.Data<>(spreadArray.get(i).getId(), spreadArray.get(i).getValue()));
            }
            gdList.add(gdSeries);
            spreadList.add(spreadSeries);

//            gdArray.add(new QualityIndicator(receiveSolutionSetCount, (new PISAHypervolume(normalizedReferenceFront)).evaluate(normalizedPopulation)));
//
//            List<XYChart.Data<Integer, Double>> update = new ArrayList<XYChart.Data<Integer, Double>>();
//            for (int i = 0; i < gdArray.size(); i++) {
//                update.add(new XYChart.Data<>(gdArray.get(i).getId(), gdArray.get(i).getValue()));
//            }
//            ObservableList<XYChart.Data<Integer, Double>> updateList = FXCollections.observableArrayList(update);
//            gdSeries.setData(updateList);
//            gdList.add(gdSeries);

            printQIsCount++;
            //result = printQualityIndicators(solutionSet, referenceParetoFront);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    osData.setValue(osList);
                    gdData.setValue(gdList);
                    spreadData.setValue(spreadList);

                    //qiResults.setValue(result);
                    receivedSSProperty.setValue(receiveSolutionSetCount);
                    printedQIProperty.setValue(printQIsCount);
                    gdErrorProperty.setValue(gd.getValue());
                    spreadErrorProperty.setValue(spread.getValue());
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void disableControlsOnStart(){
        algorithmsComboBox.setDisable(true);
        problemsComboBox.setDisable(true);
        algorithmParametersTableView.setDisable(true);
        startButton.setDisable(true);
        stopButton.setDisable(false);

        receiveSolutionSetCount = 0;
        gdErrorProperty.setValue(0.0);
        spreadErrorProperty.setValue(0.0);
        gdArray.clear();
        spreadArray.clear();
        osData.getValue().clear();
    }

    public void enableControlsOnStop(){
        algorithmsComboBox.setDisable(false);
        problemsComboBox.setDisable(false);
        algorithmParametersTableView.setDisable(false);
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    //region Events
    public void startButtonClicked(ActionEvent actionEvent){
        if (!ep.IsReady()){
            new Alert(Alert.AlertType.ERROR, "Select algorithm and problem!").showAndWait();
            return;
        }

        disableControlsOnStart();

        ep.setAlgorithmParameterList(algorithmParametersTableView.getItems());
        eaAlgorithm = ep.getJMetalAlgorithm();
        eaAlgorithm.subscribeCurrentSolutionSetReceiver(this);
        algorithm = eaAlgorithm;

        Runnable task = () -> {
            try {
                algorithm.run();
            }
            finally
            {
                enableControlsOnStop();
            }
        };

        mainLoop = new Thread(task);
        mainLoop.setDaemon(false);
        mainLoop.start();
    }

    public void stopButtonClicked(ActionEvent actionEvent){
        mainLoop.interrupt();
        try {
            algorithm = null;
        } catch (Exception ex)
        {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }

        enableControlsOnStop();
    }

    public void algorithmSelected(ActionEvent actionEvent){
        String selectedAlgorithmName = (String)algorithmsComboBox.getValue();
        if (selectedAlgorithmName != null && !selectedAlgorithmName.isEmpty()) {
            ep.setAlgorithmName(selectedAlgorithmName);
            algorithmParams = FXCollections.observableArrayList(ep.getAlgorithmParameterList());
            algorithmParametersTableView.setItems(algorithmParams);
        }
    }

    public void problemSelected(ActionEvent actionEvent) throws ClassNotFoundException {
        String selectedProblemName = (String)problemsComboBox.getValue();
        if (selectedProblemName != null && !selectedProblemName.isEmpty())
            ep.setProblemName(selectedProblemName);

//        try
//        {
//            String selectedProblemName = (String)problemsComboBox.getValue();
//            prepareProblemData(selectedProblemName);
//
//            if (osList.contains(pfSeries))
//                osList.remove(pfSeries);
//
//            pfSeries = new XYChart.Series();
//            pfSeries.setName("True Pareto front");
//
//            ArrayFront referenceFront = new ArrayFront(referenceParetoFront);
////            FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
////            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
////            Front normalizedFront = frontNormalizer.normalize(new ArrayFront(solutionSet));
////            List normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
//
//            for (int i = 0; i < referenceFront.getNumberOfPoints(); i++) {
//                pfSeries.getData().add(new XYChart.Data(referenceFront.getPoint(i).getDimensionValue(0), referenceFront.getPoint(i).getDimensionValue(1)));
//            }
//
//            osList.add(pfSeries);
//            osData.setValue(osList);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }
    //endregion

    //region Methods
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
    //endregion
}
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

import javax.swing.text.TableView;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Main implements CurrentSolutionSetReceiver<DoubleSolution>, Initializable {

    //region View controls

    @FXML
    public ScatterChart<Double, Double> objectiveSpaceChart;

    @FXML
    public Label qiResultsLabel;

    @FXML
    public Label receivedSSLabel;

    @FXML
    public Label printedQILabel;

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
    public TableColumn<AlgorithmParameter, Double> algorithmParametersValueTableColumn;

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
        printedQILabel.textProperty().bind(printedQIProperty.asString());
        solutionsV1TableColumn.setCellValueFactory(r -> r.getValue().v1Property());
        solutionsV2TableColumn.setCellValueFactory(r -> r.getValue().v2Property());

        algorithmParametersNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        algorithmParametersValueTableColumn.setCellValueFactory(new PropertyValueFactory<>("Value"));
        algorithmParametersValueTableColumn.setCellFactory(col -> new SpinnerCell<AlgorithmParameter, Double>());
        algorithmParametersValueTableColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<AlgorithmParameter, Double>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<AlgorithmParameter, Double> t) {
                        ((AlgorithmParameter) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setValue(Double.valueOf(t.getNewValue()));
                    }
                }
        );

        FillComboBoxAlgorithms();
        FillComboBoxProblems();
    }

    @Override
    public void ReceiveCurrentSolutionSet(final List<DoubleSolution> solutionSet) {

//        List<DoubleSolution> solutionSetResult =  algorithm.getResult();
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

        for (int i = 0; i < solutionSet.size(); i++) {
            osSeries.getData().add(new XYChart.Data(solutionSet.get(i).getObjective(0), solutionSet.get(i).getObjective(1)));

            SolutionDto dto = new SolutionDto();
            dto.setV1(String.valueOf(solutionSet.get(i).getObjective(0)));
            dto.setV2(String.valueOf(solutionSet.get(i).getObjective(1)));
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
            Front normalizedFront = frontNormalizer.normalize(new ArrayFront(solutionSet));
            List normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

            gdArray.add(new QualityIndicator(receiveSolutionSetCount, (new GenerationalDistance<DoubleSolution>(referenceFront)).evaluate(solutionSet)));
            spreadArray.add(new QualityIndicator(receiveSolutionSetCount, (new Spread<DoubleSolution>(referenceFront)).evaluate(solutionSet)));

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
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //region Events
    public void startButtonClicked(ActionEvent actionEvent){
        if (!ep.IsReady()){
            new Alert(Alert.AlertType.ERROR, "Select algorithm and problem!").showAndWait();
            return;
        }

        eaAlgorithm = ep.getJMetalAlgorithm();
        eaAlgorithm.subscribeCurrentSolutionSetReceiver(this);
        algorithm = eaAlgorithm;

        Runnable task = () -> {
            algorithm.run();
        };

        mainLoop = new Thread(task);
        mainLoop.setDaemon(true);
        mainLoop.start();
    }

    public void stopButtonClicked(ActionEvent actionEvent){
        //mainLoop.interrupt();

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

class SpinnerCell<S, T extends Number> extends TableCell<S, T> {

    private Spinner<T> spinner;
    private ObservableValue<T> ov;

    public SpinnerCell() {
        this(0, 1, 0, 0.1);
    }

    public SpinnerCell(double min, double max, double initial, double step) {
        this.spinner = new Spinner<>(min, max, initial, step);
        spinner.setEditable(true);
        setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(null);
            setGraphic(this.spinner);

            if(this.ov instanceof Property) {
                this.spinner.getValueFactory().valueProperty().unbindBidirectional(((Property) this.ov));
            }

            this.ov = getTableColumn().getCellObservableValue(getIndex());

            if(this.ov instanceof Property) {
                this.spinner.getValueFactory().valueProperty().bindBidirectional(((Property) this.ov));
            }
        }
    }
}
import com.orsoncharts.Chart3D;
import com.orsoncharts.Chart3DFactory;
import com.orsoncharts.Chart3DPanel;
import com.orsoncharts.Colors;
import com.orsoncharts.axis.LabelOrientation;
import com.orsoncharts.axis.NumberAxis3D;
import com.orsoncharts.data.xyz.XYZDataItem;
import com.orsoncharts.data.xyz.XYZDataset;
import com.orsoncharts.data.xyz.XYZSeries;
import com.orsoncharts.data.xyz.XYZSeriesCollection;
import com.orsoncharts.fx.Chart3DViewer;
import com.orsoncharts.graphics3d.Dimension3D;
import com.orsoncharts.graphics3d.ViewPoint3D;
import com.orsoncharts.graphics3d.swing.ZoomInAction;
import com.orsoncharts.label.StandardXYZLabelGenerator;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.xyz.ScatterXYZRenderer;
import com.orsoncharts.renderer.xyz.XYZRenderer;
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
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Pair;
import jmetalhelpers.ExperimentParams;
import jmetalhelpers.QualityIndicator;
import jmetalhelpers.SolutionDto;
import jmetalhelpers.algorithms.AlgorithmParameter;
import jmetalhelpers.algorithms.NSGAIIManager;
import jmetalhelpers.algorithms.SPEA2Manager;
import org.apache.commons.lang3.SerializationUtils;
import org.jfree.chart.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.Zoomable;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
import java.awt.*;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static jdk.nashorn.internal.objects.NativeFunction.bind;

public class Main implements CurrentSolutionSetReceiver<DoubleSolution>, Initializable {

    //region View controls
    @FXML
    public TabPane tabPane;

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
    public TableColumn<SolutionDto, String> solutionsV3TableColumn;

    @FXML javafx.scene.control.TableView<AlgorithmParameter> algorithmParametersTableView;

    @FXML
    public TableColumn<AlgorithmParameter, String> algorithmParametersNameTableColumn;

    @FXML
    public TableColumn<AlgorithmParameter, Object> algorithmParametersValueTableColumn;

    @FXML
    public Button startButton;

    @FXML
    public Button stopButton;

    @FXML
    public StackPane stackPaneOrson;

    @FXML
    public StackPane stackPaneJFREE;

    @FXML
    public StackPane stackPaneGD;

    @FXML
    public StackPane stackPaneIGD;

    @FXML
    public StackPane stackPaneSpread;

    private List<Tab> tabs = new ArrayList<>();
    private Tab tab2D = new Tab();
    private Tab tab3D = new Tab();
    //endregion

    //region Private variables

    private Lock lock = new ReentrantLock();
    private boolean isAlgorithmWorking = false;

    private ExperimentParams ep;
    private ObservableList<AlgorithmParameter> algorithmParams = FXCollections.observableArrayList();

    private Algorithm<List<DoubleSolution>> algorithm;
    private AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> eaAlgorithm;

    private final StringProperty selectedTab = new SimpleStringProperty();
    private final StringProperty qiResults = new SimpleStringProperty();
    private final SimpleIntegerProperty receivedSSProperty = new SimpleIntegerProperty();
    private final SimpleDoubleProperty gdErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty spreadErrorProperty = new SimpleDoubleProperty();

    private int receiveSolutionSetCount = 0;
    private int printQIsCount = 0;

    private Thread mainLoop = new Thread();

    private XYZSeries<String> serieFront3D = new XYZSeries<>("aprox");
    private ScatterXYZRenderer renderer = new ScatterXYZRenderer();
    private XYZPlot plot = new XYZPlot(new XYZSeriesCollection<>(), renderer, new NumberAxis3D("X"), new NumberAxis3D("Y"), new NumberAxis3D("Z"));
    private Chart3DViewer viewer = new Chart3DViewer(new Chart3D("", "", plot), false);

    private XYSeries seriesFront2D = new XYSeries("front");
    private JFreeChart chart2D = createChart2D(new XYSeriesCollection());
    private ChartCanvas canvas2D = new ChartCanvas(chart2D);

    private JFreeChart chartGD = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasGD = new ChartCanvas(chartGD);
    private XYSeries gdSeries = new XYSeries("gd");

    private JFreeChart chartIGD = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasIGD = new ChartCanvas(chartIGD);
    private XYSeries igdSeries = new XYSeries("igd");

    private JFreeChart chartSpread = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasSpread = new ChartCanvas(chartSpread);
    private XYSeries spreadSeries = new XYSeries("spread");

    private List<DoubleSolution> solutionSetResult = new ArrayList<>();

    private GenerationalDistance gdIdicator = new GenerationalDistance<DoubleSolution>();
    private InvertedGenerationalDistance igdIdicator = new InvertedGenerationalDistance<DoubleSolution>();
    private GeneralizedSpread spreadIdicator = new GeneralizedSpread<DoubleSolution>();

    private List<Double> gdValues = new ArrayList<>();
    private List<Double> igdValues = new ArrayList<>();
    private List<Double> spreadValues = new ArrayList<>();

    private final AtomicLong counter = new AtomicLong(-1);

    //endregion

    @Override
    public void initialize(URL url, ResourceBundle rb){
        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (ov, t, t1) -> {
                if (t == null || t1 == null)
                    return;

                selectedTab.setValue(t1.getId());

                if (!isAlgorithmWorking && solutionSetResult != null && solutionSetResult.size() > 0) {
                    if (selectedTab.getValue().equalsIgnoreCase("tab3d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
                            update3dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tab2d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
                            update2dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabAproximationSet")) {
                        updateSetRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabGD")) {
                        updateGDRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabIGD")) {
                        updateIGDRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabSread")) {
                        updateSpreadRelatedIU();
                    }
                }
            }
        );
        selectedTab.setValue("tab3d");

        clearControls();

        renderer.setSize(0.20);
        renderer.setColors(Colors.getEarthColors());
        viewer.getCanvas().setPanIncrement(0.01D);
        viewer.getCanvas().setRotateIncrement(0.01D);

        viewer.setZoomMultiplier(1.1D);
        stackPaneOrson.getChildren().add(viewer);
        stackPaneJFREE.getChildren().add(canvas2D);
        stackPaneGD.getChildren().add(canvasGD);
        stackPaneIGD.getChildren().add(canvasIGD);
        stackPaneSpread.getChildren().add(canvasSpread);

        canvas2D.widthProperty().bind( stackPaneJFREE.widthProperty());
        canvas2D.heightProperty().bind( stackPaneJFREE.heightProperty());

        canvasGD.widthProperty().bind( stackPaneGD.widthProperty());
        canvasGD.heightProperty().bind( stackPaneGD.heightProperty());

        canvasIGD.widthProperty().bind( stackPaneIGD.widthProperty());
        canvasIGD.heightProperty().bind( stackPaneIGD.heightProperty());

        canvasSpread.widthProperty().bind( stackPaneSpread.widthProperty());
        canvasSpread.heightProperty().bind( stackPaneSpread.heightProperty());

        ep = new ExperimentParams();

        qiResultsLabel.textProperty().bind(qiResults);
        receivedSSLabel.textProperty().bind(receivedSSProperty.asString());
        gdErrorValueLabel.textProperty().bind(gdErrorProperty.asString());
        spreadErrorValueLabel.textProperty().bind(spreadErrorProperty.asString());
        solutionsV1TableColumn.setCellValueFactory(r -> r.getValue().v1Property());
        solutionsV2TableColumn.setCellValueFactory(r -> r.getValue().v2Property());
        solutionsV3TableColumn.setCellValueFactory(r -> r.getValue().v3Property());

        algorithmParametersNameTableColumn.setCellValueFactory(new PairKeyFactory());
        algorithmParametersValueTableColumn.setCellValueFactory(new PairValueFactory());
        algorithmParametersValueTableColumn.setCellFactory(column -> new PairValueCell());

        FillComboBoxAlgorithms();
        FillComboBoxProblems();
        enableControlsOnStop();

        tabs = FXCollections.observableArrayList(tabPane.getTabs());
    }

    private void clearControls()
    {
        gdValues.clear();
        gdSeries.clear();
        XYSeriesCollection datasetGD = new XYSeriesCollection();
        datasetGD.addSeries(gdSeries);
        XYPlot plotGD = (XYPlot) chartGD.getPlot();
        plotGD.setDataset(datasetGD);

        igdValues.clear();
        igdSeries.clear();
        XYSeriesCollection datasetIGD = new XYSeriesCollection();
        datasetIGD.addSeries(igdSeries);
        XYPlot plotIGD = (XYPlot) chartIGD.getPlot();
        plotIGD.setDataset(datasetIGD);

        spreadValues.clear();
        spreadSeries.clear();
        XYSeriesCollection datasetSpread = new XYSeriesCollection();
        datasetSpread.addSeries(spreadSeries);
        XYPlot plotSpread = (XYPlot) chartSpread.getPlot();
        plotSpread.setDataset(datasetSpread);

        solutionSetResult = new ArrayList<>();
        solutionsTableView.getItems().clear();
        counter.set(-1);
    }

    @Override
    public void ReceiveCurrentSolutionSet(final List<DoubleSolution> solutionSett) {
        try {
            lock.lock();

            printQIsCount++;

            if (algorithm == null){
                lock.unlock();
                return;
            }

            solutionSetResult = algorithm.getResult();
            if (solutionSetResult.size() == 0) {
                lock.unlock();
                return;
            }

            receiveSolutionSetCount++;

            if (receiveSolutionSetCount % 100 == 0) {
                int a = 0;
            }

            double gd = gdIdicator.evaluate(solutionSetResult);
            double igd = igdIdicator.evaluate(solutionSetResult);
            double spread = spreadIdicator.evaluate(solutionSetResult);

            gdValues.add(gd);
            igdValues.add(igd);
            spreadValues.add(spread);

            lock.unlock();

            if (counter.getAndSet(1) != -1)
                return;

            Platform.runLater(() -> {
                try {
                    lock.lock();

                    gdErrorProperty.setValue(gd);
                    spreadErrorProperty.setValue(spread);
                    receivedSSProperty.setValue(receiveSolutionSetCount);

                    if (selectedTab.getValue().equalsIgnoreCase("tab3d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
                            update3dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tab2d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
                            update2dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabAproximationSet")) {
                        updateSetRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabGD")) {
                        updateGDRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabIGD")) {
                        updateIGDRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabSread")) {
                        updateSpreadRelatedIU();
                    }

                    counter.set(-1);
                    lock.unlock();
                } catch (Exception e) {
                    System.out.println("EXCEPTION: ReceiveCurrentSolutionSet()->Platform.runLater()");
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println("EXCEPTION: ReceiveCurrentSolutionSet()");
            e.printStackTrace();
        }
    }

    private void update3dChartRelatedUI(){
        XYZPlot plot = (XYZPlot) viewer.getChart().getPlot();
        XYZSeriesCollection dataset3D = createDataset(solutionSetResult);
        dataset3D.add(serieFront3D);
        plot.setDataset(dataset3D);
    }

    private void update2dChartRelatedUI(){
        XYPlot plot2D = (XYPlot) chart2D.getPlot();
        XYSeriesCollection dataset2D = createDataset2D(solutionSetResult);
        dataset2D.addSeries(seriesFront2D);
        plot2D.setDataset(dataset2D);
    }

    private void updateSetRelatedUI(){
        ObservableList<SolutionDto> solutionList = FXCollections.observableArrayList();
        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
            for (DoubleSolution aSolutionSetResult : solutionSetResult) {
                SolutionDto dto = new SolutionDto();
                dto.setV1(String.valueOf(aSolutionSetResult.getObjective(0)));
                dto.setV2(String.valueOf(aSolutionSetResult.getObjective(1)));
                solutionList.add(dto);
            }
        } else if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
            for (DoubleSolution aSolutionSetResult : solutionSetResult) {
                SolutionDto dto = new SolutionDto();
                dto.setV1(String.valueOf(aSolutionSetResult.getObjective(0)));
                dto.setV2(String.valueOf(aSolutionSetResult.getObjective(1)));
                dto.setV3(String.valueOf(aSolutionSetResult.getObjective(2)));
                solutionList.add(dto);
            }
        }

        solutionsTableView.setItems(solutionList);
    }

    private void updateGDRelatedUI(){
        gdSeries = new XYSeries("gd");
        for (int i = 0; i < gdValues.size(); i++){
            gdSeries.add(i, gdValues.get(i));
        }

        XYSeriesCollection datasetGD = new XYSeriesCollection();
        datasetGD.addSeries(gdSeries);
        XYPlot plotGD = (XYPlot) chartGD.getPlot();
        plotGD.setDataset(datasetGD);
    }

    private void updateIGDRelatedUI(){
        igdSeries = new XYSeries("igd");
        for (int i = 0; i < igdValues.size(); i++){
            igdSeries.add(i, igdValues.get(i));
        }

        XYSeriesCollection datasetIGD = new XYSeriesCollection();
        datasetIGD.addSeries(igdSeries);
        XYPlot plotIGD = (XYPlot) chartIGD.getPlot();
        plotIGD.setDataset(datasetIGD);
    }

    private void updateSpreadRelatedIU(){
        spreadSeries = new XYSeries("spread");
        for (int i = 0; i < spreadValues.size(); i++){
            spreadSeries.add(i, spreadValues.get(i));
        }

        XYSeriesCollection datasetSpread = new XYSeriesCollection();
        datasetSpread.addSeries(spreadSeries);
        XYPlot plotSpread = (XYPlot) chartSpread.getPlot();
        plotSpread.setDataset(datasetSpread);
    }

    private void disableControlsOnStart(){
        algorithmsComboBox.setDisable(true);
        problemsComboBox.setDisable(true);
        algorithmParametersTableView.setDisable(true);
        startButton.setDisable(true);
        stopButton.setDisable(false);

        receiveSolutionSetCount = 0;
        gdErrorProperty.setValue(0.0);
        spreadErrorProperty.setValue(0.0);

//        mutex.release();
//        semaphore.release();

        clearControls();
    }

    private void enableControlsOnStop(){
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
        selectProblem((String)problemsComboBox.getValue());

        Platform.setImplicitExit(false);

        ep.setAlgorithmParameterList(algorithmParametersTableView.getItems());
        eaAlgorithm = ep.getJMetalAlgorithm();
        eaAlgorithm.subscribeCurrentSolutionSetReceiver(this);
        algorithm = eaAlgorithm;

        Runnable task = () -> {
            try { algorithm.run();}
            finally {
                isAlgorithmWorking = false;
                enableControlsOnStop();
            }
        };

        mainLoop = new Thread(task);
        mainLoop.setDaemon(true);
        mainLoop.start();
        isAlgorithmWorking = true;
    }

    public void stopButtonClicked(ActionEvent actionEvent){
        mainLoop.interrupt();
        try {
            algorithm = null;
        } catch (Exception ex)
        {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
        isAlgorithmWorking = false;
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

    private void selectProblem(String problemName){
        ep.setProblemName(problemName);
        try {
            ArrayFront referenceFront = new ArrayFront(ep.getReferenceParetoFront());
            if (referenceFront.getPointDimensions() == 2) {
                seriesFront2D = createFront2D(referenceFront);
                XYPlot plot2D = (XYPlot) chart2D.getPlot();
                XYSeriesCollection dataset2D = new XYSeriesCollection();
                dataset2D.addSeries(seriesFront2D);
                plot2D.setDataset(dataset2D);

                tabPane.getTabs().get(0).setDisable(true);
                tabPane.getTabs().get(1).setDisable(false);
                selectedTab.setValue("tab2d");
                SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                selectionModel.select(1);

                solutionsTableView.getColumns().get(2).setVisible(false);
            }
            else if (referenceFront.getPointDimensions() == 3) {
                serieFront3D = createFront3D(referenceFront);

                XYZPlot plot3D = (XYZPlot) viewer.getChart().getPlot();
                XYZSeriesCollection<String> dataset3D = new XYZSeriesCollection<>();
                dataset3D.add(serieFront3D);
                plot3D.setDataset(dataset3D);

                tabPane.getTabs().get(0).setDisable(false);
                tabPane.getTabs().get(1).setDisable(true);
                selectedTab.setValue("tab3d");
                SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                selectionModel.select(0);
//                tabPane.getTabs().clear();
//                tabPane.getTabs().addAll(tabs);
//                tabPane.getTabs().remove(1);
                solutionsTableView.getColumns().get(2).setVisible(true);
            }

            gdIdicator = new GenerationalDistance<DoubleSolution>(referenceFront);
            igdIdicator = new InvertedGenerationalDistance<DoubleSolution>(referenceFront);
            spreadIdicator = new GeneralizedSpread<DoubleSolution>(referenceFront);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void problemSelected(ActionEvent actionEvent) {
        String selectedProblemName = (String)problemsComboBox.getValue();
        if (selectedProblemName != null && !selectedProblemName.isEmpty()) {
            selectProblem(selectedProblemName);
        }
    }
    //endregion

    //region Methods
    private void FillComboBoxAlgorithms()
    {
        ObservableList<String> algoritmhs = FXCollections.observableArrayList();
        algoritmhs.addAll("DBSPEA2","NSGAII", "SPEA2", "SPEA3");

        algorithmsComboBox.setItems(algoritmhs);
    }

    private void FillComboBoxProblems()
    {
        ObservableList<String> problems = FXCollections.observableArrayList();
        problems.addAll("Binh2", "ConstrEx", "Fonseca", "Golinski", "Kursawe", "Osyczka2", "Schaffer", "Srinivas", "Tanaka", "DTLZ1", "DTLZ2");

        problemsComboBox.setItems(problems);
    }

    private String printQualityIndicators(List<DoubleSolution> population, String paretoFrontFile) throws FileNotFoundException {
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

    //region Orson

    private XYZSeriesCollection createDataset(List<DoubleSolution> solutionSetResult){
        XYZSeriesCollection<String> dataset3D_ = new XYZSeriesCollection<>();

        XYZSeries serie3D_ = new XYZSeries<>(String.valueOf(Math.random()));

        for (DoubleSolution aSolutionSetResult : solutionSetResult) {
            serie3D_.add(aSolutionSetResult.getObjective(0), aSolutionSetResult.getObjective(1), aSolutionSetResult.getObjective(2));
        }

        dataset3D_.add(serie3D_);

        return dataset3D_;
    }

    private XYZSeries createFront3D(ArrayFront front){
        XYZSeries serieFront3D_ = new XYZSeries<>(String.valueOf(Math.random()));

        for (int i = 0; i < front.getNumberOfPoints(); i+=5) {
            if (i <= front.getNumberOfPoints())
                serieFront3D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1), front.getPoint(i).getDimensionValue(2));

            if (i % 5 == 0)
                i+=50;
        }

        return serieFront3D_;
    }

    //endregion

    //region JFREE

    private XYSeriesCollection createDataset2D(List<DoubleSolution> solutionSetResult){
        XYSeriesCollection dataset2D_ = new XYSeriesCollection();
        XYSeries series2D_ = new XYSeries(Math.random());

        for (DoubleSolution aSolutionSetResult : solutionSetResult) {
            series2D_.add(aSolutionSetResult.getObjective(0), aSolutionSetResult.getObjective(1));
        }

        dataset2D_.addSeries(series2D_);

        return dataset2D_;
    }

    private XYSeries createFront2D(ArrayFront front){
        XYSeries seriesFront2D_ = new XYSeries(Math.random());

        int count = front.getNumberOfPoints() / 200;

        for (int i = 0; i < front.getNumberOfPoints(); i+=count) {
            if (i <= front.getNumberOfPoints())
                seriesFront2D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1));

//            if (i % count == 0)
//                i+=count;
        }

        return seriesFront2D_;
    }

    private static JFreeChart createChart2D(XYDataset dataset) {
        return ChartFactory.createScatterPlot("", "X", "Y", dataset);
    }

    private static JFreeChart createLine2D(XYDataset dataset) {
        return ChartFactory.createXYLineChart("", "X", "Y", dataset);
    }
    //endregion
}

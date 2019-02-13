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

    //endregion

    //region Private variables

    private ExperimentParams ep;
    private ObservableList<AlgorithmParameter> algorithmParams = FXCollections.observableArrayList();

    private Algorithm<List<DoubleSolution>> algorithm;
    private AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> eaAlgorithm;

    private final StringProperty selectedTab = new SimpleStringProperty();
    private final StringProperty qiResults = new SimpleStringProperty();
    private final SimpleIntegerProperty receivedSSProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty printedQIProperty = new SimpleIntegerProperty();
    private final SimpleDoubleProperty gdErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty spreadErrorProperty = new SimpleDoubleProperty();

    int receiveSolutionSetCount = 0;
    int printQIsCount = 0;

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
    private XYSeries igdSeries = new XYSeries("gd");

    private JFreeChart chartSpread = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasSpread = new ChartCanvas(chartSpread);
    private XYSeries spreadSeries = new XYSeries("gd");

    private List<DoubleSolution> solutionSetResult = new ArrayList<>();

    private GenerationalDistance gdIdicator = new GenerationalDistance<DoubleSolution>();
    private InvertedGenerationalDistance igdIdicator = new InvertedGenerationalDistance<DoubleSolution>();
    private GeneralizedSpread spreadIdicator = new GeneralizedSpread<DoubleSolution>();

    private final AtomicLong counter = new AtomicLong(-1);
    private static Semaphore semaphore = new Semaphore(0);
    private static Semaphore mutex = new Semaphore(1);

    //endregion

    @Override
    public void initialize(URL url, ResourceBundle rb){
        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (ov, t, t1) -> {
                //lock.lock();
                selectedTab.setValue(t1.getId());
                //lock.unlock();
            }
        );

        XYPlot plotGD = (XYPlot) chartGD.getPlot();
        XYSeriesCollection datasetGD = (XYSeriesCollection)plotGD.getDataset();
        datasetGD.addSeries(gdSeries);
        plotGD.setDataset(datasetGD);

        XYPlot plotIGD = (XYPlot) chartIGD.getPlot();
        XYSeriesCollection datasetIGD = (XYSeriesCollection)plotIGD.getDataset();
        datasetIGD.addSeries(igdSeries);
        plotIGD.setDataset(datasetIGD);

        XYPlot plotSpread = (XYPlot) chartSpread.getPlot();
        XYSeriesCollection datasetSpread = (XYSeriesCollection)plotSpread.getDataset();
        datasetSpread.addSeries(spreadSeries);
        plotSpread.setDataset(datasetSpread);

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
    }

    @Override
    public void ReceiveCurrentSolutionSet(final List<DoubleSolution> solutionSett) {
        try {
            mutex.acquire();

            solutionSetResult = algorithm.getResult();
            if (solutionSetResult.size() == 0) {
                return;
            }

            double gd = gdIdicator.evaluate(solutionSetResult);
            double igd = igdIdicator.evaluate(solutionSetResult);
            double spread = spreadIdicator.evaluate(solutionSetResult);

            gdSeries.add(receiveSolutionSetCount, gd);
            igdSeries.add(receiveSolutionSetCount, igd);
            spreadSeries.add(receiveSolutionSetCount, spread);

            printQIsCount++;
            receiveSolutionSetCount++;

            if (counter.getAndSet(1) != -1)
                return;

            mutex.release();
            semaphore.release();

            Platform.runLater(() -> {
                try {
                    semaphore.acquire();

                    gdErrorProperty.setValue(gd);
                    spreadErrorProperty.setValue(spread);
                    receivedSSProperty.setValue(receiveSolutionSetCount);
                    printedQIProperty.setValue(printQIsCount);

                    if (selectedTab.getValue().toString().equalsIgnoreCase("tab3d")) {
                        //if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
                        XYZPlot plot = (XYZPlot) viewer.getChart().getPlot();
                        XYZSeriesCollection<String> dataset3D = createDataset(solutionSetResult);
                        dataset3D.add(serieFront3D);
                        plot.setDataset(dataset3D);
                        //}
                    } else if (selectedTab.getValue().toString().equalsIgnoreCase("tab2d")) {
                        XYPlot plot2D = (XYPlot) chart2D.getPlot();
                        XYSeriesCollection dataset2D = createDataset2D(solutionSetResult);
                        dataset2D.addSeries(seriesFront2D);
                        plot2D.setDataset(dataset2D);
                    } else if (selectedTab.getValue().toString().equalsIgnoreCase("tabAproximationSet")) {
                        ObservableList<SolutionDto> solutionList = FXCollections.observableArrayList();
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
                            for (int i = 0; i < solutionSetResult.size(); i++) {
                                SolutionDto dto = new SolutionDto();
                                dto.setV1(String.valueOf(solutionSetResult.get(i).getObjective(0)));
                                dto.setV2(String.valueOf(solutionSetResult.get(i).getObjective(1)));
                                solutionList.add(dto);
                            }
                        } else if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
                            for (int i = 0; i < solutionSetResult.size(); i++) {
                                SolutionDto dto = new SolutionDto();
                                dto.setV1(String.valueOf(solutionSetResult.get(i).getObjective(0)));
                                dto.setV2(String.valueOf(solutionSetResult.get(i).getObjective(1)));
                                dto.setV3(String.valueOf(solutionSetResult.get(i).getObjective(2)));
                                solutionList.add(dto);
                            }
                        }

                        solutionsTableView.setItems(solutionList);
                    } else if (selectedTab.getValue().toString().equalsIgnoreCase("tabGD")) {

                        XYSeriesCollection datasetGD = new XYSeriesCollection();
                        datasetGD.addSeries(gdSeries);
                        XYPlot plotGD = (XYPlot) chartGD.getPlot();
                        plotGD.setDataset(datasetGD);
                    } else if (selectedTab.getValue().toString().equalsIgnoreCase("tabIGD")) {
                        XYSeriesCollection datasetIGD = new XYSeriesCollection();
                        datasetIGD.addSeries(igdSeries);
                        XYPlot plotIGD = (XYPlot) chartIGD.getPlot();
                        plotIGD.setDataset(datasetIGD);
                    } else if (selectedTab.getValue().toString().equalsIgnoreCase("tabSread")) {
                        XYSeriesCollection datasetSpread = new XYSeriesCollection();
                        datasetSpread.addSeries(spreadSeries);
                        XYPlot plotSpread = (XYPlot) chartSpread.getPlot();
                        plotSpread.setDataset(datasetSpread);
                    }

                    counter.set(-1);
                    mutex.release();
                } catch (InterruptedException e) {
                    System.out.println("EXCEPTION: ReceiveCurrentSolutionSet()->Platform.runLater()");
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            System.out.println("EXCEPTION: ReceiveCurrentSolutionSet()");
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
            try { algorithm.run();}
            finally { enableControlsOnStop();}
        };

        mainLoop = new Thread(task);
        mainLoop.setDaemon(true);
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
        if (selectedProblemName != null && !selectedProblemName.isEmpty()) {
            ep.setProblemName(selectedProblemName);
            try {
                ArrayFront referenceFront = new ArrayFront(ep.getReferenceParetoFront());
                if (referenceFront.getPointDimensions() == 2) {
                    seriesFront2D = createFront2D(referenceFront);
                    XYPlot plot2D = (XYPlot) chart2D.getPlot();
                    XYSeriesCollection dataset2D = new XYSeriesCollection();
                    dataset2D.addSeries(seriesFront2D);
                    plot2D.setDataset(dataset2D);

                    solutionsTableView.getColumns().get(2).setVisible(false);
                }
                else if (referenceFront.getPointDimensions() == 3) {
                    serieFront3D = createFront3D(referenceFront);

                    XYZPlot plot3D = (XYZPlot) viewer.getChart().getPlot();
                    XYZSeriesCollection<String> dataset3D = new XYZSeriesCollection<>();
                    dataset3D.add(serieFront3D);
                    plot3D.setDataset(dataset3D);

                    solutionsTableView.getColumns().get(2).setVisible(true);
                }

                gdIdicator = new GenerationalDistance<DoubleSolution>(referenceFront);
                igdIdicator = new InvertedGenerationalDistance<DoubleSolution>(referenceFront);
                spreadIdicator = new GeneralizedSpread<DoubleSolution>(referenceFront);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

    //region Methods
    public void FillComboBoxAlgorithms()
    {
        ObservableList<String> algoritmhs = FXCollections.observableArrayList();
        algoritmhs.addAll("DBSPEA2","NSGAII", "SPEA2", "SPEA3");

        algorithmsComboBox.setItems(algoritmhs);
    }

    public void FillComboBoxProblems()
    {
        ObservableList<String> problems = FXCollections.observableArrayList();
        problems.addAll("Binh2", "ConstrEx", "Fonseca", "Golinski", "Kursawe", "Osyczka2", "Schaffer", "Srinivas", "Tanaka", "DTLZ1", "DTLZ2");

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

    //region Orson

    private XYZSeriesCollection createDataset(List<DoubleSolution> solutionSetResult){
        XYZSeriesCollection<String> dataset3D_ = new XYZSeriesCollection<>();

        XYZSeries serie3D_ = new XYZSeries<>(String.valueOf(Math.random()));

        for (int i = 0; i < solutionSetResult.size(); i++) {
            serie3D_.add(solutionSetResult.get(i).getObjective(0), solutionSetResult.get(i).getObjective(1), solutionSetResult.get(i).getObjective(2));
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

        for (int i = 0; i < solutionSetResult.size(); i++) {
            series2D_.add(solutionSetResult.get(i).getObjective(0), solutionSetResult.get(i).getObjective(1));
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
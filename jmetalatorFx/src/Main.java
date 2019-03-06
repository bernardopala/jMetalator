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
import com.orsoncharts.graphics3d.swing.DisplayPanel3D;
import com.orsoncharts.graphics3d.swing.ZoomInAction;
import com.orsoncharts.label.StandardXYZLabelGenerator;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.xyz.ScatterXYZRenderer;
import com.orsoncharts.renderer.xyz.XYZRenderer;
import com.orsoncharts.style.ChartStyles;
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
import javafx.scene.input.ScrollEvent;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public Label igdErrorValueLabel;

    @FXML
    public Label igdPlusErrorValueLabel;

    @FXML
    public Label spreadErrorValueLabel;

    @FXML
    public Label epsilonErrorValueLabel;

    @FXML
    public Label hvErrorValueLabel;

    @FXML
    public Label erErrorValueLabel;

    @FXML
    public ComboBox algorithmsComboBox;

    @FXML
    public ComboBox problemsComboBox;

    @FXML
    public CheckBox showRefPFCheckBox;

    @FXML
    public CheckBox gdCheckBox;

    @FXML
    public CheckBox igdCheckBox;

    @FXML
    public CheckBox igdPlusCheckBox;

    @FXML
    public CheckBox spreadCheckBox;

    @FXML
    public CheckBox epsilonCheckBox;

    @FXML
    public CheckBox hvCheckBox;

    @FXML
    public CheckBox erCheckBox;

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
    public StackPane stackPaneIGDPlus;

    @FXML
    public StackPane stackPaneSpread;

    @FXML
    public StackPane stackPaneEpsilon;

    @FXML
    public StackPane stackPaneHV;

    @FXML
    public StackPane stackPaneER;

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
    private final SimpleDoubleProperty igdErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty igdPlusErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty spreadErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty epsilonErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty hvErrorProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty erErrorProperty = new SimpleDoubleProperty();

    private int receiveSolutionSetCount = 0;

    private Thread mainLoop = new Thread();

    private XYZSeries<String> serieFront3D = new XYZSeries<>("aprox");
    private ScatterXYZRenderer renderer = new ScatterXYZRenderer();
    private XYZPlot plot = new XYZPlot(new XYZSeriesCollection<>(), renderer, new NumberAxis3D("F1"), new NumberAxis3D("F2"), new NumberAxis3D("F3"));

    private Chart3DViewer viewer = new Chart3DViewer(new Chart3D("", "", plot), false);

    private XYSeries seriesFront2D = new XYSeries("front");
    private JFreeChart chart2D = createChart2D(new XYSeriesCollection());
    private ChartCanvas canvas2D = new ChartCanvas(chart2D);

    boolean isGdActive = false;
    double gd = 0;
    private JFreeChart chartGD = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasGD = new ChartCanvas(chartGD);
    private XYSeries gdSeries = new XYSeries("gd");

    boolean isIgdActive = false;
    double igd = 0;
    private JFreeChart chartIGD = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasIGD = new ChartCanvas(chartIGD);
    private XYSeries igdSeries = new XYSeries("igd");

    boolean isIgdPlusActive = false;
    double igdPlus = 0;
    private JFreeChart chartIGDPlus = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasIGDPlus = new ChartCanvas(chartIGDPlus);
    private XYSeries igdPlusSeries = new XYSeries("igdPlus");

    boolean isSpreadActive = false;
    double  spread = 0;
    private JFreeChart chartSpread = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasSpread = new ChartCanvas(chartSpread);
    private XYSeries spreadSeries = new XYSeries("spread");

    boolean isEpsilonActive = false;
    double epsilon = 0;
    private JFreeChart chartEpsilon = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasEpsilon = new ChartCanvas(chartEpsilon);
    private XYSeries epsilonSeries = new XYSeries("epsilon");

    boolean isHvActive = false;
    double hv = 0;
    private JFreeChart chartHV = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasHV = new ChartCanvas(chartHV);
    private XYSeries hvSeries = new XYSeries("hv");

    boolean isErActive = false;
    double  er = 0;
    private JFreeChart chartEr = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasEr = new ChartCanvas(chartEr);
    private XYSeries erSeries = new XYSeries("er");

    boolean isShowingRefPFActive = true;
    int dimCount = 0;

    private List<DoubleSolution> solutionSetResult = new ArrayList<>();

    private GenerationalDistance gdIdicator;
    private InvertedGenerationalDistance igdIdicator;
    private InvertedGenerationalDistancePlus igdPlusIdicator;
    private GeneralizedSpread spreadIdicator;
    private Epsilon epsilonIdicator;
    private PISAHypervolume hvIdicator;
    private ErrorRatio erIdicator;

    private List<Double> gdValues = new ArrayList<>();
    private List<Double> igdValues = new ArrayList<>();
    private List<Double> igdPlusValues = new ArrayList<>();
    private List<Double> spreadValues = new ArrayList<>();
    private List<Double> epsilonValues = new ArrayList<>();
    private List<Double> hvValues = new ArrayList<>();
    private List<Double> erValues = new ArrayList<>();

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
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabIGDPlus")) {
                        updateIGDPlusRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabSread")) {
                        updateSpreadRelatedIU();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabEpsilon")) {
                        updateEpsilonRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabHV")) {
                        updateHvRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabER")) {
                        updateErRelatedIU();
                    }
                }
            }
        );

        clearControls();

        renderer.setSize(0.20);
        renderer.setColors(Colors.getEarthColors());

        viewer.getCanvas().setPanIncrement(0.01D);
        viewer.getCanvas().setRotateIncrement(0.01D);
        viewer.getChart().setStyle(ChartStyles.createIceCubeStyle());
        viewer.setZoomMultiplier(1.05D);
        stackPaneOrson.getChildren().add(viewer);

        viewer.getCanvas().setOnScroll((ScrollEvent event) -> {
            event.consume();
            if (event.getDeltaY() == 0)
                return;

            boolean up = event.getDeltaY() > 0;

            if (up)
                handleZoom(viewer, viewer.getZoomMultiplier());
            else
                handleZoom(viewer, 1.0 / viewer.getZoomMultiplier());
        });

        stackPaneJFREE.getChildren().add(canvas2D);
        stackPaneGD.getChildren().add(canvasGD);
        stackPaneIGD.getChildren().add(canvasIGD);
        stackPaneIGDPlus.getChildren().add(canvasIGDPlus);
        stackPaneSpread.getChildren().add(canvasSpread);
        stackPaneEpsilon.getChildren().add(canvasEpsilon);
        stackPaneHV.getChildren().add(canvasHV);
        stackPaneER.getChildren().add(canvasEr);

        canvas2D.widthProperty().bind( stackPaneJFREE.widthProperty());
        canvas2D.heightProperty().bind( stackPaneJFREE.heightProperty());

        canvasGD.widthProperty().bind( stackPaneGD.widthProperty());
        canvasGD.heightProperty().bind( stackPaneGD.heightProperty());

        canvasIGD.widthProperty().bind( stackPaneIGD.widthProperty());
        canvasIGD.heightProperty().bind( stackPaneIGD.heightProperty());

        canvasIGDPlus.widthProperty().bind( stackPaneIGDPlus.widthProperty());
        canvasIGDPlus.heightProperty().bind( stackPaneIGDPlus.heightProperty());

        canvasSpread.widthProperty().bind( stackPaneSpread.widthProperty());
        canvasSpread.heightProperty().bind( stackPaneSpread.heightProperty());

        canvasEpsilon.widthProperty().bind( stackPaneEpsilon.widthProperty());
        canvasEpsilon.heightProperty().bind( stackPaneEpsilon.heightProperty());

        canvasHV.widthProperty().bind( stackPaneHV.widthProperty());
        canvasHV.heightProperty().bind( stackPaneHV.heightProperty());

        canvasEr.widthProperty().bind( stackPaneER.widthProperty());
        canvasEr.heightProperty().bind( stackPaneER.heightProperty());

        ep = new ExperimentParams();

        qiResultsLabel.textProperty().bind(qiResults);
        receivedSSLabel.textProperty().bind(receivedSSProperty.asString());
        gdErrorValueLabel.textProperty().bind(gdErrorProperty.asString());
        igdErrorValueLabel.textProperty().bind(igdErrorProperty.asString());
        igdPlusErrorValueLabel.textProperty().bind(igdPlusErrorProperty.asString());
        spreadErrorValueLabel.textProperty().bind(spreadErrorProperty.asString());
        epsilonErrorValueLabel.textProperty().bind(epsilonErrorProperty.asString());
        hvErrorValueLabel.textProperty().bind(hvErrorProperty.asString());
        erErrorValueLabel.textProperty().bind(erErrorProperty.asString());
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
        tabPane.getTabs().get(0).setDisable(true);
        tabPane.getTabs().get(1).setDisable(true);
        tabPane.getTabs().get(3).setDisable(true);
        tabPane.getTabs().get(4).setDisable(true);
        tabPane.getTabs().get(5).setDisable(true);
        tabPane.getTabs().get(6).setDisable(true);
        tabPane.getTabs().get(7).setDisable(true);
        tabPane.getTabs().get(8).setDisable(true);
        tabPane.getTabs().get(9).setDisable(true);
        selectedTab.setValue("tabAproximationSet");
        tabPane.getSelectionModel().select(2);

        showRefPFCheckBox.setSelected(true);

        gdCheckBox.setDisable(true);
        igdCheckBox.setDisable(true);
        igdPlusCheckBox.setDisable(true);
        spreadCheckBox.setDisable(true);
        epsilonCheckBox.setDisable(true);
        hvCheckBox.setDisable(true);
        erCheckBox.setDisable(true);

        Object alg = (String)algorithmsComboBox.getItems().get(0);
        algorithmsComboBox.setValue(alg);
        selectAlgorithm(alg.toString());

        Object prob = (String)problemsComboBox.getItems().get(0);
        problemsComboBox.setValue(prob);
        selectProblem(prob.toString());
    }

    private void handleZoom(Chart3DViewer viewer, double multiplier) {
        ViewPoint3D viewPt = viewer.getChart().getViewPoint();
        double minDistance = viewer.getCanvas().getMinViewingDistance();
        double maxDistance = minDistance * viewer.getCanvas().getMaxViewingDistanceMultiplier();
        double valRho = Math.max(minDistance,
                Math.min(maxDistance, viewPt.getRho() * multiplier));
        viewPt.setRho(valRho);
        viewer.getCanvas().draw();
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

        igdPlusValues.clear();
        igdPlusSeries.clear();
        XYSeriesCollection datasetIGDPlus = new XYSeriesCollection();
        datasetIGDPlus.addSeries(igdPlusSeries);
        XYPlot plotIGDPlus = (XYPlot) chartIGDPlus.getPlot();
        plotIGDPlus.setDataset(datasetIGDPlus);

        spreadValues.clear();
        spreadSeries.clear();
        XYSeriesCollection datasetSpread = new XYSeriesCollection();
        datasetSpread.addSeries(spreadSeries);
        XYPlot plotSpread = (XYPlot) chartSpread.getPlot();
        plotSpread.setDataset(datasetSpread);

        epsilonValues.clear();
        epsilonSeries.clear();
        XYSeriesCollection datasetEpsilon = new XYSeriesCollection();
        datasetEpsilon.addSeries(epsilonSeries);
        XYPlot plotEpsilon = (XYPlot) chartEpsilon.getPlot();
        plotEpsilon.setDataset(datasetEpsilon);

        hvValues.clear();
        hvSeries.clear();
        XYSeriesCollection datasetHV = new XYSeriesCollection();
        datasetHV.addSeries(hvSeries);
        XYPlot plotHV = (XYPlot) chartHV.getPlot();
        plotHV.setDataset(datasetHV);

        erValues.clear();
        erSeries.clear();
        XYSeriesCollection datasetEr = new XYSeriesCollection();
        datasetEr.addSeries(erSeries);
        XYPlot plotEr = (XYPlot) chartEr.getPlot();
        plotEr.setDataset(datasetEr);

        solutionSetResult = new ArrayList<>();
        solutionsTableView.getItems().clear();
        counter.set(-1);
    }

    @Override
    public void ReceiveCurrentSolutionSet(final List<DoubleSolution> solutionSett) {
        try {
            lock.lock();

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

            gd = 0;
            if (isGdActive) {
                gd = gdIdicator.evaluate(solutionSetResult);
                gdValues.add(gd);
            }

            igd = 0;
            if (isIgdActive) {
                igd = igdIdicator.evaluate(solutionSetResult);
                igdValues.add(igd);
            }

            igdPlus = 0;
            if (isIgdPlusActive) {
                igdPlus = igdPlusIdicator.evaluate(solutionSetResult);
                igdPlusValues.add(igdPlus);
            }

            spread = 0;
            if (isSpreadActive) {
                spread = spreadIdicator.evaluate(solutionSetResult);
                spreadValues.add(spread);
            }

            epsilon = 0;
            if (isEpsilonActive) {
                epsilon = epsilonIdicator.evaluate(solutionSetResult);
                epsilonValues.add(epsilon);
            }

            hv = 0;
            if (isHvActive) {
                hv = hvIdicator.evaluate(solutionSetResult);
                hvValues.add(hv);
            }

            er = 0;
            if (isErActive) {
                er = erIdicator.evaluate(solutionSetResult);
                erValues.add(er);
            }

            lock.unlock();

            if (counter.getAndSet(1) != -1)
                return;

            Platform.runLater(() -> {
                try {
                    lock.lock();

                    gdErrorProperty.setValue(gd);
                    igdErrorProperty.setValue(igd);
                    igdPlusErrorProperty.setValue(igdPlus);
                    spreadErrorProperty.setValue(spread);
                    epsilonErrorProperty.setValue(epsilon);
                    hvErrorProperty.setValue(hv);
                    erErrorProperty.setValue(er);
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
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabIGDPlus")) {
                        updateIGDPlusRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabSread")) {
                        updateSpreadRelatedIU();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabEpsilon")) {
                        updateEpsilonRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabHV")) {
                        updateHvRelatedUI();
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabER")) {
                        updateErRelatedIU();
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
        XYZSeriesCollection dataset3D = new XYZSeriesCollection();
        if (solutionSetResult != null && solutionSetResult.size() > 0)
            dataset3D.add(createSeries3D(solutionSetResult));
        else
            dataset3D.add(new XYZSeries("3d"));

        if (isShowingRefPFActive)
            dataset3D.add(serieFront3D);
        plot.setDataset(dataset3D);
    }

    private void update2dChartRelatedUI(){
        XYPlot plot2D = (XYPlot) chart2D.getPlot();
        XYSeriesCollection dataset2D = new XYSeriesCollection();
        if (solutionSetResult != null && solutionSetResult.size() > 0)
            dataset2D.addSeries(createSeries2D(solutionSetResult));
        else
            dataset2D.addSeries(new XYSeries("Solution set"));

        if (isShowingRefPFActive)
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

    private void updateIGDPlusRelatedUI(){
        igdPlusSeries = new XYSeries("igdPlus");
        for (int i = 0; i < igdPlusValues.size(); i++){
            igdPlusSeries.add(i, igdPlusValues.get(i));
        }

        XYSeriesCollection datasetIGDPlus = new XYSeriesCollection();
        datasetIGDPlus.addSeries(igdPlusSeries);
        XYPlot plotIGDPlus = (XYPlot) chartIGDPlus.getPlot();
        plotIGDPlus.setDataset(datasetIGDPlus);
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

    private void updateEpsilonRelatedUI(){
        epsilonSeries = new XYSeries("epsilon");
        for (int i = 0; i < epsilonValues.size(); i++){
            epsilonSeries.add(i, epsilonValues.get(i));
        }

        XYSeriesCollection datasetEpsilon = new XYSeriesCollection();
        datasetEpsilon.addSeries(epsilonSeries);
        XYPlot plotEpsilon = (XYPlot) chartEpsilon.getPlot();
        plotEpsilon.setDataset(datasetEpsilon);
    }

    private void updateHvRelatedUI(){
        hvSeries = new XYSeries("hv");
        for (int i = 0; i < hvValues.size(); i++){
            hvSeries.add(i, hvValues.get(i));
        }

        XYSeriesCollection datasetHV = new XYSeriesCollection();
        datasetHV.addSeries(hvSeries);
        XYPlot plotHV = (XYPlot) chartHV.getPlot();
        plotHV.setDataset(datasetHV);
    }

    private void updateErRelatedIU(){
        erSeries = new XYSeries("er");
        for (int i = 0; i < erValues.size(); i++){
            erSeries.add(i, erValues.get(i));
        }

        XYSeriesCollection datasetEr = new XYSeriesCollection();
        datasetEr.addSeries(erSeries);
        XYPlot plotEr = (XYPlot) chartEr.getPlot();
        plotEr.setDataset(datasetEr);
    }

    private void disableControlsOnStart(){
        algorithmsComboBox.setDisable(true);
        problemsComboBox.setDisable(true);
        algorithmParametersTableView.setDisable(true);
        startButton.setDisable(true);
        stopButton.setDisable(false);

        receiveSolutionSetCount = 0;
        gdErrorProperty.setValue(0.0);
        igdErrorProperty.setValue(0.0);
        igdPlusErrorProperty.setValue(0.0);
        spreadErrorProperty.setValue(0.0);

        gdCheckBox.setDisable(true);
        igdCheckBox.setDisable(true);
        igdPlusCheckBox.setDisable(true);
        spreadCheckBox.setDisable(true);
        epsilonCheckBox.setDisable(true);
        hvCheckBox.setDisable(true);
        erCheckBox.setDisable(true);

        clearControls();
    }

    private void enableControlsOnStop(){
        algorithmsComboBox.setDisable(false);
        problemsComboBox.setDisable(false);
        algorithmParametersTableView.setDisable(false);
        startButton.setDisable(false);
        stopButton.setDisable(true);

        gdCheckBox.setDisable(false);
        igdCheckBox.setDisable(false);
        igdPlusCheckBox.setDisable(false);
        spreadCheckBox.setDisable(false);
        epsilonCheckBox.setDisable(false);
        hvCheckBox.setDisable(false);
        erCheckBox.setDisable(false);
    }

    //region Events
    public void startButtonClicked(ActionEvent actionEvent){
        if (!ep.IsReady()){
            new Alert(Alert.AlertType.ERROR, "Select algorithm and problem!").showAndWait();
            return;
        }

        selectProblem((String)problemsComboBox.getValue());
        disableControlsOnStart();

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
        mainLoop.setDaemon(false);
        mainLoop.start();
        isAlgorithmWorking = true;
    }

    public void stopButtonClicked(ActionEvent actionEvent){
        mainLoop.interrupt();

        try {
            mainLoop.join();
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
        selectAlgorithm(selectedAlgorithmName);
    }

    private void selectAlgorithm(String algorithmName){
        if (algorithmName != null && !algorithmName.isEmpty()) {
            ep.setAlgorithmName(algorithmName);
            algorithmParams = FXCollections.observableArrayList(ep.getAlgorithmParameterList());
            algorithmParametersTableView.setItems(algorithmParams);
        }
    }

    private void selectProblem(String problemName){
        gdCheckBox.setDisable(false);
        igdCheckBox.setDisable(false);
        igdPlusCheckBox.setDisable(false);
        spreadCheckBox.setDisable(false);
        epsilonCheckBox.setDisable(false);
        hvCheckBox.setDisable(false);
        erCheckBox.setDisable(false);

        ep.setProblemName(problemName);
        try {
            ArrayFront referenceFront = new ArrayFront(ep.getReferenceParetoFront());
//            FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
//            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);

            if (referenceFront.getPointDimensions() == 2) {
                dimCount = 2;
                seriesFront2D = createFront2D(referenceFront);
                solutionSetResult.clear();
                update2dChartRelatedUI();

                tabPane.getTabs().get(0).setDisable(true);
                tabPane.getTabs().get(1).setDisable(false);
                selectedTab.setValue("tab2d");
                SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                selectionModel.select(1);

                solutionsTableView.getColumns().get(2).setVisible(false);
            }
            else if (referenceFront.getPointDimensions() == 3) {
                dimCount = 3;
                serieFront3D = createFront3D(referenceFront);
                solutionSetResult.clear();
                update3dChartRelatedUI();

                tabPane.getTabs().get(0).setDisable(false);
                tabPane.getTabs().get(1).setDisable(true);
                selectedTab.setValue("tab3d");
                SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                selectionModel.select(0);

                solutionsTableView.getColumns().get(2).setVisible(true);
            }

            gdIdicator = new GenerationalDistance<DoubleSolution>(referenceFront);
            igdIdicator = new InvertedGenerationalDistance<DoubleSolution>(referenceFront);
            igdPlusIdicator = new InvertedGenerationalDistancePlus<DoubleSolution>(referenceFront);
            spreadIdicator = new GeneralizedSpread<DoubleSolution>(referenceFront);
            epsilonIdicator = new Epsilon<DoubleSolution>(referenceFront);
            hvIdicator = new PISAHypervolume(referenceFront);
            erIdicator = new ErrorRatio(referenceFront);

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

    public void showRefPFCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isShowingRefPFActive = chk.isSelected();

            if (dimCount == 2)
                update2dChartRelatedUI();
            else if (dimCount == 3)
                update3dChartRelatedUI();
        }
    }

    public void gdCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isGdActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(3).setDisable(false);
            else
                tabPane.getTabs().get(3).setDisable(true);
        }
    }

    public void igdCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isIgdActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(4).setDisable(false);
            else
                tabPane.getTabs().get(4).setDisable(true);
        }
    }

    public void igdPlusCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isIgdPlusActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(5).setDisable(false);
            else
                tabPane.getTabs().get(5).setDisable(true);
        }
    }

    public void spreadCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isSpreadActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(6).setDisable(false);
            else
                tabPane.getTabs().get(6).setDisable(true);
        }
    }

    public void epsilonCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isEpsilonActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(7).setDisable(false);
            else
                tabPane.getTabs().get(7).setDisable(true);
        }
    }

    public void hvCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isHvActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(8).setDisable(false);
            else
                tabPane.getTabs().get(8).setDisable(true);
        }
    }

    public void erCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isErActive = chk.isSelected();

            if (chk.isSelected())
                tabPane.getTabs().get(9).setDisable(false);
            else
                tabPane.getTabs().get(9).setDisable(true);
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

    private XYZSeries createSeries3D(List<DoubleSolution> solutionSetResult){
        XYZSeries serie3D_ = new XYZSeries<>("Solution set");

        for (DoubleSolution aSolutionSetResult : solutionSetResult) {
            serie3D_.add(aSolutionSetResult.getObjective(0), aSolutionSetResult.getObjective(1), aSolutionSetResult.getObjective(2));
        }

        return serie3D_;
    }

    private XYZSeries createFront3D(ArrayFront front){
        XYZSeries serieFront3D_ = new XYZSeries<>("Pareto front");

        for (int i = 0; i < front.getNumberOfPoints(); i+=5) {
            if (i <= front.getNumberOfPoints())
                serieFront3D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1), front.getPoint(i).getDimensionValue(2));

            if (i % 5 == 0)
                i+=50;
        }

/*
        for (int i = 0; i < front.getNumberOfPoints(); i+=2) {
            if (i <= front.getNumberOfPoints())
                serieFront3D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1), front.getPoint(i).getDimensionValue(2));

            if (i % 2 == 0)
                i+=10;
        }
*/
/*
        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            serieFront3D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1), front.getPoint(i).getDimensionValue(2));
        }

*/
        return serieFront3D_;
    }

    //endregion

    //region JFREE

    private XYSeries createSeries2D(List<DoubleSolution> solutionSetResult){
        XYSeries series2D_ = new XYSeries("Solution set");

        for (DoubleSolution aSolutionSetResult : solutionSetResult) {
            series2D_.add(aSolutionSetResult.getObjective(0), aSolutionSetResult.getObjective(1));
        }

        return series2D_;
    }

    private XYSeries createFront2D(ArrayFront front){
        XYSeries seriesFront2D_ = new XYSeries("Pareto front");

        int count = 1;
        if (front.getNumberOfPoints() > 1000)
            count = front.getNumberOfPoints() / 200;

        for (int i = 0; i < front.getNumberOfPoints(); i+=count) {
            if (i <= front.getNumberOfPoints())
                seriesFront2D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1));

//            if (i % count == 0)
//                i+=count;
        }

        return seriesFront2D_;
    }

    private static JFreeChart createChart2D(XYDataset dataset) {
        return ChartFactory.createScatterPlot("", "F1", "F2", dataset);
    }

    private static JFreeChart createLine2D(XYDataset dataset) {
        return ChartFactory.createXYLineChart("", "F1", "F2", dataset);
    }
    //endregion
}

import com.orsoncharts.Chart3D;
import com.orsoncharts.Colors;
import com.orsoncharts.axis.NumberAxis3D;
import com.orsoncharts.data.xyz.XYZDataItem;
import com.orsoncharts.data.xyz.XYZSeries;
import com.orsoncharts.data.xyz.XYZSeriesCollection;
import com.orsoncharts.fx.Chart3DViewer;
import com.orsoncharts.graphics3d.ViewPoint3D;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.xyz.ScatterXYZRenderer;
import com.orsoncharts.renderer.xyz.XYZRenderer;
import com.orsoncharts.style.ChartStyle;
import com.orsoncharts.style.ChartStyles;
import com.orsoncharts.style.StandardChartStyle;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import jmetalhelpers.ExperimentParams;
import jmetalhelpers.ProblemHelper;
import jmetalhelpers.SolutionDto;
import jmetalhelpers.algorithms.AlgorithmParameter;
import maths.GenLloyd;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataItem;
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
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.util.PointSolution;
import view.PairKeyFactory;
import view.PairValueCell;
import view.PairValueFactory;
import view.TabEnum;

import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main implements CurrentSolutionSetReceiver<DoubleSolution>, Initializable {

    //region View controls
    @FXML
    public TabPane tabPane;

    @FXML
    public ProgressBar progressBar;

//    @FXML
//    public Label qiResultsLabel;

    @FXML
    public Label receivedSSLabel;

    @FXML
    public Label receivedSSCountLabel;

    @FXML
    public Label problemObjCountLabel;

    @FXML
    public Label problemVariableCountLabel;

    @FXML
    public Label problemRefSolutionCountLabel;

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
    public CheckBox showSSCheckBox;

    @FXML
    public CheckBox showRefPFCheckBox;

    @FXML
    public CheckBox showRefPointsCheckBox;

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
    public CheckBox stepByStepCheckBox;

    @FXML
    public Hyperlink selectAllQIsLink;

    @FXML
    public Hyperlink selectNoneQILink;

    @FXML
    public javafx.scene.control.TableView<ObservableList<String>> solutionsTableView;

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
    public Button nextButton;

    @FXML
    public Button stopButton;

    @FXML
    public StackPane stackPaneOrson;

    @FXML
    public StackPane stackPaneJFREE;

    @FXML
    public StackPane stackPaneJFREEPC;

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
    private Map<TabEnum, Boolean> tabEnumVisibilityDictionary = new HashMap<>();
    private List<TabEnum> tabEnumOrder = new ArrayList<>();
    private List<TabEnum> tabEnumCurrentOrder = new ArrayList<>();

    private Stage stage;

    //endregion

    //region Private variables

    FrontNormalizer frontNormalizer;

    private Lock lock = new ReentrantLock();
    private boolean isAlgorithmWorking = false;

    private ExperimentParams ep;
    private ObservableList<AlgorithmParameter> algorithmParams = FXCollections.observableArrayList();

    private Algorithm<List<DoubleSolution>> algorithm;
    private AbstractEvolutionaryAlgorithm<DoubleSolution, List<DoubleSolution>> eaAlgorithm;

    private final StringProperty selectedTab = new SimpleStringProperty();
    private final StringProperty qiResults = new SimpleStringProperty();
    private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty();
    private final SimpleIntegerProperty receivedSSProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty receivedSSCountProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty problemObjCountProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty problemVariableCountProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty problemRefSolutionCountProperty = new SimpleIntegerProperty();

    private final SimpleStringProperty gdErrorProperty = new SimpleStringProperty();
    private final SimpleStringProperty igdErrorProperty = new SimpleStringProperty();
    private final SimpleStringProperty igdPlusErrorProperty = new SimpleStringProperty();
    private final SimpleStringProperty spreadErrorProperty = new SimpleStringProperty();
    private final SimpleStringProperty epsilonErrorProperty = new SimpleStringProperty();
    private final SimpleStringProperty hvErrorProperty = new SimpleStringProperty();
    private final SimpleStringProperty erErrorProperty = new SimpleStringProperty();

    private final SimpleIntegerProperty is2dChartAutoResizing = new SimpleIntegerProperty();
    private final SimpleIntegerProperty isPCChartAutoResizing = new SimpleIntegerProperty();

    private int receiveSolutionSetCount = 0;

    private Thread mainLoop = new Thread();

    private XYZSeries<String> serieFront3D = new XYZSeries<>("aprox");
    private ScatterXYZRenderer renderer = new ScatterXYZRenderer();
    private XYZPlot plot = new XYZPlot(new XYZSeriesCollection<>(), renderer, new NumberAxis3D("F1"), new NumberAxis3D("F2"), new NumberAxis3D("F3"));

    private Chart3DViewer viewer = new Chart3DViewer(new Chart3D("", "", plot), false);

    private XYSeries seriesFront2D = new XYSeries("front");
    private JFreeChart chart2D = createChart2D(new XYSeriesCollection());
    private ChartCanvas canvas2D = new ChartCanvas(chart2D);

    private XYSeriesCollection seriesFrontPC = new XYSeriesCollection();
    private JFreeChart chartPC = createLine2D(new XYSeriesCollection());
    private ChartCanvas canvasPC = new ChartCanvas(chartPC);

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

    boolean isShowingRefPointsActive = false;
    boolean isShowingRefPFActive = true;
    boolean isShowingSSActive = true;

    boolean isStepByStepActive = false;

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
    private DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
    private DecimalFormat decimalFormat2 = new DecimalFormat("0.0000E0");



    private final AtomicLong counter = new AtomicLong(-1);

    //endregion

    private void initializeTabs(){
        tabs = FXCollections.observableArrayList(tabPane.getTabs());
        tabPane.getTabs().clear();

        tabEnumVisibilityDictionary.put(TabEnum.Chart3D, false);
        tabEnumVisibilityDictionary.put(TabEnum.Chart2D, false);
        tabEnumVisibilityDictionary.put(TabEnum.ChartParallelCoordinates, false);
        tabEnumVisibilityDictionary.put(TabEnum.AproximationSet, false);
        tabEnumVisibilityDictionary.put(TabEnum.GD, false);
        tabEnumVisibilityDictionary.put(TabEnum.IGD, false);
        tabEnumVisibilityDictionary.put(TabEnum.IGDPlus, false);
        tabEnumVisibilityDictionary.put(TabEnum.Spread, false);
        tabEnumVisibilityDictionary.put(TabEnum.Epsilon, false);
        tabEnumVisibilityDictionary.put(TabEnum.HV, false);
        tabEnumVisibilityDictionary.put(TabEnum.ER, false);

        tabEnumOrder.add(TabEnum.Chart3D);
        tabEnumOrder.add(TabEnum.Chart2D);
        tabEnumOrder.add(TabEnum.ChartParallelCoordinates);
        tabEnumOrder.add(TabEnum.AproximationSet);
        tabEnumOrder.add(TabEnum.GD);
        tabEnumOrder.add(TabEnum.IGD);
        tabEnumOrder.add(TabEnum.IGDPlus);
        tabEnumOrder.add(TabEnum.Spread);
        tabEnumOrder.add(TabEnum.Epsilon);
        tabEnumOrder.add(TabEnum.HV);
        tabEnumOrder.add(TabEnum.ER);
    }

    private void setTabVisiblity(TabEnum key, Boolean value){
        tabPane.getTabs().clear();
        tabEnumCurrentOrder.clear();

        tabEnumVisibilityDictionary.replace(key, value);
        tabEnumOrder.forEach((k) -> {
                int id = tabEnumOrder.indexOf(k);
                if (tabEnumVisibilityDictionary.get(k).booleanValue()) {
                    tabEnumCurrentOrder.add(k);
                    tabPane.getTabs().add(tabs.get(id));
                }
            }
        );
    }

    public void setStage(Stage s){
        this.stage = s;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb){
        initializeTabs();


        setTabVisiblity(TabEnum.Chart2D, true);
        setTabVisiblity(TabEnum.AproximationSet, true);

        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (ov, t, t1) -> {
                if (t == null || t1 == null)
                    return;

                selectedTab.setValue(t1.getId());

                if ((!isAlgorithmWorking || isAlgorithmWorking && isStepByStepActive) && solutionSetResult != null && solutionSetResult.size() > 0) {
                    if (selectedTab.getValue().equalsIgnoreCase("tab3d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
                            update3dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tab2d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
                            update2dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabParallelCoordinates")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() > 3) {
                            updatePCChartRelatedUI();
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
        renderer.setColors(Colors.getColors2());

        viewer.getCanvas().setPanIncrement(0.01D);
        viewer.getCanvas().setRotateIncrement(0.01D);
        StandardChartStyle s = (StandardChartStyle )viewer.getChart().getStyle();
        s.setAxisLabelFont(new Font("Tahoma", 1,14));
        s.setLegendFooterFont(new Font("Tahoma", 0,12));
        s.setLegendHeaderFont(new Font("Tahoma", 0,12));
        s.setLegendItemFont(new Font("Tahoma", 0,12));
        s.setAxisTickLabelFont(new Font("Tahoma", 0,12));

        viewer.setZoomMultiplier(0.95D);
        //viewer.getCanvas().getChart().setViewPoint(new ViewPoint3D(0.7502, 1.1555, 38.1964, 45));
        stackPaneOrson.getChildren().add(viewer);
        stackPaneOrson.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    //ViewPoint3D vp = viewer.getCanvas().getChart().getViewPoint();;
                    resetZoom(viewer.getChart());
                }
            }
        });

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

        is2dChartAutoResizing.set(1);
        stackPaneJFREE.getChildren().add(canvas2D);
        stackPaneJFREE.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chart2D, false);
                    is2dChartAutoResizing.set(1);
                }
            }
        });
        stackPaneJFREE.setOnScroll((ScrollEvent event) -> {
            is2dChartAutoResizing.set(0);
        });

        isPCChartAutoResizing.set(1);
        stackPaneJFREEPC.getChildren().add(canvasPC);
        stackPaneJFREEPC.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartPC, false);
                    isPCChartAutoResizing.set(1);
                }
            }
        });
        stackPaneJFREEPC.setOnScroll((ScrollEvent event) -> {
            isPCChartAutoResizing.set(0);
        });

        stackPaneGD.getChildren().add(canvasGD);
        stackPaneGD.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartGD);
                }
            }
        });
        stackPaneIGD.getChildren().add(canvasIGD);
        stackPaneIGD.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartIGD);
                }
            }
        });
        stackPaneIGDPlus.getChildren().add(canvasIGDPlus);
        stackPaneIGDPlus.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartIGDPlus);
                }
            }
        });
        stackPaneSpread.getChildren().add(canvasSpread);
        stackPaneSpread.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartSpread);
                }
            }
        });
        stackPaneEpsilon.getChildren().add(canvasEpsilon);
        stackPaneEpsilon.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartEpsilon);
                }
            }
        });
        stackPaneHV.getChildren().add(canvasHV);
        stackPaneHV.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartHV);
                }
            }
        });
        stackPaneER.getChildren().add(canvasEr);
        stackPaneER.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    resetZoom(chartEr);
                }
            }
        });

        canvas2D.widthProperty().bind( stackPaneJFREE.widthProperty());
        canvas2D.heightProperty().bind( stackPaneJFREE.heightProperty());

        canvasPC.widthProperty().bind( stackPaneJFREEPC.widthProperty());
        canvasPC.heightProperty().bind( stackPaneJFREEPC.heightProperty());
        XYPlot plotPC = (XYPlot) chartPC.getPlot();
        NumberAxis xAxis = (NumberAxis)plotPC.getDomainAxis();
        xAxis.setTickUnit(new NumberTickUnit(1));
        xAxis.setLabel("Fn");
        NumberAxis yAxis = (NumberAxis)plotPC.getRangeAxis();
        yAxis.setLabel("");
        plotPC.setRangeAxis(yAxis);
        canvasPC.getChart().removeLegend();

//        LegendItemCollection chartLegend = new LegendItemCollection();
//        Shape shape = new Rectangle(7, 7);
//        chartLegend.add(new LegendItem("Solution set", null, null, null, shape, Color.red));
//        chartLegend.add(new LegendItem("Pareto front", null, null, null, shape, Color.darkGray));
//        plotPC.setFixedLegendItems(chartLegend);
        LegendTitle lt = new LegendTitle((Plot)chart2D.getPlot());
        lt.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        lt.setBackgroundPaint(Color.white);
        lt.setPosition(RectangleEdge.BOTTOM);
        canvasPC.getChart().addLegend(lt);

        canvasGD.widthProperty().bind( stackPaneGD.widthProperty());
        canvasGD.heightProperty().bind( stackPaneGD.heightProperty());
        setChartDefaults(chartGD);

        canvasIGD.widthProperty().bind( stackPaneIGD.widthProperty());
        canvasIGD.heightProperty().bind( stackPaneIGD.heightProperty());
        setChartDefaults(chartIGD);

        canvasIGDPlus.widthProperty().bind( stackPaneIGDPlus.widthProperty());
        canvasIGDPlus.heightProperty().bind( stackPaneIGDPlus.heightProperty());
        setChartDefaults(chartIGDPlus);

        canvasSpread.widthProperty().bind( stackPaneSpread.widthProperty());
        canvasSpread.heightProperty().bind( stackPaneSpread.heightProperty());
        setChartDefaults(chartSpread);

        canvasEpsilon.widthProperty().bind( stackPaneEpsilon.widthProperty());
        canvasEpsilon.heightProperty().bind( stackPaneEpsilon.heightProperty());
        setChartDefaults(chartEpsilon);

        canvasHV.widthProperty().bind( stackPaneHV.widthProperty());
        canvasHV.heightProperty().bind( stackPaneHV.heightProperty());
        setChartDefaults(chartHV);

        canvasEr.widthProperty().bind( stackPaneER.widthProperty());
        canvasEr.heightProperty().bind( stackPaneER.heightProperty());
        setChartDefaults(chartEr);

        ep = new ExperimentParams();

        //qiResultsLabel.textProperty().bind(qiResults);
        progressBar.progressProperty().bind(progressProperty);
        receivedSSLabel.textProperty().bind(receivedSSProperty.asString());
        receivedSSCountLabel.textProperty().bind(receivedSSCountProperty.asString());
        problemObjCountLabel.textProperty().bind(problemObjCountProperty.asString());
        problemVariableCountLabel.textProperty().bind(problemVariableCountProperty.asString());
        problemRefSolutionCountLabel.textProperty().bind(problemRefSolutionCountProperty.asString());
        gdErrorValueLabel.textProperty().bind(gdErrorProperty);
        igdErrorValueLabel.textProperty().bind(igdErrorProperty);
        igdPlusErrorValueLabel.textProperty().bind(igdPlusErrorProperty);
        spreadErrorValueLabel.textProperty().bind(spreadErrorProperty);
        epsilonErrorValueLabel.textProperty().bind(epsilonErrorProperty);
        hvErrorValueLabel.textProperty().bind(hvErrorProperty);
        erErrorValueLabel.textProperty().bind(erErrorProperty);
        solutionsV1TableColumn.setCellValueFactory(r -> r.getValue().v1Property());
        solutionsV2TableColumn.setCellValueFactory(r -> r.getValue().v2Property());
        solutionsV3TableColumn.setCellValueFactory(r -> r.getValue().v3Property());

        algorithmParametersNameTableColumn.setCellValueFactory(new PairKeyFactory());
        algorithmParametersValueTableColumn.setCellValueFactory(new PairValueFactory());
        algorithmParametersValueTableColumn.setCellFactory(column -> new PairValueCell());

        FillComboBoxAlgorithms();
        FillComboBoxProblems();
        enableControlsOnStop();
        nextButton.setDisable(true);

        selectedTab.setValue("tab2d");
        tabPane.getSelectionModel().select(0);

        showSSCheckBox.setSelected(true);
        showRefPFCheckBox.setSelected(true);
        showRefPointsCheckBox.setSelected(true);

        gdCheckBox.setDisable(true);
        igdCheckBox.setDisable(true);
        igdPlusCheckBox.setDisable(true);
        spreadCheckBox.setDisable(true);
        epsilonCheckBox.setDisable(true);
        hvCheckBox.setDisable(true);
        erCheckBox.setDisable(true);

        gdErrorProperty.setValue("0.0");
        igdErrorProperty.setValue("0.0");
        igdPlusErrorProperty.setValue("0.0");
        spreadErrorProperty.setValue("0.0");
        epsilonErrorProperty.setValue("0.0");
        hvErrorProperty.setValue("0.0");
        erErrorProperty.setValue("0.0");

        Object alg = (String)algorithmsComboBox.getItems().get(0);
        algorithmsComboBox.setValue(alg);
        selectAlgorithm(alg.toString());

        Object prob = (String)problemsComboBox.getItems().get(0);
        problemsComboBox.setValue(prob);
        selectProblem(prob.toString());
    }

    private void setChartDefaults(JFreeChart chart){
        XYPlot _plot = (XYPlot) chart.getPlot();
        NumberAxis _xAxis = (NumberAxis)_plot.getDomainAxis();
        _xAxis.setTickUnit(new NumberTickUnit(1));
    }

    private void setChartAutoTick(JFreeChart chart){
        XYPlot _plot = (XYPlot) chart.getPlot();
        NumberAxis _xAxis = (NumberAxis)_plot.getDomainAxis();
        _xAxis.setAutoTickUnitSelection(true);
    }

    private void resetZoom(JFreeChart chart){
        resetZoom(chart, true);
    }

    private void resetZoom(JFreeChart chart, boolean forceZeroOnMinY){
        XYPlot plot2D = (XYPlot) chart.getPlot();
        XYSeriesCollection ds = (XYSeriesCollection)plot2D.getDataset();

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (XYSeries s : (List<XYSeries>)ds.getSeries()) {
            List<XYDataItem> items = (List<XYDataItem>)s.getItems();
            for (XYDataItem item : items) {
                if (item.getX().doubleValue() < minX)
                    minX = item.getX().doubleValue();
                if (item.getX().doubleValue() > maxX)
                    maxX = item.getX().doubleValue();
                if (item.getY().doubleValue() < minY)
                    minY = item.getY().doubleValue();
                if (item.getY().doubleValue() > maxY)
                    maxY = item.getY().doubleValue();
            }
        }

        if (forceZeroOnMinY && minY >= 0)
            minY = 0;

        plot2D.getDomainAxis().setRange(minX,maxX);
        plot2D.getRangeAxis().setRange(minY,maxY);
        plot2D.getDomainAxis().setAutoRange(true);
        plot2D.getRangeAxis().setAutoRange(true);
    }

    private void resetZoom(Chart3D chart){
        XYZPlot plot = (XYZPlot) chart.getPlot();
        XYZSeriesCollection dataset3D = (XYZSeriesCollection)plot.getDataset();


        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (XYZSeries s : (List<XYZSeries>)dataset3D) {
            List<XYZDataItem> items = s.getItems();
            for (XYZDataItem item : items) {
                if (item.getX() < minX)
                    minX = item.getX();
                if (item.getX() > maxX)
                    maxX = item.getX();
                if (item.getY() < minY)
                    minY = item.getY();
                if (item.getY() > maxY)
                    maxY = item.getY();
                if (item.getZ() < minZ)
                    minZ = item.getZ();
                if (item.getZ() > maxZ)
                    maxZ = item.getZ();
            }
        }
        renderer.getPlot().getXAxis().setRange(minX, maxX);
        renderer.getPlot().getYAxis().setRange(minY, maxY);
        renderer.getPlot().getZAxis().setRange(minZ, maxZ);
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
                Platform.runLater(() -> {
                    receivedSSProperty.setValue(receiveSolutionSetCount);
                    progressProperty.setValue(getProgress(receiveSolutionSetCount));
                    receivedSSCountProperty.setValue(solutionSetResult.size());
                });
                lock.unlock();
                return;
            }

            receiveSolutionSetCount++;

            if (receiveSolutionSetCount == 20){
                setChartAutoTick(chartGD);
                setChartAutoTick(chartIGD);
                setChartAutoTick(chartIGDPlus);
                setChartAutoTick(chartSpread);
                setChartAutoTick(chartHV);
                setChartAutoTick(chartEpsilon);
                setChartAutoTick(chartEr);
            }

            Front front = new ArrayFront(solutionSetResult);
            Front normalizedFront = frontNormalizer.normalize(front) ;
            List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront) ;

            gd = 0;
            if (isGdActive) {
                gd = gdIdicator.evaluate(normalizedPopulation);
                gdValues.add(gd);
            }

            igd = 0;
            if (isIgdActive) {
                igd = igdIdicator.evaluate(normalizedPopulation);
                igdValues.add(igd);
            }

            igdPlus = 0;
            if (isIgdPlusActive) {
                igdPlus = igdPlusIdicator.evaluate(normalizedPopulation);
                igdPlusValues.add(igdPlus);
            }

            spread = 0;
            if (isSpreadActive) {
                spread = spreadIdicator.evaluate(normalizedPopulation);
                spreadValues.add(spread);
            }

            epsilon = 0;
            if (isEpsilonActive) {
                epsilon = epsilonIdicator.evaluate(normalizedPopulation);
                epsilonValues.add(epsilon);
            }

            hv = 0;
            if (isHvActive) {
                hv = hvIdicator.evaluate(normalizedPopulation);
                hvValues.add(hv);
            }

            er = 0;
            if (isErActive) {
                er = erIdicator.evaluate(normalizedPopulation);
                erValues.add(er);
            }

            lock.unlock();

            if (counter.getAndSet(1) != -1)
                return;

            Platform.runLater(() -> {
                try {
                    lock.lock();

                    progressProperty.setValue(getProgress(receiveSolutionSetCount));
                    gdErrorProperty.setValue(decimalFormat.format(gd) + " (" + decimalFormat2.format(gd) + ")");
                    igdErrorProperty.setValue(decimalFormat.format(igd) + " (" + decimalFormat2.format(igd) + ")");
                    igdPlusErrorProperty.setValue(decimalFormat.format(igdPlus) + " (" + decimalFormat2.format(igdPlus) + ")");
                    spreadErrorProperty.setValue(decimalFormat.format(spread) + " (" + decimalFormat2.format(spread) + ")");
                    epsilonErrorProperty.setValue(decimalFormat.format(epsilon) + " (" + decimalFormat2.format(epsilon) + ")");
                    hvErrorProperty.setValue(decimalFormat.format(hv) + " (" + decimalFormat2.format(hv) + ")");
                    erErrorProperty.setValue(decimalFormat.format(er) + " (" + decimalFormat2.format(er) + ")");
                    receivedSSProperty.setValue(receiveSolutionSetCount);
                    receivedSSCountProperty.setValue(solutionSetResult.size());

                    if (selectedTab.getValue().equalsIgnoreCase("tab3d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
                            update3dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tab2d")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
                            update2dChartRelatedUI();
                        }
                    } else if (selectedTab.getValue().equalsIgnoreCase("tabParallelCoordinates")) {
                        if (solutionSetResult.get(0).getNumberOfObjectives() > 3) {
                            updatePCChartRelatedUI();
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
                    System.out.println("EXCEPTION: ReceiveCurrentSolutionSet()->Platform.runLater()" + e.getStackTrace());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println("EXCEPTION: ReceiveCurrentSolutionSet()");
            e.printStackTrace();
        }

        if (isStepByStepActive) {
            mainLoop.suspend();
        }

        if (!isAlgorithmWorking)
            lock.unlock();
    }

    private void update3dChartRelatedUI(){
        XYZPlot plot = (XYZPlot) viewer.getChart().getPlot();
        XYZSeriesCollection dataset3D = new XYZSeriesCollection();
        XYZSeries ssSeries = createSeries3D(solutionSetResult);
        if (isShowingSSActive && solutionSetResult != null && solutionSetResult.size() > 0)
            dataset3D.add(ssSeries);
        else
            dataset3D.add(new XYZSeries("Solution set"));

         ScatterXYZRenderer rndr = (ScatterXYZRenderer)plot.getRenderer();
         rndr.setColors(Color.RED, Color.darkGray, Color.GREEN);

        //start
        XYZSeries laSeries = new XYZSeries("la");

        if (isShowingRefPointsActive && solutionSetResult != null && solutionSetResult.size() > 0) {

            ArrayList<double[]> points = new ArrayList<double[]>();
            for (int i = 0; i < solutionSetResult.size(); i++) {
                points.add(arrayOf(solutionSetResult.get(i).getObjective(0), solutionSetResult.get(i).getObjective(1), solutionSetResult.get(i).getObjective(2)));
            }

            GenLloyd gl = new GenLloyd(points.toArray(new double[points.size()][3]));
            double[][] results = gl.getClusterPoints(20);
            for (double[] point : results) {
                laSeries.add(point[0], point[1], point[2]);
            }
            /*
            List<DoubleSolution> edList = EvenlyDistributedSolutions.get(solutionSetResult, 50);
            for (DoubleSolution point : edList) {
                laSeries.add(point.getObjective(0), point.getObjective(1), point.getObjective(2));
            }
            */
        }

        if (isShowingRefPointsActive)
            dataset3D.add(laSeries);
        //end

        if (isShowingRefPFActive)
            dataset3D.add(serieFront3D);

        plot.setDataset(dataset3D);

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        List<XYZSeries> union = new ArrayList<>();
        union.add(ssSeries);
        union.add(serieFront3D);

        for (XYZSeries s : union) {
            List<XYZDataItem> items = s.getItems();
            for (XYZDataItem item : items) {
                if (item.getX() < minX)
                    minX = item.getX();
                if (item.getX() > maxX)
                    maxX = item.getX();
                if (item.getY() < minY)
                    minY = item.getY();
                if (item.getY() > maxY)
                    maxY = item.getY();
                if (item.getZ() < minZ)
                    minZ = item.getZ();
                if (item.getZ() > maxZ)
                    maxZ = item.getZ();
            }
        }
        renderer.getPlot().getXAxis().setRange(minX, maxX);
        renderer.getPlot().getYAxis().setRange(minY, maxY);
        renderer.getPlot().getZAxis().setRange(minZ, maxZ);
    }

    private void updatePCChartRelatedUI(){
        XYPlot plotPC = (XYPlot) chartPC.getPlot();

        XYSeriesCollection datasetPC = new XYSeriesCollection();
        XYSeriesCollection datasetFrontPC = new XYSeriesCollection();

        if (isShowingSSActive && solutionSetResult != null && solutionSetResult.size() > 0)
        {
            datasetPC = createSeriesCollectionPC(solutionSetResult);
        }

//        if (isShowingSSActive && solutionSetResult != null && solutionSetResult.size() > 0)
//            datasetPC.addSeries(ssSeries);
//        else
//            datasetPC.addSeries(new XYSeries("Solution set"));

        //start

//        XYSeries laSeries = new XYSeries("la");
//
//        if (solutionSetResult != null && solutionSetResult.size() > 0) {
//
//            ArrayList<double[]> points = new ArrayList<double[]>();
//            for (int i = 0; i < solutionSetResult.size(); i++) {
//                points.add(arrayOf(solutionSetResult.get(i).getObjective(0), solutionSetResult.get(i).getObjective(1)));
//            }
//            /*
//            GenLloyd gl = new GenLloyd(points.toArray(new double[points.size()][2]));
//            double[][] results = gl.getClusterPoints(20);
//            for (double[] point : results) {
//                laSeries.add(point[0], point[1]);
//            }
//            */
///*
//            GoodDistribution gd = new GoodDistribution();
//            double[][] results = gd.get(solutionSetResult);
//            for (double[] point : results) {
//                laSeries.add(point[0], point[1]);
//            }
//*/
//            /*
//            List<DoubleSolution> edList = EvenlyDistributedSolutions.get(solutionSetResult, 50);
//            for (DoubleSolution point : edList) {
//                laSeries.add(point.getObjective(0), point.getObjective(1));
//            }
//            */
//        }
//
//        if (isShowingRefPointsActive)
//            datasetPC.addSeries(laSeries);
        //end

        if (isShowingRefPFActive)
        {
            for (int k = 0; k < seriesFrontPC.getSeriesCount(); k++) {
                datasetFrontPC.addSeries(seriesFrontPC.getSeries(k));
            }
        }

        plotPC.setDataset(0,datasetPC);
        XYItemRenderer renderer0 = new XYLineAndShapeRenderer(true, false);
        for (int p = 0; p < datasetPC.getSeriesCount(); p++)
            renderer0.setSeriesPaint(p, Color.red);
        plotPC.setRenderer(0, renderer0);

        plotPC.setDataset(1,datasetFrontPC);
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        for (int p = 0; p < datasetFrontPC.getSeriesCount(); p++)
            renderer1.setSeriesPaint(p, Color.darkGray);
        plotPC.setRenderer(1, renderer1);

        if (isPCChartAutoResizing.get() == 1) {
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (int d = 0; d < plotPC.getDatasetCount(); d++) {
                XYSeriesCollection ds = (XYSeriesCollection) plotPC.getDataset(d);
                for (int j = 0; j < ds.getSeriesCount(); j++) {
                    XYSeries s = ds.getSeries(j);
                    List<XYDataItem> items = (List<XYDataItem>) s.getItems();
                    for (XYDataItem item : items) {
                        if (item.getX().doubleValue() < minX)
                            minX = item.getX().doubleValue();
                        if (item.getX().doubleValue() > maxX)
                            maxX = item.getX().doubleValue();
                        if (item.getY().doubleValue() < minY)
                            minY = item.getY().doubleValue();
                        if (item.getY().doubleValue() > maxY)
                            maxY = item.getY().doubleValue();
                    }
                }
            }

            if (minX == Double.MAX_VALUE)
                minX = 0;
            if (maxX == Double.MIN_VALUE)
                maxX = 1;
            if (minY == Double.MAX_VALUE)
                minY = 0;
            if (maxY == Double.MIN_VALUE)
                maxY = 1;


            plotPC.getDomainAxis().setRange(minX, maxX);
            plotPC.getRangeAxis().setRange(minY, maxY);
        }
//        chart2D.getXYPlot().getRangeAxis(0).setRange(minX,maxX);
//        chart2D.getXYPlot().getRangeAxis(1).setRange(minY,maxY);
    }

    private void update2dChartRelatedUI(){
        XYPlot plot2D = (XYPlot) chart2D.getPlot();
        XYSeriesCollection dataset2D = new XYSeriesCollection();
        XYSeries ssSeries = createSeries2D(solutionSetResult);
        plot2D.setDataset(dataset2D);

        if (isShowingSSActive && solutionSetResult != null && solutionSetResult.size() > 0)
            dataset2D.addSeries(ssSeries);
        else
            dataset2D.addSeries(new XYSeries("Solution set"));

        XYLineAndShapeRenderer rndr0 = (XYLineAndShapeRenderer)plot2D.getRenderer(0);
        rndr0.setUseOutlinePaint(true);

        rndr0.setSeriesShape(0, new Rectangle(8,8));
        rndr0.setSeriesPaint(0, Color.RED);
        rndr0.setSeriesOutlinePaint(0, Color.darkGray);
        rndr0.setSeriesOutlineStroke(0, new BasicStroke(1));

        rndr0.setSeriesShape(1, new Rectangle(6,6));
        rndr0.setSeriesPaint(1, Color.darkGray);
        rndr0.setSeriesOutlinePaint(1, Color.gray);
        rndr0.setSeriesOutlineStroke(1, new BasicStroke(1));

        rndr0.setSeriesShape(2, new Rectangle(8,8));
        rndr0.setSeriesPaint(2, Color.GREEN);
        rndr0.setSeriesOutlinePaint(2, Color.BLACK);
        rndr0.setSeriesOutlineStroke(2, new BasicStroke(1));
        //start
        XYSeries laSeries = new XYSeries("la");

        if (solutionSetResult != null && solutionSetResult.size() > 0) {

            ArrayList<double[]> points = new ArrayList<double[]>();
            for (int i = 0; i < solutionSetResult.size(); i++) {
                points.add(arrayOf(solutionSetResult.get(i).getObjective(0), solutionSetResult.get(i).getObjective(1)));
            }
            /*
            GenLloyd gl = new GenLloyd(points.toArray(new double[points.size()][2]));
            double[][] results = gl.getClusterPoints(20);
            for (double[] point : results) {
                laSeries.add(point[0], point[1]);
            }
            */
/*
            GoodDistribution gd = new GoodDistribution();
            double[][] results = gd.get(solutionSetResult);
            for (double[] point : results) {
                laSeries.add(point[0], point[1]);
            }
*/
            /*
            List<DoubleSolution> edList = EvenlyDistributedSolutions.get(solutionSetResult, 50);
            for (DoubleSolution point : edList) {
                laSeries.add(point.getObjective(0), point.getObjective(1));
            }
            */
        }

        if (isShowingRefPointsActive) {
            dataset2D.addSeries(laSeries);
//            XYItemRenderer renderer1 = new XYDotRenderer();
//            renderer1.setSeriesPaint(1, Color.GREEN);
//            plot2D.setRenderer(1, renderer1);
        }
        //end

        if (isShowingRefPFActive) {
            dataset2D.addSeries(seriesFront2D);
//            XYItemRenderer renderer2 = new XYDotRenderer();
//            renderer2.setSeriesPaint(2, Color.BLACK);
//            plot2D.setRenderer(2, renderer2);
        }



        if (is2dChartAutoResizing.get() == 1) {
            List<XYSeries> union = new ArrayList<>();
            union.add(ssSeries);
            union.add(seriesFront2D);
            union.add(laSeries);

            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (XYSeries s : union) {
                List<XYDataItem> items = (List<XYDataItem>) s.getItems();
                for (XYDataItem item : items) {
                    if (item.getX().doubleValue() < minX)
                        minX = item.getX().doubleValue();
                    if (item.getX().doubleValue() > maxX)
                        maxX = item.getX().doubleValue();
                    if (item.getY().doubleValue() < minY)
                        minY = item.getY().doubleValue();
                    if (item.getY().doubleValue() > maxY)
                        maxY = item.getY().doubleValue();
                }
            }

            plot2D.getDomainAxis().setRange(minX, maxX);
            plot2D.getRangeAxis().setRange(minY, maxY);
        }
//        chart2D.getXYPlot().getRangeAxis(0).setRange(minX,maxX);
//        chart2D.getXYPlot().getRangeAxis(1).setRange(minY,maxY);
    }

    private void updateSetRelatedUI(){
        solutionsTableView.getItems().clear();
        int count = solutionSetResult.size();// get(0).getNumberOfObjectives();

        for (int i = 0; i < count; i++) {
            ObservableList<String> solutionList = FXCollections.observableArrayList();
            for (int j = 0; j < solutionSetResult.get(i).getNumberOfObjectives(); j++){
                solutionList.add(String.valueOf(solutionSetResult.get(i).getObjective(j)));
            }

            solutionsTableView.getItems().add(solutionList);
        }

//        ObservableList<SolutionDto> solutionList = FXCollections.observableArrayList();
//        if (solutionSetResult.get(0).getNumberOfObjectives() == 2) {
//            for (DoubleSolution aSolutionSetResult : solutionSetResult) {
//                SolutionDto dto = new SolutionDto();
//                dto.setV1(String.valueOf(aSolutionSetResult.getObjective(0)));
//                dto.setV2(String.valueOf(aSolutionSetResult.getObjective(1)));
//                solutionList.add(dto);
//            }
//        } else if (solutionSetResult.get(0).getNumberOfObjectives() == 3) {
//            for (DoubleSolution aSolutionSetResult : solutionSetResult) {
//                SolutionDto dto = new SolutionDto();
//                dto.setV1(String.valueOf(aSolutionSetResult.getObjective(0)));
//                dto.setV2(String.valueOf(aSolutionSetResult.getObjective(1)));
//                dto.setV3(String.valueOf(aSolutionSetResult.getObjective(2)));
//                solutionList.add(dto);
//            }
//        } else if (solutionSetResult.get(0).getNumberOfObjectives() > 3) {
//            for (DoubleSolution aSolutionSetResult : solutionSetResult) {
//                SolutionDto dto = new SolutionDto();
//                dto.setV1(String.valueOf(aSolutionSetResult.getObjective(0)));
//                dto.setV2(String.valueOf(aSolutionSetResult.getObjective(1)));
//                dto.setV3(String.valueOf(aSolutionSetResult.getObjective(2)));
//                solutionList.add(dto);
//            }
//        }

        //solutionsTableView.setItems(solutionList);
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
        if (stepByStepCheckBox.isSelected())
            nextButton.setDisable(false);
        stopButton.setDisable(false);

        receiveSolutionSetCount = 0;
        progressProperty.setValue(0);
        gdErrorProperty.setValue("0.0");
        igdErrorProperty.setValue("0.0");
        igdPlusErrorProperty.setValue("0.0");
        spreadErrorProperty.setValue("0.0");
        epsilonErrorProperty.setValue("0.0");
        hvErrorProperty.setValue("0.0");
        erErrorProperty.setValue("0.0");

        selectAllQIsLink.setDisable(true);
        selectNoneQILink.setDisable(true);
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
        nextButton.setDisable(true);
        stopButton.setDisable(true);

        selectAllQIsLink.setDisable(false);
        selectNoneQILink.setDisable(false);
        gdCheckBox.setDisable(false);
        igdCheckBox.setDisable(false);
        igdPlusCheckBox.setDisable(false);
        spreadCheckBox.setDisable(false);
        epsilonCheckBox.setDisable(false);
        hvCheckBox.setDisable(false);
        erCheckBox.setDisable(false);
    }

    private double getProgress(int currentEvaluation){
        List<AlgorithmParameter> list =  algorithmParametersTableView.getItems();
        for(int i = 0; i < list.size(); i++){
            AlgorithmParameter ap = list.get(i);
            if(ap.getName() == "maxEvaluations"){
                return ((double)currentEvaluation/ap.getValue());
            }
        }

        return 0;
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
        List<AlgorithmParameter> list =  algorithmParametersTableView.getItems();
        progressProperty.setValue(0);

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

    public void stopButtonClicked(ActionEvent actionEvent) throws InterruptedException {
        //

        //algorithm = null;

        try {
            mainLoop.interrupt();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
        isAlgorithmWorking = false;
        enableControlsOnStop();
    }

    public void nextButtonClicked(ActionEvent actionEvent) throws InterruptedException {
        if (isAlgorithmWorking){
            mainLoop.resume();
        }
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

    private void setSolutionsTableViewColumnCount(int count){
        solutionsTableView.getItems().clear();
        solutionsTableView.getColumns().clear();
        for (int i = 0; i < count; i++) {
            final int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(
                    "F" + (i+1)
            );
            column.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().get(finalIdx))
            );
            solutionsTableView.getColumns().add(column);
        }
    }

    private String createProblemUrl(String problemName){
        return "org.uma.jmetal.problem.multiobjective." + problemName;
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
            dimCount = referenceFront.getPointDimensions();

            String problemUrl = createProblemUrl(ep.getProblemName());
            Problem problem = ProblemHelper.loadProblem(problemUrl,dimCount);
            problemObjCountProperty.setValue(problem.getNumberOfObjectives());
            problemVariableCountProperty.setValue(problem.getNumberOfVariables());
            problemRefSolutionCountProperty.setValue(referenceFront.getNumberOfPoints());
//            FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
//            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);

            setSolutionsTableViewColumnCount(dimCount);
            if (dimCount == 2) {
                seriesFront2D = createFront2D(referenceFront);
                solutionSetResult.clear();
                update2dChartRelatedUI();

                setTabVisiblity(TabEnum.Chart2D, true);
                setTabVisiblity(TabEnum.Chart3D, false);
                setTabVisiblity(TabEnum.ChartParallelCoordinates, false);

                selectedTab.setValue("tab2d");
                tabPane.getSelectionModel().select(0);

                //solutionsTableView.getColumns().get(2).setVisible(false);
            }
            else if (dimCount == 3) {
                serieFront3D = createFront3D(referenceFront);

                solutionSetResult.clear();
                update3dChartRelatedUI();

                setTabVisiblity(TabEnum.Chart3D, true);
                setTabVisiblity(TabEnum.Chart2D, false);
                setTabVisiblity(TabEnum.ChartParallelCoordinates, false);

                selectedTab.setValue("tab3d");
                tabPane.getSelectionModel().select(0);

                //solutionsTableView.getColumns().get(2).setVisible(true);
            }
            else if (dimCount > 3) {
                seriesFrontPC = createFrontPC(referenceFront);

                solutionSetResult.clear();
                updatePCChartRelatedUI();

                setTabVisiblity(TabEnum.ChartParallelCoordinates, true);
                setTabVisiblity(TabEnum.Chart2D, false);
                setTabVisiblity(TabEnum.Chart3D, false);
                selectedTab.setValue("tabParallelCoordinates");
                tabPane.getSelectionModel().select(0);

                //solutionsTableView.getColumns().get(2).setVisible(true);
            }

            frontNormalizer = new FrontNormalizer(referenceFront) ;
            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront) ;

            gdIdicator = new GenerationalDistance<DoubleSolution>(normalizedReferenceFront);
            igdIdicator = new InvertedGenerationalDistance<DoubleSolution>(normalizedReferenceFront);
            igdPlusIdicator = new InvertedGenerationalDistancePlus<DoubleSolution>(normalizedReferenceFront);
            spreadIdicator = new GeneralizedSpread<DoubleSolution>(normalizedReferenceFront);
            epsilonIdicator = new Epsilon<DoubleSolution>(normalizedReferenceFront);
            hvIdicator = new PISAHypervolume<DoubleSolution>(normalizedReferenceFront);
            erIdicator = new ErrorRatio(normalizedReferenceFront);

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

    public void showSSCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isShowingSSActive = chk.isSelected();

            if (dimCount == 2)
                update2dChartRelatedUI();
            else if (dimCount == 3)
                update3dChartRelatedUI();
            else if (dimCount > 3)
                updatePCChartRelatedUI();
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
            else if (dimCount > 3)
                updatePCChartRelatedUI();
        }
    }

    public void showRefPointsCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isShowingRefPointsActive = chk.isSelected();

            if (dimCount == 2)
                update2dChartRelatedUI();
            else if (dimCount == 3)
                update3dChartRelatedUI();
            else if (dimCount > 3)
                updatePCChartRelatedUI();
        }
    }

    public void gdCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isGdActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.GD, true);
            else
                setTabVisiblity(TabEnum.GD, false);
        }
    }

    public void igdCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isIgdActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.IGD, true);
            else
                setTabVisiblity(TabEnum.IGD, false);
        }
    }

    public void igdPlusCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isIgdPlusActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.IGDPlus, true);
            else
                setTabVisiblity(TabEnum.IGDPlus, false);
        }
    }

    public void spreadCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isSpreadActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.Spread, true);
            else
                setTabVisiblity(TabEnum.Spread, false);
        }
    }

    public void epsilonCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isEpsilonActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.Epsilon, true);
            else
                setTabVisiblity(TabEnum.Epsilon, false);
        }
    }

    public void hvCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isHvActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.HV, true);
            else
                setTabVisiblity(TabEnum.HV, false);
        }
    }

    public void erCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isErActive = chk.isSelected();

            if (chk.isSelected())
                setTabVisiblity(TabEnum.ER, true);
            else
                setTabVisiblity(TabEnum.ER, false);
        }
    }

    public void stepByStepCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            isStepByStepActive = chk.isSelected();

            if (isAlgorithmWorking) {
                if (chk.isSelected())
                    nextButton.setDisable(false);
                else {
                    nextButton.setDisable(true);
                    mainLoop.resume();
                }
            }
            else
                nextButton.setDisable(true);
        }
    }

    public void selectAllQIsLinkClicked(ActionEvent event) {
        if (event.getSource() instanceof Hyperlink) {
            Hyperlink hl = (Hyperlink) event.getSource();
            hl.setVisited(false);

            gdCheckBox.setSelected(false);
            gdCheckBox.fire();
            igdCheckBox.setSelected(false);
            igdCheckBox.fire();
            igdPlusCheckBox.setSelected(false);
            igdPlusCheckBox.fire();
            spreadCheckBox.setSelected(false);
            spreadCheckBox.fire();
            epsilonCheckBox.setSelected(false);
            epsilonCheckBox.fire();
            hvCheckBox.setSelected(false);
            hvCheckBox.fire();
            erCheckBox.setSelected(false);
            erCheckBox.fire();
        }
    }

    public void selectNoneQIsLinkClicked(ActionEvent event) {
        if (event.getSource() instanceof Hyperlink) {
            Hyperlink hl = (Hyperlink) event.getSource();
            hl.setVisited(false);

            gdCheckBox.setSelected(true);
            gdCheckBox.fire();
            igdCheckBox.setSelected(true);
            igdCheckBox.fire();
            igdPlusCheckBox.setSelected(true);
            igdPlusCheckBox.fire();
            spreadCheckBox.setSelected(true);
            spreadCheckBox.fire();
            epsilonCheckBox.setSelected(true);
            epsilonCheckBox.fire();
            hvCheckBox.setSelected(true);
            hvCheckBox.fire();
            erCheckBox.setSelected(true);
            erCheckBox.fire();
        }
    }
    //endregion

    //region Methods
    private void FillComboBoxAlgorithms()
    {
        ObservableList<String> algoritmhs = FXCollections.observableArrayList();
        algoritmhs.addAll("NSGAII", "SPEA2", "SPEA3", "DB1SPEA2", "DB2SPEA2", "ASPEA2", "AngleSPEA2", "ESPEA2", "ANSGAII", "AngleNSGAII", "CDASNSGAII", "nMOEA", "nMOEA-Alpha", "EpsilonBoxMOEA");

        algorithmsComboBox.setItems(algoritmhs);
    }

    private void FillComboBoxProblems()
    {
        ObservableList<String> problems = FXCollections.observableArrayList();
        problems.addAll("Binh2", "ConstrEx",
                        "DTLZ1.2D","DTLZ1.3D", "DTLZ1.4D", "DTLZ1.6D", "DTLZ1.8D",
                        "DTLZ2.2D","DTLZ2.3D", "DTLZ2.4D", "DTLZ2.6D", "DTLZ2.8D",
                        "DTLZ3.2D","DTLZ3.3D", "DTLZ3.4D", "DTLZ3.6D", "DTLZ3.8D",
                        "DTLZ4.2D","DTLZ4.3D", "DTLZ4.4D", "DTLZ4.6D", "DTLZ4.8D",
                        "DTLZ7.2D","DTLZ7.3D", "DTLZ7.4D", "DTLZ7.6D", "DTLZ7.8D",
                        "Fonseca",
                        "GLT1", "GLT2", "GLT3", "GLT4", "GLT5", "GLT6",
                        "Golinski", "Kursawe",
                        "LZ09_F1", "LZ09_F2", "LZ09_F3", "LZ09_F4", "LZ09_F5", "LZ09_F6", "LZ09_F7", "LZ09_F8", "LZ09_F9",
                        "Osyczka2", "Schaffer", "Srinivas", "Tanaka",
                        "UF1", "UF2", "UF3", "UF4", "UF5", "UF6", "UF7", "UF8", "UF9",  "UF10",
                        "Viennet2", "Viennet3",
                        "Water",
                        "WFG1.2D",
                        //"WFG1.3D",
                        "WFG2.2D",
                        //"WFG2.3D",
                        "WFG3.2D",
                        //"WFG3.3D",
                        "WFG4.2D", 
                        //"WFG4.3D",
                        "WFG5.2D",
                        //"WFG5.3D",
                        "WFG6.2D",
                        //"WFG6.3D",
                        "WFG7.2D",
                        //"WFG7.3D",
                        "WFG8.2D",
                        //"WFG8.3D",
                        "WFG9.2D",
                        //"WFG9.3D",
                        "ZDT1", "ZDT2", "ZDT3", "ZDT4", "ZDT6");

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

    private static double[] arrayOf(double x, double y)
    {
        double[] a = new double[2];
        a[0] = x;
        a[1] = y;
        return a;
    }

    private static double[] arrayOf(double x, double y, double z)
    {
        double[] a = new double[3];
        a[0] = x;
        a[1] = y;
        a[2] = z;
        return a;
    }

    private XYZSeries createFront3D(ArrayFront front){
        XYZSeries serieFront3D_ = new XYZSeries<>("Reference Pareto-front");

        for (int i = 0; i < front.getNumberOfPoints(); i++ /*i+=5*/) {
            if (i <= front.getNumberOfPoints()) {
                serieFront3D_.add(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1), front.getPoint(i).getDimensionValue(2));
            }
        //    if (i % 5 == 0)
          //      i+=50;
        }

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

    private XYSeriesCollection createSeriesCollectionPC(List<DoubleSolution> solutionSetResult){
        XYSeriesCollection collection = new XYSeriesCollection();
        int count = 0;
        for (DoubleSolution aSolutionSetResult : solutionSetResult) {
            XYSeries series2D_ = new XYSeries("ss" + count++);
            for (int i = 0; i < aSolutionSetResult.getNumberOfObjectives(); i++) {
                series2D_.add(i+1, aSolutionSetResult.getObjective(i));
            }
            collection.addSeries(series2D_);
        }

        return collection;
    }

    private XYSeries createFront2D(ArrayFront front){
        XYSeries seriesFront2D_ = new XYSeries("Reference Pareto-front");
        List<Pair<Double, Double>> pairs = new ArrayList<>();

        int count = 1;
        if (front.getNumberOfPoints() > 1000)
            count = front.getNumberOfPoints() / 200;

        for (int i = 0; i < front.getNumberOfPoints(); i++) {
                pairs.add(new Pair<>(front.getPoint(i).getDimensionValue(0), front.getPoint(i).getDimensionValue(1)));
        }

        Collections.sort(pairs, (d2,d1) -> Double.compare(d1.getKey(),d2.getKey()));

        for (int i = 0; i < pairs.size(); i+=5) {
            if (i <= pairs.size())
                seriesFront2D_.add(pairs.get(i).getKey(), pairs.get(i).getValue());

//            if (i % count == 0)
//                i+=count;
        }

        return seriesFront2D_;
    }

    private XYSeriesCollection createFrontPC(ArrayFront front){
        XYSeriesCollection collection = new XYSeriesCollection();

        int count = 0;
        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            XYSeries series2D_ = new XYSeries("rf" + count++);
            for (int j = 0; j < front.getPoint(i).getNumberOfDimensions(); j++) {
                series2D_.add(j+1, front.getPoint(i).getDimensionValue(j));
            }
            collection.addSeries(series2D_);
        }

        return collection;
    }

    private static JFreeChart createChart2D(XYDataset dataset) {
        return ChartFactory.createScatterPlot("", "F1", "F2", dataset);
    }

    private static JFreeChart createLine2D(XYDataset dataset) {
        return ChartFactory.createXYLineChart("", "F1", "F2", dataset);
    }
    //endregion

}

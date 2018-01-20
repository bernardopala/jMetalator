package view;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import javafx.util.StringConverter;
import jmetalhelpers.algorithms.AlgorithmParameter;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;


public class PairValueCell extends TableCell<AlgorithmParameter, Object> {
    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            AlgorithmParameter ap = (AlgorithmParameter)item;

            Map<String,String> settings = AlgorithmParameter.getParameterSettings(ap.getName());
            String dataType = settings.get("dataType");

            if (dataType == "Double")
            {
                Double min = Double.valueOf(settings.get("min"));
                Double max = Double.valueOf(settings.get("max"));
                Double step = Double.valueOf(settings.get("step"));

                Spinner ctrl = new Spinner<Double>(min, max, min, step);
                ctrl.getValueFactory().setValue((Double)ap.getValue());
                ctrl.setEditable(true);
                ctrl.getEditor().setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        setDoubleSpinnerValue(ctrl, (Double)ap.getValue(), min, max);
                    }
                });

                ctrl.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (!newValue){
                        setDoubleSpinnerValue(ctrl, (Double)ap.getValue(), min, max);
                    }
                });

                setGraphic(ctrl);

            }
            else
            {
                Integer min = Integer.valueOf(settings.get("min"));
                Integer max = Integer.valueOf(settings.get("max"));
                Integer step = Integer.valueOf(settings.get("step"));

                Spinner ctrl = new Spinner<Integer>(min, max, min, step);
                ctrl.getValueFactory().setValue(((Double) ap.getValue()).intValue());
                ctrl.setEditable(true);
                ctrl.getEditor().setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        setIntegerSpinnerValue(ctrl, ((Double) ap.getValue()).intValue(), min, max);
                    }
                });

                ctrl.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (!newValue){
                        setIntegerSpinnerValue(ctrl, ((Double) ap.getValue()).intValue(), min, max);
                    }
                });

                setGraphic(ctrl);
            }
//            if (item instanceof String) {
//                setText((String) item);
//                setGraphic(null);
//            } else if (item instanceof Integer) {
//                setText(Integer.toString((Integer) item));
//                setGraphic(null);
//            } else if (item instanceof Boolean) {
//                Spinner ctrl = new Spinner();
//                CheckBox checkBox = new CheckBox();
//                checkBox.setSelected((boolean) item);
//                setGraphic(checkBox);
//            } else if (item instanceof Image) {
//                setText(null);
//                ImageView imageView = new ImageView((Image) item);
//                imageView.setFitWidth(100);
//                imageView.setPreserveRatio(true);
//                imageView.setSmooth(true);
//                setGraphic(imageView);
//            } else {
//                setText("N/A");
//                setGraphic(null);
//            }
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void setIntegerSpinnerValue(Spinner<Integer> ctrl, Integer defaultValue, Integer min, Integer max){
        String text = ctrl.getEditor().getText();

        if (NumberUtils.isNumber(text)) {
            Integer enterValue = Integer.valueOf(text);
            if (enterValue > max)
                enterValue = max;
            if (enterValue < min)
                enterValue = min;

            ctrl.getValueFactory().setValue(enterValue);
            ctrl.getEditor().setText(enterValue.toString());
        }
        else {
            ctrl.getValueFactory().setValue(defaultValue);
            ctrl.getEditor().setText(defaultValue.toString());
        }

    }

    private void setDoubleSpinnerValue(Spinner<Double> ctrl, Double defaultValue, Double min, Double max){
        String text = ctrl.getEditor().getText();

        if (NumberUtils.isNumber(text)) {
            Double enterValue = Double.valueOf(text);
            if (enterValue > max)
                enterValue = max;
            if (enterValue < min)
                enterValue = min;

            ctrl.getValueFactory().setValue(enterValue);
            ctrl.getEditor().setText(enterValue.toString());

        }
        else {
            ctrl.getValueFactory().setValue(defaultValue);
            ctrl.getEditor().setText(defaultValue.toString());

        }
    }
}
package view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.Pair;
import jmetalhelpers.algorithms.AlgorithmParameter;

public class PairValueFactory implements Callback<TableColumn.CellDataFeatures<AlgorithmParameter, Object>, ObservableValue<Object>> {
    @SuppressWarnings("unchecked")
    @Override
    public ObservableValue<Object> call(TableColumn.CellDataFeatures<AlgorithmParameter, Object> data) {
        Object value = data.getValue(); //.getValue();
        return (value instanceof ObservableValue)
                ? (ObservableValue) value
                : new ReadOnlyObjectWrapper(value);
    }
}

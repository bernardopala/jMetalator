package view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.Pair;
import jmetalhelpers.algorithms.AlgorithmParameter;

import java.util.Map;

public class PairKeyFactory implements Callback<TableColumn.CellDataFeatures<AlgorithmParameter, String>, ObservableValue<String>> {
    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<AlgorithmParameter, String> data) {
        Map<String,String> settings = AlgorithmParameter.getParameterSettings(data.getValue().getName());
        String fullName = settings.get("fullName");

        return new ReadOnlyObjectWrapper(fullName);
    }
}

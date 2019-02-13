package jmetalhelpers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SolutionDto {
    private final StringProperty V1 = new SimpleStringProperty();
    private final StringProperty V2 = new SimpleStringProperty();
    private final StringProperty V3 = new SimpleStringProperty();

    public String getV1() {
        return V1.get();
    }

    public StringProperty v1Property() {
        return V1;
    }

    public void setV1(String v1) {
        this.V1.set(v1);
    }

    public String getV2() {
        return V2.get();
    }

    public StringProperty v2Property() {
        return V2;
    }

    public void setV2(String v2) {
        this.V2.set(v2);
    }

    public String getV3() {
        return V3.get();
    }

    public StringProperty v3Property() {
        return V3;
    }

    public void setV3(String v3) {
        this.V3.set(v3);
    }
}

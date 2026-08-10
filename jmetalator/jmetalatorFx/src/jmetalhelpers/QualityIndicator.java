package jmetalhelpers;

public class QualityIndicator {
    private int Id;
    private double Value;

    public QualityIndicator(int id, double value) {
        Id = id;
        Value = value;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }
}
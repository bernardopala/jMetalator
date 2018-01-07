package jmetalhelpers.algorithms;

public class AlgorithmParameter {
    private String Name;
    private double Value;

    public AlgorithmParameter(String name, double value) {
        Name = name;
        Value = value;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }
}

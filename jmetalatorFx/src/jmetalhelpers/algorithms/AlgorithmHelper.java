package jmetalhelpers.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlgorithmHelper {
    public static List<AlgorithmParameter> createParamterList(Map<String, String> parameters){
        List<AlgorithmParameter> list = new ArrayList<>();
        parameters.forEach((key, value) -> {
            list.add(new AlgorithmParameter(key, Double.valueOf(value)));
        });

        return list;
    }
}

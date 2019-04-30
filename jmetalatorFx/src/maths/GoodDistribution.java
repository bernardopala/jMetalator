package maths;

import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHullGenerator2D;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.distance.impl.EuclideanDistanceBetweenSolutionsInObjectiveSpace;
import org.uma.jmetal.util.extremevalues.impl.SolutionListExtremeValues;

import java.util.ArrayList;
import java.util.List;

public class GoodDistribution <S extends Solution<?>>  {
    private List<S> archive = new ArrayList<>();
    private SolutionListExtremeValues exVals = new SolutionListExtremeValues();

    public double[][] get(List<S> solutionList){
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < solutionList.size(); i++){
            points.add(new Point(solutionList.get(i).getObjective(0), solutionList.get(i).getObjective(1)));
        }

        ConvexHull hull = new ConvexHull();
        List<Point> hullPoints = hull.makeHull(points);

        double[][] returnList = new double[hullPoints.size()][2];
        for (int i = 0; i < hullPoints.size(); i++){
            double[] dd= new double[2];
            dd[0] = hullPoints.get(i).x;
            dd[1] = hullPoints.get(i).y;
            returnList[i] = dd;
        }

        return returnList;

//        if (archive.size() == 0) {
//            archive = solutionList;
//            return archive;
//        }
//
//
//

    }
}

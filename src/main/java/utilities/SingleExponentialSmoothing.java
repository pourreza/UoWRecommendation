package utilities;

public class SingleExponentialSmoothing {

    double smoothingFactor;

    SingleExponentialSmoothing(double smoothingFactor){
        this.smoothingFactor = smoothingFactor;
    }

    public double predictValue(double[] series){
        double predictedValue = series[0];
        for(int index=1; index<series.length; index++) {
            predictedValue = smoothingFactor * series[index] + (1 - smoothingFactor) * predictedValue;
        }
        return predictedValue;
    }
}

package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import static utilities.Printer.print;
import static java.lang.Math.sqrt;

public class TensorPrediction {

    private static int firstDimensionSize;
    private static int secondDimensionSize;
    private static int thirdDimensionSize;

    private static double[][] firstDecomposedMatrix;
    private static double[][] secondDecomposedMatrix;
    private static double[][] thirdDecomposedMatrix;
    private static double[] lambda;

    public TensorPrediction(int firstDimensionSize, int secondDimensionSize, int thirdDimensionSize){
        this.thirdDimensionSize = thirdDimensionSize;
        this.firstDimensionSize = firstDimensionSize;
        this.secondDimensionSize = secondDimensionSize;
    }

    public void decomposeTensor(int[][][] tensor, int tensorIndex) {

        print("Staring to write in file.");

        String fileName = "MashupTensor"+tensorIndex+".txt";
        print(fileName);
        try {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(new File(fileName)));
            printWriter.println(thirdDimensionSize);
            printWriter.println(firstDimensionSize);
            printWriter.println(secondDimensionSize);
            for(int thirdIndex = 0; thirdIndex< thirdDimensionSize; thirdIndex++ ){
                for(int firstIndex = 0; firstIndex< firstDimensionSize; firstIndex++){
                    String rowValues = "";
                    for(int secondIndex = 0; secondIndex< secondDimensionSize; secondIndex++){
                        rowValues = rowValues + tensor[firstIndex][secondIndex][thirdIndex] +" ";
                    }
                    printWriter.println(rowValues);
                }
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static double[][] createSimilarityMatrix(int testIndex) {
        double[][][] similarityMatrices = createSimilarityMatrices(testIndex);
        double[] frobeniusNorms = computeFrobeniusNorms(similarityMatrices);

        double[][] similarityMatrix = new double[firstDimensionSize][secondDimensionSize];
        for(int firstIndex=0; firstIndex<firstDimensionSize; firstIndex++){
            for(int secondIndex=0; secondIndex<secondDimensionSize; secondIndex++){
                for(int componentIndex=0; componentIndex<10; componentIndex++){
                    similarityMatrix[firstIndex][secondIndex]+= similarityMatrices[componentIndex][firstIndex][secondIndex]/frobeniusNorms[componentIndex];
                }
            }
        }

        return similarityMatrix;
    }

    private static double[][][] createSimilarityMatrices(int testIndex) {
        double[][][] similarityMatrices = new double[10][firstDimensionSize][secondDimensionSize];
        for(int numberOfComponents=10; numberOfComponents<101; numberOfComponents+=10){
            similarityMatrices[(numberOfComponents/10)-1] = createSimilarityMatrixForSpecificComponentSize(testIndex, numberOfComponents);
        }
        return similarityMatrices;
    }

    private static double[][] createSimilarityMatrixForSpecificComponentSize(int testIndex, int numberOfComponents) {
        loadDecomposedTensors(testIndex, numberOfComponents);
        double[][] similarityMatrix = new double[firstDimensionSize][secondDimensionSize];

        SingleExponentialSmoothing predictTime = new SingleExponentialSmoothing(0.8);

        for(int componentIndex=0; componentIndex<numberOfComponents; componentIndex++){
            double[] timeInverse = inverseVector(thirdDecomposedMatrix, componentIndex);
            double predictedTimeValue = predictTime.predictValue(timeInverse);
            for (int nodeIndex = 0; nodeIndex< firstDimensionSize; nodeIndex++){
                for (int edgeIndex = 0; edgeIndex< secondDimensionSize; edgeIndex++){
                    similarityMatrix[nodeIndex][edgeIndex]+=lambda[componentIndex]* firstDecomposedMatrix[nodeIndex][componentIndex]* secondDecomposedMatrix[edgeIndex][componentIndex]*predictedTimeValue;
                }
            }
        }
        return similarityMatrix;
    }

    private static double[] computeFrobeniusNorms(double[][][] similarityMatrices) {
        double[] frobeniusNorms = new double[10];
        for(int componentIndex=0; componentIndex<10; componentIndex++){
            double frobeniusNorm = 0;
            for(int firstIndex=0; firstIndex<firstDimensionSize; firstIndex++){
                for (int secondIndex=0; secondIndex<secondDimensionSize; secondIndex++){
                    frobeniusNorm+=similarityMatrices[componentIndex][firstIndex][secondIndex]*similarityMatrices[componentIndex][firstIndex][secondIndex];
                }
            }
            frobeniusNorm = sqrt(frobeniusNorm);
            frobeniusNorms[componentIndex] = frobeniusNorm;
        }
        return frobeniusNorms;
    }

    private static double[] inverseVector(double[][] timeVector, int componentIndex) {
        double[] vectorInverse = new double[thirdDimensionSize];
        for(int i = 0; i< thirdDimensionSize; i++){
            vectorInverse[i] = timeVector[i][componentIndex];
        }
        return vectorInverse;
    }

    private static void loadDecomposedTensors(int testIndex, int numberOfComponents) {
        thirdDecomposedMatrix = new double[thirdDimensionSize][numberOfComponents];
        secondDecomposedMatrix = new double[secondDimensionSize][numberOfComponents];
        firstDecomposedMatrix = new double[firstDimensionSize][numberOfComponents];
        lambda = new double[numberOfComponents];

        String[] fileNames = new String[4];
        fileNames[0] = "decompositions/Mashuplambda" + testIndex + numberOfComponents + ".txt";
        fileNames[1] = "decompositions/Mashupnodes" + testIndex + numberOfComponents + ".txt";
        fileNames[2] = "decompositions/Mashupedges" + testIndex + numberOfComponents + ".txt";
        fileNames[3] = "decompositions/Mashuptimes" + testIndex + numberOfComponents + ".txt";

        for (int fileType = 0; fileType < 4; fileType++) {
            File inFile = new File(fileNames[fileType]);
            Scanner in = null;
            try {
                in = new Scanner(inFile);
                in.useDelimiter("\n");

                String line = "";
                int lineCount = 0;

                while (in.hasNextLine()) {
                    line = in.nextLine().trim();
                    if (fileType == 0) {
                        lambda[lineCount] = Double.parseDouble(line);
                    } else {
                        Scanner lineIn = new Scanner(line);
                        lineIn.useDelimiter(",");

                        for (int i = 0; lineIn.hasNext(); i++) {
                            switch (fileType) {
                                case 1:
                                    firstDecomposedMatrix[lineCount][i] = Double.parseDouble(lineIn.next());
                                    break;
                                case 2:
                                    secondDecomposedMatrix[lineCount][i] = Double.parseDouble(lineIn.next());
                                    break;
                                case 3:
                                    thirdDecomposedMatrix[lineCount][i] = Double.parseDouble(lineIn.next());
                            }
                            lineIn.next();
                        }
                    }
                    lineCount++;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

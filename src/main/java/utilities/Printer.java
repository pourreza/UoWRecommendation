package utilities;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Printer {

    public static void print(String s) {
        System.out.println(s);
    }

    public static void printTensor(int[][][] tensor) {
        for (int i=0; i<tensor[0][0].length; i++)
        {
            for (int j = 0; j< tensor.length; j++){
                for (int k = 0; k< tensor[0].length; k++){
                    if(tensor[j][k][i]!=0)
                        System.out.print(tensor[j][k][i]+" ");
                }
                System.out.println();
            }
            System.out.println("@@@@@@@@@@@@@@@@");
        }
    }

    public static void printMatrix(double[][] matrix) {
        for(int i=0; i<matrix.length; i++){
            for(double d: matrix[i]){
                System.out.print(d+" ");
            }
            System.out.println();
        }
    }

    public static void printVector(double[] vector) {
        for(int i=0; i<vector.length; i++){
            System.out.print(vector[i]+" ");
        }
    }

    public static void saveToExcel(double[] precisions, double[] recalls, double[] fscores, ArrayList<Set<String>>[] oldTestHyperEdges, ArrayList<Set<String>>[] predictedTestHyperEdges, String fileName) {
        print("Started writing in excel file");

        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
        Sheet sheet = workbook.createSheet("Results");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Precision");
        headerRow.createCell(1).setCellValue("Recall");
        headerRow.createCell(2).setCellValue("F-Score");

        for(int index=0; index<precisions.length; index++){
            Row results = sheet.createRow(index+1);
            results.createCell(0).setCellValue(precisions[index]);
            results.createCell(1).setCellValue(recalls[index]);
            results.createCell(2).setCellValue(fscores[index]);
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Workbook workbook2 = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
//        Sheet sheet2 = workbook2.createSheet("Results");
//
//
//        int maxValue = oldTestHyperEdges[0].size();
//        for(int i=1 ; i<oldTestHyperEdges.length-1; i++){
//            if(maxValue<oldTestHyperEdges[i].size())
//                maxValue = oldTestHyperEdges[i].size();
//        }
//
//        Row headerRow2 = sheet2.createRow(0);
//        headerRow2.createCell(0).setCellValue("Old Hyperedges 0");
//        headerRow2.createCell(1).setCellValue("Predicted Old Hyperedges 0");
//        headerRow2.createCell(2).setCellValue("Old Hyperedges 1");
//        headerRow2.createCell(3).setCellValue("Predicted Old Hyperedges 1");
//        headerRow2.createCell(4).setCellValue("Old Hyperedges 2");
//        headerRow2.createCell(5).setCellValue("Predicted Old Hyperedges 2");
//        headerRow2.createCell(6).setCellValue("Old Hyperedges 3");
//        headerRow2.createCell(7).setCellValue("Predicted Old Hyperedges 3");
//        headerRow2.createCell(8).setCellValue("Old Hyperedges 4");
//        headerRow2.createCell(9).setCellValue("Predicted Old Hyperedges 4");
//        headerRow2.createCell(10).setCellValue("Old Hyperedges 5");
//        headerRow2.createCell(11).setCellValue("Predicted Old Hyperedges 5");
//        headerRow2.createCell(12).setCellValue("Old Hyperedges 6");
//        headerRow2.createCell(13).setCellValue("Predicted Old Hyperedges 6");

//        for(int i=0; i<maxValue; i++){
//            Row res = sheet2.createRow(i+1);
//            for(int j=0; j<oldTestHyperEdges.length-1; j++){
//                if(oldTestHyperEdges[j].size()>i){
//                    res.createCell(j*2).setCellValue(oldTestHyperEdges[j].get(i).toString());
//                }
//                else{
//                    res.createCell(j*2).setCellValue(" ");
//                }
//                if(predictedTestHyperEdges[j].size()>i){
//                    res.createCell(j*2+1).setCellValue(predictedTestHyperEdges[j].get(i).toString());
//                }
//                else{
//                    res.createCell(j*2+1).setCellValue(" ");
//                }
//            }
//        }
//
//        try {
//            FileOutputStream outputStream = new FileOutputStream("TestHyperedgesWithFewerTestsWithoutDuplications.xlsx");
//            workbook2.write(outputStream);
//            workbook2.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void printInFile(Set<String> conferencesJournals, String fileName) {
        ArrayList<String> cj = new ArrayList<String>(conferencesJournals);
        Collections.sort(cj);
        try {
            PrintWriter writer = new PrintWriter(fileName);
            for(String conference: cj){
                writer.println(conference);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printListInFile(ArrayList<String> cj, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName);
            for(String conference: cj){
                writer.println(conference);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printJSONListInFile(ArrayList<JSONObject> cj, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName);
            for(JSONObject jsonObject: cj){
                writer.println(jsonObject);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

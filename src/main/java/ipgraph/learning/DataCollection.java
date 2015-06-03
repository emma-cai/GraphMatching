package ipgraph.learning;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qingqingcai on 6/2/15.
 */
public class DataCollection {

    public static void main (String[] args) {

        String inputpath = "/Users/qingqingcai/Downloads/jacana-qa-naacl2013-data-results/train-less-than-40.manual-edit.xml";
        String outputpath = "data/MIT99.xls";
        String sheetname = "MIT99-trek8";
        List<Data> dataList = readXML(inputpath);
        writeToExcel(outputpath, sheetname, dataList);
    }

    public static void readExcel(String filepath, List<Data> data) {

    }

    public static void writeToJSON(String filepath, List<Data> dataList) {

    }

    public static void writeToExcel(String filepath, String sheetname, List<Data> dataList) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetname);
        int rownum = 0;
        for (Data data : dataList) {
            Row row = sheet.createRow(rownum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(data.id);

            cell = row.createCell(1);
            cell.setCellValue("1");

            cell = row.createCell(2);
            cell.setCellValue(data.question);

            cell = row.createCell(3);
            cell.setCellValue(data.positive);

            cell = row.createCell(4);
            cell.setCellValue(data.answer);

            row = sheet.createRow(rownum++);
            cell = row.createCell(0);
            cell.setCellValue(data.id);

            cell = row.createCell(1);
            cell.setCellValue("0");

            cell = row.createCell(2);
            cell.setCellValue(data.question);

            cell = row.createCell(3);
            cell.setCellValue(data.negative);
        }

        try {
            FileOutputStream out = new FileOutputStream(new File(filepath));
            workbook.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Data> readXML(String filepath) {

        List<Data> dataList = new ArrayList<>();

        String pairStart = "<QApairs id=";
        String pairEnd = "</QApairs>";
        String questionTag = "<question>";
        String positiveStart = "<positive>";
        String positiveEnd = "</positive>";
        String negativeStart = "<negative>";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line = null;
            String id = "";
            String question = "";
            String positive = "";
            String answer = "";
            String negative = "";
            while ((line = br.readLine()) != null) {

                if (line.startsWith(pairStart)) {
                    boolean positivestarted = true;
                    boolean negativestarted = true;
                    if (!id.isEmpty()) {
                        Data data = new Data(id, question, positive, answer, negative);
                        dataList.add(data);
                    }

                    id = line.substring(pairStart.length()+1, line.length()-2);
                    while (!(line = br.readLine()).equals(pairEnd)) {
                        if (line.startsWith(questionTag)) {
                            question = br.readLine().trim().replaceAll("\t", " ");
                        }
                        if (line.startsWith(positiveStart) && positivestarted == true) {
                            positivestarted = false;
                            positive = br.readLine().trim().replaceAll("\t", " ");
                            String prev = line;
                            while (!(line = br.readLine()).equals(positiveEnd)) {
                                prev = line;
                            }
                            answer = prev.trim().replaceAll("\t", " ");
                        }
                        if (line.startsWith(negativeStart) && negativestarted == true) {
                            negativestarted = false;
                            negative = br.readLine().trim().replaceAll("\t", " ");
                        }
                    }
                }
            }

            Data data = new Data(id, question, positive, answer, negative);
            dataList.add(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}

class Data {
    String id;
    String question;
    String positive;
    String negative;
    String answer;

    public Data(String id, String question, String positive, String answer, String negative) {

        this.id = id;
        this.question = question;
        this.positive = positive;
        this.answer = answer;
        this.negative = negative;
    }
}

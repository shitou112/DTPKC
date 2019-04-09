package algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Qian Shaofeng
 * created on 2019/1/10.
 */
public class ReadWriteCSV {

    public static String getPath(){
        String path = Test.class.getClass().getResource("/").getPath();
//        System.out.println(path);
        return path;
    }

    public static BigInteger[] readWeights(String file){
        BigInteger[] weights = null;
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }

                String[] columns = line.split(",");

                // skip first column and last column is the label
                int i = 0;
                weights = new BigInteger[columns.length];
                for (i=0; i<columns.length; i++) {
                    weights[i] = new BigInteger(columns[i]);
                }

            }
        } catch (Exception e){

        } finally{
            if (scanner != null)
                scanner.close();
        }
        return weights;
    }



    public static List<Logistic.Instance> readDataSet(String file) throws FileNotFoundException {
        List<Logistic.Instance> dataset = new ArrayList<Logistic.Instance>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
            scanner.nextLine();
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }
                //System.out.println(line);
                String[] columns = line.split(",");

                // skip first column and last column is the label
                int i = 0;
                String[] data = new String[columns.length-1];
                for (i=0; i<columns.length-1; i++) {
                    data[i] = columns[i];
                }
                String label = columns[i];
                Logistic.Instance instance = new Logistic.Instance(label, data);
                dataset.add(instance);
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
        return dataset;
    }

    public static List<DTPKCLogistic.Instance> readDataSet(String file, DTPKC dtpkc) throws FileNotFoundException {
        System.out.println("----------------------");
        System.out.println("start read data...");
        List<DTPKCLogistic.Instance> dataset = new ArrayList<DTPKCLogistic.Instance>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
            scanner.nextLine();
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] columns = line.split(",");

                // skip first column and last column is the label
                int i = 0;
                String[] data = new String[columns.length-1];
                for (i=0; i<columns.length-1; i++) {
                    data[i] = columns[i];
                }
                String label = columns[i];
                DTPKCLogistic.Instance instance = new DTPKCLogistic.Instance(label, data, dtpkc);
                dataset.add(instance);
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
        System.out.println("finish read data...");
        return dataset;
    }
}

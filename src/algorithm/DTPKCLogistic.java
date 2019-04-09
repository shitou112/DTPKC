package algorithm;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Qian Shaofeng
 * created on 2019/1/9.
 */
public class DTPKCLogistic {
    /** the learning rate */
    private EncryptData rate;

    /** the weight to learn */
    private EncryptData[] weights;

    private EncryptData z;

    private BigInteger mz;

    private BigInteger SCALE_NUMBER;

    private EncryptData C_SCALE_NUMBER_1;

    private EncryptData C_SCALE_NUMBER_2;

    private EncryptData THRESHOLD;

    private DTPKC dtpkc;

    /** the number of iterations */
    private int ITERATIONS = 1;

    private int feature_num;

    public DTPKCLogistic(int feature_num, DTPKC dtpkc) {
        this.dtpkc = dtpkc;
        this.feature_num = feature_num;

        //init scale number
        SCALE_NUMBER = new BigInteger("1000000");
        C_SCALE_NUMBER_1 = dtpkc.encrypt(SCALE_NUMBER);
        C_SCALE_NUMBER_2 = dtpkc.encrypt(SCALE_NUMBER.pow(2));


        //init rate
        BigInteger mRate = new BigInteger("1000");
        rate = dtpkc.encrypt(mRate);

        //init threshold
        THRESHOLD = dtpkc.encrypt(new BigInteger("5").multiply(SCALE_NUMBER).divide(new BigInteger("10")));

        initWeights();
    }


    public void initWeights(){
        long start = System.currentTimeMillis( );
        System.out.println("----------------------");
        System.out.println("start init...");
        //init weights parameters
        this.weights = new EncryptData[feature_num];
        BigInteger[] mWeights = ReadWriteCSV.readWeights(ReadWriteCSV.getPath()+"data/weights.csv");
        for (int i=0; i < feature_num; ++i){
//            weights[i] = dtpkc.encrypt(new BigInteger(17, new Random()).subtract(new BigInteger("65536")));
            weights[i] = dtpkc.encrypt(mWeights[i]);
        }

        printWeights("init weights: ");
        long end = System.currentTimeMillis( );
        System.out.println("finish init..."+"use time:"+(end-start));
    }

    public void train(List<Instance> instances){
        long start = System.currentTimeMillis( );
        System.out.println("----------------------");
        System.out.println("start train...");
        int dataSize = instances.size();
        for (int n=0; n<ITERATIONS; ++n) {

            for (int i=0; i < dataSize; ++i) {
                EncryptData[] x = instances.get(i).x;
                EncryptData predicted = classify(x);

                EncryptData label = instances.get(i).label;
                label = dtpkc.multiply(label, C_SCALE_NUMBER_1);

                EncryptData tmp1= dtpkc.subtract(predicted, label);
                tmp1 = dtpkc.multiply(tmp1, z);
                EncryptData tmp2 = dtpkc.subtract(C_SCALE_NUMBER_1, z);
                tmp1 = dtpkc.multiply(tmp1, tmp2);
                tmp1 = dtpkc.divide(tmp1, C_SCALE_NUMBER_2);
                EncryptData delta = tmp1;

                for (int j=0; j<feature_num; j++) {
                    tmp1 = dtpkc.multiply(rate, delta);
                    tmp1 = dtpkc.multiply(tmp1, x[j]);
                    tmp1 = dtpkc.divide(tmp1, C_SCALE_NUMBER_1);

                    EncryptData thet = tmp1;
                    weights[j] = dtpkc.subtract(weights[j], thet);

                }

                if (i % 10==0 && i > 0){
                    long end = System.currentTimeMillis( );
                    System.out.println("current datasize: "+i+" ,use time:"+(end-start));
                }
//                System.out.print("iteration: " + n + " ,current data index: " + i+" ,data size: "+dataSize+" ,");
//                printWeights("update weights: ");
            }
            long end = System.currentTimeMillis( );
            System.out.println("finish one iteration..."+"use time:"+(end-start));
        }
        long end = System.currentTimeMillis( );
        System.out.println("finish train..."+"use time:"+(end-start));
    }

    private EncryptData classify(EncryptData[] x) {
        BigInteger mv = new BigInteger("0");
        EncryptData v = dtpkc.encrypt(mv);
        for (int i=0; i<feature_num;i++)  {
            EncryptData tmp = dtpkc.multiply(weights[i], x[i]);
            v = dtpkc.add(v, tmp);
        }
        return sigmoid(v);
    }

    public List<BigInteger> classify(List<Instance> instances) {
        long start = System.currentTimeMillis( );
        System.out.println("----------------------");
        System.out.println("start classify...");
        List<BigInteger> res = new ArrayList<>();
        for (int i=0; i < instances.size(); ++i){
            EncryptData[] x = instances.get(i).x;
            EncryptData label = instances.get(i).label;
            EncryptData predict = classify(x);
            int comValue = dtpkc.slt(predict, THRESHOLD);
            String resStr = comValue>=0?"1":"0";
            res.add(new BigInteger(resStr));
//            System.out.println("label: "+label+" predict: "+predict);
        }
        long end = System.currentTimeMillis( );
        System.out.println("finish classify..."+"use time:"+(end-start));
        return res;
    }

    public double computeAuc(List<BigInteger> predicts, List<Instance> instances){
        int count = 0;
        int size = predicts.size();
        for (int i=0; i < size; ++i){
            BigInteger predict = predicts.get(i);
            BigInteger label = instances.get(i).mlabel;
            if (predict.compareTo(label)==0)
                ++count;
        }

        return count*1.0 / size;
    }

    private EncryptData sigmoid(EncryptData v) {
        dtpkc.pdo(v);
        BigInteger mv = dtpkc.pdtShowPosOrNeg(v);
        BigInteger tmp3 = mv.pow(3).multiply(new BigInteger("2")).multiply(SCALE_NUMBER).divide(new BigInteger("100"));
        BigInteger tmp1 = mv.multiply(new BigInteger("25")).multiply(SCALE_NUMBER.pow(3)).divide(new BigInteger("100"));
        BigInteger tmp0 = new BigInteger("5").multiply(SCALE_NUMBER.pow(4)).divide(new BigInteger("10"));
        BigInteger res = tmp0.add(tmp1).subtract(tmp3);

        mz = res.divide(SCALE_NUMBER.pow(3));
        z = dtpkc.encrypt(mz);
        return z;
    }

    private void printWeights(String msg){
        System.out.print(msg);
        for (int k=0; k < feature_num; ++k) {
            dtpkc.pdo(weights[k]);
            BigInteger w = dtpkc.pdtShowPosOrNeg(weights[k]);

            System.out.print(w+", ");
        }
        System.out.println();

    }

    public static class Instance {
        public BigInteger mlabel;
        public BigInteger[] mx;

        public EncryptData label;
        public EncryptData[] x;

        public Instance(String label, String[] x, DTPKC dtpkc) {
            mlabel = new BigInteger(label);
            this.label = dtpkc.encrypt(mlabel);

            mx = new BigInteger[x.length];
            this.x = new EncryptData[x.length];
            for (int i=0; i < x.length; ++i) {
                mx[i] = new BigInteger(x[i]);
                this.x[i] = dtpkc.encrypt(mx[i]);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i=0; i < x.length; ++i){
                sb.append(x[i].toString()+", ");
            }
            sb.append(" ");
            sb.append(label.toString()+"]");
            return sb.toString();
        }
    }


    public static void main(String... args) throws FileNotFoundException {
        DTPKC dtpkc = new DTPKC();
        dtpkc.kenGen();
        List<Instance> train_data = ReadWriteCSV.readDataSet(ReadWriteCSV.getPath()+"data/train_data.csv", dtpkc);
        List<Instance> test_data = ReadWriteCSV.readDataSet(ReadWriteCSV.getPath()+"data/test_data.csv", dtpkc);

        DTPKCLogistic logistic = new DTPKCLogistic(25, dtpkc);
        logistic.train(train_data);

        List<BigInteger> predicts = logistic.classify(test_data);
        double auc = logistic.computeAuc(predicts, test_data);
        System.out.println("size: "+predicts.size());
        System.out.println("auc: "+auc);
    }
}

package algorithm;

import java.math.BigInteger;

/**
 * @author Qian Shaofeng
 * created on 2019/1/9.
 */
public class EncryptData {
    public BigInteger t1;
    public BigInteger t2;
    public BigInteger ct1;

    public EncryptData(){

    }
    public EncryptData(BigInteger t1, BigInteger t2){
        this.t1 = t1;
        this.t2 = t2;
    }
}

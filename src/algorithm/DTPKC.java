package algorithm;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Qian Shaofeng
 * created on 2018/12/11.
 */
public class DTPKC {

    public long seed = 0;

    /**
     * p and q are two large primes.
     */
    private BigInteger p,  q,  lambda, lambdaInverse, lambda1, lambda2;
    private BigInteger p1, q1;
    /**
     * n = p*q, n2 = n*n
     */
    public BigInteger n, n2, nSubOne;
    /**
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1.
     */
    public BigInteger g, a;
    public BigInteger tha;
    public BigInteger h;
    /**
     * number of bits of modulus
     */
    public int bitLength;

    BigInteger SK;
    BigInteger SK1;
    BigInteger SK2;

    BigInteger[] pk=null;
    public BigInteger sk;


    private static BigInteger lcm(BigInteger p, BigInteger q) {
        BigInteger gcd, mul;
        mul = p.multiply(q);
        gcd = p.gcd(q);
        return mul.divide(gcd);
    }

    private BigInteger funL(BigInteger x){
        return x.subtract(BigInteger.ONE).divide(n);
    }

    private boolean isPrime(BigInteger x, int certainty){
        return x.isProbablePrime(certainty);
    }
    public void kenGen(){
        int certainty = 10;
        bitLength=1024;

        //generate n, p, q
        boolean flag = false;
        do{
            flag = true;
            p = BigInteger.probablePrime(bitLength, new Random());
            q = BigInteger.probablePrime(bitLength, new Random());
//            p = new BigInteger("9862284938155742366871436857320438905671075791583179515326641270549325340833639489483718330036832659958951727481875323823101307170532092338837153187628443");
//            q = new BigInteger("7986901984020290211411487668763291338428895773814064308090191603940550262206907214650071489911031996382654873978355925810410599385919586702672115698022983");

//            p = new BigInteger("2622646787");
//            q = new BigInteger("3645154559");

            p1 = p.subtract(BigInteger.ONE).divide(new BigInteger("2"));
            q1 = q.subtract(BigInteger.ONE).divide(new BigInteger("2"));

            n = p.multiply(q);

            nSubOne = n.subtract(BigInteger.ONE);

            lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));

            if (!isPrime(p1, certainty) || !isPrime(q1, certainty) || !isPrime(p, certainty) || !isPrime(q, certainty)) {
                flag = false;
            }
        } while ((!lambda.gcd(n).equals(BigInteger.ONE)) || !flag);

        //validate p, q, p1, q1 is primes
        if (!isPrime(p, certainty)){
            System.out.println("p is not prime");
        }
        if (!isPrime(q, certainty)){
            System.out.println("q is not prime");
        }
        if (!isPrime(p1, certainty)){
            System.out.println("p1 is not prime");
        }
        if (!isPrime(q1, certainty)){
            System.out.println("q1 is not prime");
        }

        //compute N^2, p1, q1
        n2 = n.multiply(n);
        lambdaInverse = lambda.modInverse(n);

        //generate g
        a = new BigInteger(bitLength, new Random(seed));
//        a = new BigInteger("43740291838977966358412011346027645009024807429724953221257839631540716696605");
        g = a.modPow(n, n2);


        int thaSize = bitLength;
        tha  = new BigInteger(thaSize, new SecureRandom());
//        tha = new BigInteger("61859245414837337432551845531881824149955236468526305350679797549893413478745");
        h = g.modPow(tha, n2);

        pk = new BigInteger[]{n, g, h};
        sk = tha;


        BigInteger s = lambda.multiply(lambda.modInverse(n.multiply(n))).mod(lambda.multiply(n).multiply(n));
        lambda1 = new BigInteger(lambda.bitLength(), new SecureRandom());
//        lambda1 = new BigInteger("7213717513743931863");
        lambda2 = s.subtract(lambda1);
        SK = lambda;
        SK1 = lambda1;
        SK2 = lambda2;

        System.out.println("=======");
        System.out.println("p: "+p);
        System.out.println("q: "+q);
        System.out.println("n: "+n);
        System.out.println("n2: "+n2);
        System.out.println("a: "+a);
        System.out.println("g: "+g);
        System.out.println("h: "+h);
        System.out.println("tha: "+tha);
        System.out.println("sk: "+sk);
        System.out.println("lambda: "+lambda);
        System.out.println("lambda1: "+ lambda1);
        System.out.println("lambda2: "+lambda2);
    }

    public EncryptData add(EncryptData x, EncryptData y){
        BigInteger ra = new BigInteger(bitLength, new Random());
        BigInteger rb = new BigInteger(bitLength, new Random());

        EncryptData cra = encrypt(ra);
        EncryptData crb = encrypt(rb);

        x.t1 = x.t1.multiply(cra.t1);
        pdo(x);
        BigInteger mx = pdtShowPosOrNeg(x);

        y.t1 = y.t1.multiply(crb.t1);
        pdo(y);
        BigInteger my = pdtShowPosOrNeg(y);

        BigInteger res = mx.add(my);

        EncryptData cr = encrypt(ra.add(rb));
        EncryptData cres = encrypt(res);
        cres.t1 = cres.t1.multiply(cr.t1.modPow(nSubOne, n2));
        return cres;
    }

    public EncryptData subtract(EncryptData x, EncryptData y){
        pdo(x);
        BigInteger mx = pdt(x);

        pdo(y);
        BigInteger my = pdt(y);
        BigInteger res = mx.subtract(my);
        return encrypt(res);
    }

    public EncryptData multiply(EncryptData x, EncryptData y){
        BigInteger rx = new BigInteger(bitLength, new Random());
        BigInteger ry = new BigInteger(bitLength, new Random());
        BigInteger Rx = new BigInteger(bitLength, new Random());
        BigInteger Ry = new BigInteger(bitLength, new Random());

        EncryptData crx = encrypt(rx);
        EncryptData cry = encrypt(ry);
        EncryptData cRx = encrypt(Rx);
        EncryptData cRy = encrypt(Ry);


        BigInteger s = cRx.t1.multiply(x.t1.modPow(n.subtract(ry), n2));
        BigInteger t = cRy.t1.multiply(y.t1.modPow(n.subtract(rx), n2));
        EncryptData X = new EncryptData();
        X.t1 = x.t1.multiply(crx.t1);
        EncryptData Y = new EncryptData();
        Y.t1 = y.t1.multiply(cry.t1);
        pdo(X);
        BigInteger mx = pdt(X);

        pdo(Y);
        BigInteger my = pdt(Y);

        BigInteger h = mx.multiply(my);
        EncryptData ch = encrypt(h);

        BigInteger s4 = encrypt(rx.multiply(ry)).t1.modPow(nSubOne, n2);
        BigInteger s5 = cRx.t1.modPow(nSubOne, n2);
        BigInteger s6 = cRy.t1.modPow(nSubOne, n2);
        BigInteger res = ch.t1.multiply(t).multiply(s).multiply(s4).multiply(s5).multiply(s6);

        X.t1 = res;
        return X;
    }

//    public EncryptData multiply(EncryptData x, EncryptData y){
//        pdo(x);
//        BigInteger mx = pdtShowPosOrNeg(x);
//
//        pdo(y);
//        BigInteger my = pdtShowPosOrNeg(y);
//        BigInteger res = mx.multiply(my);
//        return encrypt(res);
//    }

    public EncryptData divide(EncryptData x, EncryptData y){
        pdo(x);
        BigInteger mx = pdtShowPosOrNeg(x);

        pdo(y);
        BigInteger my = pdtShowPosOrNeg(y);
        BigInteger res = mx.divide(my);
        return encrypt(res);
    }

    public int slt(EncryptData x, EncryptData y){
        pdo(x);
        BigInteger mx = pdtShowPosOrNeg(x);

        pdo(y);
        BigInteger my = pdtShowPosOrNeg(y);
        return mx.compareTo(my);
    }

    // 加密
    public EncryptData encrypt(BigInteger m) {
        //bug: when r equal 0, weak dec is right, otherwise the value is wrong.
        BigInteger r = new BigInteger(bitLength, new Random());
//        System.out.println("r: "+r);

        BigInteger t1 = h.modPow(r, n2).multiply(n.multiply(m).add(BigInteger.ONE)).mod(n2);
        BigInteger t2 = g.modPow(r, n2);
        EncryptData t = new EncryptData(t1, t2);

        return t;
    }

    // 弱解密
    public BigInteger weakDecrypt(EncryptData t) {
        BigInteger t1 = t.t1;
        BigInteger t2 = t.t2;
        BigInteger m = funL(t1.multiply(t2.modPow(sk, n2).modInverse(n2)).mod(n2));
        return m;
    }

    // 强解密
    public BigInteger strongDecrypt(EncryptData t) {
        BigInteger t1 = t.t1;
        BigInteger m = funL(t1.modPow(lambda, n2)).multiply(lambdaInverse).mod(n);
        return m;
    }

    public EncryptData pdo(EncryptData t){
        BigInteger t1 = t.t1;
        BigInteger ct1 = t1.modPow(lambda1, n2);
        t.ct1 = ct1;
        return t;
    }

    public BigInteger pdt(EncryptData t){
        BigInteger t1 = t.t1;
        BigInteger ct1 = t.ct1;
        BigInteger ct2 = t1.modPow(lambda2, n2);
        BigInteger m = funL(ct1.multiply(ct2).mod(n2));
//        if (m.bitLength() > (bitLength/2))
//            m = m.subtract(n);
        return m;
    }

    public BigInteger pdtShowPosOrNeg(EncryptData t){
        BigInteger t1 = t.t1;
        BigInteger ct1 = t.ct1;
        BigInteger ct2 = t1.modPow(lambda2, n2);
        BigInteger m = funL(ct1.multiply(ct2).mod(n2));
        if (m.bitLength() > (bitLength/2))
            m = m.subtract(n);
        return m;
    }

//    public BigInteger pwdec1(BigInteger[] t){
//        BigInteger t1 = t[0];
//        BigInteger t2 = t[1];
//
//        BigInteger wt = t2.modPow(sk, n2);
//        return wt;
//    }
//
//    public BigInteger pwdec2(BigInteger[] t, BigInteger all_wt, BigInteger tha){
//        BigInteger t1 = t[0];
//        BigInteger t2 = t[1];
//
//        BigInteger wt_p = t2.modPow(sk, n2);
//        BigInteger wt = wt_p.multiply(wt_p);
//        BigInteger m = funL(t1.multiply(wt.modInverse(n2)).mod(n2));
//        return m;
//    }

}
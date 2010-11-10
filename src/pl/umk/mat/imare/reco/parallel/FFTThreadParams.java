/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.reco.parallel;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Maciek
 */
public class FFTThreadParams {
    public int beg,end,beglev,aN,jlev,numchilds,numThreads;
    public int ownid,delta;
    public double FFTresultRe[]=null;
    public double FFTresultIm[]=null;
    public int FFTinvX[]=null;
    public int n2;
    public Thread[] all=null;
    public double[] FFTexRe;
    public double[] FFTexIm;
    public Semaphore[] sem;
    public Semaphore last,first;
    public boolean working=true;
    public Barrier[] bar=null;
    public boolean doBackup=false;
    public double[] backupRe,backupIm;
    public int partial;
    public boolean compPartial=false;
    public double[] parteRe,parteIm;
    public int partialCnt;
}

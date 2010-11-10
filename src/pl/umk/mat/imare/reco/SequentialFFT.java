/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.reco;

import pl.umk.mat.imare.reco.internal.AlgebraicComplex;

/**
 *
 * @author Maciek
 */
public class SequentialFFT {

    private int imgW = 0;
    private int FFTbitsX = 0;
    private double FFTexRe[] = null, FFTexIm[] = null;
    private int FFTinvX[] = null;
    private double FFTresultRe[] = null;
    private double FFTresultIm[] = null;
    private AlgebraicComplex[] result;
    private double resultAmplitude[];
    private WindowFunction wf = null;

    public double mediana(int blocks, int steps) {
        int i, n2 = imgW >> 1, n4 = imgW >> 2;
        double max = FFTresultRe[0], min = max;
        for (i = 1; i < n2; i++) {
            if (FFTresultRe[i] > max) {
                max = FFTresultRe[i];
            } else if (FFTresultRe[i] < min) {
                min = FFTresultRe[i];
            }
        }
        double delta = (max - min) / blocks;
        int block[] = new int[blocks + 1];
        block[blocks] = imgW;
        for (i = 0; i < blocks; i++) {
            block[i] = 0;
        }
        for (i = 0; i < n2; i++) {
            int id = (int) ((FFTresultRe[i] - min) / delta);
            if ((id >= 0) && (id < blocks)) {
                block[id]++;
            }
        }
        int sum = 0;
        i = 0;
        while (sum < n4) {
            sum += block[i];
            i++;
        }
        i--;
        if (steps > 1) {
            sum -= block[i];
            if (i == blocks) {
                i--;
            }
            if (steps > 2) {
                int list[] = new int[n2];
                int done = 0;
                min += delta * i;
                delta /= blocks;
                for (i = 0; i < blocks; i++) {
                    block[i] = 0;
                }
                for (i = 0; i < n2; i++) {
                    int id = (int) ((FFTresultRe[i] - min) / delta);
                    if ((id >= 0) && (id < blocks)) {
                        block[id]++;
                        list[done] = i;
                        done++;
                    }
                }
                i = 0;
                while (sum < n4) {
                    sum += block[i];
                    i++;
                }
                i--;
                sum -= block[i];
                if (i == blocks) {
                    i--;
                }
                for (int j = 3; j <= steps; j++) {
                    min += delta * i;
                    delta /= blocks;
                    for (i = 0; i < blocks; i++) {
                        block[i] = 0;
                    }
                    i = done - 1;
                    while (i >= 0) {
                        int id = (int) ((FFTresultRe[list[i]] - min) / delta);
                        if ((id >= 0) && (id < blocks)) {
                            block[id]++;
                        } else {
                            done--;
                            list[i] = list[done];
                        }
                        i--;
                    }
                    i = 0;
                    while (sum < n4) {
                        sum += block[i];
                        i++;
                    }
                    i--;
                    sum -= block[i];
                    if (i == blocks) {
                        i--;
                    }
                }
            } else {
                min += delta * i;
                delta /= blocks;
                for (i = 0; i < blocks; i++) {
                    block[i] = 0;
                }
                for (i = 0; i < n2; i++) {
                    int id = (int) ((FFTresultRe[i] - min) / delta);
                    if ((id >= 0) && (id < blocks)) {
                        block[id]++;
                    }
                }
                i = 0;
                while (sum < n4) {
                    sum += block[i];
                    i++;
                }
                i--;
                if (i == blocks) {
                    i--;
                }
            }
        }
        return min + delta * (i + 0.5);
    }

    public SequentialFFT(int size) {
        FFTbitsX = 0;
        imgW = size;
        while (imgW > (1 << FFTbitsX)) {
            FFTbitsX++;
        }
        if ((1 << FFTbitsX) != size) {
            throw new IllegalArgumentException("requested FFT input size == " + size);
        }

        int n2 = imgW >> 1;

        FFTexRe = new double[n2];
        FFTexIm = new double[n2];
        for (int i = 0; i < n2; i++) {
            double x = (-2.0f * Math.PI * i / imgW);
            FFTexRe[i] = Math.cos(x);
            FFTexIm[i] = Math.sin(x);
        }
        FFTinvX = new int[imgW];
        FFTinvX[0] = 0;
        int nn1 = 0;
        int j = 1, an1, aN;
        for (int i = 1; i <= FFTbitsX; i++) {
            an1 = nn1;
            nn1 = (1 << i) - 1;
            aN = 1 << (FFTbitsX - i);
            while (j <= nn1) {
                FFTinvX[j] = aN | FFTinvX[j & an1];
                j++;
            }
        }
        FFTresultRe = new double[imgW];
        FFTresultIm = new double[imgW];
        result = new AlgebraicComplex[imgW>>1];
        resultAmplitude = new double[imgW>>1];
    }

    void compute() {
        int i, j, n2 = imgW >> 1, l;
        int jlev, aN, nn1, an1, id, id2;
        double z1R, z2R, z1I, z2I;

        jlev = FFTbitsX;
        aN = 1;
        for (j = n2; j > 1; j >>= 1)//for(j=n2;j>0;j>>=1)
        {
            jlev--;
            aN <<= 1;
            nn1 = ~(j - 1);
            an1 = (aN - 1) >> 1;
            for (i = 0; i < n2; i++) {
                id = i + (i & nn1);
                id2 = id | j;

                z1R = FFTresultRe[id];
                z1I = FFTresultIm[id];
                int nr = (FFTinvX[id2] & an1) << jlev;

                z2R = FFTresultRe[id2] * FFTexRe[nr] - FFTresultIm[id2] * FFTexIm[nr];
                z2I = FFTresultRe[id2] * FFTexIm[nr] + FFTresultIm[id2] * FFTexRe[nr];

                FFTresultRe[id] += z2R;
                FFTresultIm[id] += z2I;

                FFTresultRe[id2] = z1R - z2R;
                FFTresultIm[id2] = z1I - z2I;
            }
        }

        //j=1
        {
            an1 = (imgW - 1) >> 1;
            for (i = 0; i < n2; i++) {
                id = i << 1;
                id2 = id | 1;

                int nr = FFTinvX[id2] & an1;
                z2R = FFTresultRe[id2] * FFTexRe[nr] - FFTresultIm[id2] * FFTexIm[nr];
                z2I = FFTresultRe[id2] * FFTexIm[nr] + FFTresultIm[id2] * FFTexRe[nr];

                FFTresultRe[id] += z2R;
                FFTresultIm[id] += z2I;
            }
        }
        /*for(i=0;i<n2;i++)
        {
        int inv=FFTinvX[i];
        if(i<inv)
        {
        z1R=FFTresultRe[i];
        z1I=FFTresultIm[i];

        FFTresultRe[i]=FFTresultRe[inv];
        FFTresultIm[i]=FFTresultIm[inv];

        FFTresultRe[inv]=z1R;
        FFTresultIm[inv]=z1I;
        }
        z1R=FFTresultRe[i];
        z1I=FFTresultIm[i];
        FFTresultRe[i]=Math.hypot(z1I,z1R);
        FFTresultIm[i]=Math.atan2(z1I,z1R);
        }*/
        for (i = 0; i < n2; i += 2) {
            int i2 = i | 1;
            int inv = FFTinvX[i2];
            z1R = FFTresultRe[inv];
            z1I = FFTresultIm[inv];

            result[i2] = new AlgebraicComplex(z1R, z1I);
            //FFTresultRe[i2]=Math.hypot(z1I,z1R);
            //FFTresultIm[i2]=Math.atan2(z1I,z1R);

            inv = FFTinvX[i];
            if (i < inv) {
                z1R = FFTresultRe[i];
                z1I = FFTresultIm[i];

                z2R = FFTresultRe[inv];
                z2I = FFTresultIm[inv];

                result[i] = new AlgebraicComplex(z2R, z2I);
                //FFTresultRe[i]=Math.hypot(z2I,z2R);
                //FFTresultIm[i]=Math.atan2(z2I,z2R);

                result[inv] = new AlgebraicComplex(z1R, z1I);
                //FFTresultRe[inv]=Math.hypot(z1I,z1R);
                //FFTresultIm[inv]=Math.atan2(z1I,z1R);
            } else if (i == inv) {
                z1R = FFTresultRe[i];
                z1I = FFTresultIm[i];

                result[i] = new AlgebraicComplex(z1R, z1I);
                //FFTresultRe[i]=Math.hypot(z1I,z1R);
                //FFTresultIm[i]=Math.atan2(z1I,z1R);
            }
        }
        for (i = result.length-1; i >= 0; --i) resultAmplitude[i] = Math.sqrt(result[i].abs2());
    }

    public double getData(int i) {
        return resultAmplitude[i];
    }
//
//    public double getPhase(int i)
//    {
//        return FFTresultIm[i];
//    }

    public int getDataCount() {
        return imgW >> 1;
    }

    public AlgebraicComplex[] getDataArray() {
        return result;
    }
//
//    public double[] getPhaseArray()
//    {
//        return FFTresultIm;
//    }

    public void process(int probe[]) {
        if (wf == null) {
            for (int i = 0; i < imgW; i++) {
                FFTresultRe[i] = (double) probe[i];
                FFTresultIm[i] = 0.0;
            }
        } else {
            for (int i = 0; i < imgW; i++) {
                FFTresultRe[i] = (double) probe[i] * wf.value((double) i / (imgW - 1));
                FFTresultIm[i] = 0.0;
            }
        }
        compute();
    }

    public void process(double probe[]) {
        if (wf == null) {
            System.arraycopy(probe, 0, FFTresultRe, 0, imgW);
            for (int i = 0; i < imgW; i++) {
                FFTresultIm[i] = 0.0;
            }
        } else {
            for (int i = 0; i < imgW; i++) {
                FFTresultRe[i] = probe[i] * wf.value((double) i / (imgW - 1));
                FFTresultIm[i] = 0.0;
            }
        }
        compute();
    }

    public void process(AlgebraicComplex probe[]) {
        if (wf == null) {
            for (int i = 0; i < imgW; i++) {
                FFTresultRe[i] = probe[i].re;
                FFTresultIm[i] = probe[i].im;
            }
        } else {
            for (int i = 0; i < imgW; i++) {
                double w = wf.value((double) i / (imgW - 1));
                FFTresultRe[i] = probe[i].re * w;
                FFTresultIm[i] = probe[i].im * w;
            }
        }
        compute();
    }

    public void setWindowFunction(WindowFunction nwf) {
        wf = nwf;
    }
}

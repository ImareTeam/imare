/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.reco.parallel;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.umk.mat.imare.exception.FFTWrongSizeException;
import pl.umk.mat.imare.reco.WindowFunction;

/**
 *
 * @author Maciek
 */
public class FFT {
    private int imgW=0;
    private int FFTbitsX=0;
    private double FFTexRe[]=null,FFTexIm[]=null;
    private int FFTinvX[]=null;
    private double FFTresultRe[]=null;
    private double FFTresultIm[]=null;
    private double backupRe[]=null;
    private double backupIm[]=null;
    private boolean doBackup=false;
    private int numThreads=4;
    public Thread[] watek=null;
    private FFTThreadParams[] wat_param=null;
    private WindowFunction wf=null;
    private Semaphore[] sem=null;
    private FFTThreadParams params;
    private int partial=0;
    private double[] inputBuff;

    public void setPartial(int leng) throws FFTWrongSizeException
    {
      if(leng>imgW>>1)
      {
        throw new FFTWrongSizeException(leng);
      }
      else
      {
        int b=0;
        while((1<<b)<leng){b++;}
        if(leng!=(1<<b)){throw new FFTWrongSizeException(leng);}
        doBackup=true;
        partial=leng;
        inputBuff=new double[leng];
        backupRe=new double[imgW];
        backupIm=new double[imgW];
        params.doBackup=doBackup;
        params.backupIm=backupIm;
        params.backupRe=backupRe;
        params.partial=partial;
        params.partialCnt=(imgW/leng)-1;
        params.parteRe=new double[params.partialCnt+1];
        params.parteIm=new double[params.partialCnt+1];
        for(int i=0;i<=params.partialCnt;i++)
        {
          double a=2.0*Math.PI*leng*i/imgW;
          params.parteRe[i]=Math.cos(a);
          params.parteIm[i]=Math.sin(a);
        }
        for(int i=0;i<numThreads-1;i++)
        {
          wat_param[i].doBackup=doBackup;
          wat_param[i].backupIm=backupIm;
          wat_param[i].backupRe=backupRe;
          wat_param[i].partial=partial;
          wat_param[i].partialCnt=(imgW/leng)-1;
          wat_param[i].parteRe=params.parteRe;
          wat_param[i].parteIm=params.parteIm;
        }
      }
    }

    public void end()
    {
      for(int i=0;i<numThreads-1;i++)
      {
        wat_param[i].working=false;
      }
      params.first.release(numThreads-1);
    }

    public double mediana(int blocks,int steps)
    {
      int i,n2=imgW>>1,n4=imgW>>2;
      double max=FFTresultRe[0],min=max;
      for(i=1;i<n2;i++)
      {
        if(FFTresultRe[i]>max){max=FFTresultRe[i];}
        else if(FFTresultRe[i]<min){min=FFTresultRe[i];}
      }
      double delta=(max-min)/blocks;
      int block[]=new int[blocks+1];
      block[blocks]=imgW;
      for(i=0;i<blocks;i++){block[i]=0;}
      for(i=0;i<n2;i++)
      {
        int id=(int)((FFTresultRe[i]-min)/delta);
        if((id>=0)&&(id<blocks)){block[id]++;}
      }
      int sum=0;
      i=0;
      while(sum<n4)
      {
        sum+=block[i];
        i++;
      }
      i--;
      if(steps>1)
      {
        sum-=block[i];
        if(i==blocks){i--;}
        if(steps>2)
        {
          int list[]=new int[n2];
          int done=0;
          min+=delta*i;
          delta/=blocks;
          for(i=0;i<blocks;i++){block[i]=0;}
          for(i=0;i<n2;i++)
          {
            int id=(int)((FFTresultRe[i]-min)/delta);
            if((id>=0)&&(id<blocks))
            {
              block[id]++;
              list[done]=i;
              done++;
            }
          }
          i=0;
          while(sum<n4)
          {
            sum+=block[i];
            i++;
          }
          i--;
          sum-=block[i];
          if(i==blocks){i--;}
          for(int j=3;j<=steps;j++)
          {
            min+=delta*i;
            delta/=blocks;
            for(i=0;i<blocks;i++){block[i]=0;}
            i=done-1;
            while(i>=0)
            {
              int id=(int)((FFTresultRe[list[i]]-min)/delta);
              if((id>=0)&&(id<blocks))
              {
                block[id]++;
              }
              else
              {
                done--;
                list[i]=list[done];
              }
              i--;
            }
            i=0;
            while(sum<n4)
            {
              sum+=block[i];
              i++;
            }
            i--;
            sum-=block[i];
            if(i==blocks){i--;}
          }
        }
        else
        {
          min+=delta*i;
          delta/=blocks;
          for(i=0;i<blocks;i++){block[i]=0;}
          for(i=0;i<n2;i++)
          {
            int id=(int)((FFTresultRe[i]-min)/delta);
            if((id>=0)&&(id<blocks))
            {
              block[id]++;
            }
          }
          i=0;
          while(sum<n4)
          {
            sum+=block[i];
            i++;
          }
          i--;
          if(i==blocks){i--;}
        }
      }
      return min+delta*(i+0.5);
    }

    public FFT(int size,int nThreads) throws FFTWrongSizeException
    {
      if(nThreads>0){numThreads=nThreads;}
      else
      {
        numThreads=Runtime.getRuntime().availableProcessors()+1;
      }
      FFTbitsX=0;
      imgW=size;
      while(imgW>(1<<FFTbitsX)){FFTbitsX++;}
      if ((1<<FFTbitsX) != size) throw new FFTWrongSizeException(size);

      int n2=imgW>>1;

      FFTexRe=new double[n2];
      FFTexIm=new double[n2];
      for(int i=0;i<n2;i++)
      {
        double x=(-2.0f * Math.PI * i / imgW);
        FFTexRe[i]=Math.cos(x);
        FFTexIm[i]=Math.sin(x);
      }
      FFTinvX=new int[imgW];
      FFTinvX[0]=0;
      int nn1=0;
      int j=1,an1,aN;
      for(int i=1;i<=FFTbitsX;i++)
      {
        an1=nn1;
        nn1=(1<<i)-1;
        aN=1<<(FFTbitsX-i);
        while(j<=nn1)
        {
          FFTinvX[j]=aN|FFTinvX[j&an1];
          j++;
        }
      }
      FFTresultRe=new double[imgW];
      FFTresultIm=new double[imgW];

      numThreads=Math.min(numThreads,FFTbitsX>>1);

      watek=new Thread[numThreads-1];

      wat_param=new FFTThreadParams[numThreads-1];

      int jlev=FFTbitsX;
      aN=1;
      params=new FFTThreadParams();
      int maxB=1;
      while(maxB<numThreads){maxB<<=1;}
      params.bar=new Barrier[maxB];
      for(int i=0;i<maxB;i++){params.bar[i]=new Barrier();}
      params.jlev=jlev;
      params.last=new Semaphore(0);
      params.first=new Semaphore(0);
      params.aN=aN;
      params.numThreads=numThreads;
      params.ownid=0;

      params.FFTinvX=FFTinvX;
      params.FFTresultIm=FFTresultIm;
      params.FFTresultRe=FFTresultRe;
      params.n2=n2;
      params.FFTexIm=FFTexIm;
      params.FFTexRe=FFTexRe;
      for(int i=0;i<numThreads-1;i++)
      {
        wat_param[i]=new FFTThreadParams();
        wat_param[i].bar=params.bar;
        wat_param[i].jlev=jlev;
        wat_param[i].aN=aN;
        wat_param[i].numThreads=numThreads;
        wat_param[i].ownid=i+1;
        wat_param[i].last=params.last;
        wat_param[i].first=params.first;

        wat_param[i].FFTinvX=FFTinvX;
        wat_param[i].FFTresultIm=FFTresultIm;
        wat_param[i].FFTresultRe=FFTresultRe;
        wat_param[i].n2=n2;
        wat_param[i].FFTexIm=FFTexIm;
        wat_param[i].FFTexRe=FFTexRe;
        watek[i]=new Thread(new FFTThread(wat_param[i]),"FFT thread");
        watek[i].setDaemon(true);
        watek[i].start();
      }
    }

    private void computeSequential()
    {
      int i,j,n2=imgW>>1,l;
      int jlev,aN,nn1,an1,id,id2;
      double z1R,z2R,z1I,z2I;

      jlev=FFTbitsX;
      aN=1;
      for(j=n2;j>1;j>>=1)//for(j=n2;j>0;j>>=1)
      {
        jlev--;
        aN<<=1;
        nn1=~(j-1);
        an1=(aN-1)>>1;
        for(i=0;i<n2;i++)
        {
          id=i+(i&nn1);
          id2=id|j;

          z1R=FFTresultRe[id];
          z1I=FFTresultIm[id];
          int nr=(FFTinvX[id2]&an1)<<jlev;

          z2R=FFTresultRe[id2]*FFTexRe[nr]-FFTresultIm[id2]*FFTexIm[nr];
          z2I=FFTresultRe[id2]*FFTexIm[nr]+FFTresultIm[id2]*FFTexRe[nr];

          FFTresultRe[id]+=z2R;
          FFTresultIm[id]+=z2I;

          FFTresultRe[id2]=z1R-z2R;
          FFTresultIm[id2]=z1I-z2I;
        }
      }

      //j=1
      {
        an1=(imgW-1)>>1;
        for(i=0;i<n2;i++)
        {
          id=i<<1;
          id2=id|1;

          int nr=FFTinvX[id2]&an1;
          z2R=FFTresultRe[id2]*FFTexRe[nr]-FFTresultIm[id2]*FFTexIm[nr];
          z2I=FFTresultRe[id2]*FFTexIm[nr]+FFTresultIm[id2]*FFTexRe[nr];

          FFTresultRe[id]+=z2R;
          FFTresultIm[id]+=z2I;
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
      for(i=0;i<n2;i+=2)
      {
        int i2=i|1;
        int inv=FFTinvX[i2];
        z1R=FFTresultRe[inv];
        z1I=FFTresultIm[inv];

        if(params.doBackup)
        {
          params.backupRe[i2]=z1R;
          params.backupIm[i2]=z1I;
        }

        FFTresultRe[i2]=Math.hypot(z1I,z1R);
        FFTresultIm[i2]=Math.atan2(z1I,z1R);

        inv=FFTinvX[i];
        if(i<inv)
        {
          z1R=FFTresultRe[i];
          z1I=FFTresultIm[i];

          z2R=FFTresultRe[inv];
          z2I=FFTresultIm[inv];

          FFTresultRe[i]=Math.hypot(z2I,z2R);
          FFTresultIm[i]=Math.atan2(z2I,z2R);

          FFTresultRe[inv]=Math.hypot(z1I,z1R);
          FFTresultIm[inv]=Math.atan2(z1I,z1R);
        }
        else if(i==inv)
        {
          z1R=FFTresultRe[i];
          z1I=FFTresultIm[i];

          FFTresultRe[i]=Math.hypot(z1I,z1R);
          FFTresultIm[i]=Math.atan2(z1I,z1R);
        }
      }
    }

    private void compute()
    {
      /*double []re=new double[FFTresultRe.length];
      double []im=new double[FFTresultIm.length];
      System.arraycopy(FFTresultRe, 0, re, 0, FFTresultRe.length);
      System.arraycopy(FFTresultIm, 0, im, 0, FFTresultIm.length);
      computeSequential();
      double []re2=new double[FFTresultRe.length];
      double []im2=new double[FFTresultIm.length];
      System.arraycopy(FFTresultRe, 0, re2, 0, FFTresultRe.length);
      System.arraycopy(FFTresultIm, 0, im2, 0, FFTresultIm.length);

      System.arraycopy(re, 0, FFTresultRe, 0, FFTresultRe.length);
      System.arraycopy(im, 0, FFTresultIm, 0, FFTresultIm.length);
      re=null;im=null;*/

      params.first.release(numThreads-1);

      int i,j,n2=imgW>>1,l,nt=numThreads,lev;
      int jlev,aN,nn1,an1,id,id2;
      int end=n2,barid=0;
      double z1R,z2R,z1I,z2I;

      jlev=FFTbitsX;
      aN=1;
      lev=n2;
      if(params.compPartial)
      {
        while(lev>=params.partial)
        {
          jlev--;
          aN<<=1;
          if(nt>1)
          {
            int nl=nt>>1;
            barid<<=1;

            nt=nl;
            end>>=1;
            barid|=1;
          }
          lev>>=1;
        }
      }

      while(nt>1)
      {
        j=lev;
        jlev--;
        aN<<=1;
        nn1=~(j-1);
        an1=(aN-1)>>1;
        for(i=0;i<end;i+=nt)
        {
          id=i+(i&nn1);
          id2=id|j;

          z1R=FFTresultRe[id];
          z1I=FFTresultIm[id];
          int nr=(FFTinvX[id2]&an1)<<jlev;

          z2R=FFTresultRe[id2]*FFTexRe[nr]-FFTresultIm[id2]*FFTexIm[nr];
          z2I=FFTresultRe[id2]*FFTexIm[nr]+FFTresultIm[id2]*FFTexRe[nr];

          FFTresultRe[id]+=z2R;
          FFTresultIm[id]+=z2I;

          FFTresultRe[id2]=z1R-z2R;
          FFTresultIm[id2]=z1I-z2I;
        }
        params.bar[barid].add(nt);
              try {
                  params.bar[barid].sem.acquire();
              } catch (InterruptedException ex) {
                  Logger.getLogger(FFTThread.class.getName()).log(Level.SEVERE, null, ex);
              }

        lev>>=1;
        int nl=nt>>1;
        barid<<=1;

        nt=nl;
        end>>=1;
        barid|=1;

      }
      for(j=lev;j>1;j>>=1)
      {
        jlev--;
        aN<<=1;
        nn1=~(j-1);
        an1=(aN-1)>>1;
        for(i=0;i<end;i++)
        {
          id=i+(i&nn1);
          id2=id|j;

          z1R=FFTresultRe[id];
          z1I=FFTresultIm[id];
          int nr=(FFTinvX[id2]&an1)<<jlev;

          z2R=FFTresultRe[id2]*FFTexRe[nr]-FFTresultIm[id2]*FFTexIm[nr];
          z2I=FFTresultRe[id2]*FFTexIm[nr]+FFTresultIm[id2]*FFTexRe[nr];

          FFTresultRe[id]+=z2R;
          FFTresultIm[id]+=z2I;

          FFTresultRe[id2]=z1R-z2R;
          FFTresultIm[id2]=z1I-z2I;
        }
      }

      //j=1
      if(lev>0)
      {
        an1=n2-1;
        for(i=0;i<end;i++)
        {
          id=i<<1;
          id2=id|1;

          int nr=FFTinvX[id2]&an1;
          z2R=FFTresultRe[id2]*FFTexRe[nr]-FFTresultIm[id2]*FFTexIm[nr];
          z2I=FFTresultRe[id2]*FFTexIm[nr]+FFTresultIm[id2]*FFTexRe[nr];

          FFTresultRe[id]+=z2R;
          FFTresultIm[id]+=z2I;
        }
      }

      nt=numThreads;
      params.bar[params.bar.length-1].add(nt);
        try {
            params.bar[params.bar.length - 1].sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(FFT.class.getName()).log(Level.SEVERE, null, ex);
        }

      nt<<=1;
        if(params.compPartial)
        {
          for(i=params.ownid<<1;i<n2;i+=nt)
          {
            int i2=i|1;
            int inv=params.FFTinvX[i2];
            z1R=params.FFTresultRe[inv];
            z1I=params.FFTresultIm[inv];

            z1R+=params.backupRe[i2];
            z1I+=params.backupIm[i2];

            /*double alpha=Math.PI*i2*params.partial/params.n2;
            double c=Math.cos(alpha);
            double s=Math.sin(alpha);*/

            int eid=i2&params.partialCnt;
            double c=params.parteRe[eid];
            double s=params.parteIm[eid];

            double re=c*z1R-s*z1I;
            double im=c*z1I+s*z1R;

            params.backupRe[i2]=re;
            params.backupIm[i2]=im;

            params.FFTresultRe[i2]=Math.hypot(im,re);
            params.FFTresultIm[i2]=Math.atan2(im,re);

            inv=params.FFTinvX[i];
            if(i<inv)
            {
              z1R=params.FFTresultRe[i];
              z1I=params.FFTresultIm[i];

              z2R=params.FFTresultRe[inv];
              z2I=params.FFTresultIm[inv];

              /*alpha=Math.PI*i*params.partial/params.n2;
              c=Math.cos(alpha);
              s=Math.sin(alpha);*/

              eid=i&params.partialCnt;
              c=params.parteRe[eid];
              s=params.parteIm[eid];

              z2I+=params.backupIm[i];
              z2R+=params.backupRe[i];

              z1I+=params.backupIm[inv];
              z1R+=params.backupRe[inv];

              re=c*z2R-s*z2I;
              im=c*z2I+s*z2R;

              params.backupIm[i]=im;
              params.backupRe[i]=re;

              params.FFTresultRe[i]=Math.hypot(im,re);
              params.FFTresultIm[i]=Math.atan2(im,re);

              /*alpha=Math.PI*inv*params.partial/params.n2;
              c=Math.cos(alpha);
              s=Math.sin(alpha);*/

              eid=inv&params.partialCnt;
              c=params.parteRe[eid];
              s=params.parteIm[eid];

              re=c*z1R-s*z1I;
              im=c*z1I+s*z1R;

              params.backupIm[inv]=im;
              params.backupRe[inv]=re;

              params.FFTresultRe[inv]=Math.hypot(im,re);
              params.FFTresultIm[inv]=Math.atan2(im,re);
            }
            else if(i==inv)
            {
              z1R=params.FFTresultRe[i];
              z1I=params.FFTresultIm[i];

              z1I+=params.backupIm[inv];
              z1R+=params.backupRe[inv];

              /*alpha=Math.PI*inv*params.partial/params.n2;
              c=Math.cos(alpha);
              s=Math.sin(alpha);*/

              eid=inv&params.partialCnt;
              c=params.parteRe[eid];
              s=params.parteIm[eid];

              re=c*z1R-s*z1I;
              im=c*z1I+s*z1R;

              params.backupIm[inv]=im;
              params.backupRe[inv]=re;

              params.FFTresultRe[inv]=Math.hypot(im,re);
              params.FFTresultIm[inv]=Math.atan2(im,re);
            }
          }
        }
        else
        {
          for(i=params.ownid<<1;i<n2;i+=nt)
          {
            int i2=i|1;
            int inv=params.FFTinvX[i2];
            z1R=params.FFTresultRe[inv];
            z1I=params.FFTresultIm[inv];

            if(params.doBackup)
            {
              params.backupRe[i2]=z1R;
              params.backupIm[i2]=z1I;
            }

            params.FFTresultRe[i2]=Math.hypot(z1I,z1R);
            params.FFTresultIm[i2]=Math.atan2(z1I,z1R);

            inv=params.FFTinvX[i];
            if(i<inv)
            {
              z1R=params.FFTresultRe[i];
              z1I=params.FFTresultIm[i];

              z2R=params.FFTresultRe[inv];
              z2I=params.FFTresultIm[inv];

              if(params.doBackup)
              {
                params.backupRe[inv]=z1R;
                params.backupIm[inv]=z1I;

                params.backupRe[i]=z2R;
                params.backupIm[i]=z2I;
              }

              params.FFTresultRe[i]=Math.hypot(z2I,z2R);
              params.FFTresultIm[i]=Math.atan2(z2I,z2R);

              params.FFTresultRe[inv]=Math.hypot(z1I,z1R);
              params.FFTresultIm[inv]=Math.atan2(z1I,z1R);
            }
            else if(i==inv)
            {
              z1R=params.FFTresultRe[i];
              z1I=params.FFTresultIm[i];

              if(params.doBackup)
              {
                params.backupRe[i]=z1R;
                params.backupIm[i]=z1I;
              }

              params.FFTresultRe[i]=Math.hypot(z1I,z1R);
              params.FFTresultIm[i]=Math.atan2(z1I,z1R);
            }
          }
        }

        try {
            params.last.acquire(numThreads - 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(FFT.class.getName()).log(Level.SEVERE, null, ex);
        }
      /*double delta=0.0,d2;
      int did=-1;
      for(i=0;i<n2;i++)
      {
        d2=Math.abs(re2[i]-FFTresultRe[i])+Math.abs(im2[i]-FFTresultIm[i]);
        if(d2>delta){delta=d2;did=i;}
      }
      System.out.println(did+" "+delta);*/
    }

    public double getData(int i)
    {
        return FFTresultRe[i];
    }

    public double getPhase(int i)
    {
        return FFTresultIm[i];
    }

    public int getDataCount()
    {
        return imgW>>1;
    }

    public double[] getDataArray()
    {
        return FFTresultRe;
    }

    public double[] getPhaseArray()
    {
        return FFTresultIm;
    }

    public void processPartial(int[] _old,int[] _new)
    {
      if(!params.compPartial)
      {
        params.compPartial=true;
        for(int i=0;i<numThreads-1;i++){wat_param[i].compPartial=true;}
      }
      if(wf==null)
      {
        for(int i=0;i<_old.length;i++)
        {
            inputBuff[i]=(double)(_new[i]-_old[i]);
        }
        int i=0;
        while(i<imgW)
        {
          System.arraycopy(inputBuff, 0, FFTresultRe, i, _old.length);
          i+=_old.length;
        }
        for(i=0;i<imgW;i++){FFTresultIm[i]=0.0;}
      }
      else
      {

      }
      compute();
    }

    public void processPartial(double[] _old,double[] _new)
    {
      if(!params.compPartial)
      {
        params.compPartial=true;
        for(int i=0;i<numThreads-1;i++){wat_param[i].compPartial=true;}
      }
      if(wf==null)
      {
        for(int i=0;i<_old.length;i++)
        {
            inputBuff[i]=(_new[i]-_old[i]);
        }
        int i=0;
        while(i<imgW)
        {
          System.arraycopy(inputBuff, 0, FFTresultRe, i, _old.length);
          i+=_old.length;
        }
        for(i=0;i<imgW;i++){FFTresultIm[i]=0.0;}
      }
      else
      {

      }
      compute();
    }

    public void process(int probe[])
    {
        if(params.compPartial)
        {
          params.compPartial=false;
          for(int i=0;i<numThreads-1;i++){wat_param[i].compPartial=false;}
        }
        if(wf==null)
        {
          for(int i=0;i<imgW;i++)
          {
              FFTresultRe[i]=(double)probe[i];
              FFTresultIm[i]=0.0;
          }
        }
        else
        {
          for(int i=0;i<imgW;i++)
          {
              FFTresultRe[i]=(double)probe[i]*wf.value((double)i/(imgW-1));
              FFTresultIm[i]=0.0;
          }
        }
        compute();
    }

    public void process(double probe[])
    {
        if(params.compPartial)
        {
          params.compPartial=false;
          for(int i=0;i<numThreads-1;i++){wat_param[i].compPartial=false;}
        }
        if(wf==null)
        {
          System.arraycopy(probe,0,FFTresultRe,0,imgW);
          for(int i=0;i<imgW;i++)
          {
            FFTresultIm[i]=0.0;
          }
        }
        else
        {
          for(int i=0;i<imgW;i++)
          {
              FFTresultRe[i]=(double)probe[i]*wf.value((double)i/(imgW-1));
              FFTresultIm[i]=0.0;
          }
        }
        compute();
    }

    public void setWindowFunction(WindowFunction nwf)
    {
        wf=nwf;
    }

}

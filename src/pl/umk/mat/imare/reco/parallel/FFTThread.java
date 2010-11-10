/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.reco.parallel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Maciek
 */
public class FFTThread implements Runnable {

    private FFTThreadParams params;

    FFTThread(FFTThreadParams p)
    {
      params=p;
    }

    @Override
    public void run() {
      while(true)
      {
        try {
          params.first.acquire();
        } catch (InterruptedException ex) {
          Logger.getLogger(FFTThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(!params.working){break;}
        int i,j,n2=params.n2,l,nt=params.numThreads,begid=0,lev;
        int jlev,aN,nn1,an1,id,id2;
        int beg=0,end=n2,barid=0;
        double z1R,z2R,z1I,z2I;

        jlev=params.jlev;
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
              if(params.ownid<begid+nl)
              {
                nt=nl;
                end-=(end-beg)>>1;
                barid|=1;
              }
              else
              {
                nt-=nl;
                begid+=nl;
                barid+=2;
                beg+=(end-beg)>>1;
              }
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
          for(i=params.ownid-begid+beg;i<end;i+=nt)
          {
            id=i+(i&nn1);
            id2=id|j;

            z1R=params.FFTresultRe[id];
            z1I=params.FFTresultIm[id];
            int nr=(params.FFTinvX[id2]&an1)<<jlev;

            z2R=params.FFTresultRe[id2]*params.FFTexRe[nr]-params.FFTresultIm[id2]*params.FFTexIm[nr];
            z2I=params.FFTresultRe[id2]*params.FFTexIm[nr]+params.FFTresultIm[id2]*params.FFTexRe[nr];

            params.FFTresultRe[id]+=z2R;
            params.FFTresultIm[id]+=z2I;

            params.FFTresultRe[id2]=z1R-z2R;
            params.FFTresultIm[id2]=z1I-z2I;
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
          if(params.ownid<begid+nl)
          {
            nt=nl;
            end-=(end-beg)>>1;
            barid|=1;
          }
          else
          {
            nt-=nl;
            begid+=nl;
            barid+=2;
            beg+=(end-beg)>>1;
          }
        }
        for(j=lev;j>1;j>>=1)
        {
          jlev--;
          aN<<=1;
          nn1=~(j-1);
          an1=(aN-1)>>1;
          for(i=beg;i<end;i++)
          {
            id=i+(i&nn1);
            id2=id|j;

            z1R=params.FFTresultRe[id];
            z1I=params.FFTresultIm[id];
            int nr=(params.FFTinvX[id2]&an1)<<jlev;

            z2R=params.FFTresultRe[id2]*params.FFTexRe[nr]-params.FFTresultIm[id2]*params.FFTexIm[nr];
            z2I=params.FFTresultRe[id2]*params.FFTexIm[nr]+params.FFTresultIm[id2]*params.FFTexRe[nr];

            params.FFTresultRe[id]+=z2R;
            params.FFTresultIm[id]+=z2I;

            params.FFTresultRe[id2]=z1R-z2R;
            params.FFTresultIm[id2]=z1I-z2I;
          }
        }

        //j=1
        if(lev>0)
        {
          an1=n2-1;
          for(i=beg;i<end;i++)
          {
            id=i<<1;
            id2=id|1;

            int nr=params.FFTinvX[id2]&an1;
            z2R=params.FFTresultRe[id2]*params.FFTexRe[nr]-params.FFTresultIm[id2]*params.FFTexIm[nr];
            z2I=params.FFTresultRe[id2]*params.FFTexIm[nr]+params.FFTresultIm[id2]*params.FFTexRe[nr];

            params.FFTresultRe[id]+=z2R;
            params.FFTresultIm[id]+=z2I;
          }
        }
        nt=params.numThreads;
        params.bar[params.bar.length-1].add(nt);
            try {
                params.bar[params.bar.length - 1].sem.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(FFTThread.class.getName()).log(Level.SEVERE, null, ex);
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

        params.last.release();
      }
    }

}

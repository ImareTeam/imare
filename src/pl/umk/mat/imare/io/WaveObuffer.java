/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.io;

import javax.sound.sampled.AudioFormat;
import javazoom.jl.decoder.Obuffer;

/**
 *
 * @author Maciek
 */
public class WaveObuffer extends Obuffer {
    private short data[][]=null;
    private AudioFormat format = null;
    private short buff[][]=null;
    private int buffuse[]=null;
    private int numFrames=0;
    private int channelFrames[]=null;
    //private List<ArrayList<Short>> data=null;
    private int frequency=0;

    public final static int BUFF_SIZE=51200;

    public WaveObuffer(int channels,int freq,int leng)
    {
      frequency=freq;
      data=new short[channels][];
      //data=new ArrayList<ArrayList<Short>>();
      /*for(int i=0;i<channels;i++)
      {
        data.add(new ArrayList<Short>());
      }*/
      buff=new short[channels][];
      buffuse=new int[channels];
      channelFrames=new int[channels];
      if(leng<1){leng=1;}
      for(int i=0;i<channels;i++)
      {
        buff[i]=new short[BUFF_SIZE];
        buffuse[i]=0;
        channelFrames[i]=0;
        data[i]=new short[leng];
      }
    }

    public AudioFormat getFormat()
    {
      numFrames=channelFrames[0]+buffuse[0];
      for(int i=0;i<data.length;i++)
      {
        if(buffuse[i]>0)
        {
          if(channelFrames[i]+buffuse[i]>data[i].length)
          {
            short tmp[]=new short[channelFrames[i]+buffuse[i]];
            System.arraycopy(data[i], 0, tmp, 0, channelFrames[i]);
            data[i]=tmp;
          }
          System.arraycopy(buff[i], 0, data[i], channelFrames[i], buffuse[i]);
          channelFrames[i]+=buffuse[i];

          buffuse[i]=0;
        }
        if(data[i].length>channelFrames[i])
        {
          short tmp[]=new short[channelFrames[i]];
          System.arraycopy(data[i], 0, tmp, 0, channelFrames[i]);
          data[i]=tmp;
        }
        if(channelFrames[i]<numFrames){numFrames=channelFrames[i];}
      }
      format=new AudioFormat((float)frequency,16,data.length,true,true);
      //format=new AudioFormat((float)frequency,16,data.get(0).size(),true,true);
      return format;
    }

    public short[][] getData()
    {
      for(int i=0;i<data.length;i++)
      {
        if(buffuse[i]>0)
        {
          if(channelFrames[i]+buffuse[i]>data[i].length)
          {
            short tmp[]=new short[channelFrames[i]+buffuse[i]];
            System.arraycopy(data[i], 0, tmp, 0, channelFrames[i]);
            data[i]=tmp;
          }
          System.arraycopy(buff[i], 0, data[i], channelFrames[i], buffuse[i]);
          channelFrames[i]+=buffuse[i];

          buffuse[i]=0;
        }
        if(data[i].length>channelFrames[i])
        {
          short tmp[]=new short[channelFrames[i]];
          System.arraycopy(data[i], 0, tmp, 0, channelFrames[i]);
          data[i]=tmp;
        }
      }
      int bitsPerSample = format.getSampleSizeInBits();
      if (bitsPerSample < Wave.MAX_BIT) {
          for (short[] channelData : data) {
              for (int j=0; j<channelData.length; j++) {
                  channelData[j] <<= (Wave.MAX_BIT - bitsPerSample);
              }
          }
      }
      return data;
      /*int[][] dat=new int[data.size()][];
      for(int i=0;i<data.size();i++)
      {
        dat[i]=new int[data.get(i).size()];
        for(int j=0;j<data.get(i).size();j++)
        {
          dat[i][j]=data.get(i).get(j).intValue();
        }
      }
      return dat;*/
    }

  /*private final short mclip(float sample)
    {
	return ((sample > 32767.0f) ? 32767 :
           ((sample < -32768.0f) ? -32768 :
			  (short) sample));
    }

    @Override
    public void appendSamples(int channel,float[] f)
    {
      if(buffuse[channel]+32<BUFF_SIZE)
      {
        for(int i=0;i<32;i++)
        {
          buff[channel][buffuse[channel]+i]=mclip(f[i]);
        }
        buffuse[channel]+=32;
      }
      else
      {
        int d1=BUFF_SIZE-buffuse[channel];
        for(int i=0;i<d1;i++)
        {
          buff[channel][buffuse[channel]+i]=mclip(f[i]);
        }
        {
          int tmp[]=new int[channelFrames[channel]+BUFF_SIZE];
          if(channelFrames[channel]>0)
          {
            System.arraycopy(data[channel], 0,tmp, 0, channelFrames[channel]);
          }
          data[channel]=tmp;

          System.arraycopy(buff[channel], 0, data[channel],channelFrames[channel], BUFF_SIZE);

          channelFrames[channel]+=BUFF_SIZE;
        }
        int d2=32-d1;
        buffuse[channel]=d2;
        for(int i=0;i<d2;i++)
        {
          buff[channel][i]=mclip(f[i+d1]);
        }
      }
    }*/

    @Override
    public void append(int arg0, short arg1) {
        buff[arg0][buffuse[arg0]]=arg1;
        buffuse[arg0]++;
        if(buffuse[arg0]>=BUFF_SIZE)
        {
          if(channelFrames[arg0]+BUFF_SIZE>data[arg0].length)
          {
            short tmp[]=new short[channelFrames[arg0]+BUFF_SIZE];
            if(channelFrames[arg0]>0)
            {
              System.arraycopy(data[arg0], 0,tmp, 0, channelFrames[arg0]);
            }
            data[arg0]=tmp;
          }
          System.arraycopy(buff[arg0], 0, data[arg0],channelFrames[arg0], BUFF_SIZE);
          buffuse[arg0]=0;

          channelFrames[arg0]+=BUFF_SIZE;
        }
        //data.get(arg0).add(new Short(arg1));
    }

    @Override
    public void write_buffer(int arg0) {
    }

    @Override
    public void close() {
      for(int i=0;i<data.length;i++)
      {
        if(buffuse[i]>0)
        {
          if(channelFrames[i]+buffuse[i]>data[i].length)
          {
            short tmp[]=new short[channelFrames[i]+buffuse[i]];
            System.arraycopy(data[i], 0, tmp, 0, channelFrames[i]);
            data[i]=tmp;
          }
          System.arraycopy(buff[i], 0, data[i], channelFrames[i], buffuse[i]);
          channelFrames[i]+=buffuse[i];

          buffuse[i]=0;
        }
        buff[i]=null;
      }
      buff=null;
    }

    @Override
    public void clear_buffer() {
    }

    @Override
    public void set_stop_flag() {
    }

}

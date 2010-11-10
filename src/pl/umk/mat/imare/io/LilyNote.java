/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.io;

/**
 *
 * @author Maciek
 */
public class LilyNote {
    private String letter;
    private int leng;
    private int numDot;
    private int octave;
    public LilyNote(String _letter,int _leng,int _numDot,int _octave)
    {
      letter=_letter;
      leng=_leng;
      numDot=_numDot;
      octave=_octave;
    }

    public boolean isPause()
    {
      return letter.equals("s");
    }

    public String relativeWrite(LilyNote n)
    {
      if(n==null){return write();}
      else
      {
        String s=" "+letter;
        if(!letter.equals("s"))
        {
          int i,l;
          if(octave<3)
          {
            l=3-octave;
            for(i=0;i<l;i++)
            {
              s+=',';
            }
          }
          else
          {
            l=octave-3;
            for(i=0;i<l;i++)
            {
              s+='\'';
            }
          }
        }
        if(leng!=n.leng){s+=leng;}
        if(!letter.equals("s"))
        {
          for(int i=0;i<numDot;i++)
          {
            s+='.';
          }
        }
        return s;
      }
    }

    public String write()
    {
      String s=" "+letter;
      if(!letter.equals("s"))
      {
        int i,l;
        if(octave<3)
        {
          l=3-octave;
          for(i=0;i<l;i++)
          {
            s+=',';
          }
        }
        else
        {
          l=octave-3;
          for(i=0;i<l;i++)
          {
            s+='\'';
          }
        }
      }
      s+=leng;
      if(!letter.equals("s"))
      {
        for(int i=0;i<numDot;i++)
        {
          s+='.';
        }
      }
      return s;
    }
}

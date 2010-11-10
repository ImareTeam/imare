/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import pl.umk.mat.imare.reco.Note;
import pl.umk.mat.imare.reco.StaveData;
import pl.umk.mat.imare.reco.Tonality.Sign;

/**
 *
 * @author Maciek
 */
public class LilySong {
    private ArrayList<ArrayList<LilyNote>> top=null,bottom=null;

    public void write(File f) throws IOException
    {
      FileWriter outFile = new FileWriter(f);
      PrintWriter out = new PrintWriter(outFile);
      out.println("\\version \"2.12.3\"");
      out.println("\\score");
      out.println("{");
      out.println("\\new PianoStaff");
      out.println("{");
      out.println("<<");
      out.println("\\cadenzaOn");
      out.println("#(set-accidental-style 'forget 'Score)");
      int j;
      for(int id=0;id<2;id++)
      {
        ArrayList<ArrayList<LilyNote>> list=null;
        switch(id)
        {
            case 0:list=top;break;
            case 1:list=bottom;break;
        }
        if(list!=null)
        {
          out.println("\\new Staff");
          out.println("<<");
          for(j=0;j<list.size();j++)
          {
            out.println("\\new Voice \\with { \\remove Forbid_line_break_engraver }");
            out.println("{");
            if(id==1)
            {
              out.println("\\clef bass");
            }
            ArrayList<LilyNote> l=list.get(j);
            LilyNote prevn=null;
            for(int i=0;i<l.size();i++)
            {
              LilyNote n=l.get(i);

              out.print(n.relativeWrite(prevn));
              if(!n.isPause()){out.print(" \\bar \" \"");}
              prevn=n;
            }
            out.println();
            out.println("}");
          }
          out.println(">>");
        }
      }
      out.println(">>");
      out.println("}");
      out.println("}");
      out.close();
    }

    public void fromNotes(StaveData data)
    {
      for(int id=0;id<2;id++)
      {
        ArrayList<Note> d=null;
        ArrayList<ArrayList<LilyNote>> list=null;
        switch(id)
        {
          case 0:
            d=data.top;
            top=new ArrayList<ArrayList<LilyNote>>();
            list=top;
          break;
          case 1:
            d=data.bottom;
            bottom=new ArrayList<ArrayList<LilyNote>>();
            list=bottom;
          break;
        }
        if(d!=null)
        {
          boolean done[]=new boolean[d.size()];
          int numdone=0;

          for(int i=0;i<done.length;i++){done[i]=false;}
          int beg=0;
          while(numdone<done.length)
          {
            int j=beg;
            while(done[j]){j++;}
            beg=j+1;
            numdone++;
            done[j]=true;
            ArrayList<LilyNote> l=new ArrayList<LilyNote>();
            list.add(l);
            Note n=d.get(j);
            j++;
            float t=n.getOffset();
            int typ=0;
            float s=0.0f;
            while((typ<6)&&(t-s>1.0f/32))
            {
              if(t-s>=1.0/(1<<typ))
              {
                s+=1.0/(1<<typ);
                l.add(new LilyNote("s",1<<typ,0,0));
              }
              else
              {
                typ++;
              }
            }
            t=n.getOffset()+n.getDuration();
            char letter='s';
            int octave=0;
            int numX=0;
            switch(n.getPitch()%12)
            {
                case 1:
                  numX=1;
                case 0:
                  letter='c';
                break;
                case 3:
                  numX=1;
                case 2:
                  letter='d';
                break;
                case 4:
                  letter='e';
                break;
                case 6:
                  numX=1;
                case 5:
                  letter='f';
                break;
                case 8:
                  numX=1;
                case 7:
                  letter='g';
                break;
                case 10:
                  numX=1;
                case 9:
                  letter='a';
                break;
                case 11:
                  letter='b';
                break;
            }
            Sign sg=data.getTonality().adapt(n).sign;
            numX=(sg==Sign.SHARP)?1:0;
            octave=n.getPitch()/12-1;
            l.add(new LilyNote(data.getTonality().generateName(n),1<<n.getType(),n.getDots(),octave));
            while(j<done.length)
            {
              if(done[j]){j++;}
              else
              {
                n=d.get(j);
                if(n.getOffset()>=t)
                {
                  done[j]=true;
                  numdone++;
                  j++;
                  t=n.getOffset()-t;
                  typ=0;
                  s=0.0f;
                  while((typ<6)&&(t-s>1.0f/32))
                  {
                    if(t-s>=1.0/(1<<typ))
                    {
                      s+=1.0/(1<<typ);
                      l.add(new LilyNote("s",1<<typ,0,0));
                    }
                    else
                    {
                      typ++;
                    }
                  }
                  t=n.getOffset()+n.getDuration();
                  numX=0;
                  switch(n.getPitch()%12)
                  {
                      case 1:
                        numX=1;
                      case 0:
                        letter='c';
                      break;
                      case 3:
                        numX=1;
                      case 2:
                        letter='d';
                      break;
                      case 4:
                        letter='e';
                      break;
                      case 6:
                        numX=1;
                      case 5:
                        letter='f';
                      break;
                      case 8:
                        numX=1;
                      case 7:
                        letter='g';
                      break;
                      case 10:
                        numX=1;
                      case 9:
                        letter='a';
                      break;
                      case 11:
                        letter='b';
                      break;
                  }
                  sg=data.getTonality().adapt(n).sign;
                  numX=(sg==Sign.SHARP)?1:0;
                  octave=n.getPitch()/12-1;
                  l.add(new LilyNote(data.getTonality().generateName(n),1<<n.getType(),n.getDots(),octave));
                }
                else
                {
                  j++;
                }
              }
            }
          }
        }
      }
    }

}

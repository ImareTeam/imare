/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.midi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import pl.umk.mat.imare.io.LilySong;
import pl.umk.mat.imare.reco.Note;
import pl.umk.mat.imare.reco.Sonst;
import pl.umk.mat.imare.reco.StaveData;

/**
 *
 * @author Maciek
 */
public class MidiPlayer {
    public static final int END_OF_TRACK_MESSAGE = 47;

    private Sequencer sequencer=null;
    private Sequence sequence=null;
    private Synthesizer synth=null;
    private MidiChannel[] channels=null;
    private Instrument[] instrument;
    //private MidiEvent prevInstrument;
    //private ArrayList<Note> srcnotes;
    private StaveData  src=null;
    private Instrument[] acInstruments=null;
    private double tempo=Sonst.DEFAULT_TEMPO;
    private LinkedList<MidiPlayerListener> listeners;
    private boolean pause = false, stop = true;
    private boolean initialized=false;
    private MetaEventListener[] listener=null;

    public boolean haveSequence()
    {
      return sequence!=null;
    }

    public double getTempo()
    {
      return tempo;
    }

    public void setTempo(double t)
    {
      tempo=t;
    }

    public void saveSequence(File f) throws IOException
    {
      if(sequence==null){return;}
      int x[]=MidiSystem.getMidiFileTypes(sequence);
      int t=0;
      if(sequence.getTracks().length>1)
      {
        for(int i=0;i<x.length;i++)
        {
          if(x[i]==1){t=1;}
        }
        if(t==0)
        {
          t=-1;
          for(int i=0;i<x.length;i++)
          {
            if(x[i]==0){t=0;}
          }
          if(t<0){t=x[0];}
        }
      }
      else
      {
        t=-1;
        for(int i=0;i<x.length;i++)
        {
          if(x[i]==0){t=0;}
        }
        if(t<0){t=x[0];}
      }
      MidiSystem.write(sequence, t, f);
    }

    public void addListener(MetaEventListener l)
    {
      if(initialized)
      {
        sequencer.addMetaEventListener(l);
      }
      int leng=0;
      if(listener!=null)
      {
        leng=listener.length;
        MetaEventListener[] tmp=new MetaEventListener[leng+1];
        System.arraycopy(listener, 0, tmp, 0, leng);
        listener=tmp;
      }
      else
      {
        listener=new MetaEventListener[1];
      }
      listener[leng]=l;
    }

    public void removeListener(MetaEventListener l)
    {
      if(initialized)
      {
        sequencer.removeMetaEventListener(l);
      }
    }

    private double ticksPerSecond(Sequence seq,float mpq)
    {
      /*
        mpq - mictoseconds per quarter
        patrz: Sequencer.getTempoInBPM
      */
      double prop=1.0;
      float divtype=seq.getDivisionType();
      if(divtype==Sequence.PPQ)
      {
        prop=mpq;
      }
      else if(divtype==Sequence.SMPTE_24)
      {
        prop=24.0;
      }
      else if(divtype==Sequence.SMPTE_25)
      {
        prop=25.0;
      }
      else if(divtype==Sequence.SMPTE_30)
      {
        prop=30.0;
      }
      else //Sequence.SMPTE_30DROP
      {
        prop=29.97;
      }
      return seq.getResolution()*prop;
    }

    public void recreate() throws InvalidMidiDataException
    {
      //createFromNotes(srcnotes,null);
        createFromNotes(src,acInstruments);
    }

    public void createFromNotes(StaveData  data) throws InvalidMidiDataException
    {
      createFromNotes(data,acInstruments);
    }

    public void createFromNotes(StaveData data,Instrument[] ins) throws InvalidMidiDataException
    {
      src=data;
      if(data==null){return;}
      tempo=data.getTempo();
      acInstruments=new Instrument[1];//data.size()];
      int[] prog=new int[1],bank=new int[1];
      if((ins!=null)||(instrument!=null))
      {
        if(ins!=null)
        {
          for(int i=0;i<1;i++)//data.size();i++)
          {
            acInstruments[i]=ins[i];
            prog[i]=acInstruments[i].getPatch().getProgram();
            bank[i]=acInstruments[i].getPatch().getBank();
          }
        }
        else
        {
          for(int i=0;i<1;i++)//data.size();i++)
          {
            acInstruments[i]=instrument[0];
            prog[i]=instrument[0].getPatch().getProgram();
            bank[i]=instrument[0].getPatch().getBank();
          }
        }
      }
      else
      {
        acInstruments=null;
        for(int i=0;i<1;i++)
        {
          prog[i]=0;
          bank[i]=0;
        }
      }
      sequence=new Sequence(Sequence.SMPTE_30,100);
      int avChannels=0,cid=0;
      Track track=null;

      double tps=ticksPerSecond(sequence,0);
      for(int j=0;j<1;j++)//data.size();j++)
      {
        if(cid>=avChannels)
        {
          track=sequence.createTrack();
          avChannels=0;
          cid=0;
        }
       ShortMessage msg=null;
       msg=new ShortMessage();

        msg.setMessage(ShortMessage.PROGRAM_CHANGE, cid,
          prog[j], bank[j]);

        MidiEvent ev=new MidiEvent(msg,0);
        track.add(ev);

        for (Note n : data.top)
          {
            long tick=(long)(tps*n.getOffset()/tempo);

            int key=n.getPitch();
            msg=new ShortMessage();

            int vol=n.getVolume();
            //if(vol>60){vol*=2;if(vol>127){vol=127;}}

            msg.setMessage(ShortMessage.NOTE_ON, cid, key, vol);
            track.add(new MidiEvent(msg,tick));

            msg=new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_OFF, cid, key, 0);

            tick=(long)(tps*(n.getOffset()+n.getDuration())/tempo);
            track.add(new MidiEvent(msg,tick));
          }
          for(Note n : data.bottom)
          {

            long tick=(long)(tps*n.getOffset()/tempo);

            int key=n.getPitch();

            msg=new ShortMessage();

            int vol=n.getVolume();
            //if(vol>60){vol*=2;if(vol>127){vol=127;}}

            msg.setMessage(ShortMessage.NOTE_ON, cid, key, vol);
            track.add(new MidiEvent(msg,tick));

            msg=new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_OFF, cid, key, 0);

            tick=(long)(tps*(n.getOffset()+n.getDuration())/tempo);
            track.add(new MidiEvent(msg,tick));
          }
        cid++;
      }
      if(initialized){sequencer.setSequence(sequence);}
      /*File f=new File("test.mid");
        try {
            saveSequence(f);
        } catch (IOException ex) {
            Logger.getLogger(MidiPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }*/
      /*LilySong lily=new LilySong();
      lily.fromNotes(data);
        try {
            lily.write("test.ly");
        } catch (IOException ex) {
            Logger.getLogger(MidiPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }
    /*public void createFromNotes(ArrayList<Note> notes,Instrument ins) throws InvalidMidiDataException
    {
      srcnotes=notes;
      //double maxval=0.0,v;
      double v;
      int i;
      Note n;

      sequence=new Sequence(Sequence.SMPTE_30,100);
      Track track=sequence.createTrack();

      double tps=ticksPerSecond(sequence,0);

      ShortMessage msg=null;
      msg=new ShortMessage();
      if(ins==null){ins=instrument[0];}
      msg.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 
        ins.getPatch().getProgram(), ins.getPatch().getBank());

      prevInstrument=new MidiEvent(msg,0);
      track.add(prevInstrument);

      for(i=0;i<notes.size();i++)
      {
        n=notes.get(i);
        //int val=(int)(n.getVolume()/maxval*127.0);
        //val=Math.max(0, Math.min(val,127));

        //long tick=(long)(tps*n.getStart());
        long tick=(long)(tps*n.getOffset()/tempo);

        //int key=12*n.getOctave()+n.getNote()+12;

        int key=n.getDots();
        msg=new ShortMessage();
        //msg.setMessage(ShortMessage.NOTE_ON, 0, key, val);
        msg.setMessage(ShortMessage.NOTE_ON, 0, key, n.getVolume());
        track.add(new MidiEvent(msg,tick));

        msg=new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_OFF, 0, key, 0);

        tick=(long)(tps*(n.getOffset()+n.getDuration())/tempo);
        track.add(new MidiEvent(msg,tick));
      }
      sequencer.setSequence(sequence);
    }*/

    public void setSequenceInstrument(int channel,Instrument ins) throws InvalidMidiDataException
    {
      /*Track[] t = sequence.getTracks();
      t[0].remove(prevInstrument);
      ShortMessage msg = new ShortMessage();
      msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, ins.getPatch().getProgram(), ins.getPatch().getBank());
      prevInstrument = new MidiEvent(msg, 0);
      t[0].add(prevInstrument);*/
      if(initialized)
      {
        acInstruments[channel]=ins;
        recreate();
      }
    }

    public void endAllNotes(int channel)
    {
      /*
        Konczy grac wszystkie
         wszystkie nuty na kanale channel
      */
      channels[channel].allNotesOff();
    }

    public void endNote(int channel,int note)
    {
      /*
        Konczy grac nute note:
         wartosci od 0 do 127, 60 - srodkowe C
         channel - kanal na ktorym odtwarza nute (patrz setInstrument)
      */
      channels[channel].noteOff(note);
    }

    public void startNote(int channel,int note,int velocity)
    {
      /*
        Zaczyna grac nute note:
         wartosci od 0 do 127, 60 - srodkowe C
         velocity - glosnosc/predkosc nuty: 0 min 127 max
         channel - kanal na ktorym odtwarza nute (patrz setInstrument)
      */
      channels[channel].noteOn(note, velocity);
    }

    public boolean setInstrumentByName(int channel,String iname)
    {
      /*
        ustawia instrument o nazwie iname na kanale channel
        przy powodzeniu zwraca true inaczej false
      */
      if(!initialized){return false;}
      Instrument ins=findInstrument(iname);
      if(ins!=null)
      {
        return setInstrument(channel,ins);
      }
      else
      {
        return false;
      }
    }

    public boolean setInstrument(int channel,Instrument instr)
    {
      /*
        Ustawia instr na kanale channel,
        jesli sie uda, zwraca true, inaczej false
      */
      boolean b=synth.loadInstrument(instr);
      if(b)
      {
        channels[channel].programChange(instr.getPatch().getBank(),instr.getPatch().getProgram());
      }
      return b;
    }

    public Instrument findInstrument(String iname)
    {
      /*
        Zwraca instrument o podanej nazwie, lub null
        jesli takiego nie ma
      */
      if(!initialized){return null;}
      int id=-1,i=0;
      while((i<instrument.length)&&(id<0))
      {
        if(iname.equals(instrument[i].getName())){id=i;}
        else{i++;}
      }
      if(id>-1)
      {
        return instrument[id];
      }
      else
      {
        return null;
      }
    }

    public Instrument[] getDefaultInstuments()
    {
      if(!initialized){return null;}
      return synth.getDefaultSoundbank().getInstruments();
    }


    public String[] listDefaultInstuments()
    {
      /*
        Wypisuje instrumenty z banku domyslnego
      */
      if(!initialized){return new String[0];}
      Instrument[] s=synth.getDefaultSoundbank().getInstruments();
      String[] str=new String[s.length];
      for(int i=0;i<s.length;i++)
      {
        str[i]=s[i].getName();
      }
      return str;
    }

    public String[] listAllInstuments()
    {
      /*
        Wypisuje wszystkie dostepne instrumenty
      */
      if(!initialized){return new String[0];}
      String[] s=new String[instrument.length];
      int i;
      for(i=0;i<instrument.length;i++)
      {
        s[i]=instrument[i].getName();
      }
      return s;
    }

    public int getNumChannels()
    {
      /*zwraca liczbe instrumentow,
       na jakiej mozna grac jednoczesnie
       zazwyczaj 16*/
      return channels.length;
    }

    public Sequencer getSequencer()
    {
      /*
        zwraca sequencer - odpowiadajacy za odtwarzanie midi
      */
      return sequencer;
    }

    public double getPositionInSeconds()
    {
      if(!initialized){return 0.0;}
      return sequencer.getMicrosecondPosition()*0.000001;
    }

    public long getPosition()
    {
      if(!initialized){return 0;}
      return sequencer.getMicrosecondPosition();
    }

    public void setPosition(long microseconds)
    {
      /*
        ustawia pozycje odtwarzania w mikrosekundach
      */
      if(initialized)
      {
        sequencer.setMicrosecondPosition(microseconds);
      }
    }

    public long getLength()
    {
      /*
        zwraca czas nagrania w mikrosekundach
      */
      if(!initialized){return 0;}
      return sequencer.getMicrosecondLength();
    }

    public void start()
    {
      pause = false;
      stop = false;

      if(!sequencer.isRunning()){
          /* wątek ustalający pozycję */
          Runnable posUpdater = new Runnable() {
              @Override
              public void run() {
                  long newPosition;
                  while(sequencer.getMicrosecondPosition()<sequencer.getMicrosecondLength() && !stop) {
                      try {
                          Thread.sleep(1);
                      } catch (InterruptedException ex) {
                          Logger.getLogger(MidiPlayer.class.getName()).log(Level.SEVERE, null, ex);
                      }

                      if(pause) continue;
                      
                      newPosition = sequencer.getMicrosecondPosition();

                      for(MidiPlayerListener l : listeners)
                          l.positionChanged(newPosition);
                  }
                  for(MidiPlayerListener l : listeners)
                          l.notifyFinished();
              }
          };
          Thread updaterThread = new Thread(posUpdater);
          updaterThread.start();
          /* koniec wątku ustalającego pozycję */
      }
      sequencer.start();
    }

    public void pause()
    {
      sequencer.stop();
      pause = true;
    }

    public void stop()
    {
        stop = true;
        if(sequencer!=null)
        {
          sequencer.stop();
          if(sequence!=null){sequencer.setTickPosition(0);}
        }
        for(MidiPlayerListener l : listeners)
            l.notifyFinished();
    }

    public void setSequence(Sequence seq) throws InvalidMidiDataException
    {
      if(initialized)
      {
        sequence=seq;
        sequencer.setSequence(sequence);
      }
    }

    public void setStream(InputStream stream) throws InvalidMidiDataException, IOException
    {
      /*
        tworzy midi ze strumienia w formacie pliku .mid
      */
      sequence=MidiSystem.getSequence(stream);
      sequencer.setSequence(sequence);
    }

    public void setFile(String filename) throws FileNotFoundException, InvalidMidiDataException, IOException
    {
      /*
        Tworzy midi z pliku .mid
      */
      FileInputStream is=new FileInputStream(filename);
      setStream(is);
      is.close();
    }

    public boolean initMidiSystem()
    {
        if(initialized){return true;}
        initialized=true;
        try {
            if(sequencer!=null){sequencer.close();}
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            synth = MidiSystem.getSynthesizer();
            //synth.open();
            //channels=synth.getChannels();
            instrument = synth.getAvailableInstruments();
            if(instrument==null)
            {
              if(sequencer!=null){sequencer.close();}
              sequencer=null;
              synth=null;

              initialized=false;
            }
            else if(instrument.length<1)
            {
              if(sequencer!=null){sequencer.close();}
              sequencer=null;
              synth=null;

              initialized=false;
              instrument=null;
            }
            else
            {
              if(listener!=null)
              {
                for(int i=0;i<listener.length;i++)
                {
                  sequencer.addMetaEventListener(listener[i]);
                }
              }
            }
            try {
                recreate();
            } catch (InvalidMidiDataException ex) {
            }
        } catch (MidiUnavailableException ex) {
            if(sequencer!=null){sequencer.close();}
            sequencer=null;
            synth=null;
//            instrument=new Instrument[0]; //< --  WTF?! za takie cos powinno sie dostac 10 lat
											//		pracy spolecznej przy sprzataniu psich kuponow...
			instrument = null;
            initialized=false;
        }
        return initialized;
    }

    public boolean getInitialized()
    {
      return initialized;
    }

    public MidiPlayer()
    {
      /*sequencer=MidiSystem.getSequencer();
      sequencer.open();
      synth = MidiSystem.getSynthesizer();
      //synth.open();
      //channels=synth.getChannels();
      instrument=synth.getAvailableInstruments();*/
      initMidiSystem();
      this.listeners = new LinkedList<MidiPlayerListener>();
    }

	public void close() {
		if(sequencer!=null){
                        stop();
			sequencer.close();
			sequencer = null;
		}
		if(synth!=null){
			//synth.close();
			synth = null;
		}
                initialized=false;
	}

      @Override
      protected void finalize()
      {
        close();
      }

      public void addMidiPlayerListener(MidiPlayerListener newListener) {
        listeners.add(newListener);
    }
}

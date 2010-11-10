/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.io;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.Obuffer;
import pl.umk.mat.imare.exception.FileDoesNotExistException;

/**
 *
 * @author Maciek
 */
public class MP3Loader extends Wave {

	protected Point countFrames(InputStream in) {
		Bitstream stream = new Bitstream(in);
		Point p = new Point();
		//Decoder decoder = new Decoder(null);
		int cnt = -1;
		float total = 0.0f;
		//WaveCountObuffer buff=new WaveCountObuffer();
		//decoder.setOutputBuffer(buff);
		try {
			Header h = stream.readFrame();
			while (h != null) {
				cnt++;
				total += h.frequency() * h.ms_per_frame();
				//decoder.decodeFrame(h, stream);
				stream.closeFrame();
				h = stream.readFrame();
			}
			/*} catch (DecoderException ex) {
			Logger.getLogger(MP3Loader.class.getName()).log(Level.SEVERE, null, ex);*/
		} catch (BitstreamException ex) {
			cnt = -1;
			Logger.getLogger(MP3Loader.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (cnt > -1) {
			cnt++;
		}
		p.x = cnt;
		//p.y=buff.leng;
		p.y = (int) (total / 1000.0f);
		return p;
	}

	@Override
	protected void load(AudioInputStream ains) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
	}

	protected void load2(InputStream ains) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
		notifyLoadingStarted();
		InputStream sourceStream = new BufferedInputStream(ains);

		int frameCount = -1;
		InputStream in2 = new BufferedInputStream(new FileInputStream(file));
		Point p;
		//if (sourceStream.markSupported()) {
		//	sourceStream.mark(-1);
		p = countFrames(in2);
		frameCount = p.x;

		//        sourceStream.reset();
		//}
		in2.close();

		WaveObuffer output = null;
		Decoder decoder = new Decoder(null);
		Bitstream stream = new Bitstream(sourceStream);

		if (frameCount == -1) {
			frameCount = Integer.MAX_VALUE;
		}

		int frame = 0;

		try {
			for (; frame < frameCount; frame++) {
				try {
					Header header = stream.readFrame();
					if (header == null) {
						break;
					}

					if (output == null) {
						int channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
						int freq = header.frequency();
						output = new WaveObuffer(channels, freq, p.y);
						decoder.setOutputBuffer(output);
					}

					Obuffer decoderOutput = decoder.decodeFrame(header, stream);

					if (decoderOutput != output) {
						throw new InternalError("Output buffers are different.");
					}

					stream.closeFrame();
				} catch (BitstreamException ex) {
					Logger.getLogger(MP3Loader.class.getName()).log(Level.SEVERE, null, ex);
				} catch (DecoderException ex) {
					Logger.getLogger(MP3Loader.class.getName()).log(Level.SEVERE, null, ex);
				}
				notifyLoadingProgress((float) (frame + 1) / (float) frameCount);
			}
		} finally {
			if (output != null) {
				output.close();
			}
		}
		sourceStream.close();
		format = output.getFormat();
		data = output.getData();
		notifyLoadingFinished();
	}
}

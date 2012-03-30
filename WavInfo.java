

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
/*
 * WavInfo.java
 *
 * Created on April 22, 2008, 12:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author ali
 */
public class WavInfo {
    String filename;
    AudioFormat audioformat;
    long length_in_frames;
    float frameRate;
    int frameSize;
    int channels;
    int sampleRate;
    int sampleSize;
    boolean big_endian;

    /** Creates a new instance of WavInfo */
    public WavInfo(String filename) {
        this.filename = filename;
        int result =  start();

    }

    public int start(){
        File f = new File(filename);
        AudioInputStream audioinput;
      //  BufferedInputStream bufread;
         try{
            audioinput = AudioSystem.getAudioInputStream(f);
            //Print Some information about this file on the screen
            audioformat = audioinput.getFormat();
            length_in_frames = audioinput.getFrameLength();
            channels = audioformat.getChannels();
            frameRate = audioformat.getFrameRate();
            frameSize = audioformat.getFrameSize();
            sampleRate = (int)audioformat.getSampleRate();
            sampleSize = audioformat.getSampleSizeInBits();
            big_endian = audioformat.isBigEndian();
            audioinput.close();
            return 0;
         //   bufread.close();
    }catch(IOException e){
        return 1;
    }catch(UnsupportedAudioFileException e){
        return 2;
    }
    }

    public AudioFormat get_Audioformat(){
        return audioformat;

    }

    public long get_length_in_frames(){
        return length_in_frames;
    }

    public float get_frameRate(){
        return frameRate;
    }

    public int get_frameSize(){
        return frameSize;
    }

    public int get_channels(){
        return channels;
    }

    public int get_sampleRate(){
        return sampleRate;
    }

    public String get_dataFormat(){
        if(big_endian)
            return "Big Endian";
        else
            return "Little Endian";
    }

    public double get_recordingSizeinBytes(){
        return (length_in_frames * frameSize);
    }


}

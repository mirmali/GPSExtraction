/*
 * twoChannelWAV.java
 *
 * Created on May 6, 2008, 2:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author ali
 */
public class twoChannelWAV {
    String wav_file_path;
    String left_channel_file;
    String right_channel_file;
    int maxValueL=0;
    int minValueL=0;

    int maxValueR=0;
    int minValueR=0;

    int sampleMaxL=0;
    int sampleMinL=0;

    int sampleMaxR=0;
    int sampleMinR=0;

    int sampling_rate;

    /** Creates a new instance of twoChannelWAV */
    public twoChannelWAV() {
    }

    public twoChannelWAV(String wav_file_path){
        this.wav_file_path = wav_file_path;
    }

    public boolean convertToTxt(short SIZE) {

        // WAV FILE ATTRIBUTE
        AudioInputStream audioinput;
        AudioFormat audioformat;
        long length_in_frames;
        float frameRate;
        int frameSize;
        int channels;
        float sampleRate;
        int sampleSize;
        boolean big_endian;
        System.out.println(wav_file_path);

        File wavfile = new File(wav_file_path);
        BufferedInputStream bufread;

        BufferedWriter wrL,wrR; // left channel file and right channel file;
        int max_mag_in_recording=0;   // This is the place holder for the maximum magnitude in the WAV file (Used for normalizing)
        byte[] buffer  = new byte[SIZE];
        ArrayList write_list = new ArrayList();
        Integer[] write_buffer;
        int sampleCount=0;
        boolean write_buffer_full=false;
        int num_bytes_read=0;
        int valuecount=0;
        try{
            audioinput = AudioSystem.getAudioInputStream(wavfile);
            bufread = new BufferedInputStream(audioinput);
            String[] tempstr = wav_file_path.split(".wav");
            left_channel_file = tempstr[0] + "LEFT.txt";
            right_channel_file = tempstr[0] + "RIGHT.txt";
            wrL = new BufferedWriter(new FileWriter(left_channel_file));
            wrR = new BufferedWriter(new FileWriter(right_channel_file));

            audioformat = audioinput.getFormat();
            length_in_frames = audioinput.getFrameLength();
            channels = audioformat.getChannels();
            frameRate = audioformat.getFrameRate();
            frameSize = audioformat.getFrameSize();
            sampleRate = audioformat.getSampleRate();
            sampling_rate = (int)sampleRate;
            sampleSize = audioformat.getSampleSizeInBits();
            big_endian = audioformat.isBigEndian();

            bufread = new BufferedInputStream(audioinput,SIZE);

            // THIS BYTE ARRAY WILL STORE THE FRAME
            short sampleSizeinBytes = (short)(sampleSize / 8);
            int[] frame = new int[sampleSizeinBytes];
            int frame_low = 0;
            int frame_high = sampleSizeinBytes - 1;


            /*
                frame[highest] + frame[highest-1] + .... + frame[0]
             */
            int tmp;
            int tempint = 0;
            short buffer_read_count=0;
            boolean left_overs=false;
            ArrayList left_over_array = new ArrayList();
            short requires_more=0;
            byte[] left_over_byte_array=new byte[SIZE];
            boolean number_negative=false;
            boolean write_to_file=false;
            int temp_left=0;
            int temp_byte_to_int=0;

            boolean right_channel=false;
            int negative_number_shift=0;
            for(int i=sampleSizeinBytes;i<4;i++){
                negative_number_shift = negative_number_shift | 0xFF << (8*i);

            }

            while((num_bytes_read = bufread.read(buffer))!=-1){
                buffer_read_count = -1;
                if(left_overs){
                    requires_more = (short) (sampleSizeinBytes - left_over_array.size());
                    if(num_bytes_read>=requires_more){
                        for(short k=1;k<=requires_more;k++){
                            buffer_read_count = (short) (buffer_read_count + 1);
                            left_over_array.add(buffer[buffer_read_count]);
                        }
                    }
                    left_overs=false;
                    Byte[] t=new Byte[left_over_array.size()];
                    left_over_array.toArray(t);
                    temp_byte_to_int=0;
                    tempint=0;
                    temp_left=0;
                    for(int i=0;i<left_over_array.size();i++){
                        if(!( (t[i])>=0 && (t[i]<=127) )){
                            if(i==left_over_array.size()-1)
                                number_negative=true;
                            temp_byte_to_int = 256+t[i];
                        }else
                            temp_byte_to_int = t[i];
                        temp_left = temp_left + (temp_byte_to_int << (8*(i)));
                    }
                    if(number_negative){
                        tempint = (negative_number_shift) | temp_left;
                        number_negative=false;
                    }
                    else
                        tempint = temp_left;
                    write_list.add(tempint);
                    sampleCount +=1;


                    tempint = temp_left = temp_byte_to_int = 0;

                    left_over_array.clear();
                }
                while((buffer_read_count) <   num_bytes_read-1){
                    if(((num_bytes_read-1) - (buffer_read_count)) >= sampleSizeinBytes){

                       for(short i=1;i<=sampleSizeinBytes;i++){
                           buffer_read_count = (short)(buffer_read_count + 1);
                            frame[i-1]=buffer[buffer_read_count];
                        }
                       tempint = temp_left = temp_byte_to_int = 0;



                       for(int m=0;m<frame.length;m++){
                           if(!( (frame[m])>=0 && (frame[m]<=127) )){
                            if(m==frame.length-1)
                                number_negative=true;
                            temp_byte_to_int = 256+frame[m];
                        }else
                            temp_byte_to_int = frame[m];
                        temp_left = temp_left + (temp_byte_to_int << (8*(m)));
                    }
                    if(number_negative){
                        tempint = (negative_number_shift) | temp_left;
                        number_negative=false;
                    }
                    else
                        tempint = temp_left;
                       sampleCount +=1;
                    write_list.add(tempint);

                    tempint = temp_left = temp_byte_to_int = 0;
                       }

                    else{
                        // save the left over bytes which are lesser in number than frame size in a tempbuffer
                        // and turn the flag on. If flag==true then process that first and then process other.
                        left_over_array.clear();
                        short left_over_array_count = (short)(num_bytes_read - 1 - buffer_read_count);
                        for(short l=1;l<=left_over_array_count;l++){
                            buffer_read_count = (short)(buffer_read_count + 1);
                            left_over_array.add(buffer[buffer_read_count]);
                        }
                        left_overs=true;

                    }
                }
                write_to_file=true;
                if(write_to_file){

                    // store all the number in the write_buffer to the file
                    write_buffer = new Integer[write_list.size()];
                    write_list.toArray(write_buffer);
                    for(int w=0;w<write_list.size();w++){
                        if(!right_channel){
                            int storeL = write_buffer[w].intValue();
                            valuecount = valuecount + 1;

                            if((storeL > 0) && (maxValueL < storeL)){
                                maxValueL = storeL;
                                sampleMaxL = valuecount;
                            }
                            if((storeL < 0)&&(minValueL > storeL)){
                                sampleMinL = valuecount;
                                minValueL = storeL;
                            }
                            wrL.write(String.valueOf(storeL));
                            wrL.newLine();
                            right_channel = true;
                        }else{
                            valuecount = valuecount + 1;
                            int storeR = write_buffer[w].intValue();
                            if((storeR > 0) && (maxValueR < storeR)){
                                sampleMaxR = valuecount;
                                maxValueR = storeR;
                            }
                            if((storeR < 0)&&(minValueR > storeR)){
                                sampleMinR = valuecount;
                                minValueR = storeR;
                            }
                            wrR.write(String.valueOf(storeR));
                            wrR.newLine();
                            right_channel = false;
                        }

                    }
                    write_list.clear();
                }

            }
            wrR.close();
            wrL.close();
            bufread.close();
            System.out.println(sampleMaxL + "\n" + sampleMaxR+ "\n" + sampleMinL+ "\n" + sampleMinR);

    }catch(IOException e){
        System.err.println(e);
    }catch(UnsupportedAudioFileException f){
        System.err.println(f);
    }

        return true;
    }


}

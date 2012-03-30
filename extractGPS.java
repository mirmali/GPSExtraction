

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
/*
 * extractGPS.java
 *
 * Created on April 10, 2008, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author ali
 */

/**
 *  extract GPS class is used to decipher the GPS data recorded by the GPS logger with ability
 * to output the PPS pulse along with the NMEA data. The input to the class is the following in
 * the same order
 * a) The filename
 * b) the sampling rate
 * c) the baud
 * d) PPS_Width
 * e) the length of PPS to be checked for assurance
 */
public class extractGPS {
  //  String filename;
    int sampleRate;
    int baud;
    float PPS_Width;    // PPS width in millisecond
    float pps_check_percent;
    float PPS_Height;
    float PPS_Average_height;
    float NMEA_High_Low;
    float Signal_Max;
   // String result_file;


    /** Creates a new instance of extractGPS */
    public extractGPS() {

    }


    public extractGPS(int baud,int sampleRate,float PPS_Width,float pps_check_percent,float PPS_Height,float PPS_Average_height,float NMEA_High_low,float Signal_Max) {
        //this.filename = filename;
        this.baud = baud;
        this.sampleRate = sampleRate;
        this.PPS_Width = PPS_Width;
        this.pps_check_percent = pps_check_percent;
        this.NMEA_High_Low = NMEA_High_low;
        this.PPS_Height = PPS_Height;
        this.PPS_Average_height = PPS_Average_height;
        this.Signal_Max = Signal_Max;
      //  this.result_file = result_file;
    }

    /**
     * The method to extract the GPS data from the NMEA stream.
     */

    /**
             *      The GPS log data contains a PPS pulse every one second.
             *  Each of these PPS pulse is followed by a string of characters
             *  representing the NMEA sentences.
             *
             *  We look for the first PPS pulse in the data. This pulse has
             *  a lot of leading zeros which can be used to exactly find the
             *  samples representing the pulse. The pulse is 20ms wide.
             */

    public void extract(String filename,String result_file,boolean normalized){

        int symbol_size_in_samples = (int)Math.floor(sampleRate / baud);
        int pps_width_in_samples = (int)Math.floor(PPS_Width * sampleRate);
        int pps_scan_number = (int)Math.floor(pps_check_percent * pps_width_in_samples);
        int count = 0;

        // The input file instance
        FileInputStream fis;
        DataInputStream dis;
        BufferedReader buf;

        int sample_track=0;     // Keeps a track of the current sample
        float height=0;         // the amplitude of the sample

        float previous_height=0;    // the previous sample's amplitude
        float prev_prev_height=0;   // the amplitude of the sample two sample times before
        float prev_prev_prev_height=0;// the amplitude of the samplet three sample times before


        int pps_track=0;    // Keeps a track of the PPS pulse's width
        int previous_pps=0; // the sample number of start of the previous PPS pulse
        int current_pps=0;  // Current PPS pulse's beginning sample


        boolean is_this_first_pps = true;
        boolean pps_pulse_found = false;
        boolean pps_check_done = false;
        boolean count_zero_over_start_NMEA_detect=false;

        int bit_one_count = 0; // keeps track of number of consecutive LOGIC HIGH samples
        int bit_zero_count = 0; // keeps track of number of consecutive LOGIC LOW samples
        boolean transition_to_zero=false;   // TRUE when a transition from HIGH to LOW occurs
        boolean transition_to_one=false;    // TRUE when a transition from LOW to HIGH occurs
        int skip=0; // used to skip a few samples after the detection of the first

        try{
            fis = new FileInputStream(filename); // Input filename
            dis = new DataInputStream(fis);
            buf = new BufferedReader(new InputStreamReader(dis));



            int pps_found=0;  // holder for the PPS start sample
            String str;
            int stack_size = (1000 + pps_scan_number); // the Queue length.

            Queue queue_buf = new Queue(); // Queue used to store 1000 + pps_scan_number previous samples
            ArrayList ar = new ArrayList();// ArrayList to store the decoded bits
            boolean pps_check=false;

           // System.out.println()

            while((str=buf.readLine())!=null){
                sample_track = sample_track +  1;
               // System.out.println(str);
                if(sample_track==1){
                    prev_prev_prev_height = prev_prev_height = previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==2){
                    prev_prev_height = previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==3){
                    previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==4){
                    height = Float.parseFloat(str);
                }
                if(sample_track>4){
                    prev_prev_prev_height = prev_prev_height;
                    prev_prev_height = previous_height;
                    previous_height = height;
                    height = Float.parseFloat(str);
                }
                if(queue_buf.size() < stack_size)
                    queue_buf.push(height);
                else{
                    queue_buf.pop();
                    queue_buf.push(height);
                }

                // read a few samples after detecting end of the PPS_Pulse to enter the Zero field after it.
                // This prevents errors.
            //    System.out.println("Debug   --------- > "+(prev_prev_prev_height - height));
                if(skip<5 && count_zero_over_start_NMEA_detect==true){
                    //System.out.println("Inside skip a few samples");
                    skip=skip + 1;
                    continue;
                }

                // Enter this 'if' statement when the zeros after pulse detection are read.
                if(count_zero_over_start_NMEA_detect){
                    //System.out.println("inside count_zero_over");
                 // Check for transition from LOW to HIGH
                    if(prev_prev_prev_height - height < -NMEA_High_Low){
                        //System.out.println("Inisde height diff < -NMEA_H_L");
                    //    System.out.println("ONE");
                        bit_one_count = bit_one_count + 1; // Increment the LOGIC HIGH tracking holder.
                        transition_to_one = true;   // Transtion to HIGH is true
                        if(transition_to_zero){ // Enter loop when we were in LOGIC LOW field previously.
                            //System.out.println("inside transition to zero");
                            transition_to_zero=false;   // LOGIC LOW state is now false.
                           //System.out.println("ZERO");

                           int rem_x;
                           // number of samples either more or lesser than required for bit_zero_count to be a multiple
                           // of symbol_size_in_samples.
                           // Adjust the bit_zero_count to be a multiple of symbol_size_in_samples
                           rem_x = bit_zero_count % symbol_size_in_samples;
                           if(rem_x < symbol_size_in_samples / 2)
                               bit_zero_count = bit_zero_count - rem_x;
                           else
                               bit_zero_count = bit_zero_count + (symbol_size_in_samples - rem_x);
                           //System.out.println("ZERO   " + bit_zero_count);

                            double zx = bit_zero_count / symbol_size_in_samples; // Add these number of symbols to arraylist
                            for(int i=0;i<zx;i++){
                                ar.add("1"); // changed
                            }
                            bit_zero_count = 0;
                        }
                        continue;
                        }

                    // Enter this when LOW to HIGH transition occurs
                    if(prev_prev_prev_height - height > NMEA_High_Low){
                        //System.out.println("inside height diff > NMEA_h_l");
                        // Increment bit_zero_count . LOGIC LOW holder
                        bit_zero_count = bit_zero_count + 1;
                        transition_to_zero = true;
                        transition_to_one = false;
                        if(bit_one_count >0){
                            //System.out.println("inside transition to one ==false");
                            double ox;
                            int rem = bit_one_count % symbol_size_in_samples;
                            if(rem < symbol_size_in_samples /2)
                                bit_one_count = bit_one_count - rem;
                            else
                                bit_one_count = bit_one_count + (symbol_size_in_samples - rem);
                            //System.out.println("ONE   " + bit_one_count);

                        ox = bit_one_count / symbol_size_in_samples;
                            for(int i=0;i<ox;i++){
                                ar.add("0"); // changed
                            }



                        }
                        bit_one_count = 0;
                        continue;
                    }
                    if(transition_to_one){
                        //System.out.println("inside transition to one add");
                     //   System.out.println("ONE");
                        bit_one_count = bit_one_count + 1;
                        continue;
                    }

                    if(transition_to_zero){
                        //System.out.println("inside transition to zero add");
                       // System.out.println("ZERO");
                        bit_zero_count = bit_zero_count + 1;
                      //  System.out.println(bit_zero_count);
                        if(bit_zero_count > 500 ){ // when bit_zero_count becomes ridiculous it means end of NMEA is reached
                                                   // Store the NMEA detected and start another PPS detection or exit when
                                                    // end of file is reached.
                            //System.out.println("bit count > 500");

                            count = count + 1; // used to store the NMEA data in different files. Used in filename as file_count.txt
                            /*
                             *   THE NMEA DATA IS READ. STORE IT AND RESET ALL THE VARIABLES BEFORE STARTING ANOTHER
                             * DETECTION PROCESS.
                             */

                            transition_to_one=false;
                            transition_to_zero=false;
                            bit_zero_count=0;
                            bit_one_count=0;
                            count_zero_over_start_NMEA_detect=false;
                            skip=0;
                            String[] st_list = null;
                            /*
                             *  WE ADD ANOTHER ONE TO THE ARRAYLIST AS WE SKIP ADDING A ZERO AT THE END OF NMEA DATA
                             * AS WE ASSUME IT AS A PART OF THE END OF ZERO STREAM. THE LAST CHARACTER IN THE NMEA STREAM
                             * IS A LOGIC ZERO (EVERY BYTE ENDS WITH ZERO AND THE LAST CHARACTER IS <LF> i.e. LINEFEED)
                             * TO CORRECT THE DETECTED NMEA WE MUST ADD ANOTHER ZERO AT THE END.
                             */

                            ar.add("1"); // changed
                            st_list = new String[ar.size()]; // store the NMEA in a string
                            ar.toArray(st_list);
                            ar.clear(); // reset the ARRAYLIST
                            processNMEA(st_list,count,result_file);       // process the NMEA string
                            //System.out.println(sample_track);
                            //System.out.println("reached");
                           // System.exit(0);
                            continue;

                        }
                        else{
                            continue;
                        }
                    }
                 }

                if(pps_check_done){
                    //System.out.println("pps_check_done");
                    float h = prev_prev_height - height;
                   // System.out.println(h);
                    if(h < PPS_Height){
                        //System.out.println("h < pps height continue");
                        continue;
                    }
                    if(h >= PPS_Height){
                        //System.out.println("h >= pps height, count zero over start nmea detect = true");
                        // end of pulse reached. now count zeros or look for next pulse
                        //System.out.println("end -- >  " + sample_track);
                        count_zero_over_start_NMEA_detect = true;
                        skip=0;
                        pps_check_done=false;
                        continue;
                        }
                    }


                // Check the samples for amp > 0.4. If yes then increment pps_found until u make sure that it indeed is PPS
                if(height>PPS_Average_height)
                    pps_found = pps_found + 1;
                else
                    pps_found = 0;
                if(pps_found == pps_scan_number){


                        //System.out.println("pps found = scan number");
                    /** check if this really is the right sample for pulse
                     *  a) first adjust the sample for the PPS
                     *  b) check for leading zeros
                     *  c) check the number of samples between current and previous PPS (should be 48000 == one second)
                     */
                    pps_check = (check_if_true(queue_buf,stack_size,normalized) & check_distance(pps_found,current_pps));
                  //  System.out.println(pps_check);
                    if(pps_check==true){
                        //System.out.println("pps check is true");
//                        System.exit(0);
                        if(is_this_first_pps == false){
                            previous_pps = current_pps;
                            current_pps = sample_track - (pps_scan_number-1);

                        }
                        if(is_this_first_pps == true){
                            previous_pps = current_pps = sample_track - (pps_scan_number-1);
                            is_this_first_pps = false;
                        }
                        pps_check_done = true;
                       //System.out.println(current_pps);
                    }

                    if(pps_check == false){
                        pps_found = 0;
                    }


                }



            }
            buf.close();

        }catch(IOException e){

        }
    }

    public boolean check_if_true(Queue local,int local_size,boolean norm){
        Queue temp = new Queue();
        temp = local;
        int count=0;
        float tmp;
        Float obj;
        float signalmax;
        if(norm)
            signalmax = 1;
        else
            signalmax = Signal_Max;
        for(int i=1;i<=500;i++){
            obj = (Float) temp.pop();
            tmp = obj.floatValue();
            //System.out.println(tmp);

            if(tmp<0.3*signalmax)
                count = count + 1;
            else
                count = 0;
        }
       // System.out.println(count);
        if(count>=500){
            //System.out.println("count greater than 500 return true");
            return true;
        }
        else{
            //System.out.println("count greater than 500 return true");
            return false;
        }
    }

    public boolean check_distance(int pps,int current_pps){
        if((pps-current_pps)>=47990 || (pps-current_pps)<=48010)
            return true;
        else
            return false;
    }


    public void processNMEA(String[] nmeastring, int i,String nmeafile){
        System.out.println(nmeastring.length);
        String[] nmea = new String[10];
        String[] temp = new String[8];
        int j=0,k=0,z=0,c=0;
        int byte_rep=0;
        BufferedWriter w;
        boolean look_for_byte=true;
        int count=0;
        boolean startstoring=false;
        try{
            w = new BufferedWriter(new FileWriter(nmeafile,true)); // file to store the deciphered NMEA
            while(count < nmeastring.length){
                if(look_for_byte){
                    if(nmeastring[count]=="1"){
                        count=count+1;
                        continue;
                    }else{
                        look_for_byte=false;
                        count=count+1;
                        byte_rep=0;
                        k=0;
                        while(count!=nmeastring.length){
                            byte_rep = (int)(byte_rep + Math.pow(2.0,(double)(k))*Integer.parseInt(nmeastring[count]));
                            k=k+1;
                            count=count+1;
                            if(k==8){
                                count=count+1;
                                break;
                            }
                        }
                        w.write((char)(byte_rep));
                        look_for_byte=true;
                    }

                }
            }
            w.close();
        }
        catch(IOException e){
            System.err.println("error while writing NMEA data");
        }
    }




    }

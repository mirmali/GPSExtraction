import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
/*
 * extract_GPS_Data.java
 *
 * Created on April 21, 2008, 7:09 PM
 */

/**
 *
 * @author  mali1
 */
public class extract_GPS_Data extends javax.swing.JFrame {
    JFileChooser choose_file;
    File gps_wav_file;
    String file_path;
    String saved_text_file_path;
    String file_to_extract_data_from;
    
    JFileChooser new_normalized_gps_file;
    File new_gps_file;
    String new_gps_file_path_normalized;
    
    boolean same_as_previous;
    boolean use_normalized=false;
    float sampling_rate;
    String NMEA_file;
    double suggested_NMEA_transition;
    double suggested_PPS_height;
    double suggested_PPS_endHeight;
    double suggested_PPS_average;
    double nmea_start;
    double nmea_end;
    //double suggested_
    
    double sampleMax;
    double sampleMin;
    int signal_baud;
    boolean two_channel=false;
    boolean signal_channel_left=false;
    boolean use_suggested_values = true;
    double pps_trans=0;
    double nmea_trans=0;
    double pps_lower_end=0;
    
    boolean normalized_file_created = false;
    String cur_dir;
    
    
    
    
    
    
    
    /** Creates new form extract_GPS_Data */
    public extract_GPS_Data() {
        initComponents();
        String cur_dir = System.getProperty("user.dir");
      
      Pattern p = Pattern.compile("\\\\");
       Matcher m = p.matcher(cur_dir); 
       cur_dir =  m.replaceAll("/");
       cur_dir = "jar:file://" + cur_dir + "/GPSExtraction.jar!/t.jpg";
        
        
        jRadioButton5.setEnabled(false);
        jRadioButton6.setEnabled(false);
        jTextArea2.setEnabled(false);
        jLabel6.setEnabled(false);
        jRadioButton3.setEnabled(false);
        jRadioButton4.setEnabled(false);
        jTextArea1.setEnabled(false);
        jLabel4.setEnabled(false);
        jLabel2.setEnabled(false);
        jLabel3.setEnabled(false);
        jLabel20.setEnabled(false);
        jButton4.setEnabled(false);
        jButton2.setEnabled(false);
        jButton3.setEnabled(false);
        jTextArea2.setEnabled(false);
        jRadioButton5.setEnabled(false);
        jRadioButton6.setEnabled(false);
        jLabel9.setEnabled(false);
        jLabel12.setEnabled(false);
        jLabel11.setEnabled(false);
        jLabel31.setEnabled(false);
        jLabel21.setEnabled(false);
        jTextField2.setEnabled(false);
        jTextField4.setEnabled(false);
        jTextField10.setEnabled(false);
        jButton5.setEnabled(false);
        jLabel32.setEnabled(false);
        jLabel10.setEnabled(false);
        jLabel35.setEnabled(false);
        jLabel33.setEnabled(false);
        jLabel36.setEnabled(false);
        jLabel14.setEnabled(false);
        jLabel13.setEnabled(false);
        jTextField3.setEnabled(false);
        jTextField6.setEnabled(false);
        jTextField5.setEnabled(false);
        jLabel22.setEnabled(false);
        jLabel15.setEnabled(false);
        jLabel16.setEnabled(false);
        jLabel17.setEnabled(false);
        jRadioButton7.setEnabled(false);
        jRadioButton8.setEnabled(false);
        jLabel34.setEnabled(false);
        jLabel37.setEnabled(false);
        jButton6.setEnabled(false);
        
        // jTextArea2.setEditable(false);
    }
    
    public void suggestValues(){
        FileInputStream f;
        BufferedReader b;
        DataInputStream d;
        int sample_track=0;
        
        float prev_prev_prev_prev_height=0;
        float prev_prev_prev_height=0;
        float prev_prev_height=0;
        float previous_height=0;
        float height=0;
        
        String str;
        boolean sug_pps_found=false;
        boolean sug_pps_end=false;
        boolean adjust_height_if_needed=true;
        boolean PPS_done=false;
        boolean nmea=false;
        boolean adjust_nmea=false;
        
        int nmea_track_start=0;
        int nmea_track_end=0;
        int pps_start=0;
        int pps_end=0;
        int pps_width_in_samples = (int)(sampling_rate * Float.parseFloat(jTextField5.getText()));
        try{
            f = new FileInputStream(saved_text_file_path);
            d = new DataInputStream(f);
            b = new BufferedReader(new InputStreamReader(d));
            for(int i=0;i<40000;i++){
                b.readLine();
            }
            while((str=b.readLine())!=null){
                sample_track = sample_track +  1;
                // System.out.println(str);
                if(sample_track==1){
                    prev_prev_prev_prev_height = prev_prev_prev_height = prev_prev_height = previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==2){
                    prev_prev_prev_height = prev_prev_height = previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==3){
                    prev_prev_height = previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==4){
                    previous_height = height = Float.parseFloat(str);
                }
                if(sample_track==5){
                    height = Float.parseFloat(str);
                }
                if(sample_track>5){
                    prev_prev_prev_prev_height = prev_prev_prev_height;
                    prev_prev_prev_height = prev_prev_height;
                    prev_prev_height = previous_height;
                    previous_height = height;
                    height = Float.parseFloat(str);
                }
                
                if(adjust_nmea){
                    if(height>nmea_start)
                        nmea_start=height;
                    System.out.println(nmea_start  +"   " + nmea_end);
                    break;
                    
                }
                
                
                if(PPS_done){
                    if(height-prev_prev_prev_height >= 0.5*sampleMax){
                        nmea = true;
                        nmea_start=height;
                        nmea_track_start=sample_track;
                        adjust_nmea=true;
                        //       System.out.println(sample_track);
                        if(prev_prev_height < prev_prev_prev_height)
                            nmea_end = prev_prev_height;
                        else
                            nmea_end = prev_prev_prev_height;
                        continue;
                    }
                    continue;
                }
                
                if(adjust_height_if_needed){
                    if(height > suggested_PPS_height)
                        suggested_PPS_height = height;
                    adjust_height_if_needed=false;
                }
                
                
                if(sug_pps_found && (height - prev_prev_prev_height) <= -0.6*sampleMax){
                    // this could be the end of pps. Check for width
                    pps_end=sample_track;
                    if((pps_end-pps_start)>= 0.7*pps_width_in_samples){
                        suggested_PPS_endHeight = prev_prev_prev_prev_height;
                        PPS_done=true;
                        continue;
                    }else{
                        sug_pps_found=false;
                        continue;
                    }
                }
                if(sug_pps_found && (height - prev_prev_prev_height)>=-0.6*sampleMax){
                    continue;
                }
                
                if((height - prev_prev_prev_height)>=0.6*sampleMax){
                    sug_pps_found = true;
                    pps_trans = height - prev_prev_prev_height;
                    suggested_PPS_height = height;
                    //suggested_PPS_height_0 = previous_height;
                    pps_start = sample_track;
                    adjust_height_if_needed=true;
                }
                
            }
            
            
        }catch(IOException e){
            
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel20 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel21 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Program to extract GPS data from recorded WAV signal");
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CONVERT .WAV TO .TXT");

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 10));

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton1.setText("Select WAV file");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton2.setText("Convert to TXT");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel2.setText("Press to convert WAV to TXT");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel3.setText("Press to Normalize the TXT data");

        jButton3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton3.setText("Normalize");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel4.setText("Press to display WAV file information");

        jButton4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton4.setText("WAV Info");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        buttonGroup3.add(jRadioButton5);
        jRadioButton5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton5.setText("Left");
        jRadioButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton5MouseClicked(evt);
            }
        });
        jRadioButton5.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButton5StateChanged(evt);
            }
        });

        buttonGroup3.add(jRadioButton6);
        jRadioButton6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton6.setText("Right");
        jRadioButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton6MouseClicked(evt);
            }
        });

        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Monospaced", 0, 10));
        jTextArea2.setForeground(new java.awt.Color(0, 51, 255));
        jTextArea2.setRows(5);
        jTextArea2.setText("Choose the channel that contains the GPS\ndata when \"WAV\" is a 2-channel recording\nChoosing the right channel helps in \nsuggesting values for \"PARAMETERS\" below.\nNo matter which channel is selected the\n\"WAV\" is converted to separate \"TXT\"\nfiles for both the channels.\nFilename Format:\nLeft channel -> wavfilename_LEFT.txt\nRight channle -> wavfilename_RIGHT.txt\n\n");
        jScrollPane2.setViewportView(jTextArea2);

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel20.setText("Normalized file is saved as \"wavefilename_normalized.txt\"");

        jLabel23.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel23.setText("INFORMATION BOX");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(258, 258, 258)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 504, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(170, 170, 170)))
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(16, 16, 16)
                                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton5))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton6)
                        .addGap(33, 33, 33))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4)
                                    .addComponent(jButton4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jRadioButton5)
                                        .addComponent(jRadioButton6))
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(17, 17, 17)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(jButton3))
                                .addGap(11, 11, 11)
                                .addComponent(jLabel20))
                            .addComponent(jScrollPane1))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(219, 219, 219))))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton1, jButton2, jButton3, jButton4});

        jPanel2.setAutoscrolls(true);
        jLabel5.setText("EXTRACT GPS INFORMATION FROM THE GPS SIGNAL");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel7.setForeground(new java.awt.Color(153, 51, 0));
        jLabel7.setText("SET THE FOLLOWING PARAMETERS BASED ON THE SIGNAL OBSERVED. THE PARAMETER VALUES SUGGESTED ABOVE CAN BE USED IF THE SIGNAL IS NOT KNOWN.");

        jTextField2.setFont(new java.awt.Font("Tahoma", 0, 10));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton1.setText("Yes");
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton1MouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel8.setForeground(new java.awt.Color(0, 153, 153));
        jLabel8.setText("STEP 1          A)  Is the GPS TXT file same as the TXT file generated for the WAV file above ?");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton2.setText("No");
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton2MouseClicked(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel9.setForeground(new java.awt.Color(51, 51, 255));
        jLabel9.setText("STEP 2          A) If the GPS TXT file is different than the one generated above, select the file");

        jButton5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton5.setText("Select TXT file");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel10.setForeground(new java.awt.Color(153, 51, 0));
        jLabel10.setText("Baud Rate");

        jTextField3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField3.setText("4800");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel11.setForeground(new java.awt.Color(51, 51, 255));
        jLabel11.setText("Sampling Rate");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel12.setForeground(new java.awt.Color(51, 51, 255));
        jLabel12.setText(" B) User should input these values for the GPS TXTfile chosen in STEP 2-A");

        jTextField4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jTextField5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField5.setText("0.02");
        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel14.setForeground(new java.awt.Color(204, 0, 204));
        jLabel14.setText("The width of PPS  in % ( 0 - 1 ) to be checked to assure its a valid PPS");

        jTextField6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField6.setText("0.5");

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel15.setForeground(new java.awt.Color(0, 51, 51));
        jLabel15.setText(" B)  The change in Amplitude of PPS Pulse when it makes a transition from HIGH to LOW (PPS_transition) = B2 - B1");

        jTextField7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField7.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel16.setForeground(new java.awt.Color(0, 51, 51));
        jLabel16.setText(" C) PPS Lower end Height = C");

        jTextField8.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField8.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel17.setForeground(new java.awt.Color(0, 51, 51));
        jLabel17.setText(" D) The change in Amplitude of NMEA pulse when it makes a transition from HIGH to LOW = D2 - D1");

        jTextField9.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField9.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jButton6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton6.setText("Extract GPS");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton7.setText("Quit");
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton7MouseClicked(evt);
            }
        });

        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton3.setText("Yes");
        jRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton3MouseClicked(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel6.setForeground(new java.awt.Color(0, 153, 153));
        jLabel6.setText(" B)   Use the Normalized file for Data Extraction ?");

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton4.setText("No");
        jRadioButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton4MouseClicked(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel21.setForeground(new java.awt.Color(51, 51, 255));
        jLabel21.setText("Max Amplitude of the signal");

        jTextField10.setFont(new java.awt.Font("Tahoma", 0, 10));
        jTextField10.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel22.setForeground(new java.awt.Color(0, 51, 51));
        jLabel22.setText(" A) Check \"Yes\"  to use the values suggested above or \"No\" to enter new values");

        buttonGroup4.add(jRadioButton7);
        jRadioButton7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton7.setText("Yes");
        jRadioButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton7MouseClicked(evt);
            }
        });

        buttonGroup4.add(jRadioButton8);
        jRadioButton8.setFont(new java.awt.Font("Tahoma", 0, 10));
        jRadioButton8.setText("No");
        jRadioButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jRadioButton8MouseClicked(evt);
            }
        });

        jLabel18.setIcon(new javax.swing.ImageIcon("t.jpg"));

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel19.setText("B1 - PPS_Height_start");

        jLabel24.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel24.setText("B2 - PPS_Height_end");

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel25.setText("D1 - NMEA_start");

        jLabel26.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel26.setText("D2 - NMEA_end");

        jLabel27.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel27.setText("B1,B2,C,D1,D2 represent the amplitudes");

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel28.setText("of the samples at respective times");

        jLabel29.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel29.setForeground(new java.awt.Color(204, 0, 102));
        jLabel29.setText("( IF 'NO' SKIP TO STEP 2)");

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel30.setForeground(new java.awt.Color(204, 0, 102));
        jLabel30.setText("( SKIP TO STEP 3 NOW )");

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel31.setForeground(new java.awt.Color(51, 51, 255));
        jLabel31.setText("Hz");

        jLabel32.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel32.setForeground(new java.awt.Color(153, 51, 0));
        jLabel32.setText("STEP 3          Set the Baud Rate for the recorded GPS signal");

        jLabel33.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel33.setForeground(new java.awt.Color(255, 0, 0));
        jLabel33.setText("STEP 4         Set the PPS Signal Width in seconds for the recorded signal");

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel34.setForeground(new java.awt.Color(204, 0, 204));
        jLabel34.setText("STEP 5");

        jLabel35.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel35.setForeground(new java.awt.Color(153, 51, 0));
        jLabel35.setText("Default is 4800 baud");

        jLabel36.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel36.setForeground(new java.awt.Color(255, 0, 0));
        jLabel36.setText("Default is 0.02 sec");

        jLabel37.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel37.setForeground(new java.awt.Color(0, 51, 51));
        jLabel37.setText("STEP 6");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel13.setForeground(new java.awt.Color(204, 0, 204));
        jLabel13.setText("Default is 0.5");

        jButton8.setFont(new java.awt.Font("Dialog", 1, 10));
        jButton8.setText("Reset");
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton8MouseClicked(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel38.setText("C - PPS_Lower_end Height");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel34)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel32))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .addComponent(jTextField3))
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(92, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(339, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jRadioButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(jRadioButton4)
                .addGap(23, 23, 23)
                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(197, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(203, 203, 203)
                        .addComponent(jLabel5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(27, 27, 27)
                                .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(67, 67, 67))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                        .addGap(356, 356, 356)))
                .addGap(245, 245, 245))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(399, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel28)
                                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(jRadioButton7)
                                .addGap(26, 26, 26)
                                .addComponent(jRadioButton8))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(80, 80, 80)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jButton6, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING))))))
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 1109, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextField10, jTextField4, jTextField7, jTextField8, jTextField9});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton6, jButton7, jButton8});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jLabel29))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jLabel9)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel33))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel36))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel34)
                            .addComponent(jLabel14)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel35)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jRadioButton8)
                    .addComponent(jRadioButton7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jButton6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton7))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel28)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel24)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel25)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel26)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel38)))
                                .addGap(7, 7, 7))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jTextField4, jTextField8});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton6, jButton7});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel15, jLabel16, jLabel17, jTextField7});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel14, jTextField3, jTextField5, jTextField6});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, 0, 812, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>                        

    private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        reset();
    }                                     
    
  
    
    
    
    private void jRadioButton8MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        use_suggested_values = false;
        JOptionPane.showMessageDialog(null,"Attention ! Not using SUGGESTED VALUES. \n Input values in STEP 6(B,C,D)" +
                " based on the GPS signal observed. \n A decision can be made by using the PARAMETER values suggested before","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        
    }                                          
    
    private void jRadioButton7MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        use_suggested_values = true;
        jTextField7.setText(String.valueOf((0.6)* pps_trans));
        jTextField8.setText((String.valueOf((pps_lower_end))));
        jTextField9.setText(String.valueOf(nmea_trans));
        JOptionPane.showMessageDialog(null,"Using SUGGESTED VALUES","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
    }                                          
    
    private void jRadioButton5StateChanged(javax.swing.event.ChangeEvent evt) {                                           
// TODO add your handling code here:
    }                                          
    
    private void jRadioButton6MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        signal_channel_left = false;
        jRadioButton5.setEnabled(false);
        JOptionPane.showMessageDialog(null,"Convert WAV to Txt by pressing 'Convert To Txt' Button","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        if(two_channel==true){
        jLabel2.setEnabled(true);
        jButton2.setEnabled(true);
        jLabel3.setEnabled(true);
        jTextField3.setEnabled(true);
        }
    }                                          
    
    private void jRadioButton5MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        signal_channel_left = true;
        jRadioButton6.setEnabled(false);
        JOptionPane.showMessageDialog(null,"Convert WAV to Txt by pressing 'Convert To Txt' Button","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);;
        if(two_channel==true){
        jLabel2.setEnabled(true);
        jButton2.setEnabled(true);
        jLabel3.setEnabled(true);
        jTextField3.setEnabled(true);
        }
    }                                          
    
    private void jRadioButton4MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        use_normalized=false;
        setup_baud_etc();
        JOptionPane.showMessageDialog(null,"Not using NORMALIZED file. Go to STEP 3 and continue  \n", "IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        
    }                                          
    
    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {                                            
// TODO add your handling code here:
    }                                           
    
    private void jRadioButton3MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        
        if(same_as_previous && normalized_file_created){
            use_normalized = true;
            JOptionPane.showMessageDialog(null,"Using NORMALIZED file. Make sure that proper Normalized values for PARAMETERS \n" +
                    "are used in STEP 6(B,C,D)", "IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        }
        if(same_as_previous && normalized_file_created==false){
            
            jRadioButton4.setEnabled(true);
            jRadioButton4.setSelected(true);
          //  jRadioButton3.setSelected(false);
            jRadioButton3.setEnabled(false);
            use_normalized = false;
            JOptionPane.showMessageDialog(null,"Error ! Normalized file wasn't created above. To use non-Normalized TXT file\n" +
                    "press OK else Restart the tool", "IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
            
        }       
        //System.out.println(use_normalized);
      //  JOptionPane.showMessageDialog()
        
        setup_baud_etc();
    }                                          
    
    private void jButton7MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        System.exit(0);
    }                                     
    
    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        jTextArea1.append("\n  Extraction Started \n  please wait ...\n");
        extractGPS ext = new extractGPS();
        if(!use_suggested_values){
            ext.NMEA_High_Low = Float.parseFloat(jTextField9.getText());
            ext.PPS_Average_height = Float.parseFloat(jTextField8.getText());
            ext.PPS_Height = Float.parseFloat(jTextField7.getText());
        }
        if(use_suggested_values && use_normalized==false){
            ext.NMEA_High_Low = (float)nmea_trans;
            ext.PPS_Average_height = (float)pps_lower_end;
            ext.PPS_Height = (float)(0.6*pps_trans);
            
        }
        if(use_suggested_values && use_normalized==true){
            ext.NMEA_High_Low = (float)(nmea_trans / sampleMax);
            ext.PPS_Average_height = (float)(pps_lower_end / sampleMax);
            ext.PPS_Height = (float)(0.6*pps_trans/sampleMax);
        }
        ext.pps_check_percent = Float.parseFloat(jTextField6.getText());
        ext.PPS_Width = Float.parseFloat(jTextField5.getText());
        ext.baud = Integer.parseInt(jTextField3.getText());
        
        System.out.println("came here");
        if(!same_as_previous){
            ext.sampleRate = Integer.parseInt(jTextField4.getText());
            ext.Signal_Max = Integer.parseInt(jTextField10.getText());
        }
        
        else{
            WavInfo info = new WavInfo(file_path);
            ext.sampleRate = info.get_sampleRate();
            ext.Signal_Max = (float)sampleMax;
            
        }
        //  System.out.println(ext.sampleRate);
        String[] ss;
        System.out.println(use_normalized);
        // System.out.println(new_gps_file_path_normalized);
        if(use_normalized){
            System.out.println("using normalized");
            file_to_extract_data_from = new_gps_file_path_normalized;
            ss = file_to_extract_data_from.split(".txt");
            NMEA_file = ss[0] + "NMEA.txt";
            System.out.println(NMEA_file);
            System.out.println(file_to_extract_data_from);
            ext.extract(file_to_extract_data_from,NMEA_file,true);
        } else{
            System.out.println(file_to_extract_data_from);
            System.out.println("not using");
            if(same_as_previous)
                file_to_extract_data_from = saved_text_file_path;
            ss = file_to_extract_data_from.split(".txt");
            NMEA_file = ss[0] + "NMEA.txt";
            System.out.println(NMEA_file);
            //System.out.println(new_gps_file_path_normalized);
            System.out.println(file_to_extract_data_from);
            ext.extract(file_to_extract_data_from,NMEA_file,false);
            
        }
        jTextArea1.append("  EXTRACTION OF GPS DATA DONE. \n  TO PROCESS ANOTHER FILE CHOOSE NEW FILE OR PRESS QUIT TO EXIT");
        JOptionPane.showMessageDialog(null,"EXTRACTION OF GPS DATA DONE. TO CONTINUE PRESS RESET OR IF DONE QUIT !","NEXT STEP",JOptionPane.INFORMATION_MESSAGE);
        
        //  System.out.println("Extraction done");
     //   reset();
        //System.exit(0);
        
        
    }                                     
    
    private void jRadioButton2MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        same_as_previous = false;
        jLabel29.setEnabled(true);
        jRadioButton1.setEnabled(false);
        jRadioButton3.setEnabled(false);
        jRadioButton4.setEnabled(false);
        jButton5.setEnabled(true);
        jTextField10.setEnabled(true);
        jTextField2.setEnabled(true);
        jTextField4.setEnabled(true);
        jTextField10.setEnabled(true);
        jLabel6.setEnabled(false);
        jRadioButton3.setEnabled(false);
        jRadioButton4.setEnabled(false);
        JOptionPane.showMessageDialog(null,"GO TO STEP 2A","NEXT STEP",JOptionPane.INFORMATION_MESSAGE);
        jLabel9.setEnabled(true);
        jLabel12.setEnabled(true);
        jLabel11.setEnabled(true);
        jLabel31.setEnabled(true);
        jLabel21.setEnabled(true);
        //new_gps_file_path_normalized //= jTextField2.getText();
    }                                          
    
    private void jRadioButton1MouseClicked(java.awt.event.MouseEvent evt) {                                           
// TODO add your handling code here:
        same_as_previous = true;
        jRadioButton2.setEnabled(false);
        jLabel29.setEnabled(false);
        System.out.println(same_as_previous);
        jRadioButton3.setEnabled(true);
        jRadioButton4.setEnabled(true);
        jButton5.setEnabled(false);
        jTextField10.setEnabled(false);
        jTextField2.setEnabled(false);
        jTextField4.setEnabled(false);
        jLabel6.setEnabled(true);
        jRadioButton3.setEnabled(true);
        jRadioButton4.setEnabled(true);
        JOptionPane.showMessageDialog(null,"GO TO STEP 1B","NEXT STEP",JOptionPane.INFORMATION_MESSAGE);
        
        // new_gps_file_path_normalized =
        
    }                                          
    
    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        if(!same_as_previous){
            use_normalized = false;
            new_normalized_gps_file = new JFileChooser();
            new_normalized_gps_file.showOpenDialog(this);
            
            //Store the selected file in an Object of type File
            file_to_extract_data_from = new_normalized_gps_file.getSelectedFile().getPath();
            //gps_wav_file = new File(file_path);
            // System.out.println(file_to_extract_data_from);
            jTextField2.setText(file_to_extract_data_from);
           // jTextArea1.setEnabled(true);
        }
        
        setup_baud_etc();
        
    }                                     
    
    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        // Normalize the data here
        FileInputStream fis;
        DataInputStream dis;
        BufferedReader buf;
        String str;
        int divideby=0;
        if(Math.abs(sampleMin) > Math.abs(sampleMax))
            divideby = (int)Math.abs(sampleMin);
        else
            divideby = (int)Math.abs(sampleMax);
        
        try{
            
            fis = new FileInputStream(saved_text_file_path);
            dis = new DataInputStream(fis);
            buf = new BufferedReader(new InputStreamReader(dis));
            //  new_gps_file_path_normalized =
            new_gps_file_path_normalized = saved_text_file_path.substring(0,(saved_text_file_path.length()-4)) + "_normalized.txt";
            BufferedWriter normfile = new BufferedWriter(new FileWriter(new_gps_file_path_normalized));
            double w = 0;
            while((str=buf.readLine())!=null){
                w = Double.parseDouble(str)/divideby;
                normfile.write(String.valueOf(w));
                normfile.newLine();
            }
            normfile.close();
        }catch(IOException e){
            
        }
        
        jLabel20.setEnabled(true);
        jLabel20.setText("Normalized file --> " +new_gps_file_path_normalized);
        normalized_file_created = true;
        JOptionPane.showMessageDialog(null,"Normalized file created", "IMPORTANT MESSAGE !",JOptionPane.INFORMATION_MESSAGE);
        
    }                                     
    
    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        // convert to text here
        
        if(two_channel){
            twoChannelWAV channels_two = new twoChannelWAV(file_path);
            boolean twochannelresult = channels_two.convertToTxt((short)1024);
            if(twochannelresult){
                if(signal_channel_left){
                    saved_text_file_path = channels_two.left_channel_file;
                    sampleMax = channels_two.maxValueL;
                    sampleMin = channels_two.minValueL;
                } else{
                    saved_text_file_path = channels_two.right_channel_file;
                    sampleMax = channels_two.maxValueR;
                    sampleMin = channels_two.minValueR;
                }
                
                
                       
                
            }
        }else{
            oneChannelWAV channels_one = new oneChannelWAV(file_path);
            boolean onechannelresult = channels_one.convertToTxt((short)1024);
            if(onechannelresult){
                saved_text_file_path = channels_one.text_file;
                sampleMax = channels_one.maxValue;
                sampleMin = channels_one.minValue;
            }
        }
        JOptionPane.showMessageDialog(null,"Conversion to txt over.\n","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        
        // got text file
        
        suggestValues();
        // Dispaly the suggestedValues on the jTextArea
        jTextArea1.append("**********************************************************************************");
        jTextArea1.append("\n                      SUGGESTIVE VALUES FOR PARAMETERS NEEDED \n" +
                "                              TO DECIPHER THE GPS SIGNAL \n");
        jTextArea1.append("**********************************************************************************");
        jTextArea1.append("\n");
        jTextArea1.append(" Maximum Amplitude in the recording  --->  MAX = " + sampleMax + "\n");
        jTextArea1.append("\n");
        jTextArea1.append(" Height of the PPS pulse at its starting sample(HIGH)  --->  PPS_start = " + suggested_PPS_height + "\n");
        jTextArea1.append(" Height of the PPS at its ending sample(HIGH)  --->  PPS_end = " + suggested_PPS_endHeight + "\n");
        jTextArea1.append("\n");
        // jTextArea1.append((" Suggestive PPS_Height to use = ")+(int))
        jTextArea1.append((" PPS_start / MAX = " + (suggested_PPS_height/sampleMax)+"\n"));
        jTextArea1.append((" PPS_end / MAX = " + (suggested_PPS_endHeight/sampleMax)+"\n"));
        jTextArea1.append("\n");
        jTextArea1.append(" Amplitude at the UPPER END of the first NMEA pulse  --->  NMEA_upper = " + nmea_start + "\n");
        jTextArea1.append(" Amplitude at the LOWER END of the first NMEA pulse   --->  NMEA_lower = " + nmea_end + "\n");
        jTextArea1.append("\n");
        jTextArea1.append((" NMEA_start / MAX = " + (nmea_start/sampleMax)+"\n"));
        jTextArea1.append((" NMEA_end / MAX = " + (nmea_end/sampleMax)+"\n"));
        jTextArea1.append("\n\n");
        jTextArea1.append("--------------------------------SUGGESTED VALUES-----------------------------\n\n");
        // pps_trans = 0.6*(suggested_PPS_height - (suggested_PPS_endHeight));
        jTextArea1.append(" PPS Transition (HIGH to LOW) = 60% of (PPS_Height_start - PPS_Height_end) = " + (0.6)* pps_trans + "\n");
        pps_lower_end = (1.2) * suggested_PPS_endHeight ;
        jTextArea1.append(" PPS Lower End Height (HIGH) =  1.2 times PPS_Height_end = " + pps_lower_end+ "\n");
        nmea_trans = 0.6 * (nmea_start - nmea_end);
        jTextArea1.append(" NMEA Transiton (HIGH to LOW) = 60% of (nmea_start - nmea_end) = " + (0.6*nmea_trans)+ "\n");
        jTextArea1.append("\n\n");
        jTextArea1.append("-------------SUGGESTED VALUES for NORMALIZED CASE------------\n\n");
        jTextArea1.append(" PPS Transition (HIGH to LOW) = 60% of (PPS_Height_start - PPS_Height_end)/(Max_Sample_value) = " + (0.6)* pps_trans/sampleMax + "\n");
        //  pps_lower_end = (1.2) * suggested_PPS_endHeight ;
        jTextArea1.append(" PPS Lower End Height (HIGH) =  1.2 times PPS_Height_end/(Max_Sample_value) = " + pps_lower_end/sampleMax+ "\n");
        //   nmea_trans = 0.6 * (nmea_start - nmea_end);
        jTextArea1.append(" NMEA Transiton (HIGH to LOW) = 60% of (nmea_start - nmea_end)/(Max_Sample_value) = " + (0.6*nmea_trans)/sampleMax+ "\n");
        // jTextArea1.append("\n\n");
        
        
        jLabel3.setEnabled(true);
        jButton3.setEnabled(true);
        JOptionPane.showMessageDialog(null,"Suggestion of PARAMETERS used in STEP 6 is DONE ! The values are displayed in \n" +
                "the INFORMATION BOX on the LEFT","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        
        
        
    }                                     
    
    private void setup_baud_etc(){
        jLabel32.setEnabled(true);
        jLabel10.setEnabled(true);
        jLabel35.setEnabled(true);
        jLabel33.setEnabled(true);
        jLabel36.setEnabled(true);
        jLabel14.setEnabled(true);
        jLabel34.setEnabled(true);
        jLabel13.setEnabled(true);
        jTextField3.setEnabled(true);
        jTextField5.setEnabled(true);
        jTextField6.setEnabled(true);
        
        jLabel37.setEnabled(true);
        jLabel22.setEnabled(true);
        jLabel15.setEnabled(true);
        jLabel16.setEnabled(true);
        jLabel17.setEnabled(true);
        jRadioButton7.setEnabled(true);
        jRadioButton8.setEnabled(true);
        jTextField7.setEnabled(true);
        jTextField9.setEnabled(true);
        jTextField8.setEnabled(true);
        jButton6.setEnabled(true);
        
    }
    
    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        // start extracting wave file info.
        //   signal_baud = Integer.parseInt(jTextField10.getText());
        
        WavInfo wavinfo = new WavInfo(file_path);
        jTextArea1.setEnabled(true);
        jTextArea1.setRows(10);
        jTextArea1.setText(" AUDIO_FORMAT :  " + wavinfo.get_Audioformat() + "\n" +
                " NUMBER OF CHANNELS :  " + wavinfo.get_channels() + "\n" +
                " RECORDING LENGTH IN FRAME :  " + wavinfo.get_length_in_frames() + "\n" +
                " RECORDING SIZE IN BYTES :  " + wavinfo.get_recordingSizeinBytes() + "\n" +
                " RECORDING TIME (seconds) :  " + wavinfo.get_length_in_frames()/wavinfo.get_frameRate() + "\n" +
                " FRAME RATE (frames/second) :  " + wavinfo.get_frameRate() + "\n" +
                " SAMPLING RATE (samples/second) :  " + (int)wavinfo.get_sampleRate() + "\n" +
                " FRAME SIZE IN BYTES :  " + wavinfo.get_frameSize() + "\n" +
                " DATA ARRANGED IN  " + wavinfo.get_dataFormat() +" ORDER"+ "\n" );
        JOptionPane.showMessageDialog(null," WavInfo displayed in the INFORMATION BOX on the left ","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);;
        if(wavinfo.get_channels()==2){
            jRadioButton5.setEnabled(true);
            jRadioButton6.setEnabled(true);
            jTextArea2.setEnabled(true);
            
            two_channel=true;
            JOptionPane.showMessageDialog(null,"This is a 2 channel wav recording. Choose the channel that has GPS data on it so that proper values " +
                    "are suggested for the PARAMETERS used for deciphering the GPS signal", "IMPORTANT MESSAGE ABOUT RECORDING ",JOptionPane.INFORMATION_MESSAGE);
            
        }else{
        two_channel=false;
        jLabel2.setEnabled(true);
        jButton2.setEnabled(true);
        jLabel3.setEnabled(true);
        jTextField3.setEnabled(true);
      //  JOptionPane.showMessageDialog(null,"Set the Baud Rate for the recorded signal. The default rate is 4800 symbols per second\nAfter" +
       //         "the Baud is set Press 'Convert to Txt' button to convert WAV to Txt");
        
        JOptionPane.showMessageDialog(null,"Press 'Convert to Txt' Button to convert WAV to Txt !!!","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);;
        }
        
    }                                     
    
    private void formMouseClicked(java.awt.event.MouseEvent evt) {                                  
// TODO add your handling code here:
        
    }                                 
    
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {                                      
// TODO add your handling code here:
        //Choose the file that you want to process
        choose_file = new JFileChooser();
        choose_file.showOpenDialog(this);
        
        //Store the selected file in an Object of type File
        file_path = choose_file.getSelectedFile().getPath();
        gps_wav_file = new File(file_path);
        jTextField1.setText(file_path);
        jLabel4.setEnabled(true);
        jButton4.setEnabled(true);        
        JOptionPane.showMessageDialog(null,"WAV file Selected. Press 'WAV Info' button to get wav file information","IMPORTANT MESSAGE",JOptionPane.INFORMATION_MESSAGE);
        //   jLabel18 = new JLabel("C:/Users/ali/Desktop/GPSExtraction_04/insetpic.jpg");
    }                                     
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new extract_GPS_Data().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify                     
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration                   
    
    private void reset(){
        choose_file = null;
        gps_wav_file = null;
        file_path = null;
        saved_text_file_path = null;
        file_to_extract_data_from=null;
        
        new_normalized_gps_file=null;
        new_gps_file=null;
        new_gps_file_path_normalized=null;
        
        same_as_previous=false;
        use_normalized=false;
        sampling_rate=0;
        NMEA_file=null;
        suggested_NMEA_transition=0;
        suggested_PPS_height=0;
        suggested_PPS_endHeight=0;
        suggested_PPS_average=0;
        nmea_start=0;
        nmea_end=0;
        //double suggested_
        
        sampleMax=0;
        sampleMin=0;
        signal_baud=0;
        pps_trans = pps_lower_end = nmea_trans = 0;
        jTextField1.setText("");
        jButton4.setEnabled(false);
        jTextArea2.setEnabled(false);
        buttonGroup3.remove(jRadioButton5);
        jRadioButton5.setSelected(false);
        buttonGroup3.add(jRadioButton5);
        jRadioButton5.setEnabled(false);
        
        buttonGroup3.remove(jRadioButton6);
        jRadioButton6.setSelected(false);
        buttonGroup3.add(jRadioButton6);
        jRadioButton6.setEnabled(false);
       
        jLabel2.setEnabled(false);
        jButton2.setEnabled(false);
        jLabel20.setEnabled(false);
        
        //jLabel8.setEnabled(false);
        buttonGroup1.remove(jRadioButton1);
        jRadioButton1.setSelected(false);
        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setEnabled(true);
        
        buttonGroup1.remove(jRadioButton2);
        jRadioButton2.setSelected(false);
        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setEnabled(true);
        
        jLabel6.setEnabled(false);
        buttonGroup2.remove(jRadioButton3);
        jRadioButton3.setSelected(false);
        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setEnabled(false);
        
        buttonGroup2.remove(jRadioButton4);
        jRadioButton4.setSelected(false);
        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setEnabled(false);
        
        jLabel9.setEnabled(false);
        jButton5.setEnabled(false);
        jTextField2.setText("");
        jTextField2.setEnabled(false);
        
        jLabel11.setEnabled(false);
        jLabel12.setEnabled(false);
        jLabel21.setEnabled(false);
        jLabel31.setEnabled(false);
        jTextField4.setText("");
        jTextField4.setEnabled(false);
        jTextField10.setText("");
        jTextField10.setEnabled(false);
        
        jLabel32.setEnabled(false);
        jLabel10.setEnabled(false);
        jLabel35.setEnabled(false);
        jLabel33.setEnabled(false);
        
        jLabel36.setEnabled(false);
        jLabel14.setEnabled(false);
        jLabel34.setEnabled(false);
        jLabel13.setEnabled(false);
        jLabel37.setEnabled(false);
        jTextField3.setText("");
        jTextField3.setEnabled(false);
        jTextField5.setText("");
        jTextField5.setEnabled(false);
        jTextField6.setText("");
        jTextField6.setEnabled(false);
        
        jLabel7.setEnabled(false);
        jLabel22.setEnabled(false);
        jLabel15.setEnabled(false);
        jLabel16.setEnabled(false);
        jLabel17.setEnabled(false);
        jTextField3.setText("4800");
        jTextField3.setEnabled(false);
        jTextField5.setText("0.02");
        jTextField5.setEnabled(false);
        jTextField6.setText("0.5");
        jTextField6.setEnabled(false);
        buttonGroup4.remove(jRadioButton7);
        jRadioButton7.setSelected(false);
        buttonGroup4.add(jRadioButton7);
        jRadioButton7.setEnabled(false);
        
        buttonGroup4.remove(jRadioButton8);
        jRadioButton8.setSelected(false);
        buttonGroup4.add(jRadioButton8);
        jRadioButton8.setEnabled(false);
        jTextField7.setText("");
        jTextField7.setEnabled(false);
        jTextField8.setText("");
        jTextField8.setEnabled(false);        
        jTextField9.setText("");
        jTextField9.setEnabled(false);
        
        jButton6.setEnabled(false);
        
        jTextArea1.setText("");
        jTextArea1.setEnabled(false);
                
        jLabel3.setEnabled(false);
        jButton3.setEnabled(false);
        jLabel20.setEnabled(false);
                
        
        
        
        
        
        
        
        
    }
    
}

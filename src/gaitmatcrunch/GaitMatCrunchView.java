/*
 * GaitMatCrunchView.java
 */

package gaitmatcrunch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.ArrayList;

/**
 * The application's main frame.
 */

public class GaitMatCrunchView extends FrameView {

    Vector<Subject> subjectList = new Vector<Subject>();
    Vector<String> subjectDisplay = new Vector<String>();
    Saver saver;
    String dbFile = "./gaitCrunchDatabase.db";
    static int drawTime = 0;
    static double drawRT = 0;
    static Vector<Point> points;

    public GaitMatCrunchView(SingleFrameApplication app) {
        super(app);

        initComponents();
        

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                }
            }
        });
    }


    class DrawPanel extends javax.swing.JPanel {
        
        Walk w = new Walk();
        public DrawPanel(int numToDraw) {
            int i = 0;
            System.out.println("Entering draw panel constructor");
            points = new Vector<Point>();
            if( (Walk) walkList.getSelectedValue() != null ) {
                points = new Vector<Point>();
                w = (Walk) walkList.getSelectedValue();
                for(int j = 0; j < w.getLength(); j++)
                {
                    for(int k = 0; k < w.getStep(j).getNumPoints(); k++)
                    {
                        Point p = new Point();
                        
                        p.x = w.getStep(j).getPoint(k).x;
                        p.y = w.getStep(j).getPoint(k).y;
                        p.onset = w.getStep(j).getPoint(k).onset;
                        p.offset = w.getStep(j).getPoint(k).offset;
                        p.time = w.getStep(j).getPoint(k).time;
                        System.out.println("Adding point at " + p.x + p.y);
                        points.add(p);
                        i++;
//                        if(i > numToDraw)
//                            break;
                    }
//                    if(i > numToDraw)
//                        break;
                }
                System.out.println("Repainting");

                this.repaint();

            }
        }
        @Override public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            if(points != null) {
                double ontime = points.get(drawTime).onset;
                double offtime = points.get(drawTime).offset;
                System.out.println("ON OFF TIMES: " + ontime + " "+ offtime);
                double offset_x = 0;
                double offset_y = 0;
                Dimension d = this.getSize();
                double scale = Double.valueOf(scaleText.getText());
                for(int i = 0; i < points.size(); i++)
                {
                    if(i == 0){
                        offset_x = (d.width - 50) - (points.get(i).x * scale);
                        offset_y = ((d.height) / 2) - (points.get(i).y * scale);
                    }
//                    if( (points.get(i).offset > ontime && points.get(i).onset < ontime) ) {
                    if( (points.get(i).offset > ontime && points.get(i).onset < ontime) ) {
//                        System.out.println("Drawing point at " + (points.get(i).x + offset_x) + " " + (points.get(i).y + offset_y));
                        g.setColor(Color.white);
                        g.fillRect((int)(points.get(i).x * scale + offset_x), (int)(points.get(i).y * scale + offset_y), 3, 3);
                    }
//                    else if( (points.get(i).onset < offtime && points.get(i).offset > offtime) ) {
                    else if( (points.get(i).onset < offtime && points.get(i).offset > offtime) ) {
//                        System.out.println("Drawing point at " + (points.get(i).x + offset_x) + " " + (points.get(i).y + offset_y));
                        g.setColor(Color.white);
                        g.fillRect((int)(points.get(i).x * scale + offset_x), (int)(points.get(i).y * scale + offset_y), 3, 3);
                    }
//                    else if (i == points.size() - 1) {
                    else if (i == drawTime) {

                        g.setColor(Color.green);
                        g.fillRect((int)(points.get(i).x * scale + offset_x), (int)(points.get(i).y * scale + offset_y), 3, 3);
                        labelLocation.setText("Location: " + (int)(points.get(i).x) + "," + (int)(points.get(i).y) );
                        labelOnset.setText("Onset: " + points.get(i).onset);
                        labelOffset.setText("Offset: " + points.get(i).offset);
                        
                    }
                    else {
                        g.setColor(Color.red);
                        g.fillRect((int)(points.get(i).x * scale + offset_x), (int)(points.get(i).y * scale + offset_y), 3, 3);
                    }
                    
                }
            }
        }

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = GaitMatCrunchApp.getApplication().getMainFrame();
            aboutBox = new GaitMatCrunchAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        GaitMatCrunchApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        addNewWalkBut = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        subjectNumList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        walkList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        exportButton = new javax.swing.JButton();
        statsButton = new javax.swing.JButton();
        addNewSubBut = new javax.swing.JButton();
        delSubBut = new javax.swing.JButton();
        delWalkBut = new javax.swing.JButton();
        playWalkBut = new javax.swing.JButton();
        editSubBut = new javax.swing.JButton();
        dumpSetButton = new javax.swing.JButton();
        checkButton = new javax.swing.JButton();
        dumpSetButton1 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        batchImportMenuItem = new javax.swing.JMenuItem();
        menuLoadDB = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        addSubject = new javax.swing.JDialog();
        cancelBut = new javax.swing.JButton();
        addSubBut = new javax.swing.JButton();
        subNumText = new javax.swing.JTextField();
        subBdayText = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        subTdayText = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        studyText = new javax.swing.JTextField();
        addWalkDialog = new javax.swing.JDialog();
        addWalkNameText = new javax.swing.JTextField();
        addWalkBut = new javax.swing.JButton();
        cancelWalkBut = new javax.swing.JButton();
        addWalkFileText = new javax.swing.JTextField();
        browseWalkBut = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        walkStats = new javax.swing.JFrame();
        labelSubjectNum = new javax.swing.JLabel();
        labelStudyNum = new javax.swing.JLabel();
        labelTestDate = new javax.swing.JLabel();
        labelBirthDate = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        labelWalkTitle = new javax.swing.JLabel();
        labelVelocity = new javax.swing.JLabel();
        labelFlight = new javax.swing.JLabel();
        labelDSTime = new javax.swing.JLabel();
        labelPercentDS = new javax.swing.JLabel();
        labelSSTime = new javax.swing.JLabel();
        labelPercentSS = new javax.swing.JLabel();
        labelPercentFlight = new javax.swing.JLabel();
        labelTime = new javax.swing.JLabel();
        labelNumFootfalls = new javax.swing.JLabel();
        labelAvgStrideLen = new javax.swing.JLabel();
        buttonClose = new javax.swing.JButton();
        textWalkTitle = new javax.swing.JTextField();
        textTime = new javax.swing.JTextField();
        textNumFootFalls = new javax.swing.JTextField();
        textVelocity = new javax.swing.JTextField();
        textFlightTime = new javax.swing.JTextField();
        textPerFlightTime = new javax.swing.JTextField();
        textDSTime = new javax.swing.JTextField();
        textPerDS = new javax.swing.JTextField();
        textSSTime = new javax.swing.JTextField();
        textPerSS = new javax.swing.JTextField();
        textStepWid = new javax.swing.JTextField();
        textStrideLen = new javax.swing.JTextField();
        labelAvgStepLen = new javax.swing.JLabel();
        textStepLen = new javax.swing.JTextField();
        labelAvgStepWid = new javax.swing.JLabel();
        textBirthDate = new javax.swing.JTextField();
        textSubjNum = new javax.swing.JTextField();
        textStudyNum = new javax.swing.JTextField();
        textTestDate = new javax.swing.JTextField();
        exportDialog = new javax.swing.JDialog();
        textFileName = new javax.swing.JTextField();
        buttonBrowseSaveFile = new javax.swing.JButton();
        checkHeaders = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        buttonSaveFile = new javax.swing.JButton();
        buttonSaveCancel = new javax.swing.JButton();
        playDialog = new javax.swing.JDialog();
        scaleText = new javax.swing.JTextField();
        bk50 = new javax.swing.JButton();
        fw50 = new javax.swing.JButton();
        fw10 = new javax.swing.JButton();
        bk10 = new javax.swing.JButton();
        bk1 = new javax.swing.JButton();
        fw1 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        playClose = new javax.swing.JButton();
        walkScrollPane = new javax.swing.JScrollPane();
        walkGraphic = new DrawPanel(0);
        fw5 = new javax.swing.JButton();
        bk5 = new javax.swing.JButton();
        labelLocation = new javax.swing.JLabel();
        labelOnset = new javax.swing.JLabel();
        labelOffset = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        batchImportDialog = new javax.swing.JDialog();
        jScrollPane4 = new javax.swing.JScrollPane();
        batchImportFolderText = new javax.swing.JTextPane();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        batchImportBrowseButton = new javax.swing.JButton();
        batchImportConfirm = new javax.swing.JButton();
        batchImportCancel = new javax.swing.JButton();
        editSubDialog = new javax.swing.JDialog();
        editSubConfirm = new javax.swing.JButton();
        editSubCancel = new javax.swing.JButton();
        editSubIDText = new javax.swing.JTextField();
        editSubTestText = new javax.swing.JTextField();
        editSubStudyText = new javax.swing.JTextField();
        editSubBirthText = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();

        mainPanel.setMinimumSize(new java.awt.Dimension(730, 320));
        mainPanel.setName("mainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gaitmatcrunch.GaitMatCrunchApp.class).getContext().getResourceMap(GaitMatCrunchView.class);
        addNewWalkBut.setText(resourceMap.getString("addNewWalkBut.text")); // NOI18N
        addNewWalkBut.setName("addNewWalkBut"); // NOI18N
        addNewWalkBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addNewWalkButMouseClicked(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        subjectNumList.setModel(subjectNumList.getModel());
        subjectNumList.setName("subjectNumList"); // NOI18N
        subjectNumList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                subjectNumListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(subjectNumList);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        walkList.setModel(subjectNumList.getModel());
        walkList.setName("walkList"); // NOI18N
        jScrollPane2.setViewportView(walkList);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        exportButton.setText(resourceMap.getString("exportButton.text")); // NOI18N
        exportButton.setName("exportButton"); // NOI18N
        exportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exportButtonMouseClicked(evt);
            }
        });

        statsButton.setText(resourceMap.getString("statsButton.text")); // NOI18N
        statsButton.setName("statsButton"); // NOI18N
        statsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                statsButtonMouseReleased(evt);
            }
        });

        addNewSubBut.setText(resourceMap.getString("addNewSubBut.text")); // NOI18N
        addNewSubBut.setName("addNewSubBut"); // NOI18N
        addNewSubBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addNewSubButMouseReleased(evt);
            }
        });

        delSubBut.setText(resourceMap.getString("delSubBut.text")); // NOI18N
        delSubBut.setName("delSubBut"); // NOI18N
        delSubBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                delSubButMouseReleased(evt);
            }
        });

        delWalkBut.setText(resourceMap.getString("delWalkBut.text")); // NOI18N
        delWalkBut.setName("delWalkBut"); // NOI18N
        delWalkBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                delWalkButMouseReleased(evt);
            }
        });

        playWalkBut.setText(resourceMap.getString("playWalkBut.text")); // NOI18N
        playWalkBut.setName("playWalkBut"); // NOI18N
        playWalkBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                playWalkButMouseReleased(evt);
            }
        });

        editSubBut.setText(resourceMap.getString("editSubBut.text")); // NOI18N
        editSubBut.setName("editSubBut"); // NOI18N
        editSubBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                editSubButMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editSubButMouseClicked(evt);
            }
        });

        dumpSetButton.setText(resourceMap.getString("dumpSetButton.text")); // NOI18N
        dumpSetButton.setActionCommand(resourceMap.getString("dumpSetButton.actionCommand")); // NOI18N
        dumpSetButton.setName("dumpSetButton"); // NOI18N
        dumpSetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dumpSetButtonMouseClicked(evt);
            }
        });

        checkButton.setText(resourceMap.getString("checkButton.text")); // NOI18N
        checkButton.setName("checkButton"); // NOI18N
        checkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkButtonMouseClicked(evt);
            }
        });

        dumpSetButton1.setText(resourceMap.getString("dumpSetButton1.text")); // NOI18N
        dumpSetButton1.setName("dumpSetButton1"); // NOI18N
        dumpSetButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dumpSetButton1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(delSubBut, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(addNewSubBut, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(editSubBut, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                            .addComponent(addNewWalkBut, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(delWalkBut, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dumpSetButton1)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(checkButton, 0, 0, Short.MAX_VALUE)
                                .addComponent(playWalkBut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(exportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(statsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                            .addComponent(dumpSetButton)))
                    .addComponent(jLabel3))
                .addGap(64, 64, 64))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addNewSubBut)
                            .addComponent(addNewWalkBut)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dumpSetButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dumpSetButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(playWalkBut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(delWalkBut)
                    .addComponent(delSubBut))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editSubBut)
                .addGap(13, 13, 13))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addContainerGap())
        );

        saver = new Saver();
        subjectList = saver.Loader(dbFile);
        subjectNumList.setListData(subjectList);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        batchImportMenuItem.setText(resourceMap.getString("batchImportMenuItem.text")); // NOI18N
        batchImportMenuItem.setName("batchImportMenuItem"); // NOI18N
        batchImportMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                batchImportMenuItemMouseReleased(evt);
            }
        });
        fileMenu.add(batchImportMenuItem);

        menuLoadDB.setText(resourceMap.getString("menuLoadDB.text")); // NOI18N
        menuLoadDB.setName("menuLoadDB"); // NOI18N
        menuLoadDB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                menuLoadDBMouseReleased(evt);
            }
        });
        fileMenu.add(menuLoadDB);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(gaitmatcrunch.GaitMatCrunchApp.class).getContext().getActionMap(GaitMatCrunchView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 690, Short.MAX_VALUE)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel))
                .addGap(3, 3, 3))
        );

        addSubject.setTitle(resourceMap.getString("addSubject.title")); // NOI18N
        addSubject.setMinimumSize(new java.awt.Dimension(225, 225));
        addSubject.setName("addSubject"); // NOI18N

        cancelBut.setText(resourceMap.getString("cancelBut.text")); // NOI18N
        cancelBut.setName("cancelBut"); // NOI18N
        cancelBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButMouseClicked(evt);
            }
        });

        addSubBut.setText(resourceMap.getString("addSubBut.text")); // NOI18N
        addSubBut.setName("addSubBut"); // NOI18N
        addSubBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addSubButMouseClicked(evt);
            }
        });

        subNumText.setText(resourceMap.getString("subNumText.text")); // NOI18N
        subNumText.setName("subNumText"); // NOI18N

        subBdayText.setText(resourceMap.getString("subBdayText.text")); // NOI18N
        subBdayText.setName("subBdayText"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        subTdayText.setText(resourceMap.getString("subTdayText.text")); // NOI18N
        subTdayText.setName("subTdayText"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        studyText.setName("studyText"); // NOI18N

        javax.swing.GroupLayout addSubjectLayout = new javax.swing.GroupLayout(addSubject.getContentPane());
        addSubject.getContentPane().setLayout(addSubjectLayout);
        addSubjectLayout.setHorizontalGroup(
            addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addSubjectLayout.createSequentialGroup()
                .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addSubjectLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(addSubBut, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelBut))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addSubjectLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(studyText, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(subTdayText, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(subBdayText, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(subNumText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2))))
                .addGap(104, 104, 104))
        );
        addSubjectLayout.setVerticalGroup(
            addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addSubjectLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(1, 1, 1)
                .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studyText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subBdayText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subTdayText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addSubBut)
                    .addComponent(cancelBut))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        addWalkDialog.setTitle(resourceMap.getString("addWalkDialog.title")); // NOI18N
        addWalkDialog.setMinimumSize(new java.awt.Dimension(200, 200));
        addWalkDialog.setName("addWalkDialog"); // NOI18N

        addWalkNameText.setText(resourceMap.getString("addWalkNameText.text")); // NOI18N
        addWalkNameText.setName("addWalkNameText"); // NOI18N

        addWalkBut.setText(resourceMap.getString("addWalkBut.text")); // NOI18N
        addWalkBut.setName("addWalkBut"); // NOI18N
        addWalkBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addWalkButMouseReleased(evt);
            }
        });

        cancelWalkBut.setText(resourceMap.getString("cancelWalkBut.text")); // NOI18N
        cancelWalkBut.setName("cancelWalkBut"); // NOI18N
        cancelWalkBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelWalkButMouseReleased(evt);
            }
        });

        addWalkFileText.setText(resourceMap.getString("addWalkFileText.text")); // NOI18N
        addWalkFileText.setName("addWalkFileText"); // NOI18N

        browseWalkBut.setText(resourceMap.getString("browseWalkBut.text")); // NOI18N
        browseWalkBut.setName("browseWalkBut"); // NOI18N
        browseWalkBut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                browseWalkButMouseReleased(evt);
            }
        });

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        javax.swing.GroupLayout addWalkDialogLayout = new javax.swing.GroupLayout(addWalkDialog.getContentPane());
        addWalkDialog.getContentPane().setLayout(addWalkDialogLayout);
        addWalkDialogLayout.setHorizontalGroup(
            addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addWalkDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(addWalkDialogLayout.createSequentialGroup()
                        .addGroup(addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(addWalkNameText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addWalkDialogLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(addWalkBut))
                            .addComponent(addWalkFileText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
                        .addGroup(addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addWalkDialogLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelWalkBut))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(addWalkDialogLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel4))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addWalkDialogLayout.createSequentialGroup()
                                    .addGap(4, 4, 4)
                                    .addComponent(browseWalkBut))))))
                .addContainerGap())
        );
        addWalkDialogLayout.setVerticalGroup(
            addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addWalkDialogLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addWalkFileText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseWalkBut))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addWalkNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(addWalkDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelWalkBut)
                    .addComponent(addWalkBut))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        walkStats.setMinimumSize(new java.awt.Dimension(652, 418));
        walkStats.setName("walkStats"); // NOI18N
        walkStats.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelSubjectNum.setText(resourceMap.getString("labelSubjectNum.text")); // NOI18N
        labelSubjectNum.setName("labelSubjectNum"); // NOI18N
        walkStats.getContentPane().add(labelSubjectNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        labelStudyNum.setText(resourceMap.getString("labelStudyNum.text")); // NOI18N
        labelStudyNum.setName("labelStudyNum"); // NOI18N
        walkStats.getContentPane().add(labelStudyNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, -1, -1));

        labelTestDate.setText(resourceMap.getString("labelTestDate.text")); // NOI18N
        labelTestDate.setName("labelTestDate"); // NOI18N
        walkStats.getContentPane().add(labelTestDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        labelBirthDate.setText(resourceMap.getString("labelBirthDate.text")); // NOI18N
        labelBirthDate.setDoubleBuffered(true);
        labelBirthDate.setName("labelBirthDate"); // NOI18N
        labelBirthDate.setOpaque(true);
        walkStats.getContentPane().add(labelBirthDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        jSeparator1.setName("jSeparator1"); // NOI18N
        walkStats.getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        labelWalkTitle.setText(resourceMap.getString("labelWalkTitle.text")); // NOI18N
        labelWalkTitle.setDoubleBuffered(true);
        labelWalkTitle.setMinimumSize(new java.awt.Dimension(100, 20));
        labelWalkTitle.setName("labelWalkTitle"); // NOI18N
        labelWalkTitle.setOpaque(true);
        walkStats.getContentPane().add(labelWalkTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, 20));

        labelVelocity.setText(resourceMap.getString("labelVelocity.text")); // NOI18N
        labelVelocity.setMinimumSize(new java.awt.Dimension(100, 20));
        labelVelocity.setName("labelVelocity"); // NOI18N
        labelVelocity.setOpaque(true);
        walkStats.getContentPane().add(labelVelocity, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, -1, 20));

        labelFlight.setText(resourceMap.getString("labelFlight.text")); // NOI18N
        labelFlight.setMinimumSize(new java.awt.Dimension(100, 20));
        labelFlight.setName("labelFlight"); // NOI18N
        labelFlight.setOpaque(true);
        walkStats.getContentPane().add(labelFlight, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, -1, 20));

        labelDSTime.setText(resourceMap.getString("labelDSTime.text")); // NOI18N
        labelDSTime.setMinimumSize(new java.awt.Dimension(100, 20));
        labelDSTime.setName("labelDSTime"); // NOI18N
        labelDSTime.setOpaque(true);
        walkStats.getContentPane().add(labelDSTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 160, -1, 20));

        labelPercentDS.setText(resourceMap.getString("labelPercentDS.text")); // NOI18N
        labelPercentDS.setMinimumSize(new java.awt.Dimension(100, 20));
        labelPercentDS.setName("labelPercentDS"); // NOI18N
        labelPercentDS.setOpaque(true);
        walkStats.getContentPane().add(labelPercentDS, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 50, -1, 20));

        labelSSTime.setText(resourceMap.getString("labelSSTime.text")); // NOI18N
        labelSSTime.setMinimumSize(new java.awt.Dimension(100, 20));
        labelSSTime.setName("labelSSTime"); // NOI18N
        labelSSTime.setOpaque(true);
        walkStats.getContentPane().add(labelSSTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 190, -1, 20));

        labelPercentSS.setText(resourceMap.getString("labelPercentSS.text")); // NOI18N
        labelPercentSS.setMinimumSize(new java.awt.Dimension(100, 20));
        labelPercentSS.setName("labelPercentSS"); // NOI18N
        labelPercentSS.setOpaque(true);
        walkStats.getContentPane().add(labelPercentSS, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 80, -1, 20));

        labelPercentFlight.setText(resourceMap.getString("labelPercentFlight.text")); // NOI18N
        labelPercentFlight.setMinimumSize(new java.awt.Dimension(100, 20));
        labelPercentFlight.setName("labelPercentFlight"); // NOI18N
        labelPercentFlight.setOpaque(true);
        walkStats.getContentPane().add(labelPercentFlight, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 20, -1, 30));

        labelTime.setText(resourceMap.getString("labelTime.text")); // NOI18N
        labelTime.setMinimumSize(new java.awt.Dimension(100, 20));
        labelTime.setName("labelTime"); // NOI18N
        labelTime.setOpaque(true);
        walkStats.getContentPane().add(labelTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, -1, 10));

        labelNumFootfalls.setText(resourceMap.getString("labelNumFootfalls.text")); // NOI18N
        labelNumFootfalls.setMinimumSize(new java.awt.Dimension(100, 20));
        labelNumFootfalls.setName("labelNumFootfalls"); // NOI18N
        labelNumFootfalls.setOpaque(true);
        walkStats.getContentPane().add(labelNumFootfalls, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, -1, 30));

        labelAvgStrideLen.setText(resourceMap.getString("labelAvgStrideLen.text")); // NOI18N
        labelAvgStrideLen.setMinimumSize(new java.awt.Dimension(100, 20));
        labelAvgStrideLen.setName("labelAvgStrideLen"); // NOI18N
        labelAvgStrideLen.setOpaque(true);
        walkStats.getContentPane().add(labelAvgStrideLen, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 250, -1, 30));

        buttonClose.setText(resourceMap.getString("buttonClose.text")); // NOI18N
        buttonClose.setName("buttonClose"); // NOI18N
        buttonClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonCloseMouseReleased(evt);
            }
        });
        walkStats.getContentPane().add(buttonClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 350, -1, -1));

        textWalkTitle.setText(resourceMap.getString("textWalkTitle.text")); // NOI18N
        textWalkTitle.setName("textWalkTitle"); // NOI18N
        walkStats.getContentPane().add(textWalkTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 150, 170, -1));

        textTime.setText(resourceMap.getString("textTime.text")); // NOI18N
        textTime.setName("textTime"); // NOI18N
        walkStats.getContentPane().add(textTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 180, 170, -1));

        textNumFootFalls.setText(resourceMap.getString("textNumFootFalls.text")); // NOI18N
        textNumFootFalls.setName("textNumFootFalls"); // NOI18N
        walkStats.getContentPane().add(textNumFootFalls, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 210, 170, -1));

        textVelocity.setText(resourceMap.getString("textVelocity.text")); // NOI18N
        textVelocity.setName("textVelocity"); // NOI18N
        walkStats.getContentPane().add(textVelocity, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 240, 170, -1));

        textFlightTime.setText(resourceMap.getString("textFlightTime.text")); // NOI18N
        textFlightTime.setName("textFlightTime"); // NOI18N
        walkStats.getContentPane().add(textFlightTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, 170, -1));

        textPerFlightTime.setText(resourceMap.getString("textPerFlightTime.text")); // NOI18N
        textPerFlightTime.setName("textPerFlightTime"); // NOI18N
        walkStats.getContentPane().add(textPerFlightTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, 170, -1));

        textDSTime.setText(resourceMap.getString("textDSTime.text")); // NOI18N
        textDSTime.setName("textDSTime"); // NOI18N
        walkStats.getContentPane().add(textDSTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 160, 170, -1));

        textPerDS.setText(resourceMap.getString("textPerDS.text")); // NOI18N
        textPerDS.setName("textPerDS"); // NOI18N
        walkStats.getContentPane().add(textPerDS, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, 170, -1));

        textSSTime.setText(resourceMap.getString("textSSTime.text")); // NOI18N
        textSSTime.setName("textSSTime"); // NOI18N
        walkStats.getContentPane().add(textSSTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 190, 170, -1));

        textPerSS.setText(resourceMap.getString("textPerSS.text")); // NOI18N
        textPerSS.setName("textPerSS"); // NOI18N
        walkStats.getContentPane().add(textPerSS, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 80, 170, -1));

        textStepWid.setText(resourceMap.getString("textStepWid.text")); // NOI18N
        textStepWid.setName("textStepWid"); // NOI18N
        walkStats.getContentPane().add(textStepWid, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 220, 170, -1));

        textStrideLen.setText(resourceMap.getString("textStrideLen.text")); // NOI18N
        textStrideLen.setName("textStrideLen"); // NOI18N
        walkStats.getContentPane().add(textStrideLen, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 250, 170, -1));

        labelAvgStepLen.setText(resourceMap.getString("labelAvgStepLen.text")); // NOI18N
        labelAvgStepLen.setMinimumSize(new java.awt.Dimension(100, 20));
        labelAvgStepLen.setName("labelAvgStepLen"); // NOI18N
        labelAvgStepLen.setOpaque(true);
        walkStats.getContentPane().add(labelAvgStepLen, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 280, -1, 30));

        textStepLen.setText(resourceMap.getString("textStepLen.text")); // NOI18N
        textStepLen.setName("textStepLen"); // NOI18N
        walkStats.getContentPane().add(textStepLen, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 280, 170, -1));

        labelAvgStepWid.setText(resourceMap.getString("labelAvgStepWid.text")); // NOI18N
        labelAvgStepWid.setMinimumSize(new java.awt.Dimension(100, 20));
        labelAvgStepWid.setName("labelAvgStepWid"); // NOI18N
        labelAvgStepWid.setOpaque(true);
        walkStats.getContentPane().add(labelAvgStepWid, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 220, -1, 30));

        textBirthDate.setText(resourceMap.getString("textBirthDate.text")); // NOI18N
        textBirthDate.setName("textBirthDate"); // NOI18N
        walkStats.getContentPane().add(textBirthDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 110, 90, -1));

        textSubjNum.setText(resourceMap.getString("textSubjNum.text")); // NOI18N
        textSubjNum.setName("textSubjNum"); // NOI18N
        walkStats.getContentPane().add(textSubjNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, 90, -1));

        textStudyNum.setText(resourceMap.getString("textStudyNum.text")); // NOI18N
        textStudyNum.setName("textStudyNum"); // NOI18N
        walkStats.getContentPane().add(textStudyNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, 90, -1));

        textTestDate.setText(resourceMap.getString("textTestDate.text")); // NOI18N
        textTestDate.setName("textTestDate"); // NOI18N
        walkStats.getContentPane().add(textTestDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 90, -1));

        exportDialog.setMinimumSize(new java.awt.Dimension(424, 300));
        exportDialog.setName("exportDialog"); // NOI18N

        textFileName.setText(resourceMap.getString("textFileName.text")); // NOI18N
        textFileName.setName("textFileName"); // NOI18N

        buttonBrowseSaveFile.setText(resourceMap.getString("buttonBrowseSaveFile.text")); // NOI18N
        buttonBrowseSaveFile.setName("buttonBrowseSaveFile"); // NOI18N
        buttonBrowseSaveFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonBrowseSaveFileMouseReleased(evt);
            }
        });

        checkHeaders.setText(resourceMap.getString("checkHeaders.text")); // NOI18N
        checkHeaders.setName("checkHeaders"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        buttonSaveFile.setText(resourceMap.getString("buttonSaveFile.text")); // NOI18N
        buttonSaveFile.setName("buttonSaveFile"); // NOI18N
        buttonSaveFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonSaveFileMouseReleased(evt);
            }
        });

        buttonSaveCancel.setText(resourceMap.getString("buttonSaveCancel.text")); // NOI18N
        buttonSaveCancel.setName("buttonSaveCancel"); // NOI18N
        buttonSaveCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonSaveCancelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout exportDialogLayout = new javax.swing.GroupLayout(exportDialog.getContentPane());
        exportDialog.getContentPane().setLayout(exportDialogLayout);
        exportDialogLayout.setHorizontalGroup(
            exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(exportDialogLayout.createSequentialGroup()
                        .addGroup(exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(textFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(exportDialogLayout.createSequentialGroup()
                                .addComponent(checkHeaders)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonSaveCancel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonSaveFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonBrowseSaveFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        exportDialogLayout.setVerticalGroup(
            exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportDialogLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonBrowseSaveFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(exportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkHeaders)
                    .addComponent(buttonSaveFile)
                    .addComponent(buttonSaveCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        playDialog.setMinimumSize(new java.awt.Dimension(900, 330));
        playDialog.setName("playDialog"); // NOI18N

        scaleText.setText(resourceMap.getString("scaleText.text")); // NOI18N
        scaleText.setName("scaleText"); // NOI18N

        bk50.setText(resourceMap.getString("bk50.text")); // NOI18N
        bk50.setName("bk50"); // NOI18N
        bk50.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bk50MouseReleased(evt);
            }
        });

        fw50.setText(resourceMap.getString("fw50.text")); // NOI18N
        fw50.setName("fw50"); // NOI18N
        fw50.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fw50MouseReleased(evt);
            }
        });

        fw10.setText(resourceMap.getString("fw10.text")); // NOI18N
        fw10.setName("fw10"); // NOI18N
        fw10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fw10MouseReleased(evt);
            }
        });

        bk10.setText(resourceMap.getString("bk10.text")); // NOI18N
        bk10.setName("bk10"); // NOI18N
        bk10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bk10MouseReleased(evt);
            }
        });

        bk1.setText(resourceMap.getString("bk1.text")); // NOI18N
        bk1.setName("bk1"); // NOI18N
        bk1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bk1MouseReleased(evt);
            }
        });

        fw1.setText(resourceMap.getString("fw1.text")); // NOI18N
        fw1.setName("fw1"); // NOI18N
        fw1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fw1MouseReleased(evt);
            }
        });

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        playClose.setText(resourceMap.getString("playClose.text")); // NOI18N
        playClose.setName("playClose"); // NOI18N
        playClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                playCloseMouseReleased(evt);
            }
        });

        walkScrollPane.setName("walkScrollPane"); // NOI18N

        walkGraphic.setBackground(resourceMap.getColor("walkGraphic.background")); // NOI18N
        walkGraphic.setBorder(new javax.swing.border.LineBorder(resourceMap.getColor("walkGraphic.border.lineColor"), 3, true)); // NOI18N
        walkGraphic.setName("walkGraphic"); // NOI18N
        walkGraphic.setPreferredSize(new java.awt.Dimension(2000, 400));

        javax.swing.GroupLayout walkGraphicLayout = new javax.swing.GroupLayout(walkGraphic);
        walkGraphic.setLayout(walkGraphicLayout);
        walkGraphicLayout.setHorizontalGroup(
            walkGraphicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1994, Short.MAX_VALUE)
        );
        walkGraphicLayout.setVerticalGroup(
            walkGraphicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 394, Short.MAX_VALUE)
        );

        walkScrollPane.setViewportView(walkGraphic);

        fw5.setText(resourceMap.getString("fw5.text")); // NOI18N
        fw5.setName("fw5"); // NOI18N
        fw5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fw5MouseReleased(evt);
            }
        });

        bk5.setText(resourceMap.getString("bk5.text")); // NOI18N
        bk5.setName("bk5"); // NOI18N
        bk5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                bk5MouseReleased(evt);
            }
        });

        labelLocation.setText(resourceMap.getString("labelLocation.text")); // NOI18N
        labelLocation.setName("labelLocation"); // NOI18N

        labelOnset.setText(resourceMap.getString("labelOnset.text")); // NOI18N
        labelOnset.setName("labelOnset"); // NOI18N

        labelOffset.setText(resourceMap.getString("labelOffset.text")); // NOI18N
        labelOffset.setName("labelOffset"); // NOI18N

        javax.swing.GroupLayout playDialogLayout = new javax.swing.GroupLayout(playDialog.getContentPane());
        playDialog.getContentPane().setLayout(playDialogLayout);
        playDialogLayout.setHorizontalGroup(
            playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(walkScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 917, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, playDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(bk1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fw1, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(bk5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fw5, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(bk10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fw10, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bk50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fw50))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(playDialogLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scaleText, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(124, 124, 124)
                        .addComponent(playClose))
                    .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(labelOnset)
                        .addGroup(playDialogLayout.createSequentialGroup()
                            .addComponent(labelLocation)
                            .addGap(80, 80, 80)
                            .addComponent(labelOffset))))
                .addContainerGap())
        );
        playDialogLayout.setVerticalGroup(
            playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(playDialogLayout.createSequentialGroup()
                .addComponent(walkScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(playDialogLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(playDialogLayout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(playClose))
                            .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(playDialogLayout.createSequentialGroup()
                                    .addComponent(fw5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(bk5))
                                .addGroup(playDialogLayout.createSequentialGroup()
                                    .addComponent(fw1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(bk1)))
                            .addGroup(playDialogLayout.createSequentialGroup()
                                .addComponent(fw10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bk10))
                            .addGroup(playDialogLayout.createSequentialGroup()
                                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(fw50)
                                    .addComponent(labelLocation))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(playDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(bk50)
                                    .addComponent(jLabel9)
                                    .addComponent(scaleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(playDialogLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelOnset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelOffset)))
                .addContainerGap())
        );

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        batchImportDialog.setMinimumSize(new java.awt.Dimension(468, 170));
        batchImportDialog.setName("batchImportDialog"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        batchImportFolderText.setName("batchImportFolderText"); // NOI18N
        jScrollPane4.setViewportView(batchImportFolderText);

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        batchImportBrowseButton.setText(resourceMap.getString("batchImportBrowseButton.text")); // NOI18N
        batchImportBrowseButton.setName("batchImportBrowseButton"); // NOI18N
        batchImportBrowseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                batchImportBrowseButtonMouseReleased(evt);
            }
        });

        batchImportConfirm.setText(resourceMap.getString("batchImportConfirm.text")); // NOI18N
        batchImportConfirm.setName("batchImportConfirm"); // NOI18N
        batchImportConfirm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                batchImportConfirmMouseReleased(evt);
            }
        });

        batchImportCancel.setText(resourceMap.getString("batchImportCancel.text")); // NOI18N
        batchImportCancel.setName("batchImportCancel"); // NOI18N
        batchImportCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                batchImportCancelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout batchImportDialogLayout = new javax.swing.GroupLayout(batchImportDialog.getContentPane());
        batchImportDialog.getContentPane().setLayout(batchImportDialogLayout);
        batchImportDialogLayout.setHorizontalGroup(
            batchImportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(batchImportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(batchImportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(batchImportDialogLayout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(batchImportBrowseButton))
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, batchImportDialogLayout.createSequentialGroup()
                .addContainerGap(301, Short.MAX_VALUE)
                .addComponent(batchImportCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(batchImportConfirm)
                .addContainerGap())
        );
        batchImportDialogLayout.setVerticalGroup(
            batchImportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(batchImportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addGap(9, 9, 9)
                .addGroup(batchImportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(batchImportBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(batchImportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(batchImportConfirm)
                    .addComponent(batchImportCancel))
                .addContainerGap())
        );

        editSubDialog.setMinimumSize(new java.awt.Dimension(286, 278));
        editSubDialog.setName("editSubDialog"); // NOI18N
        editSubDialog.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        editSubConfirm.setText(resourceMap.getString("editSubConfirm.text")); // NOI18N
        editSubConfirm.setName("editSubConfirm"); // NOI18N
        editSubConfirm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                editSubConfirmMouseReleased(evt);
            }
        });
        editSubDialog.getContentPane().add(editSubConfirm, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 170, -1, -1));

        editSubCancel.setText(resourceMap.getString("editSubCancel.text")); // NOI18N
        editSubCancel.setName("editSubCancel"); // NOI18N
        editSubCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                editSubCancelMouseReleased(evt);
            }
        });
        editSubDialog.getContentPane().add(editSubCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 170, -1, -1));

        editSubIDText.setText(resourceMap.getString("editSubIDText.text")); // NOI18N
        editSubIDText.setMinimumSize(new java.awt.Dimension(84, 28));
        editSubIDText.setName("editSubIDText"); // NOI18N
        editSubDialog.getContentPane().add(editSubIDText, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 100, -1));

        editSubTestText.setText(resourceMap.getString("editSubTestText.text")); // NOI18N
        editSubTestText.setMaximumSize(new java.awt.Dimension(84, 28));
        editSubTestText.setMinimumSize(new java.awt.Dimension(84, 28));
        editSubTestText.setName("editSubTestText"); // NOI18N
        editSubDialog.getContentPane().add(editSubTestText, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 60, 100, -1));

        editSubStudyText.setText(resourceMap.getString("editSubStudyText.text")); // NOI18N
        editSubStudyText.setMinimumSize(new java.awt.Dimension(84, 28));
        editSubStudyText.setName("editSubStudyText"); // NOI18N
        editSubDialog.getContentPane().add(editSubStudyText, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 100, -1));

        editSubBirthText.setText(resourceMap.getString("editSubBirthText.text")); // NOI18N
        editSubBirthText.setMinimumSize(new java.awt.Dimension(84, 28));
        editSubBirthText.setName("editSubBirthText"); // NOI18N
        editSubDialog.getContentPane().add(editSubBirthText, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, 100, -1));

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N
        editSubDialog.getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        editSubDialog.getContentPane().add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N
        editSubDialog.getContentPane().add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 40, -1, -1));

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N
        editSubDialog.getContentPane().add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 100, -1, -1));

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        editSubDialog.getContentPane().add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, -1, -1));

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void addSubButMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addSubButMouseClicked
        if(subNumText.getText().equals("") || subNumText.getText() == null) {
             JOptionPane.showMessageDialog(null, "Please set an ID for the subject"
                     , "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if(studyText.getText().equals("") || studyText.getText() == null) {
            JOptionPane.showMessageDialog(null, "Please set a study for the subject"
                     , "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if(subBdayText.getText().equals("") || subBdayText.getText() == null) {
            JOptionPane.showMessageDialog(null, "Please set a Birthdate for the subject"
                     , "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if(subTdayText.getText().equals("") || subTdayText.getText() == null) {
            JOptionPane.showMessageDialog(null, "Please set a Test Date for the subject"
                     , "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            Subject s = new Subject();
            s.setBdate(subBdayText.getText());
            s.setTdate(subTdayText.getText());
            s.setId(subNumText.getText());
            s.setStudy(studyText.getText());

            subjectList.add(s);

            subjectNumList.setListData(subjectList);
            subjectNumList.setSelectedIndex(subjectNumList.getLastVisibleIndex());
            saver = new Saver(subjectList);
            addSubject.setVisible(false);
            addSubject.setAlwaysOnTop(false);
        }
    }//GEN-LAST:event_addSubButMouseClicked

    private void addNewWalkButMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewWalkButMouseClicked
        if(subjectNumList.isSelectionEmpty() == true) {
            JOptionPane.showMessageDialog(null, "Please select a subject.", "Error", busyIconIndex);
        }
        else {
            addWalkDialog.setVisible(true);
            addWalkDialog.setAlwaysOnTop(true);
        }
    }//GEN-LAST:event_addNewWalkButMouseClicked

    private void cancelButMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButMouseClicked
        addSubject.setVisible(false);
        addSubject.setAlwaysOnTop(false);
    }//GEN-LAST:event_cancelButMouseClicked

    private void browseWalkButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_browseWalkButMouseReleased
        JFileChooser fc = new JFileChooser();
        File gaitFile;
        fc.changeToParentDirectory();
        fc.setDialogTitle("Select walk file from GaitRAW.");
        int returnVal = fc.showOpenDialog(null);

        if(returnVal == fc.APPROVE_OPTION) {
            gaitFile = fc.getSelectedFile();
            addWalkFileText.setText(gaitFile.getPath());
        }
    }//GEN-LAST:event_browseWalkButMouseReleased

    private void addWalkButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addWalkButMouseReleased
        if(addWalkFileText.getText().equals("") || addWalkFileText.getText() == null) {
            JOptionPane.showMessageDialog(null,
                    "Please select a walk file.", null, busyIconIndex);
        }
        else if(addWalkNameText.getText().equals("") || addWalkNameText.getText() == null) {
            JOptionPane.showMessageDialog(null,
                    "Please select a name for the walk.", null, busyIconIndex);
        }
        else {
            String fileName = addWalkFileText.getText();
            Subject s = (Subject)subjectNumList.getSelectedValue();
            s.addWalkFromFile(fileName, addWalkNameText.getText());
            
            if(s.walks.size() > 0) {
                walkList.setListData(s.walks);
                walkList.setSelectedIndex(walkList.getLastVisibleIndex());
            }
            else{
                walkList.setListData(new Vector());
            }
            saver = new Saver(subjectList);
            addWalkDialog.setVisible(false);
            addWalkDialog.setAlwaysOnTop(false);
        }
        
    }//GEN-LAST:event_addWalkButMouseReleased

    private void subjectNumListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_subjectNumListValueChanged
        Subject s = (Subject)subjectNumList.getSelectedValue();
        if(s.walks.size() > 0) {
            walkList.setListData(s.walks);
        }
        else
        {
            walkList.setListData(new Vector());
        }
    }//GEN-LAST:event_subjectNumListValueChanged

    private void addNewSubButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewSubButMouseReleased
        addSubject.setVisible(true);
        addSubject.setAlwaysOnTop(true);
        subjectNumList.clearSelection();
    }//GEN-LAST:event_addNewSubButMouseReleased

    private void cancelWalkButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelWalkButMouseReleased
        addWalkDialog.setVisible(false);
        addWalkDialog.setAlwaysOnTop(false);
    }//GEN-LAST:event_cancelWalkButMouseReleased

    private void delSubButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delSubButMouseReleased
        if(subjectNumList.isSelectionEmpty() == true) {
            JOptionPane.showMessageDialog(null,
                    "A subject must be selected to delete.");
        }
        else{

            int returnVal = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this subject(s)?");
            if(returnVal == JOptionPane.OK_OPTION) {
                for( Object s : subjectNumList.getSelectedValues() ) {
                    subjectList.remove((Subject)s);
                }
                //subjectList.remove((Subject)subjectNumList.getSelectedValue());
                subjectNumList.setListData(subjectList);
                saver = new Saver(subjectList);
            }
        }
    }//GEN-LAST:event_delSubButMouseReleased

    private void delWalkButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delWalkButMouseReleased
        if(walkList.isSelectionEmpty() == true) {
            JOptionPane.showMessageDialog(null,
                    "A walk must be selected to delete.");
        }
        else{
            int returnVal = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this walk?");
            if(returnVal == JOptionPane.OK_OPTION) {
                Subject s = (Subject)subjectNumList.getSelectedValue();
                s.walks.remove((Walk)walkList.getSelectedValue());
                walkList.setListData(s.walks);
                saver = new Saver(subjectList);
            }
        }
    }//GEN-LAST:event_delWalkButMouseReleased

    private void exportButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportButtonMouseClicked
        Subject toExport;
        if(subjectNumList.isSelectionEmpty() == true) {
            JOptionPane.showMessageDialog(null, "Please select a subject to export.");
        }
        else {
            saveSubject = (Subject) subjectNumList.getSelectedValue();
            exportDialog.setVisible(true);
        }

    }//GEN-LAST:event_exportButtonMouseClicked

    private void statsButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_statsButtonMouseReleased
        Walk w = (Walk) walkList.getSelectedValue();

        Subject s = (Subject) subjectNumList.getSelectedValue();
        Exporter e = new Exporter(w);

        textSubjNum.setText(s.id);
        textStudyNum.setText(s.study);
        textTestDate.setText(s.tdate);
        textBirthDate.setText(s.bdate);

        textWalkTitle.setText(w.toString());
        textDSTime.setText(String.valueOf(w.totalDoubleSupport));
        textPerDS.setText(String.valueOf((w.totalDoubleSupport/w.time) * 100));
        textFlightTime.setText(String.valueOf(w.totalFlight));
        textPerFlightTime.setText(String.valueOf((w.totalFlight / w.time) * 100));
        textSSTime.setText(String.valueOf(w.totalSingleSupport));
        textPerSS.setText(String.valueOf(w.totalSingleSupport / w.time * 100));
        textVelocity.setText(String.valueOf(w.velocity));
        textTime.setText(String.valueOf(w.time));
        textStepWid.setText(String.valueOf(w.avgStepWid));
        textStrideLen.setText(String.valueOf(w.avgStrideLen));
        textStepLen.setText(String.valueOf(w.avgStepLen));
        textNumFootFalls.setText(String.valueOf(w.walk.size()));

//        textPerDS.setVisible(false);
//        textPerSS.setVisible(false);
//        textPerFlightTime.setVisible(false);
//        labelPercentDS.setVisible(false);
//        labelPercentSS.setVisible(false);
//        labelPercentFlight.setVisible(false);

        walkStats.repaint();
        walkStats.setVisible(true);

    }//GEN-LAST:event_statsButtonMouseReleased

    private void buttonCloseMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonCloseMouseReleased
        walkStats.setVisible(false);
    }//GEN-LAST:event_buttonCloseMouseReleased

    private void buttonBrowseSaveFileMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonBrowseSaveFileMouseReleased
        JFileChooser fc = new JFileChooser();
        fc.changeToParentDirectory();

        saveSubject = (Subject) subjectNumList.getSelectedValue();


        fc.setDialogTitle("Please select where you would like to save the data.");
        int returnVal = fc.showSaveDialog(null);

        if(returnVal == fc.APPROVE_OPTION) {
            //saveFile = fc.getSelectedFile();
            textFileName.setText(fc.getSelectedFile().getAbsolutePath());
        }

    }//GEN-LAST:event_buttonBrowseSaveFileMouseReleased

    private void buttonSaveFileMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonSaveFileMouseReleased

        Exporter e = new Exporter(saveSubject);
        e.exportToMacshapa(saveSubject, new File(textFileName.getText()), checkHeaders.isSelected());

        textFileName.setText("");
        exportDialog.setVisible(false);

    }//GEN-LAST:event_buttonSaveFileMouseReleased

    private void buttonSaveCancelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonSaveCancelMouseReleased
        exportDialog.setVisible(false);
    }//GEN-LAST:event_buttonSaveCancelMouseReleased

    private void menuLoadDBMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuLoadDBMouseReleased
        Saver saver = new Saver();
        JFileChooser fc = new JFileChooser();
        int ret = fc.showOpenDialog(null);
        if(ret == fc.APPROVE_OPTION)
        {
            subjectList = saver.Loader(fc.getSelectedFile().getAbsolutePath());
            subjectNumList.setListData(subjectList);
        }
    }//GEN-LAST:event_menuLoadDBMouseReleased

    private void fw10MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fw10MouseReleased
        drawTime += 10;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_fw10MouseReleased

    private void playWalkButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playWalkButMouseReleased
        System.out.println("Showing walk...");
        walkGraphic = new DrawPanel(drawTime);
        //walkScrollPane.getHorizontalScrollBar().setValue(walkGraphic.getSize().width - 10);
        playDialog.setVisible(true);
    }//GEN-LAST:event_playWalkButMouseReleased

    private void fw1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fw1MouseReleased
        drawTime += 1;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_fw1MouseReleased

    private void bk1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bk1MouseReleased
        drawTime -= 1;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_bk1MouseReleased

    private void bk10MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bk10MouseReleased
        drawTime -= 10;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_bk10MouseReleased

    private void fw50MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fw50MouseReleased
        drawTime += 50;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_fw50MouseReleased

    private void bk50MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bk50MouseReleased
        drawTime -= 50;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_bk50MouseReleased

    private void playCloseMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playCloseMouseReleased
        drawTime = 0;
        points = new Vector<Point>();
        walkGraphic = new DrawPanel(drawTime);
        playDialog.setVisible(false);
    }//GEN-LAST:event_playCloseMouseReleased

    private void fw5MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fw5MouseReleased
        drawTime += 5;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_fw5MouseReleased

    private void bk5MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bk5MouseReleased
        drawTime -= 5;
        walkGraphic = new DrawPanel(drawTime);
        playDialog.repaint();
    }//GEN-LAST:event_bk5MouseReleased

    private void batchImportMenuItemMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_batchImportMenuItemMouseReleased
        batchImportDialog.setVisible(true);
    }//GEN-LAST:event_batchImportMenuItemMouseReleased

    private void batchImportBrowseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_batchImportBrowseButtonMouseReleased
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = fc.showOpenDialog(null);
        if(ret ==  JFileChooser.APPROVE_OPTION) {
            batchImportFolderText.setText(fc.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_batchImportBrowseButtonMouseReleased

    private void batchImportCancelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_batchImportCancelMouseReleased
        batchImportDialog.setVisible(false);
    }//GEN-LAST:event_batchImportCancelMouseReleased

    private void batchImportConfirmMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_batchImportConfirmMouseReleased
        File dir = new File(batchImportFolderText.getText());
        String[] filenames = dir.list();
        Subject sub;
        String study, id, trial, temp;
        String[] fileArray;
        ArrayList<String> errs = new ArrayList<String>(filenames.length);
        
        for (String s : filenames) {
            if(s.startsWith(".")) {
                continue;
            }
            
            // process the filename
            sub = null;
            System.out.println(s);

            fileArray = s.split("-");
            
            /* Check fileArray size to avoid oob err.
             * If there aren't at least 3 parts, add to list of bad files and continue.
             */
            if(fileArray.length < 3){
                errs.add(s);
                continue;
            }
            // extract trial number with workaround for SOM naming convention.
            trial = fileArray[fileArray.length - 1].toLowerCase().replace(".txt", "");
            if(trial.contains("_")){
                trial = trial.split("_")[0];
            }
            // extract id number
            id = fileArray[fileArray.length - 2];
            // extract study code
            study = fileArray[fileArray.length - 3];
            System.out.printf("Study: %s, ID: %s, Trial: %s\n",study,id,trial);
//            study = temp.substring(temp.length() - 2, temp.length());

//            study = s.split("#")[0].substring(0, s.split("#")[0].length()-1);
//            id = s.split("#")[1].split("-")[0];
//            trial = s.split("-")[1].toLowerCase().replace(".txt", "");
//            System.out.println(id + " " + study + " " + trial);
            for (Subject j : subjectList) {
                if (j.id.equals(id) && j.study.equals(study)) {
                    sub = j;
                    break;
                }
            }
            if (sub == null) {
                sub = new Subject(id);
                sub.study = study;
                subjectList.add(sub);
            }
            System.out.println(dir.getPath() + s);
            sub.addWalkFromFile(dir.getPath() + "/" + s, trial);
        }
        subjectNumList.setListData(subjectList);
        saver = new Saver(subjectList);
        
        // Display a dialogue message box with filenames that could not be processed.
        // TODO : we should probably check to make sure the list isn't huge before outputting it.
        int numerrs = errs.size();
        if(numerrs > 0){
            StringBuilder sb = new StringBuilder(1024);
            
            if(numerrs>10)
                sb.append("Too many (").append(numerrs).append("files)");
            else{
                for(String f : errs){
                    sb.append(f).append("\n");
                }
            }
            JOptionPane.showMessageDialog(null,
                    "Following files could not be processed:\n" + sb);
        }
        batchImportDialog.setVisible(false);
    }//GEN-LAST:event_batchImportConfirmMouseReleased

    private void editSubButMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editSubButMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_editSubButMouseClicked

    private void editSubButMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editSubButMouseReleased
        if(subjectNumList.isSelectionEmpty() == true) {
            JOptionPane.showMessageDialog(null, "A subject must be selected to edit.");
        }
        else{
            Subject sub = (Subject)subjectNumList.getSelectedValue();
            editSubIDText.setText(sub.id);
            editSubStudyText.setText(sub.study);
            editSubTestText.setText(sub.tdate);
            editSubBirthText.setText(sub.bdate);
            editSubDialog.setVisible(true);
        }
    }//GEN-LAST:event_editSubButMouseReleased

    private void editSubConfirmMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editSubConfirmMouseReleased
        Subject sub = (Subject)subjectNumList.getSelectedValue();
        sub.id = editSubIDText.getText();
        sub.bdate = editSubBirthText.getText();
        sub.tdate = editSubTestText.getText();
        sub.study = editSubStudyText.getText();
        saver = new Saver(subjectList);

        editSubDialog.setVisible(false);
    }//GEN-LAST:event_editSubConfirmMouseReleased

    private void editSubCancelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editSubCancelMouseReleased
        editSubDialog.setVisible(false);
    }//GEN-LAST:event_editSubCancelMouseReleased

    private void dumpSetButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dumpSetButtonMouseClicked
        Subject toExport;

        JFileChooser fc = new JFileChooser();
        File gaitFile;
        fc.changeToParentDirectory();
        fc.setDialogTitle("Select directory to export to:");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(null);


        String dir = "";
        if(returnVal == fc.APPROVE_OPTION) {
            dir = fc.getSelectedFile().getAbsolutePath();
            for(Subject s : subjectList) {
                File save = new File(dir + "/" + s.study + "-" + s.id + ".txt");
                Exporter e = new Exporter(s);
                e.exportToMacshapa(s, save, false);
            }
            
        }

        (new JOptionPane("Finished exporting all files.")).setVisible(true);
    }//GEN-LAST:event_dumpSetButtonMouseClicked

    private void checkButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkButtonMouseClicked
        Vector<String> flaggedWalks = new Vector<String>();

        Exporter e;
        for(Subject s : subjectList) {
            e = new Exporter(s);
            for(Walk w : s.walks) {
                int lastNum = 0;
                
                for(Footfall x : w.walk) {
                    if(x.objNum == lastNum + 1) {
                        lastNum++;
                    } else {
                        flaggedWalks.add(s.study + "-" + s.id + "-" + w.trialNum + "-"
                                + Integer.toString(x.objNum) + "-Missing steps" + "\n");
                    }
                    if(x.stepWidth < 0) {
                        flaggedWalks.add(s.study + "-" + s.id + "-" + w.trialNum + "-"
                                + Integer.toString(x.objNum) + "-Negative StepWid" + "\n");
                    }
                    if(x.stepLength < 0) {
                        flaggedWalks.add(s.study + "-" + s.id + "-" + w.trialNum + "-"
                                + Integer.toString(x.objNum) + "-Negative StepLen" + "\n");
                    }
                }
            }
        }

        JOptionPane pop = new JOptionPane();
        String c = "";

        if(flaggedWalks.isEmpty()) {
            pop.setMessage("No errors found.");
        } else {
            for(String s : flaggedWalks) {
                c += s;
            }
            pop.setSize(500, 700);
            pop.setMessage(c);
        }
        pop.setVisible(true);
        System.out.println(c);
    }//GEN-LAST:event_checkButtonMouseClicked

    private void dumpSetButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dumpSetButton1MouseClicked
        Subject toExport;

        JFileChooser fc = new JFileChooser();
        File gaitFile;
        fc.changeToParentDirectory();
        fc.setDialogTitle("Select file to export to:");
        int returnVal = fc.showSaveDialog(null);


        String file = "";
        
        if(returnVal == fc.APPROVE_OPTION) {
            file = fc.getSelectedFile().getAbsolutePath();
            try{
                FileWriter writer = new FileWriter(file, true);
                for(Subject s : subjectList) {
                    Exporter e = new Exporter(s);
                    e.exportToSingleFile(s, writer, false);
                }
                writer.close();
            } catch (Exception e) {
                (new JOptionPane("ERROR: Unable to write file (Does directory exist?)")).setVisible(true);
            }
            
        }

        JOptionPane jc = new JOptionPane("Finished exporting all files to single file.");
        jc.setVisible(true);
    }//GEN-LAST:event_dumpSetButton1MouseClicked




    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewSubBut;
    private javax.swing.JButton addNewWalkBut;
    private javax.swing.JButton addSubBut;
    private javax.swing.JDialog addSubject;
    private javax.swing.JButton addWalkBut;
    private javax.swing.JDialog addWalkDialog;
    private javax.swing.JTextField addWalkFileText;
    private javax.swing.JTextField addWalkNameText;
    private javax.swing.JButton batchImportBrowseButton;
    private javax.swing.JButton batchImportCancel;
    private javax.swing.JButton batchImportConfirm;
    private javax.swing.JDialog batchImportDialog;
    private javax.swing.JTextPane batchImportFolderText;
    private javax.swing.JMenuItem batchImportMenuItem;
    private javax.swing.JButton bk1;
    private javax.swing.JButton bk10;
    private javax.swing.JButton bk5;
    private javax.swing.JButton bk50;
    private javax.swing.JButton browseWalkBut;
    private javax.swing.JButton buttonBrowseSaveFile;
    private javax.swing.JButton buttonClose;
    private javax.swing.JButton buttonSaveCancel;
    private javax.swing.JButton buttonSaveFile;
    private javax.swing.JButton cancelBut;
    private javax.swing.JButton cancelWalkBut;
    private javax.swing.JButton checkButton;
    private javax.swing.JCheckBox checkHeaders;
    private javax.swing.JButton delSubBut;
    private javax.swing.JButton delWalkBut;
    private javax.swing.JButton dumpSetButton;
    private javax.swing.JButton dumpSetButton1;
    private javax.swing.JTextField editSubBirthText;
    private javax.swing.JButton editSubBut;
    private javax.swing.JButton editSubCancel;
    private javax.swing.JButton editSubConfirm;
    private javax.swing.JDialog editSubDialog;
    private javax.swing.JTextField editSubIDText;
    private javax.swing.JTextField editSubStudyText;
    private javax.swing.JTextField editSubTestText;
    private javax.swing.JButton exportButton;
    private javax.swing.JDialog exportDialog;
    private javax.swing.JButton fw1;
    private javax.swing.JButton fw10;
    private javax.swing.JButton fw5;
    private javax.swing.JButton fw50;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelAvgStepLen;
    private javax.swing.JLabel labelAvgStepWid;
    private javax.swing.JLabel labelAvgStrideLen;
    private javax.swing.JLabel labelBirthDate;
    private javax.swing.JLabel labelDSTime;
    private javax.swing.JLabel labelFlight;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelNumFootfalls;
    private javax.swing.JLabel labelOffset;
    private javax.swing.JLabel labelOnset;
    private javax.swing.JLabel labelPercentDS;
    private javax.swing.JLabel labelPercentFlight;
    private javax.swing.JLabel labelPercentSS;
    private javax.swing.JLabel labelSSTime;
    private javax.swing.JLabel labelStudyNum;
    private javax.swing.JLabel labelSubjectNum;
    private javax.swing.JLabel labelTestDate;
    private javax.swing.JLabel labelTime;
    private javax.swing.JLabel labelVelocity;
    private javax.swing.JLabel labelWalkTitle;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuLoadDB;
    private javax.swing.JButton playClose;
    private javax.swing.JDialog playDialog;
    private javax.swing.JButton playWalkBut;
    private javax.swing.JTextField scaleText;
    private javax.swing.JButton statsButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField studyText;
    private javax.swing.JTextField subBdayText;
    private javax.swing.JTextField subNumText;
    private javax.swing.JTextField subTdayText;
    private javax.swing.JList subjectNumList;
    private javax.swing.JTextField textBirthDate;
    private javax.swing.JTextField textDSTime;
    private javax.swing.JTextField textFileName;
    private javax.swing.JTextField textFlightTime;
    private javax.swing.JTextField textNumFootFalls;
    private javax.swing.JTextField textPerDS;
    private javax.swing.JTextField textPerFlightTime;
    private javax.swing.JTextField textPerSS;
    private javax.swing.JTextField textSSTime;
    private javax.swing.JTextField textStepLen;
    private javax.swing.JTextField textStepWid;
    private javax.swing.JTextField textStrideLen;
    private javax.swing.JTextField textStudyNum;
    private javax.swing.JTextField textSubjNum;
    private javax.swing.JTextField textTestDate;
    private javax.swing.JTextField textTime;
    private javax.swing.JTextField textVelocity;
    private javax.swing.JTextField textWalkTitle;
    private javax.swing.JPanel walkGraphic;
    private javax.swing.JList walkList;
    private javax.swing.JScrollPane walkScrollPane;
    private javax.swing.JFrame walkStats;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    File saveFile;
    Subject saveSubject;
}

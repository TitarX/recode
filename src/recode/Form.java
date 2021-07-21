/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Form.java
 *
 * Created on Sep 25, 2011, 7:53:58 PM
 */
package recode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author TitarX
 */
public class Form extends javax.swing.JFrame
{

    JPopupMenu resultPopupMenu=null;
    JMenuItem cleanMenuItem=null;
    String currentFolderPath=null;
    File currentFolder=null;

    /** Creates new form Form */
    public Form()
    {
        initComponents();
        initComponents2();
    }
    private DocumentListener documentListener=new DocumentListener()
    {

        @Override
        public void insertUpdate(DocumentEvent e)
        {
            setEnabledStartButton(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
            setEnabledStartButton(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
            setEnabledStartButton(e);
        }
    };

    private void initComponents2()
    {
        Map<String,Charset> mapCharset=Charset.availableCharsets();
        Set<String> namesCharset=mapCharset.keySet();
        Iterator iterator=namesCharset.iterator();
        while(iterator.hasNext())
        {
            encodingComboBox.addItem(iterator.next());
        }
        encodingComboBox.setMaximumRowCount(8);
        encodingComboBox.setSelectedItem(Charset.forName(System.getProperty("sun.jnu.encoding")).name());

        folderTextField.getDocument().addDocumentListener(documentListener);
        findTextField.getDocument().addDocumentListener(documentListener);

        resultTextPane.setContentType("text/html");

        resultPopupMenu=new JPopupMenu();
        cleanMenuItem=new JMenuItem("Clean");
        cleanMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                cleanMenuItemActionPerformed(actionEvent);
            }
        });
        resultPopupMenu.add(cleanMenuItem);
        resultTextPane.setComponentPopupMenu(resultPopupMenu);
    }

    private void start()
    {
        if(currentFolder.exists()&&currentFolder.isDirectory()&&typeComboBox.isEnabled()
                &&encodingComboBox.getSelectedItem()!=null&&typeComboBox.getSelectedItem()!=null)
        {
            ArrayList<File> files=new ArrayList<File>();
            getFiles(currentFolder,files);
            if(files.isEmpty())
            {
                resultTextPane.setText("<center><h2 style='color:#FF0000;'>Error</h2><div>This folder contains no files of specified type</div></center>");
            }
            else
            {
                Iterator<File> iterator=files.iterator();
                InputStreamReader reader=null;
                OutputStreamWriter writer=null;
                try
                {
                    String encoding=encodingComboBox.getSelectedItem().toString();
                    String findString=findTextField.getText();
                    String replaceString=replaceTextField.getText();
                    int mode=0;
                    if(ignoreCaseCheckBox.isSelected())
                    {
                        mode|=Pattern.CASE_INSENSITIVE;
                        mode|=Pattern.UNICODE_CASE;
                    }
                    if(plainTextCheckBox.isSelected())
                    {
                        mode|=Pattern.LITERAL;
                        replaceString=Matcher.quoteReplacement(replaceString);
                    }
                    if(lineCheckBox.isSelected())
                    {
                        mode|=Pattern.MULTILINE;
                    }
                    Pattern pattern=Pattern.compile(findString,mode);
                    StringBuilder reportBuilder=new StringBuilder();
                    reportBuilder.append("<center><h2 style='color:#008000;'>Successfully</h2>");
                    boolean isMatches=false;
                    while(iterator.hasNext())
                    {
                        File file=iterator.next();
                        StringBuilder contentBuilder=new StringBuilder();
                        reader=new InputStreamReader(new FileInputStream(file),encoding);
                        while(reader.ready())
                        {
                            contentBuilder.append((char)reader.read());
                        }
                        reader.close();
                        String content=contentBuilder.toString();

                        Matcher matcher=pattern.matcher(content);
                        String changedContent=matcher.replaceAll(replaceString);
                        String lineNumbers=getLineNumbers(matcher.reset(),content);
                        if(!lineNumbers.equals(""))
                        {
                            reportBuilder.append("<div style='background-color:#8080FF;'>");
                            reportBuilder.append("<div style='background-color:#FFFACD;margin:5;'>");
                            reportBuilder.append("<h3>File:</h3>");
                            reportBuilder.append(file.getAbsolutePath());
                            reportBuilder.append("<hr size='1' color='#000000' />");
                            reportBuilder.append("<h3>Matches are found in lines:</h3>");
                            reportBuilder.append(lineNumbers);
                            reportBuilder.append("</div>");
                            reportBuilder.append("</div>");
                            reportBuilder.append("<br />");
                            isMatches=true;
                        }

                        writer=new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(),false),encoding);
                        writer.write(changedContent);
                        writer.close();
                    }
                    if(!isMatches)
                    {
                        reportBuilder.append("<div>No match</div>");
                    }
                    reportBuilder.append("</center>");
                    resultTextPane.setText(reportBuilder.toString());
                }
                catch(Exception ex)
                {
                    resultTextPane.setText("<center><h2 style='color:#FF0000;'>Error</h2><div>"+ex.getMessage()+"</div></center>");
                    Logger.getLogger(Form.class.getName()).log(Level.SEVERE,null,ex);
                }
                finally
                {
                    try
                    {
                        if(reader!=null)
                        {
                            reader.close();
                        }
                        if(writer!=null)
                        {
                            writer.close();
                        }
                    }
                    catch(IOException ex)
                    {
                        Logger.getLogger(Form.class.getName()).log(Level.SEVERE,null,ex);
                    }
                }
            }
        }
    }

    private String getLineNumbers(Matcher matcher,String content)
    {
        StringBuilder reportBuilder=new StringBuilder();
        Matcher matcherForLineCount=Pattern.compile("\\n").matcher(content);
        while(matcher.find())
        {
            if(reportBuilder.length()>0)
            {
                reportBuilder.append(", ");
            }
            int lineCount=1;
            matcherForLineCount.region(0,matcher.start());
            while(matcherForLineCount.find())
            {
                lineCount++;
            }
            reportBuilder.append(lineCount);
        }
        return reportBuilder.toString();
    }

    private void getFiles(File directory,ArrayList<File> files)
    {
        String neededFileType=typeComboBox.getSelectedItem().toString();
        if(neededFileType.equals("simple file"))
        {
            neededFileType="";
        }
        if(directory.exists()&&directory.isDirectory())
        {
            File[] listFiles=directory.listFiles();
            for(File file:listFiles)
            {
                if(file.isDirectory())
                {
                    getFiles(file,files);
                }
                else
                {
                    String fileName=file.getName();
                    String fileType=fileName.substring(getPointIndex(fileName));
                    if(fileType.equalsIgnoreCase(neededFileType))
                    {
                        files.add(file);
                    }
                }
            }
        }
    }

    private void getTypeFiles(File directory,ArrayList<String> listTypeFiles)
    {
        if(directory.exists()&&directory.isDirectory())
        {
            File[] listFiles=directory.listFiles();
            for(File file:listFiles)
            {
                if(file.isDirectory())
                {
                    getTypeFiles(file,listTypeFiles);
                }
                else
                {
                    String fileName=file.getName();
                    String fileType=fileName.substring(getPointIndex(fileName));
                    if(fileType.equals(""))
                    {
                        fileType="simple file";
                    }
                    boolean b=true;
                    Iterator iterator=listTypeFiles.iterator();
                    fileType=fileType.toLowerCase();
                    while(iterator.hasNext())
                    {
                        if(iterator.next().equals(fileType))
                        {
                            b=false;
                            break;
                        }
                    }
                    if(b)
                    {
                        listTypeFiles.add(fileType);
                    }
                }
            }
        }
    }

    private int getPointIndex(String str)
    {
        char[] chars=str.toCharArray();
        for(int i=chars.length-1;i>=0;i--)
        {
            if(chars[i]=='.')
            {
                return i;
            }
        }
        return chars.length;
    }

    private void setEnabledStartButton(DocumentEvent e)
    {
        if(folderTextField.getText().equals("")||findTextField.getText().equals(""))
        {
            startButton.setEnabled(false);
        }
        else
        {
            startButton.setEnabled(true);
        }

        if(e.getDocument().equals(folderTextField.getDocument()))
        {
            typeComboBox.removeAllItems();
            resultTextPane.setText("");
        }
    }

    private void prepareDirectory()
    {
        currentFolder=new File(currentFolderPath);
        if(currentFolder.exists()&&currentFolder.isDirectory())
        {
            ArrayList<String> listTypeFiles=new ArrayList<String>();
            getTypeFiles(currentFolder,listTypeFiles);
            Sorting sorting=new Sorting();
            sorting.sort(Sorting.ASC,listTypeFiles);
            Iterator iterator=listTypeFiles.iterator();
            typeComboBox.removeAllItems();
            while(iterator.hasNext())
            {
                typeComboBox.addItem(iterator.next());
            }
        }
        else
        {
            folderTextField.setText("");
            typeComboBox.removeAllItems();
        }
    }

    private void cleanMenuItemActionPerformed(ActionEvent actionEvent)
    {
        resultTextPane.setText("");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        folderLable = new javax.swing.JLabel();
        encodingLable = new javax.swing.JLabel();
        typeLable = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        findLable = new javax.swing.JLabel();
        replaceLable = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        startButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultTextPane = new javax.swing.JTextPane();
        folderTextField = new javax.swing.JTextField();
        replaceTextField = new javax.swing.JTextField();
        findTextField = new javax.swing.JTextField();
        typeComboBox = new javax.swing.JComboBox();
        encodingComboBox = new javax.swing.JComboBox();
        selectButton = new javax.swing.JButton();
        ignoreCaseCheckBox = new javax.swing.JCheckBox();
        plainTextCheckBox = new javax.swing.JCheckBox();
        lineCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        folderLable.setText("Folder:");

        encodingLable.setText("Encoding:");

        typeLable.setText("Type:");

        findLable.setText("Find:");

        replaceLable.setText("Replace:");

        startButton.setText("Start");
        startButton.setEnabled(false);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        resultTextPane.setEditable(false);
        jScrollPane1.setViewportView(resultTextPane);

        folderTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                folderTextFieldKeyPressed(evt);
            }
        });

        replaceTextField.setToolTipText("Regex Java");

        findTextField.setToolTipText("Regex Java");

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        ignoreCaseCheckBox.setText("Ignore case");
        ignoreCaseCheckBox.setToolTipText("UNIX_LINE and CASE_INSENSITIVE");

        plainTextCheckBox.setText("Plain text");
        plainTextCheckBox.setToolTipText("LITERAL");
        plainTextCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                plainTextCheckBoxItemStateChanged(evt);
            }
        });

        lineCheckBox.setText("^$ - Line");
        lineCheckBox.setToolTipText("MULTILINE");
        lineCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lineCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(encodingLable)
                    .addComponent(typeLable)
                    .addComponent(folderLable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(encodingComboBox, 0, 490, Short.MAX_VALUE)
                    .addComponent(folderTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 490, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectButton)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(exitButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 508, Short.MAX_VALUE)
                .addComponent(startButton)
                .addContainerGap())
            .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(replaceLable)
                    .addComponent(findLable))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ignoreCaseCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(plainTextCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(lineCheckBox))
                    .addComponent(findTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(replaceTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLable)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingLable)
                    .addComponent(encodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeLable)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoreCaseCheckBox)
                    .addComponent(plainTextCheckBox)
                    .addComponent(lineCheckBox))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(findLable)
                    .addComponent(findTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(replaceLable)
                    .addComponent(replaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(exitButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitButtonActionPerformed
    {//GEN-HEADEREND:event_exitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startButtonActionPerformed
    {//GEN-HEADEREND:event_startButtonActionPerformed
        start();
    }//GEN-LAST:event_startButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectButtonActionPerformed
    {//GEN-HEADEREND:event_selectButtonActionPerformed
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
        {
            currentFolderPath=fileChooser.getSelectedFile().getPath();
            folderTextField.setText(currentFolderPath);
            prepareDirectory();
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void folderTextFieldKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_folderTextFieldKeyPressed
    {//GEN-HEADEREND:event_folderTextFieldKeyPressed
        int key=evt.getKeyCode();
        if(key==KeyEvent.VK_ENTER)
        {
            currentFolderPath=folderTextField.getText();
            prepareDirectory();
        }
    }//GEN-LAST:event_folderTextFieldKeyPressed

    private void plainTextCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_plainTextCheckBoxItemStateChanged
    {//GEN-HEADEREND:event_plainTextCheckBoxItemStateChanged
        if(plainTextCheckBox.isSelected())
        {
            lineCheckBox.setEnabled(false);
        }
        else
        {
            lineCheckBox.setEnabled(true);
        }
    }//GEN-LAST:event_plainTextCheckBoxItemStateChanged

    private void lineCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_lineCheckBoxItemStateChanged
    {//GEN-HEADEREND:event_lineCheckBoxItemStateChanged
        if(lineCheckBox.isSelected())
        {
            plainTextCheckBox.setEnabled(false);
        }
        else
        {
            plainTextCheckBox.setEnabled(true);
        }
    }//GEN-LAST:event_lineCheckBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel encodingLable;
    private javax.swing.JButton exitButton;
    private javax.swing.JLabel findLable;
    private javax.swing.JTextField findTextField;
    private javax.swing.JLabel folderLable;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JCheckBox ignoreCaseCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JCheckBox lineCheckBox;
    private javax.swing.JCheckBox plainTextCheckBox;
    private javax.swing.JLabel replaceLable;
    private javax.swing.JTextField replaceTextField;
    private javax.swing.JTextPane resultTextPane;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton startButton;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLable;
    // End of variables declaration//GEN-END:variables
}

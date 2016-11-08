package InterpreterPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class InterpreterGUI extends JApplet implements ActionListener,MouseListener{

    private JButton fileButton;
    private JTextField textField;
    private JLabel sourceLabel;
    private JLabel resultLabel;

    public void init(){

        textField = new JTextField("Absolute Path for source code file...");
        textField.setBounds(this.getWidth()/2 - 100, 10, 200, 20);

        String buttonString = "Interpret Bare Python source";

        fileButton = new JButton(buttonString);
        fileButton.setBounds(this.getWidth()/2 - 115, 40, 230,30);

        sourceLabel = new JLabel("<html>Bare Python Source Code:");
        sourceLabel.setOpaque(true);
        sourceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        sourceLabel.setVerticalAlignment(SwingConstants.TOP);
        sourceLabel.setBackground(Color.white);

        resultLabel = new JLabel("<html>Final state of all variables:</html>");
        resultLabel.setOpaque(true);
        resultLabel.setHorizontalAlignment(SwingConstants.LEFT);
        resultLabel.setVerticalAlignment(SwingConstants.TOP);
        resultLabel.setBackground(Color.white);

        JScrollPane sourceScrollPane = new JScrollPane(sourceLabel);
        sourceScrollPane.setOpaque(true);
        sourceScrollPane.setVisible(true);

        JScrollPane resultScrollPane = new JScrollPane(resultLabel);
        resultScrollPane.setOpaque(true);
        resultScrollPane.setVisible(true);

        sourceScrollPane.setBounds(20, 80, 260, this.getHeight() - 90);
        resultScrollPane.setBounds(this.getWidth() - 20 - 260, 80, 250, this.getHeight() - 90);

        add(sourceScrollPane);
        add(resultScrollPane);
        add(fileButton);
        add(textField);
        fileButton.addActionListener(this);
        textField.addMouseListener(this);

        setLayout(null);
    }

    public void actionPerformed(ActionEvent e){
        //textField.setEnabled(false);
        String sourceFile = textField.getText();
        String source= "<html>Bare Python Source Code:<br>";
        try {
            BufferedReader input = new BufferedReader(new FileReader(sourceFile));
            String line = input.readLine();
            while (line != null){
                line = line.replace("      ", "......");
                line = line.replace("   ", "...");

                source += line + "<br>";
                line = input.readLine();
            }
            source += "</html>";
            sourceLabel.setText(source);

            Interpreter interpreter = new Interpreter(sourceFile);
            resultLabel.setText("<html> Final state of all variables:<br>" + interpreter.readSource().replaceAll("  ","<br>") + "</html>");
        }
        catch(Exception error){
            sourceLabel.setText("<html>File not found or <br>the syntax in the source code is not correct</html>");
        }

    }

    public void mouseClicked(MouseEvent e){
        textField.setText("");
    }
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){
        if(textField.getText().equals("")){
            textField.setText("Absolute Path for source code file...");
        }
    }
}
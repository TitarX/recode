/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recode;

import java.awt.EventQueue;
import javax.swing.JFrame;

/**
 *
 * @author TitarX
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                Form f=new Form();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setTitle("Recode");
                f.setSize(800,600);
                f.setVisible(true);
                f.setLocationRelativeTo(null);
            }
        });
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gaitmatcrunch;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.Vector;

/**
 *
 * @author j4lingeman
 */
public class Saver {
    String file = "./gaitCrunchDatabase.db";
    String bkFile = "./gaitCrunchDatabase.db.bak";

    public Saver()
    {
        
    }

    public Saver(Vector<Subject> subjects) {
        ObjectOutputStream objOut;
        try{
            objOut = new ObjectOutputStream(new FileOutputStream(file));

            for(Subject s : subjects) {
                objOut.writeObject(s);
            }
        }
        catch(Exception e){
            
        }
    }

    public Vector<Subject> Loader(String fileName) {
        Vector<Subject> subjects = new Vector<Subject>();
        //fileName = file;
        try{
            Object o = null;
            ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(fileName));

            while((o = objIn.readObject()) != null) {
                if(o instanceof Subject)
                    subjects.add((Subject) o);
            }
            objIn.close();
            return subjects;

        }
         catch (EOFException eof)
            {
            System.out.println("eof encountered" + eof.getMessage());
            }
          catch (OptionalDataException ode)
            {
            System.out.println("OptionalDataException" + ode.getMessage());
            }
          catch (IOException ioe)
            {
            System.out.println("IOException on read object");
            System.out.println(ioe.getMessage());
            System.out.println(ioe.toString());
            }
          catch (ClassNotFoundException cnf)
            {
            System.out.println("ClassNotFoundException");
            }

         finally{
             return subjects;
         }
            }
}

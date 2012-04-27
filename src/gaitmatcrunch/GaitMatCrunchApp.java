/*
 * GaitMatCrunchApp.java
 */

package gaitmatcrunch;

import java.util.Vector;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class GaitMatCrunchApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new GaitMatCrunchView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of GaitMatCrunchApp
     */
    public static GaitMatCrunchApp getApplication() {
        return Application.getInstance(GaitMatCrunchApp.class);
    }

    /**
     * Main method launching the application.
     */

    public Vector<Subject> mainSubjectList = new Vector<Subject>();

    public static void main(String[] args) {
        launch(GaitMatCrunchApp.class, args);
    }


}

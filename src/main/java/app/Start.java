package app;

import app.Database.DBConnect;

public class Start {
    public static void main(String[] args) {
        //Создание и отображение GUI
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new AppWindow();
                }
            });
    }
}

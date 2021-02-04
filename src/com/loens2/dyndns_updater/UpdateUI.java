package com.loens2.dyndns_updater;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class UpdateUI {
    private JTextField username;
    private JPasswordField password;
    private JTextField ip;
    private JButton update_button;
    private JTextField hostname;
    private JPanel main;
    private JLabel label_username;
    private JLabel label_password;
    private JLabel label_hostname;
    private JLabel label_ip;
    private JLabel label_status;
    private JButton load_button;
    private JButton save_button;
    private static UpdateUI updateUI = new UpdateUI();

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }



    public static void main(String[] args) {

        // Initializes Frame

        JFrame frame = new JFrame("DynDNS Updater");
        //UpdateUI updateUI = new UpdateUI();
        frame.setContentPane(updateUI.main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setLocationRelativeTo(null);

        updateUI.label_status.setVisible(false);

        runActions(updateUI);

        }

    private static volatile boolean timedOut = false;

    private static void runActions(UpdateUI updateUI) {

        updateUI.save_button.addActionListener((event) -> {
            if (!(updateUI.username.getText().isEmpty() || updateUI.password.getText().isEmpty() || updateUI.hostname.getText().isEmpty())) {
                String content = updateUI.username.getText()+"\n"+new String(updateUI.password.getPassword())+"\n"+updateUI.hostname.getText();
                writeToFile("credentials.txt",content);
                setStatus("Saved!");
            } else {
                System.out.println("enter");
                setStatus("Please fill in all required data!");
            }
        });

        updateUI.load_button.addActionListener((event) -> {
            String content = null;
            try {
                 content = readFromFile("credentials.txt");
                 if (content.isEmpty()) {
                     setStatus("Nothing saved!");
                 }
            } catch (IOException e) {
                e.printStackTrace();
                setStatus("Nothing saved!");
            }
            String[] splitContent = content.split("\n");
            updateUI.username.setText(splitContent[0]);
            updateUI.password.setText(splitContent[1]);
            updateUI.hostname.setText(splitContent[2]);
        });


        // Activation via Button Press

        updateUI.update_button.addActionListener((event) -> {
            String ip = null;

            if (updateUI.username.getText().isEmpty() || updateUI.password.getText().isEmpty() || updateUI.hostname.getText().isEmpty()) {
                setStatus("Please fill in all required data!");
                return;
            }

            if (updateUI.ip.getText().length() != 0) {
                ip = updateUI.ip.getText();
            } else if (updateUI.ip.getText().length() == 0) {

                // Gets public ip address if none is specified in the GUI
                String systemipaddress = "";
                try {
                    URL url_name = new URL("http://bot.whatismyipaddress.com");
                    BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
                    systemipaddress = sc.readLine().trim();
                } catch (Exception e) {
                    systemipaddress = "Cannot Execute Properly";
                }
                ip = systemipaddress;
            }

            // DynDNS update gets initialized

            URL url1 = null;
            try {
                url1 = new URL("http://dyndns.strato.com/nic/update?hostname="+ updateUI.hostname.getText()+"&myip="+ip);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            URLConnection uc = null;
            try {
                uc = url1.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // DynDNS user gets authorized

            String userpass = updateUI.username.getText() + ":" + updateUI.password.getPassword();
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());

            uc.setRequestProperty("Authorization", basicAuth);
            InputStream in = null;
            try {
                in = uc.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            // read site content

            String content = null;

            while (true) {
                try {
                    if (!((content = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Debug info

                System.out.println(content);
                if (content.matches("(.*)nochg(.*)") || content.matches("(.*)good(.*)")) {
                    setStatus("Done!");

                } else {
                    setStatus("Failure...");
                }

            }

        });

    }

    public static void writeToFile(String path, String contents) {
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String path) throws IOException {
        File file = new File(path);
        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[2 * 1024];
            reader.read(buffer);
            return new String(buffer).trim();
        }
    }

    public static void setStatus(String text) {
        updateUI.label_status.setText(text);
        updateUI.label_status.setVisible(true);
        new Thread("Timeout") {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timedOut = true;
                updateUI.label_status.setVisible(false);
            }
        }.start();
    }
}
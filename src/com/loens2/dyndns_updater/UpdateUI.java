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
    private JTextField hostname;
    private JPanel main;
    private JLabel label_username;
    private JLabel label_password;
    private JLabel label_hostname;
    private JLabel label_status;
    private JButton save_button;
    private static JFrame frame = new JFrame("DynDNS Updater");
    public static boolean firstTimeSetup = true;
    private static String systemipaddress = "";


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main(String[] args) {

        // Initializes UpdateUI

        UpdateUI updateUI = new UpdateUI();

        // Reads saved file
        String content = null;
        try {
            content = readFromFile("credentials.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null && !(content.equals(""))) {
            String[] splitContent = content.split("\n");
            firstTimeSetup = Boolean.parseBoolean(splitContent[0]);
            updateUI.username.setText(splitContent[1]);
            updateUI.password.setText(splitContent[2]);
            updateUI.hostname.setText(splitContent[3]);
        }

        System.out.println(firstTimeSetup);

        // Initializes Frame (First Time Setup)

        if (firstTimeSetup) {
            updateUI.initializeFrame();
        }

        runActions(updateUI);

    }

    private static volatile boolean timedOut = false;

    private void initializeFrame () {
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        label_status.setVisible(false);
    }

    private static void runActions(UpdateUI updateUI) {

        updateUI.save_button.addActionListener((event) -> {
            if (updateUI.username.getText() != null) {
                String content = "false" + "\n" + updateUI.username.getText() + "\n" + new String(updateUI.password.getPassword()) + "\n" + updateUI.hostname.getText();
                writeToFile("credentials.txt", content);
                firstTimeSetup = false;
                updateUI.label_status.setText("Saved!");
                updateUI.label_status.setVisible(true);
                new Thread("pause") {
                    public void run () {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                updateUI.label_status.setVisible(false);
                frame.setVisible(false);
                main(new String[]{});
            }
        });


        if (firstTimeSetup) return;


        try {
            URL url_name = new URL("http://ipv4bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            systemipaddress = "Cannot Execute Properly";
        }

        URL url = null;
        try {
            url = new URL("https://dyndns.strato.com/nic/update/nic/checkip.html");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        URLConnection uc = null;
        try {
            uc = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // DynDNS user gets authorized

        String userpass = updateUI.username.getText() + ":" + new String(updateUI.password.getPassword());
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

            System.out.println(systemipaddress);
            if (content.matches("(.*)"+systemipaddress+"(.*)")) {
                System.out.println("true");
                restarter();
            } else {
                updateUI.update();
            }
        }

    }

    public void update () {
        loop:
        do {


            URL url1 = null;
            try {
                url1 = new URL("http://dyndns.strato.com/nic/update?hostname=" + hostname.getText() + "&myip=" + systemipaddress);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            URLConnection uc1 = null;
            try {
                uc1 = url1.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // DynDNS user gets authorized

            String userpass = username.getText() + ":" + new String(password.getPassword());
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());

            uc1.setRequestProperty("Authorization", basicAuth);
            InputStream in1 = null;
            try {
                in1 = uc1.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in1));

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
                    System.out.println("Done");
                    restarter();
                } else {
                    System.out.println("Help");
                    initializeFrame();
                    frame.setVisible(true);
                    label_status.setText("Failure! Check credentials!");
                    label_status.setVisible(true);
                    continue loop;
                }

            }
        } while (false);
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

    public static void restarter () {

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }main(new String[] {});
    }
}



package com.loens2.dyndns_updater;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }



    public static void main(String[] args) {

        // Initializes Frame

        JFrame frame = new JFrame("DynDNS Updater");
        UpdateUI updateUI = new UpdateUI();
        frame.setContentPane(updateUI.main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setLocationRelativeTo(null);
        runActions(updateUI);

        }

    private static void runActions(UpdateUI updateUI) {

        // Activation via Button Press

        updateUI.update_button.addActionListener((event) -> {
            String ip = null;
            if (updateUI.ip.getText().length() != 0) {
                ip = updateUI.ip.getText();
                System.out.println(ip);
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

            String userpass = updateUI.username.getText() + ":" + updateUI.password.getText();
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

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
                if (content.matches("(.*)nochg(.*)") == true) {
                    System.out.println("Done");
                } else {
                    System.out.println("Help");
                }

            }

        });
    }
}
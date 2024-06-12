package org.iono_tech.Gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthenticationGUI extends JFrame {

    String apiEndpoint = "http://demo-milestone/IDP/connect/token";

    private JComboBox<String> authenticationType;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel domainLabel;
    private JTextField domainField;
    private JLabel workstationLabel;
    private JTextField workstationField;
    private JButton submitButton;
    private JLabel ipLabel; // New IP address label
    private JTextField ipField; // New IP address field
    //  [for Basic user] grant_type=password&username=[basicuser]&password=[password]&client_id=GrantValidatorClient
    private String tokenExtractor(HttpPost httppost, CloseableHttpClient httpclient) throws IOException {
        String token="";
        System.out.println("Executing request " + httppost.getRequestLine());
        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            HttpEntity responseEntity = response.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status Code: " + statusCode);
            if (responseEntity != null && statusCode==200) {
                String responseString = new String(responseEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> resultMap = mapper.readValue(responseString, new TypeReference<Map<String, Object>>(){});
                token = "Bearer "+resultMap.get("access_token");
                System.out.println("Token: " + token);

            }
            else {
            JOptionPane.showMessageDialog(this, "Failed to fetch data due: " + new String(responseEntity.getContent().readAllBytes(), StandardCharsets.UTF_8), "Error", JOptionPane.ERROR_MESSAGE);

            }
        }
        return token;
    }
    private String basicLogin(){
        String token="";
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(usernameField.getText(), new String(passwordField.getPassword()))
        );

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {

            HttpPost httppost = new HttpPost(apiEndpoint);
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            HttpEntity entity = EntityBuilder.create()
                    .setText("grant_type=password&username="+usernameField.getText()+"&password="+new String(passwordField.getPassword())+"&client_id=GrantValidatorClient")
                    .setContentType(ContentType.TEXT_PLAIN)
                    .build();

            httppost.setEntity(entity);

            token=tokenExtractor(httppost,httpclient);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }


    private String ntlmLogin(){
        String token="";
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new NTCredentials(usernameField.getText(), new String(passwordField.getPassword()), workstationField.getText(), domainField.getText())
        );

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {

            HttpPost httppost = new HttpPost(apiEndpoint);
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            HttpEntity entity = EntityBuilder.create()
                    .setText("grant_type=windows_credentials&client_id=GrantValidatorClient")
                    .setContentType(ContentType.TEXT_PLAIN)
                    .build();

            httppost.setEntity(entity);


            token=tokenExtractor(httppost,httpclient);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    public AuthenticationGUI() {
        // Set up the JFrame
        setTitle("Authentication Configuration");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2, 10, 10));
        setResizable(false);

        // Create UI components
        authenticationType = new JComboBox<>();
        authenticationType.addItem("Basic Authentication");
        authenticationType.addItem("Windows NTLM");

        usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        domainLabel = new JLabel("Domain:");
        domainField = new JTextField();
        workstationLabel = new JLabel("Workstation:");
        workstationField = new JTextField();
        ipLabel = new JLabel("IP Address:"); // New label for IP address
        ipField = new JTextField(); // New field for IP address

        submitButton = new JButton("Submit");

        // Add components to the JFrame
        add(new JLabel("Authentication Type:"));
        add(authenticationType);
        add(ipLabel); // Add IP address label
        add(ipField); // Add IP address field
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(domainLabel);
        add(domainField);
        add(workstationLabel);
        add(workstationField);

        add(new JLabel()); // Placeholder for alignment
        add(submitButton);
        domainLabel.setVisible(false);
        domainField.setVisible(false);
        workstationLabel.setVisible(false);
        workstationField.setVisible(false);

        // Event handling
        authenticationType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) authenticationType.getSelectedItem();
                if (selected.equals("Windows NTLM")) {
                    domainLabel.setVisible(true);
                    domainField.setVisible(true);
                    workstationLabel.setVisible(true);
                    workstationField.setVisible(true);
                } else {
                    domainLabel.setVisible(false);
                    domainField.setVisible(false);
                    workstationLabel.setVisible(false);
                    workstationField.setVisible(false);
                }
            }
        });

        // Action listener for the submit button (not implemented in this example)
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) authenticationType.getSelectedItem();
                apiEndpoint="http://"+ipField.getText()+"/IDP/connect/token";
                // Implement authentication logic here
                String token;
                if (selected!=null&&selected.equals("Windows NTLM"))
                    token=ntlmLogin();
                else
                    token=basicLogin();
                if(token!=null && !token.isEmpty()){
                new CameraListGui(token,AuthenticationGUI.this,ipField.getText());
                AuthenticationGUI.this.setVisible(false);
                }

                System.out.println("Submit button clicked!");
            }
        });
    }


}

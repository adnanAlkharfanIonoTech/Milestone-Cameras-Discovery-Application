package org.iono_tech;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CameraListGui extends JFrame {
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JButton refreshButton;
    private final String token;
    private final AuthenticationGUI authenticationGUI;

    CameraListGui(String token, AuthenticationGUI authenticationGUI) {
        this.token = token;
        this.authenticationGUI = authenticationGUI;
        this.setVisible(true);
        setTitle("Data Display");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create data table with columns
        tableModel = new DefaultTableModel(new Object[]{"displayName", "name", "id"}, 0);
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);
        refreshData();

        // Create refresh button
        refreshButton = new JButton("Refresh Data");
        add(refreshButton, BorderLayout.SOUTH);

        // Event handling for the refresh button
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
    }

    private void refreshData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Fetch new data and add to table
        fetchDataAndPopulateTable();
    }

    private static Camera mapToCamera(Map<String, Object> cameraMap) {
        Camera camera = new Camera();
        camera.setDisplayName((String) cameraMap.get("displayName"));
        camera.setName((String) cameraMap.get("name"));
        camera.setId((String) cameraMap.get("id"));
        // Set other fields similarly
        return camera;
    }

    private void fetchDataAndPopulateTable() {

        // URL to fetch data from
        String urlString = "http://demo-milestone/api/rest/v1/cameras";

try{
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Authorization", token);

        // Get response code
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseString = response.toString();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> responseMap = mapper.readValue(responseString, new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            List<Map<String, Object>> cameraList = responseMap.get("array");

            List<Camera> cameras = new ArrayList<>();
            for (Map<String, Object> cameraMap : cameraList) {
                Camera camera = mapToCamera(cameraMap);
                cameras.add(camera);
            }

            // Clear existing data in the table
            tableModel.setRowCount(0);

            // Add data to table
            for (Camera camera : cameras) {
                String[] fields = {camera.getDisplayName(), camera.getName(), camera.getId()};
                tableModel.addRow(fields);
            }
            System.out.println("table has been refreshed............");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to fetch data: " + responseCode, "Error", JOptionPane.ERROR_MESSAGE);
            authenticationGUI.setVisible(true);
            this.setVisible(false);

        }

        connection.disconnect();

    } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch data due: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            authenticationGUI.setVisible(true);
            this.setVisible(false);

        }
    }
}

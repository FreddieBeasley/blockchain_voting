package util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;

public class FileHandlingUtils {

    // TXT methods
    /*
    public static boolean removeFromTXTFile(String filePath, String lineToRemove) {
        boolean result = true;

        // define file paths
        File originalFile = new File(filePath);
        File tempFile = new File(filePath + ".temp");

        try (
                // Open original file for reading and temp file for writing ( with resources to ensure proper closer )
                BufferedReader reader = new BufferedReader(new FileReader(originalFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {

            // Transfer lines that should not be removed
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if (!trimmedLine.equals(lineToRemove)) {
                    writer.write(trimmedLine);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error while trying to read file: " + originalFile + " to " + tempFile + ": " + e.getMessage());
            result = false;
        }

        try {

            // Delete original file
            Files.delete(originalFile.toPath());

            // Rename temp file
            Files.move(tempFile.toPath(), originalFile.toPath());

            // Log
            System.out.println("Line: " + lineToRemove + " no longer exists in file: " + filePath);

        } catch (IOException e) {
            System.out.println("Error while trying to delete file: " + originalFile + " and rename " + tempFile + ": " + e.getMessage());
            result = false;
        }

        return result;
    }

    public static boolean AppendToTXTFile(String filePath, String lineToAppend) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(lineToAppend);
            writer.newLine();
            writer.close();
            System.out.println("Line: " + lineToAppend + " appended to : " + filePath);
            return true;
        } catch (IOException e) {
            System.out.println("Error while writing to file: " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    public static List<String> ReadFromTXTFile(String filePath) {
        return null;
    }
     */

    // JSON methods
    public static void writeToJSONFile(String filePath, Object data) throws IOException, IllegalArgumentException{
            if (!(data instanceof JSONArray || data instanceof JSONObject)){
                throw new IllegalArgumentException("Data must be of type JSONArray or JSONObject");
            }

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));) {
                if (data instanceof JSONArray) {
                    writer.write(new JSONArray((JSONArray) data).toString(4));
                } else {
                    writer.write(new JSONObject(data.toString()).toString(4));
                }
            }
    }

    public static Object readFromJSONFile(String filePath) throws IOException, JSONException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            StringBuilder stringBuilder = new StringBuilder(); // Mutable string

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String fileContents = stringBuilder.toString().trim();

            if (fileContents.isEmpty()) {
                return null;
            }

            char firstChar = fileContents.charAt(0);

            if (firstChar == '[') {
                return new JSONArray(fileContents);
            }

            if (firstChar == '{') {
                return new JSONObject(fileContents);
            }

            throw new JSONException("Invalid JSON format in file" + filePath);
        }
    }

    public static void appendToJSONFileArray(String filePath, Object data) throws IOException, JSONException, IllegalArgumentException {
        // read json array from file
        Object json = readFromJSONFile(filePath);
        JSONArray jsonArray;

        if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        } else if (json == null){
            jsonArray = new JSONArray();
        } else {
            throw new JSONException("Invalid JSON format in file" + filePath);
        }

        // Append data to json array
        if (data instanceof JSONObject || data instanceof String){
            jsonArray.put(data);
        } else {
            throw new IllegalArgumentException("Unsupported data type for appending to JSON array");
        }

        // Write new json array to file
        writeToJSONFile(filePath, jsonArray);
    }

    public static void removeFromJSONFileArray(String filePath, String ID) throws IOException, JSONException {
        if (ID == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        Object json = readFromJSONFile(filePath);
        if (!(json instanceof JSONArray)) {
            throw new JSONException("Invalid JSON format in file" + filePath);
        }

        JSONArray currentArray = (JSONArray) json;
        JSONArray newArray = new JSONArray();

        // Filtering JSONArray
        for (int i = 0; i < currentArray.length(); i++) {
            Object item = currentArray.get(i);

            if (item instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) item;
                if (ID.equals(jsonObject.getString("ID"))) {
                    newArray.put(jsonObject);
                }
            } else if (item instanceof String){
                String string = (String) item;
                if (ID.equals(string)) {
                    newArray.put(item);
                }
            } else {
                throw new JSONException("Invalid JSON format in file" + filePath);
            }
        }

        // Writing filtered JSONArray back to file
        writeToJSONFile(filePath, newArray);
    }
}
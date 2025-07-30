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
    public static boolean writeToJSONFile(String filePath, Object JSONdata) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            if (JSONdata instanceof JSONObject) {
                writer.write(((JSONObject) JSONdata).toString(4));
            } else if (JSONdata instanceof JSONArray) {
                writer.write(((JSONArray) JSONdata).toString(4));
            } else {
                throw new IllegalArgumentException("Object data type not supported");
            }

            // log
            System.out.println("JSON written to : " + filePath);
            return true;

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Failed to write json to " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean appendToJSONFileArray(String filePath, Object JSONdata){
        // Loading JSONArray from file
        Object json = readFromJSONFile(filePath);
        JSONArray jsonArray;

        if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        } else if (json == null){
            jsonArray = new JSONArray();
        } else {
            return false;
        }

        // Appending JSONData to the JSONArray
        if (JSONdata instanceof JSONObject) {
            JSONObject currentObject = (JSONObject) JSONdata;
            jsonArray.put(currentObject);
        } else if (JSONdata instanceof String) {
            String currentString = (String) JSONdata;
            jsonArray.put(currentString);
        } else {
            throw new JSONException("Object data type not supported");
        }

        // Writing JSONArray back to file
        if (writeToJSONFile(filePath, jsonArray)){
            System.out.println("JSON appended to: " + filePath);
            return true;
        }
        System.out.println("Failed to appened JSON to: " + filePath);
        return false;

    }


    public static Object readFromJSONFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            StringBuilder stringBuilder = new StringBuilder(); // Mutable string

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                stringBuilder.append(currentLine);
            }

            String string = stringBuilder.toString().trim();

            if (string.isEmpty()) {
                System.out.println("Empty JSON file: "  + filePath);
                return null;
            }

            char firstChar = string.charAt(0);

            try {
                if (firstChar == '{') {
                    JSONObject jsonObject = new JSONObject(string);
                    System.out.println("JSONObject read from : " + filePath);
                    return jsonObject;
                } else if (firstChar == '[') {
                    JSONArray jsonArray = new JSONArray(string);
                    System.out.println("JSONArray read from : " + filePath);
                    return jsonArray;
                } else {
                    throw new JSONException("Invalid JSON format in file: " + filePath);
                }
            } catch (JSONException e) {
                System.out.println("Invalid JSON format in file: " + filePath);
                return null;
            }

        } catch (IOException e) {
            System.out.println("Failed to read json from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    public static boolean removeFromJSONFileArray(String filePath, String ID) {
        // Loading JSONArray from file
        Object json = readFromJSONFile(filePath);
        if (!(json instanceof JSONArray)) {
            System.out.println("File does not contain a JSON array.");
            return false;
        }

        JSONArray currentArray = (JSONArray) json;
        JSONArray newArray = new JSONArray();

        // Filtering JSONArray
        for (int i = 0; i < currentArray.length(); i++) {
            Object item = currentArray.get(i);

            if (item instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) item;
                // If ID field doesn't match, keep the object
                if (!ID.equals(jsonObject.get("ID"))) {
                    newArray.put(jsonObject);
                }
            } else if (item instanceof String) {
                String string = (String) item;
                // If string doesn't match, keep it
                if (!ID.equals(string)) {
                    newArray.put(string);
                }
            } else {
                // If item is neither JSONObject nor String, preserve it unchanged
                throw new JSONException("Invalid item type in JSONArray at index " + i + " in file: " + filePath);
            }
        }

        // Writing filtered JSONArray back to file
        return writeToJSONFile(filePath, newArray);
    }
}
package pt.isec.pd.ticketline.src.resources.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileOpener
{
    public static List<List<String>> openFile(String filePath)
    {
            List<List<String>> fileTokens = new ArrayList<>();
    
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
    
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    fileTokens.add(Arrays.asList(values));
                }
            }
            catch (Exception e) {
                return null;
            }
            return fileTokens;
    }
}
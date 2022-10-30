package pt.isec.pd.ticketline.src.resources.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileOpener
{
    public static List<String> openFile(String filePath)
    {
            List<String> fileTokens = new ArrayList<>();
    
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
    
                while ((line = br.readLine()) != null) {
                    fileTokens.add(line);
                }
            }
            catch (Exception e) {
                return null;
            }
            return fileTokens;
    }
}
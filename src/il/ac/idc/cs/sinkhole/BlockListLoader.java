package il.ac.idc.cs.sinkhole;

import java.io.*;
import java.net.URL;
import java.util.HashSet;

/**
 * Class for loading the domain dns block list from a .txt file
 */
public class BlockListLoader {
    public BlockListLoader()
    {
    }

    /**
     * Loads the block list to a HashSet.
     * @return the block list as HashSet.
     */
    public HashSet<String> Load(String filePath)
    {
        // Establish reader.
        BufferedReader reader = getBlockListFileReader(filePath);

        if(reader == null)
        {
            return new HashSet<>();
        }

        return getPopulatedBlockList(reader);
    }

    private BufferedReader getBlockListFileReader(String filePath){
        BufferedReader reader = null;

        try
        {
            URL path = BlockListLoader.class.getResource(filePath);
            File file = new File(path.getFile());
            reader = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e)
        {
            System.out.println(String.format("BlockListLoader - exception occurred establishing reader. exception = {0}", e));
        }

        return reader;
    }

    private HashSet<String> getPopulatedBlockList(BufferedReader reader)
    {
        HashSet<String> blockList = new HashSet<>();
        String line;

        try
        {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }

                blockList.add(line);
            }
        }
        catch (IOException e)
        {
            System.out.println(String.format("BlockListLoader - exception occurred while reading file. exception = {0}", e));
        }

        return blockList;
    }
}

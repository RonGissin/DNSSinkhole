package BlockList;

import java.io.*;
import java.net.URL;
import java.util.HashSet;

public class BlockListLoader {
    public BlockListLoader()
    {
    }

    public HashSet<String> GetBlockList()
    {
        // Establish reader.
        BufferedReader reader = getBlockListFileReader();

        if(reader == null)
        {
            return new HashSet<>();
        }

        return getPopulatedBlockList(reader);
    }

    private BufferedReader getBlockListFileReader(){
        BufferedReader reader = null;

        try
        {
            URL path = BlockListLoader.class.getResource(CBlockListFileName);
            File file = new File(path.getFile());
            reader = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e)
        {
            System.out.println(String.format("BlockListLoader - exception occurred establishing reader. exception = {0}", e));
        }
        catch (IOException e)
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

    private static final String CBlockListFileName = "blocklist.txt";
}

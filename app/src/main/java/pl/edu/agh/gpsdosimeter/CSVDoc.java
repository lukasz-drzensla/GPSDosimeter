package pl.edu.agh.gpsdosimeter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class CSVDoc
{
    private int rows, columns;

    public int getRowNum()
    {
        return rows;
    }

    public int getColNum()
    {
        return columns;
    }

    private List<List<String>> elements;

    public CSVDoc(File file)
    {
        elements = new ArrayList<List<String>>();
        List<String> lines;
        lines = new ArrayList<String>();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                lines.add(line);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
        rows = lines.size();
        columns = parse(lines);
    }

    public List<String> getRow(int row)
    {
        return elements.get(row);
    }

    public List<String> getHdr()
    {
        return getRow(0);
    }

    public List<String> getColumn(int col)
    {
        List<String> res = new ArrayList<String>();
        for (List<String> row : elements)
        {
            res.add(row.get(col));
        }
        return res;
    }

    private int parse (List<String> lines)
    {
        int cols = 0;
        int j = 0;
        for (String line: lines)
        {
            List <String> newLine = new ArrayList<String>();
            String value = "";
            boolean passed_quote = false;
            for (int i = 0; i < line.length(); i++)
            {
                char c = line.charAt(i);
                if (c == '\"')
                {
                    if (passed_quote)
                    {
                        if (i < line.length() - 1)
                        {
                            if (line.charAt(i+1) == '\"')
                            {
                                value += c;
                            }
                        }
                    }
                    passed_quote = !passed_quote;
                } else if (!passed_quote && c == ',')
                {
                    newLine.add(value);
                    value = "";
                    j++;
                } else {
                    value += c;
                }
            }
            newLine.add(value);
            if (cols == 0)
            {
                cols = ++j;
            } else if (++j != cols)
            {
                return -1;
            }
            elements.add(newLine);
            j = 0;
        }
        return cols;
    }

    CSVDoc(List<List<String>> objs)
    {
        for (List<String> row : objs)
        {
            for (int i = 0; i < row.size(); i++)
            {
                if (row.get(i).contains(","))
                {
                    if (row.get(i).charAt(0) != '\"')
                    {
                        String temp = "\"" + row.get(i);
                        if (row.get(i).charAt(row.get(i).length() -1) != '\"')
                        {
                            temp += "\"";
                        }
                        row.set(i, temp);
                    }
                }
            }
        }
        elements = new ArrayList<List<String>>(objs);
        rows = elements.size();
        columns = elements.get(0).size();
    }

    public int saveToFile (String pathString)
    {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(pathString));
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        for (List<String> row : elements)
        {
            String str = "";
            for (String col : row)
            {
                str += col + ",";
            }
            str = str.substring(0, str.length()-1);
            str += "\n";
            try {
                writer.write(str);
            } catch (IOException e) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                return -1;
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
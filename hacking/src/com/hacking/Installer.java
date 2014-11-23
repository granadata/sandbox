/**
 * Developed by Robert Hodges. 
 */

package com.hacking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Implements a program to track installation of components connected by a
 * dependency graph. For a description of program semantics please refer to the
 * following reference:
 * <p/>
 * https://docs.google.com/document/pub?id=16L
 * KAU3JxoNbt3mspgoaEPGRLf4IZWFycy4fyoyrx2r8
 * </p>
 * The program invocation is as follows:
 * 
 * <pre><code>java com.hacking.Installer input_file_name</code></pre>
 * 
 * Note that this program assumes perfect syntax at present. If you use invalid
 * syntax results may be unpredictable.
 */
public class Installer
{
    /**
     * Main entry point for running the installer.
     * 
     * @param args An array containing the name of an input file.
     */
    public static void main(String[] args)
    {
        // Check for valid arguments.
        if (args.length != 1)
        {
            usage();
            System.exit(1);
        }
        String inputFileName = args[0];
        File input = new File(inputFileName);
        if (!input.canRead() || !input.isFile())
        {
            println("Invalid input file: " + input.getAbsolutePath());
            System.exit(2);
        }

        // We have a valid file. Create an graph and apply commands to the
        // graph.
        DependencyGraph graph = new DependencyGraph();
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new FileReader(input));
            String line;
            while ((line = reader.readLine()) != null)
            {
                processInput(graph, line);
            }
        }
        catch (Throwable e)
        {
            println("Program failed unexpectedly!");
            e.printStackTrace();
        }
    }

    /** Print usage. */
    public static void usage()
    {
        println("java " + Installer.class.getName() + " input_file_name");
    }

    /** Print to stdout. */
    public static void println(String s)
    {
        System.out.println(s);
    }

    /**
     * Process the next line of program input by turning it into an operation on
     * the graph.
     * 
     * @param graph The dependency graph
     * @param line Next line of input
     */
    public static void processInput(DependencyGraph graph, String line)
    {
        // Break the string a command followed by arguments.
        StringTokenizer st = new StringTokenizer(line.trim());
        String cmd = null;
        LinkedList<String> args = new LinkedList<String>();
        if (st.hasMoreTokens())
        {
            cmd = st.nextToken();
        }
        while (st.hasMoreTokens())
        {
            args.add(st.nextToken());
        }

        // If the line has no command return now. This means we ignore blank
        // lines.
        if (cmd == null)
        {
            return;
        }

        // Print the command as it appears in the input.
        println(line);

        // Parse the line and execute it against the graph.
        if ("DEPEND".equals(cmd))
        {
            String component = args.removeFirst();
            graph.depend(component, args);
        }
        else if ("INSTALL".equals(cmd))
        {
            graph.install(args.getFirst(), false);
        }
        else if ("REMOVE".equals(cmd))
        {
            graph.remove(args.getFirst(), false);
        }
        else if ("LIST".equals(cmd))
        {
            graph.list();
        }
        else if ("END".equals(cmd))
        {
            // Do nothing.
        }
        else
        {
            throw new RuntimeException("Unrecognized command: " + cmd);
        }
    }
}
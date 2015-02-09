/**
 * Copyright 2014 Robert Hodges
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Initial developer(s): Robert Hodges
 * Contributor(s):
 */

package com.granadata.hacking.compression;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Prints timestamp values in various time zones to test behavior of class
 * java.util.Timestamp. You can run this under a debugger to look at Java
 * library execution paths.
 */
public class CompressionLab
{
    /**
     * Accept options and print timestamp accordingly.
     */
    public static void main(String[] args)
    {
        OptionParser parser = new OptionParser();
        parser.accepts("length", "Run length for compression")
                .withRequiredArg().ofType(Long.class);
        parser.accepts("threads", "Max threads for reads").withRequiredArg()
                .ofType(Integer.class);
        parser.accepts("data",
                "Data to be compressed, must be at least run length in size")
                .withRequiredArg().ofType(String.class);
        parser.accepts("help");

        // Parse options.
        OptionSet options = null;
        try
        {
            options = parser.parse(args);
        }
        catch (OptionException e)
        {
            println("Parsing failed: " + e.getMessage());
            println("Try --help for command line options");
            System.exit(1);
        }
        if (options.has("help"))
        {
            try
            {
                println("Check performance of compression");
                println("Usage: compressionlab [options]");
                parser.printHelpOn(System.out);
            }
            catch (IOException e)
            {
                // This is very unlikely.
                e.printStackTrace();
            }
            System.exit(0);
        }

        // Grab option values for further processing.
        Long runLength = (Long) options.valueOf("length");
        Integer threads = (Integer) options.valueOf("threads");
        String data = (String) options.valueOf("data");
        File dataFile = new File(data);

        // Print the starting time zone information.
        printSeparator();
        println("Configuration info...");
        println(String.format("length: %d", runLength));
        println(String.format("rounds: %d", threads));
        println(String.format("data  : %s", dataFile.getPath()));

        try
        {
            // Write and read back uncompressed file.
            printSeparator();
            writeRead(dataFile, runLength, threads, false);
            // Write and read back compressed file.
            printSeparator();
            writeRead(dataFile, runLength, threads, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Write and then read back the file.
    private static void writeRead(File dataFile, long runLength, int threads,
            boolean compress) throws Exception
    {
        String outName = compress ? "out.gz" : "out";
        File outFile = new File(outName);

        // Write the file.
        OutputStream fos = new FileOutputStream(outFile);
        BufferedOutputStream os;
        if (compress)
            os = new BufferedOutputStream(new GZIPOutputStream(fos));
        else
            os = new BufferedOutputStream(fos);

        InputStream fis = new FileInputStream(dataFile);
        BufferedInputStream is = new BufferedInputStream(fis);

        long read = 0;
        byte[] buf = new byte[4096];
        long w1 = System.currentTimeMillis();
        while (read < runLength)
        {
            int requested = (int) Math.min(4096, runLength - read);
            int actual = is.read(buf, 0, requested);
            os.write(buf, 0, actual);
            read += actual;
        }
        os.flush();
        long dw = System.currentTimeMillis() - w1;
        os.close();
        is.close();

        // Print write results.
        printOp("write", outFile, 1, runLength, read, dw);

        // Read the file using sets of threads up to the max.
        int currentThreads = 1;
        while (currentThreads <= threads)
        {
            read(outFile, runLength, currentThreads, compress);
            currentThreads *= 2;
        }
    }

    private static void read(File outFile, long runLength, int numThreads,
            boolean compress) throws Exception
    {
        // Allocate thread and task array.
        ReadTask[] tasks = new ReadTask[numThreads];
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            tasks[i] = new ReadTask(outFile, runLength, compress);
            threads[i] = new Thread(tasks[i]);
        }

        // Start tasks and wait for them to finish.
        long r1 = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++)
        {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++)
        {
            threads[i].join(600000);
            if (tasks[i].read != runLength)
            {
                println(String.format("Thread read failed: [%d] read=%d", i,
                        tasks[i].read));
            }
        }

        // Print read results.
        long dr = System.currentTimeMillis() - r1;
        printOp("read", outFile, numThreads, runLength, tasks[0].read, dr);
    }

    public static void printOp(String op, File f, int threads, long runLength,
            long bytes, long duration)
    {
        double seconds = duration / 1000.0;
        double mbs = ((long) bytes) / (seconds * 1024.0 * 1024.0);
        double writtenSize = ((long) f.length() * 100.0) / runLength;
        String msg = String
                .format("Op: %s File: %s Threads: %d Bytes: %d Stored: %-6.2f%% Secs: %-6.3f MB/s: %-8.2f",
                        op, f, threads, bytes, writtenSize, seconds, mbs);
        println(msg);
    }

    // Generic output routines.
    public static void println(String msg)
    {
        System.out.println(msg);
    }

    public static void print(char c)
    {
        System.out.print(c);
    }

    public static void printSeparator()
    {
        printSeparator('-', 60);
    }

    public static void printSeparator(char sepChar, int length)
    {
        for (int i = 0; i < length; i++)
        {
            print(sepChar);
        }
        print('\n');
    }

}

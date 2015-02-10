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
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Runs tests of reading and writing using compression and encryption (or not).
 */
public class CompressionLab
{
    // Parameters from options.
    private Long      runLength = (long) 10000;
    private Integer   threads   = 1;
    private String    data;
    private File      dataFile;
    private String    algorithm = "DESede";

    // Additional properties.
    private SecretKey secretKey;

    /** Creates an instance for doing real work. */
    public CompressionLab()
    {
    }

    /**
     * Accept options and invoke tests.
     */
    public static void main(String[] args)
    {
        new CompressionLab().go(args);
    }

    /**
     * Process the test.
     */
    public void go(String[] args)
    {
        OptionParser parser = new OptionParser();
        parser.accepts("length", "Run length for compression")
                .withOptionalArg().ofType(Long.class).defaultsTo((long) 10000);
        parser.accepts("threads", "Max threads for reads").withRequiredArg()
                .ofType(Integer.class).defaultsTo(1);
        parser.accepts("data",
                "Data to be written and read, must be at least run length in size")
                .withRequiredArg().ofType(String.class);
        parser.accepts("algorithm", "Encryption algorithm").withRequiredArg()
                .ofType(String.class).defaultsTo("DESede");
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
        runLength = (Long) options.valueOf("length");
        threads = (Integer) options.valueOf("threads");
        data = (String) options.valueOf("data");
        dataFile = new File(data);
        algorithm = (String) options.valueOf("algorithm");

        // Print the starting time zone information.
        printSeparator();
        println("Configuration info...");
        println(String.format("length : %d", runLength));
        println(String.format("threads: %d", threads));
        println(String.format("data   : %s", dataFile.getPath()));
        println(String.format("encryption algorithm: %s", algorithm));

        try
        {
            // Generate a secret key for encryption.
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            if ("AES".equals(algorithm))
                keyGenerator.init(128);
            else if ("DES".equals(algorithm))
                keyGenerator.init(56);
            else if ("DESede".equals(algorithm))
                keyGenerator.init(168);
            else if ("HmacSHA1".equals(algorithm))
            {
                // Do nothing?
            }
            else if ("HmacSHA256".equals(algorithm))
            {
                // Do nothing
            }
            else
            {
                throw new Exception("Unknown encryption algorithm: "
                        + algorithm);
            }
            secretKey = keyGenerator.generateKey();

            // Write and read back uncompressed file.
            printSeparator();
            writeRead(dataFile, false, false);

            // Write and read back compressed file.
            printSeparator();
            writeRead(dataFile, true, false);

            // Write and read back encrypted file.
            printSeparator();
            writeRead(dataFile, false, true);

            // Write and read back compressed, encrypted file.
            printSeparator();
            writeRead(dataFile, true, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Write and then read back the file.
    private void writeRead(File dataFile, boolean compress, boolean encrypt)
            throws Exception
    {
        String outName;
        if (compress && encrypt)
            outName = "out.encrypt.gz";
        else if (encrypt)
            outName = "out.encrypt";
        else if (compress)
            outName = "out.gz";
        else
            outName = "out";
        File outFile = new File(outName);

        // Write the test information.
        printTest(outFile, runLength, compress, encrypt);

        // Construct stack of streams to write to file.
        OutputStream fos = new FileOutputStream(outFile);
        OutputStream b1;
        if (encrypt)
        {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            b1 = new CipherOutputStream(fos, cipher);
        }
        else
            b1 = fos;
        OutputStream b2;
        if (compress)
            b2 = new BufferedOutputStream(new GZIPOutputStream(b1));
        else
            b2 = new BufferedOutputStream(b1);
        CRC32 crc = new CRC32();
        CheckedOutputStream os = new CheckedOutputStream(b2, crc);

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
        long crcValue = crc.getValue();
        os.close();
        is.close();

        // Print write results.
        printOp("write", outFile, 1, runLength, read, dw, crcValue);

        // Read the file using sets of threads up to the max.
        int currentThreads = 1;
        while (currentThreads <= threads)
        {
            read(outFile, currentThreads, compress, encrypt, crcValue);
            currentThreads *= 2;
        }
    }

    // Read back an existing file.
    private void read(File outFile, int numThreads, boolean compress,
            boolean encrypt, long crcValue) throws Exception
    {
        // Allocate thread and task array.
        ReadTask[] tasks = new ReadTask[numThreads];
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            tasks[i] = new ReadTask(outFile, runLength, compress, encrypt,
                    secretKey, algorithm);
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
            else if (tasks[i].crcValue != crcValue)
            {
                println(String.format("Thread read failed: [%d] crc[0]=%d", i,
                        tasks[i].crcValue));
            }
        }

        // Print read results.
        long dr = System.currentTimeMillis() - r1;
        printOp("read", outFile, numThreads, runLength, tasks[0].read, dr,
                crcValue);
    }

    /** Print a summary of a test. */
    private void printTest(File f, long runLength, boolean compress,
            boolean encrypt)
    {
        String msg = String
                .format("TEST--Data File: %s Data length: %d Compressed: %s Encrypted: %s",
                        f, runLength, compress, encrypt);
        println(msg);
    }

    /** Print a summary of a test operation. */
    private void printOp(String op, File f, int threads, long runLength,
            long bytes, long duration, long crcValue)
    {
        double seconds = duration / 1000.0;
        double mbs = ((long) bytes) / (seconds * 1024.0 * 1024.0);
        double compression = 100.0 - ((long) f.length() * 100.0) / runLength;
        String msg = String
                .format("Op: %5s Threads: %d CRC: %d Compression: %5.1f%% Secs: %-6.3f MB/s: %-8.2f",
                        op, threads, crcValue, compression, seconds, mbs);
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

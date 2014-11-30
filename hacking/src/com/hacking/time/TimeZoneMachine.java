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

package com.hacking.time;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Prints timestamp values in various time zones to test behavior of class
 * java.util.Timestamp. You can run this under a debugger to look at Java
 * library execution paths.
 */
public class TimeZoneMachine
{
    /**
     * Accept options and print timestamp accordingly.
     */
    public static void main(String[] args)
    {
        OptionParser parser = new OptionParser();
        parser.accepts("timestamp",
                "Time value in milliseconds since midnight, Jan 1, 1970")
                .withRequiredArg().ofType(Long.class);
        parser.accepts("timezone",
                "Time zone name for input and output of timestamps")
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
                println("Show behavior of Java Timestamp and TimeZone classes");
                println("Usage: timezonemachine [options]");
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
        Long timestamp = (Long) options.valueOf("timestamp");
        String timezone = (String) options.valueOf("timezone");

        // Print the starting time zone information.
        printSeparator();
        println("Initial time zone settings...");
        printTimeZoneInfo();

        // Set and display values.
        printSeparator();
        long timestampMillis;
        if (timestamp == null)
        {
            println("Setting time to current time");
            timestampMillis = System.currentTimeMillis();
        }
        else
        {
            timestampMillis = timestamp;
        }
        println("Timestamp: " + timestampMillis);
        println("Time Zone: " + timezone);

        // Set an initial time stamp value now that we have the current time.
        printSeparator();
        Timestamp ts1 = new Timestamp(timestampMillis);
        println("Timestamp value #1...");
        printTimestamp(ts1);

        // Change the time.
        printSeparator();
        println("Setting new time zone...");
        TimeZone newTimeZone = TimeZone.getTimeZone(timezone);
        TimeZone.setDefault(newTimeZone);
        printTimeZoneInfo();

        // Now create a new time stamp.
        Timestamp ts2 = new Timestamp(timestampMillis);

        // Print both time stamps.
        printSeparator();
        println("Timestamp value #1...");
        printTimestamp(ts1);
        println("Timestamp value #2...");
        printTimestamp(ts2);
    }

    /**
     * Prints the current time zone.
     */
    public static void printTimeZoneInfo()
    {
        GregorianCalendar cal = new GregorianCalendar();
        TimeZone tz = cal.getTimeZone();
        println(tz.toString());
    }

    /**
     * Prints the value and string version of a time zone.
     * 
     * @param ts A timestamp to display
     */
    public static void printTimestamp(Timestamp ts)
    {
        println("Value: " + ts.getTime());
        println("String: " + ts.toString());
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

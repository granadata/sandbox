/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Author: Robert Hodges
 */

package com.granadata.hacking.knuth.hashing;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.granadata.hacking.knuth.hashing.KHashTable;

/**
 * Tests hash table behavior.
 * 
 * @see com.granadata.hacking.knuth.hashing.KHashTable
 */
public class KHashTableTest
{
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }

    /**
     * Verify that we can add, fetch, and remove a value in the hash table.
     */
    @Test
    public void testSingleKey()
    {
        KHashTable kht = new KHashTable(10);
        Assert.assertEquals("Empty table", 0, kht.size());

        // Add a value and confirm existence.
        kht.put("test", "value");
        Assert.assertEquals("Filled table", 1, kht.size());
        Assert.assertEquals("Found value", "value", kht.get("test"));
        printBuckets(kht);

        // Add the value and confirm non-existence.
        String value = (String) kht.remove("test");
        Assert.assertEquals("Removed value", "value", value);
        Assert.assertEquals("Empty table again", 0, kht.size());
        Assert.assertNull("Missing value", kht.get("test"));
    }

    /**
     * Verify that we can add and fetch multiple random keys.
     */
    @Test
    public void testRandomKeys()
    {
        KHashTable kht = new KHashTable(10);
        String[] keys = generateStrings(75, true);
        String[] values = new String[keys.length];

        // Add 50 values.
        for (int i = 0; i < keys.length; i++)
        {
            values[i] = new Integer(i).toString();
            kht.put(keys[i], values[i]);
        }

        // Print bucket counts for fun.
        Assert.assertEquals("Filled table", keys.length, kht.size());
        printBuckets(kht);

        // Find all the values again.
        for (int i = 0; i < keys.length; i++)
        {
            String value = (String) kht.get(keys[i]);
            Assert.assertEquals("looking for key: " + i, values[i], value);
        }
    }

    /**
     * Verify that we can add and fetch multiple random keys.
     */
    @Test
    public void testSystematicallyVaryingKeys()
    {
        KHashTable kht = new KHashTable(50);
        String[] keys = generateStrings(100, false);
        String[] values = new String[keys.length];

        // Add 50 values.
        for (int i = 0; i < keys.length; i++)
        {
            values[i] = new Integer(i).toString();
            kht.put(keys[i], values[i]);
        }

        // Print bucket counts for fun.
        Assert.assertEquals("Filled table", keys.length, kht.size());
        printBuckets(kht);

        // Find all the values again.
        for (int i = 0; i < keys.length; i++)
        {
            String value = (String) kht.get(keys[i]);
            Assert.assertEquals("looking for key: " + i, values[i], value);
        }
    }

    // Print out bucket counts.
    private void printBuckets(KHashTable kht)
    {
        int[] bucketCounts = kht.bucketCounts();
        println("Bucket counts:");
        for (int i = 0; i < bucketCounts.length; i++)
        {
            println(String.format("%d: [%d]", i, bucketCounts[i]));
        }
    }

    // Generate unique strings.
    private String[] generateStrings(int count, boolean random)
    {
        final String data = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String[] strings = new String[count];
        for (int i = 0; i < count; i++)
        {
            StringBuffer sb = new StringBuffer();
            for (int len = 0; len <= i; len++)
            {
                int index;
                if (random)
                    index = (int) (Math.random() * data.length());
                else
                    index = len % data.length();
                sb.append(data.charAt(index));
            }
            strings[i] = sb.toString();
        }
        return strings;
    }

    // Print a string.
    private static void println(String s)
    {
        System.out.println(s);
    }
}
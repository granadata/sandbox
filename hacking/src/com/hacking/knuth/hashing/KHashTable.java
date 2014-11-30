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

package com.hacking.knuth.hashing;

import java.util.LinkedList;

/**
 * Implements a simple hash table with parameterized hash method.
 */
public class KHashTable
{
    // Holder for a single key value pair.
    class KeyValue
    {
        Object key;
        Object value;

        KeyValue(Object key, Object value)
        {
            this.key = key;
            this.value = value;
        }
    }

    // Hash table data.
    private final KHashMethod  hashMethod;
    private final int          capacity;
    private int                size              = 0;
    private LinkedList<?>[]    buckets;

    // Default hash method.
    private static KHashMethod defaultHashMethod = new ModulusHashMethod();

    /**
     * Create hash table.
     * 
     * @param capacity Number of buckets to allocate
     * @param hashMethod Custom hash method
     */
    public KHashTable(int capacity, KHashMethod hashMethod)
    {
        this.capacity = capacity;
        this.hashMethod = hashMethod;
        this.buckets = new LinkedList<?>[capacity];
    }

    /**
     * Creates a hash table with default hash method.
     * 
     * @param capacity Number of buckets to allocate
     */
    public KHashTable(int capacity)
    {
        this(capacity, defaultHashMethod);
    }

    /**
     * Add a value to the hash table.
     * 
     * @param key Key associated with value
     * @param value Value to store, which can be null
     */
    public void put(Object key, Object value)
    {
        // Fetch the bucket.
        LinkedList<KeyValue> bucket = getBucket(key, true);

        // If the key already exists, update it.
        for (KeyValue kv : bucket)
        {
            if (kv.key.equals(key))
            {
                kv.value = value;
                return;
            }
        }

        // At this point the key does not exist, so we need to add it.
        KeyValue kv = new KeyValue(key, value);
        bucket.add(kv);
        size++;
    }

    /**
     * Return a value from the table.
     * 
     * @param key Key associated with value
     * @return Value or null if not found (null could also be the stored value)
     */
    public Object get(Object key)
    {
        // Fetch the bucket.
        LinkedList<KeyValue> bucket = getBucket(key, true);
        if (bucket == null)
            return null;

        // If the key exists, return the value.
        for (KeyValue kv : bucket)
        {
            if (kv.key.equals(key))
            {
                return kv.value;
            }
        }

        // Nothing found.
        return null;
    }

    /**
     * Removes a value from the table.
     * 
     * @param key Key associated with value
     * @return Value removed or null if not found (null could also be the stored
     *         value)
     */
    public Object remove(Object key)
    {
        // Fetch the bucket.
        LinkedList<KeyValue> bucket = getBucket(key, true);
        if (bucket == null)
            return null;

        // If the key exists, note it for removal.
        KeyValue foundKv = null;
        for (KeyValue kv : bucket)
        {
            if (kv.key.equals(key))
            {
                foundKv = kv;
                break;
            }
        }

        // If we found something, remove that value and return it.
        if (foundKv == null)
        {
            return null;
        }
        else
        {
            bucket.remove(foundKv);
            size--;
            return foundKv.value;
        }
    }

    /**
     * Returns the number of keys currently stored in the table.
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns an array showing the number of keys in each bucket.
     */
    public int[] bucketCounts()
    {
        int[] counts = new int[capacity];
        for (int i = 0; i < buckets.length; i++)
        {
            if (buckets[i] == null)
                counts[i] = 0;
            else
                counts[i] = buckets[i].size();
        }
        return counts;
    }

    /**
     * Returns the bucket for a particular key, creating it if missing.
     * 
     * @param key Key for which we need the bucket
     * @param createIfMissing If true, create the bucket if it is not found
     * @return The bucket or null if no bucket exists
     */
    private LinkedList<KeyValue> getBucket(Object key, boolean createIfMissing)
    {
        // Compute the hash key.
        String hashKey = key.toString();
        long hash = hashMethod.hash(hashKey);

        // Get the bucket, overriding pesky Java warning.
        int index = (int) hash % capacity;
        @SuppressWarnings("unchecked")
        LinkedList<KeyValue> bucket = (LinkedList<KeyValue>) buckets[index];

        // If the bucket is null, create it now if desired.
        if (bucket == null && createIfMissing)
        {
            bucket = new LinkedList<KeyValue>();
            buckets[index] = bucket;
        }

        // Return the bucket.
        return bucket;
    }
}
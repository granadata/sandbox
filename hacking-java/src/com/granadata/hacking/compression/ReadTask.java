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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Performs reads on a file.
 */
public class ReadTask implements Runnable
{
    BufferedInputStream is;
    long                runLength;
    volatile long       read;

    public ReadTask(File outFile, long runLength, boolean compress)
            throws Exception
    {
        InputStream fisR = new FileInputStream(outFile);
        if (compress)
            is = new BufferedInputStream(new GZIPInputStream(fisR));
        else
            is = new BufferedInputStream(fisR);
        this.runLength = runLength;
    }

    public void run()
    {
        try
        {
            byte[] buf = new byte[4096];
            int actual = 0;
            while (read < runLength && actual > -1)
            {
                int requested = (int) Math.min(4096, runLength - read);
                actual = is.read(buf, 0, requested);
                read += actual;
            }
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

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
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

/**
 * Performs reads on a file.
 */
public class ReadTask implements Runnable
{
    CheckedInputStream is;
    CRC32              crc;
    long               runLength;
    volatile long      read;
    volatile long      crcValue;

    public ReadTask(File outFile, long runLength, boolean compress,
            boolean encrypt, SecretKey secretKey, String algorithm)
            throws Exception
    {
        InputStream fis = new FileInputStream(outFile);
        InputStream b1;
        if (encrypt)
        {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            b1 = new CipherInputStream(fis, cipher);
        }
        else
            b1 = fis;
        BufferedInputStream b2;
        if (compress)
            b2 = new BufferedInputStream(new GZIPInputStream(b1));
        else
            b2 = new BufferedInputStream(b1);
        crc = new CRC32();
        is = new CheckedInputStream(b2, crc);
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
            crcValue = crc.getValue();
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

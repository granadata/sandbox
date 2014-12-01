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

package com.granadata.hacking.knuth.hashing;

public class ModulusHashMethod implements KHashMethod
{
    // A nice prime number.
    private static final int modValue        = 1009;
    private static final int maxAccumulation = 1009 * 65535;

    /**
     * Compute hash by adding integer values of each character and taking
     * modulus.
     */
    @Override
    public int hash(String s)
    {
        // Iterate through the string to sum character values.
        int accumulator = 0;
        for (int i = 0; i < s.length(); i++)
        {
            // Add next character and check for overflow.
            accumulator += s.charAt(i);
            if (accumulator > maxAccumulation)
            {
                accumulator -= maxAccumulation;
            }
        }
        return accumulator % modValue;
    }
}
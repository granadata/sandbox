/**
 * Developed by Robert Hodges. 
 */

package com.hacking;

import java.util.HashMap;
import java.util.List;

/**
 * This class implements a dependency graph for installation and processes
 * commands against that graph. This class is not multi-thread safe. Callers are
 * responsible for synchronization if access by multiple threads is desired.
 */
public class DependencyGraph
{
    HashMap<String, HashMap<String, String>> dependencyTable = new HashMap<String, HashMap<String, String>>();
    HashMap<String, Integer>                 componentTable  = new HashMap<String, Integer>();

    /** Creates a new instance. */
    public DependencyGraph()
    {
    }

    /**
     * Declares a dependency.
     * 
     * @param component Component name.
     * @param dependencies List of components on which it depends.
     */
    public void depend(String component, List<String> dependencies)
    {
        HashMap<String, String> dependencyMap = dependencyTable.get(component);
        if (dependencyMap == null)
        {
            dependencyMap = new HashMap<String, String>();
            dependencyTable.put(component, dependencyMap);
        }

        for (String dependency : dependencies)
        {
            dependencyMap.put(dependency, component);
        }
    }

    /**
     * Installs a component and any uninstalled dependencies, printing the name
     * of each component as it is installed.
     * 
     * @param component Component name
     * @param isDependency If true we are installing a dependency
     */
    public void install(String component, boolean isDependency)
    {
        // See if the component is already installed.
        Integer refCount = componentTable.get(component);
        if (refCount != null)
        {
            if (!isDependency)
                println(component + " is already installed");
            return;
        }

        // If not, we can install it right away with a reference count of 0.
        println("Installing " + component);
        componentTable.put(component, 0);

        // Now we need to iterate over and install each dependent item.
        HashMap<String, String> dependencyMap = dependencyTable.get(component);
        if (dependencyMap != null)
        {
            for (String dependency : dependencyMap.keySet())
            {
                Integer dependencyRefCount = componentTable.get(dependency);
                if (dependencyRefCount == null)
                {
                    // We have to install the missing component.
                    install(dependency, true);
                }
                else
                {
                    // We have to increase the dependency count.
                    componentTable.put(dependency, dependencyRefCount + 1);
                }
            }
        }
    }

    /**
     * Removes a component provided no other component depends on it.
     * 
     * @param component Component name
     * @param isDependency If true we are removing a dependency
     */
    public void remove(String component, boolean isDependency)
    {
        // See if the component is already installed.
        Integer refCount = componentTable.get(component);
        if (refCount == null)
        {
            println(component + " is not installed");
            return;
        }
        else if (refCount > 1 && isDependency)
        {
            // We are trying to remove a dependency that is still needed.
            // Reduce the reference count and return.
            {
                println(component + " is still needed");
                componentTable.put(component, refCount - 1);
            }
            return;
        }
        else if (refCount > 0 && ! isDependency)
        {
            // We are trying to remove something that is still needed. 
            {
                println(component + " is still needed");
                componentTable.put(component, refCount - 1);
            }
            return;
        }

        // If not, we can remove this item.
        componentTable.remove(component);
        println("Removing " + component);

        // Now we need to iterate over and try to remove each dependent item.
        HashMap<String, String> dependencyMap = dependencyTable.get(component);
        if (dependencyMap != null)
        {
            for (String dependency : dependencyMap.keySet())
            {
                remove(dependency, true);
            }
        }
    }

    /**
     * Lists all installed components.
     */
    public void list()
    {
        for (String component : componentTable.keySet())
        {
            println(component);
        }
    }

    /** Print a string to stdout. */
    private static void println(String s)
    {
        System.out.println(s);
    }
}
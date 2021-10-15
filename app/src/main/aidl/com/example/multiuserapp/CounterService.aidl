// IRemoteService.aidl
package com.example.multiuserapp;

// Declare any non-default types here with import statements
import com.example.multiuserapp.CounterCallback;

/** Example service interface */
interface CounterService {
    /** Request the process ID of this service, to do evil things with it. */
    int getCounter();

    /** Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void changeValue(int newValue);

    void register(CounterCallback callback);

    void unregister(CounterCallback callback);
}

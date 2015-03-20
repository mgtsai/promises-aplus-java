<!--
    Copyright (C) 2015-2020, Joseph M. G. Tsai
    under the terms of the Apache License, Version 2.0 (ALv2),
    found at http://www.apache.org/licenses/LICENSE-2.0
-->

# promises-aplus-java

Implementation of Promises/A+ in Java, plus various language sugars for programming.  Java 6 or higher is required.

## <a name="features"></a>Features
* Supports [Promises/A+](https://promisesaplus.com/) specification, including _thenable_ interface
* One of three separated interface sets can be chosen for coding 
  * __Untyped__: Promise class without Java Generics
  * __Typed__: Promise class with Java Generics
  * __Light-weight__: Short naming, and uses _Throwable_-only promise rejected reasons, suitable for cleaner codes
* Supports Java 8 Lambda
* Supports pre-built multi-argument (up to 5 arguments) fulfilled values (or rejected reasons) passing amount `.then()`
  method chaining
* Can specify certain callbacks running on the specific `java.util.concurrent.Executor` context, which generally
  be different threads, even can be RPC calls by implementing Executor interfaces
* Utility methods convenient to switch between synchronous and asynchronous programming models
* _(Not supported yet)_ Supports asynchronous sequential/parallel flow controls, such as _if-then-else_, _switch-case_,
  _while_, _for_, ...
* _(Not supported yet)_ Promise causality logging for deep debugging/inspecting runtime execution sequences

## <a name="untypedExamples"></a>Brief examples

### Using Untyped Interface Set
`java
promises.Promises.pf(null)      // Returns fulfilled promises.Promise with null value
.then(dummy -> {
    ...
    return "msg";
})                              // Return type: promises.Promise
.then(
    (String msg) -> {           // Since promises.Promise has no type parameter, this lambda should specify the explicit argument type: String 
        ...
        return 123;
    },
    (reason, exception) -> {    // Type of argument 'exception': Throwable
        ...
        return 456;
    }
)                               // Return type: promises.Promise
.then((Integer num) -> {        // Since promises.Promise has no type parameter, this lambda should specify the explicit argument type: Integer
    ...
});
`

### Using Typed Interface Set
`java
promises.typed.Promises.pf(null)                // Returns fulfilled promises.typed.Promise<Object, Object> with null value
.then(dummy -> {
    ...
    return promises.typed.Promises.v("msg");    // Can be simplified by using static import
})                                              // Return type: promises.typed.Promise<String, Object>
.then(
    msg -> {                                    // Type of argument 'msg': String
        ...
        return promises.typed.Promises.v(123);  // Can be simplified by using static import
    },
    (reason, exception) -> {                    // Type of argument 'reason': Object
        ...
        return promises.typed.Promises.v(456);  // Can be simplified by using static import
    }
)                                               // Return type: promises.typed.Promise<Integer, Object>
.then(num -> {                                  // Type of argument 'num': Integer
    ...
});
`

### Using Light-weight Interface Set
For simplicity, light-weight promise objects do not contain rejection reasons (only contain _Throwable_ objects) to
avoid specifying reason type parameter.

`java
promises.lw.Promises.pf(null)                   // Returns fulfilled promises.lw.P<Object> with null value
.then(dummy -> {
    ...
    return promises.lw.Promises.v("msg");       // Can be simplified by using static import
})                                              // Return type: promises.lw.P<String>
.then(
    msg -> {                                    // Type of argument 'msg': String
        ...
        return promises.lw.Promises.v(123);     // Can be simplified by using static import
    },
    exception -> {                              // Contains throwable-only argument for rejection
        ...
        return promises.lw.Promises.v(456);     // Can be simplified by using static import
    }
)                                               // Return type: promises.lw.P<Integer>
.then(num -> {                                  // Type of argument 'num': Integer
    ...
});
`

### Multi-argument callbacks
`java
promises.typed.Promises.pf(null)
.then(dummy -> {
    ...
    if (correct)
        return promises.typed.Promises.v("msg", 123);           // Can be simplified by using static import
    else
        return promises.typed.Promises.pr(404, "Not found");    // Can be simplified by using static import
})
.then(
    promises.typed.Promises.wf((msg, num) -> {                  // Types of arguments: (String, Integer), can be simplified by using static import
        ...
    }),
    promises.typed.Promises.wr((code, err, exception) -> {      // Types of arguments: (Integer, String, Throwable), can be simplified by using static import
    })
);
`

### Waits for promises being resolved (fulfilled or rejected)
`java
promises.typed.Promises.pf(null)
.then(...)
.then(...)
...
.then(...)
.await(60, TimeUnit.SECONDS);       // Waits for the overall promise chain being resolved
                                    // When the chaining promise is fulfilled, the value would be returned
                                    // When the chaining promise is rejected, promises.PromiseRejectedException containing return rejected reason/exception would be thrown
                                    // After 60 seconds without resolving, java.util.concurrent.TimeoutException would be thrown
`

### Executes a synchronous method (i.e. JDBC operations) on the specified Executor (Thread)

`java
java.util.concurrent.Executor exec = java.util.concurrent.Executors.newSingleThreadExecutor(thread);

promises.typed.Promises.async(exec, () -> {
    ...
    java.sql.Connection conn = dataSource.getConnection();
    java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
    stmt.setString(1, "UserName");
    java.sql.ResultSet rs = stmt.executeQuery();
    ...
    return "msg";
})
.then(
    msg -> {                    // When the synchronous callback successfully returns, the promise would be fulfilled with the returned value
        ...
    },
    (reason, exception) -> {    // When an exception is thrown in the synchronous callback, the promise would be rejected with the thrown exception
        ...
    }
);
`
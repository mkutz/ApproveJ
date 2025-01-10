# ApproveJ

A simple implementation of approval testing for the JVM


## Concepts

### ApprovalBuilders

ApprovalBuilder is the general entry point.
It allows to configure the approval process and to execute it.

```java
approve(someValue);
```

### Scrubbers

Scrubbers can be added to an approval to clean up the value before it is verified.

This is might be useful for timestamps, IDs, etc.

```java
approve(someValue)
  .withScrubber(new UuidScrubber())
```

### Printers

Printers are used to convert the value to a string before it is verified.

The main purpose of this is to make the output more readable.

By default, the ToStringPrinter is used, which simply calls the value's toString method.

```java
approve(someValue)
  .withPrinter(new JsonPrinter())
```

### Verifiers

Finally, a Verifier is used to actually verify the value and to store it if approved for the next execution.

```java
approve(someValue)
  .verify(new InplaceVerifier(approvedValue));
```

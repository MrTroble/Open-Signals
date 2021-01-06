# Open Computers API

## Introduction

The open computers api provides a simple interfacing API to access a signal controller. A signal controller can then handle exactly one Signal which has to be linked to it.

## Methods

```java
boolean hasLink();
```

This methode returns whether or not the controller is linked to a signal. This should be checked before accessing any other field.

```java
String getSignalType();
```

This returns the signal type of the linked signal as human readable string.

```java
table<String, int> getSupportedSignalTypes();
```

This returns which signal type is supported by the linked signal. E.g. a signal might not have a main signal attached to it but a distant signal hence this method would return a human readable string of the type (in this case "distancesignal") and it's according type id, which is needed to query and set the given signal type later on.

```java
boolean changeSignal(int state, int type);
```

This methode changes the part of the signal which specified by the `type` to it's new state given by `newSignal`. If `newSignal` is out of range, this throws an error. E.g. if you want to change the distance signal, you get the type id of the distance signal from the table returned by `getSupportedSignalTypes()` and a given signal state that you want to change it to. You need to pass in the `state` as first argument and the `type` as second argument. States tend to start at 0 and go upwards.

```java
int getSignalState(int type);
```

This methode returns the state of a given signal type of the linked signal. The type id is again to be retrieved by `getSupportedSignalTypes()`

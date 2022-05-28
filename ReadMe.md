# MethLog
`MethLog` is an `annotation` based method call logging plugin for observing completion time.

You just need to add `@MethLog` above any of your function & **that's it!**\
You'll see the calculated time in your Logcat with Class & Method Name.

This project uses [HunterDebug](https://github.com/Leaking/Hunter/) as a base, but only logs the completion time of annotated methods.\
`MethLog` **does not** log annotated method's `parameters` & `return values` like [HunterDebug](https://github.com/Leaking/Hunter/) or [Hugo](https://github.com/JakeWharton/hugo/).

### Adding `MethLog` in your project

In your project's root `build.gradle`
```groovy
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    
    dependencies {
        classpath 'com.lazygeniouz.methlog:methlog-plugin:1.2.8'
        // the plugin also adds the dependency for @MethLog annotation internally, 
        // so that you don't have to manually add the same to every one of your project's build.gradle.
    }
}
```
then in your app or library's `build.gradle`:
```groovy
apply plugin: 'com.android.application'
// or 'com.android.library' in case of a library
apply plugin: 'com.lazygeniouz.methlog'
```

### Using `@MethLog`
Simply annotate any function with `@MethLog` & it should work fine.

#### Example
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logMessageWithDelay("MethLog says Hi!")
    logMessageWithDelay("MethLog says Hi, again!")
}

@MethLog
private fun logMessageWithDelay(message: String) {
    Thread.sleep(2000)
    logDebug(message)
}
```

Output:
```
I/MethLog: ⇢ MainActivity.logMessageWithDelay() completed in 2000ms.
I/MethLog: ⇢ MainActivity.logMessageWithDelay() completed in 2001ms.
```

### Issues & Suggestions
This is my first Gradle Plugin 😄,\
so if you see anything wrong in the code base or something that can be improved then do let me know along with an appropriate implementation (if you happen to know) & I'll try to incorporate those changes.

Create a new issue if you face any problem or have any suggestions.\
I'll appreciate if you create a PR as well (if possible).

Finally, don't forget to ⭐️ the library! :)

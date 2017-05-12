# What is J-diff
J-diff is a simple Java library for finding the difference between two 
Plain old java objects or POJOs. It will take the two java objects and 
analyze their difference. It would return the result difference 
either in the form of HashMap<Field, Object> or as a JSON. 

Installation
---

Grab via Maven:
```xml
<dependency>
    <groupId>com.github.yordan-desta</groupId>
    <artifactId>j-diff</artifactId>
    <version>1.0.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.github.yordan-desta:j-diff:1.0.0'
```
IDifferentiableEntity
---
J-diff analyzes the difference between entities that implements the
<i>IDifferentiableEntity</i> interface or extends from <i>DifferentiableEntity</i>.
The reason for this is because j-diff not only tracks the types of objects but also gives
you the capacity to specify what objects are eligible for comparison by overriding the <i>isEqual(Object o)</i>; 
even if they are instances of the same class. 

<h4>Example</h4>
<p>

```java
public class SomeDiffClass implements IDifferentiableEntity{
    
    public int someIntField;
    public String someStringField;
    
    //...... some other fields
    
    public String uuid; // unique field for each object instance

    @Override
    public boolean isEqual(Object object) {
        //return true; if we don't care about the uniqueness of objects
        
        return object != null && object instanceof SomeDiffClass && Objects.equals(((SomeDiffClass) object).uuid, uuid);
    }
    
}

```
 Features
 ---
 <h3>1. @Differentiable </h3>
 By default every class that implements the IDifferentiableEntity interface or extends from DifferentiableEntity
 are differentiable. But J-diff gives you the ability to take further control which classes
 and also fields you want to skip differentiation by annotating them with the <i>@Differentiable</i>
 annotation. By default its considered differentiable.
 
 ```java
public class SomeDiffClass implements IDifferentiableEntity{

    @Differentiable(ignoreDiff = true) // difference will be ignored for this field
    public int someIntField;

    // other differentiable fields with out annotation
    
     public String someStringField;

}
 
 ```
 
 And also if you are extending from a base class which implements <i>IDifferentiableEntity</i>
 or extends from <i>DifferentiableEntity</i> you can annotate any class you do not want to be
 differentiated, just like the fields in the example above.
 
  <h3>2. DifferentiableLevel </h3>
  
  J-diff allows you to modify how deep you want objects differentiated through 3 levels.
  
  <h4> 2.1. SHALLOW_UPDATE (default) </h4>
  This level wil track the differential for primitive types, Dates and Strings. For differentiable class objects and lists field, 
  it will set the new field as the diff result. It will not go deeper to find which fields of the new object
  has changed.
  
   <h4> 2.2. SHALLOW_IGNORE </h4>
    This is similar to the SHALLOW_UPDATE but instead of setting the the new field as a result
    it will be ignored.
    
   <h4> 2.3. DEEP </h4>
      Does what SHALLOW_UPDATE does but also goes deeper to find what fields of the object (in the new IDifferentiable object)
      are different (if they are) and returns those values. By default it would go two levels but you can
      change it by <i>setMaxDepthCount (int maxDepthCount)</i> method.
    
 
 <h3>3. DifferentialEntityAnalyzerAsync </h3>
 
 J-diff allows you to perform differential analysis asynchronously and notifies the calling class
 upon completion through <i>IDifferentiableCallback</i>. Upon successful completion, an <i>AsyncDiffResult</i>
 object will be returned.
 
Usage
-----
```java

    
     IDifferentialEntityAnalyzer analyzer = new DifferentialEntityAnalyzer<SomeDiffClass>(oldObject, newObject);
     HashMap<Field, Object> result = analyzer.runDifferential(); //returns Field Object hashmap
        
     String jsonForm = analyzer.getPrettyJson();// returns json form result
     
     // creating analyzer with custom depth
     
     IDifferentialEntityAnalyzer analyzerWithDepth = new DifferentialEntityAnalyzer<SomeDiffClass>(oldObject, newObject,DifferentiableLevel.DEEP);


```
Asynchronous call

```java
class CallingClass implements IDifferentiableCallback{

    ....................
    
    DifferentialEntityAnalyzerAsync asyncAnalyzer = new DifferentialEntityAnalyzerAsync<CallingClass, SomeDiffClass>(this, oldObject, newObject);
    asyncAnalyzer.runDifferential();
    
    @Override
    public void onSuccess(AsyncDiffResult asyncDiffResult) {

        //do on successful result
    }

    @Override
    public void onError(DifferentialException exception) {

        //log error or what ever you want on failure
    }

}
```

License
----
<h4><a href="https://choosealicense.com/licenses/apache-2.0/">Apache License 2.0</a></h4>
 
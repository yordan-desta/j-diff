# What is J-diff
J-diff is a simple Java library for finding the difference between two 
Plain old java objects or POJOs. It will take the two java objects and 
analyze their difference. It would return the result difference 
either in the form of <b>HashMap<Field, Object> </b> or as a json. 

# IDifferentiableEntity
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
 
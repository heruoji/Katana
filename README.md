# The Katana Programing Language

This is the repository for Katana that is simple programing language.
It contains the interpreter.

This project is entirely based on [CRAFTING INTERPRETERS](https://craftinginterpreters.com/) Chapter 2.

## What is Katana
Katana is dynamic typing simple programing language.

## Data Types
### Boolean
```
true;
false;
```

### Number
```
1234;
12.34;
```

### String
```
"I am Badman";
"";
```

### Null
```
null
```

## Expressions
### comparison
It returns Boolean result.
You can compare only numbers.
```agsl
2 > 1; // return true
1 >= 2; // return false
1 < 2; // return true
2 <= 1; // return false
```

### equality
It returns Boolean result.
Values of different types are never equivalent.
```agsl
x == y;
x != y;
```

### Logical operators
```agsl
!true;
!false;
true && false;
true || false;
```

### Grouping
```agsl
var average = (min + max) / 2;
```

## Statement
### print
```
print "Hello, Katana!"
```

### expression statement
```
"Hello, Katana!";
```

### block
```
{
    print "Hello, Katana!";
    print "How are you?"
}
```

## Variables
If you omit the initializer, the variables's value defaults to `null`.
```
var hero = "Batman";
print hero; // "Batman"
```

## Control Flow
### if
```
if (condition) {
    print "true";
} else {
    print "false";
}
```

### while
```
var count = 0;
while (count < 10) {
    print count;
    count = count + 1;
}
```

### for
```
for (var i = 1; i < 10; i = i + 1){
    print i;
}
```

## Functions
### declaration and call
```
fun sum(a, b) {
    return a + b;
}
sum(a, b);
```

## Classes
```
class Hero{
    constructor(name){
        this.name =name;
    }
    
    fight() {
        print this.name + " is fighting";
    }
}

var batman = Hero("Batman");
batman.fight; //"Batman is fighting"
```

### Inheritance
```
class ArmedHero extends Hero {
    constructor(name, weapon){
        super.constructor(name);
        this.weapon = weapon;
    }
}
```

## Standard Libraries

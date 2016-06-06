## about
Parser from ``` `x` > 2``` to case classes tree, common use case - human readable and safe transport filters from frontend to backend.

#### usage examples
```
LogicalParser.toTree("""`x` == "" """).get // Expression("x", Eq, "")
```

To see more examples look at [tests](https://github.com/sonenko/logical-parser/blob/master/src/test/scala/com/github/sonenko/parser/LogicalParserTest.scala#L83)
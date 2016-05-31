To run this project, first create a configuration file in /etc/todo/todo.cfg with the following content:

```
# Example configuration
listUri = "http://localhost:8084/list"
selectItemUri = "http://localhost:8084/selectitem"
createUri = "http://localhost:8084/create"
testData = ["find water",
            "make shelter",
            "make fire",
            "find food"]
h2username = ""  # Set your secrets securely
h2password = ""
todo
{
  maximum-item-count = 10
}
```

Then do the following at the sbt project root directory:

sbt compile

sbt "project http" "run"



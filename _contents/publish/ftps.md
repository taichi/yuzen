# How to Publish to FTPS Server

* Edit your gradle.properties like the below
    `ftps.username=[your username]
    ftps.password=[your password]
    `  
* Edit build.gradle for publish to FTPS Server
    `yuzen.publish {
        ftps host : "example.jp"
    }`  
* Publish to FTPS Server. 
    `gradlew publish`  
    
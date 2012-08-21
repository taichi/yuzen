# Yuzen

Yuzen is a static contents generator similar to [jekyll](https://github.com/mojombo/jekyll).

* Yuzen is a [Gradle](http://gradle.org/) Plugin
* Yuzen uses clean template by [Thymeleaf](http://www.thymeleaf.org/)
* Yuzen translates [Markdown](http://daringfireball.net/projects/markdown/) to HTML5
    * Yuzen parses Markdown by [Pegdown](https://github.com/sirthias/pegdown/)
* Yuzen contains some useful templates
    * blog
    * slide
    * site (this one)
* Yuzen publishes contents to GitHub Pages and FTPS server


## Requirements
* java7 or more tested by 1.7.0_02
* gradle 1.1
* eclipse 3.7.2 (for development)
    * [Groovy-Eclipse 2.6.1](http://groovy.codehaus.org/Eclipse+Plugin)

## Installation
* Install Java SE 7 [JDK](http://www.oracle.com/technetwork/java/javase/downloads/)
* if you don't have gradle,
    * Download latest release archive from [here](https://github.com/taichi/yuzen/downloads)
    * Extract the archive.
        `jar xvf yuzen-startup.zip`  
* if you already use gradle,
    * apply plugin from this site.
        `apply from: 'http://yuzen.koshinuke.org/install'`
* Modify gradle.properties
    * if you use github features, edit these keys
        * github.username
        * github.password
    * if your network is in firewall, edit these keys
        * systemProp.http.proxyHost
        * systemProp.http.proxyPort
        * systemProp.https.proxyHost
        * systemProp.https.proxyPort

## How to Get Start a Blog
* Initialize template
    `gradlew initBlog`  
* Edit build.gradle for blogging.
    `blog {  
         title 'blog title'  
         subtitle 'sub title or blog description'  
         // this block uses for atom or rss feed.  
         feed {  
             syndicationURI = 'http://localhost:8080/blog'  
             author = 'john doe'  
         }  
     }
    `  
* Write your profile to _contents/profile.md
* add first entry
    `gradlew post -Ptitle=HelloWorld`  
* Write your first entry to generated file.
* Generate static contents
    `gradlew startBlog`  
* Access to [localhost:8080](http://localhost:8080)
* Check your generated contents
* Edit build.gradle for publish to GitHub Pages.
    `yuzen.publish {
        ghpages repoURI : "https://github.com/[your github account]/[your repository name].git"
    }`  
* Publish to GitHub Pages. yuzen create or update gh-pages branch
    `gradlew publish`  


## How to build your Yuzen
* clone repository
    `git clone https://github.com/taichi/yuzen.git`  
* make your eclipse environment
    `gradlew eclipse`  
* import project to eclipse
* install to your maven local repository
    `gradlew clean install`  

## License
Apache License, Version 2.0
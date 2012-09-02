# Yuzen

Yuzen is a static contents generator similar to [jekyll](https://github.com/mojombo/jekyll).

* Yuzen is a [Gradle](http://gradle.org/) Plugin
* Yuzen uses [Thymeleaf](http://www.thymeleaf.org/) for clean template
* Yuzen uses [Bootstrap](http://twitter.github.com/bootstrap/) for design
* Yuzen translates [Markdown](http://daringfireball.net/projects/markdown/) to HTML5
    * Yuzen parses Markdown by [Pegdown](https://github.com/sirthias/pegdown/)
* Yuzen contains some useful templates
    * blog
    * slide
    * site (this one)
* Yuzen publishes contents to ...
    * [GitHub Pages](./publish/githubpages/)
    * [Amazon S3](./publish/s3)
    * [FTPS server](./publish/ftps)


## Requirements
* java7 or more tested by 1.7.0_07
* gradle 1.1
* eclipse 3.7.2 (for development)
    * [Groovy-Eclipse 2.6.1](http://groovy.codehaus.org/Eclipse+Plugin)

## Installation
* Install Java SE 7 [JDK](http://www.oracle.com/technetwork/java/javase/downloads/)
* if you don't have gradle,
    * Download startup archive from [here](https://github.com/taichi/yuzen/downloads)
    * Extract the archive.
        `jar xvf yuzen-startup.zip`  
* if you already use gradle,
    * apply plugin from this site.
        `apply from: 'http://yuzen.koshinuke.org/install'`
    * or copy the below directly into your build.gradle.
        `buildscript {
            repositories {
                mavenLocal()
                mavenCentral()
                mavenRepo url:'https://oss.sonatype.org/content/repositories/releases'
            }
            dependencies {
                classpath 'org.koshinuke:yuzen:0.0.+'
            }
        }
        apply plugin: org.koshinuke.yuzen.YuzenPlugin
        `  
* Modify gradle.properties
    * if you use github features, edit these keys
        * github.username
        * github.password
    * if your network is in firewall, edit these keys
        * systemProp.http.proxyHost
        * systemProp.http.proxyPort
        * systemProp.https.proxyHost
        * systemProp.https.proxyPort

## How to Get Start your Blog
* Initialize template
    `gradlew initBlog`  
* Edit build.gradle for blogging.
    `blog {
         title 'blog title'
         subtitle 'sub title or blog description'
         profile 'profile 'minimum profile for sidemenu'
         // this block uses for atom or rss feed.
         feed {
             syndicationURI 'http://localhost:8080/blog'
             author 'john doe'
         }
     }
    `  
* Write your profile to _contents/profile.md
    * contents files encoding require **UTF-8**
* add first entry
    `gradlew post -Ptitle=HelloWorld`  
* Write your first entry to generated file
* Generate static contents
    `gradlew startBlog`  
* Access to [localhost:8080](http://localhost:8080)
* Check your generated contents
    * if you want to redesign, you should modify **_templates** directory files
        * _templates/page.html is index template. cf. [thymeleaf :: documentation](http://www.thymeleaf.org/documentation.html)
        * _templates/less/main.less is the center of the [less](http://lesscss.org/) files.
* Yuzen has some publishing features, you may use...
    * [GitHub Pages](./publish/githubpages/)
    * [Amazon S3](./publish/s3)
    * [FTPS server](./publish/ftps)


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

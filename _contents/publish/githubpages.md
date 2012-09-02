# How to Publish to GitHub Pages

Yuzen supports [GitHub Pages](http://pages.github.com/)

* Make New Repository for publish [here](https://github.com/new)
    * if you want to use existing repository, you don't do this
* Edit your gradle.properties like the below
    `github.username=[your github username]
    github.password=[your github password]
    `
* Edit build.gradle for publish to GitHub Pages
    `yuzen.publish {
        ghpages repoURI : "https://github.com/[your github account]/[your repository name].git"
    }`  
* Publish to GitHub Pages. yuzen create or update gh-pages branch
    `gradlew publish`  


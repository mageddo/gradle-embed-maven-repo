# Introduction
Resolve your maven dependencies from a standalone embed maven local repository folder(on the project directory for example)

# Setup on gradle  

Create the project folder

	$ mkdir maven

Configure your `build.gradle`

```groovy
buildscript {
	repositories {
		mavenLocal()
		maven {
			url "https://plugins.gradle.org/m2/"
		}
	}
	dependencies{
		classpath "gradle.plugin.com.mageddo:gradle-embed-maven-repo:1.0.7" // from gradle repository
	}
}
apply plugin: 'java'
apply plugin: 'com.mageddo.gradle-embed-maven-repo'
repositories {
	maven {
		url "file://${rootDir}/maven"
	}
	mavenCentral()
}
sourceCompatibility = 1.6
targetCompatibility = 1.6

task createMirror(type: RepoBuilder){
	mavenRepoFolder = file("${rootDir}/maven")
}
```

Build your project to gradle download all needle dependencies to your cache

	gradle clean build
	
Copy all dependencies to your maven embed repository

	gradle createMirror --info

Enjoy :)

# Project Examples
Inside `examples` are some examples of this plugin usage



# Reference Links
* https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/query/ArtifactResolutionQuery.html
* http://stackoverflow.com/questions/13202522/how-to-iterate-gradle-dependencies-in-custom-gradle-plugin

# License 
Licensed under the Apache License, Version 2.0
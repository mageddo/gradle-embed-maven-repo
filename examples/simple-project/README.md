# Introduction
Example project to save all dependencies to a embed maven repository

# Installing
This example resolves the plugin from the local project and not from 
the gradle central (you can change it in `build.gradle` uncomenting the plugin dependency line )

On this file directory follow the steps below

* Firstly lets install the plugin

		$ bash -c "cd ../.. && gradle install"

* Make gradle download all needle jars to gradle cache
	
		$ gradle clean build

* Run the plugin for copy all dependencies jars to the embed repository

		$ gradle createMirror

Enjoy :)
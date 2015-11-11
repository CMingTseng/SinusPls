# **SinusPls**
**A file importer for [SinusBot](https://frie.se/ts3bot/).**

## **Downloading, Contributing, & Building**
[![Build Status](http://ci.ac3-servers.eu/job/SinusPls/badge/icon)](http://ci.ac3-servers.eu/job/SinusPls/)

Download the jar file from [my build server](http://ci.ac3-servers.eu/job/SinusPls/lastSuccessfulBuild/artifact/target/SinusPls.jar).
It requires Java 7 to run, and all classpath dependencies are open source and included.

To contribute, it's in Java, it's got a formatting you can cope with, and it's on github, you should know what to do. I'm not too fussy.

To build, just use maven! `mvn clean install`.

** Well wasn't that simple! **

## **About**
SinusBot is great but due to it's early stages of development is lacking some features. One of those is a file importer. This has been requested since the beginning of development and would be extremely useful! Well here it is!

SinusBot has a somewhat unusual method of storing the files. Using a database to store the data, the file contains the actual music, and a string in the database contains a little more data about the sound file like bitrate, filesize, sample rate ect. Not sure why it does this but hey ho, I can't argue.

This is a Java program thrown together in a couple hours to allow easier importing of these files!

There is one other way I could think about going about this and it would have been utterly disgusting. Authenticating and uploading each music file via post requests. But that's for developers not for the average user.

## **Usage**

I aimed to make this reasonably easy to use. And it's more than easy for someone to make a quick GUI for the java program, so if you can/want; please do!

1.  Stop the SinusBot completely.
2.  Make sure it's stopped. This can have drastic side effects such as data loss.
3.  You need three things to hand
    1.  The music on the local file system and the location of a base folder that they're all in. (We search recursively).
    2.  The key. This is usually found in the folder from the next step. We'll use a5bd5fd7-5b0f-4a67-87ad-5a333913cebf
    3.  The location of the sqlite database. Usually something similar to _{server_root}/data/db/KEY.sqlite_.
    4.  The location of the music file storage folder. Usually in _{serverroot}/data/store/KEY
4.  Decide your parameters.
5.  Execute the command and wait. The more music you have the longer it will take. It may come up with errors, just leave it and it should import everything it can!
6.  You're done. It should all be there.

# **Parameters**

To execute the command it really is simple.

`java -jar SinusBot.jar [options]`

Here's a list of options.
```
usage: java -jar jarfile.jar [options]  
 -?,--help                 Shows this message.

 -db,--dbFile <arg>        The folder to copy the files.  
                           This is required.

 -f,--fix                  Only fix the broken files.

 -in,--inputFolder <arg>   The folder full of mp3 files.  
                           Will use the current dir if not provided.

 -out,--outFolder <arg>    The folder to copy the files.  
                           This is required.

 -v,--verbose              display additional output.
```
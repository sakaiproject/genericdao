Maven 2 is used to build and release the project code (stored in the [SourceRepo](SourceRepo.md))
https://source.sakaiproject.org/maven2/org/sakaiproject/generic-dao/

You can browse the maven 2 repo and download the package manually OR if you are using [maven 2](http://maven.apache.org/), have it manange the dependency for you (as shown below).

If you are using Maven 2 you can include generic-dao in your project by adding the following source repo to your project root pom.xml:
```
<repositories>
...
    <repository>
      <id>sakai-maven</id>
      <name>Sakai Maven Repo</name>
      <layout>default</layout>
            <url>http://source.sakaiproject.org/maven2</url>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>
...
</repositories>
```

Then you would add this (with the latest release version) to the project pom.xml where you define your DAOs:
```
<dependencies>
...
  <dependency>
    <groupId>org.sakaiproject</groupId>
    <artifactId>generic-dao</artifactId>
    <version>[0.9.8,1.0.0)</version>
  </dependency>
...
</dependencies>
```
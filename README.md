= GenericDAO =
This is the generic dao written by Aaron Zeckoski (aaronz@vt.edu)

It is designed to make it easier and faster for developers to write their DAOs
without having to rewrite the same old boring save/delete/etc... functions over
and over for each persistent type but also not having to have dependencies 
in their DAO interfaces. It also allows for good control over which persistent
objects are usable within the DAO and is easy to extend so you can add your own
DAO methods.

Usage may seem complex at first but it is actually not too bad and is highly configurable.
This is not meant to be FULL ORM but it is much more predictable and simply removes as much
busywork from persisting data as possible without getting in your way.

Building this project and putting it in your repository:
Maven1: Simply run "maven jar:install" from the root source directory of this project
Maven2: run "mvn clean install" from the root source directory of this project

There are 3 ways to use the GenericDao depending on your needs. 
1) BasicGenericDao use (minimal methods)
2) GeneralGenericDao use (many more advanced methods)
3) Extended Dao use (advanced methods plus your own)

See the javadoc overview.html file for more details.

More detailed instructions in the Sakai programmers cafe here:
http://confluence.sakaiproject.org/confluence/x/zX8

== Releasing ==

This artifact uses the Sonatype plugin for releasing to the OSS Sonatype repository.

   mvn versions:set -DnewVersion=0.10.0
   git add pom.xml
   git commit -m "Increment version for release"
   mvn clean deploy -Prelease
   git tag 0.10.0
   git push --tags
   mvn versions:set -DnewVersion=0.10.1-SNAPSHOT
   git add pom.xml
   git commit -m "Increment version for development"
   git push


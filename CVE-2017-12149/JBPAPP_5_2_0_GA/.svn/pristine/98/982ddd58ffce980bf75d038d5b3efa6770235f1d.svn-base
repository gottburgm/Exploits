This is an empty basic eclipse project whose contents thirdparty directory with artifacts and a libraries.ent which are populated by maven from artifacts in the maven repository in the "createthirdparty" target in build/build.xml ant script. 

In the early version, EAP4 used a single build/build-thirdparty.xml ant script to populate independently those contents by using the "gen-lib-file" target in tools/etc/jbossbuild, which is not exist in EAP5.

As it built the createthirdparty target inside build.xml, If you want to populate independently this thirdparty directory with artifacts and libraries.ent without building the whole EAP project, you have to run "./build.sh createthirdparty" from the command line.
(notice that users should be sure to execute 'build.sh' rather than 'ant' to ensure the correct version is being used with the correct configuration.)

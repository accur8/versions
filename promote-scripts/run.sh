

mvn --settings=settings.xml org.apache.maven.plugins:maven-gpg-plugin:1.3:sign-and-deploy-file -Dfile=a8-locus_3-0.1.0-20230427_0904_glenziohttp.jar -DpomFile=pom.xml -Pgpg -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh



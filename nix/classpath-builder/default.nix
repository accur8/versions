{
  fetchurl,
  linkFarm,
}: 
  
  let

    name = "jars-classpath";

    listDepsFn = import ./sbt-deps.nix;

    fetcherFn = 
      dep: (
        fetchurl {
            url = dep.url;
           sha256 = dep.sha256;
        }
      );

    mkEntryFromDrv = drv: { name = "lib/${drv.name}"; path = drv; };

    artifacts = listDepsFn fetcherFn;

    classpathBuilder = linkFarm name (map mkEntryFromDrv artifacts);

  in

    classpathBuilder

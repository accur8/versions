{
  fetchurl,
  linkFarm,
  jdk,
  pkgs,
  stdenv,
  unzip,
}: {
  name,
  mainClass,
  sbtDependenciesFn,
  jvmArgs ? [],
  webappExplode ? true,
}:
  
  let

    # name = "a8-sync";
    # mainClass = "a8.sync.demos.LongQueryDemo";
    # sbtDependenciesFn = import ./sbt-deps.nix;
    # jvmArgs = ["-Xmx4g"];
    # webappExplode = true;

    fetcherFn = 
      dep: (
        fetchurl {
            url = dep.url;
           sha256 = dep.sha256;
        }
      );

    artifacts = sbtDependenciesFn fetcherFn;

    linkFarmEntryFn = drv: { name = drv.name; path = drv; };

    classpathBuilder = linkFarm name (map linkFarmEntryFn artifacts);

    jvm_args = builtins.concatStringsSep " " (jvmArgs ++ [mainClass]);

    webappExploder = 
      if webappExplode then
        ''
          echo exploding webapp-composite folder
          for jar in ${classpathBuilder}/*.jar
          do
            ${unzip}/bin/unzip $jar "webapp/*" -d $out/webapp-composite 2> /dev/null 1> /dev/null || true
          done
        ''
      else
        ""
    ;

  in

    stdenv.mkDerivation {
      name = name;
      src = ./.;
      installPhase = ''

        mkdir -p $out/bin

        # create link to jdk
        ln -s ${jdk}/bin/java $out/${name}j

        # create link to lib folder derivation
        ln -s ${classpathBuilder} $out/lib

        # setup launcher script
        cp launcher-cli $out/${name}
        chmod +x $out/${name}
        substituteInPlace $out/bin/${name} \
          --replace _name_ ${name} \
          --replace _out_ $out \
          --replace _jvm_args_ "${jvm_args}"

      '' + webappExploder;
    }

    
#   pkgs.writeShellApplication 
#     {
#       name = "runit.sh";
#       text = ''
#     #!/bin/sh
#     echo hello world
#     ${jdk}/bin/java -cp ${classpathBuilder}/lib/* foo.Bar
# '';
#       runtimeInputs = [classpathBuilder];
#     }

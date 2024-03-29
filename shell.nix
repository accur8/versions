{ system ? builtins.currentSystem }:
let
  sources = import ./nix/sources.nix;

  nixpkgs = import sources.nixpkgs { inherit system; };

  a8-scripts = import sources.a8-scripts { inherit system nixpkgs; };

  my-java = nixpkgs.openjdk11_headless;

  my-scala = nixpkgs.scala.override { jre = my-java; };

  my-sbt = nixpkgs.sbt.override { jre = my-java; };
in
nixpkgs.mkShell {
  buildInputs = [
    a8-scripts.a8-scripts
    my-java
    my-sbt
    my-scala
    nixpkgs.python3
    nixpkgs.niv
  ];
}

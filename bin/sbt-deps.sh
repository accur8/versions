#!/usr/bin/env bash
set -euo pipefail

# Always run the script from the project root
cd "$(dirname "$0")/.."

#echo "Generating SBT Nix deps..."
#sbt run # select a8.versions.GenerateSbtDotNix

echo "Generating classpath derivation for exported JARs..."
#nix-build --out-link lib -E 'with import <nixpkgs> {}; (callPackage ./nix/classpath-builder {}) {jars = (import ./sbt-deps.nix); }'
nix-build --out-link lib -E 'with import <nixpkgs> {}; (callPackage ./nix/classpath-builder {})'

echo "Done!"
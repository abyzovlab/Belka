#!/bin/csh

set VERSION = "v0.1"
set distr   = $PWD/"Belka_"$VERSION".zip"

set tmpdir   = "/tmp"
set belkadir = "belka"
set maindir  = $tmpdir"/"$belkadir
set srcdir   = $maindir"/src"

rm -rf $maindir
mkdir  $maindir
mkdir  $srcdir

make 
make html

cp -r belka         $srcdir
cp -r Jama          $srcdir
cp Belka.java       $srcdir
cp BelkaApplet.java $srcdir
cp BelkaManifest    $srcdir
cp Makefile         $srcdir

cp Belka.jar   $maindir
cp README.txt  $maindir
cp license.rtf $maindir
cp -r doc      $maindir

rm -f $maindir/*~
rm -f $maindir/*/*~
rm -f $maindir/*/*/*~
rm -f $maindir/*/*/*/*~
rm -f $maindir/*/*/*/*/*~
rm -f $maindir/*/*/*/*/*/*~

rm -rf $maindir/.svn
rm -rf $maindir/*/.svn
rm -rf $maindir/*/*/.svn
rm -rf $maindir/*/*/*/.svn
rm -rf $maindir/*/*/*/*/.svn
rm -rf $maindir/*/*/*/*/*/.svn

cd $tmpdir
zip -r $distr  $belkadir
